package fr.ralala.privatestorage.items;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Text and icon (id) support.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SpinnerIconItem {
  private final int mIcon;
  private final String mText;

  public SpinnerIconItem(int icon, String text) {
    mIcon = icon;
    mText = text;
  }

  public String getText() {
    return mText;
  }

  public int getIcon() {
    return mIcon;
  }
}
