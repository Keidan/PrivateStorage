package fr.ralala.privatestorage.items;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * File option
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class FileChooserItem implements Comparable<FileChooserItem> {
  private String name;
  private String data;
  private String path;
  private int icon;

  public FileChooserItem(final String n, final String d, final String p, final int i) {
    name = n;
    data = d;
    path = p;
    icon = i;
  }

  public String getName() {
    return name;
  }

  public String getData() {
    return data;
  }

  public String getPath() {
    return path;
  }

  public int getIcon() {
    return icon;
  }

  @SuppressLint("DefaultLocale")
  @Override
  public int compareTo(@NonNull final FileChooserItem o) {
    if (this.name != null)
      return this.name.toLowerCase(Locale.getDefault()).compareTo(
          o.getName().toLowerCase());
    else
      throw new IllegalArgumentException();
  }
}
