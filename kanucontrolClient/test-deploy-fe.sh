#!/bin/bash
set -euo pipefail

ssh-add --apple-load-keychain >/dev/null 2>&1

echo "🛠️ Frontend wird gebaut..."
yarn build

echo "📁 Test-Zielordner vorbereiten..."
ssh chris@edge-prod "mkdir -p /var/www/kc_client-test/dist"

echo "📦 Test-Frontend wird kopiert..."
rsync -avz --delete dist/ \
chris@edge-prod:/var/www/kc_client-test/dist/

echo "✅ TEST-Frontend Deploy abgeschlossen."