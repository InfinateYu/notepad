package com.example.notepad;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import account.AccountManager;

public class SettingsFragment extends Fragment {
    private AccountManager manager;
    int code;
    private ListView listView;
    private SettingsAdapter adapter;
    private List<ListItem> itemList;
    private ImageView imageViewAvatar;
    private TextView textViewUsername;
    private TextView textViewSignature;


    private static final String SHARED_PREFS_NAME = "user_info";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_SIGNATURE = "signature";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_NICKNAME = "nickname";

    @Override
    public void onResume() {
        super.onResume();
        refreshUserInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshUserInfo();
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        manager = new AccountManager();
        code = 0;

        // 初始化头像 ImageView 和用户名 TextView
        imageViewAvatar = root.findViewById(R.id.image_view_avatar);
        textViewUsername = root.findViewById(R.id.text_view_username);
        textViewSignature = root.findViewById(R.id.text_view_signature);

        // 初始化列表项数据
        itemList = new ArrayList<>();
        String username = getUsername();

        boolean isLoggedIn = getLoggedInState();
        if (isLoggedIn) {
            textViewUsername.setText(username);
            imageViewAvatar.setImageResource(R.drawable.default_avatar);
            itemList.add(new ListItem("修改账号信息", true, false));
            itemList.add(new ListItem("修改密码", true, false));
            itemList.add(new ListItem("退出登录", true, false));
        } else {
            textViewUsername.setText("未登录");
            textViewSignature.setText("");
            imageViewAvatar.setImageResource(R.drawable.default_avatar);
            itemList.add(new ListItem("登录/注册", true, false));
        }

        // 设置适配器
        adapter = new SettingsAdapter(requireContext(), itemList);

        // 初始化 ListView
        listView = root.findViewById(R.id.list_view_settings);
        listView.setAdapter(adapter);

        // 设置 ListView 的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem clickedItem = itemList.get(position);
                String text = clickedItem.getText();
                Log.d("SettingsFragment", "Clicked item text: " + text);
                Log.d("SettingsFragment", "Starting LoginRegisterActivity");

                if (text.equals("登录/注册")) {
                    // 点击登录/注册，跳转到登录注册界面
                    Intent intent = new Intent(getContext(), LoginRegisterActivity.class);
                    startActivity(intent);
                } else if (text.equals("退出登录")) {
                    // 退出登录
                    setLoggedInState(false);

                    // 先处理本地数据
                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String username = sharedPreferences.getString(KEY_USERNAME, "");
                    editor.putBoolean(KEY_IS_LOGGED_IN, false);
                    editor.remove(KEY_USERNAME);
                    editor.remove(KEY_PASSWORD);
                    editor.remove(KEY_SIGNATURE);
                    editor.remove(KEY_AVATAR);
                    editor.remove(KEY_NICKNAME);
                    editor.apply();
                    // 和服务器连接
                    try {
                        final CountDownLatch downLatch = new CountDownLatch(1);
                        new Thread(() -> {
                            code = manager.logout(username);
                            downLatch.countDown();
                        }).start();
                        downLatch.await();
                        if (code == 11) {
                            setUsername("");
                            textViewUsername.setText("未登录");
                            textViewSignature.setText("");
                            imageViewAvatar.setImageResource(R.drawable.default_avatar);
                            itemList.clear();
                            itemList.add(new ListItem("登录/注册", true, false));
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (text.equals("修改账号信息")) {
                    // 跳转到修改账号界面
                    Intent intent = new Intent(getContext(), AccountInfoActivity.class);
                    startActivity(intent);
                } else if (text.equals("修改密码")) {
                    // 跳转到修改密码界面
                    Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
                    intent.putExtra("username", getUsername());
                    startActivity(intent);
                }
            }
        });

        return root;
    }

    private String getUsername() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, requireContext().MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    private boolean getLoggedInState() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, requireContext().MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private void setUsername(String username) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    private void setLoggedInState(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    private String getUserSignature() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, requireContext().MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SIGNATURE, "");
    }

    private void setUserSignature(String signature) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SIGNATURE, signature);
        editor.apply();
    }
    private int getUserAvatar() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, requireContext().MODE_PRIVATE);
        int avatarResId = sharedPreferences.getInt(KEY_AVATAR, R.drawable.default_avatar);
        return avatarResId;
    }

    private void setUserAvatar(int avatarResId) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS_NAME, requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_AVATAR, avatarResId);
        editor.apply();
    }
    private void refreshUserInfo() {
        String username = getUsername();
        String signature = getUserSignature();
        boolean isLoggedIn = getLoggedInState();
        if (isLoggedIn) {
            textViewUsername.setText(username);
            textViewSignature.setText(signature);
            imageViewAvatar.setImageResource(R.drawable.default_avatar);
            itemList.clear();
            itemList.add(new ListItem("修改账号信息", true, false));
            itemList.add(new ListItem("修改密码", true, false));
            itemList.add(new ListItem("退出登录", true, false));
        } else {
            textViewUsername.setText("未登录");
            textViewSignature.setText("");
            imageViewAvatar.setImageResource(R.drawable.default_avatar);
            itemList.clear();
            itemList.add(new ListItem("登录/注册", true, false));
        }
        adapter.notifyDataSetChanged();
    }


}