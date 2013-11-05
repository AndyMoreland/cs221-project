import java.util.List;

public class Experiment {
    private final Oracle trueClassifier;
    private final Classifier classifier;

    public Experiment(Oracle trueClassifier, Classifier classifier) {
        this.trueClassifier = trueClassifier;
        this.classifier = classifier;
    }

    public Statistics execute(List<Email> emails) {
        Statistics stats = new Statistics();
        for (Email email : emails) {
            Classifier.EmailClass trueClass = trueClassifier.classify(email);
            Classifier.EmailClass predictedClass = classifier.classify(email);

            if (predictedClass == Classifier.EmailClass.SHOULD_RESPOND_TO && predictedClass == trueClass) {
                stats.truePositive++;
            } else if (predictedClass == Classifier.EmailClass.SHOULDNT_RESPOND_TO && predictedClass == trueClass) {
                stats.trueNegative++;
            } else if (predictedClass == Classifier.EmailClass.SHOULD_RESPOND_TO && predictedClass != trueClass) {
                stats.falsePositive++;
            } else {
                stats.falseNegative++;
            }
        }

        return stats;
    }
}
