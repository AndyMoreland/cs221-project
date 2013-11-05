import com.google.common.collect.Lists;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
    private final Timestamp timestamp;
    private final String content;



    public String getContent() {
        return content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Email(String to, String from, int threadId, Timestamp timestamp, String content) {
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
                        results.getTimestamp(TIMESTAMP_COLUMN),
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
}
