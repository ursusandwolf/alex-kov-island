#!/bin/bash
set -e

# Configuration
THREADS_LIST="1 2 4 8"
DURATION=5
WIDTH=20
HEIGHT=20
TICK_MS=10
JAR="island-app/target/island-app-1.54.0.jar"

echo "Starting demonstration multithread profile sweep ($WIDTH x $HEIGHT, ${DURATION}s)..."

for T in $THREADS_LIST; do
  echo "--- Profiling with sim.threads=$T ---"
  
  # Start app
  java -jar $JAR --sim.threads=$T --sim.width=$WIDTH --sim.height=$HEIGHT --sim.tickMs=$TICK_MS --island.headless=true --island.defaultPredatorPresenceChance=1 --island.defaultHerbivorePresenceChance=2 --server.port=8080 > /tmp/app_$T.log 2>&1 &
  APP_PID=$!
  
  # Wait for start
  echo "Waiting for app to start..."
  until curl -s http://127.0.0.1:8080/actuator/health | grep -q "UP"; do
    sleep 1
    if ! kill -0 $APP_PID 2>/dev/null; then
      echo "App failed to start. Logs:"
      tail -n 20 /tmp/app_$T.log
      exit 1
    fi
  done
  
  # Start simulation
  echo "Starting simulation..."
  curl -s -X POST "http://127.0.0.1:8080/api/v1/simulation/start?type=NATURE&width=$WIDTH&height=$HEIGHT&tickMs=$TICK_MS" > /dev/null
  
  # Profile
  echo "Collecting metrics..."
  bash scripts/profile-multithreading.sh http://127.0.0.1:8080 $DURATION /tmp/profile_threads_$T.csv
  
  # Shutdown
  echo "Stopping app..."
  kill $APP_PID
  wait $APP_PID || true
  
  # Extract results
  FIRST_TICK=$(head -n 2 /tmp/profile_threads_$T.csv | tail -n 1 | cut -d',' -f3)
  LAST_TICK=$(tail -n 1 /tmp/profile_threads_$T.csv | cut -d',' -f3)
  TPS=$(echo "scale=2; ($LAST_TICK - $FIRST_TICK) / $DURATION" | bc)
  CPU=$(tail -n 1 /tmp/profile_threads_$T.csv | cut -d',' -f5)
  
  echo "Thread $T results: Ticks=$(($LAST_TICK - $FIRST_TICK)), TPS=$TPS, CPU=$CPU"
done

echo "Sweep completed."
