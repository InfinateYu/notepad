package account;

import com.alibaba.fastjson2.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import java.net.URL;
import java.net.HttpURLConnection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.Thread;

/** 用于和后端交互的类。该类不保存用户的笔记头像等信息，需要在其他地方处理保存
 * @author 雨相羽
*/
import androidx.annotation.NonNull;

public class AccountManager {
    private static final String database_url = "http://103.40.13.87:30907/"; // 前缀
    private static final Set<String> methods = Set.of("GET", "POST", "DELETE"); // 请求方法集合

    public AccountManager() {
    }


    /** 创建账户
     * @param username 用户名
     * @param password 密码
     * @return 返回一个int，为状态码。成功与否可以通过状态码判断
    */ 
    public final int register(String username, String password) {
        try {
            if (username.isEmpty() || password.isEmpty()) {
                throw new IOException();
            }

            String url = database_url + "register";
            JSONObject json_object = new JSONObject();
            json_object.put("username", username);
            json_object.put("password", password);

            String json = JSON.toJSONString(json_object);
            HttpURLConnection connection = getHttpURLConnection(url, json, "POST");

            // int responseCode = connection.getResponseCode();

            String ret_json = getReturnJson(connection);

            // 解析返回的json
            JSONObject object = JSON.parseObject(ret_json);
            int code = object.getIntValue("status");
            return code;
        }
        catch(Exception e) {
            e.printStackTrace();
            return 13;
        }
    }

    /** 登录账户
     * @param username 用户名
     * @param password 密码
     * @return 返回一个Map，如果登录成功，则包括用户的所有信息，否则仅包括状态码。成功与否可以通过状态码判断
    */ 
    public final Map<String, Object> login(String username, String password) {
        try {
            if (username.isEmpty() || password.isEmpty()) {
                throw new IOException();
            }

            String url = database_url + "login";
            JSONObject json_object = new JSONObject();
            json_object.put("username", username);
            json_object.put("password", password);

            String json = JSON.toJSONString(json_object);
            HttpURLConnection connection = getHttpURLConnection(url, json, "POST");

            // int responseCode = connection.getResponseCode();

            String ret_json = getReturnJson(connection);

            // 解析返回的json
            JSONObject object = JSON.parseObject(ret_json);
            int code = object.getIntValue("status");
            Map<String, Object> map = new HashMap<>();
            map.put("status", code);
            if (code == 11) {
                map.put("username", object.getString("username"));
                map.put("nickname", object.getString("nickname"));
                map.put("signature", object.getString("signature"));
                map.put("profile", object.getString("profile"));
                map.put("notes", JSON.parseArray(object.getJSONArray("notes").toJSONString(), String.class));
            }
            return map;
        }
        catch(Exception e) {
            e.printStackTrace();
            Map<String, Object> error_mp = new HashMap<>();
            error_mp.put("status", 13);
            return error_mp;
        }
    }

    /** 退出登录
     * @param username 用户名
     * @return 返回一个int，为状态码。成功与否可以通过状态码判断
    */ 
    public final int logout(String username) {
        try {
            if (username.isEmpty()) {
                throw new IOException();
            }

            String url = database_url + "logout";
            JSONObject json_object = new JSONObject();
            json_object.put("username", username);

            String json = JSON.toJSONString(json_object);
            HttpURLConnection connection = getHttpURLConnection(url, json, "POST");

            String ret_json = getReturnJson(connection);

            // 解析返回的json
            JSONObject object = JSON.parseObject(ret_json);
            int code = object.getIntValue("status");
            return code;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 13;
        }
    }

    /** 删除账户
     * @param username 用户名
     * @return 返回一个int，为状态码。成功与否可以通过状态码判断
    */
    public final int delUser(String username) {
        try {
            if (username.isEmpty()) {
                throw new IOException();
            }

            String url = database_url + "deluser";
            JSONObject json_object = new JSONObject();
            json_object.put("username", username);

            String json = JSON.toJSONString(json_object);
            HttpURLConnection connection = getHttpURLConnection(url, json, "POST");

            String ret_json = getReturnJson(connection);
            // 解析返回的json
            JSONObject object = JSON.parseObject(ret_json);
            int code = object.getIntValue("status");
            return code;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 13;
        }
    }

    /** 更新用户信息
     * @param username 用户名
     * @param nickname 用户昵称（可选，由于Java不支持缺省写法，为降低代码量，当不需要修改昵称时，该参数使用空字符串""）
     * @param signature 个性签名（可选，由于Java不支持缺省写法，为降低代码量，当不需要修改个签时，该参数使用空字符串""）
     * @param profile 用户简介（可选，由于Java不支持缺省写法，为降低代码量，当不需要修改简介时，该参数使用空字符串""）
     * @param new_password 新密码（可选，由于Java不支持缺省写法，为降低代码量，当不需要修改密码时，该参数使用空字符串""，但是三个参数不能同时缺省）
     * @return 返回一个int，为状态码。成功与否可以通过状态码判断
    */
    public final int updateUser(String username, String nickname, String signature, String profile, String new_password) {
        try {
            if (username.isEmpty()) {
                throw new IOException();
            }

            String url = database_url + "upduser";
            JSONObject json_object = new JSONObject();
            json_object.put("username", username);

            if (!signature.isEmpty()) {
                json_object.put("signature", Base64.getEncoder().encodeToString(signature.getBytes(StandardCharsets.UTF_8)));
            }
            if (!nickname.isEmpty()) {
                json_object.put("nickname", Base64.getEncoder().encodeToString(nickname.getBytes(StandardCharsets.UTF_8)));
            }
            if (!profile.isEmpty()) {
                json_object.put("profile", profile);
            }
            if (!new_password.isEmpty()) {
                json_object.put("password", new_password);
            }

            String json = JSON.toJSONString(json_object);
            HttpURLConnection connection = getHttpURLConnection(url, json, "POST");

            String ret_json = getReturnJson(connection);
            // 解析返回的json
            JSONObject object = JSON.parseObject(ret_json);
            int code = object.getIntValue("status");
            return code;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 13;
        }
    }
    
    /** 保存单篇笔记
     * @param username 用户名
     * @param title 笔记标题（加密后）
     * @param tags 笔记标签（加密后）
     * @param content 笔记内容（加密后）
     * @param new_title 新的笔记标题（加密后，可选，由于Java不支持缺省写法，为降低代码量，当不需要修改标题时，该参数使用空字符串""）
     * @return 返回一个int，为状态码。成功与否可以通过状态码判断
    */
    public final int saveNote(String username, String title, List<String> tags, List<String> content, String new_title){
        try {
            if (username.isEmpty()) {
                throw new IOException();
            }

            String url = database_url + "save";
            JSONObject json_object = new JSONObject();
            json_object.put("username", username);
            json_object.put("title", title);
            json_object.put("new_title", new_title);
            json_object.put("tags", tags);
            json_object.put("content", content);

            String json = JSON.toJSONString(json_object);
            HttpURLConnection connection = getHttpURLConnection(url, json, "POST");

            String ret_json = getReturnJson(connection);
            // 解析返回的json
            JSONObject object = JSON.parseObject(ret_json);
            int code = object.getIntValue("status");
            return code;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 13;
        }
    }

    /** 删除单篇笔记
     * @param username 用户名
     * @param title 笔记标题（加密后）
     * @return 返回一个int，为状态码。成功与否可以通过状态码判断
    */
    public final int deleteNote(String username, String title) {
        try {
            if (username.isEmpty()) {
                throw new IOException();
            }

            String url = database_url + "delnote";
            
            JSONObject json_object = new JSONObject();
            json_object.put("username", username);
            json_object.put("title", title);

            String json = JSON.toJSONString(json_object);
            HttpURLConnection connection = getHttpURLConnection(url, json, "POST");

            String ret_json = getReturnJson(connection);
            // 解析返回的json
            JSONObject object = JSON.parseObject(ret_json);
            int code = object.getIntValue("status");
            return code;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 13;
        }
    }

    /** 尝试和服务器建立连接
     * @param url 尝试连接的链接
     * @param json 连接时发送的json数据
     * @param method 连接方法
     * @return 如果请求正常，则返回一个HttpURLConnection对象，为和目标链接建立连接的对象，否则抛出一个IO异常
    */
    private static HttpURLConnection getHttpURLConnection(String url, String json, String method) throws IOException {
        if (!methods.contains(method)) {
            throw new IOException();
        }

        URL cUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection)cUrl.openConnection();

        connection.setConnectTimeout(3000);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setDoOutput(true);
        DataOutputStream output_stream = new DataOutputStream(connection.getOutputStream());
        output_stream.writeBytes(json);
        output_stream.flush();
        output_stream.close();

        connection.connect();
        return connection;
    }

    // 
    /** 获取服务器返回的信息
     * @param connection 和服务器建立连接的对象
     * @return 如果获取正常，则返回一个String，为返回的内容，否则抛出一个异常
    */
    private static String getReturnJson(HttpURLConnection connection) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append(System.getProperty("line.separator"));
        }
        String json = builder.toString();
        reader.close();
        connection.disconnect();
        return json;
    }
}
