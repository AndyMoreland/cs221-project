package EmailAnalysis;

public class UnigramFeature implements Feature {

    private final String word;

    public UnigramFeature(String word){
        this.word = word;
    }

    @Override
    public double getValue(CleanedEmail email) {
        if (email.getContent().contains(word)) {
            return 1.0;
        } else {
            return 0.0;
        }
    }
}
