#!/bin/bash
#
# /root/scripts/backup-proxmox-config.sh
#
set -euo pipefail

BACKUP_DIR="/mnt/mybook8tb/KC_Backup/configs/proxmox/daily"
DATE=$(date +%F)

TMP_DIR=$(mktemp -d)

echo "Erstelle Metadaten..."

mkdir -p "$TMP_DIR/root/backup-meta"

# Paketliste
dpkg --get-selections > "$TMP_DIR/root/backup-meta/packages.txt"

# Aktivierte Dienste
systemctl list-unit-files --state=enabled \
  > "$TMP_DIR/root/backup-meta/enabled-services.txt"

# Root-Crontab
crontab -l > "$TMP_DIR/root/backup-meta/root-crontab.txt" 2>/dev/null || true

# Installierte Kernel
dpkg -l | grep '^ii' | grep pve-kernel \
  > "$TMP_DIR/root/backup-meta/pve-kernels.txt"

echo "Kopiere Konfigurationsdateien..."

mkdir -p "$TMP_DIR/etc"

cp -a /etc/pve "$TMP_DIR/etc/" 2>/dev/null || true
cp -a /etc/network "$TMP_DIR/etc/" 2>/dev/null || true
cp -a /etc/hosts "$TMP_DIR/etc/" 2>/dev/null || true
cp -a /etc/fstab "$TMP_DIR/etc/" 2>/dev/null || true
cp -a /etc/exports "$TMP_DIR/etc/" 2>/dev/null || true
cp -a /etc/cron.d "$TMP_DIR/etc/" 2>/dev/null || true
cp -a /etc/cron.daily "$TMP_DIR/etc/" 2>/dev/null || true
cp -a /etc/cron.weekly "$TMP_DIR/etc/" 2>/dev/null || true
cp -a /etc/cron.monthly "$TMP_DIR/etc/" 2>/dev/null || true
cp -a /etc/systemd/system "$TMP_DIR/etc/systemd/" 2>/dev/null || true

# Eigene Skripte
mkdir -p "$TMP_DIR/root"
cp -a /root/*.sh "$TMP_DIR/root/" 2>/dev/null || true

echo "Erstelle Archiv..."

tar -czf \
  "$BACKUP_DIR/proxmox-host-config-$DATE.tar.gz" \
  -C "$TMP_DIR" .

rm -rf "$TMP_DIR"

echo "Backup erstellt:"
ls -lh "$BACKUP_DIR/proxmox-host-config-$DATE.tar.gz"