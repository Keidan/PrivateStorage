package fr.ralala.privatestorage.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Management of SQL database (except list, insert, update, remove entry)
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlHelper extends SQLiteOpenHelper implements SqlConstants {

  private String getTableDef(final String name) {
    String sb = "CREATE TABLE IF NOT EXISTS " + name + " ("
      + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + COL_KEY + " TEXT NOT NULL, " + COL_VALUE + " TEXT NOT NULL";
     sb += ", " + COL_TYPE + " INTEGER" + ");";
    return sb;
  }
  
  SqlHelper(final Context context, final String name,
      final CursorFactory factory, final int version) {
    super(context, name, factory, version);
  }

  @Override
  public void onCreate(final SQLiteDatabase db) {
    db.execSQL(getTableDef(TABLE_LIST));
    db.execSQL(getTableDef(TABLE_ENTRIES));
  }
  
  @Override
  public void onOpen(final SQLiteDatabase db) {
    onCreate(db);
  }
  
  @Override
  public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
      final int newVersion) {
    if(oldVersion == 1 && newVersion == 2) {
      db.execSQL("ALTER TABLE " + TABLE_LIST + " RENAME TO " + TABLE_LIST + "_v" + oldVersion);
      db.execSQL(getTableDef(TABLE_LIST));
    }
  }

  // Copy to sdcard for debug use
  public static String copyDatabase(final Context c, final String name,
                                  final String folder) throws Exception {
    return copyDatabase(c, name, folder, name);
  }
  public static String copyDatabase(final Context c, final String name,
                                   final String folder,
                                   final String filename) throws Exception{
    final String databasePath = c.getDatabasePath(name).getPath();
    final File f = new File(databasePath);
    OutputStream myOutput = null;
    InputStream myInput = null;
    Log.d(SqlHelper.class.getSimpleName(), " db path " + databasePath + ", exist " + f.exists());
    Exception exception = null;
    String output = null;
    if (f.exists()) {
      try {

        final File directory = new File(folder);
        if (!directory.exists())
          if(!directory.mkdir())
            Log.e(SqlHelper.class.getSimpleName(), "mkdir failed!");
        File out = new File(directory, new SimpleDateFormat("yyyyMMdd_hhmma", Locale.US).format(new Date()) + "_" + filename);
        myOutput = new FileOutputStream(out);
        myInput = new FileInputStream(databasePath);

        final byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
          myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        output = out.getAbsolutePath();
      } catch (final Exception e) {
        exception = e;
      } finally {
        try {
          if (myOutput != null)
            myOutput.close();
          if (myInput != null)
            myInput.close();
        } catch (final Exception e) {
          Log.e(SqlHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
        }
      }
    }
    if(exception != null) throw exception;
    return output;
  }

  public static void loadDatabase(Context c, final String name, File in) throws Exception{
    final String databasePath = c.getDatabasePath(name).getPath();
    final File f = new File(databasePath);
    InputStream myInput = null;
    OutputStream myOutput = null;
    Exception exception = null;
    try {
      myInput = new FileInputStream(in.getAbsolutePath());
      String outFileName = f.getAbsolutePath();
      myOutput = new FileOutputStream(outFileName);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = myInput.read(buffer))>0){
        myOutput.write(buffer, 0, length);
      }
      myOutput.flush();
    } catch (final Exception e) {
      exception = e;
    } finally {
      try {
        if (myOutput != null)
          myOutput.close();
        if (myInput != null)
          myInput.close();
      } catch (final Exception e) {
        Log.e(SqlHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
      }
    }
    if(exception != null) throw exception;
  }
}
