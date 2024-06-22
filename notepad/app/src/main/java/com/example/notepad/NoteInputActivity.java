package com.example.notepad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteInputActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText bodyEditText;
    private static final int REQUEST_IMAGE_PICK = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_input);

        titleEditText = findViewById(R.id.title_edit_text);
        bodyEditText = findViewById(R.id.body_edit_text);

        Toolbar toolbarTop = findViewById(R.id.toolbar_top);
        Button cancelButton = findViewById(R.id.cancel_button);
        Button saveButton = findViewById(R.id.save_button);

        cancelButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String body = bodyEditText.getText().toString();
            String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("body", body);
            resultIntent.putExtra("time", time);
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
            // Handle audio button click
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            // 获取选中的图片的 URI
            Uri selectedImageUri = data.getData();

            // 将选中的图片显示在 EditText 的当前光标位置
            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        // 将 Bitmap 转换为可插入到 EditText 中的 Spannable
                        ImageSpan imageSpan = new ImageSpan(this, bitmap);

                        // 获取当前光标位置
                        int cursorPosition = bodyEditText.getSelectionStart();

                        // 创建一个 SpannableStringBuilder，用于处理文本和插入图片
                        Editable editable = bodyEditText.getText();
                        SpannableStringBuilder builder = new SpannableStringBuilder(editable);

                        // 插入图片到光标位置
                        builder.insert(cursorPosition, "\n"); // 插入一个换行符
                        builder.setSpan(imageSpan, cursorPosition, cursorPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // 更新 EditText 的内容
                        bodyEditText.setText(builder);
                        bodyEditText.setSelection(cursorPosition + 1); // 将光标移动到插入图片后的位置
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
