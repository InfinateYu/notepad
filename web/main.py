# web主程序
from flask import Flask
from flask import request
from waitress import serve

import os
import sys
import json
import base64
import shutil
import hashlib

import database as User
import api_html as api

app = Flask(__name__)

user_list = []

data_path = ""

# title = "b'5L2/55So6K+05piO'"

# 测试用例
# 对应的原密码：password
# 加密方式：sha256+盐，盐：密码串的反转的md5
# user_list = {"user" : "356431046888a65fcb078e3151f78d8b5678392bb1a8514139475c939f233cd7"}

# status code:
# 10用户名错误 11成功
# 12密码错误 13连接或请求错误等
# 14状态错误 15缺少输入（注册）
# 16已存在用户名 17注册成功

# 一个测试接口
@app.route("/test", methods=["GET", "POST"])
def test():
    return "connect successful"


# 获取所有接口
@app.route("/getapi", methods=["GET"])
def getApi():
    return api.html


# 用于用户登录
# json = {username : user, password : pwd}
@app.route("/login", methods=["POST"])
def login():
    global user_list
    try:
        if request.method == "POST":
            user = request.json.get("username", "")
            pwd = request.json.get("password", "")
            
            if user == "" or pwd == "":
                return {"status" : 15, "description" : "insufficient input"}
            if user in user_list:
                return {"status" : 14, "description" : "already login"}
            
            salt = hashlib.md5(pwd[::-1].encode()).hexdigest()
            hash_pwd = hashlib.sha256((pwd + salt).encode()).hexdigest()
            code = User.login(user=user, pwd=hash_pwd)

            if code == User.UKNOWN_ERROR:
                return {"status" : 13, "description" : "unknown error"}
            elif code == User.USERNAME_ERROR:
                return {"status" : 10, "description" : "invalid username"}
            elif code == User.PASSWORD_ERROR:
                return {"status" : 12, "description" : "password error"} 
            elif code == User.NO_ERROR:
                user_list.append(user)
                info = User.getInfo(user)
                notes = User.getNotes(user)
                if info["status"] == User.NO_ERROR and notes["status"] == User.NO_ERROR:
                    return {"status" : 11, "description" : "login successful", "nickname" : info["nickname"], "profile" : info["profile"], "notes" : notes["notes"]}
                else:
                    return {"status" : 13, "description" : "unknown error"}
        else:
            return {"status" : 13, "description" : "request error"}
                
    except:
        return {"status" : 13, "description" : "connect error"}


# 退出登录
# json = {username : user}
@app.route("/logout", methods=["POST"])
def logout():
    global user_list
    try:
        if request.method == "POST":
            user = request.json.get("username", "")
        
            if user == "":
                return {"status" : 15, "description" : "insufficient input"}
            
            if user in user_list:
                user_list.remove(user)
                return {"status" : 11, "description" : "logout successful"}
            else:
                return {"status" : 14, "description" : "not logged in"}
    except:
        return {"status" : 13, "description" : "connect error"}
    

# 用于用户注册
# json = {username : user, password : pwd}
@app.route("/register", methods=["POST"])
def register():
    try:
        if request.method == "POST":
            user = request.json.get("username", "")
            pwd = request.json.get("password", "")

            salt = hashlib.md5(pwd[::-1].encode()).hexdigest()
            hash_pwd = hashlib.sha256((pwd + salt).encode()).hexdigest()

            nickname = base64.b64encode(user.encode()).decode()

            code = User.register(user=user, pwd=hash_pwd, nickname=nickname, path=data_path)

            if code == User.UKNOWN_ERROR:
                return {"status" : 13, "description" : "unknown error"}
            elif code == User.NULL_INPUT_ERROR:
                return {"status" : 15, "description" : "insufficient input"}
            elif code == User.EXIST_NAME_ERROR:
                return {"status" : 16, "description" : "exist username"}
            elif code == User.NO_ERROR:
                # 建立本地数据存储
                user_path = data_path + "user_" + user
                if not os.path.exists(user_path):
                    os.makedirs(user_path + "/notes/note1")
                    os.makedirs(user_path + "/profile")
                    shutil.copy(src=data_path + "default/profile/profile.dat", dst=user_path + "/profile")
                    shutil.copy(src=data_path + "default/notes/note1/tags.dat", dst=user_path + "/notes/note1")
                    shutil.copy(src=data_path + "default/notes/note1/content.dat", dst=user_path + "/notes/note1")
                return {"status" : 17, "description" : "register successful"}
            else:
                return {"status" : 14, "description" : "login status error"}
        else:
            return {"status" : 13, "description" : "request error"}
    except:
        return {"status" : 13, "description" : "connect error"}


# 用于笔记保存
# json = {username : user, title : title, (可选)new_title : new_title, tags : [tags], content : [content]}
@app.route("/save", methods=["POST"])
def save():
    global user_list
    try:
        if request.method == "POST":
            user = request.json.get("username", "")
            title = request.json.get("title", "")
            new_title = request.json.get("new_title", "")
            tags = request.json.get("tags", [])
            content = request.json.get("content", [])

            if user == "" or title == "":
                return {"status" : 15, "description" : "insufficient input"}
            if user not in user_list:
                return {"status" : 14, "description" : "not logged in"}
            
            info = User.saveNote(username=user, title=title, new_title=new_title)

            if info["status"] == User.UKNOWN_ERROR:
                return {"status" : 13, "description" : "unknown error"}
            elif info["status"] == User.EXIST_ERROR:
                return {"status" : 13, "description" : "exist error"}
            elif info["status"] == User.NULL_INPUT_ERROR:
                return {"status" : 15, "description" : "insufficient input"}
            elif info["status"] == User.NO_ERROR:
                # 更新本地存储
                path = info["path"]

                if not os.path.exists(path):
                    os.makedirs(path)

                with open(path + "/content.dat", "wb") as file:
                    for para in content:
                        file.write((para + "\n").encode())

                with open(path + "/tags.dat", "wb") as file:
                    for tag in tags:
                        file.write((tag + "\n").encode())

                    return {"status" : 11, "description" : "save successful"}
            else:
                return {"status" : 13, "description" : "unknown error"}
        else:
                return {"status" : 13, "description" : "unknown error"}

    except:
        return {"status" : 13, "description" : "connect error"}


# 用于删除笔记
# json = {username : user, title : title}
@app.route("/delnote", methods=["POST"])
def deleteNote():
    global user_list
    try:
        if request.method == "POST":
            user = request.json.get("username", "")
            title = request.json.get("title", "")

            if user == "" or title == "":
                return {"status" : 15, "description" : "insufficient input"}
            if user not in user_list:
                return {"status" : 14, "description" : "not logged in"}
            
            info = User.deleteNote(username=user, title=title)

            if info["status"] == User.UKNOWN_ERROR:
                return {"status" : 13, "description" : "unknown error"}
            elif info["status"] == User.EXIST_ERROR:
                return {"status" : 13, "description" : "exist error"}
            elif info["status"] == User.NULL_INPUT_ERROR:
                return {"status" : 15, "description" : "insufficient input"}
            elif info["status"] == User.NO_ERROR:
                # 更新本地存储
                path = info["path"]
                if os.path.exists(path):
                    shutil.rmtree(path)

                    return {"status" : 11, "description" : "delete successful"}
                else:
                    return {"status" : 13, "description" : "unknown error"}
            else:
                    return {"status" : 13, "description" : "unknown error"}

    except:
        return {"status" : 13, "description" : "connect error"}



# 用于更新用户信息
# json = {username : user, (可选)nickname : nickname, (可选)profile : profile, (可选)password : pwd}
@app.route("/upduser", methods=["POST"])
def updateUser():
    global user_list
    try:
        if request.method == "POST":
            user = request.json.get("username", "")
            nickname = request.json.get("nickname", "")
            profile = request.json.get("profile", "")
            pwd = request.json.get("password", "")

            if user == "" or (nickname == "" and profile == "" and pwd == ""):
                return {"status" : 15, "description" : "insufficient input"}
            if user not in user_list:
                return {"status" : 14, "description" : "not logged in"}
            
            salt = hashlib.md5(pwd[::-1].encode()).hexdigest()
            hash_pwd = hashlib.sha256((pwd + salt).encode()).hexdigest()
            
            info = User.updateInfo(username=user, nickname=nickname, password=hash_pwd)

            if info["status"] == User.UKNOWN_ERROR:
                return {"status" : 13, "description" : "unknown error"}
            elif info["status"] == User.NULL_INPUT_ERROR:
                return {"status" : 15, "description" : "insufficient input"}
            elif info["status"] == User.NO_ERROR:
                # 更新本地存储
                if profile != "":
                    path = info["path"]

                    if os.path.exists(path):
                        with open(path, "wb") as file:
                            file.write(profile)

                return {"status" : 11, "description" : "update successful"}
            else:
                return {"status" : 13, "description" : "unknown error"}
        else:
                return {"status" : 13, "description" : "unknown error"}

    except:
        return {"status" : 13, "description" : "connect error"}


# 用于删除用户
# json = {username : user}
@app.route("/deluser", methods=["POST"])
def deleteUser():
    # 需要删除数据库记录和本地的库
    global user_list
    try:
        if request.method == "POST":
            user = request.json.get("username", "")
            
            if user in user_list:
                code = User.deleteUser(username=user)

                if code == User.UKNOWN_ERROR:
                    return {"status" : 13, "description" : "unknown error"}
                elif code == User.NULL_INPUT_ERROR:
                    return {"status" : 15, "description" : "insufficient input"}
                elif code == User.NO_ERROR:
                    user_path = data_path + "user_" + user
                    if os.path.exists(user_path):
                        shutil.rmtree(user_path)
                    user_list.remove(user)
                    return {"status" : 16, "description" : "close account successful"}
                else:
                    return {"status" : 14, "description" : "login status error"}
            else:
                return {"status" : 10, "description" : "invalid username"}
        else:
            return {"status" : 13, "description" : "request error"}
    except:
        return {"status" : 13, "description" : "connect error"}


if __name__ == '__main__':
    data_path = sys.argv[0].replace("main.py", "data/").replace("\\", "/")
    json_path = sys.argv[0].replace("main.py", "host.json").replace("\\", "/")

    try:
        # 加载host信息
        with open(json_path, "rb") as file:
            js = json.load(file)
            if "host" in js.keys() and "port" in js.keys():
                host = js.get("host", "0.0.0.0")
                port = js.get("port", 5000)
                serve(app=app, host=host, port=port)
            else:
                raise Exception("信息错误")
    except:
        serve(app=app, host="0.0.0.0", port=5000)