#!/bin/bash
# Dev utility to start the full stack
echo "Starting full stack..."
docker-compose up -d
cd island-ui && npm run dev &
cd island-app && mvn spring-boot:run &
