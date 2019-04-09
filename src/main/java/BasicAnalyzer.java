import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.Analyzer;
import org.tartarus.snowball.ext.RomanianStemmer;

import java.io.*;
import java.net.URL;

public class BasicAnalyzer extends Analyzer {
    private CharArraySet m_stopWords;
    private String m_stopWordsPath;

    BasicAnalyzer(String documentsPath){
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(documentsPath);
        if (url != null) {
            m_stopWordsPath = url.getPath();
            m_stopWords = getStopWordsSet();
        }
        else
        {
            System.out.println("ERROR: wrong document path");
        }
    }

    private CharArraySet getStopWordsSet(){
        CharArraySet stopWords = CharArraySet.EMPTY_SET;

        try {
            File swFile = new File(m_stopWordsPath);
            String fileContent = FileUtils.readFileToString(swFile, "utf-8");
            stopWords = WordlistLoader.getWordSet(new StringReader(Indexer.normalizePhrase(fileContent)));
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        return stopWords;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new WhitespaceTokenizer();
        TokenStream stream = new LowerCaseFilter(tokenizer);
        stream = new ASCIIFoldingFilter(stream);
        stream = new StopFilter(stream, m_stopWords);
        stream = new SnowballFilter(stream, new RomanianStemmer());

        return new TokenStreamComponents(tokenizer, stream);
    }
}
