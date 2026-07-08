#!/bin/bash
set -euo pipefail

ssh-add --apple-load-keychain >/dev/null 2>&1

echo "🛠️ Backend wird gebaut..."
./mvnw clean package -DskipTests

echo "📦 JAR wird kopiert..."
scp target/kcserver-0.0.1-SNAPSHOT.jar \
root@kcserver-prod:/opt/kcserver/kcserver.jar

echo "🔄 Service wird neu gestartet..."
ssh root@kcserver-prod << 'EOF'
systemctl restart kcserver
systemctl status kcserver --no-pager
EOF

echo "✅ Backend Deploy abgeschlossen."