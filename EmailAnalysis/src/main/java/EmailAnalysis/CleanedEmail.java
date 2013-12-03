package EmailAnalysis;

import com.google.common.collect.Lists;

import java.sql.*;
import java.util.List;

public class CleanedEmail extends Email {
    public static final String REPLIED_TO_COLUMN = "replied_to";
    private static final String ORIGINAL_EMAIL_ID_COLUMN = "original_email_id";
    private Email originalEmail;
    private String cleanedContent;

    public void setRepliedTo(boolean repliedTo) {
        this.repliedTo = repliedTo;
    }

    private boolean repliedTo;

    public CleanedEmail(long id, Email originalEmail, String cleanedContent, boolean repliedTo) {
        super(id, originalEmail.getTo(), originalEmail.getFrom(), originalEmail.getThreadId(), originalEmail.getTimestamp(), originalEmail.getContent());
        this.originalEmail = originalEmail;
        this.cleanedContent = cleanedContent;
        this.repliedTo = repliedTo;
    }

    public CleanedEmail(Email originalEmail, String cleanedContent, boolean repliedTo) {
        this(-1, originalEmail, cleanedContent, repliedTo);
    }

    public String getRawContent() {
        return originalEmail.getContent();
    }

    @Override
    public String getContent() {
        return cleanedContent;
    }

    public boolean isRepliedTo() {
        return repliedTo;
    }

    public void saveToCleanTable(Connection connection) throws SQLException {
        PreparedStatement statement;
        if (getId() == -1) {
            statement = connection.prepareStatement("INSERT INTO " +
                "cleaned_emails" + " (\"" + TO_COLUMN + "\",\"" + FROM_COLUMN + "\"," + TIMESTAMP_COLUMN + "," + CONTENT_COLUMN + "," + THREAD_ID_COLUMN + "," + REPLIED_TO_COLUMN + "," + ORIGINAL_EMAIL_ID_COLUMN + ") VALUES (?, ?, ?, ?, ?, ?, ?)");
        } else {
            statement = connection.prepareStatement("UPDATE " +
                "cleaned_emails" + " SET \"" + TO_COLUMN + "\" = ?, \"" + FROM_COLUMN + "\" = ?, " + TIMESTAMP_COLUMN + " = ?," + CONTENT_COLUMN + " = ?," + THREAD_ID_COLUMN + " = ?," + REPLIED_TO_COLUMN + " = ?, " + ORIGINAL_EMAIL_ID_COLUMN + "= ? WHERE id = ?");
        }

        statement.setString(1, getTo());
        statement.setString(2, getFrom());
        statement.setString(3, getTimestamp());
        statement.setString(4, getContent());
        statement.setLong(5, getThreadId());
        statement.setBoolean(6, isRepliedTo());
        statement.setLong(7, originalEmail.getId());
        if (getId() != -1)
            statement.setLong(8, getId());

        statement.execute();
    }

    public static List<CleanedEmail> parseCleanEmails(Connection connection, ResultSet results) {
        List<CleanedEmail> emails = Lists.newArrayList();
        try {
            while (results.next()) {
                Email originalEmail = Email.fetchById(connection, results.getLong(ORIGINAL_EMAIL_ID_COLUMN));
                emails.add(new CleanedEmail(results.getLong(Email.ID_COLUMN), originalEmail, results.getString(Email.CONTENT_COLUMN), results.getBoolean(REPLIED_TO_COLUMN)));
            }
        } catch (SQLException e) {
            System.err.println("While attempting to read emails into memory, got error: ");
            e.printStackTrace();
        }

        return emails;
    }


    public static List<CleanedEmail> getCleanedEmails(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet emailResults = statement.executeQuery("SELECT * FROM cleaned_emails");
        return parseCleanEmails(connection, emailResults);
    }
}
