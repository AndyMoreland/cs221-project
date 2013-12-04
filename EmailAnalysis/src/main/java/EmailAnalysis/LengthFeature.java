package EmailAnalysis;

public class LengthFeature implements Feature {

    @Override
    public double getValue(CleanedEmail email) {
        return email.getContent().split(" ").length;
    }
}
