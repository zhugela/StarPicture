"""
给 4 个成员的 功能测试/ 目录补 Postman 集合
每份含 10 条功能用例（2 个功能点 × 5 条）
"""
import json
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

def make_collection(name, items):
    return {
        "info": {
            "name": name,
            "description": f"导入：Postman → File → Import → Upload Files → 选此 json\n跑法：左侧点集合 → 选每个用例 → 右侧 Send → 截图为证",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": items,
        "variable": [
            {"key": "baseUrl", "value": "http://localhost:8123/api"},
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

# ============ 朱远亮：用户注册 + 登录 ============
zhyl_items = [
    # 用户注册
    req("TC-UR-001_注册_账号密码正确", "POST", "/user/register",
        {"userAccount": "Zhyl_New01", "userPassword": "12345678", "checkPassword": "12345678"}),
    req("TC-UR-002_注册_账号已存在", "POST", "/user/register",
        {"userAccount": "Zhyl_New01", "userPassword": "12345678", "checkPassword": "12345678"}),
    req("TC-UR-003_注册_密码不一致", "POST", "/user/register",
        {"userAccount": "Zhyl_New02", "userPassword": "12345678", "checkPassword": "87654321"}),
    req("TC-UR-004_注册_账号为空", "POST", "/user/register",
        {"userAccount": "", "userPassword": "12345678", "checkPassword": "12345678"}),
    req("TC-UR-005_注册_账号长度不足", "POST", "/user/register",
        {"userAccount": "abc", "userPassword": "12345678", "checkPassword": "12345678"}),
    # 用户登录
    req("TC-UL-001_登录_账号密码正确", "POST", "/user/login",
        {"userAccount": "testuser01", "userPassword": "12345678"}),
    req("TC-UL-002_登录_密码错误", "POST", "/user/login",
        {"userAccount": "testuser01", "userPassword": "wrongpass"}),
    req("TC-UL-003_登录_账号不存在", "POST", "/user/login",
        {"userAccount": "testuser99", "userPassword": "12345678"}),
    req("TC-UL-004_登录_空body", "POST", "/user/login", {}),
    req("TC-UL-005_登录_密码小于8字符", "POST", "/user/login",
        {"userAccount": "testuser01", "userPassword": "1234567"}),
]
zhyl_col = make_collection("StarPicture_用户模块_功能测试", zhyl_items)

# ============ 李冠燃：图片本地上传 + 关键字搜索 ============
lgr_items = [
    # 图片本地上传
    req("TC-PU-001_本地上传_jpg", "POST", "/file/upload", None,
        "form-data 模式：file=1.jpg （记得设置 key=file）"),
    req("TC-PU-002_本地上传_png", "POST", "/file/upload", None,
        "form-data 模式：file=1.png"),
    req("TC-PU-003_本地上传_超过2MB", "POST", "/file/upload", None,
        "form-data 模式：file=5MB.jpg"),
    req("TC-PU-004_本地上传_非图片", "POST", "/file/upload", None,
        "form-data 模式：file=test.pdf"),
    req("TC-PU-005_本地上传_空文件", "POST", "/file/upload", None,
        "form-data 模式：file=0byte.jpg"),
    # 关键字搜索
    req("TC-PX-001_搜图_有结果", "POST", "/picture/search/picture", {"text": "猫"}),
    req("TC-PX-002_搜图_无结果", "POST", "/picture/search/picture", {"text": "xyz999"}),
    req("TC-PX-003_搜图_空文本", "POST", "/picture/search/picture", {"text": ""}),
    req("TC-PX-004_搜图_超长", "POST", "/picture/search/picture", {"text": "x" * 1000}),
    req("TC-PX-005_搜图_未登录", "POST", "/picture/search/picture", {}),
]
lgr_col = make_collection("StarPicture_图片模块_功能测试", lgr_items)

# ============ 李坤纬：空间创建 + 空间成员 ============
lkw_items = [
    # 空间创建
    req("TC-SP-001_获取空间等级", "GET", "/space/list/level"),
    req("TC-SP-002_创建空间_普通版", "POST", "/space/add",
        {"spaceName": "我的空间", "spaceLevel": 0}),
    req("TC-SP-003_创建空间_名称为空", "POST", "/space/add",
        {"spaceName": ""}),
    req("TC-SP-004_创建空间_名称超长", "POST", "/space/add",
        {"spaceName": "x" * 50}),
    req("TC-SP-005_创建空间_未登录", "POST", "/space/add",
        {"spaceName": "x"}),
    # 空间成员管理
    req("TC-SU-001_添加空间成员", "POST", "/spaceUser/add",
        {"spaceId": 1, "userId": 2, "spaceRole": "viewer"}),
    req("TC-SU-002_查询空间成员", "POST", "/spaceUser/list", {"spaceId": 1}),
    req("TC-SU-003_添加成员_重复", "POST", "/spaceUser/add",
        {"spaceId": 1, "userId": 2, "spaceRole": "viewer"}),
    req("TC-SU-004_添加成员_未登录", "POST", "/spaceUser/add",
        {"spaceId": 1, "userId": 2}),
    req("TC-SU-005_删除空间成员", "POST", "/spaceUser/delete", {"id": 1}),
]
lkw_col = make_collection("StarPicture_空间模块_功能测试", lkw_items)

# ============ 林景彬：文件上传 + 微信门户 ============
ljb_items = [
    # 文件本地上传
    req("TC-FL-001_本地上传_jpg_2MB", "POST", "/file/upload", None,
        "form-data 模式：file=1.jpg"),
    req("TC-FL-002_本地上传_超过2MB", "POST", "/file/upload", None,
        "form-data 模式：file=5MB.jpg"),
    req("TC-FL-003_本地上传_空文件", "POST", "/file/upload", None,
        "form-data 模式：file=0byte.jpg"),
    req("TC-FL-004_本地上传_非图片", "POST", "/file/upload", None,
        "form-data 模式：file=test.pdf"),
    req("TC-FL-005_本地上传_未登录", "POST", "/file/upload", None,
        "form-data 模式：file=1.jpg（不传 Cookie）"),
    # 微信公众号门户
    req("TC-WX-001_门户_GET_签名", "GET",
        "/wx/mp/portal?signature=xxx&timestamp=1234567890&nonce=abc&echostr=test"),
    req("TC-WX-002_门户_POST_XML", "POST", "/wx/mp/portal", None,
        "Body → raw → XML 模式"),
    req("TC-WX-003_创建菜单", "POST", "/wx/mp/menu/create",
        {"button": [{"name": "测试", "type": "click", "key": "test"}]}),
    req("TC-WX-004_创建菜单_空body", "POST", "/wx/mp/menu/create", {}),
    req("TC-WX-005_创建菜单_未登录", "POST", "/wx/mp/menu/create",
        {"button": [{"name": "测试", "type": "click", "key": "test"}]}),
]
ljb_col = make_collection("StarPicture_文件_公众号模块_功能测试", ljb_items)

cols = [
    (BASE / "朱远亮_脚本与截图/功能测试/user_functional.postman_collection.json", zhyl_col),
    (BASE / "李冠燃_脚本与截图/功能测试/picture_functional.postman_collection.json", lgr_col),
    (BASE / "李坤纬_脚本与截图/功能测试/space_functional.postman_collection.json", lkw_col),
    (BASE / "林景彬_脚本与截图/功能测试/file_wxmp_functional.postman_collection.json", ljb_col),
]

for p, c in cols:
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(c, ensure_ascii=False, indent=2), encoding='utf-8')
    print(f"已生成: {p}")

print(f"\n共 {len(cols)} 个 Postman 功能测试集合。")

# 同时给每个成员的"接口测试"目录里加一个"完整功能用例"集合
# 让用户从一份 .json 跑完 13 条（10 功能 + 1 性能 + 1 接口 + 1 安全）
# 但功能测试 Postman 已经够用了，不必重复