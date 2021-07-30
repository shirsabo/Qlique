package com.example.qlique.LoginAndSignUp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.qlique.LoginAndSignUp.RowItemHobby;
import com.example.qlique.R;

import java.util.List;

/**
 * CustomViewAdapter
 * holds the view in the hobbies selection, each row represents a different hobby.
 */
public class CustomViewAdapter extends ArrayAdapter<RowItemHobby> {
    private final Context context;

    /**
     * constructor.
     * @param context
     * @param resource
     * @param objects
     */
    public CustomViewAdapter(@NonNull Context context, int resource, @NonNull List<RowItemHobby> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    private static class ViewHolder{
        ImageView imageView;
        TextView hobby;
    }


    @NonNull
    @Override
    /**
     * return the view from the view holder.
     */
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;
        RowItemHobby rowItemHobby = getItem(position);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.hobby = convertView.findViewById(R.id.hobby);
            viewHolder.imageView = convertView.findViewById(R.id.icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        assert rowItemHobby != null;
        viewHolder.hobby.setText(rowItemHobby.getHobby());
        viewHolder.imageView.setImageResource(rowItemHobby.getImageId());
        return convertView;
    }


}
