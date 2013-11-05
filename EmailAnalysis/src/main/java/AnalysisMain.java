import java.sql.*;
import java.text.ParseException;
import java.util.List;

public class AnalysisMain {
    public static void main(String argv[]) throws ParseException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:/Users/andrew/cs221-project/scrape.db");
        Statement statement = connection.createStatement();
        ResultSet emailResults = statement.executeQuery("SELECT * FROM emails");
        Classifier simpleClassifier = new SimpleClassifier(connection, "andymo@stanford.edu");

        List<Email> emails = Email.parseEmails(emailResults);

        for (Email email : emails) {
            System.out.println(simpleClassifier.classify(email));
        }
    }
}
