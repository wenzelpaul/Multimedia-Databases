package com.mmdb.datatypes;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * The SearchResult is used to administrate a search result. It also includes the @{@link File} that the similarity search was based on.
 *
 * @author Wenzel Pleyer, Caterina Rotondo, Christoph Stemp, Miriam Deml
 */
public class SearchResult {
    private String indexName;
    private String comparedFile;
    private ArrayList<Hit> hits;

    /**
     * Create a new SearchResult.
     *
     * @param indexName the name of the index that the SearchResult belongs to
     * @param comparedFile the file that the similarity search was based upon
     * @param hits a List of all results for the similarity search of that index, including entry name and percentage @{@link Hit}
     */
    public SearchResult(String indexName, String comparedFile, ArrayList<Hit> hits){
        this.indexName = indexName;
        this.comparedFile = comparedFile;
        this.hits = hits;
    }

    /**
     * Returns the name of the index a SearchResult is related to
     *
     * @return the name of the index
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Sets the name of the index a SearchResult is related to
     *
     * @param indexName the name the index of this SearchResult is supposed to have
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /**
     * Returns the Hits of the index a SearchResult is related to
     *
     * @return the Hits in an ArrayList<Hit></Hit> of the index
     */
    public ArrayList<Hit> getHits() {
        return hits;
    }

    /**
     * Sets the Hits of the index a SearchResult is related to
     *
     * @param hits the Hits the index of this SearchResult is supposed to have
     */
    public void setHits(ArrayList<Hit> hits) {
        this.hits = hits;
    }

    /**
     * Returns the best Hits in an Hit[] in the requested size
     *
     * @param numberOfHits the number of requested search results
     *
     * @return an Hit[] with only the number of requested search result for the compared picture
     */
    public Hit[] getHits(int numberOfHits){
        Hit[] resultHits = new Hit[numberOfHits];

        return (Hit[]) hits.subList(0, numberOfHits -1).toArray();
    }

    /**
     * Returns the name of the compared file of this SearchResult
     *
     * @return the name of the compared file
     */
    public String getComparedFile() {
        return comparedFile;
    }

    /**
     * Sets the name of the compared file of this SearchResult
     *
     * @param comparedFile the name of the compared filee
     */
    public void setComparedFile(String comparedFile) {
        this.comparedFile = comparedFile;
    }

    /**
     * Sorts the HitList according to the comparator which is used for this project (highest to lowest)
     */
    public void sortHitList(){
        hits.sort(Comparator.comparing(Hit::getPercentage));
    }
}
