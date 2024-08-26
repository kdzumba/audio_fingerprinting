package org.kdzumba.database.repositories;

import org.kdzumba.database.entities.SongMetaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongMetaDataRepository extends JpaRepository<SongMetaData, Long> {
}