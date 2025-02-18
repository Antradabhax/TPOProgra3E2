package com.progra3_grupo2.tpo.servicios;

import com.progra3_grupo2.tpo.entidades.MovieEntity;
import com.progra3_grupo2.tpo.interfaces.MovieRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class BacktrackingService {
    private final MovieRepository movieRepository;
    private Set<String> visited;
    private List<List<String>> allPaths;

    public BacktrackingService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
        this.visited = new HashSet<>();
        this.allPaths = new ArrayList<>();
    }

    public Flux<List<String>> findAllPaths(String startTitle, String targetTitle) {
        // Reiniciar las estructuras de datos
        visited = new HashSet<>();
        allPaths = new ArrayList<>();
        
        System.out.println("\n====== INICIANDO BÚSQUEDA DE CAMINOS ======");
        System.out.println("Desde: " + startTitle);
        System.out.println("Hasta: " + targetTitle);
        
        // Iniciar el backtracking con el camino actual
        List<String> currentPath = new ArrayList<>();
        currentPath.add(startTitle);
        
        return movieRepository.findAll()
                .collectList()
                .flatMapMany(movies -> {
                    Map<String, Set<String>> graph = buildGraph(movies);
                    backtrack(startTitle, targetTitle, currentPath, graph);
                    
                    // Mostrar resumen al finalizar
                    System.out.println("\n====== RESUMEN DE LA BÚSQUEDA ======");
                    if (allPaths.isEmpty()) {
                        System.out.println("❌ No se encontraron caminos entre " + startTitle + " y " + targetTitle);
                    } else {
                        System.out.println("✅ Se encontraron " + allPaths.size() + " caminos:");
                        for (int i = 0; i < allPaths.size(); i++) {
                            System.out.println("\nCamino " + (i + 1) + ":");
                            System.out.println("Longitud: " + (allPaths.get(i).size() - 1) + " saltos");
                            System.out.println("Ruta: " + String.join(" → ", allPaths.get(i)));
                        }
                    }
                    System.out.println("\n====== FIN DE LA BÚSQUEDA ======");
                    
                    return Flux.fromIterable(allPaths);
                });
    }

    private void backtrack(String current, String target, List<String> path, Map<String, Set<String>> graph) {
        System.out.println("\n=== Estado actual del Backtracking ===");
        System.out.println("Explorando nodo: " + current);
        System.out.println("Camino actual: " + path);
        
        // Si encontramos el objetivo, guardamos el camino y retornamos
        if (current.equals(target)) {
            System.out.println("\n🎯 ¡CAMINO ENCONTRADO!");
            System.out.println("Longitud: " + (path.size() - 1) + " saltos");
            System.out.println("Ruta: " + String.join(" → ", path));
            allPaths.add(new ArrayList<>(path));
            return;
        }
        
        // Obtenemos los vecinos del nodo actual
        Set<String> neighbors = graph.getOrDefault(current, new HashSet<>());
        System.out.println("Vecinos disponibles: " + neighbors);

        // Exploramos cada vecino no visitado
        for (String next : neighbors) {
            if (!visited.contains(next)) {
                System.out.println("\n→ Intentando con vecino: " + next);
                visited.add(next);  // Marcamos como visitado antes de la recursión
                path.add(next);     // Agregamos al camino actual
                
                backtrack(next, target, path, graph);
                
                // Backtrack: deshacemos los cambios
                visited.remove(next);
                path.remove(path.size() - 1);
                System.out.println("⬅ Retrocediendo desde: " + next);
                System.out.println("  Nuevo camino después del retroceso: " + path);
            } else {
                System.out.println("❌ Vecino " + next + " ya visitado, saltando...");
            }
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
                        // Aseguramos que el grafo sea bidireccional
                        graph.computeIfAbsent(movie1.getTitle(), k -> new HashSet<>()).add(movie2.getTitle());
                        graph.computeIfAbsent(movie2.getTitle(), k -> new HashSet<>()).add(movie1.getTitle());
                    }
                }
            }
        }
        
        System.out.println("Grafo construido: " + graph);
        return graph;
    }
} 