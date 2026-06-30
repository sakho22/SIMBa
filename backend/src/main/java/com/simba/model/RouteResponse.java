package com.simba.model;

import java.util.List;

public record RouteResponse(
    List<Node> path,
    int totalTimeSeconds,
    int totalCo2Grams
) {}
