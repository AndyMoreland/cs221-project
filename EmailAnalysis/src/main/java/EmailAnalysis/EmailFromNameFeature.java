package EmailAnalysis;

import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

public class EmailFromNameFeature implements Feature {

    private static final String NAMES_FILENAME = "names.txt";
    private final Set<String> names;

    public EmailFromNameFeature() {
        BufferedReader namesFile;
        Set<String> names = Sets.newHashSet();
        try {
            namesFile = new BufferedReader(new FileReader(NAMES_FILENAME));
            String line;
            while((line = namesFile.readLine()) != null){
                if(line.length() > 2) names.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.names = names;
    }

    @Override
    public double getValue(CleanedEmail email) {
        return names.contains(email.getFrom()) ? 1.0 : 0.0;
    }
}
