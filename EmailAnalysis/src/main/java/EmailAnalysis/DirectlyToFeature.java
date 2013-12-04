package EmailAnalysis;

public class DirectlyToFeature implements Feature {

    @Override
    public double getValue(CleanedEmail email) {
        if (email.getTo().equals(Config.EMAIL_ADDRESS)) {
            return 1.0;
        } else {
            return 0.0;
        }
    }
}
