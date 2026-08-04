#!/bin/bash
set -euo pipefail
trap 'echo "❌ Fehler in Zeile $LINENO"; exit 1' ERR

command -v jq >/dev/null || {
    echo "Fehler: jq ist nicht installiert."
    exit 1
}

LOGFILE="/var/log/cloudflare-ddns.log"
exec >>"$LOGFILE" 2>&1

echo "========================================"
VERSION="2.3"
echo "Cloudflare DDNS v$VERSION"
echo "$(date '+%F %T')"
echo "Hostname: $(hostname)"

DRYRUN="${DRYRUN:-false}"

TOKEN_FILE="/opt/admin-scripts/.cloudflare.token"

if [ ! -f "$TOKEN_FILE" ]; then
    echo "Fehler: Cloudflare-Token nicht gefunden: $TOKEN_FILE"
    exit 1
fi

TOKEN=$(<"$TOKEN_FILE")

ZONE_ID="1098e0f0d01019404fb4602f8694a902"

declare -A RECORDS=(
    ["kc"]="855410c16788613032c1cf4c79896940"
    ["auth"]="c90aea80c28f53e59dab49f0c4686b46"
)

declare -A PROXY=(
    ["kc"]="true"
    ["auth"]="false"
)

IP1=$(curl --retry 3 --retry-delay 2 --fail-with-body -4 -sS \
    --connect-timeout 5 --max-time 10 \
    https://api.ipify.org)
IP2=$(curl --retry 3 --retry-delay 2 --fail-with-body -4 -sS \
    --connect-timeout 5 --max-time 10 \
    https://ifconfig.me/ip)

# Beide Dienste müssen dieselbe IP liefern
if [ "$IP1" != "$IP2" ]; then
    echo "❌ Fehler: Unterschiedliche öffentliche IPs!"
    echo "ipify     : $IP1"
    echo "ifconfig.me: $IP2"
    exit 1
fi

IP="$IP1"

if ! [[ "$IP" =~ ^([0-9]{1,3}\.){3}[0-9]{1,3}$ ]]; then
    echo "❌ Ungültige IPv4-Adresse: $IP"
    exit 1
fi

case "$IP" in
    10.*|172.16.*|172.17.*|172.18.*|172.19.*|172.2?.*|172.30.*|172.31.*|192.168.*)
        echo "❌ Private IP erkannt: $IP"
        exit 1
        ;;
esac


echo "[$(date '+%F %T')] Aktuelle IP: $IP"
echo "IP      : $IP"

update_record() {
    local RECORD_ID="$1"
    local PROXIED="$2"

    # Aktuellen DNS-Eintrag lesen
    RECORD=$(curl --retry 3 --retry-delay 2 --fail-with-body -sS --connect-timeout 5 --max-time 10 \
      -H "Authorization: Bearer $TOKEN" \
      "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records/$RECORD_ID")

      SUCCESS=$(echo "$RECORD" | jq -r '.success')

      if [ "$SUCCESS" != "true" ]; then
          echo "❌ Cloudflare konnte den DNS-Eintrag nicht lesen."
          echo "$RECORD" | jq .
          exit 1
      fi

    NAME=$(echo "$RECORD" | jq -r '.result.name')
    OLD_IP=$(echo "$RECORD" | jq -r '.result.content')
    TTL=$(echo "$RECORD" | jq -r '.result.ttl')
    CURRENT_PROXY=$(echo "$RECORD" | jq -r '.result.proxied')

    if [ "$OLD_IP" = "$IP" ] && [ "$CURRENT_PROXY" = "$PROXIED" ]; then
      echo "[$(date '+%F %T')] $NAME: IP und Proxy unverändert"
      return
    fi

    echo "[$(date '+%F %T')] $NAME: $OLD_IP -> $IP"

    echo "[$(date '+%F %T')] $NAME"
    echo "    Alte IP      : $OLD_IP"
    echo "    Neue IP      : $IP"
    echo "    Alter Proxy  : $CURRENT_PROXY"
    echo "    Neuer Proxy  : $PROXIED"

    if [ "$DRYRUN" = true ]; then
      echo "🔍 Dry-Run: Kein Update durchgeführt."
      return
    fi

    RESPONSE=$(curl --retry 3 --retry-delay 2 --fail-with-body -sS --connect-timeout 5 --max-time 10 -X PATCH \
      "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records/$RECORD_ID" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      --data "{
        \"type\":\"A\",
        \"name\":\"$NAME\",
        \"content\":\"$IP\",
        \"ttl\":$TTL,
        \"proxied\":$PROXIED
      }")

SUCCESS=$(echo "$RESPONSE" | jq -r '.success')

MESSAGES=$(echo "$RESPONSE" | jq -r '.messages[]?.message')

if [ -n "$MESSAGES" ]; then
    echo "ℹ️ Cloudflare: $MESSAGES"
fi

if [ "$SUCCESS" != "true" ]; then
    echo "❌ Cloudflare-Fehler bei $NAME"
    echo "$RESPONSE" | jq .
    exit 1
fi

# Danach verifizieren
NEW_RECORD=$(curl \
    --retry 3 --retry-delay 2 \
    --fail-with-body -sS \
    --connect-timeout 5 --max-time 10 \
    -H "Authorization: Bearer $TOKEN" \
    "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records/$RECORD_ID")

NEW_IP=$(echo "$NEW_RECORD" | jq -r '.result.content')

if [ "$NEW_IP" != "$IP" ]; then
    echo "❌ Verifikation fehlgeschlagen!"
    echo "Cloudflare meldet: $NEW_IP"
    echo "Erwartet         : $IP"
    exit 1
fi

NEW_PROXY=$(echo "$NEW_RECORD" | jq -r '.result.proxied')

if [ "$NEW_IP" != "$IP" ] || [ "$NEW_PROXY" != "$PROXIED" ]; then
    echo "❌ Verifikation fehlgeschlagen!"
    echo "Cloudflare IP    : $NEW_IP"
    echo "Erwartete IP     : $IP"
    echo "Cloudflare Proxy : $NEW_PROXY"
    echo "Erwarteter Proxy : $PROXIED"
    exit 1
fi

    echo "✅ $NAME erfolgreich aktualisiert."
}

update_record "${RECORDS[kc]}" "${PROXY[kc]}"
update_record "${RECORDS[auth]}" "${PROXY[auth]}"

echo "[$(date '+%F %T')] Cloudflare-DDNS erfolgreich beendet."
exit 0
