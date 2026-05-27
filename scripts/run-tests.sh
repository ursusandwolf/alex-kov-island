#!/bin/bash
# Runs all tests in the project modules
echo "Running all tests..."
mvn test -pl island-app,island-engine,island-nature,island-simcity
