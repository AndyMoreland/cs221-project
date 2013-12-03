package EmailAnalysis;

/* A classifier which must always return the correct answer. Used for training */
public interface Oracle extends Classifier {
    public EmailClass classify(Email email);
}
