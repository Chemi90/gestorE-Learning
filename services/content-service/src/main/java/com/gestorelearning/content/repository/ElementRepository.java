package com.gestorelearning.content.repository;

import com.gestorelearning.content.domain.ElementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ElementRepository extends JpaRepository<ElementEntity, UUID> {
    List<ElementEntity> findByUnitIdOrderByOrderIndexAsc(UUID unitId);
}
