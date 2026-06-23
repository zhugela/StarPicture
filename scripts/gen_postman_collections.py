"""
把 4 份 Postman 集合从 功能测试 移到 接口测试
（功能测试 目录已删，Postman 集合改放接口测试）
"""
import json
import shutil
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

# 4 份 Postman 集合（之前功能测试目录里，删的时候带走了）
# 重新生成（脚本不依赖功能测试目录）
def make_collection(name, base_url, items, description=""):
    return {
        "info": {
            "name": name,
            "description": description,
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": items,
        "variable": [
            {"key": "baseUrl", "value": base_url},
            {"key": "token", "value": ""},
        ]
    }

def req(name, method, path, body=None, desc=""):
    item = {
        "name": name,
        "request": {
            "method": method,
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "url": {
                "raw": "{{baseUrl}}" + path,
                "host": ["{{baseUrl}}"],
                "path": [p for p in path.split("/") if p]
            },
            "description": desc
        },
        "response": []
    }
    if body is not None:
        item["request"]["body"] = {"mode": "raw", "raw": json.dumps(body, ensure_ascii=False, indent=2)}
    return item

# ============ 朱远亮：user ============
user_items = [
    req("API-001_注册_Content-Type错误", "POST", "/user/register",
        {"userAccount": "test", "userPassword": "12345678", "checkPassword": "12345678"}),
    req("API-002_登录_无Content-Type", "POST", "/user/login",
        {"userAccount": "testuser01", "userPassword": "12345678"}),
    req("API-003_分页_缺current", "POST", "/user/list/page/vo", {"pageSize": 10}),
    req("API-004_获取_负数id", "GET", "/user/get?id=-1"),
    req("API-005_注册_缺字段", "POST", "/user/register", {}),
]
user_col = make_collection(
    "StarPicture_用户模块_接口测试",
    "http://localhost:8123/api",
    user_items,
    "朱远亮负责。覆盖 Content-Type 异常、参数缺失、类型错误等接口契约。\n导入：Postman → File → Import → 选此 json"
)

# ============ 李冠燃：picture ============
picture_items = [
    req("API-001_上传_缺multipart边界", "POST", "/file/upload", None,
        "form-data 不带 boundary，应返回 400 或 415"),
    req("API-002_编辑_id类型错误", "POST", "/picture/edit",
        {"id": "abc", "name": "x"}),
    req("API-003_查询_负数id", "GET", "/picture/get?id=-1"),
    req("API-004_分页_pageSize超限", "POST", "/picture/list/page",
        {"current": 1, "pageSize": 10000}),
    req("API-005_搜索_空文本", "POST", "/picture/search/picture", {"text": ""}),
    req("API-006_搜索_颜色_非法值", "POST", "/picture/search/color",
        {"picColor": "notacolor"}),
]
picture_col = make_collection(
    "StarPicture_图片模块_接口测试",
    "http://localhost:8123/api",
    picture_items,
    "李冠燃负责。覆盖 multipart、参数类型、空值、边界等接口契约。"
)

# ============ 李坤纬：space ============
space_items = [
    req("API-001_空间_缺Content-Type", "POST", "/space/add",
        {"spaceName": "x", "spaceLevel": 0}),
    req("API-002_成员_缺字段", "POST", "/spaceUser/add", {}),
    req("API-003_分析_缺spaceId", "POST", "/space/analyze/usage", {}),
    req("API-004_编辑_负数id", "POST", "/space/edit",
        {"id": -1, "spaceName": "x"}),
]
space_col = make_collection(
    "StarPicture_空间模块_接口测试",
    "http://localhost:8123/api",
    space_items,
    "李坤纬负责。覆盖 Content-Type 异常、参数缺失、负数 id 等接口契约。"
)

# ============ 林景彬：file + wxMp ============
file_items = [
    req("API-001_upload_缺multipart边界", "POST", "/file/upload", None,
        "form-data 不带 boundary，应返回 400 或 415"),
    req("API-002_upload_Content-Type错误", "POST", "/file/upload", None,
        "Content-Type=application/json 但 body 是二进制"),
    req("API-003_avatar_无文件", "POST", "/file/upload/avatar", None),
    req("API-004_wx_portal_无签名", "GET", "/wx/mp/portal"),
    req("API-005_wx_menu_无body", "POST", "/wx/mp/menu/create", {}),
]
file_col = make_collection(
    "StarPicture_文件_公众号模块_接口测试",
    "http://localhost:8123/api",
    file_items,
    "林景彬负责。覆盖 multipart、Content-Type、缺失参数等接口契约。"
)

cols = [
    (BASE / "朱远亮_脚本与截图/接口测试/user_api.postman_collection.json", user_col),
    (BASE / "李冠燃_脚本与截图/接口测试/picture_api.postman_collection.json", picture_col),
    (BASE / "李坤纬_脚本与截图/接口测试/space_api.postman_collection.json", space_col),
    (BASE / "林景彬_脚本与截图/接口测试/file_wxmp_api.postman_collection.json", file_col),
]

for p, c in cols:
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(c, ensure_ascii=False, indent=2), encoding='utf-8')
    print(f"已生成: {p}")

# 也给接口测试/ 写一个 README 提示（可选）
print(f"\n共 {len(cols)} 个 Postman 集合（放到 接口测试/ 目录）")
