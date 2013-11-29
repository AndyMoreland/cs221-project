import java.util.List;
import java.util.Map;

public class VowpalWabbitClassifier implements Classifier
{
    @Override
    public EmailClass classify(Email email) {
        return null;
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<Email> emails) {
        return null;
    }
}
