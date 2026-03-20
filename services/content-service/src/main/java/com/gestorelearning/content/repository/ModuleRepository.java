package com.gestorelearning.content.repository;

import com.gestorelearning.content.domain.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<ModuleEntity, UUID> {
    List<ModuleEntity> findByCourseIdOrderByOrderIndexAsc(UUID courseId);

    @Modifying
    @Query(value = """
            DELETE FROM content.modules m
            WHERE m.course_id = :courseId
            """, nativeQuery = true)
    void deleteByCourseIdNative(UUID courseId);
}
