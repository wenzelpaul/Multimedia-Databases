package com.mmdb.datatypes;

/**
 * Administrates a single result of the search.
 *
 * @author Wenzel Pleyer, Caterina Rotondo, Christoph Stemp, Miriam Deml
 */
public class Hit {
    private String filepath;
    private Double percentage;

    /**
     * Creates a new @{@link Hit}
     *
     * @param filepath the name of the entry
     * @param percentage the percentage how similiar the entry is to the compared image (see @{@link SearchResult}
     */
    public Hit(String filepath, Double percentage){
        this.filepath = filepath;
        this.percentage = percentage;
    }

    /**
     * Returns the filepath of a Hit, which is used to adminstrate a single search result
     *
     * @return the filepath of a Hit
     */
    public String getFilepath() {
        return filepath;
    }

    /**
     * Sets the filepath of a Hit, which is used to administrate a single search result
     *
     * @param filepath the filepath that the Hit should have
     */
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * Returns the percentage (of similarity) of a Hit, which is used to adminstrate a single search result
     *
     * @return the percentage of a hit
     */
    public Double getPercentage() {
        return percentage;
    }

    /**
     * Sets the percentage (of similarity) of a Hit, which is used to administrate a single search result
     *
     * @param percentage the percentage that the Hit should have
     */
    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

}
