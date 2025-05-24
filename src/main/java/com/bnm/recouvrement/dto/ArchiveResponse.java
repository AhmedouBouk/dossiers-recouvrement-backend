package com.bnm.recouvrement.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ArchiveResponse {
    private Long dossierId;
    private boolean success;
    private String message;
    private String dateArchivage;
}
