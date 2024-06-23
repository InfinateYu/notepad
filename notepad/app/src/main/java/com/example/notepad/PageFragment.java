package com.example.notepad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageFragment extends Fragment implements NoteAdapter.OnNoteClickListener, NoteAdapter.OnNoteDeleteListener {
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
        noteList.add(new Note("初始标题1", "初始内容1", "1970-1-1", ""));
        noteAdapter = new NoteAdapter(noteList);
        noteAdapter.setOnNoteClickListener(this);
        noteAdapter.setOnNoteDeleteListener(this);


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
        tagList.add("tag1");
        tagList.add("tag2");
        tagList.add("tag3");

        return view;
    }

    private void performSearch() {
        EditText searchEditText = getView().findViewById(R.id.search_edit_text);
        String query = searchEditText.getText().toString();
        filterNoteList(query);
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
            if (data.hasExtra("note")) {
                // 更新笔记
                Note updatedNote = (Note) data.getSerializableExtra("note");
                updateNote(updatedNote);
            } else {
                // 创建新笔记
                String title = data.getStringExtra("title");
                String body = data.getStringExtra("body");
                String time = data.getStringExtra("time");
                String tags = data.getStringExtra("tags");

                // 检查是否已经存在同名的笔记
                if (isNoteExist(title)) {
                    // 删除同名的旧笔记
                    deleteOldNote(title);
                }

                addNoteToRecyclerView(title, body, time, tags);
            }
        }
    }

    public void addNoteToRecyclerView(String title, String body, String time, String tags) {
        Note newNote = new Note(title, body, time, tags);
        noteList.add(newNote);
        noteAdapter.notifyItemInserted(noteList.size() - 1);
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(getActivity(), NoteInputActivity.class);
        intent.putExtra("title", note.getTitle());
        intent.putExtra("tags", readTagsFromFile(note));
        intent.putExtra("body", note.getBody());
        intent.putExtra("image_path", note.getImagePaths());
        intent.putExtra("isEditMode", true);
        startActivityForResult(intent, 1);
    }

    public void onNoteDelete(int position) {
        showDeleteConfirmationDialog(position);
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("删除笔记")
                .setMessage("确定要删除这个笔记吗?")
                .setPositiveButton("确定", (dialog, which) -> deleteNoteFromRecyclerView(position))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteNoteFromRecyclerView(int position) {
        noteList.remove(position);
        noteAdapter.notifyItemRemoved(position);
    }
    private boolean isNoteExist(String title) {
        for (Note note : noteList) {
            if (note.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    private void deleteOldNote(String title) {
        // 遍历笔记列表,找到匹配标题的笔记
        for (int i = 0; i < noteList.size(); i++) {
            if (noteList.get(i).getTitle().equals(title)) {
                // 删除笔记
                noteList.remove(i);
                noteAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    public void addNoteToRecyclerView2(String title, String body, String time, String tags) {
        Note newNote = new Note(title, body, time, tags);
        noteList.add(newNote);
        noteAdapter.notifyItemInserted(noteList.size() - 1);
    }
    private void updateNote(Note updatedNote) {
        for (int i = 0; i < noteList.size(); i++) {
            if (noteList.get(i).getTitle().equals(updatedNote.getTitle())) {
                noteList.set(i, updatedNote);
                noteAdapter.notifyItemChanged(i);
                break;
            }
        }
    }
    private void filterNoteList(String query) {
        if (query.isEmpty()) {
            // 如果搜索框为空,显示所有笔记
            noteAdapter.updateNoteList(noteList);
        } else {
            // 根据搜索内容过滤笔记列表
            List<Note> filteredList = new ArrayList<>();
            for (Note note : noteList) {
                if (note.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(note);
                }
            }
            noteAdapter.updateNoteList(filteredList);
        }
    }
    private String readTagsFromFile(Note note) {
        String tags = "";
        try {
            // 从文件 note.txt 中读取标签
            File file = new File(getContext().getFilesDir(), "note.txt");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                // 跳过第 0 行
                reader.readLine();
                // 读取第 1 行(第 2 行)作为标签
                tags = reader.readLine();
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tags;
    }

}