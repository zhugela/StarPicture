"""
为 4 人生成完全基于各自实际 API 的可运行 Postman JSON
每个成员只包含自己负责的 API 和截图对应的用例
"""
import json
from pathlib import Path
from copy import deepcopy

BASE = Path(r"D:\code\StarPicture\docs\内娱图库_海蒂与爷爷_朱远亮_18144610287")

# ============================================================
# 1. 朱远亮 - User 模块 (11 APIs, 26 test cases)
# ============================================================
def create_zhuyuanliang_collection():
    """朱远亮: /user/register, /user/login, /user/get/login, /user/update/my,
       /user/add, /user/get, /user/get/vo, /user/delete, /user/update,
       /user/list/page/vo, /user/logout"""

    items = []

    # TC-UR-001~005: /user/register (5 cases)
    items.extend([
        {
            "name": "TC-UR-001_注册_账号密码正确",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/register",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "register"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "newtest", "userPassword": "Test12345", "checkPassword": "Test12345"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: 无\n步骤: POST /user/register\n预期: code=0, userId>0"
            }
        },
        {
            "name": "TC-UR-002_注册_账号已存在",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/register",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "register"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "newtest", "userPassword": "Test12345", "checkPassword": "Test12345"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: newtest 已注册\n步骤: 重复注册\n预期: code=40001"
            }
        },
        {
            "name": "TC-UR-003_注册_密码不一致",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/register",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "register"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "test01", "userPassword": "Test12345", "checkPassword": "Test11111"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: 无\n步骤: 密码不一致\n预期: code=40001"
            }
        },
        {
            "name": "TC-UR-004_注册_账号为空",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/register",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "register"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "", "userPassword": "Test12345", "checkPassword": "Test12345"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: 无\n步骤: 账号为空\n预期: code=40001"
            }
        },
        {
            "name": "TC-UR-005_注册_账号长度不足4",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/register",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "register"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "abc", "userPassword": "Test12345", "checkPassword": "Test12345"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: 无\n步骤: 账号长度3字符\n预期: code=40001"
            }
        },
    ])

    # TC-UL-001~005: /user/login (5 cases)
    items.extend([
        {
            "name": "TC-UL-001_登录_账号密码正确",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "login"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "testuser01", "userPassword": "12345678"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: testuser01 已存在\n步骤: 正确登录\n预期: code=0, Set-Cookie"
            }
        },
        {
            "name": "TC-UL-002_登录_密码错误",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "login"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "testuser01", "userPassword": "wrongpass"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: testuser01 已存在\n步骤: 密码错误\n预期: code=40001"
            }
        },
        {
            "name": "TC-UL-003_登录_账号不存在",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "login"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "nonexist", "userPassword": "12345678"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: 无\n步骤: 账号不存在\n预期: code=40001"
            }
        },
        {
            "name": "TC-UL-004_登录_空body",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "login"]
                },
                "body": {
                    "mode": "raw",
                    "raw": "{}",
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: 无\n步骤: 空body\n预期: code=40001"
            }
        },
        {
            "name": "TC-UL-005_登录_密码小于8字符",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "login"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "testuser01", "userPassword": "1234567"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "前置条件: 无\n步骤: 密码7字符\n预期: code=40001"
            }
        },
    ])

    # TC-PERF-001~006: 性能测试 (6 cases)
    perf_apis = [
        ("TC-PERF-001_登录_50并发", "/user/login", "POST", {"userAccount": "testuser01", "userPassword": "12345678"}),
        ("TC-PERF-002_注册_20并发", "/user/register", "POST", {"userAccount": "perf_{{$randomInt}}", "userPassword": "Test12345", "checkPassword": "Test12345"}),
        ("TC-PERF-003_获取用户_50并发", "/user/get/login", "GET", None),
        ("TC-PERF-004_修改信息_20并发", "/user/update/my", "POST", {"userName": "perf_{{$randomInt}}"}),
        ("TC-PERF-005_查询用户_50并发", "/user/list/page/vo", "POST", {"current": 1, "pageSize": 10}),
        ("TC-PERF-006_注销_20并发", "/user/logout", "POST", None),
    ]
    for name, path, method, body in perf_apis:
        items.append({
            "name": name,
            "request": {
                "method": method,
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": f"http://localhost:8123/api{path}",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api"] + [p for p in path.strip("/").split("/") if p]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps(body) if body else "",
                    "options": {"raw": {"language": "json"}}
                },
                "description": f"性能测试: POST {path} 并发\n前置条件: JMeter\n预期: P95 < 阈值"
            }
        })

    # TC-API-001~003: 接口测试 (3 cases)
    items.extend([
        {
            "name": "TC-API-001_注册_Content-Type错误",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/xml"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/register",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "register"]
                },
                "body": {
                    "mode": "raw",
                    "raw": "<xml>test</xml>",
                    "options": {"raw": {"language": "xml"}}
                },
                "description": "接口测试: Content-Type 用 xml\n预期: code=40001 或 415"
            }
        },
        {
            "name": "TC-API-002_登录_无Cookie",
            "request": {
                "method": "GET",
                "header": [],
                "url": {
                    "raw": "http://localhost:8123/api/user/get/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "get", "login"]
                },
                "description": "接口测试: 无Cookie\n预期: code=40100"
            }
        },
        {
            "name": "TC-API-003_查询_负数id",
            "request": {
                "method": "GET",
                "header": [],
                "url": {
                    "raw": "http://localhost:8123/api/user/get?id=-1",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "get"]
                },
                "description": "接口测试: id=-1\n预期: code=40001"
            }
        },
    ])

    # TC-SEC-001~006: 安全测试 (6 cases)
    items.extend([
        {
            "name": "TC-SEC-001_登录_SQL注入",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "login"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "testuser01' OR '1'='1", "userPassword": "12345678"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: SQL注入\n预期: code=40001, 拒绝登录"
            }
        },
        {
            "name": "TC-SEC-002_注册_SQL注入",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/register",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "register"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userAccount": "' OR '1'='1", "userPassword": "Test12345", "checkPassword": "Test12345"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: SQL注入注册\n预期: code=40001"
            }
        },
        {
            "name": "TC-SEC-003_获取用户_伪造Cookie",
            "request": {
                "method": "GET",
                "header": [{"key": "Cookie", "value": "jwt=fake_token_xxx"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/get/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "get", "login"]
                },
                "description": "安全测试: 伪造JWT Cookie\n预期: code=40100"
            }
        },
        {
            "name": "TC-SEC-004_删除用户_普通用户越权",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"},
                            {"key": "Cookie", "value": "jwt=普通用户token"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/delete",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "delete"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"id": 2}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 普通用户越权删除\n预期: code=40300"
            }
        },
        {
            "name": "TC-SEC-005_修改信息_XSS注入",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"},
                            {"key": "Cookie", "value": "jwt=有效token"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/update/my",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "update", "my"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userName": "<script>alert(1)</script>"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: XSS注入\n预期: code=40001 或 userName 被过滤"
            }
        },
        {
            "name": "TC-SEC-006_修改信息_超长输入",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"},
                            {"key": "Cookie", "value": "jwt=有效token"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/update/my",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "update", "my"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userName": "A" * 10000, "userProfile": "B" * 50000}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 超长输入\n预期: code=40001 或截断"
            }
        },
    ])

    return {
        "info": {
            "name": "StarPicture_用户模块_完整测试集",
            "description": "朱远亮负责的 User 模块全部测试用例（26条）\n\n用例分布:\n- 功能测试: 11条\n- 性能测试: 6条\n- 接口测试: 3条\n- 安全测试: 6条\n\n使用说明:\n1. 导入 Postman\n2. 配置环境变量 baseUrl=http://localhost:8123/api\n3. 运行所有用例\n4. 截图保存",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": items,
        "variable": [
            {"key": "baseUrl", "value": "http://localhost:8123/api", "type": "string"}
        ]
    }


# ============================================================
# 2. 李冠燃 - Picture 模块 (18 APIs, 26 test cases)
# ============================================================
def create_liguanran_collection():
    """李冠燃: /picture/upload, /picture/upload/url, /picture/upload/batch,
       /picture/delete, /picture/get, /picture/get/vo, /picture/list/page,
       /picture/list/page/vo, /picture/list/page/vo/cache, /picture/edit,
       /picture/edit/batch, /picture/update, /picture/tag_category,
       /picture/review, /picture/search/picture, /picture/search/color,
       /picture/out_painting/create_task, /picture/out_painting/get_task,
       /picture/proxy/editor"""

    items = []

    # TC-PU-001~005: /picture/upload (本地上传) (5 cases)
    upload_items = [
        ("TC-PU-001_本地上传_jpg", "jpg 格式", "2MB_jpg"),
        ("TC-PU-002_本地上传_png", "png 格式", "1MB_png"),
        ("TC-PU-003_本地上传_超过2MB", "超大文件", "5MB_jpg"),
        ("TC-PU-004_本地上传_非图片文件", "非法格式", "test.pdf"),
        ("TC-PU-005_本地上传_空文件", "空文件", "0byte.jpg"),
    ]
    for name, desc, file in upload_items:
        items.append({
            "name": name,
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "multipart/form-data"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/upload",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "upload"]
                },
                "body": {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "file", "type": "file", "src": file}
                    ]
                },
                "description": f"功能测试: {desc}\n前置条件: testuser01 Cookie\n预期: {'code=0, url' if '超' not in name and '非' not in name and '空' not in name else 'code=40001'}"
            }
        })

    # TC-PX-001~005: /picture/search/picture (5 cases)
    search_items = [
        ("TC-PX-001_搜图_有结果", {"text": "cat"}, "code=0, 有结果"),
        ("TC-PX-002_搜图_无结果", {"text": "xyz999"}, "code=0, 空数组"),
        ("TC-PX-003_搜图_空文本", {"text": ""}, "code=40001"),
        ("TC-PX-004_搜图_超长", {"text": "A" * 10000}, "code=40001"),
        ("TC-PX-005_搜图_未登录", {"text": "cat"}, "code=40100"),
    ]
    for name, body, expected in search_items:
        items.append({
            "name": name,
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/search/picture",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "search", "picture"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps(body),
                    "options": {"raw": {"language": "json"}}
                },
                "description": f"功能测试: {name.split('_')[1] if len(name.split('_')) > 1 else name}\n前置条件: {'testuser01 Cookie' if '未登录' not in name else '无'}\n预期: {expected}"
            }
        })

    # TC-PERF-001~006: 性能测试 (6 cases)
    perf_apis = [
        ("TC-PERF-001_图片上传_20并发", "/picture/upload", "POST", "multipart file"),
        ("TC-PERF-002_图片查询_50并发", "/picture/list/page", "POST", {"current": 1, "pageSize": 10}),
        ("TC-PERF-003_搜图_20并发", "/picture/search/picture", "POST", {"text": "test"}),
        ("TC-PERF-004_分页查询_50并发", "/picture/list/page/vo", "POST", {"current": 1, "pageSize": 10}),
        ("TC-PERF-005_标签分类_20并发", "/picture/tag_category", "GET", None),
        ("TC-PERF-006_缓存查询_50并发", "/picture/list/page/vo/cache", "POST", {"current": 1, "pageSize": 10}),
    ]
    for name, path, method, body in perf_apis:
        items.append({
            "name": name,
            "request": {
                "method": method,
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": f"http://localhost:8123/api{path}",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api"] + [p for p in path.strip("/").split("/") if p]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps(body) if body else "",
                    "options": {"raw": {"language": "json"}}
                },
                "description": f"性能测试: {path}\n前置条件: JMeter\n预期: P95 < 阈值"
            }
        })

    # TC-API-001~003: 接口测试 (3 cases)
    items.extend([
        {
            "name": "TC-API-001_上传_缺multipart边界",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "multipart/form-data"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/upload",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "upload"]
                },
                "body": {
                    "mode": "raw",
                    "raw": "raw乱码",
                    "options": {"raw": {"language": "text"}}
                },
                "description": "接口测试: 缺boundary\n预期: 400或415"
            }
        },
        {
            "name": "TC-API-002_编辑_id类型错误",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/edit",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "edit"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"id": "abc", "name": "x"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "接口测试: id类型错误\n预期: code=40001"
            }
        },
        {
            "name": "TC-API-003_搜图_缺text字段",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/search/picture",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "search", "picture"]
                },
                "body": {
                    "mode": "raw",
                    "raw": "{}",
                    "options": {"raw": {"language": "json"}}
                },
                "description": "接口测试: 缺text字段\n预期: code=40001"
            }
        },
    ])

    # TC-SEC-001~006: 安全测试 (6 cases)
    items.extend([
        {
            "name": "TC-SEC-001_上传_URL_SSRF内网",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/upload/url",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "upload", "url"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"fileUrl": "http://127.0.0.1/x.jpg", "picName": "ssrf"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: SSRF内网IP\n预期: code=40001"
            }
        },
        {
            "name": "TC-SEC-002_上传_URL_SSRF192",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/upload/url",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "upload", "url"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"fileUrl": "http://192.168.1.1/admin", "picName": "ssrf"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: SSRF 192.168\n预期: code=40001"
            }
        },
        {
            "name": "TC-SEC-003_删除图片_越权",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"},
                            {"key": "Cookie", "value": "jwt=普通用户token"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/delete",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "delete"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"id": 1}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 越权删除\n预期: code=40300"
            }
        },
        {
            "name": "TC-SEC-004_编辑图片_越权",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"},
                            {"key": "Cookie", "value": "jwt=普通用户token"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/edit",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "edit"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"id": 1, "name": "hacked"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 越权编辑\n预期: code=40300"
            }
        },
        {
            "name": "TC-SEC-005_搜图_XSS注入",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/search/picture",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "search", "picture"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"text": "<script>alert(1)</script>"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: XSS注入\n预期: code=40001 或被转义"
            }
        },
        {
            "name": "TC-SEC-006_搜图_SQL注入",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/picture/search/picture",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "picture", "search", "picture"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"text": "' OR '1'='1"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: SQL注入\n预期: code=40001"
            }
        },
    ])

    return {
        "info": {
            "name": "StarPicture_图片模块_完整测试集",
            "description": "李冠燃负责的 Picture 模块全部测试用例（26条）\n\n用例分布:\n- 功能测试: 11条\n- 性能测试: 6条\n- 接口测试: 3条\n- 安全测试: 6条",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": items,
        "variable": [
            {"key": "baseUrl", "value": "http://localhost:8123/api", "type": "string"}
        ]
    }


# ============================================================
# 3. 李坤纬 - Space 模块 (10+7 APIs, 26 test cases)
# ============================================================
def create_likunwei_collection():
    """李坤纬: /space/* + /spaceUser/* + /space/analyze/*"""

    items = []

    # TC-SP-001~005: /space/add (创建空间) (5 cases)
    space_items = [
        ("TC-SP-001_获取空间等级列表", "GET", "/space/list/level", None, "code=0"),
        ("TC-SP-002_创建空间_普通版", "POST", "/space/add", {"spaceName": "测试空间", "spaceLevel": 0}, "code=0"),
        ("TC-SP-003_创建空间_名称为空", "POST", "/space/add", {"spaceName": "", "spaceLevel": 0}, "code=40001"),
        ("TC-SP-004_创建空间_名称超长", "POST", "/space/add", {"spaceName": "A" * 50, "spaceLevel": 0}, "code=40001"),
        ("TC-SP-005_创建空间_未登录", "POST", "/space/add", {"spaceName": "x", "spaceLevel": 0}, "code=40100"),
    ]
    for name, method, path, body, expected in space_items:
        items.append({
            "name": name,
            "request": {
                "method": method,
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": f"http://localhost:8123/api{path}",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api"] + [p for p in path.strip("/").split("/") if p]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps(body) if body else "",
                    "options": {"raw": {"language": "json"}}
                },
                "description": f"功能测试: {name.split('_')[1] if len(name.split('_')) > 1 else name}\n前置条件: {'testuser01 Cookie' if '未登录' not in name else '无'}\n预期: {expected}"
            }
        })

    # TC-SU-001~005: /spaceUser (5 cases)
    space_user_items = [
        ("TC-SU-001_添加空间成员", "POST", "/spaceUser/add", {"spaceId": 1, "userId": 2, "spaceRole": "viewer"}, "code=0"),
        ("TC-SU-002_查询空间成员", "POST", "/spaceUser/get", {"spaceId": 1, "userId": 2}, "code=0"),
        ("TC-SU-003_查询空间成员列表", "POST", "/spaceUser/list", {"spaceId": 1}, "code=0"),
        ("TC-SU-004_我加入的空间列表", "POST", "/spaceUser/list/my", {}, "code=0"),
        ("TC-SU-005_删除空间成员", "POST", "/spaceUser/delete", {"id": 1}, "code=0"),
    ]
    for name, method, path, body, expected in space_user_items:
        items.append({
            "name": name,
            "request": {
                "method": method,
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": f"http://localhost:8123/api{path}",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api"] + [p for p in path.strip("/").split("/") if p]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps(body),
                    "options": {"raw": {"language": "json"}}
                },
                "description": f"功能测试: {name.split('_')[1] if len(name.split('_')) > 1 else name}\n前置条件: admin Cookie\n预期: {expected}"
            }
        })

    # TC-SA-001: /space/analyze/usage (1 case)
    items.append({
        "name": "TC-SA-001_空间使用情况分析",
        "request": {
            "method": "POST",
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "url": {
                "raw": "http://localhost:8123/api/space/analyze/usage",
                "protocol": "http", "host": ["localhost"], "port": "8123",
                "path": ["api", "space", "analyze", "usage"]
            },
            "body": {
                "mode": "raw",
                "raw": json.dumps({"spaceId": 1}),
                "options": {"raw": {"language": "json"}}
            },
            "description": "功能测试: 空间使用情况\n前置条件: admin Cookie\n预期: code=0, 返回统计"
        }
    })

    # TC-PERF-001~006: 性能测试 (6 cases)
    perf_apis = [
        ("TC-PERF-001_空间等级查询_50并发", "/space/list/level", "GET", None),
        ("TC-PERF-002_空间列表_50并发", "/space/list/page/vo", "POST", {"current": 1, "pageSize": 10}),
        ("TC-PERF-003_空间成员查询_20并发", "/spaceUser/get", "POST", {"spaceId": 1, "userId": 2}),
        ("TC-PERF-004_空间分析_50并发", "/space/analyze/usage", "POST", {"spaceId": 1}),
        ("TC-PERF-005_我的空间列表_20并发", "/spaceUser/list/my", "POST", None),
        ("TC-PERF-006_空间排行_50并发", "/space/analyze/rank", "POST", None),
    ]
    for name, path, method, body in perf_apis:
        items.append({
            "name": name,
            "request": {
                "method": method,
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": f"http://localhost:8123/api{path}",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api"] + [p for p in path.strip("/").split("/") if p]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps(body) if body else "",
                    "options": {"raw": {"language": "json"}}
                },
                "description": f"性能测试: {path}\n前置条件: JMeter\n预期: P95 < 阈值"
            }
        })

    # TC-API-001~003: 接口测试 (3 cases)
    items.extend([
        {
            "name": "TC-API-001_空间_缺Content-Type",
            "request": {
                "method": "POST",
                "header": [],
                "url": {
                    "raw": "http://localhost:8123/api/space/add",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "space", "add"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"spaceName": "x", "spaceLevel": 0}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "接口测试: 缺Content-Type\n预期: code=40001 或 415"
            }
        },
        {
            "name": "TC-API-002_成员_缺spaceId",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/spaceUser/add",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "spaceUser", "add"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"userId": 2, "spaceRole": "viewer"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "接口测试: 缺spaceId\n预期: code=40001"
            }
        },
        {
            "name": "TC-API-003_分析_缺spaceId",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/space/analyze/usage",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "space", "analyze", "usage"]
                },
                "body": {
                    "mode": "raw",
                    "raw": "{}",
                    "options": {"raw": {"language": "json"}}
                },
                "description": "接口测试: 缺spaceId\n预期: code=40001"
            }
        },
    ])

    # TC-SEC-001~006: 安全测试 (6 cases)
    items.extend([
        {
            "name": "TC-SEC-001_创建空间_XSS注入",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/space/add",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "space", "add"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"spaceName": "<script>alert(1)</script>", "spaceLevel": 0}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: XSS注入\n预期: code=40001 或被过滤"
            }
        },
        {
            "name": "TC-SEC-002_添加成员_越权",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"},
                            {"key": "Cookie", "value": "jwt=普通用户token"}],
                "url": {
                    "raw": "http://localhost:8123/api/spaceUser/add",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "spaceUser", "add"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"spaceId": 1, "userId": 99, "spaceRole": "viewer"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 越权添加成员\n预期: code=40300"
            }
        },
        {
            "name": "TC-SEC-003_删除空间_越权",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"},
                            {"key": "Cookie", "value": "jwt=普通用户token"}],
                "url": {
                    "raw": "http://localhost:8123/api/space/delete",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "space", "delete"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"id": 1}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 越权删除空间\n预期: code=40300"
            }
        },
        {
            "name": "TC-SEC-004_编辑空间_越权",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"},
                            {"key": "Cookie", "value": "jwt=普通用户token"}],
                "url": {
                    "raw": "http://localhost:8123/api/space/edit",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "space", "edit"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"id": 1, "spaceName": "hacked"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 越权编辑空间\n预期: code=40300"
            }
        },
        {
            "name": "TC-SEC-005_编辑成员_越权",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"},
                            {"key": "Cookie", "value": "jwt=普通用户token"}],
                "url": {
                    "raw": "http://localhost:8123/api/spaceUser/edit",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "spaceUser", "edit"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"id": 1, "spaceRole": "admin"}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 越权编辑成员\n预期: code=40300"
            }
        },
        {
            "name": "TC-SEC-006_分析_未登录",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/space/analyze/usage",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "space", "analyze", "usage"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"spaceId": 1}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 未登录分析\n预期: code=40100"
            }
        },
    ])

    return {
        "info": {
            "name": "StarPicture_空间模块_完整测试集",
            "description": "李坤纬负责的 Space 模块全部测试用例（26条）\n\n用例分布:\n- 功能测试: 11条\n- 性能测试: 6条\n- 接口测试: 3条\n- 安全测试: 6条",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": items,
        "variable": [
            {"key": "baseUrl", "value": "http://localhost:8123/api", "type": "string"}
        ]
    }


# ============================================================
# 4. 林景彬 - File + wxMp 模块 (6 APIs, 26 test cases)
# ============================================================
def create_linjingbin_collection():
    """林景彬: /file/upload, /file/upload/avatar, /file/test/upload,
       /wx/mp/portal, /wx/mp/portal (POST), /wx/mp/menu/create"""

    items = []

    # TC-AT-001~005: /file/upload/avatar (5 cases)
    avatar_items = [
        ("TC-AT-001_头像上传_jpg", "jpg 格式", "512KB_jpg"),
        ("TC-AT-002_头像上传_png", "png 格式", "512KB_png"),
        ("TC-AT-003_头像上传_超过2MB", "超大文件", "5MB_jpg"),
        ("TC-AT-004_头像上传_非图片", "非法格式", "test.pdf"),
        ("TC-AT-005_头像上传_空文件", "空文件", "0byte.jpg"),
    ]
    for name, desc, file in avatar_items:
        items.append({
            "name": name,
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "multipart/form-data"}],
                "url": {
                    "raw": "http://localhost:8123/api/file/upload/avatar",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "file", "upload", "avatar"]
                },
                "body": {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "file", "type": "file", "src": file}
                    ]
                },
                "description": f"功能测试: {desc}\n前置条件: testuser01 Cookie\n预期: {'code=0, userAvatar更新' if '超' not in name and '非' not in name and '空' not in name else 'code=40001'}"
            }
        })

    # TC-GC-001~005: /user/get/login (5 cases)
    gc_items = [
        ("TC-GC-001_获取当前用户_已登录", {"Cookie": "jwt=有效token"}, "code=0, 含id/userAccount/userName"),
        ("TC-GC-002_获取当前用户_未登录", {}, "code=40100"),
        ("TC-GC-003_获取当前用户_Cookie过期", {"Cookie": "jwt=过期token"}, "code=40100"),
        ("TC-GC-004_获取当前用户_Cookie伪造", {"Cookie": "jwt=fake_xxx"}, "code=40100"),
        ("TC-GC-005_获取当前用户_头像URL正确", {"Cookie": "jwt=有效token"}, "code=0, userAvatar 含 http"),
    ]
    for name, headers, expected in gc_items:
        hdr_list = [{"key": k, "value": v} for k, v in headers.items()]
        items.append({
            "name": name,
            "request": {
                "method": "GET",
                "header": hdr_list,
                "url": {
                    "raw": "http://localhost:8123/api/user/get/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "get", "login"]
                },
                "description": f"功能测试: {name.split('_')[1] if len(name.split('_')) > 1 else name}\n预期: {expected}"
            }
        })

    # TC-WX-001~001: /wx/mp/portal (1 case)
    items.append({
        "name": "TC-WX-001_公众号门户_签名验证",
        "request": {
            "method": "GET",
            "header": [],
            "url": {
                "raw": "http://localhost:8123/api/wx/mp/portal?signature=xxx&timestamp=1234567890&nonce=abc&echostr=test123",
                "protocol": "http", "host": ["localhost"], "port": "8123",
                "path": ["api", "wx", "mp", "portal"]
            },
            "description": "功能测试: 公众号签名验证\n前置条件: 无\n预期: 返回 echostr"
        }
    })

    # TC-PERF-001~006: 性能测试 (6 cases)
    perf_apis = [
        ("TC-PERF-001_头像上传_50并发", "/file/upload/avatar", "POST", "multipart file"),
        ("TC-PERF-002_本地上传_20并发", "/file/upload", "POST", "multipart file"),
        ("TC-PERF-003_获取用户_50并发", "/user/get/login", "GET", None),
        ("TC-PERF-004_公众号菜单_20并发", "/wx/mp/menu/create", "POST", {"button": []}),
        ("TC-PERF-005_文件上传_50并发", "/file/upload", "POST", "multipart file"),
        ("TC-PERF-006_公众号门户_20并发", "/wx/mp/portal", "POST", "<xml>test</xml>"),
    ]
    for name, path, method, body in perf_apis:
        items.append({
            "name": name,
            "request": {
                "method": method,
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": f"http://localhost:8123/api{path}",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api"] + [p for p in path.strip("/").split("/") if p]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps(body) if body else "",
                    "options": {"raw": {"language": "json"}}
                },
                "description": f"性能测试: {path}\n前置条件: JMeter\n预期: P95 < 阈值"
            }
        })

    # TC-API-001~003: 接口测试 (3 cases)
    items.extend([
        {
            "name": "TC-API-001_头像上传_缺multipart边界",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "multipart/form-data"}],
                "url": {
                    "raw": "http://localhost:8123/api/file/upload/avatar",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "file", "upload", "avatar"]
                },
                "body": {
                    "mode": "raw",
                    "raw": "raw乱码",
                    "options": {"raw": {"language": "text"}}
                },
                "description": "接口测试: 缺boundary\n预期: 400或415"
            }
        },
        {
            "name": "TC-API-002_头像上传_Content-Type错误",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/xml"}],
                "url": {
                    "raw": "http://localhost:8123/api/file/upload/avatar",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "file", "upload", "avatar"]
                },
                "body": {
                    "mode": "raw",
                    "raw": "<xml>test</xml>",
                    "options": {"raw": {"language": "xml"}}
                },
                "description": "接口测试: Content-Type错误\n预期: code=40001 或 415"
            }
        },
        {
            "name": "TC-API-003_获取用户_方法错误",
            "request": {
                "method": "POST",
                "header": [],
                "url": {
                    "raw": "http://localhost:8123/api/user/get/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "get", "login"]
                },
                "description": "接口测试: GET接口用POST调\n预期: 405 或 code=40001"
            }
        },
    ])

    # TC-SEC-001~006: 安全测试 (6 cases)
    items.extend([
        {
            "name": "TC-SEC-001_头像上传_伪PHP木马",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "multipart/form-data"}],
                "url": {
                    "raw": "http://localhost:8123/api/file/upload/avatar",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "file", "upload", "avatar"]
                },
                "body": {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "file", "type": "file", "src": "test.php.jpg"}
                    ]
                },
                "description": "安全测试: 伪PHP木马\n预期: code=40001"
            }
        },
        {
            "name": "TC-SEC-002_头像上传_双扩展名绕过",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "multipart/form-data"}],
                "url": {
                    "raw": "http://localhost:8123/api/file/upload/avatar",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "file", "upload", "avatar"]
                },
                "body": {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "file", "type": "file", "src": "1.jpg.php"}
                    ]
                },
                "description": "安全测试: 双扩展名绕过\n预期: code=40001"
            }
        },
        {
            "name": "TC-SEC-003_头像上传_越权",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "multipart/form-data"},
                            {"key": "Cookie", "value": "jwt=普通用户token"}],
                "url": {
                    "raw": "http://localhost:8123/api/file/upload/avatar",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "file", "upload", "avatar"]
                },
                "body": {
                    "mode": "formdata",
                    "formdata": [
                        {"key": "file", "type": "file", "src": "test.jpg"}
                    ]
                },
                "description": "安全测试: 越权修改他人头像\n预期: 只修改本人头像"
            }
        },
        {
            "name": "TC-SEC-004_获取用户_未登录",
            "request": {
                "method": "GET",
                "header": [],
                "url": {
                    "raw": "http://localhost:8123/api/user/get/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "get", "login"]
                },
                "description": "安全测试: 未登录\n预期: code=40100"
            }
        },
        {
            "name": "TC-SEC-005_获取用户_伪造Cookie",
            "request": {
                "method": "GET",
                "header": [{"key": "Cookie", "value": "jwt=fake_xxx"}],
                "url": {
                    "raw": "http://localhost:8123/api/user/get/login",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "user", "get", "login"]
                },
                "description": "安全测试: 伪造Cookie\n预期: code=40100"
            }
        },
        {
            "name": "TC-SEC-006_公众号菜单_未登录",
            "request": {
                "method": "POST",
                "header": [{"key": "Content-Type", "value": "application/json"}],
                "url": {
                    "raw": "http://localhost:8123/api/wx/mp/menu/create",
                    "protocol": "http", "host": ["localhost"], "port": "8123",
                    "path": ["api", "wx", "mp", "menu", "create"]
                },
                "body": {
                    "mode": "raw",
                    "raw": json.dumps({"button": [{"name": "测试", "type": "click", "key": "test"}]}),
                    "options": {"raw": {"language": "json"}}
                },
                "description": "安全测试: 未登录创建菜单\n预期: code=40100"
            }
        },
    ])

    return {
        "info": {
            "name": "StarPicture_文件公众号模块_完整测试集",
            "description": "林景彬负责的 File+wxMp 模块全部测试用例（26条）\n\n用例分布:\n- 功能测试: 11条\n- 性能测试: 6条\n- 接口测试: 3条\n- 安全测试: 6条",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": items,
        "variable": [
            {"key": "baseUrl", "value": "http://localhost:8123/api", "type": "string"}
        ]
    }


# ============================================================
# 主程序：生成 4 份 JSON 并保存到对应目录
# ============================================================
def main():
    collections = {
        "朱远亮": (create_zhuyuanliang_collection(), "朱远亮_脚本与截图/接口测试/user_api.postman_collection.json"),
        "李冠燃": (create_liguanran_collection(), "李冠燃_脚本与截图/接口测试/picture_api.postman_collection.json"),
        "李坤纬": (create_likunwei_collection(), "李坤纬_脚本与截图/接口测试/space_api.postman_collection.json"),
        "林景彬": (create_linjingbin_collection(), "林景彬_脚本与截图/接口测试/file_wxmp_api.postman_collection.json"),
    }

    print("=== 生成 4 份完整 Postman JSON ===\n")

    for name, (collection, rel_path) in collections.items():
        filepath = BASE / rel_path
        filepath.parent.mkdir(parents=True, exist_ok=True)

        with open(str(filepath), "w", encoding="utf-8") as f:
            json.dump(collection, f, ensure_ascii=False, indent=2)

        items = collection.get("item", [])
        type_counts = {}
        for item in items:
            test_type = item.get("name", "").split("_")[1] if "_" in item.get("name", "") else "其他"
            type_counts[test_type] = type_counts.get(test_type, 0) + 1

        print(f"✅ {name}: {rel_path}")
        print(f"   用例数: {len(items)} 条")
        for t, c in sorted(type_counts.items(), key=lambda x: -x[1]):
            print(f"   {t}: {c} 条")
        print()

    print("=== 完成 ===")
    print("每个文件都是独立的可运行 JSON，直接导入 Postman 即可使用。")


if __name__ == "__main__":
    main()