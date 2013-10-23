#! /usr/bin/env ruby

require 'gmail'
require 'sqlite3'
require 'sequel'
require 'io/console'
require 'cgi'

DBFILE = "scrape.db"

def usage()
  print "Usage: ruby scrape.rb username [password]"
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
      String :timestamp
      Bignum :thread_id
    end
  rescue
    puts "#{DBFILE} already exists. Delete it to re-scrape."
    exit 1
  end

  puts "Working..."
  values = []
  (gmail.inbox.find.concat gmail.mailbox('[Gmail]/Sent Mail').find).each do |email|
    begin
      to = email.to[0]
      from = email.from[0]
      values.push ["#{to.mailbox}@#{to.host}", "#{from.mailbox}@#{from.host}", email.date, email.body.to_s, email.thread_id]
    rescue Exception => e
      puts "Skipping malformed email #{e}"
    end
  end 

  db[:emails].import([:to, :from, :timestamp, :content, :thread_id], values)
  puts "Created #{DBFILE} from #{db[:emails].count} emails."

  gmail.logout
end

main
