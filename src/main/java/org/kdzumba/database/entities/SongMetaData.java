package org.kdzumba.database.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "song_meta_data")
@AllArgsConstructor
@NoArgsConstructor
public class SongMetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name="artist", nullable = false)
    private String artist;

    @Column(name = "song", nullable = false)
    private String song;

    @Column(name = "year", nullable = false)
    private int year;

    @OneToMany(mappedBy = "songMetaData")
    private Set<FingerprintHashEntity> hashes;

    @Override
    public String toString() {
        return "{Artist: " + this.artist + ", Song: " + this.song + " Year: " + this.year + "}";
    }
}