package fr.ralala.privatestorage.sql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.SqlEntryItem;
import fr.ralala.privatestorage.items.SqlItem;
import fr.ralala.privatestorage.items.SqlNameItem;
import fr.ralala.privatestorage.ui.utils.UI;
import fr.ralala.privatestorage.utils.BlowfishCipher;
import fr.ralala.privatestorage.utils.Sys;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Factory object for the SQL requests.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlFactory implements SqlConstants {
  private static final String ENTRIES_ID_SEPARATOR = ",";
  private SQLiteDatabase mBdd = null;
  private final SqlHelper mHelper;
  private final BlowfishCipher mCipher;
  private final Context  mContext;
  
  public SqlFactory(final BlowfishCipher cipher, final Context context) {
    mHelper = new SqlHelper(context, DB_NAME, null, VERSION_BDD);
    mContext = context;
    mCipher = cipher;
  }

  /* ENTRIES FUNCTIONS */
  public List<SqlItem> getEntries(SqlNameItem owner) throws Exception {
    if(owner.getValue() != null && !owner.getValue().isEmpty()) {
      String query = "SELECT * FROM " + TABLE_ENTRIES + " WHERE " + COL_ID + " IN (" + owner.getValue() + ")";
      return query(query, true);
    }
    return new ArrayList<>();
  }

  public void addEntry(SqlNameItem owner, SqlEntryItem entry) throws Exception {
    insert(TABLE_ENTRIES, entry);
    String s = owner.getValue() + ENTRIES_ID_SEPARATOR + entry.getId();
    if(s.startsWith(ENTRIES_ID_SEPARATOR))
      s = s.substring(ENTRIES_ID_SEPARATOR.length());
    owner.setValue(s);
    updateName(owner);
  }

  public void updateEntry(SqlEntryItem form) throws Exception {
    update(TABLE_ENTRIES, form);
  }

  public void deleteEntry(SqlNameItem owner, SqlEntryItem entry) throws Exception {
    delete(TABLE_ENTRIES, entry);
    if(owner.getValue() != null && !owner.getValue().isEmpty()) {
      owner.setValue(owner.getValue().replaceAll("\\b," + entry.getId() + "\\b", ""));
      updateName(owner);
    }
  }

  /* NAMES FUNCTIONS */
  public void addName(SqlNameItem name) throws Exception {
    insert(TABLE_LIST, name);
  }
  public void updateName(SqlNameItem name) throws Exception {
    update(TABLE_LIST, name);
  }

  public List<SqlItem> getNames() throws Exception {
    return select(null);
  }

  public SqlNameItem getName(String name) throws Exception {
    List<?> l = select(name);
    if(l == null || l.isEmpty())
      return null;
    return (SqlNameItem)l.get(0);
  }

  public void deleteName(SqlNameItem name) {
    delete(TABLE_LIST, name);
    if(name.getValue() != null && !name.getValue().isEmpty()) {
      String[] v = name.getValue().split(ENTRIES_ID_SEPARATOR);
      for(String s : v) {
        int i = Integer.parseInt(s);
        delete(TABLE_ENTRIES, i);
      }
    }
  }

  /* COMMON FUNCTIONS */
  public void close() {
    if(mBdd != null && mBdd.isOpen())
      mBdd.close();
  }

  private SQLiteDatabase bdd()  {
    if(mBdd == null || !mBdd.isOpen()) {
      mBdd = mHelper.getWritableDatabase();
      int version = 0;
      if(isTableExists(TABLE_LIST + "_v"+version)) {
        try {
          version = 1;
          Log.i(getClass().getSimpleName(), "TABLE_LISTv\"+version+\" found");
          List<SqlNameItem> old_list = readNamesV1();
          mBdd.delete(TABLE_LIST, null, null);
          for (SqlNameItem n : old_list)
            addName(n);
          bdd().execSQL("DROP TABLE IF EXISTS " + TABLE_LIST + "_v" + version + ";");
        } catch(Exception e) {
          Log.e(getClass().getSimpleName(), "Exception " + e.getMessage(), e);
          UI.showAlertDialog(mContext, R.string.error, R.string.error_db_update);
        }
      }
      if(version != 0)
        Sys.restartApplication(mContext, mContext.getString(R.string.restart_from_db_update_vn) + " " + (version + 1));
    }
    return mBdd;
  }


  private boolean isTableExists(String tableName) {
    Cursor cursor = mBdd.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
    if(cursor!=null) {
      if(cursor.getCount()>0) {
        cursor.close();
        return true;
      }
      cursor.close();
    }
    return false;
  }

  private void delete(final String table, SqlItem item) {
    delete(table, item.getId());
  }

  private void delete(final String table, long id) {
    bdd().delete(table, COL_ID + " = " + id, null);
  }

  private void insert(final String table, SqlItem item) throws Exception {
    final ContentValues values = new ContentValues();
    if(item instanceof SqlNameItem)
      values.put(COL_TYPE, SqlNameItem.Type.toInt(((SqlNameItem)item).getType()));
    else if(item instanceof SqlEntryItem)
      values.put(COL_TYPE, SqlEntryItem.Type.toInt(((SqlEntryItem)item).getType()));
    values.put(COL_KEY, mCipher.encrypt(mContext.getString(R.string.blowfish_cipher_key), item.getKey()));
    values.put(COL_VALUE, mCipher.encrypt(mContext.getString(R.string.blowfish_cipher_key), item.getValue()));
    item.setId(bdd().insert(table, null, values));
  }
  
  private void update(final String table, SqlItem item) throws Exception {
    final ContentValues values = new ContentValues();
    if(item instanceof SqlNameItem)
      values.put(COL_TYPE, SqlNameItem.Type.toInt(((SqlNameItem)item).getType()));
    else if(item instanceof SqlEntryItem)
      values.put(COL_TYPE, SqlEntryItem.Type.toInt(((SqlEntryItem)item).getType()));
    values.put(COL_KEY, mCipher.encrypt(mContext.getString(R.string.blowfish_cipher_key), item.getKey()));
    values.put(COL_VALUE, mCipher.encrypt(mContext.getString(R.string.blowfish_cipher_key), item.getValue()));
    bdd().update(table, values, COL_ID + " = " + item.getId(), null);
  }

  private List<SqlItem> select(final String key) throws Exception {
    String query = "SELECT  * FROM " + SqlConstants.TABLE_LIST;
    if(key != null)
      query += " WHERE " + COL_KEY + "= '" + mCipher.encrypt(mContext.getString(R.string.blowfish_cipher_key), key) + "'";
    return query(query, SqlConstants.TABLE_LIST.equals(TABLE_ENTRIES));
  }

  private List<SqlItem> query(final String query, boolean entries) throws Exception {
    final List<SqlItem> list = new ArrayList<>();
    final Cursor c = bdd().rawQuery(query, null);
    if (c.moveToFirst()) {
      do {
        final String key = mCipher.decrypt(mContext.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_KEY));
        final String value = mCipher.decrypt(mContext.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_VAL));
        SqlItem it;
        if(entries) {
          SqlEntryItem.Type type = SqlEntryItem.Type.fromInt(c.getInt(NUM_COL_TYPE));
          it = new SqlEntryItem(c.getInt(NUM_COL_ID), type, key, value);
        } else {
          SqlNameItem.Type type = SqlNameItem.Type.fromInt(c.getInt(NUM_COL_TYPE));
          it = new SqlNameItem(c.getInt(NUM_COL_ID), type, key, value);
        }
        list.add(it);
      } while (c.moveToNext());
    }
    c.close();
    list.sort(Comparator.comparing(SqlItem::getKey));
    return list;
  }

  private List<SqlNameItem> readNamesV1() throws Exception {
    final List<SqlNameItem> list = new ArrayList<>();
    final Cursor c = bdd().rawQuery("SELECT * FROM " + TABLE_LIST + "_v1", null);
    if (c.moveToFirst()) {
      do {
        final String key = mCipher.decrypt(mContext.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_KEY));
        final String value = mCipher.decrypt(mContext.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_VAL));
        list.add(new SqlNameItem(c.getInt(NUM_COL_ID), SqlNameItem.Type.DISPLAY, key, value));
      } while (c.moveToNext());
    }
    c.close();
    list.sort(Comparator.comparing(SqlItem::getKey));
    return list;
  }
  
  public void removeAll() {
    bdd().execSQL("DROP TABLE IF EXISTS " + TABLE_LIST + ";");
    bdd().execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES + ";");
  }
}
