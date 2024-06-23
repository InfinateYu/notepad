package com.example.notepad;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.CountDownLatch;

import account.AccountManager;

public class ChangeUsernameActivity extends AppCompatActivity {
    private AccountManager manager;
    int code;
    private static final String SHARED_PREFS_NAME = "user_info";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NICKNAME = "nickname";
    private TextView textViewCurrentUsername;
    private EditText editTextNewUsername;
    private Button buttonCancel, buttonConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);

        manager = new AccountManager();
        code = 0;
        // 初始化控件
        textViewCurrentUsername = findViewById(R.id.text_view_current_username);
        editTextNewUsername = findViewById(R.id.edit_text_new_username);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonConfirm = findViewById(R.id.button_confirm);

        // 获取当前用户名并显示
        String currentUsername = getUsername();
        textViewCurrentUsername.setText("当前昵称: " + getNickname());

        // 设置"取消"按钮的点击事件
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 结束当前 Activity,返回到上一个页面
            }
        });

        // 设置"确认"按钮的点击事件
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = editTextNewUsername.getText().toString().trim();
                if (newUsername.isEmpty()) {
                    emptyAlert();
                }
                if (newUsername.length() > 24) {
                    tooLongAlert();
                }
                if (newUsername.equals(currentUsername)) {
                    sameAlert();
                }
                try {
                    String username = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).getString(KEY_USERNAME, "");

                    final CountDownLatch downLatch = new CountDownLatch(1);
                    new Thread(() -> {
                        code = manager.updateUser(username, newUsername, "", "", "");
                        downLatch.countDown();
                    }).start();
                    downLatch.await();

                    if (code == 11) {
                        // 修改昵称成功
                        setUsername(newUsername);
                        successAlert();
                        finish();
                    } else {
                        failAlert(code);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    private String getNickname() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_NICKNAME, "");
    }

    private void setUsername(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NICKNAME, username);
        editor.apply();
    }

    private void emptyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("请输入合法的昵称")
                .setPositiveButton("确定", null)
                .show();
    }

    private void tooLongAlert() {
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("昵称过长（不超过24个字符）")
                .setPositiveButton("确定", null)
                .show();
    }

    private void sameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("不得与原昵称相同")
                .setPositiveButton("确定", null)
                .show();
    }

    private void successAlert() {
        new AlertDialog.Builder(this)
                .setTitle("修改成功")
                .setMessage("昵称修改成功")
                .setPositiveButton("确定", null)
                .show();
    }

    private void failAlert(int code) {
        new AlertDialog.Builder(this)
                .setTitle("修改错误")
                .setMessage("昵称修改错误，错误码: " + code)
                .setPositiveButton("确定", null)
                .show();
    }
}