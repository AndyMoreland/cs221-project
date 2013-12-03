package EmailAnalysis;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VowpalWabbitClassifier implements TrainableClassifier
{
    public static final String VW_BINARY = "/usr/local/bin/vw";

    private static final String DATA_DIR = "vw_data";
    private static final String TRAINING_DATA_FILENAME = "vw_training_data.txt";
    private static final String MODEL_FILENAME = "vw.model";
    private static final String TEST_DATA_FILENAME = "vw_test_data.txt";
    private static final Double THRESHOLD = 0.9;

    private String workingDirectory;
    private Oracle oracle;
    private Map<Email,EmailClass> results;


    public VowpalWabbitClassifier(String workingDirectory){
        this.workingDirectory = workingDirectory;
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

        // "-p", "/dev/stdout", "--quiet"
        ProcessBuilder pb = new ProcessBuilder(
                VW_BINARY,
                "--cache_file", "train.cache",
                "-d", trainingDataPath,
                "-f", MODEL_FILENAME,
//                "-k", "--invert_hash", MODEL_FILENAME + ".readable",
                "--passes", "25",
                "-q", "mm");
        Process trainingProcess = null;
        try {
            System.out.println("Training model with arguments: " + pb.command().toString());
            trainingProcess = pb.start();
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(trainingProcess.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(trainingProcess.getErrorStream()));
            List<String> errorLines = IOUtils.readLines(stdErr);
            for (String errorLine : errorLines) {
                System.err.println("VOWPAL WABBIT TRAINING: " + errorLine);
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
        File testCache = new File("test.cache");
        File trainCache = new File("train.cache");
        File modelFile = new File(MODEL_FILENAME);

        testCache.delete();
        trainCache.delete();
        modelFile.delete();
    }

    private String writeData(List<CleanedEmail> source, String filename) throws IOException {
    // Setup filesystem
        String dataDirectoryPath = workingDirectory + "/" + DATA_DIR;
        String dataFilePath = dataDirectoryPath + "/" + filename;

        File trainingDataDirectory = new File(dataDirectoryPath);
        trainingDataDirectory.mkdir();

        // Clear out old data
        File oldDataFile = new File(dataFilePath);
        oldDataFile.delete();
        BufferedWriter dataFile = new BufferedWriter(new FileWriter(dataFilePath));
        System.out.println("Outputting vw files to directory: " + dataDirectoryPath);

        // Write data to file
        for (CleanedEmail email : source) {
            // Class and label
            if (oracle.classify(email) == EmailClass.SHOULD_RESPOND_TO) {
                dataFile.write("1 shouldrespond|");
            } else {
                dataFile.write("-1 shouldntrespond|");
            }
            // Namespace
            dataFile.write("messagebody ");
            // Features
            dataFile.write(email.getContent());
            dataFile.newLine();
        }

        dataFile.close();
        return dataFilePath;
    }

    @Override
    public EmailClass classify(CleanedEmail email) {
        if (results == null) {
            System.err.println("Attempting to use vowpal wabbit classifier before running batch classification is incorrect.");
            return null;
        }

        return results.get(email);
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<CleanedEmail> emails) {
        String testDataPath = null;
        try {
            System.out.println("Outputting vw test files.");
            testDataPath = writeData(emails, TEST_DATA_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        results = new HashMap<Email, EmailClass>();
        ProcessBuilder pb = new ProcessBuilder(VW_BINARY, "-t", "--cache_file", "test.cache",
                "-i", MODEL_FILENAME, "-d", testDataPath, "-p", "/dev/stdout", "--quiet");
        Process trainingProcess = null;
        try {
            System.out.println("Testing model with arguments: " + pb.command().toString());
            trainingProcess = pb.start();
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(trainingProcess.getInputStream()));

            String line;
            for (int i = 0; (line = stdOut.readLine()) != null; i++) {
                double predictionValue = Double.parseDouble(line.substring(0, line.indexOf(" ")));
                if (predictionValue > THRESHOLD){
                    results.put(emails.get(i), EmailClass.SHOULD_RESPOND_TO);
                } else {
                    results.put(emails.get(i), EmailClass.SHOULDNT_RESPOND_TO);
                }
            }
            trainingProcess.waitFor();
            System.out.println("Testing finished with code: " + trainingProcess.exitValue());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return results;
    }
}
