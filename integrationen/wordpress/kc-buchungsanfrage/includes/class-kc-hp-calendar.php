<?php
if (!defined('ABSPATH')) { exit; }
final class KC_HP_Calendar {
    public static function init(): void {
        add_shortcode('kc_belegungskalender', [self::class, 'shortcode']);
        add_action('rest_api_init', static function () {
            register_rest_route('kc-buchungsanfrage/v1', '/kalender', ['methods' => 'POST',
                'permission_callback' => ['KC_HP_Plugin', 'permission'], 'callback' => [self::class, 'read']]);
        });
    }
    public static function shortcode($atts): string {
        $mode = KC_HP_Api::settings()['mode'];
        if ($mode === 'off' || ($mode === 'preview' && !current_user_can('manage_options'))) return '<p>Der Belegungskalender ist derzeit nicht freigeschaltet.</p>';
        $atts = shortcode_atts(['anfrage_url' => get_permalink()], $atts, 'kc_belegungskalender');
        $url = esc_url_raw((string) $atts['anfrage_url'], ['https', 'http']);
        if (!$url) $url = get_permalink();
        wp_enqueue_style('kc-hp-calendar', plugins_url('assets/calendar.css', KC_HP_FILE), [], KC_HP_VERSION);
        wp_enqueue_script('kc-hp-calendar', plugins_url('assets/calendar.js', KC_HP_FILE), [], KC_HP_VERSION, true);
        wp_localize_script('kc-hp-calendar', 'kcCalendar', ['endpoint' => rest_url('kc-buchungsanfrage/v1/kalender'),
            'nonce' => current_user_can('manage_options') ? wp_create_nonce('wp_rest') : '']);
        return '<section class="kc-calendar" data-anfrage-url="' . esc_url($url) . '" aria-label="Belegungskalender">'
            . '<h2>Belegungskalender</h2>'
            . '<div class="kc-calendar-navigation"><button type="button" class="kc-calendar-prev" aria-label="Vorheriger Monat">←</button>'
            . '<h3 class="kc-calendar-month"></h3><button type="button" class="kc-calendar-next" aria-label="Nächster Monat">→</button></div>'
            . '<p class="kc-calendar-message" role="status" aria-live="polite">Belegung wird geladen …</p><div class="kc-calendar-grid"></div>'
            . '<p class="kc-calendar-legend"><span data-status="FREI">Frei</span> · <span data-status="RESERVIERT">Reserviert</span> · <span data-status="BELEGT">Belegt</span> · <span data-status="GESPERRT">Gesperrt</span></p>'
            . '<p class="kc-calendar-note"></p><p>Ein Klick auf einen freien Tag öffnet die Buchungsanfrage. Eine Anfrage reserviert noch keinen Termin. Die Verfügbarkeit des gesamten Aufenthalts wird im Formular erneut geprüft.</p></section>';
    }
    public static function read($request) {
        if (strlen($request->get_body()) > 2000) return new WP_Error('kc_size', 'Ungültige Kalenderabfrage.', ['status' => 413]);
        if (!KC_HP_Queue::rate('kalender', 60, 600)) return new WP_Error('kc_rate', 'Bitte später erneut versuchen.', ['status' => 429]);
        $body = $request->get_json_params();
        if (!is_array($body) || !is_string($body['monat'] ?? null) || !preg_match('/^(20[0-9]{2}|2100)-(0[1-9]|1[0-2])$/D', $body['monat']))
            return new WP_Error('kc_month', 'Bitte einen gültigen Monat wählen.', ['status' => 400]);
        $catalog = KC_HP_Api::call('katalog');
        if (is_wp_error($catalog)) return $catalog;
        $areas = $catalog['bereiche'] ?? [];
        // Dieser Bootshauskalender zeigt ausschließlich das EG, unabhängig von der Katalogreihenfolge.
        $eg = array_values(array_filter($areas, static function ($area) {
            $name = strtolower(trim(preg_replace('/\s+/u', ' ', (string) ($area['bezeichnung'] ?? ''))));
            return $name === 'erdgeschoss mit wiese';
        }));
        if (count($eg) !== 1) {
            return new WP_Error('kc_eg', 'Der Mietbereich Erdgeschoss mit Wiese ist nicht eindeutig freigegeben. Bitte die KC-Konfiguration prüfen.', ['status' => 503]);
        }
        $id = $eg[0]['id'] ?? null;
        if (!is_int($id) || !in_array($id, array_column($areas, 'id'), true))
            return new WP_Error('kc_area', 'Mietbereich nicht freigegeben.', ['status' => 403]);
        $data = KC_HP_Api::call('kalender', ['monat' => $body['monat'], 'mietbereichId' => $id]);
        if (is_wp_error($data)) return $data;
        // Öffentlicher Kalender: freigegebener Mietername, keine Kontakt-/Bankdaten oder internen Buchungs-IDs.
        $days = [];
        foreach ($data['tage'] ?? [] as $day) {
            $public = array_intersect_key($day, array_flip(['datum', 'status', 'verfuegbar', 'buchungsnummern']));
            if (isset($day['belegungen']) && is_array($day['belegungen'])) {
                $public['belegungen'] = array_map(static function ($item) {
                    $public = array_intersect_key(is_array($item) ? $item : [], array_flip(['von', 'bis', 'status', 'buchungsnummer', 'mietername']));
                    if (($public['status'] ?? '') === 'GESPERRT') unset($public['mietername']);
                    return $public;
                }, $day['belegungen']);
            }
            $days[] = $public;
        }
        $response = new WP_REST_Response(['catalog' => KC_HP_Api::public_catalog($catalog),
            'monat' => $data['monat'], 'mietbereichId' => $id, 'tage' => $days]);
        $response->header('Cache-Control', 'no-store, private');
        return $response;
    }
}
