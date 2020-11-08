package fr.ralala.privatestorage.items;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Generic item
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlItem {
  private long mId = 0;
  private String mKey = null;
  private String mValue = null;

  public long getId() {
    return mId;
  }

  public void setId(long id) {
    mId = id;
  }

  public String getKey() {
    return mKey;
  }

  public void setKey(String key) {
    mKey = key;
  }

  public String getValue() {
    return mValue == null ? "" : mValue;
  }

  public void setValue(String value) {
    mValue = value;
  }

  public String toString() {
    return mKey + "=" + mValue;
  }
}
