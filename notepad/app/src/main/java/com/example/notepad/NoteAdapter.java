package com.example.notepad;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;

    public NoteAdapter(List<Note> noteList) {
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.titleTextView.setText(note.getTitle());
        holder.timeTextView.setText(note.getTime());

        // 设置删除按钮的点击监听器
        holder.deleteButton.setOnClickListener(v -> {
            removeNoteAt(position);
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里处理点击笔记的逻辑,比如打开笔记编辑页面
                // handleNoteClick(note);
            }
        });
    }

//    @Override
//    private void handleNoteClick(Note note) {
//        // 在这里编写点击笔记时的处理逻辑
//        // 比如打开笔记编辑页面
//        Intent intent = new Intent(v.getContext(), NoteEditActivity.class);
//        intent.putExtra("note", note);
//        v.getContext().startActivity(intent);
//    }


    // 删除指定位置的笔记
    private void removeNoteAt(int position) {
        noteList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, noteList.size());
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView timeTextView;
        ImageButton deleteButton; // 添加删除按钮



        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title);
            timeTextView = itemView.findViewById(R.id.note_time);
            deleteButton = itemView.findViewById(R.id.delete_button); // 初始化删除按钮
        }
    }
}
