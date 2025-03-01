package com.bnm.recouvrement.dto;

import lombok.Data;

@Data
public class AssignRoleRequest {
    private Integer userId;   // ID of the user to whom the role will be assigned
    private String roleName;  // The name of the role to be assigned
}
