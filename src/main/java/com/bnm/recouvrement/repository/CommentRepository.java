package com.bnm.recouvrement.repository;

import com.bnm.recouvrement.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDossierIdOrderByCreatedAtDesc(Long dossierId);
}