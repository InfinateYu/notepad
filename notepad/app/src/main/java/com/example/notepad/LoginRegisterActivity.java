package com.example.notepad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import java.util.Base64;

import account.AccountManager;

public class LoginRegisterActivity extends AppCompatActivity {
    private AccountManager manager;
    int code;
    Map<String, Object> map;

    private TextView textViewTitle;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonCancel;
    private Button buttonLogin;
    private Button buttonLoginMode;
    private Button buttonRegisterMode;

    private boolean isLoginMode = true;
    private boolean isLoggedIn = false;

    private static final String SHARED_PREFS_NAME = "user_info";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_SIGNATURE = "signature";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_NICKNAME = "nickname";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        manager = new AccountManager();
        code = 0;
        map = new HashMap<>();

        textViewTitle = findViewById(R.id.text_view_title);
        editTextUsername = findViewById(R.id.edit_text_username);
        editTextPassword = findViewById(R.id.edit_text_password);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonLogin = findViewById(R.id.button_login);
        buttonLoginMode = findViewById(R.id.button_login_mode);
        buttonRegisterMode = findViewById(R.id.button_register_mode);

        buttonCancel.setOnClickListener(v -> finish());

        buttonLoginMode.setOnClickListener(v -> {
            textViewTitle.setText("登录");
            buttonLogin.setText("登录");
            isLoginMode = true;
        });

        buttonRegisterMode.setOnClickListener(v -> {
            textViewTitle.setText("注册");
            buttonLogin.setText("注册");
            isLoginMode = false;
        });

        buttonLogin.setOnClickListener(v -> {
            if (isLoginMode) {
                // 处理登录逻辑
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                // 检查账号和密码是否为空
                if (username.isEmpty()) {
                    // 账号为空,给出提示
                    showUsernameEmptyAlert();
                    return;
                }
                if (password.isEmpty()) {
                    // 密码为空,给出提示
                    showPasswordEmptyAlert();
                    return;
                }
                // 检查账号和密码是否符合要求
                if (!isValidInput(username, password)) {
                    showInvalidInputAlert();
                    return;
                }
                try {
                    final CountDownLatch downLatch = new CountDownLatch(1);
                    // 验证输入的账号和密码是否与保存的一致
                    new Thread(() -> {
                        map = manager.login(username, password);
                        downLatch.countDown();
                    }).start();
                    downLatch.await();
                    code = (int) map.get("status");
                    if (code == 11) {
                        // 登录成功
                        // 保存信息到 SharedPreferences
                        String profile = (String) map.get("profile");
                        String signature = (String) map.get("signature");
                        String nickname = (String) map.get("nickname");
                        saveUserCredentials(username, password, nickname, profile, signature);

                        // TODO: 这里还需要对笔记内容做处理（需要笔记部分完成）

                        setLoggedInState();
                        showLoginSuccessAlert();
                        startActivity(new Intent(LoginRegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // 登录失败,给出提示
                        showInvalidLoginAlert(code);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // 处理注册逻辑
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                // 检查账号和密码是否为空
                if (username.isEmpty()) {
                    // 账号为空,给出提示
                    showUsernameEmptyAlert();
                    return;
                }
                if (password.isEmpty()) {
                    // 密码为空,给出提示
                    showPasswordEmptyAlert();
                    return;
                }
                // 检查账号和密码是否符合要求
                if (!isValidInput(username, password)) {
                    showInvalidInputAlert();
                    return;
                }
                try {
                    final CountDownLatch downLatch = new CountDownLatch(1);
                    new Thread(() -> {
                        code = manager.register(username, password);
                        downLatch.countDown();
                    }).start();
                    downLatch.await();

                    if (code == 17) {
                        // 注册成功,给出提示
                        showRegistrationSuccessAlert();
                        startActivity(new Intent(LoginRegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        showInvalidRegisterAlert(code);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 检查之前的登录状态
        isLoggedIn = getLoggedInState();
        if (isLoggedIn) {
            // 已登录,直接跳转到主界面
            startActivity(new Intent(LoginRegisterActivity.this, MainActivity.class));
            finish();
        }
    }

    private void saveUserCredentials(String username, String password, String nickname, String profile, String signature) {
        // 需要先把头像存在本地路径
        byte[] avatar = Base64.getDecoder().decode(profile); // 解码后的二进制数据
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
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(avatar, 0, avatar.length);
            fileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 把昵称、个签处理一下
        String nkname = new String(Base64.getDecoder().decode(nickname), StandardCharsets.UTF_8);
        String sig = new String(Base64.getDecoder().decode(signature), StandardCharsets.UTF_8);
        // 然后再写信息
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_NICKNAME, nkname);
        editor.putString(KEY_AVATAR, path);
        editor.putString(KEY_SIGNATURE, sig);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    private boolean getLoggedInState() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void setLoggedInState() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    private void showUsernameEmptyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("账号不能为空")
                .setPositiveButton("确定", null)
                .show();
    }

    private void showPasswordEmptyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("密码不能为空")
                .setPositiveButton("确定", null)
                .show();
    }

    private void showInvalidRegisterAlert(int code) {
        new AlertDialog.Builder(this)
                .setTitle("注册失败")
                .setMessage("错误码: " + code)
                .setPositiveButton("确定", null)
                .show();
    }

    private void showLoginSuccessAlert() {
        new AlertDialog.Builder(this)
                .setTitle("登录成功")
                .setMessage("欢迎登录!")
                .setPositiveButton("确定", null)
                .show();
    }

    private void showInvalidLoginAlert(int code) {
        new AlertDialog.Builder(this)
                .setTitle("登录失败")
                .setMessage("错误码: " + code)
                .setPositiveButton("确定", null)
                .show();
    }

    private void showRegistrationSuccessAlert() {
        new AlertDialog.Builder(this)
                .setTitle("注册成功")
                .setMessage("欢迎注册!")
                .setPositiveButton("确定", null)
                .show();
    }
    private boolean isValidInput(String username, String password) {
        // 检查输入是否仅包含字母和数字
        String regex = "^[a-zA-Z0-9]*$";
        return username.matches(regex) && password.matches(regex);
    }

    private void showInvalidInputAlert() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("账号和密码只能由字母和数字组成")
                .setPositiveButton("确定", null)
                .show();
    }
}