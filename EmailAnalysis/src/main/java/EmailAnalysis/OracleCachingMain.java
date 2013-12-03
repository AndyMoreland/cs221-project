package EmailAnalysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class OracleCachingMain {
    public static void main(String argv[]) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
            List<CleanedEmail> emails = CleanedEmail.getCleanedEmails(connection);
            CorrectClassifier oracle = new CorrectClassifier(connection, Config.EMAIL_ADDRESS);

            int i = 0;
            for (CleanedEmail email : emails) {
                System.out.println("Caching oracle values for email: " + i++);
                email.setRepliedTo(oracle.classify(email) == Classifier.EmailClass.SHOULD_RESPOND_TO);
                email.saveToCleanTable(connection);
            }
        } catch (SQLException e) {
            System.err.println("Encountered SQL exception while cleaning data. Error was:");
            e.printStackTrace();
        }

    }
}
