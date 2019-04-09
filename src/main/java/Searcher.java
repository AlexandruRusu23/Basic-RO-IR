import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

import java.net.URL;

class Searcher {

    private IndexSearcher m_indexSearcher;
    private QueryParser m_queryParser;

    private String m_indexPath;

    Searcher(String indexerPath, BasicAnalyzer analyzer){
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(indexerPath);
        if (url != null)
        {
            m_indexPath = url.getPath();
        }
        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(m_indexPath));
            DirectoryReader directoryReader = DirectoryReader.open(indexDirectory);
            m_indexSearcher = new IndexSearcher(directoryReader);
            m_queryParser = new QueryParser("content", analyzer);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void search(String query){
        long startTimestamp = System.currentTimeMillis();
        try {
            TopDocs topDocs = getTopDocs(query);
            long endTimestamp = System.currentTimeMillis();

            if(topDocs.totalHits.value > 0)
            {
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document document;
                    try {
                        document = m_indexSearcher.doc(scoreDoc.doc);

                        System.out.println("filename: " + document.get("file_name"));
                        System.out.println("file type: " + document.get("file_type"));
                        System.out.println("file path: " + document.get("file_path"));
                        System.out.println("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Time to retrieve: " + (endTimestamp - startTimestamp));
            }
            else {
                System.out.println("No files.");
            }
        }
        catch (ParseException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private TopDocs getTopDocs(String searchQuery) throws ParseException, IOException {
        Query query = m_queryParser.parse(searchQuery);
        return m_indexSearcher.search(query, 5);
    }
}
