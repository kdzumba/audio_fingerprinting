package org.kdzumba.database.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.kdzumba.dataModels.FingerprintHash;

@Getter
@Setter
@Entity
@Table(name = "fingerprint_hash")
public class FingerprintHashEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "hash", nullable = false)
    private int hash;

    @Column(name = "anchor_time_offset", nullable = false)
    private double anchorTimeOffset;

    @ManyToOne
    @JoinColumn(name = "meta_data_id", referencedColumnName = "id", nullable = false)
    private SongMetaData songMetaData;

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        FingerprintHashEntity that = (FingerprintHashEntity) other;
        return this.hash == that.hash;
    }
}