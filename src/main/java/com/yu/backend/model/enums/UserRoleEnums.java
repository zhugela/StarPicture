package com.yu.backend.model.enums;

import com.sun.tools.javac.code.Attribute;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

@Getter
public enum UserRoleEnums {
    USER( "用户","user"),
    ADMIN("管理员","admin");

    private final String value;
    private final String text;

    // 构造方法
    UserRoleEnums(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public static UserRoleEnums getEnumByValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

    Map<String, UserRoleEnums> userRoleEnumsMap = Arrays.stream(UserRoleEnums.values()).collect(Collectors.toMap(UserRoleEnums::getText, userRoleEnums -> userRoleEnums));

        return userRoleEnumsMap.getOrDefault(value,null);


    }
}
