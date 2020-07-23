package com.mmdb.datatypes;

import java.math.BigDecimal;

/**
 * Encapsulates constants for Oracle ODCI. Based on odci.h
 */
public class ODCIConst {
    // ------------------------------------------------------------------------
    // Return Status
    // ------------------------------------------------------------------------
    /**
     * Constant for a return status.
     */
    public final static java.math.BigDecimal ODCI_SUCCESS = new BigDecimal(0);
    /**
     * Constant for a return status.
     */
    public final static java.math.BigDecimal ODCI_ERROR = new BigDecimal(1);
    /**
     * Constant for a return status.
     */
    public final static java.math.BigDecimal ODCI_WARNING = new BigDecimal(2);
    /**
     * Constant for a return status.
     */
    public final static java.math.BigDecimal ODCI_ERROR_CONTINUE = new BigDecimal(3);
    /**
     * Constant for a return status.
     */
    public final static java.math.BigDecimal ODCI_FATAL = new BigDecimal(4);
    // ------------------------------------------------------------------------
    // ODCIAlterIndex
    // ------------------------------------------------------------------------
    /**
     * Constant for ODCIAlterIndex alter_option
     */
    public final static java.math.BigDecimal ODCI_ALTIDX_NONE = new BigDecimal(0);
    /**
     * Constant for ODCIAlterIndex alter_option
     */
    public final static java.math.BigDecimal ODCI_ALTIDX_RENAME = new BigDecimal(1);
    /**
     * Constant for ODCIAlterIndex alter_option
     */
    public final static java.math.BigDecimal ODCI_ALTIDX_REBUILD = new BigDecimal(2);
    /**
     * Constant for ODCIAlterIndex alter_option
     */
    public final static java.math.BigDecimal ODCI_ALTIDX_REBUILD_ONL = new BigDecimal(3);
    /**
     * Constant for ODCIAlterIndex alter_option
     */
    public final static java.math.BigDecimal ODCI_ALTIDX_MODIFY_COL = new BigDecimal(4);
    /**
     * Constant for ODCIAlterIndex alter_option
     */
    public final static java.math.BigDecimal ODCI_ALTIDX_UPDATE_BLOCK_REFS = new BigDecimal(5);
    /**
     * Constant for ODCIAlterIndex alter_option
     */
    public final static java.math.BigDecimal ODCI_ALTIDX_RENAME_COL = new BigDecimal(6);
    /**
     * Constant for ODCIAlterIndex alter_option
     */
    public final static java.math.BigDecimal ODCI_ALTIDX_RENAME_TAB = new BigDecimal(7);
    /**
     * Constant for ODCIAlterIndex alter_option
     */
    public final static java.math.BigDecimal ODCI_ALTIDX_MIGRATE = new BigDecimal(8);

    // ------------------------------------------------------------------------
    // Constants for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
    // ------------------------------------------------------------------------
    /**
     * Constant for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
     */
    public final static int ODCI_PRED_EXACT_MATCH = 0x0001;
    /**
     * Mask for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
     */
    public final static int ODCI_PRED_PREFIX_MATCH = (0x0002);
    /**
     * Mask for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
     */
    public final static int ODCI_PRED_INCLUDE_START = (0x0004);
    /**
     * Mask for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
     */
    public final static int ODCI_PRED_INCLUDE_STOP = (0x0008);
    /**
     * Mask for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
     */
    public final static int ODCI_PRED_OBJECT_FUNC = (0x0010);
    /**
     * Mask for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
     */
    public final static int ODCI_PRED_OBJECT_PKG = (0x0020);
    /**
     * Mask for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
     */
    public final static int ODCI_PRED_OBJECT_TYPE = (0x0040);
    /**
     * Mask for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
     */
    public final static int ODCI_PRED_MULTI_TABLE = (0x0080);

    /**
     * Mask for de.unipa.mmdb.middleend.mock.ODCIPredInfo.Flags
     */
    public final static int ODCI_PRED_NOT_EQUAL = (0x0100);

    // ------------------------------------------------------------------------
    // Constants for QueryInfo.Flags
    // ------------------------------------------------------------------------
    /**
     * Constant for QueryInfo.Flags
     */
    public final static int ODCI_QUERY_FIRST_ROWS = 0x01;
    /**
     * Constant for QueryInfo.Flags
     */
    public final static int ODCI_QUERY_ALL_ROWS = 0x02;
    /**
     * Constant for QueryInfo.Flags
     */
    public final static int ODCI_QUERY_SORT_ASC = 0x04;
    /**
     * Constant for QueryInfo.Flags
     */
    public final static int ODCI_QUERY_SORT_DESC = 0x08;
    /**
     * Constant for QueryInfo.Flags
     */
    public final static int ODCI_QUERY_BLOCKING = 0x10;

    // ------------------------------------------------------------------------
    // Constants for ScnFlg(Func /w Index Context)
    // ------------------------------------------------------------------------
    public final static int ODCI_CLEANUP_CALL = 1;
    public final static int ODCI_REGULAR_CALL = 2;

    // ------------------------------------------------------------------------
    // Constants for ODCIFuncInfo.Flags
    // ------------------------------------------------------------------------
    public final static java.math.BigDecimal ODCI_OBJECT_FUNC = new BigDecimal(0x01);
    public final static java.math.BigDecimal ODCI_OBJECT_PKG = new BigDecimal(0x02);
    public final static java.math.BigDecimal ODCI_OBJECT_TYPE = new BigDecimal(0x04);
    // ------------------------------------------------------------------------
    // Constants for ODCIArgDesc.ArgType
    // ------------------------------------------------------------------------
    public final static java.math.BigDecimal ODCI_ARG_OTHER = new BigDecimal(1);
    /**
     * column
     */
    public final static java.math.BigDecimal ODCI_ARG_COL = new BigDecimal(2);
    /**
     * literal
     */
    public final static java.math.BigDecimal ODCI_ARG_LIT = new BigDecimal(3);
    /**
     * object attribute
     */
    public final static java.math.BigDecimal ODCI_ARG_ATTR = new BigDecimal(4);
    public final static java.math.BigDecimal ODCI_ARG_NULL = new BigDecimal(5);
    public final static java.math.BigDecimal ODCI_ARG_CURSOR = new BigDecimal(6);

    /**
     * Maximum size of ODCIArgDescList array
     */
    public final static java.math.BigDecimal ODCI_ARG_DESC_LIST_MAXSIZE = new BigDecimal(32767);

    // ------------------------------------------------------------------------
    // Constants for ODCIArgDesc.ArgType
    // ------------------------------------------------------------------------
    public final static java.math.BigDecimal ODCI_PERCENT_OPTION = new BigDecimal(1);
    public final static java.math.BigDecimal ODCI_ROW_OPTION = new BigDecimal(2);

    // ------------------------------------------------------------------------
    // Constants for ODCIStatsOptions.Flags
    // ------------------------------------------------------------------------
    public final static java.math.BigDecimal ODCI_ESTIMATE_STATS = new BigDecimal(0x01);
    public final static java.math.BigDecimal ODCI_COMPUTE_STATS = new BigDecimal(0x02);
    public final static java.math.BigDecimal ODCI_VALIDATE = new BigDecimal(0x04);



}
