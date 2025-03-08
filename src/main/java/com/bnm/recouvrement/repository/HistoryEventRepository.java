package com.bnm.recouvrement.repository;

import com.bnm.recouvrement.entity.HistoryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryEventRepository extends JpaRepository<HistoryEvent, Long> {
    List<HistoryEvent> findAllByOrderByTimestampDesc();
    List<HistoryEvent> findByUsernameOrderByTimestampDesc(String username);
    List<HistoryEvent> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);
}
