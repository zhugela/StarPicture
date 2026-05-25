package com.yu.backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间级别选项（用于前端展示）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
