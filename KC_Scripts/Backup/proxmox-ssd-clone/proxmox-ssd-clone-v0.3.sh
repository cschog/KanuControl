#!/usr/bin/env bash

set -Eeuo pipefail

##############################################################################
# Proxmox SSD Clone
# Version 0.3
#
# Diese Version führt KEINE Änderungen am System durch.
# Sie prüft lediglich:
#   - Quell-SSD
#   - Ziel-SSD
#   - laufende VMs
##############################################################################

VERSION="0.3"

##############################################################################
# Konfiguration
##############################################################################

SOURCE_SERIAL="200644800702"
TARGET_SERIAL="195223801315"

LOGFILE="/var/log/proxmox-ssd-clone.log"
LOCKFILE="/var/lock/proxmox-ssd-clone.lock"

##############################################################################
# Globale Variablen
##############################################################################

SOURCE_DEVICE=""
SOURCE_MODEL=""
SOURCE_SERIAL_FOUND=""

TARGET_DEVICE=""
TARGET_MODEL=""
TARGET_SERIAL_FOUND=""

RUNNING_VMS=()

##############################################################################
# Lockfile
##############################################################################

exec 9>"$LOCKFILE"

if ! flock -n 9; then
    echo "Das Skript läuft bereits."
    exit 1
fi

##############################################################################
# Logging
##############################################################################

log() {
    echo "$(date '+%F %T')  $*" | tee -a "$LOGFILE"
}

step() {
    echo
    echo "--------------------------------------------------"
    echo "$1"
    echo "--------------------------------------------------"
}

die() {
    echo
    echo "FEHLER: $1"
    log "FEHLER: $1"
    exit 1
}

##############################################################################
# SSD-Funktionen
##############################################################################

find_disk() {

    local search_serial="$1"
    local line
    local name
    local model
    local serial

    while IFS= read -r line
    do
        name=$(sed -n 's/.*NAME="\([^"]*\)".*/\1/p' <<<"$line")
        model=$(sed -n 's/.*MODEL="\([^"]*\)".*/\1/p' <<<"$line")
        serial=$(sed -n 's/.*SERIAL="\([^"]*\)".*/\1/p' <<<"$line")

        if [[ "$serial" == "$search_serial" ]]; then
            printf "%s|%s|%s\n" "$name" "$model" "$serial"
            return 0
        fi
    done < <(lsblk -d -P -o NAME,MODEL,SERIAL)

    return 1
}

##############################################################################
# VM-Funktionen
##############################################################################

get_running_vms() {

    RUNNING_VMS=()

    while read -r vmid
    do
        [[ -z "$vmid" ]] && continue

        if [[ "$(qm status "$vmid")" == "status: running" ]]; then
            RUNNING_VMS+=("$vmid")
        fi

    done < <(qm list | awk 'NR>1 {print $1}')
}

##############################################################################
# Hauptprogramm
##############################################################################

clear

echo
echo "=================================================="
echo " PROXMOX SSD CLONE"
echo " Version $VERSION"
echo "=================================================="

log "Programm gestartet."

step "Suche Quell-SSD"

SOURCE_INFO=$(find_disk "$SOURCE_SERIAL") || die "Quell-SSD nicht gefunden."

IFS="|" read -r SOURCE_DEVICE SOURCE_MODEL SOURCE_SERIAL_FOUND <<<"$SOURCE_INFO"

echo "Gerät : /dev/$SOURCE_DEVICE"
echo "Modell: $SOURCE_MODEL"
echo "Serial: $SOURCE_SERIAL_FOUND"

step "Suche Ziel-SSD"

TARGET_INFO=$(find_disk "$TARGET_SERIAL") || die "Ziel-SSD nicht gefunden."

IFS="|" read -r TARGET_DEVICE TARGET_MODEL TARGET_SERIAL_FOUND <<<"$TARGET_INFO"

echo "Gerät : /dev/$TARGET_DEVICE"
echo "Modell: $TARGET_MODEL"
echo "Serial: $TARGET_SERIAL_FOUND"

[[ "$SOURCE_DEVICE" != "$TARGET_DEVICE" ]] || die "Quelle und Ziel sind identisch."

step "Laufende VMs"

get_running_vms

if ((${#RUNNING_VMS[@]}==0)); then
    echo "Keine laufenden VMs gefunden."
else
    for vmid in "${RUNNING_VMS[@]}"; do
        name=$(qm config "$vmid" | awk -F': ' '/^name:/ {print $2}')
        printf "  %-4s %s\n" "$vmid" "$name"
    done
fi

echo
echo "Anzahl laufender VMs: ${#RUNNING_VMS[@]}"

echo
echo "=================================================="
echo " V0.3 erfolgreich abgeschlossen"
echo " Es wurden keine Änderungen am System vorgenommen."
echo "=================================================="

log "Version $VERSION erfolgreich beendet."
