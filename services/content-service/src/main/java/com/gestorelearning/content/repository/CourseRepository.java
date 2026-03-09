package com.gestorelearning.content.repository;

import com.gestorelearning.content.domain.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {
    List<CourseEntity> findByOrganizationId(UUID organizationId);
}
