package com.bnm.recouvrement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.Comment;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.repository.CommentRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DossierRecouvrementRepository dossierRepository;

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByDossierId(Long dossierId) {
        return commentRepository.findByDossierIdOrderByCreatedAtDesc(dossierId);
    }

    @Transactional
public Comment addComment(Long dossierId, String content) {
    DossierRecouvrement dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier not found"));
    Comment comment = new Comment();
    comment.setContent(content);
    comment.setDossier(dossier);
    return commentRepository.save(comment); // Sauvegarde et retourne le commentaire avec l'ID généré
}
}