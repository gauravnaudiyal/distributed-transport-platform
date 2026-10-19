package com.groupb.booking.repository;

import com.groupb.booking.model.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
}
