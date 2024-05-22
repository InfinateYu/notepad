# 与数据库直接连接的接口
import MySQLdb
import time

# 错误类型
NO_ERROR = 100 # 无错误
UKNOWN_ERROR = 101 # 未知错误（用作其他错误类型的补充）
USERNAME_ERROR = 102 # 用户名输入错误（登录不存在的用户名）
PASSWORD_ERROR = 103 # 密码错误
EXIST_NAME_ERROR = 104 # 用户名输入错误（创建已存在的用户名）
NULL_INPUT_ERROR = 105 # 空白输入
EXIST_ERROR = 106 # 不存在

note_template = {"title" : "", "time" : "", "tags" : [], "content" : []} # 笔记文件的模板，其中均为加密后信息

default_title = "b'5L2/55So6K+05piO'"

# 用户登录
def login(user: str, pwd: str) -> int:
    # 先处理空白输入
    if user == "" or pwd == "":
        return NULL_INPUT_ERROR
    
    code = UKNOWN_ERROR
    db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
    cursor = db.cursor()
    try:
        # 执行sql语句
        judge_login = "SELECT * FROM users WHERE username = \"" + user + "\""
        cursor.execute(judge_login)
        result = cursor.fetchone()
        if result == None:
            code = USERNAME_ERROR
        else:
            if pwd == result[1]:
                code = NO_ERROR
            else:
                code = PASSWORD_ERROR
    except:
        db.rollback()
    finally:
        db.close()
        return code


# 用户注册
def register(user: str, pwd: str, nickname: str, path: str) -> int:
    code = UKNOWN_ERROR

    # 先处理空白输入
    if user == "" or pwd == "":
        return NULL_INPUT_ERROR
        
    db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
    cursor = db.cursor()
    try:
        # 执行sql语句
        judge_username = "SELECT username FROM users WHERE username = \"" + user + "\""
        cursor.execute(judge_username)
        result = cursor.fetchone()
        if result != None:
            code = EXIST_NAME_ERROR
        else:
            try:
                reg = "INSERT INTO users \
                        VALUES (\"" + user + "\", \"" + pwd + "\", \"" + nickname + "\", \"" + path + "user_" + user +"\")" 
                cursor.execute(reg)
                db.commit()

                cursor = db.cursor()
                reg = "CREATE TABLE " + user + "_notes ( \
                        id INT AUTO_INCREMENT PRIMARY KEY, \
                        title VARCHAR(200) NOT NULL UNIQUE, \
                        date DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW() \
                        )"
                copy_note = "INSERT INTO " + user + "_notes (title) VALUES (\"" + default_title + "\")"
                cursor.execute(reg)
                cursor.execute(copy_note)
                db.commit()
                code = NO_ERROR
            except:
                db.rollback()
    except:
        db.rollback()
    finally:
        db.close()
        return code


# 删除用户信息
def deleteUser(username: str) -> int:
    # 先处理空白输入
    if username == "":
        return NULL_INPUT_ERROR
    
    code = UKNOWN_ERROR

    db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
    cursor = db.cursor()
    try:
        # 执行sql语句
        judge_username = "SELECT * FROM users WHERE username = \"" + username + "\""
        cursor.execute(judge_username)
        result = cursor.fetchone()
        if result != None:
            del_sql1 = "DELETE FROM users WHERE username = \"" + username + "\""
            del_sql2 = "DROP TABLE " + username + "_notes"
            cursor.execute(del_sql1)
            cursor.execute(del_sql2)
            db.commit()
            code = NO_ERROR
    except:
        db.rollback()
    finally:
        db.close()
        return code


# 获取用户信息
def getInfo(username: str) -> dict:
    code = UKNOWN_ERROR

    nickname = ""
    profile = ""

    # 先处理空白输入
    if username == "":
        return {"status" : NULL_INPUT_ERROR, "nickname" : nickname, "profile" : profile}
    
    # 从数据库获取数据
    db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
    cursor = db.cursor()
    try:
        # 执行sql语句
        judge_username = "SELECT * FROM users WHERE username = \"" + username + "\""
        cursor.execute(judge_username)
        result = cursor.fetchone()

        if result == None:
            code = UKNOWN_ERROR
        else:
            # 昵称
            nickname = result[2]

            # 获取路径
            path = result[3]
            res_file = path + "/profile/profile.dat"

            with open(res_file, "r") as file:
                profile = file.read()

            code = NO_ERROR
    except:
        db.rollback()
    finally:
        db.close()
        return {"status" : code, "nickname" : nickname, "profile" : profile}
    

# 更新用户信息（昵称，密码等）
def updateInfo(username: str, nickname: str = "", password: str = "") -> dict:
    code = UKNOWN_ERROR

    res_file = ""

    # 先处理空白输入
    if username == "":
        return {"status" : NULL_INPUT_ERROR, "path" : res_file}
    
    # 从数据库获取数据
    db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
    cursor = db.cursor()
    try:
        # 执行sql语句
        judge_username = "SELECT * FROM users WHERE username = \"" + username + "\""
        cursor.execute(judge_username)
        result = cursor.fetchone()

        if result == None:
            code = UKNOWN_ERROR
        else:
            # 更新密码
            if password != "":
                upd_pwd = "UPDATE users SET password = \"" + password + "\""
                cursor.execute(upd_pwd)
                db.commit()

            # 更新昵称
            if nickname != "":
                upd_nkn = "UPDATE users SET nickname = \"" + nickname + "\""
                cursor.execute(upd_nkn)
                db.commit()

            # 获取路径
            path = result[3]
            res_file = path + "/profile/profile.dat"
            code = NO_ERROR
    except:
        db.rollback()
    finally:
        db.close()
        return {"status" : code, "path" : res_file}


# 获取所有笔记
def getNotes(username: str) -> dict:
    code = UKNOWN_ERROR
    notes = []

    # 先处理空白输入
    if username == "":
        return NULL_INPUT_ERROR
        
    # 从数据库获取数据
    db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
    cursor = db.cursor()
    try:
        # 执行sql语句
        judge_username = "SELECT * FROM users WHERE username = \"" + username + "\";"
        cursor.execute(judge_username)
        result = cursor.fetchone()

        if result == None:
            code = UKNOWN_ERROR
        else:
            # 先获取路径
            path = result[3]

            # 执行sql语句
            note_titles = "SELECT * FROM " + username + "_notes;"
            cursor.execute(note_titles)
            results = cursor.fetchall()

            for res in results:
                note = note_template.copy()
                id = str(res[0])
                res_path = path + "/notes/note" + id + "/"
                tags_file = res_path + "tags.dat"
                content_file = res_path + "content.dat"

                note["title"] = res[1]
                note["time"] = res[2]

                with open(tags_file) as file:
                    tags = []
                    for line in file.readlines():
                        tags.append(line.rstrip("\n"))
                    note["tags"] = tags.copy()

                with open(content_file) as file:
                    content = []
                    for line in file.readlines():
                        content.append(line.rstrip("\n"))
                    note["content"] = content.copy()

                notes.append(note)

            code = NO_ERROR
    except:
        db.rollback()
    finally:
        db.close()
        return {"status" : code, "notes" : notes}
    

# 删除笔记
def deleteNote(username: str, title: str) -> dict:
    code = UKNOWN_ERROR
    res_path = ""

    # 先处理空白输入
    if username == "":
        return {"status" : NULL_INPUT_ERROR, "path" : res_path}
    
    # 从数据库获取数据
    db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
    cursor = db.cursor()
    try:
        # 执行sql语句
        judge_username = "SELECT * FROM users WHERE username = \"" + username + "\""
        cursor.execute(judge_username)
        result = cursor.fetchone()

        if result != None: 
            note_lib = username + "_notes"
            # 获取路径
            path = result[3]

            # 执行sql语句（获取文件信息）
            find_note = "SELECT * FROM " + note_lib + " WHERE title = \"" + title + "\""
            cursor.execute(find_note)
            note_res = cursor.fetchone()

            if note_res != None:
                id = str(note_res[0])
                res_path = path + "/notes/note" + id

                # 删除
                del_sql = "DELETE FROM " + note_lib + " WHERE title = \"" + title + "\""
                cursor.execute(del_sql)
                db.commit()

                code = NO_ERROR
            else:
                code = EXIST_ERROR
    except:
        db.rollback()
    finally:
        db.close()
        return {"status" : code, "path" : res_path}


# 保存笔记
def saveNote(username: str, title: str, new_title: str = "") -> dict:
    code = UKNOWN_ERROR
    res_path = ""

    # 先处理空白输入
    if username == "":
        return {"status" : NULL_INPUT_ERROR, "path" : res_path}
        
    # 从数据库获取数据
    db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
    cursor = db.cursor()
    try:
        # 执行sql语句
        judge_username = "SELECT * FROM users WHERE username = \"" + username + "\""
        cursor.execute(judge_username)
        result = cursor.fetchone()

        if result != None: 
            note_lib = username + "_notes"
            # 获取路径
            path = result[3]

            # 执行sql语句（获取文件信息）
            find_note = "SELECT * FROM " + note_lib + " WHERE title = \"" + title + "\""
            cursor.execute(find_note)
            note_res = cursor.fetchone()

            if note_res != None:
                id = str(note_res[0])
                res_path = path + "/notes/note" + id

                # 更新日期
                upd_sql = "UPDATE " + note_lib + " SET date = NOW() WHERE title = \"" + title + "\""
                # 更新标题
                if new_title != "" and new_title != title:
                    # 更新标题
                    upd_sql = "UPDATE " + note_lib + " SET title = \"" + new_title + "\" WHERE title = \"" + title + "\""
                
                cursor.execute(upd_sql)
                db.commit()

                code = NO_ERROR
            else:
                # 新创建
                crt_sql = "INSERT INTO " + note_lib + " (title) VALUES (\"" + title + "\")"
                cursor.execute(crt_sql)
                db.commit()
                find = "SELECT * FROM " + note_lib + " WHERE title = \"" + title + "\""
                cursor.execute(find)
                note_res = cursor.fetchone()
                id = str(note_res[0])
                res_path = path + "/notes/note" + id

                code = NO_ERROR

    except:
        db.rollback()
    finally:
        db.close()
        return {"status" : code, "path" : res_path}

