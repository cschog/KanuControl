<?php
/**
 * Plugin Name: KC Buchungsanfrage
 * Description: Vereinsneutrales Anfrageformular mit Verfügbarkeitsprüfung und Übernahme in KanuControl.
 * Version: 0.1.11
 * Requires at least: 6.5
 * Requires PHP: 8.2
 * Text Domain: kc-buchungsanfrage
 */
if (!defined('ABSPATH')) { exit; }
define('KC_HP_VERSION', '0.1.11');
define('KC_HP_FILE', __FILE__);
require_once __DIR__ . '/includes/class-kc-hp-api.php';
require_once __DIR__ . '/includes/class-kc-hp-queue.php';
require_once __DIR__ . '/includes/class-kc-hp-status-mails.php';
require_once __DIR__ . '/includes/class-kc-hp-plugin.php';
require_once __DIR__ . '/includes/class-kc-hp-calendar.php';
register_activation_hook(__FILE__, ['KC_HP_Queue', 'install']);
register_deactivation_hook(__FILE__, static function () { wp_clear_scheduled_hook('kc_hp_work'); });
add_filter('cron_schedules', static function ($schedules) {
    $schedules['kc_hp_minute'] = ['interval' => 60, 'display' => 'KC-Anfragen prüfen'];
    return $schedules;
});
add_action('kc_hp_work', ['KC_HP_Queue', 'work']);
add_action('plugins_loaded', ['KC_HP_Plugin', 'init']);
