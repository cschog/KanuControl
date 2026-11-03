<?php
if (!defined('ABSPATH')) { exit; }
final class KC_HP_Api {
    public static function settings(): array {
        return array_merge(['mode' => 'off', 'api_url' => '', 'token_url' => '', 'client_id' => '',
            'client_secret' => '', 'object_id' => 0], (array) get_option('kc_hp_settings', []));
    }
    public static function https_url(string $value): string {
        $value = trim($value);
        $parts = wp_parse_url($value);
        return is_array($parts) && strtolower($parts['scheme'] ?? '') === 'https'
            && !empty($parts['host']) && !isset($parts['user']) && !isset($parts['pass'])
            && !isset($parts['query']) && !isset($parts['fragment']) ? rtrim($value, '/') : '';
    }
    public static function target(): string {
        $s = self::settings();
        return hash('sha256', wp_json_encode([$s['api_url'], $s['token_url'], $s['client_id'], (int) $s['object_id']]));
    }
    private static function token(bool $refresh = false) {
        $s = self::settings();
        $secret = defined('KC_HOMEPAGE_CLIENT_SECRET') ? KC_HOMEPAGE_CLIENT_SECRET : $s['client_secret'];
        if (!$s['client_id'] || !$secret || !self::https_url($s['token_url'])) {
            return new WP_Error('kc_config', 'Der technische KC-Zugang ist noch nicht eingerichtet.', ['status' => 503]);
        }
        $key = 'kc_hp_token_' . hash('sha256', self::target() . $secret);
        if (!$refresh && ($token = get_transient($key))) { return $token; }
        $result = wp_safe_remote_post($s['token_url'], ['timeout' => 12, 'redirection' => 0, 'sslverify' => true,
            'limit_response_size' => 65536, 'body' => ['grant_type' => 'client_credentials', 'client_id' => $s['client_id'], 'client_secret' => $secret]]);
        if (is_wp_error($result) || wp_remote_retrieve_response_code($result) !== 200) {
            return new WP_Error('kc_login', 'Die Verbindung zu KC konnte nicht authentifiziert werden.', ['status' => 503]);
        }
        $body = json_decode(wp_remote_retrieve_body($result), true);
        if (empty($body['access_token']) || !is_string($body['access_token'])) {
            return new WP_Error('kc_login', 'Die Anmeldung bei KC ist fehlgeschlagen.', ['status' => 503]);
        }
        $ttl = max(1, min(300, (int) ($body['expires_in'] ?? 60) - 30));
        set_transient($key, $body['access_token'], $ttl);
        return $body['access_token'];
    }
    public static function call(string $endpoint, ?array $body = null, bool $refresh = false) {
        $s = self::settings();
        $allowed = in_array($endpoint, ['katalog', 'verfuegbarkeit', 'anfragen', 'nachrichten', 'kalender'], true)
            || preg_match('~^nachrichten/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}/quittieren$~Di', $endpoint);
        if (!$allowed
            || !self::https_url($s['api_url']) || (int) $s['object_id'] < 1) {
            return new WP_Error('kc_config', 'Die KC-Verbindung ist noch nicht vollständig eingerichtet.', ['status' => 503]);
        }
        $token = self::token($refresh);
        if (is_wp_error($token)) { return $token; }
        $url = $s['api_url'] . '/integrationen/homepage/objekte/' . (int) $s['object_id'] . '/' . $endpoint;
        $args = ['method' => $body === null ? 'GET' : 'POST', 'timeout' => 15, 'redirection' => 0,
            'sslverify' => true, 'limit_response_size' => 256000,
            'headers' => ['Authorization' => 'Bearer ' . $token, 'Content-Type' => 'application/json', 'Accept' => 'application/json']];
        if ($body !== null) { $args['body'] = wp_json_encode($body); }
        $response = wp_safe_remote_request($url, $args);
        if (is_wp_error($response)) {
            return new WP_Error('kc_network', 'KC ist gerade nicht erreichbar. Die Übertragung kann wiederholt werden.', ['status' => 503]);
        }
        $status = wp_remote_retrieve_response_code($response);
        if ($status === 401 && !$refresh) { return self::call($endpoint, $body, true); }
        $data = json_decode(wp_remote_retrieve_body($response), true);
        if ($status < 200 || $status >= 300) {
            $message = is_array($data) && is_string($data['message'] ?? null)
                ? sanitize_text_field($data['message']) : 'KC hat die Anfrage abgelehnt. Bitte die Verbindung bzw. Eingaben prüfen.';
            return new WP_Error('kc_rejected', $message, ['status' => $status >= 400 && $status <= 599 ? $status : 502]);
        }
        if (!is_array($data) || !isset($data['data']) || !is_array($data['data'])) {
            return new WP_Error('kc_response', 'Die Antwort von KC konnte nicht verarbeitet werden.', ['status' => 502]);
        }
        return $data['data'];
    }
    public static function public_catalog(array $catalog): array {
        return array_intersect_key($catalog, array_flip(['konfigurationVersion', 'bezeichnung', 'mietobjekt',
            'anreisezeit', 'abreisezeit', 'datenschutzUrl', 'hausregelnUrl', 'teilnehmerPflicht', 'bereiche']));
    }
}
