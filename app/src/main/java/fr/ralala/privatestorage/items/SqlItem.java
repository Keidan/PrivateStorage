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
  private long id = 0;
  private String key = null;
  private String value = null;

  public long getId() {
    return id;
  }

  public long setId(long id) {
    return this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value == null ? "" : value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String toString() {
    return key + "=" + value;
  }
}
