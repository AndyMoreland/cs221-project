package EmailAnalysis;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EmailAnalysis.DataSplitter which ignores emails that were sent by the user specified by userEmailAddress
 */
public class DataSplitterImpl implements DataSplitter {
    private List<CleanedEmail> training;
    private List<CleanedEmail> test;
    private String userEmailAddress;

    /**
     * Construct a EmailAnalysis.DataSplitterImpl
     * @param userEmailAddress Emails sent by this address will be ignored.
     */
    public DataSplitterImpl(String userEmailAddress) {
        this.userEmailAddress = userEmailAddress;
        training = new ArrayList<CleanedEmail>();
        test = new ArrayList<CleanedEmail>();
    }

    @Override
    public void splitData(List<CleanedEmail> emails) {
        List<CleanedEmail> receivedEmails = getReceivedEmails(emails);

        Collections.shuffle(receivedEmails);
        int i = 0;
        for(; i < receivedEmails.size() * 0.7; i++){
            training.add(receivedEmails.get(i));
        }
        for(; i < receivedEmails.size(); i++){
            test.add(receivedEmails.get(i));
        }
    }

    private List<CleanedEmail> getReceivedEmails(List<CleanedEmail> emails) {
        List<CleanedEmail> receivedEmails = Lists.newArrayList();
        for (CleanedEmail email : emails) {
            if (!email.getFrom().equals(userEmailAddress)) {
                receivedEmails.add(email);
            }
        }

        return receivedEmails;
    }

    @Override
    public List<CleanedEmail> getTrainingData() {
        return training;
    }

    @Override
    public List<CleanedEmail> getTestData() {
        return test;
    }
}
