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
root@kcserver-prod:/opt/kcserver/kcserver.jar

echo "🔄 Service wird neu gestartet..."
ssh root@kcserver-prod << 'EOF'
systemctl restart kcserver
systemctl status kcserver --no-pager
EOF

echo "✅ Backend Deploy abgeschlossen."
