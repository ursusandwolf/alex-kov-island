module com.island.app {
    requires com.island.engine;
    requires com.island.nature;
    requires com.island.simcity;
    
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires org.slf4j;
    requires static lombok;

    // Spring Boot
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.web;
    requires spring.webmvc;
    requires spring.beans;
    requires spring.core;
    requires spring.messaging;
    requires spring.websocket;
    requires jakarta.annotation;
    requires jakarta.validation;
    
    // Infrastructure
    requires io.swagger.v3.oas.annotations;
    requires spring.boot.actuator;
    requires spring.boot.actuator.autoconfigure;
    requires micrometer.registry.prometheus;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires jakarta.persistence;
    requires spring.tx;

    opens com.island;
    opens com.island.config;
    opens com.island.controller;
    opens com.island.persistence;
    opens com.island.service;
    uses com.island.engine.core.SimulationPlugin;
}
