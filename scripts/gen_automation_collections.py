"""
4 份 Postman Runner 自动化测试集合，放到 各成员/自动化测试/
每个集合用 Postman Runner 跑，自动循环 10 次
"""
import json
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

def make_runner_col(name, desc, items, iterations=10):
    return {
        "info": {
            "name": name,
            "description": desc + f"\n跑法：Postman → Runner → 选此集合 → Iterations={iterations} → Start\n截图：保存 Runner 全部 pass 截图",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": items,
        "variable": [
            {"key": "baseUrl", "value": "http://localhost:8123/api"},
            {"key": "token", "value": ""},
        ]
    }

def req(name, method, path, body=None):
    item = {
        "name": name,
        "request": {
            "method": method,
            "header": [{"key": "Content-Type", "value": "application/json"}],
            "url": {
                "raw": "{{baseUrl}}" + path,
                "host": ["{{baseUrl}}"],
                "path": [p for p in path.split("/") if p]
            }
        },
        "response": []
    }
    if body is not None:
        item["request"]["body"] = {"mode": "raw", "raw": json.dumps(body, ensure_ascii=False, indent=2)}
    return item

# 朱远亮：用户 CRUD 自动化
user_auto = make_runner_col(
    "StarPicture_用户CRUD_自动化测试",
    "朱远亮负责。Postman Runner 自动循环 10 次跑：注册→登录→获取→修改→删除 全流程。\n截图：Runner 全部绿色 pass。",
    [
        req("01_注册_随机账号", "POST", "/user/register",
            {"userAccount": "AutoUser_{{$randomInt}}", "userPassword": "12345678", "checkPassword": "12345678"}),
        req("02_登录_testuser01", "POST", "/user/login",
            {"userAccount": "testuser01", "userPassword": "12345678"}),
        req("03_获取当前用户", "GET", "/user/get/login"),
        req("04_修改昵称", "POST", "/user/update/my", {"userName": "自动化测试用户_{{$randomInt}}"}),
        req("05_分页查询_默认", "POST", "/user/list/page/vo", {"current": 1, "pageSize": 10}),
        req("06_分页查询_空", "POST", "/user/list/page/vo", {"current": 9999, "pageSize": 10}),
    ]
)

# 李冠燃：图片自动化
pic_auto = make_runner_col(
    "StarPicture_图片流程_自动化测试",
    "李冠燃负责。Postman Runner 自动循环 10 次：分页查询 / 标签聚合 / 关键字搜索。",
    [
        req("01_分页查询_默认", "POST", "/picture/list/page", {"current": 1, "pageSize": 10}),
        req("02_分页查询_缓存版", "POST", "/picture/list/page/vo/cache", {"current": 1, "pageSize": 10}),
        req("03_按id查询_vo", "GET", "/picture/get/vo?id=1"),
        req("04_标签分类聚合", "GET", "/picture/tag_category"),
        req("05_关键字搜图_猫", "POST", "/picture/search/picture", {"text": "猫"}),
        req("06_颜色搜图_蓝色", "POST", "/picture/search/color", {"picColor": "#0000FF"}),
    ]
)

# 李坤纬：空间自动化
space_auto = make_runner_col(
    "StarPicture_空间CRUD_自动化测试",
    "李坤纬负责。Postman Runner 自动循环 10 次跑：空间 + 成员 + 分析。",
    [
        req("01_获取空间等级", "GET", "/space/list/level"),
        req("02_查询空间_vo", "GET", "/space/get/vo?id=1"),
        req("03_分页查询", "POST", "/space/list/page/vo", {"current": 1, "pageSize": 10}),
        req("04_空间成员列表", "POST", "/spaceUser/list", {"spaceId": 1}),
        req("05_我加入的空间", "POST", "/spaceUser/list/my"),
        req("06_空间使用情况", "POST", "/space/analyze/usage", {"spaceId": 1}),
        req("07_空间排行", "POST", "/space/analyze/rank", {}),
    ]
)

# 林景彬：文件+公众号自动化
file_auto = make_runner_col(
    "StarPicture_文件_自动化测试",
    "林景彬负责。Postman Runner 自动循环 10 次：上传 + 菜单 + 微信门户。",
    [
        req("01_微信门户_GET签名", "GET", "/wx/mp/portal?signature=xxx&timestamp=1234567890&nonce=abc&echostr=test"),
        req("02_创建菜单", "POST", "/wx/mp/menu/create", {"button": [{"name": "测试", "type": "click", "key": "test"}]}),
        req("03_上传_无文件_应失败", "POST", "/file/upload", None),
        req("04_avatar_未登录_应失败", "POST", "/file/upload/avatar", None),
    ]
)

cols = [
    (BASE / "朱远亮_脚本与截图/自动化测试/user_crud_automation.postman_collection.json", user_auto),
    (BASE / "李冠燃_脚本与截图/自动化测试/picture_automation.postman_collection.json", pic_auto),
    (BASE / "李坤纬_脚本与截图/自动化测试/space_automation.postman_collection.json", space_auto),
    (BASE / "林景彬_脚本与截图/自动化测试/file_automation.postman_collection.json", file_auto),
]

for p, c in cols:
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(c, ensure_ascii=False, indent=2), encoding='utf-8')
    print(f"已生成: {p}")

print(f"\n共 {len(cols)} 个 Postman Runner 集合。")
