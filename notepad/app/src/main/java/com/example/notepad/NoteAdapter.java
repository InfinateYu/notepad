package com.example.notepad;

import android.content.Intent;
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
    private OnNoteClickListener onNoteClickListener;
    private OnNoteDeleteListener onNoteDeleteListener;
    public void setOnNoteDeleteListener(OnNoteDeleteListener listener) {
        this.onNoteDeleteListener = listener;
    }

    public interface OnNoteDeleteListener {
        void onNoteDelete(int position);
    }

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.onNoteClickListener = listener;
    }

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
        holder.bind(note, onNoteClickListener);

        holder.deleteButton.setOnClickListener(v -> {
            if (onNoteDeleteListener != null) {
                onNoteDeleteListener.onNoteDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTextView;
        TextView timeTextView;
        ImageButton deleteButton;
        TextView tagTextView;
        private Note note;
        private OnNoteClickListener onNoteClickListener;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title);
            timeTextView = itemView.findViewById(R.id.note_time);
            deleteButton = itemView.findViewById(R.id.delete_button);
            tagTextView = itemView.findViewById(R.id.note_tag);
            itemView.setOnClickListener(this);
        }

        public void bind(Note note, OnNoteClickListener listener) {
            this.note = note;
            this.onNoteClickListener = listener;
            titleTextView.setText(note.getTitle());
            timeTextView.setText(note.getTime());

            if (note.getTags() == "" || note.getTags() == null) {
                tagTextView.setText("No Tags");
            } else {
                tagTextView.setText(note.getTags());
            }

            // 设置删除按钮的点击监听器
            deleteButton.setOnClickListener(v -> {
                if (onNoteClickListener != null) {
                    onNoteClickListener.onNoteClick(note);
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (onNoteClickListener != null) {
                onNoteClickListener.onNoteClick(note);
            }
        }
    }
    public void updateNoteList(List<Note> newNoteList) {
        this.noteList = newNoteList;
        notifyDataSetChanged();
    }


}