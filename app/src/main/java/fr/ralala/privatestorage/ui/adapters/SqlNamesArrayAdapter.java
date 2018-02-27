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
    View view = convertView;
    ViewHolder holder;
    final SqlItem t = kvlist.get(position);
    if (view == null) {
      final LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert vi != null;
      view = vi.inflate(id, null);
      holder = new ViewHolder();
      holder.key = view.findViewById(R.id.key);
      holder.type = view.findViewById(R.id.type);
      holder.menu = view.findViewById(R.id.menu);
      view.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) view.getTag();
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
}

