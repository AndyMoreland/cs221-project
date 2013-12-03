package EmailAnalysis;

import java.sql.*;
import java.text.ParseException;
import java.util.*;

public class AnalysisMain {
    public static void main(String argv[]) throws ParseException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
        List<Email> emails = Email.getEmails(connection);
        DataSplitter splitter = new DataSplitterImpl(Config.EMAIL_ADDRESS);
        splitter.splitData(emails);

        // SimpleClassifier simpleClassifier = new SimpleClassifier(connection, Config.EMAIL_ADDRESS);
        CorrectClassifier oracle = new CorrectClassifier(connection, Config.EMAIL_ADDRESS);

        // RainbowClassifier rainbowClassifier = new RainbowClassifier(Config.PROJECT_PATH);
        // rainbowClassifier.train(splitter.getTrainingData(), oracle);

        VowpalWabbitClassifier vowpalWabbitClassifier = new VowpalWabbitClassifier(Config.PROJECT_PATH);
        vowpalWabbitClassifier.train(splitter.getTrainingData(), oracle);

        // Classifier combinedClassifier = new CombinedClassifier(rainbowClassifier, simpleClassifier);

        System.out.println("Executing experiment");
        // Experiment experiment = new Experiment(oracle, combinedClassifier );
        Experiment experiment = new Experiment(oracle, vowpalWabbitClassifier);
        // Experiment experiment = new Experiment(oracle, rainbowClassifier);
        Statistics stats = experiment.execute(splitter.getTestData());

        System.out.println("Precision: " + stats.getPrecision() + " Recall: " + stats.getRecall());
        System.out.println("True positive: " + stats.truePositive);
        System.out.println("True negative: " + stats.trueNegative);
        System.out.println("False positive: " + stats.falsePositive);
        System.out.println("False negative: " + stats.falseNegative);
    }
}
