package com.example.notepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AccountInfoAdapter extends ArrayAdapter<ListItem> {

    private Context context;
    private List<ListItem> itemList;

    public AccountInfoAdapter(@NonNull Context context, List<ListItem> itemList) {
        super(context, 0, itemList);
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_account_info, parent, false);
        }

        ListItem item = itemList.get(position);

        ImageView imageViewIcon = view.findViewById(R.id.image_view_icon);
        TextView textViewTitle = view.findViewById(R.id.text_view_title);

        if (item.getIconResId() != 0) {
            imageViewIcon.setVisibility(View.VISIBLE);
            imageViewIcon.setImageResource(item.getIconResId());
        } else {
            imageViewIcon.setVisibility(View.GONE);
        }

        textViewTitle.setText(item.getText());

        return view;
    }
}