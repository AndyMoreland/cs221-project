package EmailAnalysis;

import java.util.List;

/* A classifier which is able to be trained on some set of data. Most classifiers are trainable. */
public interface TrainableClassifier extends Classifier {
    public void train(List<CleanedEmail> trainingData, Oracle oracle);
}
