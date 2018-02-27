package fr.ralala.privatestorage.ui.adapters;

import fr.ralala.privatestorage.items.SqlItem;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Simple array adapter
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public abstract class SqlItemArrayAdapter extends BaseAdapter {

  Context c = null;
  protected int id = 0;
  List<SqlItem> kvlist = null;
  private final List<SqlItem> arraylist;
  SqlItemArrayAdapterMenuListener listener = null;
  int popupView = 0;
  private boolean viewAll = true;

  public interface SqlItemArrayAdapterMenuListener {
    void onMenuEdit(SqlItem t);
    void onMenuDelete(SqlItem t);
  }

  SqlItemArrayAdapter(final Context context, final int textViewResourceId,
                             final List<SqlItem> objects, SqlItemArrayAdapterMenuListener listener, int popupView) {
    this.c = context;
    this.id = textViewResourceId;
    this.listener = listener;
    this.popupView = popupView;
    this.kvlist = objects;
    this.arraylist = new ArrayList<>();
    this.arraylist.addAll(kvlist);
  }

  public boolean contains(SqlItem sti) {
    for(SqlItem s : arraylist)
      if( s.toString().equals(sti.toString()))
        return true;
    return false;
  }

  public void add(SqlItem sti) {
    kvlist.add(sti);
    arraylist.add(sti);
    kvlist.sort(Comparator.comparing(SqlItem::getKey));
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return kvlist.size();
  }

  @Override
  public SqlItem getItem(int position) {
    return kvlist.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public void remove(final SqlItem sti) {
    kvlist.remove(sti);
    arraylist.remove(sti);
    super.notifyDataSetChanged();
  }

  // Filter Class
  public void filter(String charText) {
    charText = charText.toLowerCase(Locale.getDefault());
    kvlist.clear();
    if (charText.length() == 0) {
      kvlist.addAll(arraylist);
    } else {
      for (final SqlItem kv : arraylist) {
        if (kv.getKey().toLowerCase(Locale.getDefault()).contains(charText)
          || kv.getValue().toLowerCase(Locale.getDefault()).contains(charText)) {
          kvlist.add(kv);
        }
      }
    }
    notifyDataSetChanged();
  }

  public boolean isViewAll() {
    return viewAll;
  }

  public void setViewAll(boolean viewAll) {
    this.viewAll = viewAll;
    kvlist.clear();
    if (viewAll)
      kvlist.addAll(arraylist);
    notifyDataSetChanged();
  }
}
