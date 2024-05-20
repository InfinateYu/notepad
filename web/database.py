# 与数据库直接连接的接口
import MySQLdb

# 错误类型
NO_ERROR = 100 # 无错误
UKNOWN_ERROR = 101 # 未知错误（用作其他错误类型的补充）
USERNAME_ERROR = 102 # 用户名输入错误（登录不存在的用户名）
PASSWORD_ERROR = 103 # 密码错误
EXIST_NAME_ERROR = 104 # 用户名输入错误（创建已存在的用户名）
NULL_INPUT_ERROR = 105 # 空白输入

note_template = {"title" : "", "tags" : [str], "content" : [str]} # 笔记文件的模板，其中均为加密后信息

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
        judge_login = "SELECT * FROM users WHERE username = \"" + user + "\";"
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
def register(user: str, pwd: str, path: str) -> int:
    code = UKNOWN_ERROR

    # 先处理空白输入
    if user == "" or pwd == "":
        return NULL_INPUT_ERROR
        
    db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
    cursor = db.cursor()
    try:
        # 执行sql语句
        judge_username = "SELECT username FROM users WHERE username = \"" + user + "\";"
        cursor.execute(judge_username)
        result = cursor.fetchone()
        if result != None:
            code = EXIST_NAME_ERROR
        else:
            try:
                reg = "INSERT INTO users \
                        VALUES (\"" + user + "\", \"" + pwd + "\", \"" + user + "\", \"" + path + "user_" + user +"\")" 
                cursor.execute(reg)
                db.commit()

                cursor = db.cursor()
                reg = "CREATE TABLE " + user + "_notes ( \
                        id INT AUTO_INCREMENT PRIMARY KEY, \
                        title VARCHAR(200) NOT NULL UNIQUE \
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
        judge_username = "SELECT * FROM users WHERE username = \"" + username + "\";"
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

                with open(tags_file) as file:
                    note["tags"] = file.read().splitlines()

                with open(content_file) as file:
                    note["content"] = file.read().splitlines()

                notes.append(note)

            code = NO_ERROR
    except:
        db.rollback()
    finally:
        db.close()
        return {"status" : code, "notes" : notes}
    

# 删除笔记
def deleteNotes(username: str, titles: list[str]) -> int:
    pass


# 保存笔记(
def saveNote(username: str, notename: str):
    pass

