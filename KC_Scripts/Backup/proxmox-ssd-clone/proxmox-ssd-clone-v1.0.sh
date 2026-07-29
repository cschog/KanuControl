#!/usr/bin/env bash
set -Eeuo pipefail

###############################################################################
# Proxmox SSD Clone
# Version 1.0
#
# Features
#  - Root-, Lock- und Fehlerbehandlung
#  - SSD-Erkennung über Seriennummer
#  - Größen-/Modellanzeige
#  - Sicherheitsabfrage (YES)
#  - Laufende VMs merken
#  - VMs sauber herunterfahren
#  - Auf Shutdown warten
#  - SSD klonen (dd + status=progress)
#  - sync / Flush
#  - Nur vorher laufende VMs starten
#  - Abschlussbericht
###############################################################################

VERSION="1.0"

SOURCE_SERIAL="200644800702"
TARGET_SERIAL="195223801315"

LOGFILE="/var/log/proxmox-ssd-clone.log"
LOCKFILE="/var/lock/proxmox-ssd-clone.lock"

SHUTDOWN_TIMEOUT=300
POLL_INTERVAL=5

declare -A VM_NAMES
RUNNING_VM_IDS=()

START_TS=$(date +%s)

green="\033[1;32m"
yellow="\033[1;33m"
red="\033[1;31m"
blue="\033[1;34m"
reset="\033[0m"

log(){ echo "$(date '+%F %T') $*" | tee -a "$LOGFILE"; }
die(){ echo -e "${red}FEHLER:${reset} $1"; log "FEHLER: $1"; exit 1; }

cleanup(){
 rc=$?
 [[ $rc -eq 0 ]] && log "Programm erfolgreich beendet." || log "Programm mit Exitcode $rc beendet."
}
trap cleanup EXIT

[[ $EUID -eq 0 ]] || die "Bitte als root starten."

exec 9>"$LOCKFILE"
flock -n 9 || die "Das Skript läuft bereits."

banner(){
clear
echo -e "${blue}"
cat <<EOF
============================================================
                 PROXMOX SSD CLONE
                     Version $VERSION
============================================================
EOF
echo -e "${reset}"
}

step(){
echo
echo "------------------------------------------------------------"
echo "$1"
echo "------------------------------------------------------------"
}

find_disk(){
 local serial="$1"
 lsblk -bdP -o NAME,MODEL,SERIAL,SIZE | while read -r line; do
   eval "$line"
   [[ "$SERIAL" == "$serial" ]] && {
      echo "$NAME|$MODEL|$SERIAL|$SIZE"
      return
   }
 done
}

human(){
 numfmt --to=iec-i --suffix=B "$1"
}

get_running_vms(){
 while read -r vmid; do
   [[ -z "$vmid" ]] && continue
   if [[ "$(qm status "$vmid")" == "status: running" ]]; then
      RUNNING_VM_IDS+=("$vmid")
      VM_NAMES[$vmid]="$(qm config "$vmid" | awk -F': ' '/^name:/ {print $2}')"
   fi
 done < <(qm list | awk 'NR>1{print $1}')
}

shutdown_vms(){
 for id in "${RUNNING_VM_IDS[@]}"; do
   echo "Stoppe VM $id (${VM_NAMES[$id]})"
   qm shutdown "$id"
 done
}

wait_shutdown(){
 local t=0
 while :; do
   local active=0
   for id in "${RUNNING_VM_IDS[@]}"; do
      [[ "$(qm status "$id")" == "status: running" ]] && ((active++))
   done
   ((active==0)) && return 0
   ((t>=SHUTDOWN_TIMEOUT)) && return 1
   printf "\rNoch %d VM(s) aktiv... %3d/%d s" "$active" "$t" "$SHUTDOWN_TIMEOUT"
   sleep "$POLL_INTERVAL"
   t=$((t+POLL_INTERVAL))
 done
}

start_vms(){
 for id in "${RUNNING_VM_IDS[@]}"; do
   echo "Starte VM $id (${VM_NAMES[$id]})"
   qm start "$id"
 done
}

banner
log "Programm gestartet"

IFS="|" read -r SRC_DEV SRC_MODEL SRC_SER SRC_SIZE <<<"$(find_disk "$SOURCE_SERIAL")"
IFS="|" read -r DST_DEV DST_MODEL DST_SER DST_SIZE <<<"$(find_disk "$TARGET_SERIAL")"

[[ -n "${SRC_DEV:-}" ]] || die "Quell-SSD nicht gefunden."
[[ -n "${DST_DEV:-}" ]] || die "Ziel-SSD nicht gefunden."
[[ "$SRC_DEV" != "$DST_DEV" ]] || die "Quelle und Ziel identisch."
[[ "$SRC_SIZE" == "$DST_SIZE" ]] || die "SSDs unterschiedlich groß."

findmnt -rn -S "/dev/$DST_DEV" >/dev/null && die "Ziel-SSD ist eingehängt."

step "SSD-Informationen"

printf "Quelle : /dev/%s\n" "$SRC_DEV"
printf "Modell : %s\n" "$SRC_MODEL"
printf "Größe  : %s\n\n" "$(human "$SRC_SIZE")"

printf "Ziel   : /dev/%s\n" "$DST_DEV"
printf "Modell : %s\n" "$DST_MODEL"
printf "Größe  : %s\n" "$(human "$DST_SIZE")"

step "Laufende VMs"

get_running_vms

if ((${#RUNNING_VM_IDS[@]}==0)); then
 echo "Keine laufenden VMs."
else
 for id in "${RUNNING_VM_IDS[@]}"; do
   printf " %4s  %s\n" "$id" "${VM_NAMES[$id]}"
 done
fi

echo
echo -e "${yellow}ACHTUNG!${reset}"
echo "Alle Daten auf /dev/$DST_DEV werden gelöscht."
read -rp "Zum Fortfahren bitte YES eingeben: " ans
[[ "$ans" == "YES" ]] || die "Abgebrochen."

step "VMs herunterfahren"
shutdown_vms

step "Warte auf Shutdown"
wait_shutdown || die "Timeout beim Herunterfahren."

echo
step "SSD wird geklont"

sync
log "Starte dd"

dd if="/dev/$SRC_DEV" \
   of="/dev/$DST_DEV" \
   bs=64M \
   conv=fsync,noerror \
   status=progress

sync
blockdev --flushbufs "/dev/$DST_DEV"

log "Klonen abgeschlossen"

step "VMs starten"
start_vms

END_TS=$(date +%s)
RUNTIME=$((END_TS-START_TS))
H=$((RUNTIME/3600))
M=$(((RUNTIME%3600)/60))
S=$((RUNTIME%60))

echo
echo -e "${green}"
cat <<EOF
============================================================
                     FERTIG
============================================================

Version        : $VERSION
Quelle         : /dev/$SRC_DEV
Ziel           : /dev/$DST_DEV
VMs gestartet  : ${#RUNNING_VM_IDS[@]}
Laufzeit       : ${H}h ${M}m ${S}s

Klonen erfolgreich abgeschlossen.
============================================================
EOF
echo -e "${reset}"
