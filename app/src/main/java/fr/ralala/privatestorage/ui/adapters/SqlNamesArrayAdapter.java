package fr.ralala.privatestorage.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.SqlEntryItem;
import fr.ralala.privatestorage.items.SqlItem;
import fr.ralala.privatestorage.items.SqlNameItem;
import fr.ralala.privatestorage.ui.utils.UI;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Names array adapter
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlNamesArrayAdapter extends SqlItemArrayAdapter {


  private class ViewHolder {
    TextView key = null;
    ImageView type = null;
    ImageView menu;
  }

  public SqlNamesArrayAdapter(final Context context, final int textViewResourceId,
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
      holder.type = (ImageView) v.findViewById(R.id.type);
      holder.menu = (ImageView) v.findViewById(R.id.menu);
      v.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }
    holder.key.setText(t.getKey());
    if(SqlNameItem.class.isInstance(t)) {
      SqlNameItem sei = (SqlNameItem)t;
      switch (sei.getType()) {
        case DISPLAY:
          holder.type.setImageResource(R.mipmap.ic_menu_view);
          break;
        case HIDE:
          holder.type.setImageResource(R.mipmap.ic_menu_unview);
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
}

