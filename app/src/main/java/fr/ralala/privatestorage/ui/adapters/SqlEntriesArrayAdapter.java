package fr.ralala.privatestorage.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.SqlEntryItem;
import fr.ralala.privatestorage.items.SqlItem;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Entries array adapter
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlEntriesArrayAdapter extends RecyclerViewAdapter<SqlEntriesArrayAdapter.ViewHolder> {
  private int mValueVisible = -1;

  public static class ViewHolder extends RecyclerViewAdapter.ViewHolder {
    TextView key;
    TextView value;
    ImageView type;

    ViewHolder(AdapterOnClickListener onClickListener, AdapterOnLongClickListener onLongClickListener, View view) {
      super(onClickListener, onLongClickListener, view);
      key = view.findViewById(R.id.key);
      value = view.findViewById(R.id.value);
      type = view.findViewById(R.id.type);
    }
  }
  public SqlEntriesArrayAdapter(final RecyclerView recyclerView, final int textViewResourceId,
                              final List<SqlItem> objects) {
    super(recyclerView, textViewResourceId, objects);
  }


  public void fillEntry(int position, SqlEntriesArrayAdapter.ViewHolder viewHolder, SqlItem t) {
    viewHolder.key.setText(t.getKey());

    viewHolder.value.setText(t.getValue());

    if(t instanceof SqlEntryItem) {
      SqlEntryItem sei = (SqlEntryItem)t;
      switch (sei.getType()) {
        case EMAIL:
          viewHolder.type.setImageResource(R.mipmap.ic_mail);
          break;
        case URL:
          viewHolder.type.setImageResource(R.mipmap.ic_url);
          break;
        case PHONE:
          viewHolder.type.setImageResource(R.mipmap.ic_phone);
          break;
        case COMPOSE:
          viewHolder.type.setImageResource(R.mipmap.ic_compose);
          break;
        case PASSWORD: {
          viewHolder.type.setImageResource(R.mipmap.ic_password);
          String val = t.getValue();
          viewHolder.value.setText(mValueVisible != position ? val.replaceAll("(?s).", "*") : val);
          break;
        }
        case LOGIN: {
          viewHolder.type.setImageResource(R.mipmap.ic_login);
          String [] split = t.getValue().split("\n");
          String val = mRecyclerView.getContext().getString(R.string.login) + ": " + split[0] + "\n";
          val += mRecyclerView.getContext().getString(R.string.password) + ": " + (mValueVisible != position ? split[1].replaceAll("(?s).", "*") : split[1]);
          viewHolder.value.setText(val);
          break;
        }
        case TEXT:
          viewHolder.type.setImageResource(R.mipmap.ic_copy);
          break;
      }
    }
  }

  public SqlEntriesArrayAdapter.ViewHolder instantiateEntry(View view) {
    return new SqlEntriesArrayAdapter.ViewHolder(mOnClickListener, mOnLongClickListener, view);
  }
  
  public void setValueVisible(int valueVisible) {
    mValueVisible = valueVisible;
    safeNotifyDataSetChanged();
  }
}
