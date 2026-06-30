package com.simba.service;

import com.simba.model.Edge;
import com.simba.model.Node;
import com.simba.model.RouteResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DijkstraService {

    private final GraphLoaderService graphLoader;

    // L'injection de dépendance : Spring connecte automatiquement notre base de
    // données (GraphLoader) à cet algorithme
    public DijkstraService(GraphLoaderService graphLoader) {
        this.graphLoader = graphLoader;
    }

    // Un "record" interne qui représente l'état d'exploration pour notre file de
    // priorité
    private record State(String nodeId, int currentWeight) implements Comparable<State> {
        @Override
        public int compareTo(State other) {
            // Permet à la file de toujours mettre l'état avec le coût le plus faible en
            // premier
            return Integer.compare(this.currentWeight, other.currentWeight);
        }
    }

    public RouteResponse findShortestPath(String startId, String endId, String optimizeBy) {
        Map<String, Node> nodes = graphLoader.getNodes();
        Map<String, List<Edge>> adjList = graphLoader.getAdjacencyList();

        // 1. Sécurité : Vérifier que les arrêts demandés existent bien dans notre RAM
        if (!nodes.containsKey(startId) || !nodes.containsKey(endId)) {
            throw new IllegalArgumentException("Le point de départ ou d'arrivée est introuvable.");
        }

        // 2. Initialisation des structures de données de Dijkstra
        PriorityQueue<State> pq = new PriorityQueue<>();
        Map<String, Integer> minWeights = new HashMap<>(); // Stocke le coût minimum pour atteindre chaque nœud
        Map<String, Edge> previousEdge = new HashMap<>(); // Le "fil d'Ariane" pour reconstruire le chemin à la fin

        // On met la distance de tous les nœuds à l'infini (le plus grand entier
        // possible)
        for (String nodeId : nodes.keySet()) {
            minWeights.put(nodeId, Integer.MAX_VALUE);
        }

        // Le point de départ a une distance de 0
        minWeights.put(startId, 0);
        pq.add(new State(startId, 0));

        // 3. Boucle principale d'exploration
        while (!pq.isEmpty()) {
            State current = pq.poll(); // On prend toujours le nœud le plus "proche" disponible
            String currentNodeId = current.nodeId();

            // Optimisation : Si on est arrivé à destination, on peut arrêter de chercher
            if (currentNodeId.equals(endId)) {
                break;
            }

            // Si on a trouvé un meilleur chemin pour ce nœud entre-temps, on ignore cet
            // état
            if (current.currentWeight() > minWeights.get(currentNodeId)) {
                continue;
            }

            // 4. Explorer les voisins du nœud actuel
            List<Edge> neighbors = adjList.getOrDefault(currentNodeId, Collections.emptyList());
            for (Edge edge : neighbors) {
                // On choisit le poids (Temps ou CO2) selon la demande de l'utilisateur
                int edgeWeight = optimizeBy.equalsIgnoreCase("co2") ? edge.weightCo2() : edge.weightTime();

                int newWeight = minWeights.get(currentNodeId) + edgeWeight;

                // Si ce nouveau chemin est meilleur que l'ancien, on met à jour
                if (newWeight < minWeights.get(edge.target())) {
                    minWeights.put(edge.target(), newWeight);
                    previousEdge.put(edge.target(), edge); // On retient par où on est passé
                    pq.add(new State(edge.target(), newWeight));
                }
            }
        }

        // 5. Reconstruire et renvoyer la réponse finale
        return buildResponse(startId, endId, previousEdge, nodes);
    }

    private RouteResponse buildResponse(String startId, String endId, Map<String, Edge> previousEdge,
            Map<String, Node> nodes) {
        // Si la destination n'est pas dans nos traces, c'est qu'aucun chemin n'existe
        if (!previousEdge.containsKey(endId) && !startId.equals(endId)) {
            return new RouteResponse(Collections.emptyList(), 0, 0); // Chemin vide
        }

        List<Node> path = new ArrayList<>();
        int totalTime = 0;
        int totalCo2 = 0;

        // On remonte le fil d'Ariane depuis l'arrivée jusqu'au départ
        String current = endId;
        path.add(nodes.get(current));

        while (!current.equals(startId)) {
            Edge edge = previousEdge.get(current);
            totalTime += edge.weightTime();
            totalCo2 += edge.weightCo2();

            current = edge.source();
            path.add(nodes.get(current));
        }

        // On inverse la liste pour qu'elle aille du départ vers l'arrivée
        Collections.reverse(path);

        return new RouteResponse(path, totalTime, totalCo2);
    }
}