import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalysisMain {
    public static void main(String argv[]) throws ParseException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:/Users/andrew/cs221-project/scrape.db");
        Statement statement = connection.createStatement();
        ResultSet emailResults = statement.executeQuery("SELECT * FROM emails");
        Classifier simpleClassifier = new SimpleClassifier(connection, "andymo@stanford.edu");

        List<Email> training = new ArrayList<Email>();
        List<Email> test = new ArrayList<Email>();
        splitData(Email.parseEmails(emailResults), training, test);

        for (Email email : training) {
            System.out.println(simpleClassifier.classify(email));
        }
    }

    private static void splitData(List<Email> all, List<Email> training, List<Email> test){
        Collections.shuffle(all);
        int i = 0;
        for(; i < all.size() * 0.7; i++){
            training.add(all.get(i));
        }
        for(; i < all.size(); i++){
            test.add(all.get(i));
        }
    }
}
