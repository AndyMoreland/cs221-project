package EmailAnalysis;

public class EmailSentBetweenFeature implements Feature {


    private final int begin;
    private final int end;

    public EmailSentBetweenFeature(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public double getValue(CleanedEmail email) {
        int sent = email.getDateTime().getHours();
        boolean fits = false;
        if(begin < end){
            fits = (begin <= sent && sent < end);
        } else {
            fits = (begin <= sent || sent < end);
        }
        return fits ? 1.0 : 0.0;
    }
}
