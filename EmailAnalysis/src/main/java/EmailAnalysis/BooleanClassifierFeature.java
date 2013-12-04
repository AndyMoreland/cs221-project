package EmailAnalysis;

public class BooleanClassifierFeature implements Feature {
    private final Classifier trainClassifier;
    private final Classifier testClassifier;

    public BooleanClassifierFeature(Classifier trainClassifier, Classifier testClassifier) {
        this.trainClassifier = trainClassifier;
        this.testClassifier = testClassifier;
    }

    @Override
    public double getValue(CleanedEmail email) {
        Classifier classifier = null;
        if (trainClassifier.classify(email) != null) { classifier = trainClassifier; } else { classifier = testClassifier; }
        if (classifier.classify(email) == Classifier.EmailClass.SHOULD_RESPOND_TO) {
            return 1.0;
        } else {
            return 0.0;
        }
    }
}
