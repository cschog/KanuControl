# KanuControl – Backup Architektur (Produktivumgebung)

## Produktionssysteme

| IP             | System        | Funktion                     |
| -------------- | ------------- | ---------------------------- |
| 192.168.100.31 | postgres-prod | PostgreSQL Datenbank         |
| 192.168.100.32 | kcserver-prod | Spring Boot KC Server        |
| 192.168.100.33 | keycloak-prod | Keycloak Identity Server     |
| 192.168.100.34 | edge-prod     | Reverse Proxy / Edge / HTTPS |

---

# Ziel der Backup-Strategie

Die Backup-Architektur soll:

* Datenverlust minimieren
* schnelle Wiederherstellung ermöglichen
* einfache Administration erlauben
* DSGVO-konform nachvollziehbar sein
* auch bei Hardware-Ausfall des Dell funktionieren

---

# Grundprinzip

Nicht nur die Datenbank sichern.

Es werden drei Ebenen abgesichert:

1. PostgreSQL Daten
2. VM-/Systemzustände
3. Konfigurationen und Secrets

---

# Empfohlene Backup-Architektur

## Ebene 1 – PostgreSQL Dumps (wichtigste Ebene)

System:

* postgres-prod (192.168.100.31)

Backup-Typ:

* täglicher pg_dump
* zusätzlich regelmäßiger Full Dump

Empfehlung:

### Täglich

* Zeit: nachts (z. B. 02:00)
* Format:

```bash
pg_dump -Fc kanucontrol > kanucontrol_YYYYMMDD.dump
```

Vorteile:

* konsistent
* schnell
* einzelne DB wiederherstellbar
* unabhängig von Proxmox

## Aufbewahrung

| Typ                | Aufbewahrung |
| ------------------ | ------------ |
| tägliche Dumps     | 14 Tage      |
| wöchentliche Dumps | 8 Wochen     |
| monatliche Dumps   | 12 Monate    |

---

# Ebene 2 – Proxmox VM Backups

Zusätzlich zu den DB Dumps.

Ziel:

* komplette VM Wiederherstellung
* schnelles Recovery
* Schutz bei Konfigurationsfehlern

Zu sichern:

* postgres-prod
* kcserver-prod
* keycloak-prod
* edge-prod

Empfehlung:

## Backup-Zeitfenster

| VM            | Zeit  |
| ------------- | ----- |
| postgres-prod | 03:00 |
| keycloak-prod | 03:15 |
| kcserver-prod | 03:30 |
| edge-prod     | 03:45 |

## Modus

* snapshot mode
* compression aktivieren

---

# Ebene 3 – Konfigurationsbackups

Zusätzlich sichern:

## kcserver-prod

Wichtige Dateien:

```text
application.yml
.env
systemd services
Docker compose Dateien
TLS Zertifikate
```

## keycloak-prod

Wichtige Dateien:

```text
realm exports
client configs
Docker compose
.env
```

## edge-prod

Wichtige Dateien:

```text
nginx configs
traefik configs
TLS Zertifikate
DNS configs
```

---

# Sehr wichtig: Offsite Backup

Ein Backup auf demselben Dell ist KEIN vollständiges Backup.

Aktuell übernimmt der MiniMac Server "legolas" die Rolle des sekundären Backup-Ziels.

System:

| Host    | IP             |
| ------- | -------------- |
| legolas | 192.168.100.20 |

Das schützt bereits gegen:

* VM Fehler
* versehentliche Löschung
* beschädigte Proxmox Storage Daten
* einzelne SSD Ausfälle

Aber:

Der MiniMac steht vermutlich im selben Gebäude.

Deshalb später zusätzlich sinnvoll:

* externe USB Backup Platte
* zweiter Standort
* verschlüsseltes Cloud Backup

---

# Empfohlene Minimal-Architektur

## Lokale Ebene

Dell / Proxmox:

* tägliche VM Backups
* tägliche pg_dumps

## Externe Ebene

Zusätzlich:

* nächtliche Synchronisation auf den MiniMac Fileserver

System:

| Host    | IP             | Rolle                            |
| ------- | -------------- | -------------------------------- |
| legolas | 192.168.100.20 | zentraler Backup- und Fileserver |

Empfehlung:

* eigener SMB Share nur für Backups
* getrennte Ordner:

```text
/backups/proxmox
/backups/postgres
/backups/configs
```

* Zugriff nur für Backup-User
* keine normalen Benutzerfreigaben mischen

---

# Wiederherstellungsstrategie

Backups sind nur sinnvoll, wenn Restore funktioniert.

Deshalb:

## Monatlicher Restore-Test

Testen:

1. neue Test-VM
2. PostgreSQL installieren
3. Dump einspielen
4. KC starten
5. Login testen

---

# Wiederherstellungsszenarien

## Fall 1 – Datenfehler

Lösung:

* pg_dump Restore

Dauer:

* Minuten

---

## Fall 2 – kaputte VM

Lösung:

* Proxmox VM Restore

Dauer:

* 10–30 Minuten

---

## Fall 3 – kompletter Dell Ausfall

Lösung:

* Offsite Restore
* Proxmox neu installieren
* VMs zurückspielen

Dauer:

* Stunden

---

# Reihenfolge beim Restore

Wichtig:

1. postgres-prod
2. keycloak-prod
3. kcserver-prod
4. edge-prod

Denn:

* KC benötigt PostgreSQL
* KC benötigt Keycloak
* Edge benötigt die Backend-Systeme

---

# Empfohlene Erweiterungen später

## Mittelfristig

* Backup Monitoring
* automatische Backup-Prüfung
* verschlüsselte Offsite Backups
* immutable Backups
* WAL Archiving
* Point-in-Time Recovery

---

# Prioritäten für KanuControl jetzt

## Sofort

* pg_dump automatisieren
* Proxmox Backup Job anlegen
* externe Kopie definieren

## Danach

* Restore testen
* Aufbewahrung automatisieren
* Monitoring

## Später

* Verschlüsselung
* PITR
* HA/Failover

---

# PostgreSQL Backup – Implementierte Produktivlösung

## Systemübersicht

| Komponente       | Wert                         |
| ---------------- | ---------------------------- |
| VM               | postgres-prod                |
| IP               | 192.168.100.31               |
| Datenbank        | kanucontrol                  |
| Backup-Ziel      | legolas                      |
| Backup-Server-IP | 192.168.100.20               |
| SMB Share        | KC_Backup                    |
| Zielordner       | /mnt/legolas-backup/postgres |

---

# Architektur

```text
postgres-prod
    ↓ pg_dump
CIFS Mount
    ↓
legolas:/Volumes/4 TB TOURO/KC_Backup/postgres
```

---

# SMB Mount Konfiguration

## Mountpunkt

```text
/mnt/legolas-backup
```

---

## Credentials-Datei

Pfad:

```text
/root/.smbcredentials
```

Inhalt:

```text
username=kcbackup
password=********
```

Rechte:

```bash
chmod 600 /root/.smbcredentials
```

---

## /etc/fstab

```text
//192.168.100.20/KC_Backup /mnt/legolas-backup cifs credentials=/root/.smbcredentials,uid=postgres,gid=postgres,file_mode=0660,dir_mode=0770,nofail,x-systemd.automount 0 0
```

---

# Backup Script

Pfad:

```text
/opt/kc-backup/postgres-backup.sh
```

Inhalt:

```bash
#!/bin/bash

cd /tmp

DATE=$(date +%Y%m%d_%H%M)

BACKUP_DIR=/mnt/legolas-backup/postgres

/usr/bin/sudo -u postgres /usr/bin/pg_dump -Fc kanucontrol > $BACKUP_DIR/kanucontrol_$DATE.dump

find $BACKUP_DIR -name "*.dump" -mtime +14 -delete
```

---

# Script-Rechte

```bash
chmod +x /opt/kc-backup/postgres-backup.sh
```

---

# Cronjob

## Eintrag

```text
0 2 * * * /opt/kc-backup/postgres-backup.sh >> /var/log/postgres-backup.log 2>&1
```

## Ausführungszeit

* täglich um 02:00 Uhr

---

# Logging

Logdatei:

```text
/var/log/postgres-backup.log
```

---

# Aufbewahrung

Aktuell:

* automatische Löschung nach 14 Tagen

Implementiert über:

```bash
find $BACKUP_DIR -name "*.dump" -mtime +14 -delete
```

---

# Verifikation

Erfolgreich getestet:

* SMB Mount
* Schreibrechte
* pg_dump
* automatische Dateierstellung
* Rotation
* Cron-Ausführung

---

# Beispiel erfolgreicher Dumps

```text
kanucontrol_20260513_2024.dump
kanucontrol_20260513_2028.dump
```

---

# Wiederherstellung (Restore)

Beispiel:

```bash
sudo -u postgres pg_restore -d kanucontrol <dumpfile>
```

---

# Nächste Schritte

## Kurzfristig

* Restore-Test durchführen
* Logrotation ergänzen
* Backup Monitoring

## Danach

* Proxmox VM Backups
* Config Backups weiterer VMs
* Offsite-Kopie erweitern

---

# Wichtigste Erkenntnis

Für KanuControl ist aktuell:

"Ein getestetes Backup ist wichtiger als komplexe Hochverfügbarkeit."
