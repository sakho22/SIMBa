package com.simba.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Edge(
    String source,
    String targer,
    String mode,

    @JsonProperty("weight_time")
    int weightTime,
    @JsonProperty("weight_co2")
    int weightCo2
) {}
