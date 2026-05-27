package com.island.controller;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Supported simulation domains.
 */
public enum SimulationType {
    NATURE,
    SIMCITY;

    @JsonCreator
    public static SimulationType fromString(String value) {
        return SimulationType.valueOf(value.toUpperCase());
    }
}
