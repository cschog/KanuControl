<?php
define('WP_INSTALLING', true);
require '/var/www/html/wp-load.php';
require_once ABSPATH . 'wp-admin/includes/upgrade.php';
if (!is_blog_installed()) { wp_install('KC – lokale Plugin-Prüfung', 'localadmin', 'admin@example.test', false, '', 'local-test-only'); }
update_option('siteurl', 'http://127.0.0.1:18765'); update_option('home', 'http://127.0.0.1:18765');
require_once ABSPATH . 'wp-admin/includes/plugin.php';
activate_plugin('kc-buchungsanfrage/kc-buchungsanfrage.php');
update_option('kc_hp_settings', ['mode' => 'live', 'api_url' => 'https://kc.invalid/api', 'token_url' => 'https://login.invalid/token', 'client_id' => 'verein-homepage', 'client_secret' => 'local-test-only', 'object_id' => 7], false);
if (!get_page_by_path('anfrage')) { wp_insert_post(['post_title' => 'Bootshaus anfragen – lokale Vorschau', 'post_name' => 'anfrage', 'post_status' => 'publish', 'post_type' => 'page', 'post_content' => '[kc_belegungskalender][kc_buchungsanfrage]']); }
echo 'WordPress ' . get_bloginfo('version') . ', PHP ' . PHP_VERSION . "\n";
echo 'Testseite: http://127.0.0.1:18765/?p=' . get_page_by_path('anfrage')->ID . "\n";
