# Touro Backup 3.0

## Features

-   Incremental snapshots using `rsync --link-dest`
-   Automatic cleanup (keeps the newest 12 backups)
-   Automatic retry if the disk becomes full by deleting the oldest
    snapshot
-   Progress display using `--info=progress2`
-   Logging
-   Safe unmount via `trap`

## Install

Copy `touro-backup.sh` to `/root/scripts/` and make it executable:

``` bash
chmod +x /root/scripts/touro-backup.sh
```

## Restore

Each backup directory is a complete snapshot. Simply copy files back
with `cp` or `rsync`.
