package com.example.notepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SettingsAdapter extends ArrayAdapter<ListItem> {

    private Context mContext;
    private List<ListItem> mItems;

    public SettingsAdapter(Context context, List<ListItem> items) {
        super(context, 0, items);
        mContext = context;
        mItems = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(R.layout.list_item_settings, parent, false);
        }

        // 获取当前位置的 ListItem 对象
        ListItem currentItem = mItems.get(position);

        // 找到 TextView 控件并设置文本
        TextView textView = listItemView.findViewById(R.id.text_view);
        textView.setText(currentItem.getText());

        // 根据 ListItem 的可见性设置 TextView 的可见性
        if (currentItem.isVisible()) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }

        // 根据 ListItem 的可点击性设置 TextView 的可点击性
        if (currentItem.isClickable()) {
            textView.setClickable(true);
            textView.setFocusable(true);
        } else {
            textView.setClickable(false);
            textView.setFocusable(false);
        }

        return listItemView;
    }
}
