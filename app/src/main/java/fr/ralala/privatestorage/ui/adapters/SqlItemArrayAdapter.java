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

  protected Context mC;
  protected int mId;
  protected List<SqlItem> mKvlist;
  private final List<SqlItem> mArraylist;
  protected SqlItemArrayAdapterMenuListener mListener;
  protected int mPopupView;
  private boolean mViewAll = true;

  public interface SqlItemArrayAdapterMenuListener {
    void onMenuEdit(SqlItem t);
    void onMenuDelete(SqlItem t);
  }

  SqlItemArrayAdapter(final Context context, final int textViewResourceId,
                             final List<SqlItem> objects, SqlItemArrayAdapterMenuListener listener, int popupView) {
    mC = context;
    mId = textViewResourceId;
    mListener = listener;
    mPopupView = popupView;
    mKvlist = objects;
    mArraylist = new ArrayList<>();
    mArraylist.addAll(mKvlist);
  }

  public boolean contains(SqlItem sti) {
    for(SqlItem s : mArraylist)
      if( s.toString().equals(sti.toString()))
        return true;
    return false;
  }

  public void add(SqlItem sti) {
    mKvlist.add(sti);
    mArraylist.add(sti);
    mKvlist.sort(Comparator.comparing(SqlItem::getKey));
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return mKvlist.size();
  }

  @Override
  public SqlItem getItem(int position) {
    return mKvlist.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public void remove(final SqlItem sti) {
    mKvlist.remove(sti);
    mArraylist.remove(sti);
    super.notifyDataSetChanged();
  }

  // Filter Class
  public void filter(String charText) {
    charText = charText.toLowerCase(Locale.getDefault());
    mKvlist.clear();
    if (charText.length() == 0) {
      mKvlist.addAll(mArraylist);
    } else {
      for (final SqlItem kv : mArraylist) {
        if (kv.getKey().toLowerCase(Locale.getDefault()).contains(charText)
          || kv.getValue().toLowerCase(Locale.getDefault()).contains(charText)) {
          mKvlist.add(kv);
        }
      }
    }
    notifyDataSetChanged();
  }

  public boolean isViewAll() {
    return mViewAll;
  }

  public void setViewAll(boolean viewAll) {
    mViewAll = viewAll;
    mKvlist.clear();
    if (viewAll)
      mKvlist.addAll(mArraylist);
    notifyDataSetChanged();
  }
}
