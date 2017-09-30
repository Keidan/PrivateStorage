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

  private String getTableDef(final String name, final boolean hasType) {
    StringBuilder sb = new StringBuilder();
    sb.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (")
      .append(COL_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
      .append(COL_KEY).append(" TEXT NOT NULL, ")
      .append(COL_VALUE).append(" TEXT NOT NULL");
    if(hasType)
      sb.append(", ").append(COL_TYPE).append(" INTEGER");
    sb.append(");");
    return sb.toString();
  }
  
  SqlHelper(final Context context, final String name,
      final CursorFactory factory, final int version) {
    super(context, name, factory, version);
  }

  @Override
  public void onCreate(final SQLiteDatabase db) {
    db.execSQL(getTableDef(TABLE_LIST, false));
    db.execSQL(getTableDef(TABLE_ENTRIES, true));
  }
  
  @Override
  public void onOpen(final SQLiteDatabase db) {
    onCreate(db);
  }
  
  @Override
  public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
      final int newVersion) {
  }

  // Copy to sdcard for debug use
  public static String copyDatabase(final Context c, final String name,
                                    final String folder) throws Exception {
    return copyDatabase(c, name, folder, name, true);
  }
  public static String copyDatabase(final Context c, final String name,
                                    final String folder,
                                    final String filename, boolean date) throws Exception{
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
          directory.mkdir();
        File out = new File(directory, !date ? filename : (new SimpleDateFormat("yyyyMMdd_hhmma", Locale.US).format(new Date()) + "_" + name));
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
