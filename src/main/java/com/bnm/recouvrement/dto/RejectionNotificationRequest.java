package com.bnm.recouvrement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectionNotificationRequest {
    private String reason;
    private List<String> userTypes;
} 