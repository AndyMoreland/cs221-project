import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* You'll need to have the rainbow classifier in /usr/local/bin/rainbow" */
public class RainbowClassifier implements TrainableClassifier {
    public static final String RAINBOW_BINARY = "/usr/local/bin/rainbow";
    private String workingDirectory;
    private String modelFilePath;
    private Oracle oracle;
    private Pattern falsePattern = Pattern.compile(".*\\/(\\d+) shouldRespond shouldntRespond:([0-9.]+) .*");
    private Pattern truePattern = Pattern.compile(".*\\/(\\d+) shouldRespond shouldRespond:([0-9.]+) .*");

    public RainbowClassifier(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void train(List<Email> trainingData, Oracle oracle) {
        String trainingDataDirectoryPath = workingDirectory + "/" + "training_data";
        File trainingDataDirectory = new File(trainingDataDirectoryPath);
        deleteOldStuff(trainingDataDirectory);
        modelFilePath = workingDirectory + "/" + "model";
        File modelFile = new File(modelFilePath);
        deleteOldStuff(modelFile);
        trainingDataDirectory.mkdir();
        File positiveTrainingData = new File(trainingDataDirectoryPath + "/" + "shouldRespond");
        File negativeTrainingData = new File(trainingDataDirectoryPath + "/" + "shouldntRespond");
        positiveTrainingData.mkdir();
        negativeTrainingData.mkdir();

        /* First we write out files corresponding to each email. */
        try {
            System.out.println("Outputting training files to working directory: " + workingDirectory);
            int index = 0;
            for (Email email : trainingData) {
                File trainingDataFile;

                if (oracle.classify(email) == EmailClass.SHOULD_RESPOND_TO) {
                    trainingDataFile = new File(positiveTrainingData + "/" + index);
                } else {
                    trainingDataFile = new File(negativeTrainingData + "/" + index);
                }

                FileWriter writer = new FileWriter(trainingDataFile);
                writer.write(email.getContent());
                writer.close();
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Then we train the model on the files. */
        ProcessBuilder pb = new ProcessBuilder(RAINBOW_BINARY,
                "-d", modelFilePath,
                "--index", trainingDataDirectoryPath + "/shouldRespond", trainingDataDirectoryPath + "/shouldntRespond");
        Process trainingProcess = null;
        try {
            System.out.println("Training model with arguments: " + pb.command().toString());
            trainingProcess = pb.start();
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(trainingProcess.getInputStream()));
            String line;
            while ((line = stdOut.readLine()) != null) {
                System.out.println("Training: " + line);
            }
            trainingProcess.waitFor();
            System.out.println("Training finished with code: " + trainingProcess.exitValue());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void deleteOldStuff(File trainingDataDirectory) {
        if (trainingDataDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(trainingDataDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public EmailClass classify(Email email) {
        /* Not implemented yet. Meh. */
        return EmailClass.SHOULD_RESPOND_TO;
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<Email> emails) {
        String testDirectoryPath = workingDirectory + "/test_data";
        File testDirectory = new File(testDirectoryPath);
        /* this is goofy but required */
        File testDirectory1 = new File(testDirectoryPath + "/shouldRespond/");
        File testDirectory2 = new File(testDirectoryPath + "/shouldntRespond/");
        deleteOldStuff(testDirectory);
        testDirectory.mkdir();
        testDirectory1.mkdir();
        testDirectory2.mkdir();
        for (int i = 0; i < emails.size(); i++) {
            File emailFile = new File(testDirectoryPath + "/shouldRespond/" + i);
            try {
                BufferedWriter emailWriter = new BufferedWriter(new FileWriter(emailFile));
                emailWriter.write(emails.get(i).getContent());
                emailWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String testDataPath = workingDirectory + "/" + "test_data/";
        ProcessBuilder pb = new ProcessBuilder(RAINBOW_BINARY, "-d", modelFilePath, "--test-files", testDataPath);
        System.out.println(pb.command());
        try {
            /* Execute the rainbow classifier and read its results in from stdin */
            Process classifier = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(classifier.getInputStream()));
            Map<Email, EmailClass> classes = Maps.newHashMap();

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                Matcher trueMatcher = truePattern.matcher(line);
                Matcher falseMatcher = falsePattern.matcher(line);

                if (trueMatcher.find()) {
                    classes.put(emails.get(Integer.parseInt(trueMatcher.group(1))), EmailClass.SHOULD_RESPOND_TO);
                } else if (falseMatcher.find()) {
                    classes.put(emails.get(Integer.parseInt(falseMatcher.group(1))), EmailClass.SHOULDNT_RESPOND_TO);
                }
            }

            return classes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Maps.newHashMap();
    }
}
