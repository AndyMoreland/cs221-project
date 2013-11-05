public interface Classifier {
    public enum EmailClass {
        SHOULD_RESPOND_TO,
        SHOULDNT_RESPOND_TO
    }

    public EmailClass classify(Email email);
}
