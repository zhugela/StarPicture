package com.yu.backend.model.dto.space;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SpaceUserRole implements Serializable {

    private String key;

    private String name;

    private List<String> permissions;

    private String description;

    private static final long serialVersionUID = 1L;
}
