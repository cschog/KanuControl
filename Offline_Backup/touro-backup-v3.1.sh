#!/usr/bin/env bash
set -Eeuo pipefail

# Touro Backup v3.1

SOURCE="/mnt/mybook8tb/KC_Backup"
TARGET="/mnt/touro4tb"
BACKUP_ROOT="$TARGET/KC_Backup"
LOGFILE="$SOURCE/logs/touro-backup.log"

MAX_BACKUPS=12
RESERVE_BYTES=$((20*1024*1024*1024))

log(){ echo "$(date '+%F %T') $*" | tee -a "$LOGFILE"; }

cleanup(){
  if mountpoint -q "$TARGET"; then
    sync
    umount "$TARGET"
  fi
}
trap cleanup EXIT

fail(){ log "ERROR: $*"; exit 1; }

[[ -d "$SOURCE" ]] || fail "Source not found: $SOURCE"

mountpoint -q "$TARGET" || mount "$TARGET"
[[ -d "$BACKUP_ROOT" ]] || mkdir -p "$BACKUP_ROOT"

SNAPSHOT=$(date +"%Y-%m-%d_%H-%M-%S")
DEST="$BACKUP_ROOT/$SNAPSHOT"

count_backups(){ find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d | wc -l; }

oldest_backup(){ find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d | sort | head -1; }

latest_backup(){ find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d | sort | tail -1; }

while [ "$(count_backups)" -ge "$MAX_BACKUPS" ]; do
  O=$(oldest_backup)
  [[ -n "$O" ]] || break
  [[ "$O" == "$BACKUP_ROOT/"* ]] || fail "Safety check failed"
  log "Removing old backup $(basename "$O")"
  rm -rf -- "$O"
done

SRC_SIZE=$(du -sb "$SOURCE" | awk '{print $1}')
NEEDED=$((SRC_SIZE+RESERVE_BYTES))

while :; do
  FREE=$(df --output=avail -B1 "$TARGET" | tail -1)
  [ "$FREE" -ge "$NEEDED" ] && break
  O=$(oldest_backup)
  [[ -n "$O" ]] || fail "Not enough free space."
  [[ "$O" != "$DEST" ]] || fail "Refusing to delete current backup."
  log "Freeing space by removing $(basename "$O")"
  rm -rf -- "$O"
done

mkdir -p "$DEST"

LATEST=$(latest_backup)
LINK=()
if [[ -n "${LATEST:-}" && "$LATEST" != "$DEST" ]]; then
  LINK=(--link-dest="$LATEST")
fi

echo "====================================================="
echo "        TOURO OFFLINE BACKUP v3.1"
echo "====================================================="
echo "Source : $SOURCE"
echo "Target : $DEST"
echo

START=$(date +%s)

rsync -a \
  --delete \
  --human-readable \
  --stats \
  --info=progress2 \
  "${LINK[@]}" \
  "$SOURCE/" \
  "$DEST/" | tee -a "$LOGFILE"

END=$(date +%s)
DUR=$((END-START))
FREE=$(df -h "$TARGET" | awk 'NR==2{print $4}')

log "Backup finished in $(printf '%02d:%02d:%02d' $((DUR/3600)) $(((DUR%3600)/60)) $((DUR%60)))"
log "Free space remaining: $FREE"
echo
echo "Backup completed successfully."
