package account;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.util.Base64;

import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class SecureAccountManager {
    /*
    账户的创建与登录相关处理
    使用SHA256+盐进行加密
    */

    private static final int CREATE_ACCOUNT = 1001;
    private static final int LOGIN_ACCOUNT = 1002;
    private static final int GET_SALT = 1003;

    private final String database_url = "http://103.40.13.87:30907/"; // 前缀

    private final String ERROR_CONNECTION = "网络错误或账号不存在";


    // 创建账户
    public boolean createAccount(String username, String password) {
        sendAccountInfo(username, encrypted_password, salt, CREATE_ACCOUNT);
        return true;
    }

    // 登录账户
    public boolean login(String username, String password) {

        boolean success = sendAccountInfo(username, password, salt, LOGIN_ACCOUNT);

        // TODO
        return success;
    }


    // HTTPS传输
    public boolean sendAccountInfo(String username, String password, String salt, int code) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(database_url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());

            String encrypted_password = encryptPassword(password, salt);
            String json = "{\"code\":\"" + code + "\"username\":\"" + username
                    + "\",\"password\":\"" + encrypted_password
                    + "\",\"salt\":\"" + salt + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (code == CREATE_ACCOUNT) {
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    // 传输成功

                    return true;
                }
                else {
                    throw new RuntimeException();
                }
            }
            else if (code == LOGIN_ACCOUNT) {
                // TODO

                return true;
            }
            else {
                throw new IOException();
            }

        } catch (Exception e) {
            // 异常处理
            // TODO

            return false;
        }
    }
}
