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
public class SqlEntryItem extends SqlItem{
  private Type mType = Type.NONE;

  public enum Type {
    NONE,
    EMAIL,
    URL,
    PHONE,
    TEXT,
    COMPOSE,
    PASSWORD;

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
        case 5:
          return Type.COMPOSE;
        case 6:
          return Type.PASSWORD;
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
        case COMPOSE:
          return 5;
        case PASSWORD:
          return 6;
        default:
          return 4;
      }
    }
  }

  public SqlEntryItem(Type type, String key, String value) {
    this(0, type, key, value);
  }

  public SqlEntryItem(long id, Type type, String key, String value) {
    setId(id);
    setKey(key);
    setValue(value);
    setType(type);
  }

  public void set(SqlEntryItem sti) {
    setId(sti.getId());
    setKey(sti.getKey());
    setValue(sti.getValue());
    setType(sti.mType);
  }

  public Type getType() {
    return mType;
  }

  public void setType(Type type) {
    mType = type;
  }
}
