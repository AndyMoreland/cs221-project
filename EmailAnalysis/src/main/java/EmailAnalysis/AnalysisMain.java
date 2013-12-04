package EmailAnalysis;

import com.google.common.collect.Lists;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
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

        VowpalWabbitClassifier vowpalWabbitClassifier = new VowpalWabbitClassifier(Config.PROJECT_PATH);
        vowpalWabbitClassifier.train(splitter.getTrainingData(), oracle);
        vowpalWabbitClassifier.batchClassify(splitter.getTestData());

        VowpalWabbitClassifier vowpalWabbiTrainClassifier = new VowpalWabbitClassifier(Config.PROJECT_PATH);
        vowpalWabbiTrainClassifier.train(splitter.getTrainingData(), oracle);
        vowpalWabbiTrainClassifier.batchClassify(splitter.getTrainingData());

        List<Feature> features = Lists.newArrayList();
        populateSimpleFeatures(features);
        features.add(new BooleanClassifierFeature(vowpalWabbiTrainClassifier, vowpalWabbitClassifier));
        features.add(new RespondedToFeature(connection));

        WiseRFClassifier wiseRFClassifier = new WiseRFClassifier(Config.PROJECT_PATH, features);
        wiseRFClassifier.train(splitter.getTrainingData(), oracle);

        executeExperiment("WiseRF classifier", oracle, splitter, wiseRFClassifier);
//        executeExperiment("Vowpal Wabbit classifier", oracle, splitter, vowpalWabbitClassifier);
    }

    private static void populateSimpleFeatures(List<Feature> features){
//        features.add(new LengthFeature());
        features.add(new DirectlyToFeature());
        features.add(new UnsubscribeFeature());
        features.add(new NoReplyFeature());
        features.add(new EmailFromNameFeature());
        features.add(new EmailSentBetweenFeature(8, 12));
        features.add(new EmailSentBetweenFeature(12, 16));
        features.add(new EmailSentBetweenFeature(16, 20));
        features.add(new EmailSentBetweenFeature(20, 0));
        features.add(new EmailSentBetweenFeature(0, 4));
        features.add(new EmailSentBetweenFeature(4, 8));
    }

    private static void executeExperiment(String name, Oracle oracle, DataSplitter splitter, Classifier wiseRFClassifier) {
        System.out.println("Executing experiment: " + name);
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
