package com.simba.controller;

import com.simba.model.RouteResponse;
import com.simba.service.DijkstraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
// L'annotation CrossOrigin est cruciale : elle autorise votre page HTML/Leaflet
// à communiquer avec ce serveur Java, même s'ils ne sont pas sur le même port.
@CrossOrigin(origins = "*")
public class RouteController {

    private final DijkstraService dijkstraService;

    // Injection de dépendance du service
    public RouteController(DijkstraService dijkstraService) {
        this.dijkstraService = dijkstraService;
    }

    @GetMapping("/route")
    public ResponseEntity<?> getRoute(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(defaultValue = "time") String optimize) {

        try {
            // On appelle l'algorithme avec les paramètres reçus dans l'URL
            RouteResponse response = dijkstraService.findShortestPath(start, end, optimize);

            // On renvoie un code HTTP 200 (OK) avec le JSON généré
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Si l'utilisateur a rentré un faux ID (ex: "TCL_9999999"), on renvoie une
            // erreur 400
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Pour toute autre erreur inattendue, on renvoie une erreur 500
            return ResponseEntity.internalServerError().body("Une erreur interne est survenue lors du calcul.");
        }
    }
}