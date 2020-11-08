package fr.ralala.privatestorage.dropbox;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.sql.SqlHelper;
import fr.ralala.privatestorage.ui.utils.UI;
import fr.ralala.privatestorage.utils.Sys;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Manage exportation and importation using dropbox
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DropboxImportExport implements DropboxListener {
  private static final String PATH = "";
  private final DropboxHelper mHelper;
  private AlertDialog mDialog = null;
  private File mFile = null;
  private Context mContext = null;
  private DropboxUploaded mDropboxUploaded = null;
  private DropboxDownloaded mDropboxDownloaded = null;


  /**
   * Creates the import/export instance.
   */
  public DropboxImportExport() {
    mHelper = DropboxHelper.helper();
  }

  public interface DropboxUploaded {
    /**
     * File uploaded to dropbox.
     * @param error True on error.
     */
    void dropboxUploaded(final boolean error);
  }
  public interface DropboxDownloaded {
    /**
     * File downloaded from dropbox.
     * @param error True on error.
     */
    void dropboxDownloaded(final boolean error);
  }

  /**
   * Imports the data base.
   * @param c The Android context.
   */
  public void importDatabase(final Activity c) {
    mContext = c;
    mDropboxDownloaded = null;
    if(mHelper.connect(c, c.getString(R.string.app_key))) {
      if(mDialog == null)
        mDialog = UI.showProgressDialog(c, R.string.data_transfer);
      if(!c.isFinishing())
      {
        mDialog.show();
      }
      new ListFolderTask(mHelper.getClient(), this).execute(PATH);
    }
  }

  /**
   * Exports the database.
   * @param c The Android context.
   * @param displayDialog True to display a dialog box.
   * @param dropboxUploaded The export listener.
   */
  public void exportDatabase(final Activity c, boolean displayDialog, final DropboxUploaded dropboxUploaded) {
    mContext = c;
    mDropboxUploaded = dropboxUploaded;
    if(mDialog == null && displayDialog)
      mDialog = UI.showProgressDialog(c, R.string.data_transfer);
    if(mHelper.connect(c, c.getString(R.string.app_key))) {
      if(mDialog != null && !c.isFinishing())
        mDialog.show();
      try {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mFile = new File(SqlHelper.copyDatabase(c, SqlHelper.DB_NAME, path.getAbsolutePath()));
        new UploadFileTask(c, mHelper.getClient(), this).execute(Uri.fromFile(mFile).toString(), PATH);
      } catch(Exception e) {
        safeRemove();
        if(mDialog != null)
          mDialog.dismiss();
        UI.toastLong(c, c.getString(R.string.error) + ": " + e.getMessage());
        Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
      }
    }
  }

  /**
   * Removes safely
   */
  private void safeRemove() {
    if(mFile != null) {
      //noinspection ResultOfMethodCallIgnored
      mFile.delete();
      mFile = null;
    }
  }

  /**
   * Called when the upload on dropbox is complete.
   * @param result The upload result.
   */
  @Override
  public void onDropboxUploadComplete(FileMetadata result) {
    if(mDialog != null)
      mDialog.dismiss();
    UI.toast(mContext, mContext.getString(R.string.export_success));
    safeRemove();
    if(mDropboxUploaded != null)
      mDropboxUploaded.dropboxUploaded(false);
  }

  /**
   * Called when the upload on dropbox is finished on error.
   * @param e Error exception.
   */
  @Override
  public void onDropboxUploadError(Exception e) {
    if(mDialog != null)
      mDialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to upload file.", e);
    UI.toast(mContext, mContext.getString(R.string.error_dropbox_upload));
    safeRemove();
    if(mDropboxUploaded != null)
      mDropboxUploaded.dropboxUploaded(true);
  }

  /**
   * Called when the download on dropbox is complete.
   * @param result The upload result.
   */
  @Override
  public void onDroptboxDownloadComplete(File result) {
    if(mDialog != null)
      mDialog.dismiss();
    try {
      loadDb(mContext, result);
    } catch(Exception e) {
      UI.toastLong(mContext, mContext.getString(R.string.error) + ": " + e.getMessage());
      Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
    }
    safeRemove();
    if(mDropboxDownloaded != null)
      mDropboxDownloaded.dropboxDownloaded(false);
  }

  /**
   * Called when the download on dropbox is finished on error.
   * @param e Error exception.
   */
  @Override
  public void onDroptboxDownloadError(Exception e) {
    if(mDialog != null)
      mDialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to download file.", e);
    UI.toast(mContext, mContext.getString(R.string.error_dropbox_download));
    safeRemove();
    if(mDropboxDownloaded != null)
      mDropboxDownloaded.dropboxDownloaded(true);
  }


  /**
   * Called when the recovery of the file list of the dropbox is complete.
   * @param result The listing result.
   */
  @Override
  public void onDroptboxListFoderDataLoaded(ListFolderResult result) {
    if(mDialog != null)
      mDialog.dismiss();
    List<Metadata> list = result.getEntries();
    list.sort(Comparator.comparing(Metadata::getName));
    if (list.isEmpty())
      UI.toastLong(mContext, mContext.getString(R.string.error_no_files));
    else {
      computeAndLoad(mContext, list, new AlertDialogListListener<Metadata>() {
        @Override
        public void onClick(final Metadata m) {
          try {
            if(mDialog != null)
              mDialog.show();
            new DownloadFileTask(mContext, mHelper.getClient(), DropboxImportExport.this).execute((FileMetadata)m);
          } catch (Exception e) {
            UI.toastLong(mContext, mContext.getString(R.string.error) + ": " + e.getMessage());
            Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
          }
        }
      });
    }
  }

  /**
   * Called when the recovery of the file list of the dropbox is finished on error.
   * @param e Error exception.
   */
  @Override
  public void onDroptboxListFoderError(Exception e) {
    if(mDialog != null)
      mDialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to get the file list.", e);
    UI.toast(mContext, mContext.getString(R.string.error_dropbox_list_directory));
    if(mDropboxDownloaded != null)
      mDropboxDownloaded.dropboxDownloaded(true);
  }

  /**
   * Loads the database.
   * @param c The Android context.
   * @param file The db file to load.
   * @throws Exception If an exception is thrown.
   */
  public static void loadDb(final Context c, File file) throws Exception{
    SqlHelper.loadDatabase(c, SqlHelper.DB_NAME, file);
    Sys.restartApplication(c, R.string.import_success);
  }


  /**
   * Prepares the files retrieved from dropbox and display the content in a dialog box.
   * @param c The Android context.
   * @param list The file list.
   * @param yes Yes listener (dialog box)
   * @param <T> File type.
   * @param <V> Value type.
   */
  public static <T, V> void computeAndLoad(final Context c, final List<T> list, AlertDialogListListener<V> yes) {
    List<V> files = compute(list);
    if(files.isEmpty())
      UI.toastLong(c, c.getString(R.string.error_no_files));
    else {
      showAlertDialog(c, files, yes);
    }
  }

  /**
   * Prepares the list.
   * @param list The output list.
   * @param <T> File type.
   * @param <V> Value type.
   * @return List<V>
   */
  @SuppressWarnings("unchecked")
  private static <T, V> List<V> compute(final List<T> list) {
    List<V> files = new ArrayList<>();
    for(T t : list) {
      if(t instanceof File) {
        File f = (File)t;
        if (f.getName().endsWith(".sqlite3"))
          files.add((V)f);
      } else if(t instanceof FileMetadata) {
        FileMetadata f = (FileMetadata)t;
        if (f.getName().endsWith(".sqlite3"))
          files.add((V)f);
      }
    }
    return files;
  }

  private static class ListItem<T> {
    public String name;
    T value;

    ListItem(final String name, final T value) {
      this.name = name;
      this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
      return name;
    }
  }

  public interface AlertDialogListListener<T> {
    /**
     * Called when the yes button is clicked.
     * @param t The associated object.
     */
    void onClick(T t);
  }

  /**
   * Displays an alert dialog with the files list retrieved from dropbox.
   * @param <T> File type.
   * @param c The Android context.
   * @param list The list to display.
   * @param yes Yes listener (dialog box)
   */
  private static <T> void showAlertDialog(final Context c, List<T> list, final AlertDialogListListener<T> yes) {
    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    builder.setTitle(c.getResources().getString(R.string.box_select_db_file));
    builder.setIcon(android.R.drawable.ic_dialog_alert);
    List<ListItem<T>> items = new ArrayList<>();
    for(T s : list) {
      String ss = new File(s.toString()).getName();
      if(ss.endsWith("\"}")) ss = ss.substring(0, ss.length() - 2);
      items.add(new ListItem<>(ss, s));
    }
    final ArrayAdapter<ListItem<T>> arrayAdapter = new ArrayAdapter<>(c, R.layout.select_dialog_item, items);
    builder.setNegativeButton(c.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

    builder.setAdapter(arrayAdapter, (dialog, which) -> {
      dialog.dismiss();
      ListItem<T> li = arrayAdapter.getItem(which);
      if(yes != null && li != null) yes.onClick(li.value);
    });
    builder.show();
  }
}
