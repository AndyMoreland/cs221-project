import com.google.common.collect.Lists;

import java.sql.*;
import java.util.List;

public class SimpleClassifier implements Classifier {
    private Connection connection;
    private String selfEmailAddress;
    private List<String> emailTargets = Lists.newArrayList();

    public SimpleClassifier(Connection connection, String selfEmailAddress) {
        this.connection = connection;
        this.selfEmailAddress = selfEmailAddress;

        computeEmailTargets();
    }

    private void computeEmailTargets() {
        Statement statement;
        try {
            statement = connection.createStatement();
            ResultSet emailTargets = statement.executeQuery("SELECT to FROM emails WHERE from = '" + selfEmailAddress + "';");

            while (emailTargets.next()) {
                this.emailTargets.add(emailTargets.getString(Email.TO_COLUMN));
            }
            System.out.println("Loaded " + this.emailTargets.size() + "email recipients.");
        } catch (SQLException e) {
            System.err.println("While attempting to compute email targets for " + selfEmailAddress);
            e.printStackTrace();
        }
    }

    private boolean hasRespondedToSender(Email email) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("SELECT count(*) as replies from emails WHERE thread_id = ? AND timestamp > ?");
            statement.setInt(0, email.getThreadId());
            statement.setString(1, email.getSqlDateTime());
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
        return EmailClass.SHOULDNT_RESPOND_TO;
    }
}
