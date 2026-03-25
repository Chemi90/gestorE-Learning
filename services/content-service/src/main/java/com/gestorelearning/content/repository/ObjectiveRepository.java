package com.gestorelearning.content.repository;

import com.gestorelearning.content.domain.ObjectiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ObjectiveRepository extends JpaRepository<ObjectiveEntity, UUID> {
    List<ObjectiveEntity> findByUnitIdOrderByOrderIndexAsc(UUID unitId);
}
