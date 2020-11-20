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
  private int mValueVisible = -1;

  private static class ViewHolder {
    TextView key = null;
    TextView value = null;
    ImageView type = null;
    ImageView menu;
  }

  public SqlEntriesArrayAdapter(final Context context, final int textViewResourceId,
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
      holder.value = view.findViewById(R.id.value);
      holder.type = view.findViewById(R.id.type);
      holder.menu = view.findViewById(R.id.menu);
      view.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) view.getTag();
    }
    holder.key.setText(t.getKey());

    holder.value.setText(t.getValue());

    if(t instanceof SqlEntryItem) {
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
          holder.value.setText(mValueVisible != position ? val.replaceAll("(?s).", "*") : val);
          break;
        }
        case LOGIN: {
          holder.type.setImageResource(R.mipmap.ic_login);
          String [] split = t.getValue().split("\n");
          String val = mC.getString(R.string.login) + ": " + split[0] + "\n";
          val += mC.getString(R.string.password) + ": " + (mValueVisible != position ? split[1].replaceAll("(?s).", "*") : split[1]);
          holder.value.setText(val);
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

  public void setValueVisible(int valueVisible) {
    mValueVisible = valueVisible;
    notifyDataSetChanged();
  }
}
