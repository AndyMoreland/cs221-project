package EmailAnalysis;

public interface Feature {
    /**
     * Returns the value of this feature given an email.
     *
     * @param email
     * @return The value of the feature. No scale is specified.
     */
    public double getValue(CleanedEmail email);
}
