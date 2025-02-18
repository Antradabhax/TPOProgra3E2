package com.progra3_grupo2.tpo.servicios;

import com.progra3_grupo2.tpo.entidades.MovieEntity;
import com.progra3_grupo2.tpo.interfaces.MovieRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class RamificacionPodaService {
    private final MovieRepository movieRepository;
    private Map<String, Integer> heuristica;

    public RamificacionPodaService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
        this.heuristica = new HashMap<>();
    }

    public Flux<List<String>> encontrarMejorCamino(String startTitle, String targetTitle) {
        return movieRepository.findAll()
                .collectList()
                .flatMapMany(movies -> {
                    calcularHeuristica(movies, targetTitle);
                    Map<String, Set<String>> graph = buildGraph(movies);
                    List<List<String>> mejoresCaminos = branchAndBound(startTitle, targetTitle, graph);
                    return Flux.fromIterable(mejoresCaminos);
                });
    }

    private void calcularHeuristica(List<MovieEntity> movies, String target) {
        MovieEntity targetMovie = movies.stream()
                .filter(m -> m.getTitle().equals(target))
                .findFirst()
                .orElse(null);

        if (targetMovie != null) {
            Set<String> targetActors = new HashSet<>();
            targetMovie.getActors().forEach(actor -> targetActors.add(actor.getName()));

            movies.forEach(movie -> {
                long actoresCompartidos = movie.getActors().stream()
                        .filter(actor -> targetActors.contains(actor.getName()))
                        .count();
                heuristica.put(movie.getTitle(), (int) actoresCompartidos);
            });
        }
    }

    private List<List<String>> branchAndBound(String start, String target, Map<String, Set<String>> graph) {
        PriorityQueue<CaminoParcial> cola = new PriorityQueue<>();
        List<List<String>> mejoresCaminos = new ArrayList<>();
        Set<String> visitados = new HashSet<>();
        int mejorCosto = Integer.MAX_VALUE;

        cola.offer(new CaminoParcial(Arrays.asList(start), 0));
        System.out.println("\n=== Iniciando Branch and Bound ===");
        System.out.println("Película inicial: " + start);
        System.out.println("Película objetivo: " + target);

        while (!cola.isEmpty()) {
            CaminoParcial actual = cola.poll();
            String ultimoNodo = actual.camino.get(actual.camino.size() - 1);
            
            System.out.println("\nExplorando camino: " + actual.camino);
            System.out.println("Costo actual: " + actual.costo);

            if (ultimoNodo.equals(target)) {
                System.out.println("¡Camino encontrado hasta el objetivo!");
                if (actual.costo < mejorCosto) {
                    System.out.println("Nuevo mejor camino encontrado (costo: " + actual.costo + ")");
                    mejorCosto = actual.costo;
                    mejoresCaminos.clear();
                    mejoresCaminos.add(new ArrayList<>(actual.camino));
                } else if (actual.costo == mejorCosto) {
                    System.out.println("Camino alternativo encontrado con mismo costo");
                    mejoresCaminos.add(new ArrayList<>(actual.camino));
                }
                continue;
            }

            if (actual.costo >= mejorCosto) {
                System.out.println("PODA: Camino actual descartado por costo mayor o igual a " + mejorCosto);
                continue;
            }

            visitados.add(ultimoNodo);
            Set<String> vecinos = graph.getOrDefault(ultimoNodo, new HashSet<>());
            
            System.out.println("Ramificando desde: " + ultimoNodo);
            System.out.println("Vecinos disponibles: " + vecinos);
            
            for (String vecino : vecinos) {
                if (!visitados.contains(vecino)) {
                    List<String> nuevoCamino = new ArrayList<>(actual.camino);
                    nuevoCamino.add(vecino);
                    
                    int nuevoCosto = actual.costo + 1 - heuristica.getOrDefault(vecino, 0);
                    System.out.println("  → Agregando rama hacia: " + vecino + " (costo estimado: " + nuevoCosto + ")");
                    cola.offer(new CaminoParcial(nuevoCamino, nuevoCosto));
                }
            }
            
            visitados.remove(ultimoNodo);
        }

        System.out.println("\n=== Búsqueda completada ===");
        System.out.println("Mejores caminos encontrados: " + mejoresCaminos);
        return mejoresCaminos;
    }

    private static class CaminoParcial implements Comparable<CaminoParcial> {
        List<String> camino;
        int costo;

        CaminoParcial(List<String> camino, int costo) {
            this.camino = camino;
            this.costo = costo;
        }

        @Override
        public int compareTo(CaminoParcial otro) {
            return Integer.compare(this.costo, otro.costo);
        }
    }

    private Map<String, Set<String>> buildGraph(List<MovieEntity> movies) {
        Map<String, Set<String>> graph = new HashMap<>();
        
        for (MovieEntity movie1 : movies) {
            for (MovieEntity movie2 : movies) {
                if (!movie1.getTitle().equals(movie2.getTitle())) {
                    boolean shareActor = movie1.getActors().stream()
                            .anyMatch(actor1 -> movie2.getActors().stream()
                                    .anyMatch(actor2 -> actor2.getName().equals(actor1.getName())));
                    
                    if (shareActor) {
                        graph.computeIfAbsent(movie1.getTitle(), k -> new HashSet<>()).add(movie2.getTitle());
                    }
                }
            }
        }
        return graph;
    }
}
