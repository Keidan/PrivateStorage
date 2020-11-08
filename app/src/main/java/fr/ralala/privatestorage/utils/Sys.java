package fr.ralala.privatestorage.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ralala.privatestorage.PrivateStorageApp;
import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.sql.SqlHelper;
import fr.ralala.privatestorage.ui.activities.fchooser.FileChooserActivity;
import fr.ralala.privatestorage.ui.utils.UI;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Android SYS helper.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class Sys {
  public static final String EXTRA_RESTART = "EXTRA_RESTART";
  private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

  public static boolean validateURL(String url) {
    try {
      new URL(url);
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  public static boolean validateEmail(String emailStr) {
    Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
    return matcher.find();
  }

  public static void openBrowser(final Context context, final String url) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    context.startActivity(intent);

  }

  public static void openDialer(final Context context, final String number) {
    Intent intent = new Intent(Intent.ACTION_DIAL);
    intent.setData(Uri.parse("tel:"+number));
    context.startActivity(intent);
  }

  public static void openMail(final Context context, String mailto) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri data = Uri.parse("mailto:"+mailto);
    intent.setData(data);
    context.startActivity(intent);
  }

  public static void clipboard(final Context context, String textToClip) {
    if(textToClip == null) textToClip = "null";
    ClipboardManager myClipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData myClip = ClipData.newPlainText("text", textToClip);
    myClipboard.setPrimaryClip(myClip);
  }


  public static void restartApplication(final Context c, final String string) {
    if(string != null)
      UI.toast(c, string);
    Intent startActivity = c.getApplicationContext().getPackageManager()
        .getLaunchIntentForPackage(c.getApplicationContext().getPackageName());
    assert startActivity != null;
    PrivateStorageApp app = (PrivateStorageApp)c.getApplicationContext();
    app.setFrom(null);
    startActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity.putExtra(EXTRA_RESTART, string);
    int mPendingIntentId = 123456;
    PendingIntent mPendingIntent = PendingIntent.getActivity(c, mPendingIntentId, startActivity,
        startActivity.getFlags());
    AlarmManager mgr = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    if(mgr != null) {
      mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
      if(c instanceof Activity)
        ActivityCompat.finishAffinity(((Activity)c));
      Process.killProcess(Process.myPid());
    }
  }

  public static void restartApplication(final Context c, final int string_id) {
    restartApplication(c, string_id == -1 ? null : c.getString(string_id));
  }

  public static void switchTo(final Activity activity, final Class<?> c, boolean clear) {
    final Intent i = new Intent(activity.getApplicationContext(), c);
    if(clear) {
      i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        i.addFlags(0x8000); // equal to Intent.FLAG_ACTIVITY_CLEAR_TASK which is only available from API level 11
    }
    activity.startActivity(i);
  }

  public static void kill(final Activity a) {
    a.finish();
    Process.killProcess(Process.myPid());
    System.exit(0);
  }


  /**
   * Export the database to dropbox.
   * @param app Main application
   * @param c Android context.
   */
  public static void exportDropbox(PrivateStorageApp app, Activity c) {
    app.getDropboxImportExport().exportDatabase(c, true, null);
  }

  /**
   * Export the database to device.
   * @param a Activity.
   */
  public static void exportDevice(Activity a){
    final Intent i = new Intent(a.getApplicationContext(), FileChooserActivity.class);
    i.putExtra(FileChooserActivity.FILECHOOSER_TYPE_KEY, "" + FileChooserActivity.FILECHOOSER_TYPE_DIRECTORY_ONLY);
    i.putExtra(FileChooserActivity.FILECHOOSER_TITLE_KEY, a.getString(R.string.pref_title_export));
    i.putExtra(FileChooserActivity.FILECHOOSER_MESSAGE_KEY, a.getString(R.string.use_folder) + ":? ");
    i.putExtra(FileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment
        .getExternalStorageDirectory().getAbsolutePath());
    i.putExtra(FileChooserActivity.FILECHOOSER_SHOW_KEY, "" + FileChooserActivity.FILECHOOSER_SHOW_DIRECTORY_ONLY);
    a.startActivityForResult(i, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY);
  }

  /**
   * Called when the file chooser is disposed with a result.
   * @param a Activity.
   * @param requestCode The request code.
   * @param resultCode The result code.
   * @param data The Intent data.
   * @return true if consumed.
   */
  public static boolean exportDeviceActivityResult(Activity a, final int requestCode, final int resultCode, final Intent data) {
    if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY) {
      if (resultCode == Activity.RESULT_OK) {
        String dir = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);
        try {
          SqlHelper.copyDatabase(a, SqlHelper.DB_NAME, dir);
          UI.toast(a, a.getString(R.string.export_success));
        } catch(Exception e) {
          UI.toastLong(a, a.getString(R.string.error) + ": " + e.getMessage());
          Log.e(Sys.class.getSimpleName(), "Error: " + e.getMessage(), e);
        }
      }
      return true;
    }
    return false;
  }
}
