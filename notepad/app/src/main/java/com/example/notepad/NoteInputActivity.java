package com.example.notepad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteInputActivity extends AppCompatActivity {
    private EditText titleEditText;
    private RecyclerView noteInputRecyclerView;
    private NoteItemAdapter noteItemAdapter;
    private List<NoteItem> noteItems;
    private List<String> tags = new ArrayList<>();
    private int maxTagCount = 3;
    private TextView tagCountTextView;
    private Button addTagButton;
    private static final int REQUEST_IMAGE_PICK = 1;
    private int currentPosition = -1;
    private AlertDialog addTagDialog;
    private EditText tagEditText;
    private List<Button> tagButtons = new ArrayList<>();
    private View addTagDialogView;
    private boolean isEditMode = false;
    private Note editingNote = null;
    private static final String NOTES_DIR = "notes";
    private static final int REQUEST_AUDIO_RECORD = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_input);

        titleEditText = findViewById(R.id.title_edit_text);
        noteInputRecyclerView = findViewById(R.id.note_input_recycler_view);

        addTagDialogView = findViewById(R.id.add_tag_dialog);


        // 初始化 noteItems 和 noteItemAdapter
        noteItems = new ArrayList<>();
        noteItemAdapter = new NoteItemAdapter(noteItems);
        noteInputRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteInputRecyclerView.setAdapter(noteItemAdapter);
        boolean isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        if (isEditMode) {
            String title = getIntent().getStringExtra("title");
            String tags = getIntent().getStringExtra("tags");
            String body = getIntent().getStringExtra("body");
            String[] imagePaths = getIntent().getStringArrayExtra("image_path");


            try {
                // 查找与标题相同的文件夹
                File notesDir = new File(getExternalFilesDir(null), NOTES_DIR);
                File noteDir = new File(notesDir, title);

                if (noteDir.exists()) {
                    // 读取 note.txt 文件中的内容
                    File noteFile = new File(noteDir, "note.txt");
                    String[] lines = new String(Files.readAllBytes(noteFile.toPath())).split("\n");


                    for (int i = 3; i < lines.length; i++) {
                        String line = lines[i];
                        if (line.startsWith("[文本]")) {
                            String text = line.substring("[文本]".length());
                            noteItems.add(new NoteItem(text));
                        } else if (line.startsWith("[图片]")) {
                            String base64Image = line.substring("[图片]".length());
                            Bitmap bitmap = base64ToBitmap(base64Image);

                            int insertPosition = currentPosition == -1 ? noteItems.size() : currentPosition + 1;
                            noteItems.add(insertPosition, new NoteItem(bitmap, this));
                            noteItems.add(insertPosition + 1, new NoteItem()); // 插入空的 EditText item
                            noteItemAdapter.notifyItemRangeInserted(insertPosition, 2);
                        }
                    }

                    // 填充到界面上
                    titleEditText.setText(lines[0]);
                    // tagsEditText.setText(tags);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            noteItems.add(new NoteItem()); // 添加一个空的文本项
            noteItemAdapter = new NoteItemAdapter(noteItems);
            noteInputRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            noteInputRecyclerView.setAdapter(noteItemAdapter);
        }

        // 找到 toolbar_add_tag 并获取 addTagButton 和 tagCountTextView 的引用
        Toolbar toolbarAddTag = findViewById(R.id.toolbar_add_tag);
        addTagButton = toolbarAddTag.findViewById(R.id.add_tag_button);
        tagCountTextView = findViewById(R.id.tag_count_text_view);

        // 设置 addTagButton 的点击事件监听器
        addTagButton.setOnClickListener(v -> {
            if (tags.size() >= maxTagCount) {
                showMaxTagCountDialog();
            } else {
                showAddTagDialog();
            }
        });

        // 添加 RecyclerView 的点击事件监听器
        noteInputRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, noteInputRecyclerView, getOnItemClickListener()));


        Toolbar toolbarTop = findViewById(R.id.toolbar_top);
        Button cancelButton = findViewById(R.id.cancel_button);
        Button saveButton = findViewById(R.id.save_button);

        cancelButton.setOnClickListener(v -> finish());
        cancelButton.setOnClickListener(v -> onBackPressed());

        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            StringBuilder tagBuilder = new StringBuilder();
            boolean isFirstTag = true;

            // 获取 Toolbar 上的所有按钮
            ViewGroup tagsToolbar = (ViewGroup) findViewById(R.id.toolbar_add_tag);
            for (int i = 0; i < tagsToolbar.getChildCount(); i++) {
                View child = tagsToolbar.getChildAt(i);
                if (child instanceof Button) {
                    String tagText = ((Button) child).getText().toString().trim();
                    if (!tagText.equals("添加标签")) {
                        if (!isFirstTag) {
                            tagBuilder.append(",");
                        }
                        tagBuilder.append(tagText);
                        isFirstTag = false;
                    }
                }
            }

            String tags = tagBuilder.toString();

// 读取 RecyclerView 中所有 NoteItem 的内容并拼接成 body 字符串
            StringBuilder bodyBuilder = new StringBuilder();
            for (NoteItem item : noteItems) {
                if (item.getType() == NoteItem.TYPE_TEXT) {
                    bodyBuilder.append("[文本]").append(item.getText()).append("\n");
                } else if (item.getType() == NoteItem.TYPE_IMAGE) {
                    bodyBuilder.append("[图片]").append(item.bitmapToBase64(item.getImage())).append("\n");
                }
            }
            String body = bodyBuilder.toString().trim();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("time", time);
            resultIntent.putExtra("tags", tags);
            resultIntent.putExtra("body", body);
            saveNote();
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        Toolbar toolbarBottom = findViewById(R.id.toolbar_bottom);
        View keyboardButton = findViewById(R.id.keyboard_button);
        View imageButton = findViewById(R.id.image_button);
        View audioButton = findViewById(R.id.audio_button);

        keyboardButton.setOnClickListener(v -> {
            titleEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        imageButton.setOnClickListener(v -> {
            // 打开系统图片选择器
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        audioButton.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            startActivityForResult(intent, REQUEST_AUDIO_RECORD);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        int insertPosition = currentPosition == -1 ? noteItems.size() : currentPosition + 1;

                        noteItems.add(insertPosition, new NoteItem(bitmap, this));
                        noteItems.add(insertPosition + 1, new NoteItem()); // 插入空的 EditText item
                        noteItemAdapter.notifyItemRangeInserted(insertPosition, 2);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_AUDIO_RECORD && resultCode == RESULT_OK && data != null) {
            // 获取录制的音频文件的URI
            Uri audioUri = data.getData();

            // 将音频文件保存到应用的内部存储
            String audioFilePath = saveAudioFile(audioUri);

            // 将音频文件的信息添加到笔记中
            int insertPosition = currentPosition == -1 ? noteItems.size() : currentPosition + 1;
            noteItems.add(insertPosition, new NoteItem(audioFilePath, "", this));
            noteItems.add(insertPosition + 1, new NoteItem()); // 插入空的 EditText item
            noteItemAdapter.notifyItemRangeInserted(insertPosition, 2);
        }
    }

    private RecyclerViewItemClickListener.OnItemClickListener getOnItemClickListener() {
        return new RecyclerViewItemClickListener.OnItemClickListener() {
            private GestureDetector gestureDetector = new GestureDetector(NoteInputActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    if (e.getButtonState() == MotionEvent.BUTTON_SECONDARY) { // 检查是否为右键
                        View view = noteInputRecyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (view != null) {
                            int position = noteInputRecyclerView.getChildAdapterPosition(view);
                            showDeleteConfirmationDialog(position);
                        }
                    }
                }
            });

            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                boolean intercepted = gestureDetector.onTouchEvent(e);
                return intercepted;
            }

            @Override
            public void onItemClick(View view, int position) {
                currentPosition = position;
                // 在这里添加对应的处理逻辑
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // 不需要实现,因为已经通过 GestureDetector 处理了右键长按事件
            }
        };
    }

    void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    noteItemAdapter.deleteItem(position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void saveNote() {
        String title = titleEditText.getText().toString();
        String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String tagString = String.join(",", tags);

        List<String> noteContent = new ArrayList<>();
        List<String> imagePaths = new ArrayList<>();
        int imageCounter = 1;

        StringBuilder bodyBuilder = new StringBuilder();
        for (NoteItem item : noteItems) {
            if (item.getImage() == null) {
                bodyBuilder.append("[文本]").append(item.getText()).append("\n");
            } else if (item.getType() == NoteItem.TYPE_IMAGE) {
                bodyBuilder.append("[图片]").append(item.bitmapToBase64(item.getImage())).append("\n");
            }
        }
        String body = bodyBuilder.toString().trim();

//        // 如果是编辑模式,则更新现有的笔记
//        if (getIntent().hasExtra("title")) {
//            updateNoteInDatabase(getIntent().getStringExtra("title"), title, time, tagString, String.join("\n", noteContent), imagePaths);
//        } else { // 如果是新建模式,则保存新的笔记
//            saveNoteToDatabase(title, time, tagString, String.join("\n", noteContent), imagePaths);
//        }
        // 如果是编辑模式,则删除旧的笔记
        if (isEditMode) {
            deleteOldNote();
        }

        saveNoteToDatabase(title, time, tagString, body, imagePaths);
        saveNoteToFileSystem(title, time, tagString, body, imagePaths);

        // 设置结果 Intent 并返回
        Intent resultIntent = new Intent();
        resultIntent.putExtra("title", title);
        resultIntent.putExtra("time", time);
        resultIntent.putExtra("tags", tagString);
        resultIntent.putExtra("content", String.join("\n", noteContent));
        resultIntent.putStringArrayListExtra("imagePaths", new ArrayList<>(imagePaths));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void saveNoteToDatabase(String title, String time, String tags, String content, List<String> imagePaths) {
        String imagePathsStr = String.join(",", imagePaths);
        Note note = new Note(title, content, time, tags, imagePathsStr);
        NoteDatabase.getInstance(this).saveNote(note);
    }

    private void showMaxTagCountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最多有三个标签")
                .setMessage("您已达到标签的上限,不能再添加新的标签了。")
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showAddTagDialog() {
        if (addTagDialog == null) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);
            tagEditText = dialogView.findViewById(R.id.tag_edit_text);
            Button cancelButton = dialogView.findViewById(R.id.cancel_button);
            Button confirmButton = dialogView.findViewById(R.id.confirm_button);

            cancelButton.setOnClickListener(v -> addTagDialog.dismiss());
            confirmButton.setOnClickListener(v -> {
                String tag = tagEditText.getText().toString().trim();
                if (tag.isEmpty()) {
                    showEmptyTagErrorDialog();
                } else {
                    addTag(tag);
                    addTagDialog.dismiss();
                }
            });

            addTagDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();
        }
        addTagDialog.show();
    }

    private void showEmptyTagErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("标签不能为空")
                .setMessage("请输入一个有效的标签。")
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void addTag(String tag) {
        tags.add(tag);
        updateTagCountText();
        createTagButton(tag);
    }

    private void updateTagCountText() {
        tagCountTextView.setText("标签数量: " + tags.size());
    }

    private void createTagButton(String tag) {
        Button tagButton = new Button(this);
        tagButton.setText(tag);
        tagButton.setBackgroundResource(R.drawable.rounded_rectangle_border);
        tagButton.setOnClickListener(v -> {
            // 处理点击标签按钮的逻辑
        });
        ((Toolbar) findViewById(R.id.toolbar_add_tag)).addView(tagButton);
        tagButtons.add(tagButton);
    }

    private Note getNoteFromDatabase(int noteId) {
        SQLiteDatabase database = NoteDatabase.getInstance(this).getDatabase();
        Cursor cursor = database.query("notes", null, "id = ?", new String[]{String.valueOf(noteId)}, null, null, null);

        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            String imagePathsString = cursor.getString(cursor.getColumnIndexOrThrow("imagePaths"));

            String[] imagePaths = TextUtils.isEmpty(imagePathsString) ? new String[0] : imagePathsString.split(",");

            Note note = new Note(title, content, time, "", "");
            cursor.close();
            return note;
        } else {
            cursor.close();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void saveNoteToFileSystem(String title, String time, String tags, String content, List<String> imagePaths) {
        try {
            // 创建笔记目录
            File notesDir = new File(getExternalFilesDir(null), NOTES_DIR);
            if (!notesDir.exists()) {
                notesDir.mkdir();
            }

            // 创建笔记文件夹
            File noteDir = new File(notesDir, title);
            if (!noteDir.exists()) {
                noteDir.mkdir();
            }

            // 保存笔记内容到文件
            File noteFile = new File(noteDir, "note.txt");
            FileOutputStream fos = new FileOutputStream(noteFile);
            fos.write((title + "\n").getBytes());
            fos.write((time + "\n").getBytes());
            fos.write((tags + "\n").getBytes());
            fos.write(content.getBytes());
            fos.close();

            // 保存图片到文件夹
            for (int i = 0; i < imagePaths.size(); i++) {
                String imagePath = imagePaths.get(i);
                File imageFile = new File(noteDir, "image_" + (i + 1) + ".jpg");
                FileOutputStream imageOut = new FileOutputStream(imageFile);
                byte[] imageData = Files.readAllBytes(Paths.get(imagePath));
                imageOut.write(imageData);
                imageOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadNoteFromFileSystem(String title, String tags, String body, String[] imagePaths) {
        try {
            // 查找与标题相同的文件夹
            File notesDir = new File(getExternalFilesDir(null), NOTES_DIR);
            File noteDir = new File(notesDir, title);

            if (noteDir.exists()) {
                // 读取 note.txt 文件中的内容
                File noteFile = new File(noteDir, "note.txt");
                String[] lines = new String(Files.readAllBytes(noteFile.toPath())).split("\n");
                if (lines.length >= 3) {
                    title = lines[0];
                    tags = lines[2];
                    body = lines[3];

                    // 填充到界面上
                    titleEditText.setText(title);
                    // tagsEditText.setText(tags);
                    noteItems.add(new NoteItem(body)); // 设置第一个 NoteItem 的内容为笔记正文
                    noteItemAdapter = new NoteItemAdapter(noteItems);
                    noteInputRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    noteInputRecyclerView.setAdapter(noteItemAdapter);


                    // 在这里处理图片的显示
                    for (String imagePath : imagePaths) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                            int insertPosition = noteItems.size();
                            noteItems.add(insertPosition, new NoteItem(bitmap, this));

                            noteItemAdapter.notifyItemInserted(insertPosition);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String saveImageToFileSystem(Bitmap bitmap, int imageCounter) {
        File imagesDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "note_images");
        if (!imagesDir.exists()) {
            imagesDir.mkdir();
        }

        String imageName = "note_image_" + imageCounter + ".jpg";
        File imageFile = new File(imagesDir, imageName);

        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private void deleteOldNote() {
        // 获取旧笔记的标题
        String oldTitle = getIntent().getStringExtra("title");

        // 从文件系统中删除旧笔记
        File notesDir = new File(getExternalFilesDir(null), NOTES_DIR);
        File oldNoteDir = new File(notesDir, oldTitle);
        deleteRecursive(oldNoteDir);

    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    private String saveAudioFile(Uri audioUri) {
        // 从Uri中读取音频数据,并将其保存到应用的内部存储中
        // 返回保存的文件路径
        return "a";
    }


}