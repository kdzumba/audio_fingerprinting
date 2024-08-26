package org.kdzumba.database.repositories;

import org.kdzumba.database.entities.FingerprintHashEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FingerprintHashRepository extends JpaRepository<FingerprintHashEntity, Long> {
    List<FingerprintHashEntity> findAllByOrderByHashAsc();
}