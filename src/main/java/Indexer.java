import org.apache.commons.io.FileUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.RomanianStemmer;

import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.text.Normalizer;

class Indexer {

    private static final String TXT_FILE_EXTENSION = ".txt";
    private static final String PDF_FILE_EXTENSION = ".pdf";
    private static final String DOC_FILE_EXTENSION = ".doc";
    private static final String DOCX_FILE_EXTENSION = ".docx";

    private String m_documentsPath = "";
    private String m_indexPath;
    private BasicAnalyzer m_analyzer;

    private IndexWriter m_writer;

    Indexer(String documentsPath, String indexerPath) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(documentsPath);
        if (url != null) {
            m_documentsPath = url.getPath();
        }
        url = classLoader.getResource(indexerPath);
        if (url != null)
        {
            m_indexPath = url.getPath();
        }
        try {
            m_analyzer = new BasicAnalyzer("romanian_stopwords.txt");
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(m_analyzer);
            Directory indexDirectory = FSDirectory.open(Paths.get(m_indexPath));
            m_writer = new IndexWriter(indexDirectory, indexWriterConfig);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    BasicAnalyzer getAnalyzer() {
        return m_analyzer;
    }

    private void indexAllTxtFiles() {
        FilenameFilter txtFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(TXT_FILE_EXTENSION);
            }
        };

        try {
            File folder = new File(m_documentsPath);
            File[] listOfFiles = folder.listFiles(txtFileFilter);

            ArrayList<Document> documentArrayList = new ArrayList<>();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    Document document = new Document();
                    String file_content = FileUtils.readFileToString(file, "utf-8");
                    document.add(new TextField("file_name", file.getName(), Field.Store.YES));
                    document.add(new TextField("content", file_content, Field.Store.YES));
                    document.add(new TextField("file_type", TXT_FILE_EXTENSION, Field.Store.YES));
                    document.add(new TextField("file_path", file.getPath(), Field.Store.YES));
                    documentArrayList.add(document);
                }
                m_writer.addDocuments(documentArrayList);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * reading line by line
     */
    private void indexAllPdfFiles() {
        FilenameFilter pdfFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(PDF_FILE_EXTENSION);
            }
        };
        try {
            File folder = new File(m_documentsPath);
            File[] listOfFiles = folder.listFiles(pdfFileFilter);

            ArrayList<Document> documentArrayList = new ArrayList<>();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    PDDocument pdfDocument = PDDocument.load(file);
                    if (!pdfDocument.isEncrypted()) {
                        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                        stripper.setSortByPosition(true);
                        PDFTextStripper tStripper = new PDFTextStripper();
                        String pdfFileToText = tStripper.getText(pdfDocument);

                        Document document = new Document();
                        document.add(new TextField("file_name", file.getName(), Field.Store.YES));
                        document.add(new TextField("content", pdfFileToText, Field.Store.YES));
                        document.add(new TextField("file_type", PDF_FILE_EXTENSION, Field.Store.YES));
                        document.add(new TextField("file_path", file.getPath(), Field.Store.YES));
                        documentArrayList.add(document);
                    }
                    pdfDocument.close();
                }
                m_writer.addDocuments(documentArrayList);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void indexAllDocFiles() {
        FilenameFilter pdfFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(DOC_FILE_EXTENSION);
            }
        };

        try {
            File folder = new File(m_documentsPath);
            File[] listOfFiles = folder.listFiles(pdfFileFilter);

            if (listOfFiles != null) {
                ArrayList<Document> documentArrayList = new ArrayList<>();
                for (File file : listOfFiles) {
                    FileInputStream documentInputStream = new FileInputStream(file);
                    HWPFDocument docFile = new HWPFDocument(documentInputStream);
                    WordExtractor wordExtractor = new WordExtractor(docFile);
                    String documentText = wordExtractor.getText();

                    Document document = new Document();
                    document.add(new TextField("file_name", file.getName(), Field.Store.YES));
                    document.add(new TextField("content", documentText, Field.Store.YES));
                    document.add(new TextField("file_type", DOC_FILE_EXTENSION, Field.Store.YES));
                    document.add(new TextField("file_path", file.getPath(), Field.Store.YES));
                    documentArrayList.add(document);
                }
                m_writer.addDocuments(documentArrayList);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void indexAllDocxFiles() {
        FilenameFilter pdfFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(DOCX_FILE_EXTENSION);
            }
        };

        try {
            File folder = new File(m_documentsPath);
            File[] listOfFiles = folder.listFiles(pdfFileFilter);

            if (listOfFiles != null) {
                ArrayList<Document> documentArrayList = new ArrayList<>();
                for (File file : listOfFiles) {
                    FileInputStream documentInputStream = new FileInputStream(file);
                    XWPFDocument docxFile = new XWPFDocument(documentInputStream);
                    XWPFWordExtractor wordExtractor = new XWPFWordExtractor(docxFile);

                    String entireText = wordExtractor.getText();

                    Document document = new Document();
                    document.add(new TextField("file_name", file.getName(), Field.Store.YES));
                    document.add(new TextField("content", entireText, Field.Store.YES));
                    document.add(new TextField("file_type", DOCX_FILE_EXTENSION, Field.Store.YES));
                    document.add(new TextField("file_path", file.getPath(), Field.Store.YES));
                    documentArrayList.add(document);
                }
                m_writer.addDocuments(documentArrayList);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void indexAllFiles() {
        indexAllDocFiles();
        indexAllDocxFiles();
        indexAllPdfFiles();
        indexAllTxtFiles();
        try {
            m_writer.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void deleteIndexes() throws IOException {
        File[] files = new File(m_indexPath).listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Files.delete(file.toPath());
                }
            }
        }
    }

    static String normalizePhrase(String phrase){
        return Normalizer.normalize(phrase.toLowerCase(), Normalizer.Form.NFKD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
