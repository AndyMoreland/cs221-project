package EmailAnalysis;

import java.util.List;
import java.util.Map;

public class HueristicClassifier implements Classifier {
    private Classifier classifier;
    private Oracle oracle;

    public HueristicClassifier(Classifier classifier, Oracle oracle) {
        this.classifier = classifier;
        this.oracle = oracle;
    }

    @Override
    public EmailClass classify(CleanedEmail email) {
        return null;
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<CleanedEmail> emails) {
        Map<Email, EmailClass> classes = classifier.batchClassify(emails);
        int correct = 0;
        int incorrect = 0;
        for (Email email : classes.keySet()) {
            if(email.getContent().split(" ").length > 100) {
                if (oracle.classify((CleanedEmail) email) == EmailClass.SHOULDNT_RESPOND_TO) {
                    correct++;
                } else {
                    incorrect++;
                }
                classes.put(email, EmailClass.SHOULDNT_RESPOND_TO);
            }
        }
        System.out.println("Correctly discarded " + correct + " and incorrectly discard " + incorrect);

        return classes;
    }
}
