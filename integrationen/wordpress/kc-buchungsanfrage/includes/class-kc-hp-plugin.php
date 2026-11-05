<?php
if (!defined('ABSPATH')) { exit; }
final class KC_HP_Plugin {
    public static function init(): void {
        if (get_option('kc_hp_db_version') !== KC_HP_VERSION) { KC_HP_Queue::install(); }
        add_action('admin_menu', static function () {
            add_options_page('KC Buchungsanfrage', 'KC Buchungsanfrage', 'manage_options', 'kc-buchungsanfrage', [self::class, 'admin']);
        });
        add_action('admin_init', static function () {
            register_setting('kc_hp', 'kc_hp_settings', ['sanitize_callback' => [self::class, 'sanitize_settings']]);
        });
        add_action('rest_api_init', [self::class, 'routes']);
        add_action('admin_post_kc_hp_retry', [self::class, 'retry']);
        add_action('admin_post_kc_hp_test', [self::class, 'test']);
        add_action('admin_post_kc_hp_resend', [self::class, 'resend']);
        KC_HP_Calendar::init();
        add_shortcode('kc_buchungsanfrage', [self::class, 'shortcode']);
        add_action('wp_enqueue_scripts', [self::class, 'assets']);
    }
    public static function assets(): void {
        wp_register_style('kc-hp', plugins_url('assets/form.css', KC_HP_FILE), [], KC_HP_VERSION);
        wp_register_script('kc-hp', plugins_url('assets/form.js', KC_HP_FILE), [], KC_HP_VERSION, true);
    }
    public static function sanitize_settings($input): array {
        $old = KC_HP_Api::settings();
        $input = is_array($input) ? $input : [];
        $next = [
            'mode' => in_array($input['mode'] ?? '', ['off', 'preview', 'live'], true) ? $input['mode'] : 'off',
            'api_url' => KC_HP_Api::https_url((string) ($input['api_url'] ?? '')),
            'token_url' => KC_HP_Api::https_url((string) ($input['token_url'] ?? '')),
            'client_id' => sanitize_text_field($input['client_id'] ?? ''),
            'object_id' => absint($input['object_id'] ?? 0),
            'client_secret' => isset($input['client_secret']) && is_string($input['client_secret']) && $input['client_secret'] !== ''
                ? $input['client_secret'] : $old['client_secret'],
        ];
        if ($next['mode'] !== 'off' && (!$next['api_url'] || !$next['token_url'] || !$next['client_id'] || !$next['object_id']
            || (!defined('KC_HOMEPAGE_CLIENT_SECRET') && !$next['client_secret']))) {
            add_settings_error('kc_hp', 'connection', 'Bitte zuerst die vollständige HTTPS-Verbindung und den technischen Zugang einrichten.');
            return $old;
        }
        return $next;
    }
    public static function permission() {
        $mode = KC_HP_Api::settings()['mode'];
        if ($mode !== 'live' && !($mode === 'preview' && current_user_can('manage_options'))) {
            return new WP_Error('kc_disabled', 'Das Onlineformular ist derzeit nicht freigeschaltet.', ['status' => 403]);
        }
        // Browserzugriff nur von der eigenen Homepage. Dies ersetzt nicht den Spamschutz.
        $source = $_SERVER['HTTP_ORIGIN'] ?? $_SERVER['HTTP_REFERER'] ?? '';
        $a = wp_parse_url($source); $b = wp_parse_url(home_url('/'));
        if (!is_array($a) || !is_array($b) || strtolower($a['scheme'] ?? '') !== strtolower($b['scheme'] ?? '')
            || strtolower($a['host'] ?? '') !== strtolower($b['host'] ?? '') || ($a['port'] ?? 443) !== ($b['port'] ?? 443)) {
            return new WP_Error('kc_origin', 'Bitte das Formular direkt auf der Homepage öffnen.', ['status' => 403]);
        }
        return true;
    }
    public static function routes(): void {
        foreach (['session', 'verfuegbarkeit', 'anfrage'] as $name) {
            register_rest_route('kc-buchungsanfrage/v1', '/' . $name, ['methods' => 'POST',
                'permission_callback' => [self::class, 'permission'], 'callback' => [self::class, $name]]);
        }
    }
    private static function no_cache(array $body, int $status = 200): WP_REST_Response {
        $r = new WP_REST_Response($body, $status);
        $r->header('Cache-Control', 'no-store, private');
        return $r;
    }
    public static function session($request) {
        if (!KC_HP_Queue::rate('session', 30, 600)) { return new WP_Error('kc_rate', 'Bitte später erneut versuchen.', ['status' => 429]); }
        $catalog = KC_HP_Api::call('katalog');
        if (is_wp_error($catalog)) { return $catalog; }
        $cookie = $_COOKIE['kc_hp_session'] ?? '';
        if (!is_string($cookie) || !preg_match('/^[a-f0-9]{64}$/D', $cookie)) {
            $cookie = bin2hex(random_bytes(32));
            setcookie('kc_hp_session', $cookie, ['expires' => time() + 7200, 'path' => '/',
                'secure' => is_ssl(), 'httponly' => true, 'samesite' => 'Lax']);
        }
        $issued = time();
        $random = bin2hex(random_bytes(16));
        $message = $issued . ':' . $random;
        $signature = hash_hmac('sha256', $message . ':' . $cookie, wp_salt('nonce'));
        return self::no_cache(['ticket' => $message . ':' . $signature, 'mode' => KC_HP_Api::settings()['mode'],
            'catalog' => KC_HP_Api::public_catalog($catalog)]);
    }
    private static function payload($request) {
        if (strlen($request->get_body()) > 20000) { return new WP_Error('kc_size', 'Die Eingabe ist zu umfangreich.', ['status' => 413]); }
        $body = $request->get_json_params();
        if (!is_array($body)) { return new WP_Error('kc_json', 'Die Eingabe konnte nicht gelesen werden.', ['status' => 400]); }
        $ticket = $body['ticket'] ?? '';
        $cookie = $_COOKIE['kc_hp_session'] ?? '';
        if (!is_string($ticket) || !is_string($cookie) || !preg_match('/^[a-f0-9]{64}$/D', $cookie)
            || !preg_match('/^(\d{10}):([a-f0-9]{32}):([a-f0-9]{64})$/D', $ticket, $parts)
            || (int) $parts[1] > time() || (int) $parts[1] < time() - 7200
            || !hash_equals(hash_hmac('sha256', $parts[1] . ':' . $parts[2] . ':' . $cookie, wp_salt('nonce')), $parts[3])) {
            return new WP_Error('kc_ticket', 'Die Formularsitzung ist abgelaufen. Bitte neu laden.', ['status' => 403]);
        }
        if (!empty($body['website'])) { return new WP_Error('kc_spam', 'Die Anfrage konnte nicht angenommen werden.', ['status' => 400]); }
        return $body;
    }
    public static function validate_selection($selection) {
        if (!is_array($selection) || !is_string($selection['anreise'] ?? null) || !is_string($selection['abreise'] ?? null)
            || !preg_match('/^\d{4}-\d{2}-\d{2}$/D', $selection['anreise']) || !preg_match('/^\d{4}-\d{2}-\d{2}$/D', $selection['abreise'])
            || !is_array($selection['positionen'] ?? null) || count($selection['positionen']) < 1 || count($selection['positionen']) > 200) {
            return new WP_Error('kc_selection', 'Bitte Zeitraum und Mietbereiche auswählen.', ['status' => 400]);
        }
        $positions = []; $ids = [];
        foreach ($selection['positionen'] as $p) {
            if (!is_array($p) || !is_int($p['mietbereichId'] ?? null) || $p['mietbereichId'] < 1
                || !is_int($p['anzahl'] ?? null) || $p['anzahl'] < 1 || $p['anzahl'] > 10000 || isset($ids[$p['mietbereichId']])) {
                return new WP_Error('kc_selection', 'Bitte gültige Mengen ohne doppelte Mietbereiche auswählen.', ['status' => 400]);
            }
            $ids[$p['mietbereichId']] = true;
            $positions[] = ['mietbereichId' => $p['mietbereichId'], 'anzahl' => $p['anzahl']];
        }
        return ['anreise' => $selection['anreise'], 'abreise' => $selection['abreise'], 'positionen' => $positions];
    }
    public static function verfuegbarkeit($request) {
        $body = self::payload($request); if (is_wp_error($body)) { return $body; }
        if (!KC_HP_Queue::rate('availability', 60, 600)) { return new WP_Error('kc_rate', 'Zu viele Prüfungen. Bitte kurz warten.', ['status' => 429]); }
        $selection = self::validate_selection($body['auswahl'] ?? null);
        if (is_wp_error($selection)) { return $selection; }
        $result = KC_HP_Api::call('verfuegbarkeit', $selection);
        return is_wp_error($result) ? $result : self::no_cache(array_intersect_key($result, array_flip(['verfuegbar', 'hinweis'])));
    }
    public static function validate_submission(array $body) {
        $selection = self::validate_selection($body['auswahl'] ?? null);
        if (is_wp_error($selection)) { return $selection; }
        $result = ['auswahl' => $selection];
        foreach (['vorname' => 100, 'name' => 100, 'telefon' => 80, 'strasse' => 200, 'plz' => 20, 'ort' => 120, 'countryCode' => 2,
            'veranstalter' => 500, 'bemerkung' => 3000] as $key => $max) {
            if (!is_string($body[$key] ?? '')) { return new WP_Error('kc_input', 'Ungültige Eingabe.', ['status' => 400]); }
            $text = $key === 'bemerkung' ? sanitize_textarea_field($body[$key] ?? '') : sanitize_text_field($body[$key] ?? '');
            if (($text === '' && !in_array($key, ['veranstalter', 'bemerkung'], true)) || (function_exists('mb_strlen') ? mb_strlen($text) : strlen($text)) > $max) {
                return new WP_Error('kc_input', 'Bitte alle Pflichtfelder innerhalb der angegebenen Länge ausfüllen.', ['status' => 400]);
            }
            $result[$key] = $text;
        }
        if (!is_string($body['email'] ?? null) || !is_email($body['email']) || strlen($body['email']) > 254
            || !preg_match('/^[A-Z]{2}$/D', $result['countryCode']) || !is_string($body['anfrageId'] ?? null)
            || !preg_match('/^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}$/Di', $body['anfrageId'])
            || !is_int($body['konfigurationVersion'] ?? null) || $body['konfigurationVersion'] < 0
            || ($body['hausregelnAkzeptiert'] ?? false) !== true || ($body['datenschutzGelesen'] ?? false) !== true) {
            return new WP_Error('kc_input', 'Bitte E-Mail-Adresse und Hinweise prüfen und alle Pflichtfelder ausfüllen.', ['status' => 400]);
        }
        foreach (['anzahlJugendliche', 'anzahlErwachsene', 'jugendAlterVon', 'jugendAlterBis'] as $key) {
            $value = $body[$key] ?? null;
            if ($value !== null && (!is_int($value) || $value < 0 || $value > (str_starts_with($key, 'jugendAlter') ? 120 : 100000))) {
                return new WP_Error('kc_input', 'Bitte Teilnehmerzahlen und Alter prüfen.', ['status' => 400]);
            }
            $result[$key] = $value;
        }
        $result['email'] = $body['email']; $result['anfrageId'] = strtolower($body['anfrageId']);
        $result['konfigurationVersion'] = $body['konfigurationVersion'];
        $result['hausregelnAkzeptiert'] = true; $result['datenschutzGelesen'] = true;
        return $result;
    }
    public static function anfrage($request) {
        $body = self::payload($request); if (is_wp_error($body)) { return $body; }
        if (!KC_HP_Queue::rate('submit', 5, 1800)) { return new WP_Error('kc_rate', 'Zu viele Anfragen. Bitte später erneut versuchen.', ['status' => 429]); }
        $data = self::validate_submission($body); if (is_wp_error($data)) { return $data; }
        if (KC_HP_Api::settings()['mode'] === 'preview') {
            return self::no_cache(['status' => 'preview', 'message' => 'Vorschau: Es wurde keine Anfrage gespeichert und keine E-Mail versendet.']);
        }
        $id = KC_HP_Queue::enqueue($data); if (is_wp_error($id)) { return $id; }
        KC_HP_Queue::process($id);
        $result = KC_HP_Queue::response($id);
        return self::no_cache($result, $result['status'] === 'pending' ? 202 : ($result['status'] === 'failed' ? 409 : 200));
    }
    public static function shortcode(): string {
        $mode = KC_HP_Api::settings()['mode'];
        if ($mode !== 'live' && !($mode === 'preview' && current_user_can('manage_options'))) {
            return '<p>Die Online-Buchungsanfrage ist derzeit nicht freigeschaltet. Bitte nutze die Kontaktmöglichkeiten auf dieser Seite.</p>';
        }
        // Block-Themes können Shortcodes vor wp_enqueue_scripts rendern.
        self::assets();
        wp_enqueue_style('kc-hp'); wp_enqueue_script('kc-hp');
        wp_localize_script('kc-hp', 'kcHomepage', ['endpoint' => rest_url('kc-buchungsanfrage/v1/'),
            'nonce' => current_user_can('manage_options') ? wp_create_nonce('wp_rest') : '']);
        ob_start(); require __DIR__ . '/form.php'; return ob_get_clean();
    }
    public static function test(): void {
        if (!current_user_can('manage_options')) { wp_die('Kein Zugriff.'); }
        check_admin_referer('kc_hp_test');
        $result = KC_HP_Api::call('katalog');
        set_transient('kc_hp_test_' . get_current_user_id(), is_wp_error($result)
            ? ['ok' => false, 'message' => $result->get_error_message()]
            : ['ok' => true, 'message' => 'KC-Verbindung erfolgreich. Mietobjekt: ' . ($result['mietobjekt'] ?? '') . '. Es wurde keine Anfrage angelegt.'], 60);
        wp_safe_redirect(admin_url('options-general.php?page=kc-buchungsanfrage')); exit;
    }
    public static function retry(): void {
        global $wpdb;
        if (!current_user_can('manage_options')) { wp_die('Kein Zugriff.'); }
        check_admin_referer('kc_hp_retry');
        $id = absint($_POST['id'] ?? 0);
        // Bestätigte Anfragen behalten ihren Zustand: nur ausstehende Mails werden erneut bearbeitet.
        $wpdb->query($wpdb->prepare('UPDATE ' . KC_HP_Queue::table()
            . " SET status=IF(status='confirmed','confirmed','pending'), attempts=0, next_attempt=0 WHERE id=%d AND lease_until<%d", $id, time()));
        KC_HP_Queue::process($id);
        wp_safe_redirect(admin_url('options-general.php?page=kc-buchungsanfrage')); exit;
    }
    public static function resend(): void {
        global $wpdb;
        if (!current_user_can('manage_options')) { wp_die('Kein Zugriff.'); }
        check_admin_referer('kc_hp_resend');
        $id = absint($_POST['id'] ?? 0);
        $table = KC_HP_Queue::table();
        $row = $wpdb->get_row($wpdb->prepare("SELECT * FROM $table WHERE id=%d AND status='confirmed' AND lease_until<%d", $id, time()));
        if ($row) {
            $result = json_decode($row->result_json, true);
            // Ausschließlich E-Mails erneut anstoßen. Status und Anfrage-ID bleiben bestätigt.
            $wpdb->update($table, ['guest_mail' => !empty($result['gastBestaetigung']) ? 'pending' : 'off',
                'admin_mail' => !empty($result['verwaltungsBenachrichtigung']) ? 'pending' : 'off', 'next_attempt' => 0], ['id' => $id]);
            KC_HP_Queue::process($id);
        }
        wp_safe_redirect(admin_url('options-general.php?page=kc-buchungsanfrage')); exit;
    }
    public static function mail_diagnostic(?string $json): string {
        if (!$json) { return 'Versanddetails beim bisherigen Aufruf noch nicht erfasst.'; }
        $info = json_decode($json, true);
        if (!is_array($info)) { return 'Versanddetails nicht lesbar.'; }
        $transport = ['smtp' => 'SMTP', 'mail' => 'PHP mail()', 'sendmail' => 'Sendmail', 'qmail' => 'Qmail'];
        $text = $transport[$info['transport'] ?? ''] ?? 'Versandweg durch Plugin oder Filter; Standard-Mailer nicht beobachtet';
        if (!empty($info['from'])) { $text .= ' · Absender: ' . $info['from']; }
        if (!empty($info['error_code'])) { $text .= ' · Fehlercode: ' . $info['error_code']; }
        return $text;
    }

    public static function admin(): void {
        global $wpdb;
        if (!current_user_can('manage_options')) { return; }
        $s = KC_HP_Api::settings();
        echo '<div class="wrap"><h1>KC Buchungsanfrage</h1>';
        settings_errors();
        $notice = get_transient('kc_hp_test_' . get_current_user_id());
        if ($notice) {
            delete_transient('kc_hp_test_' . get_current_user_id());
            echo '<div class="notice ' . ($notice['ok'] ? 'notice-success' : 'notice-error') . '"><p>' . esc_html($notice['message']) . '</p></div>';
        }
        echo '<p>Einbindung im Classic Editor oder Shortcode-Block: <code>[kc_buchungsanfrage]</code>. Zunächst Vorschau wählen. Im Vorschaumodus sind Formular und lesende KC-Prüfungen nur für Administratoren verfügbar; es entstehen keine Buchungen und E-Mails.</p>';
        echo '<form method="post" action="options.php">'; settings_fields('kc_hp');
        echo '<table class="form-table"><tr><th><label for="kc-mode">Betriebsmodus</label></th><td><select id="kc-mode" name="kc_hp_settings[mode]">';
        foreach (['off' => 'Deaktiviert', 'preview' => 'Vorschau nur für Administratoren', 'live' => 'Öffentlich freigeschaltet'] as $value => $label) {
            echo '<option value="' . esc_attr($value) . '" ' . selected($s['mode'], $value, false) . '>' . esc_html($label) . '</option>';
        }
        echo '</select></td></tr>';
        foreach (['api_url' => 'KC-API-Basisadresse (einschließlich /api)', 'token_url' => 'Keycloak-Token-Adresse', 'client_id' => 'Technische Client-ID', 'object_id' => 'Mietobjekt-ID'] as $key => $label) {
            echo '<tr><th><label for="kc-' . esc_attr($key) . '">' . esc_html($label) . '</label></th><td><input class="regular-text" id="kc-' . esc_attr($key) . '" name="kc_hp_settings[' . esc_attr($key) . ']" type="' . ($key === 'object_id' ? 'number' : (str_ends_with($key, 'url') ? 'url' : 'text')) . '" value="' . esc_attr((string) $s[$key]) . '" /></td></tr>';
        }
        echo '<tr><th><label for="kc-secret">Client-Secret</label></th><td><input class="regular-text" id="kc-secret" type="password" autocomplete="new-password" name="kc_hp_settings[client_secret]" value="" /><p class="description">Leer lassen, um das gespeicherte Secret zu behalten. Status: ' . ((defined('KC_HOMEPAGE_CLIENT_SECRET') || $s['client_secret']) ? 'hinterlegt' : 'fehlt') . '. Alternativ Konstante KC_HOMEPAGE_CLIENT_SECRET in wp-config.php setzen. Secrets werden nie angezeigt.</p></td></tr></table>';
        submit_button('Einstellungen speichern'); echo '</form>';
        echo '<form method="post" action="' . esc_url(admin_url('admin-post.php')) . '"><input type="hidden" name="action" value="kc_hp_test" />';
        wp_nonce_field('kc_hp_test'); submit_button('Verbindung zu KC prüfen (ohne Anfrage)', 'secondary'); echo '</form>';
        echo '<h2>Übertragung und E-Mail-Versand</h2><p>„sent“ bedeutet an den WordPress-Maildienst übergeben, nicht nachgewiesen zugestellt. Wiederholungen verwenden dieselbe Anfrage-ID. Die Warteschlange arbeitet über WP-Cron; bei wenig Seitenaufrufen einen externen Cron einrichten. Deaktivierter Modus pausiert auch die Warteschlange.</p>';
        $rows = $wpdb->get_results('SELECT id,request_id,status,guest_mail,admin_mail,guest_mail_info,admin_mail_info,attempts,last_error,created_at FROM ' . KC_HP_Queue::table() . ' ORDER BY id DESC LIMIT 50');
        echo '<table class="widefat striped"><thead><tr><th>Vorgang / Eingang (UTC)</th><th>KC-Status</th><th>Gast / Verwaltung</th><th>Hinweis</th><th>Aktion</th></tr></thead><tbody>';
        foreach ($rows as $row) {
            echo '<tr><td>' . esc_html($row->request_id) . '<br>' . esc_html($row->created_at) . '</td><td>' . esc_html($row->status) . '</td><td>' . esc_html($row->guest_mail . ' / ' . $row->admin_mail) . '</td><td>' . esc_html($row->last_error ?? '') . '</td><td>';
            if ($row->status === 'confirmed') {
                echo '<p><small>Gast: ' . esc_html(self::mail_diagnostic($row->guest_mail_info)) . '<br>Verwaltung: '
                    . esc_html(self::mail_diagnostic($row->admin_mail_info)) . '</small></p>';
                if ($row->guest_mail !== 'off' || $row->admin_mail !== 'off') {
                    echo '<form method="post" action="' . esc_url(admin_url('admin-post.php')) . '"><input type="hidden" name="action" value="kc_hp_resend" /><input type="hidden" name="id" value="' . (int) $row->id . '" />';
                    wp_nonce_field('kc_hp_resend'); echo '<button class="button">E-Mails erneut versenden</button><p class="description">Kann zu doppelten Nachrichten führen. Legt keine neue KC-Anfrage an.</p></form>';
                }
            }
            if ($row->status !== 'confirmed' || $row->guest_mail === 'pending' || $row->admin_mail === 'pending') {
                echo '<form method="post" action="' . esc_url(admin_url('admin-post.php')) . '"><input type="hidden" name="action" value="kc_hp_retry" /><input type="hidden" name="id" value="' . (int) $row->id . '" />';
                wp_nonce_field('kc_hp_retry'); echo '<button class="button">Erneut bearbeiten</button></form>';
            }
            echo '</td></tr>';
        }
        if (!$rows) { echo '<tr><td colspan="5">Noch keine Vorgänge vorhanden.</td></tr>'; }
        echo '</tbody></table>';
        echo '<h2>Statusmails aus KC</h2><p>Statusmails werden jetzt direkt vom KanuControl-Server über dessen SMTP-Konfiguration versendet. WordPress verarbeitet keine KC-Statusmail-Aufträge mehr. Die vorhandene Tabelle mit älteren Aufträgen bleibt zur Rückwärtskompatibilität erhalten.</p>';
        echo '<p>Abgeschlossene lokale Kopien werden nach 30 Tagen entfernt. Ausstehende und angehaltene Vorgänge bleiben zur Prüfung erhalten. Zugangsdaten in Datenbanksicherungen entsprechend schützen. Die aktuelle Anleitung liegt im Plugin als README.md.</p></div>';
    }
}
