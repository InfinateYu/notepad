# web主程序
from flask import Flask
from flask import request

import hashlib

from database import User

app = Flask(__name__)


# 测试用例
# 对应的原密码：password
# 加密方式：sha256+盐，盐：密码串的反转的md5
user_list = {"user" : "356431046888a65fcb078e3151f78d8b5678392bb1a8514139475c939f233cd7"}

login_user: User


# 一个测试接口
@app.route("/test", methods=["GET"])
def test():
    return "connect successful"


# 用于用户登录
# 未完成
# params = {username : user, password : pwd}
# status code: 10用户名错误 11成功 12密码错误 13连接错误或其他错误
@app.route("/login", methods=["GET"])
def login():
    global login_user
    try:
        if request.method == "GET":
            user = request.args.get("username", "")
            pwd = request.args.get("password", "")
            
            salt = hashlib.md5(pwd[::-1].encode()).hexdigest()
            hash_pwd = hashlib.sha256((pwd + salt).encode()).hexdigest()

            code = login_user.login(user=user, pwd=hash_pwd)

            if code == User.UKNOWN_ERROR:
                return {"status" : 13, "description" : "unknown error"}
            elif code == User.USERNAME_ERROR:
                return {"status" : 10, "description" : "invalid username"}
            elif code == User.PASSWORD_ERROR:
                return {"status" : 12, "description" : "password error"} 
            elif code == User.NO_ERROR:
                # 这里要返回账户内容
    
                return {"status" : 11, "description" : "login successful", "notes" : login_user.getNotes()}

                
    except:
        return {"status" : 13, "description" : "request error"}


# 用于用户注册
# 未完成
# params = {username : user, password : pwd}
@app.route("/register", methods=["POST"])
def register():
    pass


# 用于笔记保存
# 未完成
@app.route("/save", methods=["POST"])
def save():
    pass


if __name__ == '__main__':
    login_user = User()
    app.run(debug=True, host="0.0.0.0")