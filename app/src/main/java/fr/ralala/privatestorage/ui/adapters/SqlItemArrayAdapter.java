package fr.ralala.privatestorage.ui.adapters;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.SqlTableItem;
import fr.ralala.privatestorage.ui.utils.UI;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Simple array adapter
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlItemArrayAdapter extends BaseAdapter {

  private Context c = null;
  private int id = 0;

  private List<SqlTableItem> kvlist = null;
  private final List<SqlTableItem> arraylist;
  private SqlItemArrayAdapterMenuListener listener = null;
  private boolean displayValue = false;
  private int popupView = 0;
  private final LayoutInflater         mInflater;

  private class ViewHolder {
    TextView key = null;
    TextView value = null;
    ImageView type = null;
    ImageView menu;
  }
  public interface SqlItemArrayAdapterMenuListener {
    void onMenuEdit(SqlTableItem t);
    void onMenuDelete(SqlTableItem t);
  }

  public SqlItemArrayAdapter(final Context context, final int textViewResourceId,
                                 final List<SqlTableItem> objects, SqlItemArrayAdapterMenuListener listener, boolean displayValue, int popupView) {
    mInflater = LayoutInflater.from(context);
    this.displayValue = displayValue;
    this.c = context;
    this.id = textViewResourceId;
    this.listener = listener;
    this.popupView = popupView;
    this.kvlist = objects;
    this.arraylist = new ArrayList<>();
    this.arraylist.addAll(kvlist);
  }

  public boolean contains(SqlTableItem sti) {
    for(SqlTableItem s : arraylist)
      if( s.toString().equals(sti.toString()))
        return true;
    return false;
  }

  public void add(SqlTableItem sti) {
    kvlist.add(sti);
    arraylist.add(sti);
    Collections.sort(kvlist, new Comparator<SqlTableItem>() {
      @Override
      public int compare(final SqlTableItem lhs, final SqlTableItem rhs) {
        return lhs.getKey().compareTo(rhs.getKey());
      }
    });
    notifyDataSetChanged();
  }


  public void replace(List<SqlTableItem> aListP) {
    arraylist.clear();
    arraylist.addAll(aListP);
    this.kvlist = aListP;
  }

  @Override
  public int getCount() {
    return kvlist.size();
  }

  @Override
  public SqlTableItem getItem(int position) {
    return kvlist.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public void remove(final SqlTableItem sti) {
    kvlist.remove(sti);
    arraylist.remove(sti);
    super.notifyDataSetChanged();
  }

  @Override
  public @NonNull View getView(final int position, final View convertView,
                               @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    final SqlTableItem t = kvlist.get(position);
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(id, null);
      holder = new ViewHolder();
      holder.key = (TextView) v.findViewById(R.id.key);
      if(displayValue) {
        holder.value = (TextView) v.findViewById(R.id.value);
        holder.type = (ImageView) v.findViewById(R.id.type);
      }
      holder.menu = (ImageView) v.findViewById(R.id.menu);
      v.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }
    holder.key.setText(t.getKey());
    if(displayValue) {
      holder.value.setText(t.getValue());
      switch(t.getType()) {
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

  // Filter Class
  public void filter(String charText) {
    charText = charText.toLowerCase(Locale.getDefault());
    kvlist.clear();
    if (charText.length() == 0) {
      kvlist.addAll(arraylist);
    } else {
      for (final SqlTableItem kv : arraylist) {
        if (kv.getKey().toLowerCase(Locale.getDefault()).contains(charText)
          || kv.getValue().toLowerCase(Locale.getDefault()).contains(charText)) {
          kvlist.add(kv);
        }
      }
    }
    notifyDataSetChanged();
  }

}
