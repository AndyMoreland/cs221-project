package EmailAnalysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class CleaningMain {

    public static final String CLEANED_EMAILS_TABLE = "cleaned_emails";

    public static void main(String argv[]) {
        try {
            List<Email> emails = null;
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
            emails = Email.getRawEmails(connection);
            EmailCleaner cleaner = new EmailCleanerImpl();

            int i = 0;
            for (Email email : emails) {
                System.out.println("Cleaning email " + i++);
                Email cleanedEmail = cleaner.cleanEmail(email);
                cleanedEmail.saveToCleanTable(connection);
            }
        } catch (SQLException e) {
            System.err.println("Encountered SQL exception while cleaning data. Error was:");
            e.printStackTrace();
        }
    }
}