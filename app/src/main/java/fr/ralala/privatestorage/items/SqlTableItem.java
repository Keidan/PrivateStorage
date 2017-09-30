package fr.ralala.privatestorage.items;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Table item
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlTableItem {
  private long id = 0;
  private String key = null;
  private String value = null;
  private Type type = Type.NONE;

  public enum Type {
    NONE,
    EMAIL,
    URL,
    PHONE,
    TEXT,
    COMPOSE;

    public static Type fromInt(int i) {
      switch (i) {
        case 0:
          return Type.NONE;
        case 1:
          return Type.EMAIL;
        case 2:
          return Type.URL;
        case 3:
          return Type.PHONE;
        case 4:
          return Type.TEXT;
        case 5:
          return Type.COMPOSE;
        default:
          return Type.TEXT;
      }
    }

    public static int toInt(Type t) {
      switch(t) {
        case NONE:
          return 0;
        case EMAIL:
          return 1;
        case URL:
          return 2;
        case PHONE:
          return 3;
        case TEXT:
          return 4;
        case COMPOSE:
          return 5;
        default:
          return 4;
      }
    }
  }

  public SqlTableItem(Type type, String key, String value) {
    this(0, type, key, value);
  }

  public SqlTableItem(long id, Type type, String key, String value) {
    this.id = id;
    this.key = key;
    this.value = value;
    this.type = type;
  }

  public void set(SqlTableItem sti) {
    setId(sti.id);
    setKey(sti.key);
    setValue(sti.value);
    setType(sti.type);
  }

  public long getId() {
    return id;
  }

  public long setId(long id) {
    return this.id = id;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
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
