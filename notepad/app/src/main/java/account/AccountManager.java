package account;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.*;

import java.util.Arrays;
import java.util.List;
import java.util.Base64;

import java.net.URL;
import java.net.HttpURLConnection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class AccountManager {
    private String username;
    private String password;

    private final String database_url = "http://103.40.13.87:30907/"; // 前缀

    // private final String ERROR_CONNECTION = "网络错误或账号不存在";

    public AccountManager() {
        username = "";
        password = "";
    }

    // 设置当前登录信息
    public void set(@NonNull String username, @NonNull String password) {
        // 使用base64简单加密
        this.username = username;
        this.password = Base64.getEncoder().encodeToString(password.getBytes());
    }

    // 测试接口
    public final String test() {
        try {
            if (username.isEmpty() || password.isEmpty()) {
                throw new IOException();
            }

            String url = "http://192.168.132.129:5000/test?username=" + username;
            JSONObject jsonObject = new JSONObject();
            List<String> ls = Arrays.asList("abc", "avsaa", "1rfw");
            jsonObject.put("password", password);
            jsonObject.put("notes", ls);


            String json = JSON.toJSONString(jsonObject);
            HttpURLConnection connection = getHttpURLConnection(url, json, "POST");

            // int responseCode = connection.getResponseCode();
            String retJson = getReturnJson(connection);

            connection.disconnect();
            return retJson;
        }
        catch(Exception e) {
            return "error";
        }
    }

    // 创建账户
    public final boolean register() {
        try {
            if (username.isEmpty() || password.isEmpty()) {
                throw new IOException();
            }

            String url = database_url + "register?username=" + username;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("password", password);

            String json = JSON.toJSONString(jsonObject);
            HttpURLConnection connection = getHttpURLConnection(url, json, "POST");

            // int responseCode = connection.getResponseCode();

            String retJson = getReturnJson(connection);
            // TODO: 解析返回的json
            JSONObject object = JSON.parseObject(retJson);

            connection.disconnect();
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }

    // 登录账户
    public final boolean login(String username, String password) {
        // TODO: 完全没做
        return false;
    }

    private static HttpURLConnection getHttpURLConnection(String url, String json, String method) throws IOException {
        URL regUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection)regUrl.openConnection();

        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(json);
        outputStream.flush();
        outputStream.close();

        connection.connect();
        return connection;
    }

    private static String getReturnJson(HttpURLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            boolean flag = false;
            while ((line = reader.readLine()) != null) {
                if (flag) {
                    builder.append(System.getProperty("line.separator"));
                } else {
                    flag = true;
                }
                builder.append(line);
            }
            String json = builder.toString();
            reader.close();
            return json;
        }
        catch (Exception e) {
            return "";
        }
    }
}
