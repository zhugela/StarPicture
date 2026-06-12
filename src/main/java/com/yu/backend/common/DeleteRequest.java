package com.yu.backend.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 空间 id（分表路由键；公共图库传 0 或不传）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
