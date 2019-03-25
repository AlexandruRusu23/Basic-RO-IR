import org.apache.commons.io.FileUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.List;

class Indexer {

    private static final String TXT_FILE_EXTENSION = ".txt";
    private static final String PDF_FILE_EXTENSION = ".pdf";
    private static final String DOC_FILE_EXTENSION = ".doc";
    private static final String DOCX_FILE_EXTENSION = ".docx";

    private String m_documentsPath = "";

    Indexer(String documentsPath) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(documentsPath);
        if (url != null) {
            m_documentsPath = url.getPath();
        }
    }

    private void readAllTxtFiles() {
        FilenameFilter txtFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(TXT_FILE_EXTENSION);
            }
        };

        try {
            File folder = new File(m_documentsPath);
            File[] listOfFiles = folder.listFiles(txtFileFilter);

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    String content = FileUtils.readFileToString(file, "utf-8");
                    System.out.println(content);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void readAllPdfFiles() {
        FilenameFilter pdfFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(PDF_FILE_EXTENSION);
            }
        };

        try {
            File folder = new File(m_documentsPath);
            File[] listOfFiles = folder.listFiles(pdfFileFilter);

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    PDDocument document = PDDocument.load(file);
                    if (!document.isEncrypted()) {
                        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                        stripper.setSortByPosition(true);

                        PDFTextStripper tStripper = new PDFTextStripper();

                        String pdfFileInText = tStripper.getText(document);
                        //System.out.println("Text:" + st);

                        // split by whitespace
                        String[] lines = pdfFileInText.split("\\r?\\n");
                        for (String line : lines) {
                            System.out.println(line);
                        }
                    }
                    document.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void readAllDocFiles() {
        FilenameFilter pdfFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(DOC_FILE_EXTENSION);
            }
        };

        try {
            File folder = new File(m_documentsPath);
            File[] listOfFiles = folder.listFiles(pdfFileFilter);

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    FileInputStream documentInputStream = new FileInputStream(file);

                    HWPFDocument docFile = new HWPFDocument(documentInputStream);

                    WordExtractor wordExtractor = new WordExtractor(docFile);
                    String[] fileData = wordExtractor.getParagraphText();

                    for (String paragraph : fileData) {
                        System.out.println(paragraph);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NullPointerException nullEx) {
            nullEx.printStackTrace();
        }
    }

    private void readAllDocxFiles() {
        FilenameFilter pdfFileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(DOCX_FILE_EXTENSION);
            }
        };

        try {
            File folder = new File(m_documentsPath);
            File[] listOfFiles = folder.listFiles(pdfFileFilter);

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    FileInputStream documentInputStream = new FileInputStream(file);
                    XWPFDocument docxFile = new XWPFDocument(documentInputStream);
                    List<XWPFParagraph> paragraphList = docxFile.getParagraphs();

                    for (XWPFParagraph paragraph : paragraphList) {
                        System.out.println(paragraph.getText());
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void readAllFiles() {
        readAllDocFiles();
        readAllDocxFiles();
        readAllPdfFiles();
        readAllTxtFiles();
    }
}
