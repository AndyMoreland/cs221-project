package EmailAnalysis;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import static EmailAnalysis.Classifier.EmailClass.SHOULDNT_RESPOND_TO;
import static EmailAnalysis.Classifier.EmailClass.SHOULD_RESPOND_TO;

/**
 * EmailAnalysis.DataSplitter which ignores emails that were sent by the user specified by userEmailAddress
 */
public class UnbalancedDataSplitter implements DataSplitter {
    private List<CleanedEmail> training;
    private List<CleanedEmail> test;
    private String userEmailAddress;
    private Oracle oracle;
    private double ratio;

    /**
     * Construct a EmailAnalysis.DataSplitterImpl
     * @param userEmailAddress Emails sent by this address will be ignored.
     * @param ratio Percentage of training data that should be of the "respondTo" class
     */
    public UnbalancedDataSplitter(String userEmailAddress, Oracle oracle, double ratio) {
        this.userEmailAddress = userEmailAddress;
        this.oracle = oracle;
        this.ratio = ratio;
        training = Lists.newArrayList();
        test = Lists.newArrayList();
    }

    @Override
    public void splitData(List<CleanedEmail> emails) {
        List<CleanedEmail> receivedEmails = getReceivedEmails(emails);
        List<CleanedEmail> respondToEmails = filterEmailsByClass(receivedEmails, SHOULD_RESPOND_TO);
        List<CleanedEmail> noRespondToEmails = filterEmailsByClass(receivedEmails, SHOULDNT_RESPOND_TO);

        System.out.println("Found " + respondToEmails.size() + " positive examples.");
        System.out.println("Found " + noRespondToEmails.size() + " negative examples.");
        System.out.println("Built a training set with " + respondToEmails.size() * 0.7 + " positive examples.");
        System.out.println("Built a training set with " + respondToEmails.size() * 0.7 + " negative examples.");

        Collections.shuffle(receivedEmails);
        int i = 0;
        int j = 0;
        for(; i < respondToEmails.size() * 0.7; i++){
            training.add(respondToEmails.get(i));
        }
        for(; j < respondToEmails.size() * 0.7; j++) {
            training.add(noRespondToEmails.get(j));
        }
        for(; i < respondToEmails.size(); i++){
            test.add(respondToEmails.get(i));
        }
        for(; j < noRespondToEmails.size(); j++) {
            test.add(noRespondToEmails.get(j));
        }
        Collections.shuffle(training);
        Collections.shuffle(test);
    }

    private List<CleanedEmail> filterEmailsByClass(List<CleanedEmail> emails, Classifier.EmailClass requiredClass) {
        List<CleanedEmail> filteredEmails = Lists.newArrayList();
        for (CleanedEmail email : emails) {
            if (oracle.classify(email) == requiredClass) {
                filteredEmails.add(email);
            }
        }
        return filteredEmails;
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

