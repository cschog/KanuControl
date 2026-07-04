Touro Backup v3.1

Highlights
- Timestamped snapshots
- rsync progress (--info=progress2)
- Hard-link snapshots via --link-dest
- Keeps max. 12 backups
- Deletes oldest backups when space is insufficient
- 20 GB safety reserve
- Automatic mount/unmount
- Logging to touro-backup.log

Install:
cp touro-backup-v3.1.sh /root/scripts/touro-backup.sh
chmod +x /root/scripts/touro-backup.sh
