package com.example.notepad;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.CountDownLatch;

import account.AccountManager;

public class ChangePasswordActivity extends AppCompatActivity {
    private AccountManager manager;
    int code;
    private TextView textViewUsername;
    private EditText editTextNewPassword;
    private Button buttonCancel, buttonConfirm;

    private static final String SHARED_PREFS_NAME = "user_info";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        manager = new AccountManager();
        code = 0;

        textViewUsername = findViewById(R.id.text_view_username);
        editTextNewPassword = findViewById(R.id.edit_text_new_password);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonConfirm = findViewById(R.id.button_confirm);

        // 获取从 SettingsFragment 传递过来的用户名
        String username = getIntent().getStringExtra("username");
        textViewUsername.setText("当前账号: " + username);

        buttonCancel.setOnClickListener(v -> finish());
        buttonConfirm.setOnClickListener(v -> {
            String newPassword = editTextNewPassword.getText().toString();
            if (newPassword.isEmpty()) {
                // 显示密码不能为空的提示
                showPasswordEmptyAlert();
            } else {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
                String password = sharedPreferences.getString(KEY_PASSWORD, "");
                if (password.equals(newPassword)) {
                    showPasswordSameAlert();
                }
                // 保存新密码并关闭 Activity
                try {
                    final CountDownLatch downLatch = new CountDownLatch(1);
                    new Thread(() -> {
                        code = manager.updateUser(username, "", "", "", password);
                        downLatch.countDown();
                    }).start();
                    downLatch.await();

                    if (code == 11) {
                        // 修改密码成功
                        updatePasswordInSharedPreferences(username, newPassword);
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

    private void updatePasswordInSharedPreferences(String username, String newPassword) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PASSWORD, newPassword);
        editor.apply();
    }

    private void showPasswordEmptyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("密码不能为空")
                .setPositiveButton("确定", null)
                .show();
    }

    private void showPasswordSameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("密码不能与原密码相同")
                .setPositiveButton("确定", null)
                .show();
    }

    private void successAlert() {
        new AlertDialog.Builder(this)
                .setTitle("修改成功")
                .setMessage("密码修改成功")
                .setPositiveButton("确定", null)
                .show();
    }
    private void failAlert(int code) {
        new AlertDialog.Builder(this)
                .setTitle("修改错误")
                .setMessage("密码修改错误，错误码: " + code)
                .setPositiveButton("确定", null)
                .show();
    }
}