package fr.ralala.privatestorage.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    View view = convertView;
    ViewHolder holder;
    final SqlItem t = kvlist.get(position);
    if (view == null) {
      final LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert vi != null;
      view = vi.inflate(id, null);
      holder = new ViewHolder();
      holder.key = view.findViewById(R.id.key);
      holder.value = view.findViewById(R.id.value);
      holder.type = view.findViewById(R.id.type);
      holder.menu = view.findViewById(R.id.menu);
      view.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) view.getTag();
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
          holder.value.setText(valueVisible != position ? val.replaceAll("(?s).", "*") : val);
          break;
        }
        case TEXT:
          holder.type.setImageResource(R.mipmap.ic_copy);
          break;
      }
    }
    /* Show the popup menu if the user click on the 3-dots item. */
    try {
      holder.menu.setOnClickListener((vv) -> {
        switch (vv.getId()) {
          case R.id.menu:
            final PopupMenu popup = new PopupMenu(c, vv);
            MenuPopupHelper menuHelper = new MenuPopupHelper(c, (MenuBuilder) popup.getMenu(), vv);
            menuHelper.setForceShowIcon(true);
            popup.getMenuInflater().inflate(popupView, popup.getMenu());
            menuHelper.show();
            popup.setOnMenuItemClickListener((item) -> {
              if (listener != null && R.id.edit == item.getItemId())
                listener.onMenuEdit(t);
              else if (listener != null && R.id.delete == item.getItemId())
                listener.onMenuDelete(t);
              return true;
            });
            break;
          default:
            break;
        }
      });
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
    }
    return view;
  }

  public void setValueVisible(int valueVisible) {
    this.valueVisible = valueVisible;
    notifyDataSetChanged();
  }
}
