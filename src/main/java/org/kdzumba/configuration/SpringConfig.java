package org.kdzumba.configuration;

import org.kdzumba.AudioProcessor;
import org.kdzumba.database.repositories.FingerprintHashRepository;
import org.kdzumba.database.repositories.SongMetaDataRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    @Bean
    public AudioProcessor audioProcessor(FingerprintHashRepository fingerprintHashRepository,
                                         SongMetaDataRepository songMetaDataRepository) {
        return new AudioProcessor(fingerprintHashRepository, songMetaDataRepository);
    }
}
