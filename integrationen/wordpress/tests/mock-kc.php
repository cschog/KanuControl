<?php
add_filter('pre_http_request', function ($pre, $args, $url) {
    if (!str_contains($url, '.invalid/')) return new WP_Error('blocked', 'Netzwerkzugriff im Test blockiert.');
    $reply = function ($data, $status = 200) { return ['headers' => [], 'body' => wp_json_encode($data), 'response' => ['code' => $status, 'message' => 'Test'], 'cookies' => []]; };
    if (str_ends_with($url, '/token')) return $reply(['access_token' => 'local-test-token', 'expires_in' => 300]);
    if (str_ends_with($url, '/nachrichten')) {
        $ack = get_option('kc_mock_status_acks', []);
        return $reply(['data' => array_values(array_filter(get_option('kc_mock_status_events', []), function ($event) use ($ack) { return empty($ack[$event['id']]); }))]);
    }
    if (preg_match('~/nachrichten/([a-f0-9-]{36})/quittieren$~', $url, $matches)) {
        if (get_option('kc_mock_status_ack_fail')) { return new WP_Error('network', 'Simulierter Quittierungsfehler'); }
        $ack = get_option('kc_mock_status_acks', []); $ack[$matches[1]] = true; update_option('kc_mock_status_acks', $ack, false);
        return $reply(['data' => ['quittiert' => true]]);
    }
    if (str_ends_with($url, '/katalog')) return $reply(['data' => ['konfigurationVersion' => 2, 'bezeichnung' => 'Lokaler Test', 'mietobjekt' => 'Bootshaus – lokale Vorschau',
        'anreisezeit' => '16:00', 'abreisezeit' => '14:00', 'datenschutzUrl' => 'https://verein.invalid/datenschutz', 'hausregelnUrl' => 'https://verein.invalid/regeln',
        'teilnehmerPflicht' => true, 'bereiche' => [
            ['id' => 10, 'bezeichnung' => 'Bootshaus EG einschließlich Wiese', 'beschreibung' => 'Aufenthalt und gemeinsame Nutzung des Außenbereichs.', 'bestand' => 1, 'mengeneinheit' => null, 'berechnungsart' => 'TAG', 'einzelpreis' => 300, 'erforderlicheIds' => []],
            ['id' => 11, 'bezeichnung' => 'Schlafräume und Bad im OG', 'beschreibung' => 'Als Erweiterung zum Erdgeschoss.', 'bestand' => 1, 'mengeneinheit' => null, 'berechnungsart' => 'TAG', 'einzelpreis' => 150, 'erforderlicheIds' => [10]],
            ['id' => 12, 'bezeichnung' => 'Gruppengrill', 'beschreibung' => '', 'bestand' => 3, 'mengeneinheit' => 'Stück', 'berechnungsart' => 'PAUSCHAL', 'einzelpreis' => 15, 'erforderlicheIds' => []],
        ]]]);
    if (str_ends_with($url, '/kalender')) {
        $input = json_decode($args['body'], true); $days = [];
        $count = (int) (new DateTimeImmutable($input['monat'] . '-01'))->format('t');
        for ($i = 1; $i <= $count; $i++) $days[] = ['datum' => $input['monat'] . '-' . sprintf('%02d', $i),
            'status' => $i === 2 ? 'BELEGT' : ($i === 3 ? 'RESERVIERT' : 'FREI'), 'verfuegbar' => $i === 2 || $i === 3 ? 0 : 1,
            'buchungsnummern' => $i === 2 ? ['2027-001'] : ($i === 3 ? ['2027-002'] : []), 'email' => 'niemals-oeffentlich@example.test'];
        return $reply(['data' => ['monat' => $input['monat'], 'mietbereichId' => $input['mietbereichId'], 'tage' => $days]]);
    }
    if (str_ends_with($url, '/verfuegbarkeit')) return $reply(['data' => ['verfuegbar' => true, 'hinweis' => 'Der Zeitraum ist verfügbar. Eine Anfrage reserviert noch keinen Termin.']]);
    if (str_ends_with($url, '/anfragen')) {
        if (get_option('kc_mock_network')) return new WP_Error('network', 'Simulierter Netzwerkausfall');
        $payload = json_decode($args['body'], true); $id = $payload['anfrageId'];
        $records = get_option('kc_mock_records', []);
        if (!isset($records[$id])) {
            $n = count($records) + 1;
            $records[$id] = ['buchungId' => $n, 'buchungsnummer' => 'TEST-' . $n, 'status' => 'ANFRAGE',
                'gastBestaetigung' => true, 'verwaltungsBenachrichtigung' => true, 'empfaengerEmail' => 'verwaltung@example.test'];
            update_option('kc_mock_records', $records, false);
        }
        return $reply(['data' => $records[$id]]);
    }
    return $reply(['message' => 'Nicht implementierter Testpfad'], 404);
}, 10, 3);
add_filter('pre_wp_mail', function ($pre, $args) {
    if (get_option('kc_mock_mail_fail') && str_contains($args['to'], 'gast@')) { update_option('kc_mock_mail_fail', false); return false; }
    $log = get_option('kc_mock_mails', []); $log[] = ['to' => $args['to'], 'subject' => $args['subject'], 'message' => $args['message'], 'headers' => $args['headers']];
    update_option('kc_mock_mails', $log, false); return true;
}, 10, 2);
