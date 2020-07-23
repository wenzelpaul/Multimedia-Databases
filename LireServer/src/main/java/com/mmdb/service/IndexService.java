package com.mmdb.service;

import com.mmdb.datatypes.Hit;
import com.mmdb.datatypes.IndexDAO;
import com.mmdb.datatypes.SearchResult;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * The class IndexService bundles activities relating the indexing of pictures based on the LIRE framework.
 *
 * @author Wenzel Pleyer, Caterina Rotondo, Christoph Stemp, Miriam Deml
 */
public class IndexService {
    private static String path = "Indexes/";
    private final static Logger LOGGER = Logger.getLogger(IndexService.class.getName());
    //initialize the logger
    static {
        try {
            FileHandler fileTxt = new FileHandler("Logging.txt");
            SimpleFormatter formatterTxt = new SimpleFormatter();
            fileTxt.setFormatter(formatterTxt);
            LOGGER.addHandler(fileTxt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Shows all indexes that the current file system has to offer
     *
     * @return a String of all names of the indexes
     */
    public static String showIndexes() {
        String[] indexes = listIndexes();

        return Arrays.toString(indexes);
    }

    /**
     * Returns all index names within a String[]
     *
     * @return a String[] of all indexes
     */
    private static String[] listIndexes() {
        String[] indexes;

        File folder = new File("Indexes");
        if (folder.exists()) {
            //find all file in the Indexes directory (which is used by the create method)
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                indexes = new String[listOfFiles.length];

                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isDirectory()) {
                        indexes[i] = listOfFiles[i].getName();
                    }
                }
            } else {
                indexes = new String[0];
            }
        } else {
            //if the indexes directory doesn't exist, there can't be any created indexes yet
            LOGGER.log(Level.INFO, "There are no indexes yet.");
            indexes = new String[0];
        }

        return indexes;
    }

    /**
     * Checks whether an index exists within the file system
     *
     * @param indexName name of the index that the user wants to check for
     *
     * @return true, if the index exists
     */
    public static boolean exists(String indexName) {
        String name = indexName + "";
        String[] indexes = listIndexes();
        boolean exists = false;
        for (String index : indexes) {
            if (index.equals(name)) {
                exists = true;
            }
        }

        return exists;
    }

    /**
     * Creates an index out of a directory of pictures
     *
     * @param indexName        the name the index is supposed to have. If the name is already taken, the next available number is added
     * @param pictureDirectory the file directory that is supposed to be indexed
     *
     * @return returns the IndexDAO including all entries, or null if the index couldn't be created
     */
    public static IndexDAO createIndex(String indexName, File pictureDirectory) {
        if (pictureDirectory.exists() && pictureDirectory.isDirectory()) {
            // Getting all images from a directory and its sub directories.
            ArrayList<String> images;
            try {
                images = FileUtils.getAllImages(pictureDirectory, true);
                // Creating a FCTH document builder and indexing all files.
                GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(FCTH.class);

                // Creating an Lucene IndexWriter
                Analyzer analyzer = new WhitespaceAnalyzer();
                IndexWriterConfig conf = new IndexWriterConfig(analyzer);
                String tempName = indexName;
                int counter = 0;

                //if the name already exist, add a number
                while (exists(tempName)) {
                    tempName = tempName + counter;
                    ++counter;
                }

                indexName = tempName;
                FSDirectory directory = FSDirectory.open(Paths.get(path + indexName));
                IndexWriter iw = new IndexWriter(directory, conf);
                // Iterating through images building the low level features
                for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
                    String imageFilePath = it.next();

                    try {
                        BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                        File file = new File(imageFilePath);
                        Document document = globalDocumentBuilder.createDocument(img, FilenameUtils.removeExtension(file.getName()));
                        iw.addDocument(document);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "The index could not be created.");
                        return null;
                    }
                }
                // closing the IndexWriter
                iw.close();

                LOGGER.log(Level.INFO, "The index " + indexName + " was created.");
                return new IndexDAO(indexName);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An IO exception occured while trying to create the index " + pictureDirectory.getName() + ": " + e.getMessage());
                return null;
            }
        } else {
            LOGGER.log(Level.INFO, "Directory " + pictureDirectory.getName() + " does not exist or is not a directory.");
            return null;
        }
    }

    /**
     * Creates an index
     *
     * @param indexName the name that the index is supposed to have
     *
     * @return an IndexDAO including the name
     */
    public static IndexDAO createIndex(String indexName){
        try {
            LOGGER.log(Level.INFO, "Entering createIndex.");
            FSDirectory directory = FSDirectory.open(Paths.get(path + indexName));
            LOGGER.log(Level.INFO, "created "+ path + indexName);
            Analyzer analyzer = new WhitespaceAnalyzer();
            IndexWriterConfig conf = new IndexWriterConfig(analyzer);
            IndexWriter iw = new IndexWriter(directory, conf);
            iw.close();
            LOGGER.log(Level.INFO, "The index " + indexName + " was created.");
            return new IndexDAO(indexName);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An IO exception occured while trying to create the index " + indexName + ": " + e.getMessage());
            return null;
        }
    }


    /**
     * Deletes a whole index from the file system.
     *
     * @param indexName the index (name) that is supposed to be deleted.
     *
     * @return returns true, if the deletion within the file system was successful
     */
    public static boolean deleteIndex(String indexName) {
        LOGGER.log(Level.INFO, "Deleting "+ indexName + ".");
        String name = "Indexes/" + indexName + "";
        File file = new File(name);
        if (file.exists() && file.isDirectory()) {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(file);
                LOGGER.log(Level.INFO, "The index " + indexName + " was deleted.");
                return true;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An IO exception occured while trying to delete the index " + indexName + ": " + e.getMessage());
                return false;
            }
        } else {
            LOGGER.log(Level.INFO, "Directory " + indexName + " does not exist or is not a directory.");
            return false;
        }
    }

    /**
     * Truncates an index. That means that all indexes documents are deleted, but the index does still exist as an empty index.
     *
     * @param indexName the name of the index that is supposed to be truncated
     *
     * @return returns true, if the index was successfully truncated (in the file system)
     */
    public static boolean truncate(String indexName) {
        String name = indexName + "";
        FSDirectory directory;
        try {
            directory = FSDirectory.open(Paths.get(path + name));
            IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
            IndexWriter writer = new IndexWriter(directory, config);

            //deletes all entries within the index
            writer.deleteAll();
            writer.close();

            LOGGER.log(Level.INFO, "The index " + indexName + " was truncated.");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An IO exception occured while trying to truncate the index " + indexName + ": " + e.getMessage());
            return false;
        }

    }

    /**
     * Adds an entry to an index by indexing it. This method is designed to only add single image files and not whole directories.
     *
     * @param index     The name of the index that the new entry is supposed to be added to.
     * @param entryname The identifier for the file.
     * @param img      The image file of the new entry.
     *
     * @return returns true, if the insert was successful
     */
    public static boolean insert(String index, String entryname, BufferedImage img) {
        // Creating a FCTH document builder and indexing all files.
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(FCTH.class);

        // Creating an Lucene IndexWriter
        Analyzer analyzer = new WhitespaceAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        String name = index + "";
        try {
            //get the existing directory for the index
            FSDirectory directory = FSDirectory.open(Paths.get(path + name));
            IndexWriter iw = new IndexWriter(directory, conf);
            IndexReader ir = DirectoryReader.open(directory);
            IndexSearcher is = new IndexSearcher(ir);
            TopDocs results = is.search(new TermQuery(new Term("ImageIdentifier", entryname)), 1);
            if (results.totalHits == 0) {
                //use the requested entry name as a name for the image
                Document document = globalDocumentBuilder.createDocument(img, entryname);
                //add the entry to the index
                iw.addDocument(document);

                iw.flush();
                iw.commit();
                iw.forceMerge(1);
                iw.close();
                ir.close();
                directory.close();

                LOGGER.log(Level.INFO, "The file " + entryname + " was  successfully added to the index named " + index + ".");
                return true;
            } else {
                iw.close();
                ir.close();
                directory.close();

                LOGGER.log(Level.SEVERE, "The file " + entryname + " couldn't be added to the index named " + index + ".");
                return false;
            }

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "An exception occured while trying to add the file " + entryname + " to the index named " + index + ":" + e.getMessage());
                return false;
            }
        }

    /**
     * Removes a single image file from an index.
     *
     * @param index     The name of the index that the file should be removed from.
     * @param entryname The image identifier.
     *
     * @return Returns true, if the entry could be deleted from the index in the filepath.
     */
    public static boolean remove(String index, String entryname) {
        File indexFile = new File(path + index);
        if (indexFile.exists() && indexFile.isDirectory()) {
            //only single image files are accepted
            String name = index + "";
            FSDirectory directory;
            try {
                directory = FSDirectory.open(Paths.get(path + name));
                IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
                IndexWriter writer = new IndexWriter(directory, config);
                IndexReader reader = DirectoryReader.open(directory);
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs results = searcher.search(new TermQuery(new Term("ImageIdentifier", entryname)), 1);
                if (results.totalHits == 1) {
                    //LIRE saves the indexed files in fields with the name "ImageIdentifier" and the value of the file name (path)
                    Term term = new Term("ImageIdentifier", entryname);
                    Query query = new TermQuery(term);

                    writer.deleteDocuments(query);

                    writer.commit();
                    reader.close();
                    writer.close();
                    directory.close();

                    LOGGER.log(Level.INFO, "The entry " + entryname + " was  successfully removed from the index named " + index + ".");
                    return true;
                } else {
                    reader.close();
                    writer.close();
                    directory.close();

                    LOGGER.log(Level.SEVERE, "The entry " + entryname + " couldn't be removed from the index named " + index + ".");
                    return false;
                }

            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An exception occured while trying to remove the entry " + entryname + " from the index named " + index + ":" + e.getMessage());
                return false;
            }

        }

        LOGGER.log(Level.SEVERE, "An exception occured while trying to remove the entry " + entryname + " from the index named " + index + ": The index couldn't be found.");
        return false;
    }

    /**
     * Searches for a given image (path) within an index and lists the most similiar entries in that index.
     *
     * @param index the name of the index that is to be searched
     * @param fileStr  the image file of the image that is used for comparisons (as a String that is read by the ImageIO via a FileInputStream)
     *
     * @return returns null, if the search failed, or a SearchResult
     */
    public static SearchResult listSimilarEntries(String index, String fileStr) {
        BufferedImage img = null;
		int counter = 0;
        PrintWriter writer;
        try {
            img = ImageIO.read(new FileInputStream(fileStr));
			writer = new PrintWriter("scores.txt", "UTF-8");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An IOException exception occured while searching the index named " + index + ":" + e.getMessage());
            return null;
        }
        IndexReader ir;
        try {
            ir = DirectoryReader.open(FSDirectory.open(Paths.get(path + index + "")));
            //FCTH is used for the search
            ImageSearcher searcher = new GenericFastImageSearcher(ir.numDocs(), FCTH.class);

            // searching with an image file ...
            ImageSearchHits hits = searcher.search(img, ir);

            ArrayList<Hit> searchHits = new ArrayList<>();
            Hit temp;

            for (int i = 0; i < hits.length(); i++) {
                String fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
				String rowid = new String(Base64.getDecoder().decode(fileName));
                temp = new Hit(fileName, hits.score(i));
				if (counter < 12){
                    writer.println(rowid + ";" + String.valueOf(hits.score(i)));
                    counter++;
                }
				
                searchHits.add(temp);
            }


            ir.close();
			writer.close();
            //create a SearchResult that can be saved within an IndexDAO
            SearchResult searchResult = new SearchResult(index, fileStr, searchHits);
            LOGGER.log(Level.INFO, "The index " + index + " was  successfully searched.");

            return searchResult;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An exception occured while searching the index named " + index + ":" + e.getMessage());
            return null;
        }
    }

    /**
     * Supports the logging
     *
     * @param level the log {@link Level}
     * @param message the message that should be passed to the logger
     */
    public static void toLogger(Level level, String message){
        LOGGER.log(level, "ORACLE --"+ message);
    }
}
