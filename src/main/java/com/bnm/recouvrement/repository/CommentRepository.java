package com.bnm.recouvrement.repository;

import com.bnm.recouvrement.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDossierIdOrderByCreatedAtAsc(Long dossierId);
    List<Comment> findByDossierIdOrderByCreatedAtDesc(Long dossierId);
}
