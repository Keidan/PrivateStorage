package fr.ralala.privatestorage.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.FileChooserItem;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Listview adapter
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class FileChooserArrayAdapter extends ArrayAdapter<FileChooserItem> {
  private final Context mContext;
  private final int mId;
  private final List<FileChooserItem> mItems;
  private SparseBooleanArray mSelectedItemsIds;

  private class ViewHolder {
    ImageView icon;
    TextView name;
    TextView data;
  }

  /**
   * Creates the array adapter.
   * @param context The Android context.
   * @param textViewResourceId The resource id of the container.
   * @param objects The objects list.
   */
  public FileChooserArrayAdapter(final Context context, final int textViewResourceId,
                          final List<FileChooserItem> objects) {
    super(context, textViewResourceId, objects);
    mContext = context;
    mId = textViewResourceId;
    mItems = objects;
    mSelectedItemsIds = new SparseBooleanArray();
  }

  /**
   * Toggles the item selection.
   * @param position Item position.
   */
  public void toggleSelection(int position) {
    selectView(position, !mSelectedItemsIds.get(position));
  }

  /**
   * Removes the item selection.
   */
  public void removeSelection() {
    mSelectedItemsIds = new SparseBooleanArray();
    notifyDataSetChanged();
  }

  /**
   * Select a view.
   * @param position Position.
   * @param value Selection value.
   */
  private void selectView(int position, boolean value) {
    if (value)
      mSelectedItemsIds.put(position, true);
    else
      mSelectedItemsIds.delete(position);
    notifyDataSetChanged();
  }

  /**
   * Returns the selection count.
   * @return int
   */
  public int getSelectedCount() {
    return mSelectedItemsIds.size();
  }

  /**
   * Returns the selected ids.
   * @return SparseBooleanArray
   */
  public SparseBooleanArray getSelectedIds() {
    return mSelectedItemsIds;
  }

  /**
   * Returns if the position is checked or not.
   * @param position The item position.
   * @return boolean
   */
  public boolean isPositionChecked(int position) {
    return mSelectedItemsIds.get(position);
  }

  /**
   * Returns an items at a specific position.
   * @param i The item index.
   * @return The item.
   */
  @Override
  public FileChooserItem getItem(final int i) {
    return mItems.get(i);
  }

  @Override
  public int getPosition(FileChooserItem item) {
    return super.getPosition(item);
  }

  @Override
  public int getCount() {
    return mItems.size();
  }

  @Override
  public long getItemId(int position) {
    return super.getItemId(position);
  }

  /**
   * Returns the current view.
   * @param position The view position.
   * @param convertView The view to convert.
   * @param parent The parent.
   * @return The new view.
   */
  @Override
  public @NonNull View getView(final int position, final View convertView,
                               @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    if (v == null) {
      final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert inflater != null;
      v = inflater.inflate(mId, null);
      holder = new ViewHolder();
      holder.data = v.findViewById(R.id.data);
      holder.icon = v.findViewById(R.id.icon);
      holder.name = v.findViewById(R.id.name);
      v.setTag(holder);
    } else {
      holder = (ViewHolder)v.getTag();
    }
    final FileChooserItem o = mItems.get(position);
    if (o != null) {
      holder.name.setText(o.getName());
      holder.data.setText(o.getData());

      if(o.isPreview()) {
        holder.icon.post(() -> {
          Bitmap bitmap = BitmapFactory.decodeFile(o.getPath());
          Resources r = mContext.getResources();
          int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());
          Drawable drawable = new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap(bitmap, px, px, true));
          holder.icon.setImageDrawable(drawable);
          holder.icon.setAdjustViewBounds(true);
        });
      } else
        holder.icon.setImageDrawable(o.getIcon());

    }
    v.setBackgroundColor(ContextCompat.getColor(mContext, mSelectedItemsIds.get(position) ? R.color.colorAccent : R.color.windowBackground));
    return v;
  }

}
