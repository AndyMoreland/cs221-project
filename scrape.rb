#! /usr/bin/env ruby

require 'gmail'
require 'sqlite3'
require 'sequel'
require 'io/console'
require 'cgi'
require 'thread'

DBFILE = "scrape.db"

class ThreadPool
  def initialize(size)
    @size = size
    @jobs = Queue.new
    @pool = Array.new(@size) do |i|
      Thread.new do
        Thread.current[:id] = i
        catch(:exit) do
          loop do
            job, args = @jobs.pop
            job.call(*args)
          end
        end
      end
    end
  end

  def schedule(*args, &block)
    @jobs << [block, args]
  end

  def shutdown
    @size.times do
      schedule { throw :exit }
    end
    @pool.map(&:join)
  end

  def wait_for_shutdown
    sleep(10) until @jobs.size == 0
    shutdown
  end

end

def usage()
  puts "Usage: ruby scrape.rb username [password]"
  exit 1
end

def main()
  case ARGV.length
  when 0
    usage
  when 1
    print "Password:"
    password = STDIN.noecho(&:gets).chomp # wtf
    puts
  when 2
    password = ARGV[1]
  else
    usage
  end
  username = ARGV[0]

  gmail_client = Gmail::Client::Plain.new(username, password)
  gmail_client.connect!
  gmail = gmail_client

  db = Sequel.sqlite(DBFILE)
  begin
    db.create_table :emails do
      primary_key :id
      
      String :content
      String :to
      String :from
      DateTime :timestamp
      Bignum :thread_id
    end
    db.create_table :cleaned_emails do
      primary_key :id
      
      String :content
      String :to
      String :from
      DateTime :timestamp
      Bignum :thread_id
    end

  rescue
    puts "#{DBFILE} already exists. Delete it to re-scrape."
    exit 1
  end

  puts "Working..."
  values = []

  # Clear out spam
  gmail.mailbox('[Gmail]/Spam').find.each do |email|
    email.delete!
  end

  # Scrape all mail, including sent and archived mail
  mails = gmail.mailbox('[Gmail]/All Mail').find
  pool = ThreadPool.new(32)
  for i in 0..mails.size-1 do
    pool.schedule i do |i|
      puts "Scraped email #{i} out of #{mails.size}"
      email = mails[i]
      begin
        to = email.to ? email.to[0] : email.cc[0]
        from = email.from[0]
        part = (email.multipart? ? (email.text_part || email.html_part) : email)
        body = ""
        unless part.nil?
          body = part.body.decoded
        end
        # Data now cleaned later in dataflow
        # body.gsub!(/<blockquote(\s|\S)*<\/blockquote>/, "") # remove nested conversations
        # body.gsub!(/--[a-f0-9]+--(\s|\S)*/, "") # remove attachments, (--HEXGARBAGE-- and everything after it)

        values.push ["#{to.mailbox}@#{to.host}", "#{from.mailbox}@#{from.host}", DateTime.parse(email.date), body, email.thread_id]
      rescue Exception => e
        puts "Skipping malformed email: #{e}"
      end

      if i % 10 == 0
        db[:emails].import([:to, :from, :timestamp, :content, :thread_id], values)
        values = []
        puts "Importing batch of emails."
      end
    end
  end 

  pool.wait_for_shutdown
  db[:emails].import([:to, :from, :timestamp, :content, :thread_id], values)
  puts "Created #{DBFILE} from #{db[:emails].count} emails."

  gmail.logout
end

main
