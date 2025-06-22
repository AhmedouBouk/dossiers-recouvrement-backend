package com.bnm.recouvrement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveRequest {
    private String motif;
    private String utilisateur;
}
