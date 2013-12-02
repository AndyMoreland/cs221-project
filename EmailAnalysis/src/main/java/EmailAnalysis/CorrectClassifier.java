package EmailAnalysis;

import com.google.common.collect.Maps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * This classifier is intended to provide "ground truth" -- it should return the correct answer for all emails passed to it.
 */
public class CorrectClassifier implements Oracle {
    private final Connection connection;
    private final String selfEmail;

    public CorrectClassifier(Connection connection, String selfEmail) {
        this.connection = connection;
        this.selfEmail = selfEmail;
    }


    private boolean didRespondToEmail(Email email) {
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement("SELECT count(*) as replies from emails WHERE thread_id = ? AND timestamp > ?");
            statement.setLong(1, email.getThreadId());
            statement.setString(2, email.getTimestamp());
            ResultSet laterResponses = statement.executeQuery();
            return laterResponses.getInt("replies") > 0;
        } catch (SQLException e) {
            System.err.println("While attempting to check if sender " + email.getFrom() + " was responded to: ");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public EmailClass classify(Email email) {
        if (didRespondToEmail(email)) {
            return EmailClass.SHOULD_RESPOND_TO;
        } else {
            return EmailClass.SHOULDNT_RESPOND_TO;
        }
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<Email> emails) {
        Map<Email, EmailClass> classes = Maps.newHashMap();

        for (Email email : emails) {
            classes.put(email, classify(email));
        }

        return classes;
    }
}
