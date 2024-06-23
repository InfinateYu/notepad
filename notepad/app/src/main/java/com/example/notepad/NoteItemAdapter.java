package com.example.notepad;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<NoteItem> noteItems;

    public NoteItemAdapter(List<NoteItem> noteItems) {
        this.noteItems = noteItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NoteItem.TYPE_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_text, parent, false);
            return new TextViewHolder(view);
        } else if (viewType == NoteItem.TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_image, parent, false);
            return new ImageViewHolder(view);
        } else { // TYPE_EMPTY_TEXT
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_empty_text, parent, false);
            return new EmptyTextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NoteItem noteItem = noteItems.get(position);
        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).textEditText.setText(noteItem.getText());
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).imageView.setImageBitmap(noteItem.getImage());
        } else if (holder instanceof EmptyTextViewHolder) {
            ((EmptyTextViewHolder) holder).textEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    noteItem.setText(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return noteItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return noteItems.get(position).getType();
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        EditText textEditText;

        TextViewHolder(@NonNull View itemView) {
            super(itemView);
            textEditText = itemView.findViewById(R.id.note_text_edit_text);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.note_image_view);
        }
    }
    static class EmptyTextViewHolder extends RecyclerView.ViewHolder {
        EditText textEditText;

        EmptyTextViewHolder(@NonNull View itemView) {
            super(itemView);
            textEditText = itemView.findViewById(R.id.note_text_edit_text);
        }
    }
    public void deleteItem(int position) {
        NoteItem item = noteItems.get(position);
        item.setDeleted(true);
        notifyItemChanged(position);
    }


}