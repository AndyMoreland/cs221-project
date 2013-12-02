import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DataSplitter which ignores emails that were sent by the user specified by userEmailAddress
 */
public class DataSplitterImpl implements DataSplitter {
    private List<Email> training;
    private List<Email> test;
    private String userEmailAddress;

    /**
     * Construct a DataSplitterImpl
     * @param userEmailAddress Emails sent by this address will be ignored.
     */
    public DataSplitterImpl(String userEmailAddress) {
        this.userEmailAddress = userEmailAddress;
        training = new ArrayList<Email>();
        test = new ArrayList<Email>();
    }

    @Override
    public void splitData(List<Email> emails) {
        List<Email> receivedEmails = getReceivedEmails(emails);

        Collections.shuffle(receivedEmails);
        int i = 0;
        for(; i < receivedEmails.size() * 0.7; i++){
            training.add(receivedEmails.get(i));
        }
        for(; i < receivedEmails.size(); i++){
            test.add(receivedEmails.get(i));
        }
    }

    private List<Email> getReceivedEmails(List<Email> emails) {
        List<Email> receivedEmails = Lists.newArrayList();
        for (Email email : emails) {
            if (!email.getFrom().equals(userEmailAddress)) {
                receivedEmails.add(email);
            }
        }

        return receivedEmails;
    }

    @Override
    public List<Email> getTrainingData() {
        return training;
    }

    @Override
    public List<Email> getTestData() {
        return test;
    }
}
