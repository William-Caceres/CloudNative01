#!/bin/bash
set -euo pipefail

esperar() {
  local contenedor="$1"
  for _ in $(seq 1 30); do
    if docker exec "$contenedor" rabbitmq-diagnostics -q ping >/dev/null 2>&1; then
      return 0
    fi
    sleep 5
  done
  echo "ERROR: $contenedor no respondio" >&2
  exit 1
}

esperar cn-dev-rabbit-1
esperar cn-dev-rabbit-2

if docker exec cn-dev-rabbit-2 rabbitmqctl cluster_status 2>/dev/null | grep -q "rabbit@rabbit-local1"; then
  echo "rabbit-local2 ya esta en el cluster"
else
  docker exec cn-dev-rabbit-2 rabbitmqctl stop_app
  docker exec cn-dev-rabbit-2 rabbitmqctl join_cluster rabbit@rabbit-local1
  docker exec cn-dev-rabbit-2 rabbitmqctl start_app
fi

docker exec cn-dev-rabbit-1 rabbitmqctl cluster_status
