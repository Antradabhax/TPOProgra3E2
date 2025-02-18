package com.progra3_grupo2.tpo.controllers;

import com.progra3_grupo2.tpo.entidades.MovieEntity;
import com.progra3_grupo2.tpo.interfaces.MovieRepository;
import com.progra3_grupo2.tpo.servicios.BacktrackingService;
import com.progra3_grupo2.tpo.servicios.RecorridoGrafosService;
import com.progra3_grupo2.tpo.servicios.RamificacionPodaService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieRepository movieRepository;
    private final BacktrackingService backtrackingService;
    private final RecorridoGrafosService recorridoGrafoService;
    private final RamificacionPodaService ramificacionPodaService;

    public MovieController(MovieRepository movieRepository, 
                         BacktrackingService backtrackingService,
                         RecorridoGrafosService recorridoGrafoService,
                         RamificacionPodaService ramificacionPodaService) {
        this.movieRepository = movieRepository;
        this.backtrackingService = backtrackingService;
        this.recorridoGrafoService = recorridoGrafoService;
        this.ramificacionPodaService = ramificacionPodaService;
    }

    @PutMapping
    Mono<MovieEntity> createOrUpdateMovie(@RequestBody MovieEntity newMovie) {
        return movieRepository.save(newMovie);
    }

    @GetMapping(value = { "", "/" }, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<MovieEntity> getMovies() {
        return movieRepository.findAll();
    }

    @GetMapping("/backtrack")
    public Flux<List<String>> findPathWithBacktracking(
            @RequestParam String startTitle,
            @RequestParam String targetTitle) {
        return backtrackingService.findAllPaths(startTitle, targetTitle);
    }

    @GetMapping("/dfs")
    public Flux<String> recorridoDFS(@RequestParam String startMovie) {
        return recorridoGrafoService.recorridoDFS(startMovie);
    }

    @GetMapping("/bfs")
    public Flux<String> recorridoBFS(@RequestParam String startMovie) {
        return recorridoGrafoService.recorridoBFS(startMovie);
    }

    @GetMapping("/branch-and-bound")
    public Flux<List<String>> findPathWithBranchAndBound(
            @RequestParam String startTitle,
            @RequestParam String targetTitle) {
        return ramificacionPodaService.encontrarMejorCamino(startTitle, targetTitle);
    }
}