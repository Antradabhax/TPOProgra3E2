package com.progra3_grupo2.tpo.servicios;

import com.progra3_grupo2.tpo.entidades.MovieEntity;
import com.progra3_grupo2.tpo.interfaces.MovieRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class RecorridoGrafosService {
    private final MovieRepository movieRepository;

    public RecorridoGrafosService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Flux<String> recorridoDFS(String startMovie) {
        return movieRepository.findAll()
                .collectList()
                .flatMapMany(movies -> {
                    Map<String, Set<String>> graph = buildGraph(movies);
                    List<String> result = dfs(startMovie, graph);
                    return Flux.fromIterable(result);
                });
    }

    public Flux<String> recorridoBFS(String startMovie) {
        return movieRepository.findAll()
                .collectList()
                .flatMapMany(movies -> {
                    Map<String, Set<String>> graph = buildGraph(movies);
                    List<String> result = bfs(startMovie, graph);
                    return Flux.fromIterable(result);
                });
    }

    private List<String> dfs(String start, Map<String, Set<String>> graph) {
        List<String> visited = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        stack.push(start);

        System.out.println("\n=== Iniciando recorrido DFS ===");
        System.out.println("Nodo inicial: " + start);

        while (!stack.isEmpty()) {
            String current = stack.pop();
            System.out.println("\nPila actual: " + stack);

            if (!visited.contains(current)) {
                System.out.println("🔍 Visitando: " + current);
                visited.add(current);

                Set<String> neighbors = graph.getOrDefault(current, new HashSet<>());
                System.out.println("Vecinos encontrados: " + neighbors);

                for (String neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        System.out.println("  → Agregando a la pila: " + neighbor);
                        stack.push(neighbor);
                    } else {
                        System.out.println("  ❌ " + neighbor + " ya visitado, ignorando...");
                    }
                }
            }
        }

        System.out.println("\n=== Recorrido DFS completado ===");
        System.out.println("Orden de visita: " + visited);
        return visited;
    }

    private List<String> bfs(String start, Map<String, Set<String>> graph) {
        List<String> visited = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(start);

        System.out.println("\n=== Iniciando recorrido BFS ===");
        System.out.println("Nodo inicial: " + start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            System.out.println("\nCola actual: " + queue);

            if (!visited.contains(current)) {
                System.out.println("🔍 Visitando: " + current);
                visited.add(current);

                Set<String> neighbors = graph.getOrDefault(current, new HashSet<>());
                System.out.println("Vecinos encontrados: " + neighbors);

                for (String neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        System.out.println("  → Agregando a la cola: " + neighbor);
                        queue.offer(neighbor);
                    } else {
                        System.out.println("  ❌ " + neighbor + " ya visitado, ignorando...");
                    }
                }
            }
        }

        System.out.println("\n=== Recorrido BFS completado ===");
        System.out.println("Orden de visita: " + visited);
        return visited;
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