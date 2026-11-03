import json, urllib.request, urllib.error, http.cookiejar, uuid, subprocess
BASE='http://127.0.0.1:18765'
ROOT=BASE+'/?rest_route=/kc-buchungsanfrage/v1/'
jar=http.cookiejar.CookieJar(); client=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
checks=0
def php(code):
    p=subprocess.run(['docker','exec','kc-hp-test-wp','php','-r',"require '/var/www/html/wp-load.php'; "+code],capture_output=True,text=True,check=True)
    return p.stdout.strip()
def post(path, body, origin=True):
    headers={'Content-Type':'application/json'}
    if origin: headers['Origin']=BASE
    req=urllib.request.Request(ROOT+path,data=json.dumps(body).encode(),headers=headers)
    try:
        r=client.open(req); return r.status,json.load(r)
    except urllib.error.HTTPError as e: return e.code,json.load(e)
def check(condition,label):
    global checks
    assert condition,label
    checks+=1; print('PASS',label,flush=True)
def state():
    return json.loads(php("echo wp_json_encode(['records'=>count(get_option('kc_mock_records',[])), 'mails'=>count(get_option('kc_mock_mails',[])), 'rows'=>$wpdb->get_results('SELECT id,status,guest_mail,admin_mail FROM '.KC_HP_Queue::table())]);"))
php("$wpdb->query('DELETE FROM '.KC_HP_Queue::table()); $wpdb->query('DELETE FROM '.KC_HP_Queue::limits()); delete_option('kc_mock_records'); delete_option('kc_mock_mails'); delete_option('kc_mock_network'); delete_option('kc_mock_mail_fail'); delete_option('kc_mock_status_events'); delete_option('kc_mock_status_acks'); delete_option('kc_mock_status_ack_fail'); $wpdb->query('DELETE FROM '.KC_HP_Status_Mails::table()); $s=KC_HP_Api::settings(); $s['mode']='live'; $s['api_url']='https://kc.invalid/api'; update_option('kc_hp_settings',$s);")
check(post('session',{},False)[0]==403,'Fremder bzw. fehlender Ursprung wird abgewiesen')
status,session=post('session',{});check(status==200,'Formularsitzung und Katalog werden geladen')
ticket=session['ticket'];check(any(c.name=='kc_hp_session' for c in jar),'Sitzungscookie gesetzt')
sel={'anreise':'2027-06-10','abreise':'2027-06-12','positionen':[{'mietbereichId':10,'anzahl':1}]}
check(post('verfuegbarkeit',{'auswahl':sel})[0]==403,'Fehlendes Sitzungsticket wird abgewiesen')
check(post('verfuegbarkeit',{'ticket':ticket,'auswahl':sel})[1]['verfuegbar'] is True,'Verfügbarkeit wird geprüft')
def payload():
    return {'ticket':ticket,'anfrageId':str(uuid.uuid4()),'konfigurationVersion':2,'auswahl':sel,'vorname':'Test','name':'Gast','email':'gast@example.test','telefon':'0123456789','strasse':'Teststraße 1','plz':'12345','ort':'Testort','countryCode':'DE','veranstalter':'Schule','bemerkung':'Nur lokaler Test','anzahlJugendliche':8,'jugendAlterVon':12,'jugendAlterBis':16,'anzahlErwachsene':2,'hausregelnAkzeptiert':True,'datenschutzGelesen':True}
a=payload(); code,result=post('anfrage',a)
check(code==200 and result['status']=='confirmed','Anfrage erfolgreich übernommen')
check(state()['records']==1 and state()['mails']==2,'Eine Buchung und zwei getrennte E-Mails')
check(post('anfrage',a)[1]['buchungsnummer']==result['buchungsnummer'],'Wiederholung liefert dieselbe Buchungsnummer')
check(state()['records']==1 and state()['mails']==2,'Wiederholung erzeugt weder Buchung noch E-Mails doppelt')
a['name']='Geändert';check(post('anfrage',a)[0]==409,'Veränderte Daten mit derselben Vorgangs-ID werden abgewiesen')
# Eigene Grenzen für isolierte Testfälle zurücksetzen; im Produktivcode bleiben sie aktiv.
php("$wpdb->query('DELETE FROM '.KC_HP_Queue::limits()); update_option('kc_mock_network',true);")
b=payload(); code,result=post('anfrage',b);check(code==202 and result['status']=='pending','Netzausfall behält Anfrage in dauerhafter Warteschlange')
check(state()['records']==1 and state()['mails']==2,'Vor KC-Bestätigung keine Mails')
php("update_option('kc_mock_network',false); $wpdb->query('UPDATE '.KC_HP_Queue::table().' SET next_attempt=0'); KC_HP_Queue::work();")
check(state()['records']==2 and state()['mails']==4,'Wiederholung nach Netzausfall überträgt und versendet')
php("update_option('kc_mock_mail_fail',true);")
c=payload();check(post('anfrage',c)[1]['status']=='confirmed','Mailfehler ändert erfolgreiche Buchungsübernahme nicht')
s=state();check(s['records']==3 and s['mails']==5 and s['rows'][-1]['guest_mail']=='pending','Gast-Mail bleibt separat ausstehend')
php("$wpdb->query('UPDATE '.KC_HP_Queue::table().' SET next_attempt=0'); KC_HP_Queue::work();")
check(state()['records']==3 and state()['mails']==6,'Nur fehlgeschlagene Gast-Mail wird wiederholt')
php("update_option('kc_mock_network',true);")
d=payload();check(post('anfrage',d)[1]['status']=='pending','Weiterer Vorgang für Zielwechseltest gespeichert')
php("$s=KC_HP_Api::settings(); $s['api_url']='https://anderer-verein.invalid/api'; update_option('kc_hp_settings',$s); update_option('kc_mock_network',false); $wpdb->query('UPDATE '.KC_HP_Queue::table().' SET next_attempt=0'); KC_HP_Queue::work();")
check(state()['records']==3 and state()['rows'][-1]['status']=='failed','Zielwechsel kann alte Anfragen nicht in anderen Verein übertragen')
php("$s=KC_HP_Api::settings(); $s['api_url']='https://kc.invalid/api'; $s['mode']='preview'; update_option('kc_hp_settings',$s);")
check(post('session',{})[0]==403,'Vorschau ist nicht für Gäste zugänglich')
check(php("echo KC_HP_Api::https_url('http://unsicher.invalid')==='' && KC_HP_Api::https_url('https://user:secret@verein.invalid')==='' ? 'ok':'bad';")=='ok','Verbindungs-URLs erfordern HTTPS ohne eingebettete Zugangsdaten')
php("$s=KC_HP_Api::settings(); $s['mode']='live'; update_option('kc_hp_settings',$s);")
check(php("$wpdb->query('DELETE FROM '.KC_HP_Queue::limits()); echo KC_HP_Queue::rate('isoliert',2,60) && KC_HP_Queue::rate('isoliert',2,60) && !KC_HP_Queue::rate('isoliert',2,60) ? 'ok':'bad';")=='ok','Aufrufbegrenzung greift nach dem konfigurierten Limit')
preview = payload()
preview.pop('ticket')
code = "$s=KC_HP_Api::settings(); $s['mode']='preview'; update_option('kc_hp_settings',$s); wp_set_current_user(get_user_by('login','localadmin')->ID); $_SERVER['HTTP_ORIGIN']='http://127.0.0.1:18765'; $_COOKIE['kc_hp_session']=str_repeat('a',64); $t=time().':'.str_repeat('b',32); $p=json_decode(base64_decode('"+__import__('base64').b64encode(json.dumps(preview).encode()).decode()+"'),true); $p['ticket']=$t.':'.hash_hmac('sha256',$t.':'.$_COOKIE['kc_hp_session'],wp_salt('nonce')); $r=new WP_REST_Request('POST'); $r->set_header('content-type','application/json'); $r->set_body(wp_json_encode($p)); $before=$wpdb->get_var('SELECT COUNT(*) FROM '.KC_HP_Queue::table()); $m=count(get_option('kc_mock_mails',[])); $out=KC_HP_Plugin::anfrage($r); echo wp_json_encode(['allowed'=>KC_HP_Plugin::permission()===true,'status'=>$out->get_data()['status'],'unchanged'=>$before===$wpdb->get_var('SELECT COUNT(*) FROM '.KC_HP_Queue::table()) && $m===count(get_option('kc_mock_mails',[]))]); $s['mode']='live'; update_option('kc_hp_settings',$s);"
preview_result=json.loads(php(code))
check(preview_result=={'allowed':True,'status':'preview','unchanged':True},'Admin-Vorschau erzeugt weder Warteschlangeneinträge noch Mails')
before_resend=state()
php("wp_set_current_user(get_user_by('login','localadmin')->ID); $_POST['id']=$wpdb->get_var(\"SELECT id FROM \".KC_HP_Queue::table().\" WHERE status='confirmed' ORDER BY id LIMIT 1\"); $_REQUEST['_wpnonce']=wp_create_nonce('kc_hp_resend'); KC_HP_Plugin::resend();")
after_resend=state()
check(after_resend['records']==before_resend['records'] and after_resend['mails']==before_resend['mails']+2,'Erneuter Mailversand erzeugt keine zweite KC-Anfrage')
check(php("$row=$wpdb->get_row(\"SELECT guest_mail_info,admin_mail_info FROM \".KC_HP_Queue::table().\" WHERE status='confirmed' ORDER BY id LIMIT 1\"); echo $row->guest_mail_info && $row->admin_mail_info ? 'ok':'bad';")=='ok','Versanddiagnose wird für beide Empfänger gespeichert')
info=json.loads(php("add_filter('pre_wp_mail',function($pre,$args){do_action('phpmailer_init',(object)['From'=>'info@kanucontrol.de','Mailer'=>'smtp']);do_action('wp_mail_succeeded',$args);return true;},20,2); $m=new ReflectionMethod('KC_HP_Queue','mail');$r=$m->invoke(null,'gast@example.test','Lokaler Test','Test',[]);echo $r[1];"))
check(info['from']=='info@kanucontrol.de' and info['transport']=='smtp' and info['success_hook'] is True,'Tatsächlicher Absender und SMTP-Transport werden erfasst')
failed=json.loads(php("add_filter('pre_wp_mail',function(){do_action('wp_mail_failed',new WP_Error('local_smtp_failed','Nur simulierter Fehler'));return false;},20); $m=new ReflectionMethod('KC_HP_Queue','mail');$r=$m->invoke(null,'gast@example.test','Lokaler Test','Test',[]);echo wp_json_encode(['accepted'=>$r[0],'info'=>json_decode($r[1],true)]);"))
check(failed['accepted'] is False and failed['info']['error_code']=='local_smtp_failed','SMTP-Fehler wird ohne Zugangsdaten protokolliert')
# Statusänderungen werden unabhängig von der ursprünglichen Formular-Kopie zugestellt.
def status_event(status, number='TEST-STATUS'):
    return {'id':str(uuid.uuid4()),'buchungsnummer':number,'status':status,'empfaenger':'gast@example.test','replyTo':'verwaltung@example.test','betreff':status+' '+number,'text':'Status '+status+' für '+number,'createdAt':'2026-11-02T18:00:00Z'}
def set_events(events):
    import base64
    encoded=base64.b64encode(json.dumps(events).encode()).decode()
    php("update_option('kc_mock_status_events',json_decode(base64_decode('"+encoded+"'),true),false);")
def status_state():
    return json.loads(php("echo wp_json_encode(['records'=>count(get_option('kc_mock_records',[])),'mails'=>get_option('kc_mock_mails',[]),'acks'=>get_option('kc_mock_status_acks',[]),'rows'=>$wpdb->get_results('SELECT id,event_id,state,acknowledged,last_error FROM '.KC_HP_Status_Mails::table().' ORDER BY id')]);"))
base_state=status_state(); booking_count=base_state['records']; count=len(base_state['mails'])
reserved=status_event('RESERVIERT'); confirmed=status_event('BESTAETIGT');set_events([reserved,confirmed])
php('KC_HP_Status_Mails::work();')
s=status_state();check(len(s['mails'])==count+1 and s['mails'][-1]['subject'].startswith('RESERVIERT'),'Reservierung wird vor späterer Bestätigung versendet')
php('KC_HP_Status_Mails::work();')
s=status_state();check(len(s['mails'])==count+2 and s['mails'][-1]['subject'].startswith('BESTAETIGT'),'Bestätigung folgt in der Reihenfolge der Statusänderungen')
php('KC_HP_Status_Mails::work();')
check(len(status_state()['mails'])==count+2,'Erneutes Abholen erzeugt keine doppelten Statusmails')
count=len(status_state()['mails']); cancelled=status_event('STORNIERT');set_events([cancelled])
php("update_option('kc_mock_status_ack_fail',true); KC_HP_Status_Mails::work();")
s=status_state();check(len(s['mails'])==count+1 and s['rows'][-1]['state']=='sent' and s['rows'][-1]['acknowledged']=='0','Erfolgreiche Mail wird vor KC-Quittierung dauerhaft vermerkt')
php("update_option('kc_mock_status_ack_fail',false); $wpdb->query('UPDATE '.KC_HP_Status_Mails::table().' SET next_attempt=0'); KC_HP_Status_Mails::work();")
s=status_state();check(len(s['mails'])==count+1 and s['rows'][-1]['acknowledged']=='1','Fehlgeschlagene Quittierung wird ohne zweite Mail wiederholt')
rejected=status_event('ABGELEHNT');set_events([rejected]);count=len(s['mails'])
php("update_option('kc_mock_mail_fail',true); KC_HP_Status_Mails::work();")
s=status_state();check(len(s['mails'])==count and s['rows'][-1]['state']=='pending','SMTP-Fehler lässt die Statusmail offen')
php("$wpdb->query('UPDATE '.KC_HP_Status_Mails::table().' SET next_attempt=0'); KC_HP_Status_Mails::work();")
check(len(status_state()['mails'])==count+1,'Fehlgeschlagene Statusmail kann später zugestellt werden')
ready=status_event('ABRECHENBAR');set_events([ready]);count=len(status_state()['mails'])
php("$s=KC_HP_Api::settings();$s['mode']='preview';update_option('kc_hp_settings',$s);KC_HP_Status_Mails::work();")
check(len(status_state()['mails'])==count,'Vorschaumodus versendet keine Statusnachrichten')
php("$s=KC_HP_Api::settings();$s['mode']='live';update_option('kc_hp_settings',$s);KC_HP_Status_Mails::work();")
check(status_state()['mails'][-1]['subject'].startswith('ABRECHENBAR'),'Abrechenbar erhält eine eigene Statusnachricht')
settled=status_event('ABGESCHLOSSEN');set_events([settled]);count=len(status_state()['mails'])
php("update_option('kc_mock_mail_fail',true);KC_HP_Status_Mails::work();$s=KC_HP_Api::settings();$s['api_url']='https://fremd.invalid/api';update_option('kc_hp_settings',$s);$wpdb->query('UPDATE '.KC_HP_Status_Mails::table().' SET next_attempt=0');KC_HP_Status_Mails::work();")
check(len(status_state()['mails'])==count and 'Zielverbindung' in status_state()['rows'][-1]['last_error'],'Zielwechsel kann Statusmails nicht für einen anderen Verein versenden')
php("$s=KC_HP_Api::settings();$s['api_url']='https://kc.invalid/api';update_option('kc_hp_settings',$s);$wpdb->query('UPDATE '.KC_HP_Status_Mails::table().' SET next_attempt=0');KC_HP_Status_Mails::work();")
s=status_state();check(len(s['mails'])==count+1 and s['mails'][-1]['subject'].startswith('ABGESCHLOSSEN'),'Abrechnungsabschluss wird nach Verbindungswiederherstellung versendet')
check(s['records']==booking_count,'Alle Statusmails erzeugen keine neuen Buchungen')
upgrade_before=state(); upgrade_mails=len(status_state()['mails'])
php("$wpdb->query('DROP TABLE '.KC_HP_Status_Mails::table());$wpdb->query('ALTER TABLE '.KC_HP_Queue::table().' DROP COLUMN guest_mail_info, DROP COLUMN admin_mail_info');update_option('kc_hp_db_version','0.1.0');")
upgrade_after=state() # Nächster WordPress-Start führt ausschließlich die additive Plugin-Migration aus.
upgrade_info=json.loads(php("echo wp_json_encode(['version'=>get_option('kc_hp_db_version'),'table'=>$wpdb->get_var(\"SHOW TABLES LIKE '\".KC_HP_Status_Mails::table().\"'\"),'mails'=>count(get_option('kc_mock_mails',[]))]);"))
check(upgrade_after['records']==upgrade_before['records'] and upgrade_after['rows']==upgrade_before['rows'] and upgrade_info['version']=='0.1.5' and bool(upgrade_info['table']) and upgrade_info['mails']==upgrade_mails,'Plugin-Upgrade erhält bestehende Anfragen und versendet nicht automatisch')
print(str(checks)+' WordPress-Prüfungen erfolgreich.')

php("$wpdb->query('DELETE FROM '.KC_HP_Queue::limits());")
code,cal=post('kalender',{'monat':'2027-07','mietbereichId':10})
check(code==200 and len(cal['tage'])==31,'Kalender lädt einen vollständigen Monat aus KC')
check(cal['tage'][1]['buchungsnummern']==['2027-001'] and cal['tage'][2]['status']=='RESERVIERT','Buchungsnummern und Reservierungsstatus werden übernommen')
check('niemals-oeffentlich' not in json.dumps(cal),'Kalender leitet keine zusätzlichen personenbezogenen Felder weiter')
check(post('kalender',{'monat':'2027-07','mietbereichId':999})[0]==403,'Kalender weist nicht freigegebene Mietbereiche ab')
check(post('kalender',{'monat':'2027-99','mietbereichId':10})[0]==400,'Ungültige Kalendermonate werden abgewiesen')
check(post('kalender',{'monat':'2027-07'},False)[0]==403,'Kalender schützt den serverseitigen Zugang vor fremden Browser-Ursprüngen')
print(f'{checks} WordPress-Prüfungen einschließlich Kalender erfolgreich.',flush=True)
