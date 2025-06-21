package com.bnm.recouvrement.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejetRequest {
    private String motif;
    private List<String> typesUtilisateurs;
    private List<Long> utilisateursIds;
}
