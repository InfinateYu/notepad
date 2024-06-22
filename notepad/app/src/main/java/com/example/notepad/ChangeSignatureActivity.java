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

public class ChangeSignatureActivity extends AppCompatActivity {
    private AccountManager manager;
    int code;
    private static final String SHARED_PREFS_NAME = "user_info";
    private static final String KEY_SIGNATURE = "signature";
    private static final String KEY_USERNAME = "username";

    private TextView textViewCurrentSignature;
    private EditText editTextNewSignature;
    private Button buttonCancel, buttonConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_signature);

        manager = new AccountManager();
        code = 0;

        // 初始化控件
        textViewCurrentSignature = findViewById(R.id.text_view_current_signature);
        editTextNewSignature = findViewById(R.id.edit_text_new_signature);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonConfirm = findViewById(R.id.button_confirm);

        // 获取当前个性签名并显示
        String currentSignature = getSignature();
        textViewCurrentSignature.setText("当前个性签名: " + currentSignature);

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
                String newSignature = editTextNewSignature.getText().toString().trim();
                if (newSignature.isEmpty()) {
                    emptyAlert();
                }
                if (newSignature.length() > 64) {
                    tooLongAlert();
                }
                if (newSignature.equals(currentSignature)) {
                    sameAlert();
                }

                try {
                    String username = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).getString(KEY_USERNAME, "");

                    final CountDownLatch downLatch = new CountDownLatch(1);
                    new Thread(() -> {
                        code = manager.updateUser(username, "", newSignature, "", "");
                        downLatch.countDown();
                    }).start();
                    downLatch.await();

                    if (code == 11) {
                        // 修改个签成功
                        setSignature(newSignature);
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

    private String getSignature() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SIGNATURE, "");
    }

    private void setSignature(String signature) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SIGNATURE, signature);
        editor.apply();
    }

    private void emptyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("请输入合法的签名")
                .setPositiveButton("确定", null)
                .show();
    }

    private void tooLongAlert() {
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("签名过长（不超过64个字符）")
                .setPositiveButton("确定", null)
                .show();
    }

    private void sameAlert() {
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("不得与原签名相同")
                .setPositiveButton("确定", null)
                .show();
    }

    private void successAlert() {
        new AlertDialog.Builder(this)
                .setTitle("修改成功")
                .setMessage("个性签名修改成功")
                .setPositiveButton("确定", null)
                .show();
    }

    private void failAlert(int code) {
        new AlertDialog.Builder(this)
                .setTitle("修改错误")
                .setMessage("个性签名修改错误，错误码: " + code)
                .setPositiveButton("确定", null)
                .show();
    }
}