package fr.ralala.privatestorage.ui.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

  public static void forcePopupMenuIcons(final PopupMenu popup) {
    try {
      Field[] fields = popup.getClass().getDeclaredFields();
      for (Field field : fields) {
        if ("mPopup".equals(field.getName())) {
          field.setAccessible(true);
          Object menuPopupHelper = field.get(popup);
          Class<?> classPopupHelper = Class.forName(menuPopupHelper
            .getClass().getName());
          Method setForceIcons = classPopupHelper.getMethod(
            "setForceShowIcon", boolean.class);
          setForceIcons.invoke(menuPopupHelper, true);
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, c.getResources().getString(R.string.ok),
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
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
      .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface  dialog, int whichButton) {
          if(yes != null) yes.onClick(null);
        }})
      .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          if(no != null) no.onClick(null);
        }}).show();
  }

  public static void toast(final Context c, final String message, final int timer) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, timer);
    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
    if (null!=tv) {
      Drawable drawable = c.getResources().getDrawable(R.mipmap.ic_launcher);
      final Bitmap b = ((BitmapDrawable) drawable).getBitmap();
      final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
      tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(c.getResources(), bitmapResized), null, null, null);
      tv.setCompoundDrawablePadding(5);
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

    public ListItem(final String name, final T value) {
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
      String ss = new File(s.toString()).getName().toString();
      if(ss.endsWith("\"}")) ss = ss.substring(0, ss.length() - 2);
      items.add(new ListItem<T>(ss, s));
    }
    final ArrayAdapter<ListItem> arrayAdapter = new ArrayAdapter<>(c, android.R.layout.select_dialog_singlechoice, items);
    builder.setNegativeButton(c.getString(R.string.cancel), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        if(yes != null) yes.onClick(arrayAdapter.getItem(which).value);
      }
    });
    builder.show();
  }
}