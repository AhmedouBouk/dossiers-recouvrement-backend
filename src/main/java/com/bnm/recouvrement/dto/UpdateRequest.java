package com.bnm.recouvrement.dto;

public class UpdateRequest {
  
    private Long id; // Assuming user has a unique ID
    private String name;
    private String email;
    private String password;
    private String role;
    private String userType;
    private Long agenceId;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public Long getAgenceId() {
        return agenceId;
    }
    
    public void setAgenceId(Long agenceId) {
        this.agenceId = agenceId;
    }
}
