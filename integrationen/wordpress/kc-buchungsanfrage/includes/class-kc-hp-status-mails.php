<?php
if (!defined('ABSPATH')) { exit; }
/** Eigene Mailwarteschlange: unabhängig von der 30-Tage-Kopie des Anfrageformulars. */
final class KC_HP_Status_Mails {
    public static function table(): string { global $wpdb; return $wpdb->prefix . 'kc_hp_status_mails'; }
    public static function install(): void {
        global $wpdb;
        $charset = $wpdb->get_charset_collate();
        dbDelta('CREATE TABLE ' . self::table() . " (
            id bigint unsigned NOT NULL AUTO_INCREMENT,
            event_id varchar(36) NOT NULL,
            target_hash varchar(64) NOT NULL,
            buchungsnummer varchar(64) NOT NULL,
            payload_json longtext NOT NULL,
            state varchar(20) NOT NULL DEFAULT 'pending',
            acknowledged tinyint NOT NULL DEFAULT 0,
            mail_info text NULL,
            last_error text NULL,
            attempts int NOT NULL DEFAULT 0,
            next_attempt bigint NOT NULL DEFAULT 0,
            lease_until bigint NOT NULL DEFAULT 0,
            created_at datetime NOT NULL,
            PRIMARY KEY  (id),
            UNIQUE KEY event_id (event_id),
            KEY work (state,acknowledged,next_attempt),
            KEY booking_order (target_hash,buchungsnummer,state,id)
        ) $charset;");
    }
    public static function work(): void {
        global $wpdb;
        if (KC_HP_Api::settings()['mode'] !== 'live') { return; }
        $events = KC_HP_Api::call('nachrichten');
        if (is_wp_error($events)) {
            update_option('kc_hp_status_poll_error', $events->get_error_message(), false);
        } else {
            delete_option('kc_hp_status_poll_error');
            foreach ($events as $event) {
                if (!is_array($event) || !is_string($event['id'] ?? null)
                    || !preg_match('/^[a-f0-9-]{36}$/Di', $event['id']) || !is_email($event['empfaenger'] ?? '')
                    || !in_array($event['status'] ?? '', ['ANFRAGE', 'RESERVIERT', 'BESTAETIGT', 'ABGELEHNT', 'STORNIERT', 'ABRECHENBAR', 'ABGESCHLOSSEN'], true)
                    || !is_string($event['betreff'] ?? null) || !is_string($event['text'] ?? null)
                    || empty($event['buchungsnummer'])) {
                    update_option('kc_hp_status_poll_error', 'KC hat eine unvollständige Statusnachricht geliefert. Bitte Empfängeradresse und Backend prüfen.', false);
                    continue;
                }
                $ok = $wpdb->query($wpdb->prepare('INSERT IGNORE INTO ' . self::table()
                    . ' (event_id,target_hash,buchungsnummer,payload_json,created_at) VALUES (%s,%s,%s,%s,%s)',
                    $event['id'], KC_HP_Api::target(), $event['buchungsnummer'], wp_json_encode($event), gmdate('Y-m-d H:i:s')));
                if ($ok === false) {
                    update_option('kc_hp_status_poll_error', 'Statusnachricht konnte nicht lokal gesichert werden.', false);
                }
            }
        }
        // Reihenfolge je Buchung beibehalten, auch wenn eine ältere Nachricht noch aussteht.
        $table = self::table();
        $ids = $wpdb->get_col($wpdb->prepare("SELECT m.id FROM $table m
            WHERE m.next_attempt<=%d AND m.lease_until<%d AND (m.state='pending' OR m.acknowledged=0)
            AND NOT EXISTS (SELECT 1 FROM $table old WHERE old.target_hash=m.target_hash
                AND old.buchungsnummer=m.buchungsnummer AND old.id<m.id AND old.state='pending')
            ORDER BY m.id LIMIT 3", time(), time()));
        foreach ($ids as $id) { self::process((int) $id); }
        $wpdb->query($wpdb->prepare("DELETE FROM $table WHERE state='sent' AND acknowledged=1 AND created_at<%s",
            gmdate('Y-m-d H:i:s', time() - 30 * DAY_IN_SECONDS)));
    }
    public static function process(int $id): void {
        global $wpdb;
        if (KC_HP_Api::settings()['mode'] !== 'live') { return; }
        $table = self::table(); $now = time();
        if ($wpdb->query($wpdb->prepare("UPDATE $table SET lease_until=%d WHERE id=%d AND lease_until<%d", $now + 180, $id, $now)) !== 1) { return; }
        try {
            $row = $wpdb->get_row($wpdb->prepare("SELECT * FROM $table WHERE id=%d", $id));
            if (!$row || (int) $row->next_attempt > $now || ($row->state === 'sent' && (int) $row->acknowledged === 1)) { return; }
            if (!hash_equals($row->target_hash, KC_HP_Api::target())) {
                self::retry_later($row, 'Die Zielverbindung wurde geändert. Ursprüngliche Verbindung vor Wiederholung wiederherstellen.');
                return;
            }
            $older = $wpdb->get_var($wpdb->prepare("SELECT COUNT(*) FROM $table WHERE target_hash=%s AND buchungsnummer=%s AND id<%d AND state='pending'",
                $row->target_hash, $row->buchungsnummer, $id));
            if ((int) $older > 0) { return; }
            $event = json_decode($row->payload_json, true);
            if ($row->state === 'pending') {
                $headers = ['Content-Type: text/plain; charset=UTF-8'];
                if (is_email($event['replyTo'] ?? '')) { $headers[] = 'Reply-To: ' . $event['replyTo']; }
                [$accepted, $info] = KC_HP_Queue::mail($event['empfaenger'], sanitize_text_field($event['betreff']), $event['text'], $headers);
                if (!$accepted) {
                    $wpdb->update($table, ['mail_info' => $info], ['id' => $id]);
                    self::retry_later($row, 'Statusnachricht konnte nicht an den Maildienst übergeben werden.');
                    return;
                }
                // Vor der KC-Quittierung dauerhaft festhalten: Ein Quittierungsfehler darf nicht erneut mailen.
                if ($wpdb->update($table, ['state' => 'sent', 'mail_info' => $info, 'last_error' => null], ['id' => $id]) === false) { return; }
            }
            $ack = KC_HP_Api::call('nachrichten/' . $row->event_id . '/quittieren', []);
            if (is_wp_error($ack) || ($ack['quittiert'] ?? false) !== true) {
                self::retry_later($row, 'Mail übergeben; KC-Quittierung steht noch aus.');
                return;
            }
            $wpdb->update($table, ['acknowledged' => 1, 'last_error' => null, 'next_attempt' => 0], ['id' => $id]);
        } finally {
            $wpdb->update($table, ['lease_until' => 0], ['id' => $id]);
        }
    }
    private static function retry_later(object $row, string $message): void {
        global $wpdb;
        $attempts = (int) $row->attempts + 1;
        $wpdb->update(self::table(), ['attempts' => $attempts, 'last_error' => $message,
            'next_attempt' => time() + min(3600, 60 * (2 ** min(6, $attempts)))], ['id' => $row->id]);
    }
    public static function retry(): void {
        global $wpdb;
        if (!current_user_can('manage_options')) { wp_die('Kein Zugriff.'); }
        check_admin_referer('kc_hp_status_retry');
        $id = absint($_POST['id'] ?? 0);
        $wpdb->query($wpdb->prepare('UPDATE ' . self::table() . ' SET next_attempt=0,attempts=0 WHERE id=%d AND lease_until<%d', $id, time()));
        self::process($id);
        wp_safe_redirect(admin_url('options-general.php?page=kc-buchungsanfrage')); exit;
    }
    public static function admin(): void {
        global $wpdb;
        echo '<h2>Statusnachrichten aus KC</h2><p>Reservierung, Bestätigung, Ablehnung, Stornierung und Änderungen des Abrechnungsstatus werden automatisch abgeholt. Der Versand läuft über WP-Cron. „sent“ bedeutet an den Maildienst übergeben; eine erfolgreiche KC-Quittierung verhindert einen erneuten Versand beim nächsten Abruf.</p>';
        $error = get_option('kc_hp_status_poll_error');
        if ($error) { echo '<div class="notice notice-warning inline"><p>' . esc_html($error) . '</p></div>'; }
        $rows = $wpdb->get_results('SELECT * FROM ' . self::table() . ' ORDER BY id DESC LIMIT 50');
        echo '<table class="widefat striped"><thead><tr><th>Buchung / Status</th><th>Empfänger</th><th>Versand / KC-Quittierung</th><th>Hinweis</th><th>Aktion</th></tr></thead><tbody>';
        foreach ($rows as $row) {
            $event = json_decode($row->payload_json, true);
            echo '<tr><td>' . esc_html($row->buchungsnummer) . '<br>' . esc_html($event['status'] ?? '') . '</td><td>'
                . esc_html($event['empfaenger'] ?? '') . '</td><td>' . esc_html($row->state)
                . ' / ' . ((int) $row->acknowledged === 1 ? 'quittiert' : 'offen') . '</td><td>'
                . esc_html($row->last_error ?? '');
            if ($row->mail_info) { echo '<br><small>' . esc_html(KC_HP_Plugin::mail_diagnostic($row->mail_info)) . '</small>'; }
            echo '</td><td>';
            if ($row->state !== 'sent' || (int) $row->acknowledged === 0) {
                echo '<form method="post" action="' . esc_url(admin_url('admin-post.php')) . '"><input type="hidden" name="action" value="kc_hp_status_retry" /><input type="hidden" name="id" value="' . (int) $row->id . '" />';
                wp_nonce_field('kc_hp_status_retry'); echo '<button class="button">Erneut bearbeiten</button></form>';
            }
            echo '</td></tr>';
        }
        if (!$rows) { echo '<tr><td colspan="5">Noch keine Statusnachrichten abgeholt.</td></tr>'; }
        echo '</tbody></table>';
    }
}
