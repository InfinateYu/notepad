package com.example.notepad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class NoteItem {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_EMPTY_TEXT = 2;
    public static final int TYPE_SOUND = 3; // 新增类型
    private ImageView imageView;
    private Button soundButton; // 新增音频按钮
    private boolean isDeleted;

    private int type;
    private String text;
    private Bitmap image;
    private String audio;

    public NoteItem(String text) {
        this.type = TYPE_TEXT;
        this.text = text;
    }

    public NoteItem(Bitmap image, Context context) {
        this.type = TYPE_IMAGE;
        this.image = image;
        this.imageView = new ImageView(context);
        this.imageView.setImageBitmap(image);
        this.imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public NoteItem() {
        this.type = TYPE_EMPTY_TEXT;
        this.text = "";
    }

    public NoteItem(String audio, String a,Context context) {
        this.type = TYPE_SOUND;
        this.soundButton = new Button(context);
        this.soundButton.setText("播放音频");
        this.soundButton.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Bitmap getImage() {
        return image;
    }

    public Button getSoundButton() {
        return soundButton;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    public static Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}