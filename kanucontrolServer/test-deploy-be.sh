#!/bin/bash
set -euo pipefail

echo "🛠️ Backend wird gebaut..."
./mvnw clean package -Dmaven.test.skip=true

JAR_FILE=$(find target -maxdepth 1 -type f -name 'kcserver-*.jar' ! -name '*-original.jar' | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ Kein kcserver-JAR in target gefunden."
    exit 1
fi

echo "📦 Verwende: $JAR_FILE"

scp "$JAR_FILE" \
    chris@kc-test:/opt/kanucontrol/kcserver/kcserver.jar

echo "🐳 Docker Image wird neu gebaut..."

ssh chris@kc-test "sudo -n /usr/local/bin/kc-test-restart"

echo "✅ TEST-Backend Deploy abgeschlossen."