package com.mmdb.controller;

import com.mmdb.datatypes.IndexDAO;
import com.mmdb.datatypes.SearchResult;
import com.mmdb.service.IndexService;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * The restController is used as an interface that calls the methods of the @{@link com.mmdb.service.IndexService} responding to a REST call.
 *
 * @author Wenzel Pleyer, Caterina Rotondo, Christoph Stemp, Miriam Deml
 */
@RestController
public class restController {
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
     *Lists all names of currently existing indexes
     *
     * @return a String including all names
     */
    @GetMapping("/index")
    @ResponseBody
    public String getIndex(){
        return com.mmdb.service.IndexService.showIndexes();
    }

    /**
     * Checks whether an index exists within the file system
     *
     * @param index the name you want to check for
     * @return true, if an index with that name exists
     */
    @GetMapping("/index/{indexname}")
    @ResponseBody
    public Boolean exists(@PathVariable("indexname")String index){
        return com.mmdb.service.IndexService.exists(index);
    }

    /**
     * Deletes an index from the file system
     *
     * @param index the name of the index that you want to delete
     * @return true, if the index was successfully deleted
     */
    @DeleteMapping("/index/{indexname}")
    @ResponseBody
    public Boolean deleteIndex(@PathVariable("indexname")String index){
        return com.mmdb.service.IndexService.deleteIndex(index);
    }

    /**
     * Truncates an index, which means that all entries of that index are deleted, but the index is not (empty index)
     *
     * @param index the index that you want to truncate
     * @return true, if the index could be truncated
     */
    @DeleteMapping("index/{indexname}/truncate")
    @ResponseBody
    public Boolean truncate(@PathVariable("indexname") String index) {
        return com.mmdb.service.IndexService.truncate(index);
    }

    /**
     * Removes an entry from an index
     *
     * @param index the name of the index
     * @param path the name of the entry that is supposed to be removed
     * @return true, if the entry could be removed
     */
    @DeleteMapping("index/{indexname}/{entryname}")
    @ResponseBody
    public Boolean remove(@PathVariable("indexname") String index, @PathVariable("entryname") String path) {
        return com.mmdb.service.IndexService.remove(index, path);
    }

    /**
     * Creates an index
     *
     * @param index the name the index is supposed to have
     * @param indexFile the directory that should be indexed
     *
     * @return an @{@link IndexDAO} that includes the name of the index
     */
    @PostMapping("index/{indexname}")
    @ResponseBody
    public IndexDAO createIndex(@PathVariable("indexname") String index, @RequestParam(value = "file", required = false) String indexFile) {
        if (indexFile != null){
            File file = new File(indexFile);
            return com.mmdb.service.IndexService.createIndex(index, file);
        } else {
            return com.mmdb.service.IndexService.createIndex(index);
        }

    }

    /**
     * Adds an entry to an index
     *
     * @param index the index that the entry should be added to
     * @param entryname the name the entry/image should have
     * @param insertedFile the image that is supposed to be added
     * @return true, if the image was successfully added to the index
     */
    @PostMapping("index/{indexname}/{entryname}")
    @ResponseBody
    public Boolean insert(@PathVariable("indexname") String index,@PathVariable("entryname") String entryname, @RequestParam("file") String insertedFile) {
        try {
            BufferedImage img = ImageIO.read(new FileInputStream(insertedFile));
            return com.mmdb.service.IndexService.insert(index, entryname, img);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "This exception occured during the insert method: " + e.getMessage());
            return false;
        }
    }

    /**
     * Does a similarity search for a given file within an index
     *
     * @param index name of the index that is to be searched
     * @param fileStr the image file that should be used for comparisons
     *
     * @return returns a @{@link IndexDAO} which can be saved within the ODCIIndexInterface
     */
    @PostMapping("index/{indexname}/search")
    @ResponseBody
    public SearchResult search(@PathVariable("indexname") String index, @RequestParam("file") String fileStr){
        return com.mmdb.service.IndexService.listSimilarEntries(index, fileStr);
    }

    /**
     * Supports the logging
     *
     * @param strLevel the log {@link Level}
     * @param message the message that should be passed to the logger
     */
    @PostMapping("logger")
    public void toLogger(@RequestParam("level") String strLevel,@RequestParam("message") String message){
        Level level = Level.parse(strLevel);
        com.mmdb.service.IndexService.toLogger(level, message);
    }

}
