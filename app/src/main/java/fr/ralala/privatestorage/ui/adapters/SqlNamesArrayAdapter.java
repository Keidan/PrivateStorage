package fr.ralala.privatestorage.ui.adapters;

import android.annotation.SuppressLint;
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


  private static class ViewHolder {
    TextView key = null;
    ImageView type = null;
    ImageView menu;
  }

  public SqlNamesArrayAdapter(final Context context, final int textViewResourceId,
                             final List<SqlItem> objects, SqlItemArrayAdapterMenuListener listener, int popupView) {
    super(context, textViewResourceId, objects, listener, popupView);
  }

  @SuppressLint("RestrictedApi")
  @Override
  public @NonNull
  View getView(final int position, final View convertView,
               @NonNull final ViewGroup parent) {
    View view = convertView;
    ViewHolder holder;
    final SqlItem t = mKvlist.get(position);
    if (view == null) {
      final LayoutInflater vi = (LayoutInflater) mC.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert vi != null;
      view = vi.inflate(mId, null);
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
    if(t instanceof SqlNameItem) {
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
        if (vv.getId() == R.id.menu) {
          final PopupMenu popup = new PopupMenu(mC, vv);
          MenuPopupHelper menuHelper = new MenuPopupHelper(mC, (MenuBuilder) popup.getMenu(), vv);
          menuHelper.setForceShowIcon(true);
          popup.getMenuInflater().inflate(mPopupView, popup.getMenu());
          menuHelper.show();
          popup.setOnMenuItemClickListener((item) -> {
            if (mListener != null && R.id.edit == item.getItemId())
              mListener.onMenuEdit(t);
            else if (mListener != null && R.id.delete == item.getItemId())
              mListener.onMenuDelete(t);
            return true;
          });
        }
      });
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
    }
    return view;
  }
}

