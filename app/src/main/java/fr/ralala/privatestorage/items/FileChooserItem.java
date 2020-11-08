package fr.ralala.privatestorage.items;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import java.util.Locale;

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
  private final String mName;
  private final String mData;
  private final String mPath;
  private final Drawable mIcon;

  /**
   * Creates a new chooser option.
   * @param n The option name.
   * @param d The option data.
   * @param p The option path.
   * @param i The option icon.
   */
  public FileChooserItem(final String n, final String d, final String p, final Drawable i) {
    mName = n;
    mData = d;
    mPath = p;
    mIcon = i;
  }

  /**
   * Returns the name.
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Returns the data.
   * @return String
   */
  public String getData() {
    return mData;
  }

  /**
   * Returns the path.
   * @return String
   */
  public String getPath() {
    return mPath;
  }

  /**
   * Returns the drawable icon.
   * @return Drawable
   */
  public Drawable getIcon() {
    return mIcon;
  }

  /**
   * Compares this instance to an other instance.
   * @param o Instance to compare.
   * @return int
   */
  @SuppressLint("DefaultLocale")
  @Override
  public int compareTo(@NonNull final FileChooserItem o) {
    if (this.mName != null)
      return this.mName.toLowerCase(Locale.getDefault()).compareTo(
          o.getName().toLowerCase());
    else
      throw new IllegalArgumentException();
  }
}
