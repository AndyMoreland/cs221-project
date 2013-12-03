package EmailAnalysis;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class EmailCleanerImpl implements EmailCleaner {
    private static final String STOP_WORDS_FILE = "stopwords.txt";
    private Set<String> stopWords;

    public EmailCleanerImpl() {
        stopWords = loadStopWords();
    }

    @Override
    public CleanedEmail cleanEmail(Email email) {
        String content = email.getContent();
        content = removeQuotedConversations(content);
        content = removeAttachments(content);
        content = removeHTMLTags(content);
        content = content.toLowerCase();
        content = removeLinebreaks(content);
        content = removePunctuation(content);
        content = removeSignature(content);

        List<String> words = Lists.newArrayList(content.split("\\s+"));
//        words = removeStopWords(words);
        String newContent = Joiner.on(" ").skipNulls().join(words);

        return new CleanedEmail(email, newContent, false);
    }

    private String removeHTMLTags(String content) {
        return Jsoup.parse(content).text();
//        return content.replaceAll("<p>", "").replaceAll("</p>", "").replaceAll("<span.*?>", "").replaceAll("</span>", "");
    }

    private String removeAttachments(String content) {
        return content.replaceAll("_NextPart.*", "");
    }

    private String removeQuotedConversations(String content) {
        String intermediateContent = content.replaceAll("<blockquote>.*", "");
        return Pattern.compile("^\\s*>.*", Pattern.MULTILINE).matcher(intermediateContent).replaceAll("");
    }

    private String removePunctuation(String content) {
        return content.replaceAll("[^a-zA-Z ]", "");
    }

    private List<String> removeStopWords(List<String> content) {
        for (int i = 0; i < content.size(); i++) {
            if (stopWords.contains(content.get(i))) {
                content.set(i, null);
            }
        }

        return content;
    }

    private String removeSignature(String content) {
        return content;
    }

    private String removeLinebreaks(String content) {
        return content.replace('\n', ' ');
    }

    private Set<String> loadStopWords() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(STOP_WORDS_FILE));
            String line;
            stopWords = Sets.newHashSet();
            while ((line = reader.readLine()) != null) {
                stopWords.add(line);
            }
        } catch (IOException e) {
            System.err.println("Failed to load stop words file.");
            e.printStackTrace();
        }

        return stopWords;
    }
}
