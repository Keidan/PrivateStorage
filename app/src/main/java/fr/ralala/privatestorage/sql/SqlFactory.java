package fr.ralala.privatestorage.sql;

import java.util.ArrayList;
import java.util.Collections;
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
  private SQLiteDatabase      bdd     = null;
  private SqlHelper           helper  = null;
  private BlowfishCipher      cipher  = null;
  private Context             context = null;
  
  public SqlFactory(final BlowfishCipher cipher, final Context context) throws Exception {
    helper = new SqlHelper(context, DB_NAME, null, VERSION_BDD);
    this.context = context;
    this.cipher = cipher;
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
    return select(TABLE_LIST, null);
  }

  public SqlNameItem getName(String name) throws Exception {
    List<?> l = select(TABLE_LIST, name);
    if(l == null || l.isEmpty())
      return null;
    return (SqlNameItem)l.get(0);
  }

  public void deleteName(SqlNameItem name) {
    delete(TABLE_LIST, name);
    if(name.getValue() != null && !name.getValue().isEmpty()) {
      String v[] = name.getValue().split(ENTRIES_ID_SEPARATOR);
      for(String s : v) {
        Integer i = Integer.parseInt(s);
        delete(TABLE_ENTRIES, i);
      }
    }
  }

  /* COMMON FUNCTIONS */
  public void close() {
    if(bdd != null && bdd.isOpen())
      bdd.close();
  }

  private SQLiteDatabase bdd()  {
    if(bdd == null || !bdd.isOpen()) {
      bdd = helper.getWritableDatabase();
      int version = 0;
      if(isTableExists(TABLE_LIST + "_v"+version)) {
        try {
          version = 1;
          Log.i(getClass().getSimpleName(), "TABLE_LISTv\"+version+\" found");
          List<SqlNameItem> old_list = readNamesV1();
          bdd.delete(TABLE_LIST, null, null);
          for (SqlNameItem n : old_list)
            addName(n);
          bdd().execSQL("DROP TABLE IF EXISTS " + TABLE_LIST + "_v" + version + ";");
        } catch(Exception e) {
          Log.e(getClass().getSimpleName(), "Exception " + e.getMessage(), e);
          UI.showAlertDialog(context, R.string.error, R.string.error_db_update);
        }
      }
      if(version != 0)
        Sys.restartApplication(context, context.getString(R.string.restart_from_db_update_vn) + " " + (version + 1));
    }
    return bdd;
  }


  public boolean isTableExists(String tableName) {
    Cursor cursor = bdd.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
    if(cursor!=null) {
      if(cursor.getCount()>0) {
        cursor.close();
        return true;
      }
      cursor.close();
    }
    return false;
  }

  private int delete(final String table, SqlItem item) {
    return delete(table, item.getId());
  }

  private int delete(final String table, long id) {
    return bdd().delete(table, COL_ID + " = " + id, null);
  }

  private long insert(final String table, SqlItem item) throws Exception {
    final ContentValues values = new ContentValues();
    if(SqlNameItem.class.isInstance(item))
      values.put(COL_TYPE, SqlNameItem.Type.toInt(((SqlNameItem)item).getType()));
    else if(SqlEntryItem.class.isInstance(item))
      values.put(COL_TYPE, SqlEntryItem.Type.toInt(((SqlEntryItem)item).getType()));
    values.put(COL_KEY, cipher.encrypt(context.getString(R.string.blowfish_cipher_key), item.getKey()));
    values.put(COL_VALUE, cipher.encrypt(context.getString(R.string.blowfish_cipher_key), item.getValue()));
    return item.setId(bdd().insert(table, null, values));
  }
  
  private int update(final String table, SqlItem item) throws Exception {
    final ContentValues values = new ContentValues();
    if(SqlNameItem.class.isInstance(item))
      values.put(COL_TYPE, SqlNameItem.Type.toInt(((SqlNameItem)item).getType()));
    else if(SqlEntryItem.class.isInstance(item))
      values.put(COL_TYPE, SqlEntryItem.Type.toInt(((SqlEntryItem)item).getType()));
    values.put(COL_KEY, cipher.encrypt(context.getString(R.string.blowfish_cipher_key), item.getKey()));
    values.put(COL_VALUE, cipher.encrypt(context.getString(R.string.blowfish_cipher_key), item.getValue()));
    return bdd().update(table, values, COL_ID + " = " + item.getId(), null);
  }

  private List<SqlItem> select(final String table, final String key) throws Exception {
    String query = "SELECT  * FROM " + table;
    if(key != null)
      query += " WHERE " + COL_KEY + "= '" + cipher.encrypt(context.getString(R.string.blowfish_cipher_key), key) + "'";
    return query(query, table.equals(TABLE_ENTRIES));
  }

  private List<SqlItem> query(final String query, boolean entries) throws Exception {
    final List<SqlItem> list = new ArrayList<>();
    final Cursor c = bdd().rawQuery(query, null);
    if (c.moveToFirst()) {
      do {
        final String key = cipher.decrypt(context.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_KEY));
        final String value = cipher.decrypt(context.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_VAL));
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
    Collections.sort(list, new Comparator<SqlItem>() {
      @Override
      public int compare(final SqlItem lhs, final SqlItem rhs) {
        return lhs.getKey().compareTo(rhs.getKey());
      }
    });
    return list;
  }

  private List<SqlNameItem> readNamesV1() throws Exception {
    final List<SqlNameItem> list = new ArrayList<>();
    final Cursor c = bdd().rawQuery("SELECT * FROM " + TABLE_LIST + "_v1", null);
    if (c.moveToFirst()) {
      do {
        final String key = cipher.decrypt(context.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_KEY));
        final String value = cipher.decrypt(context.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_VAL));
        list.add(new SqlNameItem(c.getInt(NUM_COL_ID), SqlNameItem.Type.DISPLAY, key, value));
      } while (c.moveToNext());
    }
    c.close();
    Collections.sort(list, new Comparator<SqlItem>() {
      @Override
      public int compare(final SqlItem lhs, final SqlItem rhs) {
        return lhs.getKey().compareTo(rhs.getKey());
      }
    });
    return list;
  }
  
  public void removeAll() {
    bdd().execSQL("DROP TABLE IF EXISTS " + TABLE_LIST + ";");
    bdd().execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES + ";");
  }
}
