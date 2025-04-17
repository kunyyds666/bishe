package com.easypan.entity.enums;


import lombok.Getter;

@Getter
public enum FileFolderTypeEnums {
    FILE(0, "文件"),
    FOLDER(1, "目录");

    private Integer type;
    private String desc;

    FileFolderTypeEnums(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

}
