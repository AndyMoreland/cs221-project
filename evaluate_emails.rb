#! /usr/bin/env ruby

require 'sqlite3'
require 'sequel'
require 'io/console'
require 'cgi'
require 'set'

DBFILE = "scrape.db"

SELF_EMAIL = "andymo@stanford.edu"

class Experiment

  def initialize
    @db = Sequel.sqlite(DBFILE)
    @emails = @db[:emails]
    @received_emails = @emails.where(:to => SELF_EMAIL)
    @sent_emails = @emails.where(:from => SELF_EMAIL)
    @people_emailed = Set.new(@sent_emails.map { |sent_email| sent_email[:to] }.uniq)
    @true_reply = 0 
    @false_reply = 0
    @true_no_reply = 0
    @false_no_reply = 0
  end

  def will_reply_to?(email)
    @people_emailed.include?(email[:from])
  end

  def did_reply_to?(email)
    future_emails = @emails.where(:thread_id => email[:thread_id])
      .where("timestamp > #{@db.literal(email[:timestamp])}")
      .exclude(:to => SELF_EMAIL)
    future_emails.count > 0
  end

  def execute
    @received_emails.each do |email|
      prediction = will_reply_to?(email)
      actual = did_reply_to?(email)

      if prediction == actual && prediction
        @true_reply += 1
      elsif prediction == actual && !prediction
        @true_no_reply += 1
      elsif prediction != actual && prediction
        @false_reply += 1
      elsif prediction != actual && !prediction
        @false_no_reply += 1
      end
    end

    puts @true_reply
    puts @false_reply
    puts @true_no_reply
    puts @false_no_reply
  end
end


experiment = Experiment.new
experiment.execute
