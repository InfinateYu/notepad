# web主程序
from flask import Flask
from flask import request

import os
import sys
import shutil
import hashlib

import database as User

app = Flask(__name__)

user_list = []

data_path = ""

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
@app.route("/test")
def test():
    return "connect successful"


# 用于用户登录
# params = {username : user, password : pwd}
@app.route("/login", methods=["GET"])
def login():
    global user_list
    try:
        if request.method == "GET":
            user = request.args.get("username", "")
            pwd = request.args.get("password", "")
            
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


# 用于用户注册
# params = {username : user, password : pwd}
@app.route("/register", methods=["POST"])
def register():
    global data_path
    try:
        if request.method == "POST":
            user = request.args.get("username", "")
            pwd = request.args.get("password", "")

            salt = hashlib.md5(pwd[::-1].encode()).hexdigest()
            hash_pwd = hashlib.sha256((pwd + salt).encode()).hexdigest()

            code = User.register(user=user, pwd=hash_pwd, path=data_path)

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
                    os.makedirs(user_path + "/notes")
                    os.makedirs(user_path + "/profile")
                    shutil.copy(src=data_path + "default/profile/profile.dat", dst=user_path + "/profile")
                return {"status" : 16, "description" : "register successful"}
            else:
                return {"status" : 14, "description" : "login status error"}
        else:
            return {"status" : 13, "description" : "request error"}
    except:
        return {"status" : 13, "description" : "connect error"}


# 用于笔记保存
# 未完成
# param = {username : user, note : {"title" : "", "tags" : [str], "content" : [str]}}
@app.route("/save", methods=["POST"])
def save():
    pass


# 用于删除笔记
# 未完成
# param = {username : user, title : ""}
@app.route("/delnote", methods=["POST"])
def deleteNote():
    pass



# 用于更新用户信息
# 未完成
# param = {username : user}
# data
@app.route("/upduser", methods=["POST"])
def updateUser():
    # request.form["profile"]
    pass


# 用于删除用户
# param = {username : user}
@app.route("/deluser", methods=["DELETE"])
def deleteUser():
    # 需要删除数据库记录和本地的库
    global user_list, data_path
    try:
        if request.method == "DELETE":
            user = request.args.get("username", "")
            
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
    app.run(debug=True, host="0.0.0.0")