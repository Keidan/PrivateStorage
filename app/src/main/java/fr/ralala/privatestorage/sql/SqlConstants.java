package fr.ralala.privatestorage.sql;
/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Constants SQL
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public interface SqlConstants {
   int    VERSION_BDD                = 2;
   String DB_NAME                    = "pstorage.sqlite3";
   String TABLE_LIST                 = "tbl_list";
   String TABLE_ENTRIES              = "tbl_entries";
   String COL_ID                     = "c_id";
   String COL_KEY                    = "c_key";
   String COL_VALUE                  = "c_value";
   String COL_TYPE                   = "c_type";
   int    NUM_COL_ID                 = 0;
   int    NUM_COL_KEY                = 1;
   int    NUM_COL_VAL                = 2;
   int    NUM_COL_TYPE               = 3;
}
