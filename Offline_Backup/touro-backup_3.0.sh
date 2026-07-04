#!/usr/bin/env bash
set -Eeuo pipefail

# Touro Offline Backup 3.0
# Incremental snapshots using rsync --link-dest

SOURCE="/mnt/mybook8tb/KC_Backup"
TARGET_MOUNT="/mnt/touro4tb"
BACKUP_ROOT="$TARGET_MOUNT/KC_Backup"
LOGFILE="$SOURCE/logs/touro-backup.log"

MAX_BACKUPS=12
RESERVE_GB=20

log() {
  echo "$(date '+%F %T') $*" | tee -a "$LOGFILE"
}

cleanup() {
  if mountpoint -q "$TARGET_MOUNT"; then
    sync
    umount "$TARGET_MOUNT"
  fi
}
trap cleanup EXIT

[[ -d "$SOURCE" ]] || { echo "Source not found"; exit 1; }

mount "$TARGET_MOUNT"

mkdir -p "$BACKUP_ROOT"

BACKUP_NAME=$(date +"%Y-%m-%d_%H-%M-%S")
BACKUP_DIR="$BACKUP_ROOT/$BACKUP_NAME"

# keep only newest MAX_BACKUPS-1 before creating the new one
while [ "$(find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d | wc -l)" -ge "$MAX_BACKUPS" ]; do
  OLDEST=$(find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d | sort | head -1)
  [[ -n "$OLDEST" ]] || break
  [[ "$OLDEST" == "$BACKUP_ROOT/"* ]] || { echo "Safety check failed"; exit 1; }
  log "Removing old backup $(basename "$OLDEST")"
  rm -rf -- "$OLDEST"
done

LATEST=$(find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d | sort | tail -1 || true)

LINKDEST=""
if [[ -n "${LATEST:-}" ]]; then
  LINKDEST="--link-dest=$LATEST"
fi

mkdir -p "$BACKUP_DIR"

log "Backup started"
echo "Source : $SOURCE"
echo "Target : $BACKUP_DIR"
echo

while true; do
  set +e
  rsync -a --delete \
    --human-readable \
    --stats \
    --info=progress2 \
    $LINKDEST \
    "$SOURCE/" "$BACKUP_DIR/" | tee -a "$LOGFILE"
  RC=${PIPESTATUS[0]}
  set -e

  if [ "$RC" -eq 0 ]; then
    break
  fi

  if [ "$RC" -eq 11 ] || [ "$RC" -eq 28 ]; then
    OLDEST=$(find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d ! -path "$BACKUP_DIR" | sort | head -1)
    if [[ -z "$OLDEST" ]]; then
      echo "Disk full and nothing left to delete."
      exit 1
    fi
    log "Disk full. Removing $(basename "$OLDEST") and retrying."
    rm -rf -- "$OLDEST"
    continue
  fi

  exit "$RC"
done

FREE=$(df -h "$TARGET_MOUNT" | awk 'NR==2{print $4}')
log "Backup finished. Free space: $FREE"
echo "Backup completed successfully."
