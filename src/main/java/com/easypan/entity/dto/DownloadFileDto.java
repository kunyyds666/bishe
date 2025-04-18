package com.easypan.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DownloadFileDto {
    private String downloadCode;
    private String fileId;
    private String fileName;
    private String filePath;

}
