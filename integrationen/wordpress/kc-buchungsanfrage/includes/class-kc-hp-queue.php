<?php
if (!defined('ABSPATH')) { exit; }
final class KC_HP_Queue {
    public static function table(): string { global $wpdb; return $wpdb->prefix . 'kc_hp_anfragen'; }
    public static function limits(): string { global $wpdb; return $wpdb->prefix . 'kc_hp_limits'; }
    public static function install(): void {
        global $wpdb;
        require_once ABSPATH . 'wp-admin/includes/upgrade.php';
        $charset = $wpdb->get_charset_collate();
        dbDelta('CREATE TABLE ' . self::table() . " (
            id bigint unsigned NOT NULL AUTO_INCREMENT,
            request_id varchar(36) NOT NULL,
            payload_hash varchar(64) NOT NULL,
            target_hash varchar(64) NOT NULL,
            payload_json longtext NOT NULL,
            result_json longtext NULL,
            status varchar(20) NOT NULL DEFAULT 'pending',
            guest_mail varchar(20) NOT NULL DEFAULT 'pending',
            admin_mail varchar(20) NOT NULL DEFAULT 'pending',
            guest_mail_info text NULL,
            admin_mail_info text NULL,
            attempts int NOT NULL DEFAULT 0,
            next_attempt bigint NOT NULL DEFAULT 0,
            lease_until bigint NOT NULL DEFAULT 0,
            last_error text NULL,
            created_at datetime NOT NULL,
            PRIMARY KEY  (id),
            UNIQUE KEY request_id (request_id),
            KEY work (status,next_attempt)
        ) $charset;");
        dbDelta('CREATE TABLE ' . self::limits() . " (
            bucket varchar(64) NOT NULL,
            hits int NOT NULL,
            expires bigint NOT NULL,
            PRIMARY KEY  (bucket)
        ) $charset;");
        KC_HP_Status_Mails::install();
        update_option('kc_hp_db_version', KC_HP_VERSION, false);
        if (!wp_next_scheduled('kc_hp_work')) { wp_schedule_event(time() + 60, 'kc_hp_minute', 'kc_hp_work'); }
    }
    public static function rate(string $action, int $max, int $seconds): bool {
        global $wpdb;
        // Nur REMOTE_ADDR verwenden, keine vom Besucher frei setzbaren Proxy-Header.
        $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
        $bucket = hash_hmac('sha256', $action . ':' . $ip, wp_salt('nonce'));
        $sql = $wpdb->prepare('INSERT INTO ' . self::limits() . ' (bucket,hits,expires) VALUES (%s,1,%d)
            ON DUPLICATE KEY UPDATE hits=IF(expires<%d,1,hits+1), expires=IF(expires<%d,VALUES(expires),expires)',
            $bucket, time() + $seconds, time(), time());
        if ($wpdb->query($sql) === false) { return false; }
        return (int) $wpdb->get_var($wpdb->prepare('SELECT hits FROM ' . self::limits() . ' WHERE bucket=%s', $bucket)) <= $max;
    }
    public static function enqueue(array $payload) {
        global $wpdb;
        $json = wp_json_encode($payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        $hash = hash('sha256', $json);
        $row = $wpdb->get_row($wpdb->prepare('SELECT * FROM ' . self::table() . ' WHERE request_id=%s', $payload['anfrageId']));
        if ($row) {
            if (!hash_equals($row->payload_hash, $hash) || !hash_equals($row->target_hash, KC_HP_Api::target())) {
                return new WP_Error('kc_duplicate', 'Diese Vorgangs-ID wurde bereits verwendet. Bitte das Formular neu öffnen.', ['status' => 409]);
            }
            return (int) $row->id;
        }
        $ok = $wpdb->insert(self::table(), ['request_id' => $payload['anfrageId'], 'payload_hash' => $hash,
            'target_hash' => KC_HP_Api::target(), 'payload_json' => $json, 'created_at' => gmdate('Y-m-d H:i:s')]);
        if ($ok === false) {
            // Gleichzeitige identische POSTs können denselben UNIQUE-Schlüssel treffen.
            $row = $wpdb->get_row($wpdb->prepare('SELECT * FROM ' . self::table() . ' WHERE request_id=%s', $payload['anfrageId']));
            if ($row && hash_equals($row->payload_hash, $hash) && hash_equals($row->target_hash, KC_HP_Api::target())) { return (int) $row->id; }
            return new WP_Error('kc_store', 'Die Anfrage konnte nicht gespeichert werden. Bitte später erneut versuchen.', ['status' => 503]);
        }
        return (int) $wpdb->insert_id;
    }
    public static function mail(string $to, string $subject, string $body, array $headers): array {
        $info = ['from' => '', 'transport' => '', 'success_hook' => false, 'error_code' => ''];
        $mailer = static function ($mail) use (&$info) {
            $info['from'] = sanitize_email($mail->From);
            $info['transport'] = in_array($mail->Mailer, ['smtp', 'mail', 'sendmail', 'qmail'], true) ? $mail->Mailer : 'other';
        };
        $success = static function () use (&$info) { $info['success_hook'] = true; };
        $failure = static function ($error) use (&$info) { $info['error_code'] = sanitize_key($error->get_error_code()); };
        add_action('phpmailer_init', $mailer, PHP_INT_MAX);
        add_action('wp_mail_succeeded', $success, PHP_INT_MAX);
        add_action('wp_mail_failed', $failure, PHP_INT_MAX);
        try {
            $accepted = (bool) wp_mail($to, $subject, $body, $headers);
        } catch (Throwable $error) {
            $accepted = false;
            $info['error_code'] = 'mail_exception';
        } finally {
            remove_action('phpmailer_init', $mailer, PHP_INT_MAX);
            remove_action('wp_mail_succeeded', $success, PHP_INT_MAX);
            remove_action('wp_mail_failed', $failure, PHP_INT_MAX);
        }
        return [$accepted, wp_json_encode($info)];
    }

    public static function process(int $id): void {
        global $wpdb;
        if (KC_HP_Api::settings()['mode'] !== 'live') { return; }
        $table = self::table();
        $now = time();
        $locked = $wpdb->query($wpdb->prepare("UPDATE $table SET lease_until=%d WHERE id=%d AND lease_until<%d", $now + 180, $id, $now));
        if ($locked !== 1) { return; }
        try {
            $row = $wpdb->get_row($wpdb->prepare("SELECT * FROM $table WHERE id=%d", $id));
            if (!$row || $row->status === 'failed' || (int) $row->next_attempt > $now) { return; }
            if (!hash_equals($row->target_hash, KC_HP_Api::target())) {
                $wpdb->update($table, ['status' => 'failed', 'last_error' => 'Die Zielverbindung wurde geändert. Vor einer Wiederholung die ursprüngliche Verbindung wiederherstellen.'], ['id' => $id]);
                return;
            }
            $payload = json_decode($row->payload_json, true);
            if ($row->status === 'pending') {
                $result = KC_HP_Api::call('anfragen', $payload);
                if (is_wp_error($result)) {
                    $status = (int) ($result->get_error_data()['status'] ?? 503);
                    $attempts = (int) $row->attempts + 1;
                    $terminal = ($status >= 400 && $status < 500 && $status !== 429) || $attempts >= 10;
                    $wpdb->update($table, ['status' => $terminal ? 'failed' : 'pending', 'attempts' => $attempts,
                        'next_attempt' => $now + min(3600, 60 * (2 ** min(6, $attempts))),
                        'last_error' => $result->get_error_message()], ['id' => $id]);
                    return;
                }
                if (empty($result['buchungsnummer']) || ($result['status'] ?? '') !== 'ANFRAGE') {
                    $wpdb->update($table, ['last_error' => 'KC hat keine gültige Eingangsbestätigung geliefert.', 'next_attempt' => $now + 300], ['id' => $id]);
                    return;
                }
                // Erst dauerhaft festhalten, dass KC gespeichert hat. Bei DB-Fehler keine E-Mails senden.
                $ok = $wpdb->update($table, ['status' => 'confirmed', 'result_json' => wp_json_encode($result), 'last_error' => null,
                    'guest_mail' => !empty($result['gastBestaetigung']) ? 'pending' : 'off',
                    'admin_mail' => !empty($result['verwaltungsBenachrichtigung']) ? 'pending' : 'off', 'attempts' => 0, 'next_attempt' => 0], ['id' => $id]);
                if ($ok === false) { return; }
                $row = $wpdb->get_row($wpdb->prepare("SELECT * FROM $table WHERE id=%d", $id));
            }
            if ($row->status !== 'confirmed') { return; }
            $result = json_decode($row->result_json, true);
            $dates = $payload['auswahl']['anreise'] . ' bis ' . $payload['auswahl']['abreise'];
            $number = sanitize_text_field($result['buchungsnummer']);
            $admin = is_email($result['empfaengerEmail'] ?? '') ? $result['empfaengerEmail'] : '';
            $errors = [];
            if ($row->guest_mail === 'pending') {
                $body = 'Hallo ' . $payload['vorname'] . ",\n\nvielen Dank für deine Anfrage für den Zeitraum " . $dates
                    . '. Wir haben sie unter der Nummer ' . $number . " erhalten.\n\nDies ist eine Eingangsbestätigung, noch keine Reservierung oder Buchungsbestätigung. Wir prüfen deine Angaben und melden uns bei dir.\n";
                $headers = ['Content-Type: text/plain; charset=UTF-8'];
                if ($admin) { $headers[] = 'Reply-To: ' . $admin; }
                [$sent, $info] = self::mail($payload['email'], 'Deine Buchungsanfrage ' . $number . ' ist eingegangen', $body, $headers);
                $wpdb->update($table, ['guest_mail_info' => $info], ['id' => $id]);
                if ($sent) { $wpdb->update($table, ['guest_mail' => 'sent'], ['id' => $id]); }
                else { $errors[] = 'Eingangsbestätigung konnte nicht an den Maildienst übergeben werden.'; }
            }
            if ($row->admin_mail === 'pending') {
                $body = 'Neue Homepage-Anfrage ' . $number . "\nZeitraum: " . $dates . "\nKontakt: " . $payload['vorname'] . ' ' . $payload['name']
                    . "\nE-Mail: " . $payload['email'] . "\nTelefon: " . $payload['telefon']
                    . "\nJugendliche: " . ($payload['anzahlJugendliche'] ?? 'nicht erfasst')
                    . "\nAlter ca.: " . ($payload['jugendAlterVon'] ?? '–') . ' bis ' . ($payload['jugendAlterBis'] ?? '–')
                    . "\nErwachsene: " . ($payload['anzahlErwachsene'] ?? 'nicht erfasst')
                    . "\nMietumfang: " . ($result['mietumfang'] ?? 'Bitte in KC ansehen')
                    . "\nVeranstalter: " . ($payload['veranstalter'] ?: 'nicht angegeben')
                    . "\nBemerkung: " . ($payload['bemerkung'] ?: 'keine')
                    . "\n\nBitte in KanuControl unter Vermietung → Buchung anhand der Buchungsnummer prüfen und weiterbearbeiten.\n";
                [$sent, $info] = $admin ? self::mail($admin, 'Neue Homepage-Anfrage ' . $number, $body,
                    ['Content-Type: text/plain; charset=UTF-8', 'Reply-To: ' . $payload['email']]) : [false, ''];
                $wpdb->update($table, ['admin_mail_info' => $info], ['id' => $id]);
                if ($sent) { $wpdb->update($table, ['admin_mail' => 'sent'], ['id' => $id]); }
                else { $errors[] = 'Verwaltungsnachricht konnte nicht an den Maildienst übergeben werden.'; }
            }
            $wpdb->update($table, ['last_error' => $errors ? implode(' ', $errors) : null, 'next_attempt' => $errors ? $now + 300 : 0], ['id' => $id]);
        } finally {
            $wpdb->update($table, ['lease_until' => 0], ['id' => $id]);
        }
    }
    public static function response(int $id): array {
        global $wpdb;
        $row = $wpdb->get_row($wpdb->prepare('SELECT * FROM ' . self::table() . ' WHERE id=%d', $id));
        if (!$row) { return ['status' => 'failed', 'message' => 'Der lokale Vorgang konnte nicht gelesen werden.']; }
        if ($row->status === 'confirmed') {
            $result = json_decode($row->result_json, true);
            return ['status' => 'confirmed', 'buchungsnummer' => $result['buchungsnummer'],
                'message' => 'Deine Anfrage ist in KC eingegangen. Dies ist noch keine Reservierung oder Buchungsbestätigung.'];
        }
        if ($row->status === 'failed') {
            return ['status' => 'failed', 'message' => 'Die Übertragung ist angehalten: ' . $row->last_error . ' Bitte kontaktiere die Vermietung und nenne Vorgang ' . $row->request_id . '.'];
        }
        return ['status' => 'pending', 'message' => 'Deine Anfrage ist auf der Homepage gespeichert und wartet auf die Übertragung an KC. Bitte nicht erneut als neue Anfrage absenden. Vorgang: ' . $row->request_id];
    }
    public static function work(): void {
        global $wpdb;
        if (KC_HP_Api::settings()['mode'] !== 'live') { return; }
        $ids = $wpdb->get_col($wpdb->prepare('SELECT id FROM ' . self::table()
            . " WHERE next_attempt<=%d AND lease_until<%d AND (status='pending' OR (status='confirmed' AND (guest_mail='pending' OR admin_mail='pending'))) ORDER BY id LIMIT 5", time(), time()));
        foreach ($ids as $id) { self::process((int) $id); }
        $wpdb->query($wpdb->prepare('DELETE FROM ' . self::limits() . ' WHERE expires<%d', time() - 3600));
        // Kopien abgeschlossener Versandvorgänge nach 30 Tagen entfernen. Unklare Vorgänge bleiben zur Prüfung erhalten.
        $wpdb->query($wpdb->prepare('DELETE FROM ' . self::table()
            . " WHERE status='confirmed' AND guest_mail<>'pending' AND admin_mail<>'pending' AND created_at<%s", gmdate('Y-m-d H:i:s', time() - 30 * DAY_IN_SECONDS)));
    }
}
