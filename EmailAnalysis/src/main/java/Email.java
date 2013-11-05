import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private final int threadId;
    private final DateTime timestamp;
    private final String content;

    public String getContent() {
        return content;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Email(String to, String from, int threadId, DateTime timestamp, String content) {
        this.to = to;
        this.from = from;
        this.threadId = threadId;
        this.timestamp = timestamp;
        this.content = content;
    }

    public static List<Email> parseEmails(ResultSet results) {
        List<Email> emails = Lists.newArrayList();
        try {
            while (results.next()) {
                emails.add(new Email(
                        results.getString(TO_COLUMN),
                        results.getString(FROM_COLUMN),
                        results.getInt(THREAD_ID_COLUMN),
                        parseDate(results.getString(TIMESTAMP_COLUMN)),
                        results.getString(CONTENT_COLUMN)
                ));
            }
        } catch (SQLException e) {
            System.err.println("While attempting to read emails into memory, got error: ");
            e.printStackTrace();
        }

        return emails;
    }

    public int getThreadId() {
        return threadId;
    }

    private static DateTime parseDate(String dateString) {
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

    public static String getSqlDateTime(DateTime dateTime) {
        DateTimeFormatter isoFormatter = ISODateTimeFormat.dateTimeParser();
        StringWriter writer = new StringWriter();
        try {
            isoFormatter.printTo(writer, dateTime);
            return writer.toString();
        } catch (IOException e) {
            System.err.println("Failed to format " + dateTime);
            return null;
        }
    }
}
