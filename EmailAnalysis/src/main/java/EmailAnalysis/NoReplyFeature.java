package EmailAnalysis;

public class NoReplyFeature implements Feature {
    @Override
    public double getValue(CleanedEmail email) {
        if (email.getFrom().toLowerCase().contains("no") && email.getFrom().toLowerCase().contains("reply")) {
            return 1.0;
        } else {
            return 0.0;
        }
    }
}
