package EmailAnalysis;

public class BooleanClassFeature implements Feature {
    private Classifier classifier;

    public BooleanClassFeature(Classifier classifier) {
        this.classifier = classifier;
    }

    @Override
    public float getValue(CleanedEmail email) {
        return 0;
    }
}
