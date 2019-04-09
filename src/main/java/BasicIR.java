import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BasicIR {

    public static void main(String[] args) throws IOException{
        Indexer indexer = new Indexer("documents", "indexer");
        indexer.indexAllFiles();
        System.out.println("Files are indexed.\n\n");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Query: ");
        String query = reader.readLine();
        while (!query.equals(":quit")) {
            Searcher searcher = new Searcher("indexer/", indexer.getAnalyzer());
            searcher.search(query);
            System.out.print("Query: ");
            query = reader.readLine();
        }

        try {
            indexer.deleteIndexes();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
