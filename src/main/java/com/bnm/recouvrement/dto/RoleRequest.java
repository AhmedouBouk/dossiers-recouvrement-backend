package com.bnm.recouvrement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    private String roleName;       // The name of the new role
    private List<String> permissions; // List of permissions assigned to this role
}
