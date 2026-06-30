package com.simba.model;

import java.util.List;


public record GraphData (
    List<Node> nodes,
    List<Edge> edges
){}
