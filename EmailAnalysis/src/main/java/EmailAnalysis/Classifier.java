package EmailAnalysis;

import java.util.List;
import java.util.Map;

public interface Classifier {
    public enum EmailClass {
        SHOULD_RESPOND_TO,
        SHOULDNT_RESPOND_TO
    }

    public EmailClass classify(CleanedEmail email);
    public Map<Email, EmailClass> batchClassify(List<CleanedEmail> emails);
}
