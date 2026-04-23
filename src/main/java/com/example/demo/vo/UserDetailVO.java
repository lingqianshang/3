package com.example.demo.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String address;
}