import java.util.List;

/** Splits the data into training and test. */
public interface DataSplitter {
    public void splitData(List<Email> emails);

    public List<Email> getTrainingData();
    public List<Email> getTestData();
}
