#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"

echo "🛠️ Backend wird gebaut..."
./mvnw clean package -DskipTests

JAR_FILE=$(find target -maxdepth 1 -type f -name 'kcserver-*.jar' ! -name '*-original.jar' | head -n 1)
[[ -n "$JAR_FILE" ]] || { echo "❌ Kein kcserver-JAR gefunden."; exit 1; }

echo "📦 Übertrage: $JAR_FILE"
scp "$JAR_FILE" root@kcserver-prod:/opt/kcserver/kcserver.jar.new
ssh root@kcserver-prod 'bash -s' <<'REMOTE'
set -euo pipefail
cd /opt/kcserver
backup="kcserver.jar.before-$(date +%Y%m%d-%H%M%S)"
cp -p kcserver.jar "$backup"
chown --reference=kcserver.jar kcserver.jar.new
chmod --reference=kcserver.jar kcserver.jar.new
mv kcserver.jar.new kcserver.jar
systemctl restart kcserver
pid=$(systemctl show kcserver -p MainPID --value)
echo "🔎 Warte auf vollständigen Anwendungsstart..."
ready=false
for attempt in $(seq 1 60); do
    if [[ "$(systemctl show kcserver -p MainPID --value)" != "$pid" ]] || ! systemctl is-active --quiet kcserver; then
        break
    fi
    if curl --fail --silent --max-time 15 http://127.0.0.1:8090/v3/api-docs >/dev/null; then
        ready=true
        break
    fi
    sleep 2
done
if [[ "$ready" != true ]]; then
    echo "❌ Backend nicht bereit. Vorheriges JAR gesichert als $backup."
    journalctl -u kcserver -n 80 --no-pager
    exit 1
fi
sleep 10
[[ "$(systemctl show kcserver -p MainPID --value)" == "$pid" ]]
systemctl is-active --quiet kcserver
curl --fail --silent --max-time 5 http://127.0.0.1:8090/v3/api-docs >/dev/null
sha256sum kcserver.jar
systemctl status kcserver --no-pager
REMOTE

echo "✅ Backend gestartet und HTTP-Bereitschaft geprüft."
