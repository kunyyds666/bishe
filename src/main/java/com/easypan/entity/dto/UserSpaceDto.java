package com.easypan.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class UserSpaceDto implements Serializable {
    private Long useSpace;
    private Long totalSpace;

}
