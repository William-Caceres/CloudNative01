#!/bin/bash
# Une rabbit2 al cluster formado por rabbit1. Idempotente: si ya esta unido,
# no hace nada. Se ejecuta en la EC2 cn-rabbit despues de docker compose up -d.
# Los volumenes persisten la membresia, por lo que en reinicios posteriores
# del lab el cluster se re-forma solo y este script no vuelve a hacer join.
set -euo pipefail

esperar() {
  local contenedor="$1"
  for _ in $(seq 1 24); do
    if docker exec "$contenedor" rabbitmq-diagnostics -q ping >/dev/null 2>&1; then
      return 0
    fi
    sleep 5
  done
  echo "ERROR: $contenedor no respondio a tiempo" >&2
  exit 1
}

esperar rabbit1
esperar rabbit2

if docker exec rabbit2 rabbitmqctl cluster_status 2>/dev/null | grep -q "rabbit@rabbit1"; then
  echo "rabbit2 ya forma parte del cluster"
else
  docker exec rabbit2 rabbitmqctl stop_app
  docker exec rabbit2 rabbitmqctl join_cluster rabbit@rabbit1
  docker exec rabbit2 rabbitmqctl start_app
  echo "rabbit2 unido al cluster"
fi

echo "=== cluster_status ==="
docker exec rabbit1 rabbitmqctl cluster_status
