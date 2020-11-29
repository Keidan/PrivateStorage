package fr.ralala.privatestorage.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.SqlItem;
import fr.ralala.privatestorage.items.SqlNameItem;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Names array adapter
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlNamesArrayAdapter extends RecyclerViewAdapter<SqlNamesArrayAdapter.ViewHolder> {


  public static class ViewHolder extends RecyclerViewAdapter.ViewHolder {
    TextView key;
    ImageView type;

    ViewHolder(AdapterOnClickListener onClickListener, AdapterOnLongClickListener onLongClickListener, View view) {
      super(onClickListener, onLongClickListener, view);
      key = view.findViewById(R.id.key);
      type = view.findViewById(R.id.type);
    }
  }

  public SqlNamesArrayAdapter(final RecyclerView recyclerView, final int textViewResourceId,
                             final List<SqlItem> objects) {
    super(recyclerView, textViewResourceId, objects);
  }


  public void fillEntry(int position, ViewHolder viewHolder, SqlItem t) {
    viewHolder.key.setText(t.getKey());
    if(t instanceof SqlNameItem) {
      SqlNameItem sei = (SqlNameItem)t;
      switch (sei.getType()) {
        case DISPLAY:
          viewHolder.type.setImageResource(R.mipmap.ic_menu_view);
          break;
        case HIDE:
          viewHolder.type.setImageResource(R.mipmap.ic_menu_unview);
          break;
      }
    }
  }

  public ViewHolder instantiateEntry(View view) {
    return new ViewHolder(mOnClickListener, mOnLongClickListener, view);
  }
}

