package fr.ralala.privatestorage.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Process;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ralala.privatestorage.PrivateStorageApp;
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
    PrivateStorageApp app = (PrivateStorageApp)c.getApplicationContext();
    app.setFrom(null);
    Intent i = app.getPackageManager()
      .getLaunchIntentForPackage(c.getApplicationContext().getPackageName() );
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    c.startActivity(i);
  }

  public static void restartApplication(final Context c, final int string_id) {
    restartApplication(c, string_id == -1 ? null : c.getString(string_id));
  }

  public static void switchTo(final Activity activity, final Class<?> c, boolean clear) {
    switchTo(activity, c, null, null, clear);
  }

  public static void switchTo(final Activity activity, final Class<?> c,
                              final String extraKey, final String extraValue, boolean clear) {
    final Intent i = new Intent(activity.getApplicationContext(), c);
    if (extraKey != null && extraValue != null)
      i.putExtra(extraKey, extraValue);
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
}
