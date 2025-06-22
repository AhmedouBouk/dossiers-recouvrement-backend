package com.bnm.recouvrement.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rejets")
public class Rejet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierRecouvrement dossier;
    
    @Column(nullable = false, length = 1000)
    private String motif;
    
    @Column(nullable = false)
    private LocalDateTime dateRejet = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "rejete_par_id", nullable = false)
    private User rejetePar;
    
    @ManyToMany
    @JoinTable(
        name = "rejet_utilisateurs_notifies",
        joinColumns = @JoinColumn(name = "rejet_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> utilisateursNotifies = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "rejet_types_utilisateurs", joinColumns = @JoinColumn(name = "rejet_id"))
    @Column(name = "type_utilisateur")
    private List<String> typesUtilisateurs = new ArrayList<>();
    
    @Column(nullable = false)
    private boolean traite = false;
    
    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;
    
    @ManyToOne
    @JoinColumn(name = "traite_par_id")
    private User traitePar;
    
    // Constructeur pratique pour la création
    public Rejet(DossierRecouvrement dossier, String motif, User rejetePar, List<String> typesUtilisateurs) {
        this.dossier = dossier;
        this.motif = motif;
        this.rejetePar = rejetePar;
        this.typesUtilisateurs = typesUtilisateurs;
    }
}
