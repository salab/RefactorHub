#!/usr/bin/env bash
# wait-for-it.sh: Wait until a host and port are available
# (Official: https://github.com/vishnubob/wait-for-it)

set -e

HOST=$(echo $1 | cut -d: -f1)
PORT=$(echo $1 | cut -d: -f2)
shift

while ! nc -z $HOST $PORT; do
  echo "Waiting for $HOST:$PORT..."
  sleep 1
done

exec "$@"

