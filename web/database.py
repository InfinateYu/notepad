import MySQLdb


class User:
    """
    与数据库直接连接的接口
    """
    
    # 登录状态
    STATUS_NOT_LOGGED = False
    STATUS_LOGGED = True

    # 错误类型
    NO_ERROR = 100 # 无错误
    UKNOWN_ERROR = 101 # 未知错误（用作其他错误类型的补充）
    USERNAME_ERROR = 102 # 用户名输入错误（登录不存在的用户名）
    PASSWORD_ERROR = 103 # 密码错误
    EXIST_NAME_ERROR = 104 # 用户名输入错误（创建已存在的用户名）
    NULL_INPUT_ERROR = 105 # 空白输入


    def __init__(self):
        self.user: str
        self.login_status = User.STATUS_NOT_LOGGED # 未登录：0 已登录：1
        self.note_template = {"title" : "", "tags" : [], "content" : ""} # 笔记文件的模板，其中均为加密后信息


    # 用户登录
    def login(self, user: str, pwd: str) -> int:
        code = User.UKNOWN_ERROR
        if self.login_status == User.STATUS_NOT_LOGGED:
            db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
            cursor = db.cursor()
            try:
                # 执行sql语句
                judge_login = "SELECT * FROM users WHERE username = \"" + user + "\";"
                cursor.execute(judge_login)
                result = cursor.fetchone()
                if result == None:
                    code = User.USERNAME_ERROR
                else:
                    if pwd == result[1]:
                        self.login_status = User.STATUS_LOGGED
                        self.user = user
                        code = User.NO_ERROR
                    else:
                        code = User.PASSWORD_ERROR
            except:
                db.rollback()
            finally:
                db.close()
                return code
        else:
            return code


    # 更新用户数据（注册）
    # 未完成
    def register(self, user: str, pwd: str) -> int:
        code = User.UKNOWN_ERROR
        if self.login_status == User.STATUS_NOT_LOGGED:
            # 先处理空白输入
            if user == "" or pwd == "":
                return User.NULL_INPUT_ERROR
            
            db = MySQLdb.connect("localhost", "root", "1234567890", "notepad", charset="utf8")
            cursor = db.cursor()
            try:
                # 执行sql语句
                judge_username = "SELECT username FROM users WHERE username = \"" + user + "\";"
                cursor.execute(judge_username)
                result = cursor.fetchone()
                if result != None:
                    code = User.EXIST_NAME_ERROR
                else:
                    try:
                        reg = "INSERT INTO users \
                               VALUES (\"" + user + "\", \"" + pwd + "\")" 
                        cursor.execute(reg)
                        db.commit()
                        code = User.NO_ERROR
                    except:
                        db.rollback()
            except:
                db.rollback()
            finally:
                db.close()
                return code
        else:
            return code


    # 更新笔记信息
    def updateNotes(self):
        pass


    # 获取所有笔记
    def getNotes(self) -> list[dict[str]]:
        if self.login_status == User.STATUS_LOGGED:
            note = self.note_template.copy()
            
            # 从数据库获取数据

            return [note]
        else:
            return []