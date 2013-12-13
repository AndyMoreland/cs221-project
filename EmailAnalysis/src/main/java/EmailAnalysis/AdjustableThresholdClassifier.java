package EmailAnalysis;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class AdjustableThresholdClassifier implements Classifier {
    private ContinuousClassifier wrappedClassifier;

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    private double threshold = 0.5;

    public AdjustableThresholdClassifier(ContinuousClassifier wrappedClassifier) {
        this.wrappedClassifier = wrappedClassifier;
    }

    @Override
    public EmailClass classify(CleanedEmail email) {
        if (wrappedClassifier.getClassificationConfidence(email) > threshold) {
            return EmailClass.SHOULD_RESPOND_TO;
        } else {
            return EmailClass.SHOULDNT_RESPOND_TO;
        }
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<CleanedEmail> emails) {
        Map<Email,EmailClass> classes = Maps.newHashMap();

        for (CleanedEmail email : emails) {
            classes.put(email, classify(email));
        }

        return classes;
    }
}
