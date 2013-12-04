package EmailAnalysis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RespondedToFeature implements Feature {
    private Connection connection;

    public RespondedToFeature(Connection connection) {
        this.connection = connection;
    }

    @Override
    public double getValue(CleanedEmail email) {
        String sender = email.getFrom();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT count(*) as previous_replies from cleaned_emails WHERE \"to\" = ? AND timestamp < ? AND \"from\" = ?;");
            statement.setString(1, sender);
            statement.setString(2, email.getTimestamp());
            statement.setString(3, Config.EMAIL_ADDRESS);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                double previous_reply_count = resultSet.getLong("previous_replies");

                return previous_reply_count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
