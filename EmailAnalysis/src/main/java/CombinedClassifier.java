import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class CombinedClassifier implements Classifier {

    private final RainbowClassifier rainbowClassifier;
    private final SimpleClassifier simpleClassifier;

    public CombinedClassifier(RainbowClassifier rainbowClassifier, SimpleClassifier simpleClassifier) {
        this.rainbowClassifier = rainbowClassifier;
        this.simpleClassifier = simpleClassifier;
    }

    private EmailClass combineScores(EmailClass rainbowScore, EmailClass simpleScore) {
        if (rainbowScore == EmailClass.SHOULD_RESPOND_TO && simpleScore == EmailClass.SHOULD_RESPOND_TO) {
            return EmailClass.SHOULD_RESPOND_TO;
        } else {
            return EmailClass.SHOULDNT_RESPOND_TO;
        }
    }

    @Override
    public EmailClass classify(Email email) {
        return combineScores(rainbowClassifier.classify(email), simpleClassifier.classify(email));
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<Email> emails) {
        Map<Email, EmailClass> classes =  Maps.newHashMap();
        Map<Email, EmailClass> rainbowClasses = rainbowClassifier.batchClassify(emails);
        Map<Email, EmailClass> simpleClasses = simpleClassifier.batchClassify(emails);

        for (Email email : emails) {
            if (rainbowClasses.containsKey(email)) {
                classes.put(email, combineScores(rainbowClasses.get(email), simpleClasses.get(email)));
            }
        }

        return classes;
    }
}
