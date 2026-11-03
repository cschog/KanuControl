#!/bin/sh
set -eu
KC_TEST_DIR=$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)
KC_PLUGIN_DIR=$(CDPATH='' cd -- "$KC_TEST_DIR/../kc-buchungsanfrage" && pwd)
for kc_container in kc-hp-test-db kc-hp-test-wp; do
    if docker inspect "$kc_container" >/dev/null 2>&1; then
        echo "Testname bereits belegt: $kc_container. Bestehende Container werden nicht verändert." >&2
        exit 1
    fi
done
# Nur eigens angelegte Testressourcen entfernen.
docker network create --internal kc-hp-local-test >/dev/null
cleanup() {
    docker rm -fv kc-hp-test-wp kc-hp-test-db >/dev/null 2>&1 || true
    docker network rm kc-hp-local-test >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM
docker run -d --name kc-hp-test-db --network kc-hp-local-test --tmpfs /var/lib/mysql \
    -e MYSQL_ROOT_PASSWORD=local-test-only -e MYSQL_DATABASE=wordpress -e MYSQL_USER=wordpress \
    -e MYSQL_PASSWORD=local-test-only mysql:8.0 >/dev/null
docker run -d --name kc-hp-test-wp --network kc-hp-local-test -p 127.0.0.1:18765:80 \
    -e WORDPRESS_DB_HOST=kc-hp-test-db -e WORDPRESS_DB_USER=wordpress -e WORDPRESS_DB_PASSWORD=local-test-only \
    -e WORDPRESS_DB_NAME=wordpress -e "WORDPRESS_CONFIG_EXTRA=define('DISABLE_WP_CRON', true);" \
    -v "$KC_PLUGIN_DIR:/var/www/html/wp-content/plugins/kc-buchungsanfrage:ro" wordpress:php8.2-apache >/dev/null
# Docker Desktop benötigt eine nicht-interne Verbindung für die localhost-Portweiterleitung.
# Die MU-Erweiterung fängt alle WordPress-HTTP-Aufrufe und sämtliche Mails ab.
docker network connect bridge kc-hp-test-wp
kc_ready=0
for kc_attempt in $(seq 1 60); do
    if docker exec kc-hp-test-db mysql --protocol=TCP -h127.0.0.1 -uwordpress -plocal-test-only --database=wordpress -e "SELECT 1" >/dev/null 2>&1 \
       && docker exec kc-hp-test-wp test -f /var/www/html/wp-load.php; then
        kc_ready=1
        break
    fi
    sleep 1
done
if [ "$kc_ready" != 1 ]; then echo "Testumgebung wurde nicht bereit." >&2; exit 1; fi
docker exec kc-hp-test-wp mkdir -p /var/www/html/wp-content/mu-plugins
docker cp "$KC_TEST_DIR/mock-kc.php" kc-hp-test-wp:/var/www/html/wp-content/mu-plugins/mock-kc.php
docker cp "$KC_TEST_DIR/setup.php" kc-hp-test-wp:/tmp/kc-setup.php
docker exec kc-hp-test-wp php /tmp/kc-setup.php
python3 "$KC_TEST_DIR/check_wordpress.py"
node "$KC_TEST_DIR/browser.cjs"
