package fr.ralala.privatestorage.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import fr.ralala.privatestorage.items.SqlItem;

/**
 * ******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Adapter used for the RecyclerView
 * </p>
 *
 * @author Keidan
 * <p>
 */
public abstract class RecyclerViewAdapter<T extends RecyclerViewAdapter.ViewHolder> extends RecyclerView.Adapter<T> {
  protected int mId;
  protected List<SqlItem> mKvlist;
  private final List<SqlItem> mArraylist;
  private boolean mViewAll = true;
  protected final RecyclerView mRecyclerView;
  protected AdapterOnClickListener mOnClickListener;
  protected AdapterOnLongClickListener mOnLongClickListener;

  RecyclerViewAdapter(RecyclerView recyclerView, final int textViewResourceId,
                      final List<SqlItem> objects) {
    mRecyclerView = recyclerView;
    mId = textViewResourceId;
    mKvlist = objects;
    mArraylist = new ArrayList<>();
    mArraylist.addAll(mKvlist);
  }

  public void setClickListeners(AdapterOnClickListener onClickListener, AdapterOnLongClickListener onLongClickListener) {
    mOnClickListener = onClickListener;
    mOnLongClickListener = onLongClickListener;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    protected AdapterOnClickListener mOnClickListener;
    protected AdapterOnLongClickListener mOnLongClickListener;

    ViewHolder(AdapterOnClickListener onClickListener, AdapterOnLongClickListener onLongClickListener, View view) {
      super(view);
      mOnClickListener = onClickListener;
      mOnLongClickListener = onLongClickListener;
      view.setOnClickListener(this);
      view.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
      if(mOnClickListener != null)
        mOnClickListener.onItemClick(getAdapterPosition(), v);
    }

    @Override
    public boolean onLongClick(View v) {
      if(mOnLongClickListener != null)
        mOnLongClickListener.onItemLongClick(getAdapterPosition(), v);
      return false;
    }
  }

  public interface AdapterOnClickListener {
    void onItemClick(int position, View v);
  }
  public interface AdapterOnLongClickListener {
    void onItemLongClick(int position, View v);
  }




  public abstract void fillEntry(int position, T viewHolder, SqlItem t);
  public abstract T instantiateEntry(View view);

  /**
   * Returns an item.
   * @param position Item position.
   * @return T
   */
  public SqlItem getItem(int position) {
    return mKvlist.get(position);
  }

  /**
   * Called when the view is created.
   * @param viewGroup The view group.
   * @param i The position
   * @return ViewHolder
   */
  @Override
  public @NonNull T onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(mId, viewGroup, false);
    return instantiateEntry(view);
  }

  /**
   * Called on Binding the view holder.
   * @param viewHolder The view holder.
   * @param i The position.
   */
  @Override
  public void onBindViewHolder(@NonNull T viewHolder, int i) {
    if(mKvlist.isEmpty()) return;
    if(i > mKvlist.size())
      i = 0;
    final SqlItem t = mKvlist.get(i);
    if (t != null) {
      fillEntry(i, viewHolder, t);
    }
  }

  /**
   * Returns the items count/
   * @return int
   */
  @Override
  public int getItemCount() {
    return mKvlist.size();
  }

  /**
   * Adds an item.
   * @param item The item to add.
   */
  public void addItem(SqlItem item) {
    mKvlist.add(item);
    mArraylist.add(item);
    mKvlist.sort(Comparator.comparing(SqlItem::getKey));
    safeNotifyDataSetChanged();
  }

  /**
   * Removes an item.
   * @param item The item to remove.
   */
  public void removeItem(SqlItem item) {
    mKvlist.remove(item);
    mArraylist.remove(item);
    safeNotifyDataSetChanged();
  }

  /**
   * This method call mRecyclerView.getRecycledViewPool().clear() and notifyDataSetChanged().
   */
  public void safeNotifyDataSetChanged() {
    mRecyclerView.getRecycledViewPool().clear();
    try {
      notifyDataSetChanged();
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
    }
  }

  public boolean contains(SqlItem sti) {
    for(SqlItem s : mArraylist)
      if( s.toString().equals(sti.toString()))
        return true;
    return false;
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
    safeNotifyDataSetChanged();
  }

  public boolean isViewAll() {
    return mViewAll;
  }

  public void setViewAll(boolean viewAll) {
    mViewAll = viewAll;
    mKvlist.clear();
    if (viewAll)
      mKvlist.addAll(mArraylist);
    safeNotifyDataSetChanged();
  }
}
