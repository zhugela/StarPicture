"""
合并 4 人所有 JSON 文件，更新接口测试 JSON，保留所有截图
"""
import json
import shutil
from pathlib import Path
from copy import deepcopy

BASE = Path(r"D:\code\StarPicture\docs\内娱图库_海蒂与爷爷_朱远亮_18144610287")

# 每个成员的新功能测试 JSON 路径（gen_all_cases_and_postman.py 生成的）
NEW_JSON = {
    "朱远亮": BASE / "朱远亮_脚本与截图/功能测试/朱远亮_functional.postman_collection.json",
    "李冠燃": BASE / "李冠燃_脚本与截图/功能测试/李冠燃_functional.postman_collection.json",
    "李坤纬": BASE / "李坤纬_脚本与截图/功能测试/李坤纬_functional.postman_collection.json",
    "林景彬": BASE / "林景彬_脚本与截图/功能测试/林景彬_functional.postman_collection.json",
}

# 旧功能测试 JSON 路径
OLD_JSON = {
    "朱远亮": BASE / "朱远亮_脚本与截图/功能测试/user_functional.postman_collection.json",
    "李冠燃": BASE / "李冠燃_脚本与截图/功能测试/picture_functional.postman_collection.json",
    "李坤纬": BASE / "李坤纬_脚本与截图/功能测试/space_functional.postman_collection.json",
    "林景彬": BASE / "林景彬_脚本与截图/功能测试/file_wxmp_functional.postman_collection.json",
}

# 接口测试 JSON 路径
API_JSON = {
    "朱远亮": BASE / "朱远亮_脚本与截图/接口测试/user_api.postman_collection.json",
    "李冠燃": BASE / "李冠燃_脚本与截图/接口测试/picture_api.postman_collection.json",
    "李坤纬": BASE / "李坤纬_脚本与截图/接口测试/space_api.postman_collection.json",
    "林景彬": BASE / "林景彬_脚本与截图/接口测试/file_wxmp_api.postman_collection.json",
}


def merge_collections(old_path, new_path):
    """合并两个 Postman 集合，去重（以 name 为 key）"""
    old_data = json.loads(old_path.read_text(encoding="utf-8"))
    new_data = json.loads(new_path.read_text(encoding="utf-8"))

    old_items = {item["name"]: item for item in old_data.get("item", [])}
    new_items = {item["name"]: item for item in new_data.get("item", [])}

    # 合并：新 items 覆盖旧 items（如果 name 相同）
    merged = {**old_items, **new_items}

    # 按原来的顺序排列（先旧后新）
    merged_list = list(old_data.get("item", []))
    seen_names = {item["name"] for item in merged_list}
    for item in new_data.get("item", []):
        if item["name"] not in seen_names:
            merged_list.append(item)
            seen_names.add(item["name"])

    result = deepcopy(new_data)
    result["item"] = merged_list
    return result


def update_api_json(api_path, member):
    """更新接口测试 JSON，加入新的测试用例"""
    if not api_path.exists():
        # 如果没有接口测试 JSON，从新功能测试 JSON 中提取接口相关的
        return

    data = json.loads(api_path.read_text(encoding="utf-8"))

    # 为每个成员添加特定的接口测试用例
    new_items = []
    if member == "朱远亮":
        new_items = [
            {
                "name": "TC-API-001_注册_Content-Type错误",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/xml"}],
                    "url": {
                        "raw": "http://localhost:8123/api/user/register",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "user", "register"]
                    },
                    "body": {
                        "mode": "raw",
                        "raw": "<xml>test</xml>",
                        "options": {"raw": {"language": "xml"}}
                    }
                }
            },
            {
                "name": "TC-API-002_登录_无Cookie",
                "request": {
                    "method": "GET",
                    "header": [],
                    "url": {
                        "raw": "http://localhost:8123/api/user/get/login",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "user", "get", "login"]
                    }
                }
            },
            {
                "name": "TC-API-003_分页_第0页",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "http://localhost:8123/api/user/list/page/vo",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "user", "list", "page", "vo"]
                    },
                    "body": {
                        "mode": "raw",
                        "raw": json.dumps({"current": 0, "pageSize": 10}),
                        "options": {"raw": {"language": "json"}}
                    }
                }
            }
        ]
    elif member == "李冠燃":
        new_items = [
            {
                "name": "TC-API-001_上传_缺multipart边界",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "multipart/form-data"}],
                    "url": {
                        "raw": "http://localhost:8123/api/file/upload",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "file", "upload"]
                    },
                    "body": {
                        "mode": "raw",
                        "raw": "raw乱码内容",
                        "options": {"raw": {"language": "text"}}
                    }
                }
            },
            {
                "name": "TC-API-002_编辑_id类型错误",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "http://localhost:8123/api/picture/edit",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "picture", "edit"]
                    },
                    "body": {
                        "mode": "raw",
                        "raw": json.dumps({"id": "abc", "name": "x"}),
                        "options": {"raw": {"language": "json"}}
                    }
                }
            },
            {
                "name": "TC-API-003_搜图_缺text字段",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "http://localhost:8123/api/picture/search/picture",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "picture", "search", "picture"]
                    },
                    "body": {
                        "mode": "raw",
                        "raw": json.dumps({}),
                        "options": {"raw": {"language": "json"}}
                    }
                }
            }
        ]
    elif member == "李坤纬":
        new_items = [
            {
                "name": "TC-API-001_空间_缺Content-Type",
                "request": {
                    "method": "POST",
                    "header": [],
                    "url": {
                        "raw": "http://localhost:8123/api/space/add",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "space", "add"]
                    },
                    "body": {
                        "mode": "raw",
                        "raw": json.dumps({"spaceName": "x", "spaceLevel": 0}),
                        "options": {"raw": {"language": "json"}}
                    }
                }
            },
            {
                "name": "TC-API-002_分析_缺spaceId",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/json"}],
                    "url": {
                        "raw": "http://localhost:8123/api/space/analyze/usage",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "space", "analyze", "usage"]
                    },
                    "body": {
                        "mode": "raw",
                        "raw": json.dumps({}),
                        "options": {"raw": {"language": "json"}}
                    }
                }
            }
        ]
    elif member == "林景彬":
        new_items = [
            {
                "name": "TC-API-001_头像上传_缺multipart边界",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "multipart/form-data"}],
                    "url": {
                        "raw": "http://localhost:8123/api/file/upload",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "file", "upload"]
                    },
                    "body": {
                        "mode": "raw",
                        "raw": "raw乱码",
                        "options": {"raw": {"language": "text"}}
                    }
                }
            },
            {
                "name": "TC-API-002_头像上传_Content-Type错误",
                "request": {
                    "method": "POST",
                    "header": [{"key": "Content-Type", "value": "application/xml"}],
                    "url": {
                        "raw": "http://localhost:8123/api/file/upload",
                        "protocol": "http",
                        "host": ["localhost"],
                        "port": "8123",
                        "path": ["api", "file", "upload"]
                    },
                    "body": {
                        "mode": "raw",
                        "raw": "<xml>test</xml>",
                        "options": {"raw": {"language": "xml"}}
                    }
                }
            }
        ]

    # 合并新 items（去重）
    existing_names = {item["name"] for item in data.get("item", [])}
    for item in new_items:
        if item["name"] not in existing_names:
            data["item"].append(item)

    api_path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")


# 主逻辑
print("=== 开始合并和更新 ===\n")

for member in ["朱远亮", "李冠燃", "李坤纬", "林景彬"]:
    print(f"\n【{member}】")

    # 1. 合并功能测试 JSON
    old_json = OLD_JSON[member]
    new_json = NEW_JSON[member]
    if old_json.exists() and new_json.exists():
        merged = merge_collections(old_json, new_json)
        # 保存合并后的 JSON 到旧文件
        old_json.write_text(json.dumps(merged, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"  功能测试: 合并 {old_json.name} ({len(merged.get('item', []))} 条)")
    elif new_json.exists():
        # 如果没有旧文件，直接用新文件
        merged = json.loads(new_json.read_text(encoding="utf-8"))
        old_json.write_text(json.dumps(merged, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"  功能测试: 创建 {old_json.name} ({len(merged.get('item', []))} 条)")

    # 2. 更新接口测试 JSON
    api_json = API_JSON[member]
    if api_json.exists():
        update_api_json(api_json, member)
        api_data = json.loads(api_json.read_text(encoding="utf-8"))
        print(f"  接口测试: 更新 {api_json.name} ({len(api_data.get('item', []))} 条)")

    # 3. 列出安全测试文件（保留，不修改）
    sec_dir = BASE / f"{member}_脚本与截图/安全测试"
    if sec_dir.exists():
        files = list(sec_dir.glob("*"))
        print(f"  安全测试: {len(files)} 个文件保留")

    # 4. 列出性能测试文件（保留，不修改）
    perf_dir = BASE / f"{member}_脚本与截图/性能测试"
    if perf_dir.exists():
        files = list(perf_dir.glob("*"))
        print(f"  性能测试: {len(files)} 个文件保留")

    # 5. 列出功能测试截图（保留，不删）
    func_dir = BASE / f"{member}_脚本与截图/功能测试"
    if func_dir.exists():
        pngs = list(func_dir.glob("*.png"))
        jsons = list(func_dir.glob("*.json"))
        print(f"  功能测试截图: {len(pngs)} 张 PNG，{len(jsons)} 个 JSON")

print("\n=== 完成 ===")
print("所有截图和原有文件都保留，JSON 已更新。")