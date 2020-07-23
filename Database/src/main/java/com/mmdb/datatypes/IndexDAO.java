package com.mmdb.datatypes;

import java.io.Serializable;

/**
 * The IndexDAO is used to administrate an index. Each index has a name (which is not supposed to change) and a search result.
 * The @{@link SearchResult} contains a result for a single similarity search.
 *
 * @author Wenzel Pleyer, Caterina Rotondo, Christoph Stemp, Miriam Deml
 */
public class IndexDAO implements Serializable {
    private String name;
    private SearchResult searchResult;

    /**
     * Creates a new IndexDAO.
     *
     * @param indexName the name of the index
     */
    public IndexDAO(String indexName) {
        this.name = indexName;
    }

    /**
     * Returns the name of an IndexDAO, which is used to administrate an index.
     *
     * @return the name of an IndexDAO
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the SearchResult of an IndexDAO, which is used to administrate an index.
     *
     * @return the SearchResult of an IndexDAO
     */
    public SearchResult getSearchResult() {
        return searchResult;
    }

    /**
     * An IndexDAO can administrate one @{@link SearchResult} of a similarity search.
     *
     * @param searchResult the current SearchResult that in index needs to save
     */
    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }

    /**
     * If the similarity search is supposed to be closed, the current @{@link SearchResult} is removed from the @{@link IndexDAO}.
     */
    public void closeSearchResult(){
        setSearchResult(null);
    }

}
