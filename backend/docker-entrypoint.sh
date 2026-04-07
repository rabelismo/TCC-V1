#!/bin/sh
# Convert Railway's DATABASE_URL to Spring Boot env vars if present
if [ -n "$DATABASE_URL" ] && [ -z "$DB_URL" ]; then
    export DB_URL="jdbc:postgresql://${DATABASE_URL#*@}"
    export DB_USERNAME="$(echo "$DATABASE_URL" | sed -n 's|.*://\([^:]*\):.*|\1|p')"
    export DB_PASSWORD="$(echo "$DATABASE_URL" | sed -n 's|.*://[^:]*:\([^@]*\)@.*|\1|p')"
fi

# Railway injects PORT; map to Spring's SERVER_PORT
if [ -n "$PORT" ] && [ -z "$SERVER_PORT" ]; then
    export SERVER_PORT="$PORT"
fi

exec java -jar app.jar "$@"
