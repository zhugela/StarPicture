package com.yu.backend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceDeleteRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
