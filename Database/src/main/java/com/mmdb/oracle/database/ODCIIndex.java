package com.mmdb.oracle.database;

import com.mmdb.datatypes.*;
import oracle.CartridgeServices.ContextManager;
import oracle.CartridgeServices.CountException;
import oracle.CartridgeServices.InvalidKeyException;
import oracle.ODCI.*;
import oracle.jdbc.driver.OracleConnection;
import oracle.jdbc.driver.OracleDriver;
import oracle.jpub.runtime.MutableStruct;
import oracle.sql.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;

/**
 * The ODCIIndex interface specifies all routines to implement an
 * indextype within an oracle database structure.
 * In this case it is used to operate the data flow between the REST webservice and the database.
 *
 * @author Wenzel Pleyer, Caterina Rotondo, Christoph Stemp, Miriam Deml
 */
@SuppressWarnings("unused")
public class ODCIIndex implements CustomDatum, CustomDatumFactory {
    private static final String SERVER_URL = "http://localhost:8090";


    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private static ODCIIndex[] scanctx = new ODCIIndex[1];
    private static BigDecimal scanctxKey = new BigDecimal(0);

    private MutableStruct _struct;
    private static final String _SQL_NAME = "SYSTEM.LIREINDEX";
    private static int[] _sqlType = {4};
    private static CustomDatumFactory[] _factory = new CustomDatumFactory[1];
    private static final ODCIIndex _ODCIIndexFactory = new ODCIIndex();

    /**
     * Creates the _struct within the constructor.
     */
    @SuppressWarnings("WeakerAccess")
    public ODCIIndex() {
        _struct = new MutableStruct(new Object[1], _sqlType, _factory);
    }

    /**
     * @return the CustomDatumFactory of this ODCIIndex()
     */
    public static CustomDatumFactory getFactory() {
        return _ODCIIndexFactory;
    }

    private BigDecimal getScanctxKey() {
        return scanctxKey;
    }

    private static void setScanctxKey(BigDecimal key) {
        scanctxKey = key;
    }

    /**
     * Creates an index that is saved in the REST webservice and added to the scanContext.
     *
     * @param ia contains information about the index and the indexed column
     * @param parms The PARAMETERS string passed in not interpreted by Oracle. The
     * maximum size of the parameter string is 1,000 characters.
     * @param env The environment handle passed to the routine
     *
     * @return ODCIConst.ODCI_SUCCESS, if the the index was created successfully; or ODCIConst.ODCI_ERROR, if it failed
     * @throws SQLException throws an {@link SQLException}
     */
    @SuppressWarnings("deprecation")
    public static BigDecimal ODCIIndexCreate(ODCIIndexInfo ia, String parms, ODCIEnv env) throws SQLException {
        toLogger(Level.INFO.toString(), "Entered ODCIIndexCreate");
        Connection oracleConnection = new OracleDriver().defaultConnection();
        Statement statement = oracleConnection.createStatement();
        ResultSet resultSet = statement.executeQuery("Select ROWID, COLUMN1 From IMG_TABLE");

        boolean exists = checkIfIndexExists(ia.getIndexName());
        //drop the index, if it already exists
        if (exists) {
            ODCIIndexDrop(ia, env);
        }
        //create empty index
        boolean success = createNewEmptyLireIndex(ia.getIndexName());

        if (resultSet == null) {
            statement.close();
            if (success) {
                toLogger(Level.INFO.toString(), "The index named " + ia.getIndexName() + " was created.");
                return ODCIConst.ODCI_SUCCESS;
            } else {
                toLogger(Level.SEVERE.toString(), "The index named " + ia.getIndexName() + " couldn't be created.");
                return ODCIConst.ODCI_ERROR;
            }
        } else {
            toLogger(Level.INFO.toString(), "Found existing elements in table. Adding them to Index");
            if (success) {
                while (resultSet.next()) {
                    String rowID = resultSet.getString("ROWID");
                    BFILE bfile = (BFILE) resultSet.getObject("COLUMN1");
                    try {
                        BigDecimal returnVal = addFileToLireIndex(ia.getIndexName(), rowID, getPathFromBFile(oracleConnection, bfile));
                        if (returnVal.equals(ODCIConst.ODCI_ERROR)) {
                            return ODCIConst.ODCI_ERROR;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                statement.close();
                return ODCIConst.ODCI_SUCCESS;
            } else {
                return ODCIConst.ODCI_ERROR;
            }

        }
    }

    /**
     * Creates a new empty Lire index
     *
     * @param indexName the name of the Index
     * @return true, if the creation was successful
     */
    private static boolean createNewEmptyLireIndex(String indexName) {

        URL url;
        HttpURLConnection connection;
        Integer status = 0;
        try {
            url = new URL(SERVER_URL + "/index/" + indexName);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            status = connection.getResponseCode();
        } catch (IOException e) {
            toLogger(Level.SEVERE.toString(), "IOException in createNewEmptyLireIndex " + indexName);
        }
        if (status > 199 && status < 300) {
            toLogger(Level.INFO.toString(), "successfully created empty index " + indexName);
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method forwards logging messages to the backend logger
     *
     * @param level   the logging level
     * @param message the messages that should be logged
     */
    private static void toLogger(String level, String message) {
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL(SERVER_URL + "/logger?level=" + URLEncoder.encode(level, "UTF-8") + "&message=" + URLEncoder.encode(message, "UTF-8"));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks, if the Index with name {@code indexName} exists in the Lire backend.
     *
     * @param indexName the name of the index
     * @return true, if the index exists, false otherwise
     */
    private static boolean checkIfIndexExists(String indexName) {
        toLogger(Level.INFO.toString(), "entering checkIfIndexExists for index " + indexName);
        URL url;
        HttpURLConnection connection;
        boolean exists = false;

        try {
            url = new URL(SERVER_URL + "/index/" + indexName);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.getResponseCode();
            InputStream inStrm = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inStrm);
            BufferedReader br = new BufferedReader(inputStreamReader);
            exists = Boolean.valueOf(br.readLine());
            inputStreamReader.close();
            br.close();
            inStrm.close();
        } catch (IOException e) {
            toLogger(Level.SEVERE.toString(), "IOException in checkIfIndexExists " + indexName);
        }
        toLogger(Level.INFO.toString(), "Does index exist? " + exists);
        return exists;

    }

    /**
     * This method retrieves the location of a BFile from the BFile itself by searching the database directory
     *
     * @param connection the connection to the oracle database
     * @param bfile the BFile, the location should be retrieved
     *
     * @return the location of the file described by the BFile
     * @throws SQLException throws an {@link SQLException}
     */
    @SuppressWarnings("deprecation")
    private static String getPathFromBFile(Connection connection, BFILE bfile) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(
                String.format("SELECT directory_path FROM ALL_DIRECTORIES WHERE upper(directory_name) = '%s'",
                        bfile.getDirAlias()));
        resultSet.next();

        String path = resultSet.getString(1);
        statement.close();

        return path + File.separator + bfile.getName();
    }

    /**
     * Drops the Index with the name {@code indexName} from the Lire backend
     *
     * @param indexName the name of the index
     *
     * @return true, if the index was dropped successfully, false otherwise
     */
    private static boolean dropLireIndex(String indexName) {
        URL url;
        HttpURLConnection connection;
        Integer status = 0;
        try {
            url = new URL(SERVER_URL + "/index/" + indexName);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            status = connection.getResponseCode();
        } catch (IOException e) {
            toLogger(Level.SEVERE.toString(), "IOException in dropLireIndex " + indexName);
        }
        if (status > 199 && status < 300) {
            toLogger(Level.INFO.toString(), "successfully dropped " + indexName);
            return true;
        } else {
            return false;
        }

    }

    /**
     * This method adds a single file to the lire index
     *
     * @param indexName the index the file should be added to
     * @param rid the rowid of the file in the database, counts as unique identifier
     * @param path the path to the file on the disk
     *
     * @return true, if the insertion was successful, false otherwise
     * @throws IOException throws an {@link IOException}
     */
    private static BigDecimal addFileToLireIndex(String indexName, String rid, String path) throws IOException {
        byte[] encodedRid = Base64.getEncoder().encode(rid.getBytes());
        URL url = new URL(SERVER_URL + "/index/" + indexName + "/" + new String(encodedRid) + "?file=" + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        Integer status = connection.getResponseCode();

        if (status > 199 && status < 300) {
            toLogger(Level.INFO.toString(), "The entry named " + rid + " was added to the index named " + indexName + ".");
            return ODCIConst.ODCI_SUCCESS;
        } else {
            toLogger(Level.SEVERE.toString(), "The entry named " + rid + " couldn't be added to the index named " + indexName + ".");
            return ODCIConst.ODCI_ERROR;
        }

    }

    /**
     * Deletes an index from the file system of the REST webservice.
     *
     * @param ia Contains information about the index and the indexed column
     * @param env The environment handle passed to the routine
     *
     * @return ODCIConst.ODCI_SUCCESS, if the the index was created successfully; or ODCIConst.ODCI_ERROR, if it failed
     */
    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public static BigDecimal ODCIIndexDrop(ODCIIndexInfo ia, ODCIEnv env) {
        boolean lireSuccess = true;
        boolean dbSuccess = true;
        try {
            boolean lireExists = checkIfIndexExists(ia.getIndexName());


            if (lireExists) {
                lireSuccess = dropLireIndex(ia.getIndexName());
            }

        } catch (SQLException e) {
            toLogger(Level.SEVERE.toString(), "This exception occured during the ODCIIndexDrop method: " + e.getMessage());
            dbSuccess = false;
        }

        if (lireSuccess && dbSuccess) {
            return ODCIConst.ODCI_SUCCESS;
        } else {
            return ODCIConst.ODCI_ERROR;
        }

    }

    /**
     * Adds an entry to an already existing index.
     *
     * @param ia Contains information about the index and the indexed column
     * @param rid The row identifier of the new row in the table
     * @param newval The value of the indexed column in the inserted row
     * @param env The environment handle passed to the routine
     *
     * @return ODCIConst.ODCI_SUCCESS, if the the index was created successfully; or ODCIConst.ODCI_ERROR, if it failed
     * @throws SQLException throws an {@link SQLException}
     */
    @SuppressWarnings({"deprecation", "WeakerAccess"})
    public static BigDecimal ODCIIndexInsert(ODCIIndexInfo ia, String rid, BFILE newval, ODCIEnv env) throws SQLException {

        try {
            Connection oracleConnection = new OracleDriver().defaultConnection();

            String path = getPathFromBFile(oracleConnection, newval);

            return addFileToLireIndex(ia.getIndexName(), rid, path);

        } catch (MalformedURLException e) {
            toLogger(Level.SEVERE.toString(), "This MalformedURLException exception occured during the ODCIIndexInsert method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (ProtocolException e) {
            toLogger(Level.SEVERE.toString(), "This ProtocolException exception occured during the ODCIIndexInsert method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (IOException e) {
            toLogger(Level.SEVERE.toString(), "This IOException exception occured during the ODCIIndexInsert method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        }
    }


    /**
     * Invoked when a row is deleted from a table that has a domain index defined on one or more of its columns.
     *
     * @param ia Contains information about the index and the indexed column
     * @param rid The row identifier of the deleted row
     * @param oldval The value of the indexed column in the deleted row. The data type is
     * identical to that of the indexed column.
     * @param env The environment handle passed to the routine
     *
     * @return ODCIConst.ODCI_SUCCESS, if the the index was created successfully; or ODCIConst.ODCI_ERROR, if it failed
     */
    @SuppressWarnings({"deprecation", "WeakerAccess"})
    public static BigDecimal ODCIIndexDelete(ODCIIndexInfo ia, String rid, BFILE oldval, ODCIEnv env) {
        URL url;
        try {

            Connection oracleConnection = new OracleDriver().defaultConnection();

            String path = getPathFromBFile(oracleConnection, oldval);
            byte[] encodedRid = Base64.getEncoder().encode(rid.getBytes());
            url = new URL(SERVER_URL + "/index/" + ia.getIndexName() + "/" + new String(encodedRid));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            Integer status = connection.getResponseCode();

            if (status > 199 && status < 300) {
                toLogger(Level.INFO.toString(), "The entry named " + oldval + " was removed from the index named " + ia.getIndexName() + ".");
                return ODCIConst.ODCI_SUCCESS;
            } else {
                toLogger(Level.SEVERE.toString(), "The entry named " + oldval + " couldn't be removed from the index named " + ia.getIndexName() + ".");
                return ODCIConst.ODCI_ERROR;
            }
        } catch (MalformedURLException e) {
            toLogger(Level.SEVERE.toString(), "This MalformedURLException exception occured during the ODCIIndexDelete method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (SQLException e) {
            toLogger(Level.SEVERE.toString(), "This SQLException exception occured during the ODCIIndexDelete method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (ProtocolException e) {
            toLogger(Level.SEVERE.toString(), "This ProtocolException exception occured during the ODCIIndexDelete method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (IOException e) {
            toLogger(Level.SEVERE.toString(), "This IOException exception occured during the ODCIIndexDelete method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        }

    }

    /**
     * Invoked when a row is updated in a table that has a defined domain index on one or more of its columns.
     *
     * @param ia Contains information about the index and the indexed column
     * @param rid The row identifier of the updated row
     * @param oldval The value of the indexed column before the update. The data type is
     * identical to that of the indexed column.
     * @param newval The value of the indexed column after the update. The data type is
     * identical to that of the indexed column.
     * @param env The environment handle passed to the routine
     *
     * @return ODCIConst.ODCI_SUCCESS, if the the index was created successfully; or ODCIConst.ODCI_ERROR, if it failed
     * @throws SQLException throws an {@link SQLException}
     */
    @SuppressWarnings("deprecation")
    public static BigDecimal ODCIIndexUpdate(ODCIIndexInfo ia, String rid, BFILE oldval, BFILE newval, ODCIEnv env) throws SQLException {
        BigDecimal delete = ODCIIndexDelete(ia, rid, oldval, env);
        BigDecimal insert = ODCIIndexInsert(ia, rid, newval, env);

        if (delete.equals(ODCIConst.ODCI_SUCCESS) && insert.equals(ODCIConst.ODCI_SUCCESS)) {
            toLogger(Level.INFO.toString(), "Updated " + rid);
            return ODCIConst.ODCI_SUCCESS;
        }
        toLogger(Level.SEVERE.toString(), "Could not update " + rid + ". Consult earlier log messages.");
        return ODCIConst.ODCI_ERROR;
    }

    /**
     * Starts the similarity search and collects all possible search results for a searchFile that is compared to all
     * entries of an index.
     *
     * @param scanctx The value of the scan context returned by some previous related
     * query-time call (such as the corresponding ancillary operator, if
     * invoked before the primary operator); NULL otherwise
     * @param ia Contains information about the index and the indexed column
     * @param pi Contains information about the operator predicate
     * @param qi Contains query information (hints plus list of ancillary operators
     * referenced)
     * @param strt The start value of the bounds on the operator return value. The data
     * type is identical to that of the operator's return value
     * @param stop The stop value of the bounds on the operator return value. The data
     * type is identical to that of the operator's return value.
     * @param valargs The value arguments of the operator invocation. The number and data
     * types of these arguments are identical to those of the value arguments
     * to the operator.
     * @param env The environment handle passed to the routine
     *
     * @return ODCIConst.ODCI_SUCCESS, if the the index was created successfully; or ODCIConst.ODCI_ERROR, if it failed
     * @throws SQLException throws an {@link SQLException}
     */
    @SuppressWarnings("AccessStaticViaInstance")
    public static BigDecimal ODCIIndexStart(ODCIIndex[] scanctx, ODCIIndexInfo ia, ODCIPredInfo pi, ODCIQueryInfo qi, BigDecimal strt, BigDecimal stop, String valargs, ODCIEnv env) throws SQLException {
        URL url;
        Connection oracleConnection = new OracleDriver().defaultConnection();
        PreparedStatement statement = oracleConnection.prepareStatement(String.format("SELECT * FROM %s.%s_index WHERE ROWID = ?", ia.getIndexSchema(), ia.getIndexName()));
        //statement.setString(1, rid);


        try {
            url = new URL(SERVER_URL + "/index/" + ia.getIndexName() + "/search?file=" + valargs);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            br.close();
            toLogger(Level.INFO.toString(), sb.toString());
            JSONObject jo = new JSONObject(sb.toString());
            String indexName = jo.get("indexName").toString();
            String hitsAsString = jo.get("hits").toString();

            //adds all hits (the individual search result for an entry) to an ArrayList
            ArrayList<Hit> hits = new ArrayList<>();
            hitsAsString = hitsAsString.replace("[", "");
            hitsAsString = hitsAsString.replace("]", "");
            hitsAsString = hitsAsString.replace("},", "};");
            String[] h = hitsAsString.split(";");
            for (String s : h) {
                jo = new JSONObject(s);
                String filepath = jo.get("filepath").toString();
                String percentage = jo.get("percentage").toString();
                double value = Double.parseDouble(percentage);
                Hit singleHit = new Hit(filepath, value);
                hits.add(singleHit);
            }

            //adds the hits to the SearchResult that is then added to the IndexDAO
            SearchResult searchResult;
            searchResult = new SearchResult(indexName, valargs, hits);
            IndexDAO indexDAO = new IndexDAO(ia.getIndexName());
            indexDAO.setSearchResult(searchResult);

            scanctx[0] = new ODCIIndex();
            scanctx[0].setScanctxKey(new BigDecimal(ContextManager.setContext(indexDAO)));
            return ODCIConst.ODCI_SUCCESS;

        } catch (MalformedURLException e) {
            toLogger(Level.SEVERE.toString(), "This MalformedURLException occured during the ODCIIndexStart method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (SQLException e) {
            toLogger(Level.SEVERE.toString(), "This SQLException occured during the ODCIIndexStart method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (ProtocolException e) {
            toLogger(Level.SEVERE.toString(), "This ProtocolException occured during the ODCIIndexStart method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (IOException e) {
            toLogger(Level.SEVERE.toString(), "This IOException occured during the ODCIIndexStart method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (JSONException e) {
            toLogger(Level.SEVERE.toString(), "This JSONException occured during the ODCIIndexStart method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        } catch (CountException e) {
            toLogger(Level.SEVERE.toString(), "This CountException occured during the ODCIIndexStart method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        }

    }

    /**
     * Invoked repeatedly to retrieve the rows satisfying the operator predicate.
     *
     * @param nrows Is the maximum number of result rows that can be returned to Oracle
     * in this call
     * @param rids Is the array of row identifiers for the result rows being returned by this
     * call
     * @param env The environment handle passed to the routine
     *
     * @return ODCIConst.ODCI_SUCCESS, if the the index was created successfully; or ODCIConst.ODCI_ERROR, if it failed
     */
    public BigDecimal ODCIIndexFetch(BigDecimal nrows, ODCIRidList[] rids, ODCIEnv env) {
        toLogger(Level.INFO.toString(), "entered fetch");
        try {
            IndexDAO result;
            result = (IndexDAO) ContextManager.getContext(scanctxKey.intValue());
            result.getSearchResult().sortHitList();
            int hitListLength = result.getSearchResult().getHits().size();
            int nRows;
            if (hitListLength > 12) {
                nRows = 12;
            } else {
                nRows = hitListLength;
            }
            ArrayList<Hit> results = new ArrayList<>(result.getSearchResult().getHits().subList(0, nRows));
            toLogger(Level.INFO.toString(), "result List lenght: " + results.size());

            String[] rowIdList = new String[nRows + 1];
            toLogger(Level.INFO.toString(), "nRows = " + nRows);
            for (int i = 0; i < nRows; i++) {
                toLogger(Level.INFO.toString(), results.get(i).getFilepath());
                rowIdList[i] = new String(Base64.getDecoder().decode(results.get(i).getFilepath()));
            }
            rowIdList[rowIdList.length - 1] = null;

            rids[0] = new ODCIRidList(rowIdList);
            return ODCIConst.ODCI_SUCCESS;
        } catch (InvalidKeyException e) {
            toLogger(Level.SEVERE.toString(), "This exception occured during the ODCIIndexFetch method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        }
    }


    /**
     * Closes an index within the scanContext. Before results can be fetched again, a new search has to be initialized
     * via the ODCIIndexStart method.
     *
     * @param env The environment handle passed to the routine
     *
     * @return ODCIConst.ODCI_SUCCESS, if the the index was created successfully; or ODCIConst.ODCI_ERROR, if it failed
     */
    public BigDecimal ODCIIndexClose(ODCIEnv env) {
        toLogger(Level.INFO.toString(), "entered close");
        try {
            ContextManager.clearContext(scanctxKey.intValue());
            scanctx[0] = null;
        } catch (InvalidKeyException e) {
            toLogger(Level.SEVERE.toString(), "This exception occured during the ODCIIndexClose method: " + e.getMessage());
            return ODCIConst.ODCI_ERROR;
        }

        return ODCIConst.ODCI_SUCCESS;
    }


    /**
     * returns the Datum of this class
     *
     * @param oracleConnection the connection to the Oracle DB
     *
     * @return the Datum
     * @throws SQLException throws an {@link SQLException}
     */
    @Override
    @SuppressWarnings("deprecation")
    public Datum toDatum(OracleConnection oracleConnection) throws SQLException {

        return _struct.toDatum(oracleConnection, _SQL_NAME);

    }

    /**
     * returns the CustomDatum of this class
     *
     * @param datum the Datum of this class
     * @param i int i
     *
     * @return the CustomDatum
     */
    @Override
    @SuppressWarnings("deprecation")
    public CustomDatum create(Datum datum, int i) {
        if (datum == null) {
            return null;
        }

        ODCIIndex index = new ODCIIndex();
        index._struct = new MutableStruct((STRUCT) datum, _sqlType, _factory);
        return index;
    }

}
