package fr.ralala.privatestorage.ui.adapters;

import java.util.List;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import fr.ralala.privatestorage.R;
import fr.ralala.privatestorage.items.FileChooserItem;

/**
 *******************************************************************************
 * <p><b>Project PrivateStorage</b><br/>
 * Listview adapter
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class FileChooserArrayAdapter extends ArrayAdapter<FileChooserItem> {

  private final Context      c;
  private final int          id;
  private final List<FileChooserItem> items;

  public FileChooserArrayAdapter(final Context context, final int textViewResourceId,
                                    final List<FileChooserItem> objects) {
    super(context, textViewResourceId, objects);
    c = context;
    id = textViewResourceId;
    items = objects;
  }

  @Override
  public FileChooserItem getItem(final int i) {
    return items.get(i);
  }

  @Override
  public @NonNull View getView(final int position, final View convertView,
               @NonNull final ViewGroup parent) {
    View v = convertView;
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) c
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(id, null);
    }
    final FileChooserItem o = items.get(position);
    if (o != null) {
      final ImageView i1 = (ImageView) v.findViewById(R.id.icon);
      final TextView t1 = (TextView) v.findViewById(R.id.name);
      final TextView t2 = (TextView) v.findViewById(R.id.data);
      if (i1 != null)
        i1.setImageResource(o.getIcon());
      if (t1 != null)
        t1.setText(o.getName());
      if (t2 != null)
        t2.setText(o.getData());

    }
    return v;
  }

}