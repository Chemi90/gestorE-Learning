package com.gestorelearning.auth.repository;

import com.gestorelearning.auth.domain.OrganizationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {
}