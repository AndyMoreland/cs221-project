package test;

import EmailAnalysis.CorrectClassifier;
import EmailAnalysis.Email;
import EmailAnalysis.EmailCleaner;
import EmailAnalysis.EmailCleanerImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EmailCleanerImplTest {
    private EmailCleaner cleaner = new EmailCleanerImpl();

    @Test
    public void testRemovesPunctuation() {
        Email punctuatedEmail = createDummyEmailWithContent("hello. there.? this is cool.!9");
        assertEquals("cool", cleaner.cleanEmail(punctuatedEmail).getContent());
    }

    @Test
    public void testLowercasesString() {
        Email uppercaseEmail = createDummyEmailWithContent("PERPLEXING DISCOVERY");
        assertEquals("perplexing discovery", cleaner.cleanEmail(uppercaseEmail).getContent());
    }

    @Test
    public void testRemovesLinebreaks() {
        Email linebreakyEmail = createDummyEmailWithContent("intensely\nexciting\nwords");
        assertEquals("intensely exciting words", cleaner.cleanEmail(linebreakyEmail).getContent());
    }

    @Test
    public void testRemovesBasicHTML() {
        Email htmlEmail = createDummyEmailWithContent("intense <span>excitation</span>");
        assertEquals("intense excitation", cleaner.cleanEmail(htmlEmail).getContent());
    }

    private static Email createDummyEmailWithContent(String content) {
        return new Email("", "", 0, "", content, false);
    }
}
