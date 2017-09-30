package fr.ralala.privatestorage.ui.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.SpinnerIconItem;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Array adapter with texts and icons support.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SpinnerIconArrayAdapter extends ArrayAdapter<SpinnerIconItem> {
  private List<SpinnerIconItem> items;

  public SpinnerIconArrayAdapter(Context context, List<SpinnerIconItem> data) {
    super(context, android.R.layout.simple_spinner_item);
    addAll(data);
    this.items = data;
  }


  private class ViewHolder {
    TextView text = null;
    ImageView icon = null;
  }

  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    return refreshView(position, convertView);
  }

  @Override
  public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
    return refreshView(position, convertView);
  }

  private View refreshView(int position, View convertView) {
    try {
      View v = convertView;
      ViewHolder holder;
      final SpinnerIconItem sii = items.get(position);
      if (v == null) {
        final LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.spinner_view, null);
        holder = new ViewHolder();
        holder.text = (TextView) v.findViewById(R.id.text);
        holder.icon = (ImageView) v.findViewById(R.id.icon);
        v.setTag(holder);
      } else {
          /* We recycle a View that already exists */
        holder = (ViewHolder) v.getTag();
      }
      holder.text.setText(sii.getText());
      holder.icon.setImageResource(sii.getIcon());
      return v;
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      return convertView;
    }
  }
}