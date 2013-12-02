package EmailAnalysis;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleClassifier implements Classifier {
    private Connection connection;
    private String selfEmailAddress;
    private Set<String> emailTargets = Sets.newHashSet();

    public SimpleClassifier(Connection connection, String selfEmailAddress) {
        this.connection = connection;
        this.selfEmailAddress = selfEmailAddress;

        computeEmailTargets();
    }

    private void computeEmailTargets() {
        Statement statement;
        try {
            statement = connection.createStatement();
            ResultSet emailTargets = statement.executeQuery("SELECT `to` FROM emails WHERE `from` = '" + selfEmailAddress + "';");

            while (emailTargets.next()) {
                this.emailTargets.add(emailTargets.getString(Email.TO_COLUMN));
            }
            System.out.println("Loaded " + this.emailTargets.size() + " email recipients.");
        } catch (SQLException e) {
            System.err.println("While attempting to compute email targets for " + selfEmailAddress);
            e.printStackTrace();
        }
    }

    @Override
    public EmailClass classify(Email email) {
        if (emailTargets.contains(email.getFrom())) {
            return EmailClass.SHOULD_RESPOND_TO;
        } else {
            return EmailClass.SHOULDNT_RESPOND_TO;
        }
    }

    @Override
    public Map<Email, EmailClass> batchClassify(List<Email> emails) {
        Map<Email, EmailClass> classes = Maps.newHashMap();

        for (Email email : emails) {
            classes.put(email, classify(email));
        }

        return classes;
    }
}
