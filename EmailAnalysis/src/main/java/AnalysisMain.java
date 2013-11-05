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

        Experiment experiment = new Experiment(new CorrectClassifier(connection, "andymo@stanford.edu"), simpleClassifier);
        Statistics stats = experiment.execute(emails);

        System.out.println("Precision: " + stats.getPrecision() + " Recall: " + stats.getRecall());
    }
}
