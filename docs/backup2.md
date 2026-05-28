                                                                
# KanuControl Backup System

## Übersicht

Dieses Verzeichnis enthält alle Backup-Skripte für die KanuControl Infrastruktur.

Backups werden zentral auf dem NAS/Server `legolas` gespeichert.

Mountpoint:

/mnt/legolas-backup

---

# Enthaltene Systeme

| VM | Zweck |
|---|---|
| postgres-prod | PostgreSQL Datenbank |
| keycloak-prod | Keycloak Konfiguration |
| kcserver-prod | KanuControl Backend Konfiguration |
| edge-prod | Reverse Proxy / nginx / SSL / Cloudflare |

---

# Backup-Arten

## PostgreSQL Backup

Script:

/opt/kc-backup/postgres-backup.sh

Enthält:
- PostgreSQL Datenbank `kanucontrol`
- pg_dump Export

Ziel:
- /mnt/legolas-backup/postgres/

---

## Keycloak Backup

Script:

/opt/kc-backup/keycloak-backup.sh

Enthält:
- Realms
- Clients
- Rollen
                                                                       [ Read 169 lines ]
^G Help         ^O Write Out    ^W Where Is     ^K Cut          ^T Execute      ^C Location     M-U Undo        M-A Set Mark    M-] To Bracket  M-Q Previous
^X Exit         ^R Read File    ^\ Replace      ^U Paste        ^J Justify      ^/ Go To Line   M-E Redo        M-6 Copy        ^Q Where Was    M-W Next
