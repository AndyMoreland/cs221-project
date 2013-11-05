import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.List;

public class RainbowClassifier implements Classifier {
    private String workingDirectory;
    private String modelFilePath;
    private final List<Email> trainingData;
    private Oracle oracle;

    public RainbowClassifier(String workingDirectory, List<Email> trainingData, Oracle oracle) {
        this.workingDirectory = workingDirectory;
        this.trainingData = trainingData;
        this.oracle = oracle;

        trainModel();
    }

    private void trainModel() {
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

                FileWriter writer = writer = new FileWriter(trainingDataFile);
                writer.write(email.getContent());
                writer.close();
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Then we train the model on the files. */
        ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/rainbow", "-d", modelFilePath, "--index", trainingDataDirectoryPath + "/*");
        Process trainingProcess = null;
        try {
            System.out.println("Training model with arguments: " + pb.command().toString());
            trainingProcess = pb.start();
            trainingProcess.waitFor();
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
        ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/rainbow", "-d", modelFilePath, "--query");
        Process classifierProcess = null;
        try {
            classifierProcess = pb.start();
            OutputStream stdIn = classifierProcess.getOutputStream();
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(classifierProcess.getInputStream()));
            stdIn.write(email.getContent().getBytes());
            stdIn.flush();
            stdIn.close();

            String line;
            while ((line = stdOut.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return EmailClass.SHOULD_RESPOND_TO;
    }
}
