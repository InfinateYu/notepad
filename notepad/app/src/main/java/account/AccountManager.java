package account;

import java.util.Base64;
import java.net.URL;
import java.net.HttpURLConnection;

import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOError;
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
    public void set(String username, String password) {
        // 使用base64简单加密
        this.username = username;
        this.password = Base64.getEncoder().encodeToString(password.getBytes());
    }
    
    // 创建账户
    public final boolean register() {
        try {
            if (username.equals("") || password.equals("")) {
                throw new IOException();
            }

            String url = database_url + "register?username=" + username;
            String json = "{\"password\" : \"" + password + "\"}";

            URL regUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)regUrl.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(json);
            outputStream.flush();
            outputStream.close();

            connection.connect();

            // int responseCode = connection.getResponseCode();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            Boolean flag = false;
            while ((line = reader.readLine()) != null) {
                if (flag) {
                    builder.append(System.getProperty("line.separator"));
                }
                else {
                    flag = true;
                }
                builder.append(line);
            }
            String retJson = builder.toString();
            // TODO: 解析返回的json

            reader.close();
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
}
