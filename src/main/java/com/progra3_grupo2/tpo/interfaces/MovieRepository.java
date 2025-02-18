package com.progra3_grupo2.tpo.interfaces;

import com.progra3_grupo2.tpo.entidades.MovieEntity;
import org.neo4j.driver.types.Path;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public interface MovieRepository extends ReactiveNeo4jRepository<MovieEntity, String> {
    Mono<MovieEntity> findOneByTitle(String title);

    @Query("""
        MATCH path = (start:Movie {title: $startTitle})-[*]-(target:Movie {title: $targetTitle})
        RETURN path
    """)
    Flux<Path> findBacktrackPath(String startTitle, String targetTitle);

}
