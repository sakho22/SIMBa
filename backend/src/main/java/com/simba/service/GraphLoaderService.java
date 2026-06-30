package com.simba.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simba.model.Edge;
import com.simba.model.GraphData;
import com.simba.model.Node;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GraphLoaderService {

    // Notre base de données en RAM (Lecture ultra-rapide O(1))
    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    @PostConstruct
    public void init() {
        System.out.println("Démarrage du chargement du graphe SIMBa...");

        try {
            // Le chemin relatif vers du fichier JSON généré par Python
            File jsonFile = new File("../data/processed/simba_graph.json");

            ObjectMapper mapper = new ObjectMapper();
            GraphData graphData = mapper.readValue(jsonFile, GraphData.class);

            // 1. Remplissage du dictionnaire des nœuds
            for (Node node : graphData.nodes()) {
                nodes.put(node.id(), node);
                // On initialise une liste vide d'arêtes pour chaque nœud (pour éviter les
                // NullPointerException)
                adjacencyList.put(node.id(), new ArrayList<>());
            }

            // 2. Création de la Liste d'Adjacence
            for (Edge edge : graphData.edges()) {
                // On ajoute l'arête à la liste des départs de ce nœud
                adjacencyList.get(edge.source()).add(edge);
            }

            System.out.println("Graphe chargé en mémoire avec succès !");
            System.out.println("   -> Noeuds chargés : " + nodes.size());
            System.out.println("   -> Arêtes chargées : " + graphData.edges().size());

        } catch (Exception e) {
            System.err.println("ERREUR CRITIQUE : Impossible de charger le graphe JSON.");
            System.err.println("Vérifiez que le fichier existe bien à l'emplacement indiqué.");
            e.printStackTrace();
        }
    }

    // Getters pour que l'algorithme de Dijkstra puisse consulter ces données
    public Map<String, Node> getNodes() {
        return nodes;
    }

    public Map<String, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }
}