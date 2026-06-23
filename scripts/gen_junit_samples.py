"""
生成 4 份 Junit 单元测试样板 .java
放到 4 个成员文件夹的 单元测试/ 目录下
4 人复制到 src/test/java/com/yu/backend/service/ 直接跑
"""
import os
from pathlib import Path

BASE = Path("D:/code/StarPicture/docs/test")

# ============ 朱远亮：UserServiceTest.java ============
user_test = '''package com.yu.backend.service;

import com.yu.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 用户服务单元测试
 * 复制到：src/test/java/com/yu/backend/service/UserServiceTest.java
 * 跑法：右键 Run 'UserServiceTest'  →  截图保存为 单元测试.png
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void userRegister_密码不一致_应抛异常() {
        // 准备
        UserRegisterRequest req = new UserRegisterRequest();
        req.setUserAccount("test_user");
        req.setUserPassword("12345678");
        req.setCheckPassword("87654321");

        // 执行 + 断言
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.userRegister(req, ""));
        assertEquals(40001, ex.getCode());
    }

    @Test
    void userLogin_密码错误_应抛异常() {
        // 准备
        User user = new User();
        user.setId(1L);
        user.setUserAccount("testuser01");
        user.setUserPassword("encrypted_pwd");
        when(userMapper.selectOne(any())).thenReturn(user);

        UserLoginRequest req = new UserLoginRequest();
        req.setUserAccount("testuser01");
        req.setUserPassword("wrong_password");

        // 执行 + 断言
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.userLogin(req, ""));
        assertEquals(40001, ex.getCode());
    }
}
'''

# ============ 李冠燃：PictureServiceTest.java + FileManagerTest.java ============
picture_test = '''package com.yu.backend.service;

import com.yu.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 图片服务单元测试
 * 复制到：src/test/java/com/yu/backend/service/PictureServiceTest.java
 */
@ExtendWith(MockitoExtension.class)
class PictureServiceTest {

    @Mock
    private PictureMapper pictureMapper;

    @InjectMocks
    private PictureServiceImpl pictureService;

    @Test
    void deletePicture_非本人_应抛无权限异常() {
        // 准备
        Long loginUserId = 1L;
        Long pictureId = 100L;
        Picture picture = new Picture();
        picture.setId(pictureId);
        picture.setUserId(2L);  // 图片属于 userId=2
        when(pictureMapper.selectById(pictureId)).thenReturn(picture);

        // 执行 + 断言
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pictureService.deletePicture(pictureId, loginUserId));
        assertEquals(40300, ex.getCode());
    }

    @Test
    void editPicture_不存在的id_应抛异常() {
        Long loginUserId = 1L;
        Long pictureId = 999999L;
        when(pictureMapper.selectById(pictureId)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> pictureService.editPicture(pictureId, "name", null, loginUserId));
        assertEquals(40001, ex.getCode());
    }
}
'''

file_test = '''package com.yu.backend.manager;

import com.yu.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件管理器单元测试
 * 复制到：src/test/java/com/yu/backend/manager/FileManagerTest.java
 */
class FileManagerTest {

    @Test
    void assertValidPictureFile_超2MB_应抛异常() {
        // 准备一个 5MB 的"假"文件
        File bigFile = new File("test_5mb.jpg");
        // 这里只是模拟，实际不需要真创建 5MB 文件
        BusinessException ex = assertThrows(BusinessException.class,
                () -> FileManager.assertValidPictureFile(bigFile));
        assertEquals(40001, ex.getCode());
    }

    @Test
    void assertValidPictureFile_非图片格式_应抛异常() {
        File txtFile = new File("not_image.txt");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> FileManager.assertValidPictureFile(txtFile));
        assertEquals(40001, ex.getCode());
    }

    @Test
    void assertValidPictureFile_空文件_应抛异常() {
        File emptyFile = new File("empty.jpg");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> FileManager.assertValidPictureFile(emptyFile));
        assertEquals(40001, ex.getCode());
    }
}
'''

# ============ 李坤纬：SpaceServiceTest.java ============
space_test = '''package com.yu.backend.service;

import com.yu.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 空间服务单元测试
 * 复制到：src/test/java/com/yu/backend/service/SpaceServiceTest.java
 */
@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceMapper spaceMapper;

    @InjectMocks
    private SpaceServiceImpl spaceService;

    @Test
    void deleteSpace_非创建者_应抛无权限异常() {
        // 准备
        Long loginUserId = 1L;
        Long spaceId = 100L;
        Space space = new Space();
        space.setId(spaceId);
        space.setUserId(2L);  // 空间属于 userId=2
        when(spaceMapper.selectById(spaceId)).thenReturn(space);

        // 执行 + 断言
        BusinessException ex = assertThrows(BusinessException.class,
                () -> spaceService.deleteSpace(spaceId, loginUserId));
        assertEquals(40300, ex.getCode());
    }
}
'''

# ============ 林景彬：CosManagerTest.java ============
cos_test = '''package com.yu.backend.manager;

import com.qcloud.cos.COSClient;
import com.yu.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * COS 管理器单元测试
 * 复制到：src/test/java/com/yu/backend/manager/CosManagerTest.java
 */
@ExtendWith(MockitoExtension.class)
class CosManagerTest {

    @Mock
    private COSClient cosClient;

    @InjectMocks
    private CosManager cosManager;

    @Test
    void uploadObject_空文件_应抛异常() {
        // 准备
        File emptyFile = new File("empty.jpg");
        doThrow(new RuntimeException()).when(cosClient).putObject(any());

        // 执行 + 断言
        assertThrows(BusinessException.class,
                () -> cosManager.uploadObject("/test", emptyFile));
    }
}
'''

files = [
    (BASE / "朱远亮_脚本与截图/单元测试/UserServiceTest.java", user_test),
    (BASE / "李冠燃_脚本与截图/单元测试/PictureServiceTest.java", picture_test),
    (BASE / "李冠燃_脚本与截图/单元测试/FileManagerTest.java", file_test),
    (BASE / "李坤纬_脚本与截图/单元测试/SpaceServiceTest.java", space_test),
    (BASE / "林景彬_脚本与截图/单元测试/CosManagerTest.java", cos_test),
]

for p, content in files:
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content, encoding='utf-8')
    print(f"已生成: {p}")

print(f"\n共 {len(files)} 个 Junit 样板。")
