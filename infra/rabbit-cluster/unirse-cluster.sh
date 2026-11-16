#!/bin/bash
# Une el nodo local al cluster de rabbit1. Idempotente.
set -euo pipefail

set -a
. ./.env
set +a

NODO="${RABBIT_NODE}"

esperar() {
  for _ in $(seq 1 30); do
    if docker exec "$NODO" rabbitmq-diagnostics -q ping >/dev/null 2>&1; then
      return 0
    fi
    sleep 5
  done
  echo "ERROR: $NODO no respondio a tiempo" >&2
  exit 1
}

esperar

if [ "$NODO" != "rabbit1" ]; then
  if docker exec "$NODO" rabbitmqctl cluster_status 2>/dev/null | grep -q "rabbit@rabbit1"; then
    echo "$NODO ya forma parte del cluster"
  else
    docker exec "$NODO" rabbitmqctl stop_app
    docker exec "$NODO" rabbitmqctl join_cluster rabbit@rabbit1
    docker exec "$NODO" rabbitmqctl start_app
    echo "$NODO unido al cluster"
  fi
fi

echo "=== cluster_status ==="
docker exec "$NODO" rabbitmqctl cluster_status
