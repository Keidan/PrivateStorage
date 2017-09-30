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
  private int icon;
  private String text;

  public SpinnerIconItem(int icon, String text) {
    this.icon = icon;
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public int getIcon() {
    return icon;
  }
}
