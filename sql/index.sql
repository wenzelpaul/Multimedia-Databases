CREATE TYPE LireIndex AS OBJECT
(
	scanctx INTEGER,

	STATIC FUNCTION ODCIGetInterfaces(ifclist OUT SYS.ODCIObjectList)RETURN NUMBER,

	STATIC FUNCTION ODCIIndexCreate(ia sys.ODCIIndexInfo, parms VARCHAR2, env sys.ODCIEnv) RETURN NUMBER AS LANGUAGE JAVA NAME
	'com.mmdb.oracle.database.ODCIIndex.ODCIIndexCreate(oracle.ODCI.ODCIIndexInfo, java.lang.String, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

	STATIC FUNCTION ODCIIndexDrop(ia sys.ODCIIndexInfo, env sys.ODCIEnv) RETURN NUMBER AS LANGUAGE JAVA NAME
	'com.mmdb.oracle.database.ODCIIndex.ODCIIndexDrop(oracle.ODCI.ODCIIndexInfo, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

	STATIC FUNCTION ODCIIndexInsert(ia sys.ODCIIndexInfo, rid VARCHAR2, newval BFILE, env sys.ODCIEnv) RETURN NUMBER AS LANGUAGE JAVA NAME
	'com.mmdb.oracle.database.ODCIIndex.ODCIIndexInsert(oracle.ODCI.ODCIIndexInfo, java.lang.String, oracle.sql.BFILE, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

	STATIC FUNCTION ODCIIndexDelete(ia sys.ODCIIndexInfo, rid VARCHAR2, oldval BFILE, env sys.ODCIEnv) RETURN NUMBER AS LANGUAGE JAVA NAME
	'com.mmdb.oracle.database.ODCIIndex.ODCIIndexDelete(oracle.ODCI.ODCIIndexInfo, java.lang.String, oracle.sql.BFILE, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

	STATIC FUNCTION ODCIIndexUpdate(ia sys.ODCIIndexInfo, rid VARCHAR2, oldval BFILE, newval BFILE, env sys.ODCIEnv) RETURN NUMBER AS LANGUAGE JAVA NAME
	'com.mmdb.oracle.database.ODCIIndex.ODCIIndexUpdate(oracle.ODCI.ODCIIndexInfo, java.lang.String, oracle.sql.BFILE, oracle.sql.BFILE, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

	STATIC FUNCTION ODCIIndexStart(sctx IN OUT LireIndex, ia sys.ODCIIndexInfo, pi sys.ODCIPredInfo, qi sys.ODCIQueryInfo, strt NUMBER, stop NUMBER, valargs VARCHAR2, env sys.ODCIEnv) RETURN NUMBER AS LANGUAGE JAVA NAME
	'com.mmdb.oracle.database.ODCIIndex.ODCIIndexStart(com.mmdb.oracle.database.ODCIIndex[], oracle.ODCI.ODCIIndexInfo, oracle.ODCI.ODCIPredInfo, oracle.ODCI.ODCIQueryInfo, java.math.BigDecimal, java.math.BigDecimal, java.lang.String, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',
	

	MEMBER FUNCTION ODCIIndexFetch(nrows NUMBER, rids OUT sys.ODCIRidList, env sys.ODCIEnv) RETURN NUMBER AS LANGUAGE JAVA NAME
	'com.mmdb.oracle.database.ODCIIndex.ODCIIndexFetch(java.math.BigDecimal, oracle.ODCI.ODCIRidList[], oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

	MEMBER FUNCTION ODCIIndexClose(env sys.ODCIEnv) RETURN NUMBER AS LANGUAGE JAVA NAME
	'com.mmdb.oracle.database.ODCIIndex.ODCIIndexClose(oracle.ODCI.ODCIEnv) return java.math.BigDecimal'
);
/

CREATE OR REPLACE TYPE BODY LireIndex AS 
  STATIC FUNCTION ODCIGetInterfaces(ifclist OUT sys.ODCIObjectList) 
  RETURN NUMBER IS
  BEGIN
    ifclist := sys.ODCIObjectList(sys.ODCIObject('SYS','ODCIINDEX2'));
    return ODCIConst.Success;
  END ODCIGetInterfaces;
END;
/

CREATE OR REPLACE FUNCTION is_similar( a BFILE, b VARCHAR2) RETURN NUMBER AS
BEGIN
	RETURN 1;
END;
/

CREATE OR REPLACE OPERATOR Similarity
BINDING (BFILE, VARCHAR2) RETURN NUMBER 
Using is_similar;

CREATE OR REPLACE INDEXTYPE LireIndexType
For Similarity(BFILE, VARCHAR2)
Using LireIndex;

EXIT;
/