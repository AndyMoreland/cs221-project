package EmailAnalysis;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class Experiment {
    private final Oracle trueClassifier;
    private final Classifier classifier;

    public List<CleanedEmail> getFalsePositives() {
        return falsePositives;
    }

    public List<CleanedEmail> getFalseNegatives() {
        return falseNegatives;
    }

    private List<CleanedEmail> falsePositives = Lists.newArrayList();
    private List<CleanedEmail> falseNegatives = Lists.newArrayList();

    public Experiment(Oracle trueClassifier, Classifier classifier) {
        this.trueClassifier = trueClassifier;
        this.classifier = classifier;
    }

    public Statistics execute(List<CleanedEmail> emails) {
        Map<Email, Classifier.EmailClass> predictedClasses = classifier.batchClassify(emails);
        Map<Email, Classifier.EmailClass> trueClasses = trueClassifier.batchClassify(emails);

        Statistics stats = new Statistics();
        for (CleanedEmail email : emails) {
            Classifier.EmailClass trueClass = trueClasses.get(email);
            if (!predictedClasses.containsKey(email)) { continue; }
            Classifier.EmailClass predictedClass = predictedClasses.get(email);

            if (predictedClass == Classifier.EmailClass.SHOULD_RESPOND_TO && predictedClass == trueClass) {
                stats.truePositive++;
            } else if (predictedClass == Classifier.EmailClass.SHOULDNT_RESPOND_TO && predictedClass == trueClass) {
                stats.trueNegative++;
            } else if (predictedClass == Classifier.EmailClass.SHOULD_RESPOND_TO && predictedClass != trueClass) {
                stats.falsePositive++;
                falsePositives.add(email);
            } else {
                stats.falseNegative++;
                falseNegatives.add(email);
            }
        }

        return stats;
    }
}
