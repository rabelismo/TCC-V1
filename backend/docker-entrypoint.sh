#!/bin/sh
# Convert DATABASE_URL (Railway/Render) to Spring Boot env vars
# Handles both postgres:// and postgresql:// prefixes
if [ -n "$DATABASE_URL" ] && [ -z "$DB_URL" ]; then
    DB_HOST_AND_PATH="${DATABASE_URL#*@}"
    export DB_URL="jdbc:postgresql://${DB_HOST_AND_PATH}"
    export DB_USERNAME="$(echo "$DATABASE_URL" | sed -n 's|.*://\([^:]*\):.*|\1|p')"
    export DB_PASSWORD="$(echo "$DATABASE_URL" | sed -n 's|.*://[^:]*:\([^@]*\)@.*|\1|p')"

    # Render requires SSL; append sslmode if not already present
    if [ -n "$RENDER" ]; then
        case "$DB_URL" in
            *sslmode=*) ;;
            *\?*) export DB_URL="${DB_URL}&sslmode=require" ;;
            *)    export DB_URL="${DB_URL}?sslmode=require" ;;
        esac
    fi
fi

# PaaS platforms inject PORT; map to Spring's SERVER_PORT
if [ -n "$PORT" ] && [ -z "$SERVER_PORT" ]; then
    export SERVER_PORT="$PORT"
fi

exec java -jar app.jar "$@"
