package com.gtbs.adminauthority.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorityInstructionAuditRepository extends JpaRepository<AuthorityInstructionAuditEntity, UUID> {

    @Query(
            value =
                    """
            SELECT * FROM authority_instruction_audit a
            WHERE a.segment_ids IS NOT NULL
              AND EXISTS (
                SELECT 1 FROM jsonb_array_elements_text(a.segment_ids) elem
                WHERE elem = :segmentId
              )
            ORDER BY a.created_at DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<AuthorityInstructionAuditEntity> findRecentForSegment(@Param("segmentId") String segmentId, @Param("limit") int limit);
}
