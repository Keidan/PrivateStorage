package fr.ralala.privatestorage.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.ralala.privatestorage.R;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Android UI helper.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class UI {

  /**
   * Returns the res ID from the attributes.
   * @param activity The context Activity.
   * @param attr The attribute ID.
   * @return int
   */
  public static int getResIdFromAttribute(final Activity activity, final int attr) {
    if(attr==0)
      return 0;
    final TypedValue typedvalueattr = new TypedValue();
    activity.getTheme().resolveAttribute(attr,typedvalueattr,true);
    return typedvalueattr.resourceId;
  }

  /**
   * Displays a progress dialog.
   * @param context The Android context.
   * @param message The progress message.
   * @return AlertDialog
   */
  public static AlertDialog showProgressDialog(Context context, int message) {
    LayoutInflater layoutInflater = LayoutInflater.from(context);
    final ViewGroup nullParent = null;
    View view = layoutInflater.inflate(R.layout.progress_dialog, nullParent);
    AlertDialog progress = new AlertDialog.Builder(context).create();
    TextView tv = view.findViewById(R.id.text);
    tv.setText(message);
    progress.setCancelable(false);
    progress.setView(view);
    return progress;
  }

  /* tool function used to display a message box */
  public static void showAlertDialog(final Context c, final int title, final int message) {
    showAlertDialog(c, title, c.getResources().getString(message));
  }
  /* tool function used to display a message box */
  public static void showAlertDialog(final Context c, final int title, final String message) {
    AlertDialog alertDialog = new AlertDialog.Builder(c).create();
    alertDialog.setTitle(c.getResources().getString(title));
    alertDialog.setMessage(message);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, c.getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss());
    alertDialog.show();
  }
  public static void showConfirmDialog(final Context c, final int title,
                                       int message, final android.view.View.OnClickListener yes,
                                       final android.view.View.OnClickListener no) {
    showConfirmDialog(c, c.getString(title), c.getString(message), yes, no);
  }

  public static void showConfirmDialog(final Context c, final String title,
                                       String message, final android.view.View.OnClickListener yes,
                                       final android.view.View.OnClickListener no) {
    new AlertDialog.Builder(c)
      .setTitle(title)
      .setMessage(message)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setPositiveButton(android.R.string.yes, (dialog, which) -> {
        if(yes != null) yes.onClick(null);
      })
      .setNegativeButton(android.R.string.no, (dialog, which) -> {
        if(no != null) no.onClick(null);
      }).show();
  }

  private static void toast(final Context c, final String message, final int timer) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, timer);
    TextView tv = toast.getView().findViewById(android.R.id.message);
    if (null!=tv) {
      Drawable drawable = ContextCompat.getDrawable(c, R.mipmap.ic_launcher);
      if(drawable != null) {
        final Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
        tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(c.getResources(), bitmapResized), null, null, null);
        tv.setCompoundDrawablePadding(5);
      }
    }
    toast.show();
  }

  public static void toast_long(final Context c, final int message) {
    toast(c, c.getResources().getString(message), Toast.LENGTH_LONG);
  }

  public static void toast_long(final Context c, final String message) {
    toast(c, message, Toast.LENGTH_LONG);
  }

  public static void toast(final Context c, final String message) {
    toast(c, message, Toast.LENGTH_SHORT);
  }

  public static void toast(final Context c, final int message) {
    toast(c, c.getResources().getString(message));
  }

  public interface AlertDialogListListener<T> {
    void onClick(T t);
  }


  private static class ListItem<T> {
    public String name;
    public T value;

    ListItem(final String name, final T value) {
      this.name = name;
      this.value = value;
    }

    public String toString() {
      return name;
    }
  }
  public static <T> void showAlertDialog(final Context c, final int title, List<T> list, final AlertDialogListListener yes) {
    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    builder.setTitle(c.getResources().getString(title));
    builder.setIcon(android.R.drawable.ic_dialog_alert);
    List<ListItem> items = new ArrayList<>();
    for(T s : list) {
      String ss = new File(s.toString()).getName();
      if(ss.endsWith("\"}")) ss = ss.substring(0, ss.length() - 2);
      items.add(new ListItem<>(ss, s));
    }
    final ArrayAdapter<ListItem> arrayAdapter = new ArrayAdapter<>(c, android.R.layout.select_dialog_singlechoice, items);
    builder.setNegativeButton(c.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

    builder.setAdapter(arrayAdapter, (dialog, which) -> {
      dialog.dismiss();
      if(yes != null) //noinspection ConstantConditions,unchecked
        yes.onClick(arrayAdapter.getItem(which).value);
    });
    builder.show();
  }
}
