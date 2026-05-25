#!/bin/bash

set -euo pipefail

BASE_URL="${1:-http://127.0.0.1:8080}"
DURATION_SECONDS="${2:-30}"
OUTPUT_FILE="${3:-/tmp/island-multithread-profile.csv}"

metric_value() {
  local metric_name="$1"
  local response
  response="$(curl -s "${BASE_URL}/actuator/metrics/${metric_name}" || true)"
  printf '%s' "$response" | sed -n 's/.*"value"[[:space:]]*:[[:space:]]*\([0-9.eE+-]*\).*/\1/p' | head -n 1
}

snapshot_field() {
  local field_name="$1"
  local response
  response="$(curl -s "${BASE_URL}/api/v1/simulation/snapshot" || true)"
  printf '%s' "$response" | sed -n "s/.*\"${field_name}\"[[:space:]]*:[[:space:]]*\\([0-9][0-9]*\\).*/\\1/p" | head -n 1
}

status_value() {
  local response
  response="$(curl -s "${BASE_URL}/api/v1/simulation/status" || true)"
  printf '%s' "$response" | sed -n 's/.*"status"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n 1
}

echo "timestamp,status,tick,total_entities,process_cpu_usage,jvm_heap_used_bytes,jvm_gc_pause_max_seconds" > "$OUTPUT_FILE"

end_time=$(( $(date +%s) + DURATION_SECONDS ))
while [ "$(date +%s)" -lt "$end_time" ]; do
  timestamp="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  status="$(status_value)"
  tick="$(snapshot_field tickCount)"
  total_entities="$(snapshot_field totalEntityCount)"
  cpu_usage="$(metric_value process.cpu.usage)"
  heap_used="$(metric_value jvm.memory.used)"
  gc_pause_max="$(metric_value jvm.gc.pause)"

  echo "${timestamp},${status:-UNKNOWN},${tick:-0},${total_entities:-0},${cpu_usage:-0},${heap_used:-0},${gc_pause_max:-0}" >> "$OUTPUT_FILE"
  sleep 1
done

echo "Profile written to $OUTPUT_FILE"
