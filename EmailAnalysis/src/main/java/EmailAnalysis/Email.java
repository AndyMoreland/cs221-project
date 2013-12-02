package EmailAnalysis;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class Email {
    public static final String TO_COLUMN = "to";
    public static final String FROM_COLUMN = "from";
    public static final String TIMESTAMP_COLUMN = "timestamp";
    public static final String CONTENT_COLUMN = "content";
    public static final String THREAD_ID_COLUMN = "thread_id";

    private final String to;
    private final String from;
    private final long threadId;
    private final String timestamp;
    private final String content;

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Email(String to, String from, long threadId, String timestamp, String content) {
        this.to = to;
        this.from = from;
        this.threadId = threadId;
        this.timestamp = timestamp;
        this.content = content;
    }

    public long getThreadId() {
        return threadId;
    }

    private DateTime getDateTime(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZ");
        SimpleDateFormat secondFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss ZZZZ");

        try {
            return new DateTime(format.parse(dateString));
        } catch (ParseException e) {
            try {
                return new DateTime(secondFormat.parse(dateString));
            } catch (ParseException e1) {
                System.err.println("Failed to parse datetime: " + dateString);
                return null;
            }
        }
    }

    public static List<Email> parseEmails(ResultSet results) {
        List<Email> emails = Lists.newArrayList();
        try {
            while (results.next()) {
                emails.add(new Email(
                        results.getString(TO_COLUMN),
                        results.getString(FROM_COLUMN),
                        results.getLong(THREAD_ID_COLUMN),
                        results.getString(TIMESTAMP_COLUMN),
                        results.getString(CONTENT_COLUMN)
                ));
            }
        } catch (SQLException e) {
            System.err.println("While attempting to read emails into memory, got error: ");
            e.printStackTrace();
        }

        return emails;
    }

    public static List<Email> getEmails(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet emailResults = statement.executeQuery("SELECT * FROM cleaned_emails");
        return Email.parseEmails(emailResults);
    }

    public static List<Email> getRawEmails(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet emailResults = statement.executeQuery("SELECT * FROM emails");
        return Email.parseEmails(emailResults);
    }

    public void saveToCleanTable(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO " +
                "cleaned_emails" + " (\"" + TO_COLUMN + "\",\"" + FROM_COLUMN + "\"," + TIMESTAMP_COLUMN + "," + CONTENT_COLUMN + "," + THREAD_ID_COLUMN + ") VALUES (?, ?, ?, ?, ?)");

        statement.setString(1, getTo());
        statement.setString(2, getFrom());
        statement.setString(3, getTimestamp());
        statement.setString(4, getContent());
        statement.setLong(5, getThreadId());

        statement.execute();
    }
}
