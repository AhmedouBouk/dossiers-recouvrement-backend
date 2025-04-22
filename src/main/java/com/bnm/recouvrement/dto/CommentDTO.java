package com.bnm.recouvrement.dto;

import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private String content;
    private String createdAt; 
    private Long dossierId;
    private UserInfo user;
    private String reminderDateTime;

    // Sous-DTO pour exposer l'id et le nom d'utilisateur
    public static class UserInfo {
        public Integer id;
        public String name;
        public UserInfo(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // Constructeur à partir de l'entité Comment
    public CommentDTO(com.bnm.recouvrement.entity.Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt().toString();
        this.dossierId = comment.getDossier().getId();
        if (comment.getUser() != null) {
            this.user = new UserInfo(comment.getUser().getId(), comment.getUser().getName());
        }
        if (comment.getReminderDateTime() != null) {
            this.reminderDateTime = comment.getReminderDateTime().toString();
        } else {
            this.reminderDateTime = null;
        }
    }

    public UserInfo getUser() {
        return user;
    }
    public void setUser(UserInfo user) {
        this.user = user;
    }
    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Long getDossierId() {
        return dossierId;
    }

    public void setDossierId(Long dossierId) {
        this.dossierId = dossierId;
    }

    public String getReminderDateTime() {
        return reminderDateTime;
    }

    public void setReminderDateTime(String reminderDateTime) {
        this.reminderDateTime = reminderDateTime;
    }
}