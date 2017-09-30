package fr.ralala.privatestorage.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.SqlTableItem;
import fr.ralala.privatestorage.utils.BlowfishCipher;

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
  public List<SqlTableItem> getEntries(SqlTableItem owner) throws Exception {
    if(owner.getValue() != null && !owner.getValue().isEmpty()) {
      String query = "SELECT * FROM " + TABLE_ENTRIES + " WHERE " + COL_ID + " IN (" + owner.getValue() + ")";
      return query(query, true);
    }
    return new ArrayList<>();
  }

  public void addEntry(SqlTableItem owner, SqlTableItem entry) throws Exception {
    insert(TABLE_ENTRIES, entry);
    String s = owner.getValue() + ENTRIES_ID_SEPARATOR + entry.getId();
    if(s.startsWith(ENTRIES_ID_SEPARATOR))
      s = s.substring(ENTRIES_ID_SEPARATOR.length());
    owner.setValue(s);
    updateName(owner);
  }

  public void updateEntry(SqlTableItem form) throws Exception {
    update(TABLE_ENTRIES, form);
  }

  public void deleteEntry(SqlTableItem owner, SqlTableItem entry) throws Exception {
    delete(TABLE_ENTRIES, entry);
    if(owner.getValue() != null && !owner.getValue().isEmpty()) {
      owner.setValue(owner.getValue().replaceAll("\\b," + entry.getId() + "\\b", ""));
      updateName(owner);
    }
  }

  /* NAMES FUNCTIONS */
  public void addName(SqlTableItem name) throws Exception {
    insert(TABLE_LIST, name);
  }
  public void updateName(SqlTableItem name) throws Exception {
    update(TABLE_LIST, name);
  }

  public List<SqlTableItem> getNames() throws Exception {
    return select(TABLE_LIST, null);
  }

  public SqlTableItem getName(String name) throws Exception {
    List<SqlTableItem> l = select(TABLE_LIST, name);
    if(l == null || l.isEmpty())
      return null;
    return l.get(0);
  }

  public void deleteName(SqlTableItem name) {
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

  private SQLiteDatabase bdd() {
    if(bdd == null || !bdd.isOpen())
      bdd = helper.getWritableDatabase();
    return bdd;
  }

  private int delete(final String table, SqlTableItem item) {
    return delete(table, item.getId());
  }

  private int delete(final String table, long id) {
    return bdd().delete(table, COL_ID + " = " + id, null);
  }

  private long insert(final String table, SqlTableItem item) throws Exception {
    final ContentValues values = new ContentValues();
    if(table.equals(TABLE_ENTRIES))
      values.put(COL_TYPE, SqlTableItem.Type.toInt(item.getType()));
    values.put(COL_KEY, cipher.encrypt(context.getString(R.string.blowfish_cipher_key), item.getKey()));
    values.put(COL_VALUE, cipher.encrypt(context.getString(R.string.blowfish_cipher_key), item.getValue()));
    return item.setId(bdd().insert(table, null, values));
  }
  
  private int update(final String table, SqlTableItem item) throws Exception {
    final ContentValues values = new ContentValues();
    if(table.equals(TABLE_ENTRIES))
      values.put(COL_TYPE, SqlTableItem.Type.toInt(item.getType()));
    values.put(COL_KEY, cipher.encrypt(context.getString(R.string.blowfish_cipher_key), item.getKey()));
    values.put(COL_VALUE, cipher.encrypt(context.getString(R.string.blowfish_cipher_key), item.getValue()));
    return bdd().update(table, values, COL_ID + " = " + item.getId(), null);
  }

  private List<SqlTableItem> select(final String table, final String key) throws Exception {
    String query = "SELECT  * FROM " + table;
    if(key != null)
      query += " WHERE " + COL_KEY + "= '" + cipher.encrypt(context.getString(R.string.blowfish_cipher_key), key) + "'";
    return query(query, table.equals(TABLE_ENTRIES));
  }

  private List<SqlTableItem> query(final String query, boolean containsType) throws Exception {
    final List<SqlTableItem> list = new ArrayList<>();
    final Cursor c = bdd().rawQuery(query, null);
    if (c.moveToFirst()) {
      do {
        SqlTableItem.Type type = SqlTableItem.Type.NONE;
        if(containsType)
          type = SqlTableItem.Type.fromInt(c.getInt(NUM_COL_TYPE));
        final SqlTableItem kv = new SqlTableItem(c.getInt(NUM_COL_ID), type,
          cipher.decrypt(context.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_KEY)),
          cipher.decrypt(context.getString(R.string.blowfish_cipher_key),c.getString(NUM_COL_VAL)));
        list.add(kv);
      } while (c.moveToNext());
    }
    c.close();
    Collections.sort(list, new Comparator<SqlTableItem>() {
      @Override
      public int compare(final SqlTableItem lhs, final SqlTableItem rhs) {
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
