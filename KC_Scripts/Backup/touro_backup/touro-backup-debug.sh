#!/usr/bin/env bash

set -Eeuo pipefail
trap 'rc=$?; log "FEHLER in Zeile ${LINENO}: ${BASH_COMMAND} (Exit-Code: ${rc})"; exit $rc' ERR

##############################################################################
# Konfiguration
##############################################################################

HA_URL="http://192.168.100.10:8123"
HA_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkN2QwZTY0NDQ0Nzg0M2U0YmNmMzBiYWZlNmQxODAzZCIsImlhdCI6MTc4NTI2MzQwNywiZXhwIjoyMTAwNjIzNDA3fQ.CZL2geZpq26ret0ri6ho2s-_y2EHp6tE7luTncuUrdg"

HA_STATUS_ENTITY="input_text.backup_status"

SOURCE="/mnt/mybook8tb/KC_Backup"
TARGET="/mnt/touro4tb"
BACKUP_ROOT="$TARGET/KC_Backup"
LOGFILE="$SOURCE/logs/touro-backup.log"
STATUSFILE="$SOURCE/logs/touro-backup.status"

MAX_BACKUPS=12
SUCCESS=false

##############################################################################
# Lockfile
##############################################################################

exec 9>/var/lock/touro-backup.lock

if ! flock -n 9; then
    echo "Ein Backup läuft bereits."
    exit 1
fi

##############################################################################
# Logging
##############################################################################

log() {
    echo "$(date '+%F %T')  $*" | tee -a "$LOGFILE"
}

ha_status() {
    local STATUS="$1"

    curl -s \
      -X POST \
      -H "Authorization: Bearer $HA_TOKEN" \
      -H "Content-Type: application/json" \
      "$HA_URL/api/services/input_text/set_value" \
      -d "{
            \"entity_id\":\"$HA_STATUS_ENTITY\",
            \"value\":\"$STATUS\"
          }" >/dev/null
}

##############################################################################
# Cleanup
##############################################################################

cleanup() {

    if mountpoint -q "$TARGET"; then
        log "Synchronisiere Dateisystem..."
        sync

        log "Hänge Touro aus..."
        while mountpoint -q "$TARGET"; do
            umount "$TARGET"
        done
    fi

    if $SUCCESS; then
        echo
        echo "=========================================="
        echo " Backup erfolgreich abgeschlossen"
        echo "=========================================="
        echo
        echo "Laufzeit         : $(printf "%02d:%02d:%02d" \
            $((SECONDS/3600)) \
            $(((SECONDS%3600)/60)) \
            $((SECONDS%60)))"
        echo "Backups          : $BACKUPS / $MAX_BACKUPS"
        echo "Freier Speicher  : $FREE"
        echo

        ha_status "OK"
        log "Backup erfolgreich abgeschlossen."
        echo "OK $(date '+%F %T')" > "$STATUSFILE"
    else
        ha_status "ERROR"
        log "Backup fehlgeschlagen."
        echo "ERROR $(date '+%F %T')" > "$STATUSFILE"
    fi
}

trap cleanup EXIT

##############################################################################
# Prüfungen
##############################################################################

[[ -d "$SOURCE" ]] || { log "Quelle existiert nicht: $SOURCE"; exit 1; }

TOURO_UUID="3a5843a9-055d-482d-b65d-96e9adbcadc1"

if ! blkid | grep -q "$TOURO_UUID"; then
    log "FEHLER: Touro-Festplatte nicht gefunden."
    ha_status "ERROR: Touro nicht gefunden"
    exit 1
fi

if ! mountpoint -q "$TARGET"; then
    mount "$TARGET"
fi

if ! mountpoint -q "$TARGET"; then
    log "FEHLER: Touro konnte nicht gemountet werden."
    ha_status "ERROR: Mount fehlgeschlagen"
    exit 1
fi

[[ -d "$BACKUP_ROOT" ]] || {
    log "FEHLER: Backup-Verzeichnis fehlt auf der Touro."
    exit 1
}

find "$BACKUP_ROOT" -maxdepth 1 -type d -name "*.incomplete" -exec rm -rf {} +

STAMP=$(date +"%Y-%m-%d_%H-%M-%S")
TMP="$BACKUP_ROOT/${STAMP}.incomplete"
DEST="$BACKUP_ROOT/${STAMP}"
mkdir "$TMP"

echo
echo "=========================================="
echo " TOURO OFFLINE BACKUP: 1.1"
echo "=========================================="
echo

echo "Quelle : $SOURCE"
echo "Ziel   : $DEST"
echo

log "Backup gestartet."
echo "RUNNING $(date '+%F %T')" > "$STATUSFILE"
ha_status "RUNNING"

START=$(date +%s)

log "Starte rsync..."

rsync \
    -a \
    --human-readable \
    --stats \
    --info=progress2 \
    "$SOURCE/" \
    "$TMP/" 2>&1 | tee -a "$LOGFILE"

log "rsync beendet."

log "Benenne Snapshot um..."
mv "$TMP" "$DEST"
log "Snapshot erfolgreich umbenannt."

log "Ermittle Anzahl vorhandener Backups..."

COUNT=$(find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d | wc -l)

log "Es wurden $COUNT Backups gefunden."

if (( COUNT > MAX_BACKUPS )); then
    REMOVE=$((COUNT-MAX_BACKUPS))
    log "Es werden $REMOVE alte Backups gelöscht."

    find "$BACKUP_ROOT" \
        -mindepth 1 \
        -maxdepth 1 \
        -type d \
        -name "20??-??-??_*" |
    sort |
    head -n "$REMOVE" |
    while read -r DIR; do
        [[ "$DIR" == "$BACKUP_ROOT/"* ]] || {
            log "FEHLER: Ungültiger Pfad $DIR"
            exit 1
        }

        log "Lösche $(basename "$DIR")"
        rm -rf -- "$DIR"
    done

    log "Alte Backups gelöscht."
else
    log "Es müssen keine alten Backups gelöscht werden."
fi

log "Berechne Statistik..."

END=$(date +%s)
SECONDS=$((END-START))

log "Ermittle freien Speicher..."
FREE=$(df -h "$TARGET" | awk 'NR==2 {print $4}')

log "Ermittle Backup-Anzahl..."
BACKUPS=$(find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d -name "20??-??-??_*" | wc -l)

log "Backups vorhanden: $BACKUPS"

log "Setze SUCCESS=true"
SUCCESS=true

log "Backup-Skript erfolgreich beendet."
