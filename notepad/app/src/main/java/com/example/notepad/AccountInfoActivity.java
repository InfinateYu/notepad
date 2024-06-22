package com.example.notepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import account.AccountManager;

public class AccountInfoActivity extends AppCompatActivity {
    private AccountManager manager;
    int code;
    private ListView listView;
    private AccountInfoAdapter adapter;
    private List<ListItem> itemList;
    private String username;
    private String signature;
    private ImageView imageViewAvatar;
    private static final String AVATAR_FILE_NAME = "user_avatar.jpg";


    private static final String SHARED_PREFS_NAME = "user_info";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_SIGNATURE = "signature";
    private static final String BROADCAST_ACTION_UPDATE_USERNAME = "com.example.notepad.UPDATE_USERNAME";
    private static final String BROADCAST_ACTION_UPDATE_SIGNATURE = "com.example.notepad.UPDATE_SIGNATURE";
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private String avatarFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        manager = new AccountManager();
        code = 0;
        // 获取用户昵称和个性签名
        username = getUsername();
        signature = getSignature();
        avatarFilePath = getAvatarFilePath();

        // 初始化头像 ImageView
        imageViewAvatar = findViewById(R.id.image_view_avatar);
        updateAvatarImage();

        // 初始化列表项数据
        itemList = new ArrayList<>();
        itemList.add(new ListItem("修改头像", signature, true, false));
        itemList.add(new ListItem("修改昵称", signature, true, false));
        itemList.add(new ListItem("修改个性签名", signature, true, false));
        itemList.add(new ListItem("返回上一页", "无", true, false));

        // 设置适配器
        adapter = new AccountInfoAdapter(this, itemList);

        // 初始化 ListView
        listView = findViewById(R.id.list_view_account_info);
        listView.setAdapter(adapter);

        // 设置 ListView 的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem clickedItem = itemList.get(position);
                String text = clickedItem.getText();
                Log.d("AccountInfoActivity", "Clicked item text: " + text);

                if (text.equals("修改头像")) {
                    // 打开系统照片浏览器
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                } else if (text.equals("修改昵称")) {
                    // 处理修改昵称的逻辑
                    Intent intent = new Intent(AccountInfoActivity.this, ChangeUsernameActivity.class);
                    startActivity(intent);
                } else if (text.equals("修改个性签名")) {
                    // 处理修改个性签名的逻辑
                    Intent intent = new Intent(AccountInfoActivity.this, ChangeSignatureActivity.class);
                    startActivity(intent);
                } else if (text.equals("返回上一页")) {
                    // 返回上一个页面
                    finish();
                }
            }
        });

        // 注册广播接收器
        registerReceiver(updateUsernameReceiver, new IntentFilter(BROADCAST_ACTION_UPDATE_USERNAME));
        registerReceiver(updateSignatureReceiver, new IntentFilter(BROADCAST_ACTION_UPDATE_SIGNATURE));


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        unregisterReceiver(updateUsernameReceiver);
        unregisterReceiver(updateSignatureReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            // 获取选择的图片 URI
            Uri selectedImageUri = data.getData();

            // 将图片保存到内部存储空间
            Bitmap bitmap = getBitmapFromUri(selectedImageUri);
            String avatarFilePath = saveAvatarToInternalStorage(bitmap);

            // 更新列表项中的头像
            itemList.get(0).setIconResId(R.drawable.custom_avatar);
            adapter.notifyDataSetChanged();

            // 保存头像文件路径到 SharedPreferences
            saveAvatarFilePath(avatarFilePath);

            // 更新头像显示
            updateAvatarImage();
        }
    }

    private BroadcastReceiver updateUsernameReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 更新用户昵称
            username = getUsername();
            itemList.get(0).setText(username);
            adapter.notifyDataSetChanged();
        }
    };

    private BroadcastReceiver updateSignatureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 更新用户个性签名
            signature = getSignature();
            itemList.get(1).setText(signature);
            itemList.get(2).setText(signature);
            adapter.notifyDataSetChanged();
        }
    };

    private String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    private String getSignature() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SIGNATURE, "");
    }


    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String saveAvatarToInternalStorage(Bitmap bitmap) {
        try {
            File internalStorageDir = getFilesDir();
            File avatarFile = new File(internalStorageDir, AVATAR_FILE_NAME);
            FileOutputStream outputStream = new FileOutputStream(avatarFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return avatarFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getAvatarResourceId() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        String avatarFilePath = sharedPreferences.getString(KEY_AVATAR, null);
        if (avatarFilePath != null) {
            File avatarFile = new File(avatarFilePath);
            if (avatarFile.exists()) {
                return R.drawable.custom_avatar;
            }
        }
        return R.drawable.default_avatar;
    }

    private String getAvatarFilePath() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_AVATAR, null);
    }

    private void saveAvatarFilePath(String avatarFilePath) {
        try (FileInputStream fileInputStream = new FileInputStream(avatarFilePath)) {
            int len = fileInputStream.available();
            byte[] data = new byte[len];
            fileInputStream.read(data);
            String profile = Arrays.toString(Base64.getEncoder().encode(data));

            // 和数据库连接
            final CountDownLatch downLatch = new CountDownLatch(1);
            // 验证输入的账号和密码是否与保存的一致
            new Thread(() -> {
                code = manager.updateUser(username, "", "", profile, "");
                downLatch.countDown();
            }).start();
            downLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String path = getFilesDir().getAbsolutePath() + File.separator +  "avatar" + File.separator + "avatar.jpg";
        File file = new File(path);
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Path src = Paths.get(avatarFilePath);
            Path dest = Paths.get(path);
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_AVATAR, path);
        editor.apply();
    }

    private void updateAvatarImage() {
        String avatarFilePath = getAvatarFilePath();
        if (avatarFilePath != null) {
            File avatarFile = new File(avatarFilePath);
            if (avatarFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(avatarFilePath);
                imageViewAvatar.setImageBitmap(bitmap);
            } else {
                imageViewAvatar.setImageResource(R.drawable.default_avatar);
            }
        } else {
            imageViewAvatar.setImageResource(R.drawable.default_avatar);
        }
    }
}