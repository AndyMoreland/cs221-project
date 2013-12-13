package EmailAnalysis;

import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

public class WiseRFClassifier implements TrainableClassifier, ContinuousClassifier {

    private static final String WISERF_BINARY = "/Users/andrew/WiseRF/bin/wiserf";
    private static final String WISERF_ROOT = "/Users/andrew/WiseRF";

    private static final String DATA_DIR = "wiserf_data";
    private static final String MODEL_FILENAME = "wiserf.model";
    private static final String TRAINING_DATA_FILENAME = "wiserf_train.csv";
    private static final String TEST_DATA_FILENAME = "wiserf_test.csv";
    private static final Double THRESHOLD = 0.05;

    private Map<CleanedEmail, Double> classificationConfidences = null;
    private final List<Feature> features;
    private String workingDirectory;
    private Oracle oracle;

    public WiseRFClassifier(String workingDirectory, List<Feature> features){
        this.workingDirectory = workingDirectory;
        this.features = features;
    }

    @Override
    public void train(List<CleanedEmail> trainingData, Oracle oracle) {
        this.oracle = oracle;

        deleteOldFiles();

        String trainingDataPath = null;
        try {
            System.out.println("Outputting vw training files.");
            trainingDataPath = writeData(trainingData, TRAINING_DATA_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProcessBuilder pb = new ProcessBuilder(
                WISERF_BINARY, "learn-classifier",
                "--in-file", trainingDataPath,
                "--model-file", workingDirectory + "/" + DATA_DIR + "/" + MODEL_FILENAME,
                "--num-trees", "250",
                "--class-column", "1",
                "--feature-importances-file", "importances.txt"
        );

        Map<String, String> env = pb.environment();
        env.put("WISERF_ROOT", WISERF_ROOT);

        Process trainingProcess = null;
        try {
            System.out.println("Training model with arguments: " + pb.command().toString());
            trainingProcess = pb.start();
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(trainingProcess.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(trainingProcess.getErrorStream()));
            List<String> errorLines = IOUtils.readLines(stdErr);
            for (String errorLine : errorLines) {
                System.err.println("WISERF TRAINING: " + errorLine);
            }
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

    private void deleteOldFiles() {
        File oldModel = new File(DATA_DIR + "/" + MODEL_FILENAME);
        oldModel.delete();
    }

    private String writeData(List<CleanedEmail> source, String filename) throws IOException {
        // Setup filesystem
        String dataDirectoryPath = workingDirectory + "/" + DATA_DIR;
        String dataFilePath = dataDirectoryPath + "/" + filename;

        File dataDirectory = new File(dataDirectoryPath);
        dataDirectory.mkdir();

        // Clear out old data
        File oldDataFile = new File(dataFilePath);
        oldDataFile.delete();
        BufferedWriter dataFile = new BufferedWriter(new FileWriter(dataFilePath));
        System.out.println("Outputting wiserf files to directory: " + dataDirectoryPath);

        // Write data to file
        for (CleanedEmail email : source) {
            // Class and label
            if (oracle.classify(email) == EmailClass.SHOULD_RESPOND_TO) {
                dataFile.write("1");
            } else {
                dataFile.write("0");
            }
            // Features
            for (Feature f : features){
                dataFile.write("," + f.getValue(email));
            }
            dataFile.newLine();
        }

        dataFile.close();
        return dataFilePath;
    }

    @Override
    public EmailClass classify(CleanedEmail email) {
        // Go away woamg
        return null;
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<CleanedEmail> emails) {
        String testDataPath = null;
        Map<Email, EmailClass> results = Maps.newHashMap();
        classificationConfidences = Maps.newHashMap();
        try {
            System.out.println("Outputting wiserf training files.");
            testDataPath = writeData(emails, TEST_DATA_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProcessBuilder pb = new ProcessBuilder(
                WISERF_BINARY, "test-classifier",
                "--in-file", testDataPath,
                "--model-file", workingDirectory + "/" + DATA_DIR + "/" + MODEL_FILENAME,
                "--probabilities-file", "probabilities.txt",
                "--class-column", "1"
        );

        Map<String, String> env = pb.environment();
        env.put("WISERF_ROOT", WISERF_ROOT);

        Process testProcess = null;
        try {
            System.out.println("Testing model with arguments: " + pb.command().toString());
            testProcess = pb.start();
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(testProcess.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(testProcess.getErrorStream()));
            List<String> errorLines = IOUtils.readLines(stdErr);
            for (String errorLine : errorLines) {
                System.err.println("WISERF TESTING: " + errorLine);
            }
            String line;
            for (int i = 0; (line = stdOut.readLine()) != null; i++) {
                System.out.println("WISERF TESTING: " + line);
            }

            int i = 0;
            BufferedReader probReader = new BufferedReader(new FileReader("probabilities.txt"));
            while ((line = probReader.readLine()) != null) {
                String[] probs = line.split(" ");
                Double shouldntRespondProb = Double.parseDouble(probs[0]);
                if (shouldntRespondProb > THRESHOLD) {
                    results.put(emails.get(i), EmailClass.SHOULDNT_RESPOND_TO);
                } else {
                    results.put(emails.get(i), EmailClass.SHOULD_RESPOND_TO);
                }

                classificationConfidences.put(emails.get(i), 1 - shouldntRespondProb);
                i++;
            }
            testProcess.waitFor();
            System.out.println("Testing finished with code: " + testProcess.exitValue());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return results;
    }

    @Override
    public Double getClassificationConfidence(CleanedEmail email) {
        if (classificationConfidences == null) {
            System.err.println("Attempting to fetch classification confidence before classification has occurred.");
        }

        return classificationConfidences.get(email);
    }
}
