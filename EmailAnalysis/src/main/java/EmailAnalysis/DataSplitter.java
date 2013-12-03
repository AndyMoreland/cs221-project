package EmailAnalysis;

import java.util.List;

/** Splits the data into training and test. */
public interface DataSplitter {
    public void splitData(List<CleanedEmail> emails);

    public List<CleanedEmail> getTrainingData();
    public List<CleanedEmail> getTestData();
}
