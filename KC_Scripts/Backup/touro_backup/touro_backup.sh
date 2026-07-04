#!/usr/bin/env bash
#
# /root/scripts/touro-backup.sh
#

set -Eeuo pipefail

##############################################################################
# Konfiguration
##############################################################################

SOURCE="/mnt/mybook8tb/KC_Backup"
TARGET="/mnt/touro4tb"
BACKUP_ROOT="$TARGET/KC_Backup"
LOGFILE="$SOURCE/logs/touro-backup.log"

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
    log "Backup erfolgreich abgeschlossen."
else
    echo
    echo "=========================================="
    echo " Backup FEHLGESCHLAGEN"
    echo "=========================================="
    echo
    log "Backup fehlgeschlagen."
fi
}


trap cleanup EXIT

##############################################################################
# Prüfungen
##############################################################################

[[ -d "$SOURCE" ]] || {
    log "Quelle existiert nicht: $SOURCE"
    exit 1
}

if ! mountpoint -q "$TARGET"; then
    mount "$TARGET"
fi

mkdir -p "$BACKUP_ROOT"

##############################################################################
# Unvollständige Backups entfernen
##############################################################################

find "$BACKUP_ROOT" \
    -maxdepth 1 \
    -type d \
    -name "*.incomplete" \
    -exec rm -rf {} +

##############################################################################
# Snapshotname
##############################################################################

STAMP=$(date +"%Y-%m-%d_%H-%M-%S")

TMP="$BACKUP_ROOT/${STAMP}.incomplete"
DEST="$BACKUP_ROOT/${STAMP}"

mkdir "$TMP"

##############################################################################
# Backup
##############################################################################

echo
echo "=========================================="
echo " TOURO OFFLINE BACKUP"
echo "=========================================="
echo

echo "Quelle : $SOURCE"
echo "Ziel   : $DEST"
echo

log "Backup gestartet."

START=$(date +%s)

rsync \
    -a \
    --human-readable \
    --stats \
    --info=progress2 \
    "$SOURCE/" \
    "$TMP/" \
| tee -a "$LOGFILE"

##############################################################################
# Snapshot abschließen
##############################################################################

mv "$TMP" "$DEST"

##############################################################################
# Alte Backups löschen
##############################################################################

COUNT=$(
find "$BACKUP_ROOT" \
    -mindepth 1 \
    -maxdepth 1 \
    -type d \
| wc -l
)

if (( COUNT > MAX_BACKUPS ))
then

    REMOVE=$((COUNT-MAX_BACKUPS))

    find "$BACKUP_ROOT" \
        -mindepth 1 \
        -maxdepth 1 \
        -type d \
        -name "20??-??-??_*" \
    | sort \
    | head -n "$REMOVE" \
    | while read -r DIR
      do
          [[ "$DIR" == "$BACKUP_ROOT/"* ]] || {
              log "FEHLER: Ungültiger Pfad $DIR"
              exit 1
           }

           log "Lösche $(basename "$DIR")"
            rm -rf -- "$DIR"
       done
fi

##############################################################################
# Statistik
##############################################################################

END=$(date +%s)

SECONDS=$((END-START))

FREE=$(df -h "$TARGET" | awk 'NR==2 {print $4}')

BACKUPS=$(find "$BACKUP_ROOT" \
    -mindepth 1 \
    -maxdepth 1 \
    -type d \
    -name "20??-??-??_*" \
| wc -l)

SUCCESS=true
