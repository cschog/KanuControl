#!/usr/bin/env python3
"""Client und Server gemeinsam hochzählen; kein Commit, Tag oder Deployment."""
import argparse
import json
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("part", choices=("patch", "minor", "major"))
parser.add_argument("--dry-run", action="store_true")
args = parser.parse_args()
client = root / "kanucontrolClient/package.json"
server = root / "kanucontrolServer/pom.xml"
client_text, server_text = client.read_text(), server.read_text()
old = json.loads(client_text)["version"]
backend = ET.fromstring(server_text).findtext("{http://maven.apache.org/POM/4.0.0}version")
if backend != old or not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+", old):
    sys.exit(f"Abbruch: Versionen ungültig oder unterschiedlich: Client={old}, Server={backend}")
major, minor, patch = map(int, old.split("."))
new = {"patch": f"{major}.{minor}.{patch + 1}",
       "minor": f"{major}.{minor + 1}.0",
       "major": f"{major + 1}.0.0"}[args.part]
client_new, count = re.subn(r'("version"\s*:\s*")' + re.escape(old) + r'(")', lambda m: m[1] + new + m[2], client_text, count=1)
server_new, server_count = re.subn(r'(<version>)' + re.escape(old) + r'(</version>)', lambda m: m[1] + new + m[2], server_text, count=1)
if count != 1 or server_count != 1:
    sys.exit("Abbruch: Versionsfelder nicht eindeutig gefunden.")
if (root / "kanucontrolClient/package-lock.json").exists():
    sys.exit("Abbruch: package-lock.json vorhanden; Lockdatei muss ebenfalls aktualisiert werden.")
if not args.dry_run:
    client.write_text(client_new)
    try:
        server.write_text(server_new)
    except OSError:
        client.write_text(client_text)
        raise
print(f"{old} → {new}" + (" (Vorschau)" if args.dry_run else ""))
print("Versionshinweise ergänzen und Tests/Builds ausführen. Kein Deployment ausgeführt.")
