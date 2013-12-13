package EmailAnalysis;

public interface ContinuousClassifier {
    /**
     * Get the probability that the given email should be responded to.
     * @param email
     * @return Probability that the email ought to be responded to.
     */
    public Double getClassificationConfidence(CleanedEmail email);
}
