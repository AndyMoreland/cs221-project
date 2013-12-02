import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalysisMain {
    public static void main(String argv[]) throws ParseException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
        Statement statement = connection.createStatement();
        ResultSet emailResults = statement.executeQuery("SELECT * FROM emails");

        SimpleClassifier simpleClassifier = new SimpleClassifier(connection, Config.EMAIL_ADDRESS);
        CorrectClassifier oracle = new CorrectClassifier(connection, Config.EMAIL_ADDRESS);

        List<Email> training = new ArrayList<Email>();
        List<Email> test = new ArrayList<Email>();
        splitData(Email.parseEmails(emailResults), training, test);

        RainbowClassifier rainbowClassifier = new RainbowClassifier(Config.PROJECT_PATH, training, oracle);
        Classifier combinedClassifier = new CombinedClassifier(rainbowClassifier, simpleClassifier);

        System.out.println("Executing experiment");
        Experiment experiment = new Experiment(oracle, combinedClassifier );
        Statistics stats = experiment.execute(test);

        System.out.println("Precision: " + stats.getPrecision() + " Recall: " + stats.getRecall());
        System.out.println("True positive: " + stats.truePositive);
        System.out.println("True negative: " + stats.trueNegative);
        System.out.println("False positive: " + stats.falsePositive);
        System.out.println("False negative: " + stats.falseNegative);

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
