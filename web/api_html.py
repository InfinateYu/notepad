html = """<!DOCTYPE html>
<html>
    <head>
        <meta charset='utf-8'>
        <title>API</title>
    </head>

    <body style="background-color: #6687fff0; color: #ffe3e3da">
        <h1>除本接口外，所有可以调用的接口如下：</h1>
        <ol>
            <li>/test
                <ul>
                    <li>允许的请求方法：GET, POST</li>
                    <li>url参数：无</li>
                    <li>json参数：无</li>
                    <li>说明：测试接口，如果连接成功，返回connect successful</li>
                </ul>
            </li>
            <li>/register
                <ul>
                    <li>允许的请求方法：POST</li>
                    <li>url参数：无</li>
                    <li>json参数：{username : String, password : String}</li>
                    <li>说明：用户注册，返回状态码和对应</li>
                </ul>
            </li>
            <li>/login
                <ul>
                    <li>允许的请求方法：POST</li>
                    <li>url参数：无</li>
                    <li>json参数：{username : String, password : String}</li>
                    <li>说明：用户登录，如果成功，返回用户信息，否则返回错误码</li>
                </ul>
            </li>
            <li>/logout
                <ul>
                    <li>允许的请求方法：POST</li>
                    <li>url参数：无</li>
                    <li>json参数：{username : String}</li>
                    <li>说明：退出登录，返回状态码（只有用户处于登录状态时才会正常退出）</li>
                </ul>
            </li>
            <li>/upduser
                <ul>
                    <li>允许的请求方法：POST</li>
                    <li>url参数：无</li>
                    <li>json参数：{username : String, (可选)nickname : String, (可选)profile : String, (可选)password : String}</li>
                    <li>说明：更新用户信息，用户必须处于登录状态</li>
                </ul>
            </li>
            <li>/deluser
                <ul>
                    <li>允许的请求方法：POST</li>
                    <li>url参数：无</li>
                    <li>json参数：{username : String}</li>
                    <li>说明：删除用户信息，用户必须处于登录状态时才能删除</li>
                </ul>
            </li>
            <li>/save
                <ul>
                    <li>允许的请求方法：POST</li>
                    <li>url参数：无</li>
                    <li>json参数：{username : String, title : String, (可选)new_title : String, tags : List, content : List}</li>
                    <li>说明：保存或修改笔记信息（题目、标签、内容等），用户必须处于登录状态时才能保存</li>
                </ul>
            </li>
            <li>/delnote
                <ul>
                    <li>允许的请求方法：POST</li>
                    <li>url参数：无</li>
                    <li>json参数：{username : String, title : String}</li>
                    <li>说明：删除笔记，用户必须处于登录状态时才能删除</li>
                </ul>
            </li>
        </ol>
    </body>
</html>
"""
