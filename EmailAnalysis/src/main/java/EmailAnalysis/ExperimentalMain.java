package EmailAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static EmailAnalysis.Classifier.EmailClass;

public class ExperimentalMain {
    public static void main(String argv[]) throws SQLException, IOException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Config.DB_PATH);
        Oracle oracle = new CachedOracle();

        List<CleanedEmail> emails = CleanedEmail.getCleanedEmails(connection);
        BufferedWriter wr = new BufferedWriter(new FileWriter("direct_address.csv"));

        for (CleanedEmail email : emails) {
            if (!email.getFrom().equals("andymo@stanford.edu")) {
                EmailClass trueClass = oracle.classify(email);
                wr.write(((trueClass == EmailClass.SHOULD_RESPOND_TO) ? 1 : 0) + ", " + (email.getContent().contains("andy") ? 1 : 0));
                wr.write("\n");
            }
        }

    }
}
//        int repliedNotToMe = 0;
//        int didntReplyNotToMe = 0;
//        for (CleanedEmail email : emails) {
//            EmailClass trueClass = oracle.classify(email);
//            if (trueClass == EmailClass.SHOULD_RESPOND_TO && !email.getTo().equals("andymo@stanford.edu") && !email.getFrom().equals("andymo@stanford.edu")) {
//                repliedNotToMe++;
//                System.out.println(email.getTo());
//            } else if (trueClass == EmailClass.SHOULDNT_RESPOND_TO && !email.getTo().equals("andymo@stanford.edu")) {
//                didntReplyNotToMe++;
//            }
//        }
//        System.out.println(repliedNotToMe);
//        System.out.println(didntReplyNotToMe);
