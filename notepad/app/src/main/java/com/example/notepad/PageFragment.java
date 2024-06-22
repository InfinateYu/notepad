package com.example.notepad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;



public class PageFragment extends Fragment {

    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private RecyclerView recyclerView;
    private List<String> tagList; // 存储标签列表

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);

        noteList = new ArrayList<>();
        // 添加一些初始数据到 noteList 中
        noteList.add(new Note("初始标题1", "初始内容1", "2023-06-01"));
        noteAdapter = new NoteAdapter(noteList);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(noteAdapter);
        noteAdapter.notifyDataSetChanged();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NoteInputActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        Button searchButton = view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        Button tagButton = view.findViewById(R.id.tag_button);
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTagDialog();
            }
        });

        // 初始化标签列表
        tagList = new ArrayList<>();
        tagList.add("标签1");
        tagList.add("标签2");
        tagList.add("标签3");

        return view;
    }

    private void performSearch() {
        EditText searchEditText = getView().findViewById(R.id.search_edit_text);
        String query = searchEditText.getText().toString();
        // 在这里处理搜索逻辑
    }

    private void showTagDialog() {
        String[] tags = tagList.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("选择标签")
                .setItems(tags, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击了标签列表中的某个标签
                        String selectedTag = tags[which];
                        handleTagSelection(selectedTag);
                    }
                });
        builder.create().show();
    }

    private void handleTagSelection(String selectedTag) {
        // 在这里处理标签选择逻辑
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
            String title = data.getStringExtra("title");
            String body = data.getStringExtra("body");
            String time = data.getStringExtra("time");
            // Add the note to the RecyclerView
            addNoteToRecyclerView(title, body, time);
        }
    }

    public void addNoteToRecyclerView(String title, String body, String time) {
        Note newNote = new Note(title, body, time);
        noteList.add(newNote);
        noteAdapter.notifyItemInserted(noteList.size() - 1);
    }
}
