package fr.ralala.privatestorage.items;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Name item
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlNameItem extends SqlItem {
  private Type mType = Type.DISPLAY;

  public enum Type {
    DISPLAY,
    HIDE;

    public static Type fromInt(int i) {
      switch (i) {
        case 1:
          return Type.HIDE;
        case 0:
        default:
          return Type.DISPLAY;
      }
    }

    public static int toInt(Type t) {
      switch(t) {
        case HIDE:
          return 1;
        case DISPLAY:
        default:
          return 0;
      }
    }
  }

  public SqlNameItem(Type type, String key, String value) {
    this(0, type, key, value);
  }

  public SqlNameItem(long id, Type type, String key, String value) {
    setId(id);
    setKey(key);
    setValue(value);
    setType(type);
  }

  public void set(SqlNameItem sti) {
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
