package fr.ralala.privatestorage.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.SqlEntryItem;
import fr.ralala.privatestorage.items.SqlItem;
import fr.ralala.privatestorage.ui.utils.UI;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Entries array adapter
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlEntriesArrayAdapter extends SqlItemArrayAdapter {
  private int valueVisible = -1;

  private class ViewHolder {
    TextView key = null;
    TextView value = null;
    ImageView type = null;
    ImageView menu;
  }

  public SqlEntriesArrayAdapter(final Context context, final int textViewResourceId,
                              final List<SqlItem> objects, SqlItemArrayAdapterMenuListener listener, int popupView) {
    super(context, textViewResourceId, objects, listener, popupView);
  }

  @Override
  public @NonNull
  View getView(final int position, final View convertView,
               @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    final SqlItem t = kvlist.get(position);
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(id, null);
      holder = new ViewHolder();
      holder.key = (TextView) v.findViewById(R.id.key);
      holder.value = (TextView) v.findViewById(R.id.value);
      holder.type = (ImageView) v.findViewById(R.id.type);
      holder.menu = (ImageView) v.findViewById(R.id.menu);
      v.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }
    holder.key.setText(t.getKey());

    Log.e("TAG", "valueVisible:"+valueVisible);
    holder.value.setText(t.getValue());

    if(SqlEntryItem.class.isInstance(t)) {
      SqlEntryItem sei = (SqlEntryItem)t;
      switch (sei.getType()) {
        case EMAIL:
          holder.type.setImageResource(R.mipmap.ic_mail);
          break;
        case URL:
          holder.type.setImageResource(R.mipmap.ic_url);
          break;
        case PHONE:
          holder.type.setImageResource(R.mipmap.ic_phone);
          break;
        case COMPOSE:
          holder.type.setImageResource(R.mipmap.ic_compose);
          break;
        case PASSWORD: {
          holder.type.setImageResource(R.mipmap.ic_password);
          String val = t.getValue();
          holder.value.setText(valueVisible == -1 ? val.replaceAll("(?s).", "*") : val);
          break;
        }
        case TEXT:
          holder.type.setImageResource(R.mipmap.ic_copy);
          break;
      }
    }
    /* Show the popup menu if the user click on the 3-dots item. */
    try {
      holder.menu.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          switch (v.getId()) {
            case R.id.menu:
              final PopupMenu popup = new PopupMenu(c, v);
                /* Force the icons display */
              UI.forcePopupMenuIcons(popup);
              popup.getMenuInflater().inflate(popupView, popup.getMenu());
                /* Init the default behaviour */
              popup.show();
              popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                  if (listener != null && R.id.edit == item.getItemId())
                    listener.onMenuEdit(t);
                  else if (listener != null && R.id.delete == item.getItemId())
                    listener.onMenuDelete(t);
                  return true;
                }
              });
              break;
            default:
              break;
          }
        }
      });
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
    }
    return v;
  }

  public void setValueVisible(int valueVisible) {
    this.valueVisible = valueVisible;
    notifyDataSetChanged();
  }
}
