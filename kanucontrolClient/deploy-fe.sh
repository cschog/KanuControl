#!/bin/bash

ssh-add --apple-load-keychain >/dev/null 2>&1

echo "🛠️ Frontend wird gebaut..."
yarn build

echo "📁 Zielordner vorbereiten..."
ssh chris@edge-prod "mkdir -p /var/www/kc_client/dist"

echo "📦 Frontend wird kopiert..."
rsync -avz --delete dist/ \
chris@edge-prod:/var/www/kc_client/dist/

echo "✅ Frontend Deploy abgeschlossen."