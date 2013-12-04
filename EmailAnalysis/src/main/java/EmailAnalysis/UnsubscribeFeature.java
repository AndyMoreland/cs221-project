package EmailAnalysis;

public class UnsubscribeFeature implements Feature {

    @Override
    public double getValue(CleanedEmail email) {
        if (email.getContent().contains("unsubscribe")) {
            return 1.0;
        } else {
            return 0.0;
        }
    }
}
