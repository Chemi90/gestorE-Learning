package com.gestorelearning.content.repository;

import com.gestorelearning.content.domain.UnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UnitRepository extends JpaRepository<UnitEntity, UUID> {
    List<UnitEntity> findByModuleIdOrderByOrderIndexAsc(UUID moduleId);
}
