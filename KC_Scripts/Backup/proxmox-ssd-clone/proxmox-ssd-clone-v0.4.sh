#!/usr/bin/env bash
set -Eeuo pipefail

##############################################################################
# Proxmox SSD Clone
# Version 0.4
#
# V0.4:
#   - SSDs erkennen
#   - Laufende VMs ermitteln
#   - VMs sauber herunterfahren
#   - Auf Shutdown warten
#   - KEIN Klonen, KEIN Neustart der VMs
##############################################################################

VERSION="0.4"

SOURCE_SERIAL="200644800702"
TARGET_SERIAL="195223801315"

LOGFILE="/var/log/proxmox-ssd-clone.log"
LOCKFILE="/var/lock/proxmox-ssd-clone.lock"

SHUTDOWN_TIMEOUT=300
POLL_INTERVAL=5

SOURCE_DEVICE=""
TARGET_DEVICE=""
RUNNING_VM_IDS=()
RUNNING_VM_NAMES=()

exec 9>"$LOCKFILE"
flock -n 9 || { echo "Das Skript läuft bereits."; exit 1; }

log() {
    echo "$(date '+%F %T')  $*" | tee -a "$LOGFILE"
}

step() {
    echo
    echo "=================================================="
    echo "$1"
    echo "=================================================="
}

die() {
    echo
    echo "FEHLER: $1"
    log "FEHLER: $1"
    exit 1
}

find_disk() {
    local search="$1"
    local line name model serial

    while IFS= read -r line; do
        name=$(sed -n 's/.*NAME="\([^"]*\)".*/\1/p' <<<"$line")
        model=$(sed -n 's/.*MODEL="\([^"]*\)".*/\1/p' <<<"$line")
        serial=$(sed -n 's/.*SERIAL="\([^"]*\)".*/\1/p' <<<"$line")

        if [[ "$serial" == "$search" ]]; then
            printf "%s|%s|%s\n" "$name" "$model" "$serial"
            return 0
        fi
    done < <(lsblk -d -P -o NAME,MODEL,SERIAL)

    return 1
}

get_running_vms() {
    RUNNING_VM_IDS=()
    RUNNING_VM_NAMES=()

    while read -r vmid; do
        [[ -z "$vmid" ]] && continue
        if [[ "$(qm status "$vmid")" == "status: running" ]]; then
            RUNNING_VM_IDS+=("$vmid")
            RUNNING_VM_NAMES+=("$(qm config "$vmid" | awk -F': ' '/^name:/ {print $2}')")
        fi
    done < <(qm list | awk 'NR>1 {print $1}')
}

shutdown_running_vms() {
    local i
    for ((i=0;i<${#RUNNING_VM_IDS[@]};i++)); do
        printf "Stoppe VM %-4s %s\n" "${RUNNING_VM_IDS[$i]}" "${RUNNING_VM_NAMES[$i]}"
        qm shutdown "${RUNNING_VM_IDS[$i]}"
    done
}

wait_for_shutdown() {
    local elapsed=0
    local running

    while true; do
        running=0

        for vmid in "${RUNNING_VM_IDS[@]}"; do
            if [[ "$(qm status "$vmid")" == "status: running" ]]; then
                ((running++))
            fi
        done

        if (( running == 0 )); then
            echo
            echo "Alle VMs wurden beendet."
            return 0
        fi

        if (( elapsed >= SHUTDOWN_TIMEOUT )); then
            echo
            echo "Folgende VMs laufen noch:"
            for ((i=0;i<${#RUNNING_VM_IDS[@]};i++)); do
                if [[ "$(qm status "${RUNNING_VM_IDS[$i]}")" == "status: running" ]]; then
                    printf "  %-4s %s\n" "${RUNNING_VM_IDS[$i]}" "${RUNNING_VM_NAMES[$i]}"
                fi
            done
            return 1
        fi

        printf "\rNoch %d VM(s) aktiv... (%d/%d s)" "$running" "$elapsed" "$SHUTDOWN_TIMEOUT"
        sleep "$POLL_INTERVAL"
        elapsed=$((elapsed + POLL_INTERVAL))
    done
}

clear
echo
echo "=================================================="
echo " PROXMOX SSD CLONE"
echo " Version $VERSION"
echo "=================================================="

log "Programm gestartet."

step "SSD-Prüfung"

IFS="|" read -r SOURCE_DEVICE _ _ <<<"$(find_disk "$SOURCE_SERIAL")" || die "Quell-SSD nicht gefunden."
IFS="|" read -r TARGET_DEVICE _ _ <<<"$(find_disk "$TARGET_SERIAL")" || die "Ziel-SSD nicht gefunden."

echo "Quelle : /dev/$SOURCE_DEVICE"
echo "Ziel   : /dev/$TARGET_DEVICE"

[[ "$SOURCE_DEVICE" != "$TARGET_DEVICE" ]] || die "Quelle und Ziel identisch."

step "Laufende VMs"

get_running_vms

if ((${#RUNNING_VM_IDS[@]}==0)); then
    echo "Keine laufenden VMs gefunden."
    log "Keine laufenden VMs."
    exit 0
fi

for ((i=0;i<${#RUNNING_VM_IDS[@]};i++)); do
    printf "  %-4s %s\n" "${RUNNING_VM_IDS[$i]}" "${RUNNING_VM_NAMES[$i]}"
done

echo
echo "Anzahl: ${#RUNNING_VM_IDS[@]}"

step "VMs herunterfahren"

shutdown_running_vms

step "Warte auf Shutdown"

if wait_for_shutdown; then
    echo
    echo "=================================================="
    echo " Version $VERSION erfolgreich"
    echo " Alle VMs wurden beendet."
    echo " Es wurde noch NICHT geklont."
    echo "=================================================="
    log "Alle VMs heruntergefahren."
else
    die "Timeout beim Herunterfahren der VMs."
fi
