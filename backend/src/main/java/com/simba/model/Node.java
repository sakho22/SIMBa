package com.simba.model;

public record Node(
    String id,
    String nom,
    double lat,
    double lon,
    String type
) {}