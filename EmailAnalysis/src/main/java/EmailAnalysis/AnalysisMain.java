package EmailAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class AnalysisMain {
    public static void main(String argv[]) throws ParseException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
        Oracle oracle = new CachedOracle();
        List<CleanedEmail> emails = CleanedEmail.getCleanedEmails(connection);
        DataSplitter splitter = new UnbalancedDataSplitter(Config.EMAIL_ADDRESS, oracle, 0.5);
        splitter.splitData(emails);

//        SimpleClassifier simpleClassifier = new SimpleClassifier(connection, Config.EMAIL_ADDRESS);

//        RainbowClassifier rainbowClassifier = new RainbowClassifier(Config.PROJECT_PATH);
//        rainbowClassifier.train(splitter.getTrainingData(), oracle);

//        VowpalWabbitClassifier vowpalWabbitClassifier = new VowpalWabbitClassifier(Config.PROJECT_PATH);
//        vowpalWabbitClassifier.train(splitter.getTrainingData(), oracle);

        WiseRFClassifier wiseRFClassifier = new WiseRFClassifier(Config.PROJECT_PATH, new ArrayList<Feature>());
        wiseRFClassifier.train(splitter.getTrainingData(), oracle);

//        HueristicClassifier hueristicClassifier = new HueristicClassifier(vowpalWabbitClassifier, oracle);

//        Classifier combinedClassifier = new CombinedClassifier(rainbowClassifier, simpleClassifier);

        System.out.println("Executing experiment");
//        Experiment experiment = new Experiment(oracle, vowpalWabbitClassifier);
//        Experiment experiment = new Experiment(oracle, rainbowClassifier);
//        Experiment experiment = new Experiment(oracle, combinedClassifier);
        Experiment experiment = new Experiment(oracle, wiseRFClassifier);

        Statistics stats = experiment.execute(splitter.getTestData());

        System.out.println("Precision: " + stats.getPrecision() + " Recall: " + stats.getRecall());
        System.out.println("True positive: " + stats.truePositive);
        System.out.println("True negative: " + stats.trueNegative);
        System.out.println("False positive: " + stats.falsePositive);
        System.out.println("False negative: " + stats.falseNegative);

        writeIncorrectlyClassifiedEmails(experiment);
    }

    private static void writeIncorrectlyClassifiedEmails(Experiment experiment) {
        try {
            BufferedWriter fpWr = new BufferedWriter(new FileWriter("false_positives.txt"));
            BufferedWriter fnWr = new BufferedWriter(new FileWriter("false_negatives.txt"));

            for (CleanedEmail falsePositive : experiment.getFalsePositives()) {
                fpWr.write(falsePositive.getContent() + "\n \n");
            }

            for (CleanedEmail falseNegative : experiment.getFalseNegatives()) {
                fnWr.write(falseNegative.getContent() + "\n \n");
            }
            fpWr.close();
            fnWr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
