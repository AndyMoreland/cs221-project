package EmailAnalysis;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class CachedOracle implements Oracle {
    @Override
    public EmailClass classify(CleanedEmail email) {
        if(email.isRepliedTo()) {
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

    @Override
    public EmailClass classify(Email email) {
        System.err.println("SHOULD NOT USE CACHED CLASSIFIER ON REGULAR EMAIL");
        return EmailClass.SHOULD_RESPOND_TO;
    }
}
