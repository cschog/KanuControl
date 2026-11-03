(() => {
  'use strict';
  const boot = window.kcHomepage;
  if (!boot) return;
  document.querySelectorAll('.kc-hp').forEach(async (root) => {
    const form = root.querySelector('form');
    const message = root.querySelector('.kc-hp-message');
    const availability = root.querySelector('.kc-hp-availability');
    const check = root.querySelector('.kc-hp-check');
    const submit = root.querySelector('.kc-hp-submit');
    const resources = root.querySelector('.kc-hp-resources');
    const field = (name) => form.elements.namedItem(name);
    const storageKey = 'kc-hp:' + location.pathname + ':' + boot.endpoint;
    let session, checked = '', pending = null, busy = false;
    const resourceInputs = new Map();
    const text = (value, error = false) => {
      message.textContent = value;
      message.dataset.error = String(error);
    };
    const saveLocal = (value) => {
      try { sessionStorage.setItem(storageKey, JSON.stringify(value)); }
      catch { /* Übertragung bleibt möglich; bei unklarer Antwort nicht neu laden. */ }
    };
    const readLocal = () => {
      try { return JSON.parse(sessionStorage.getItem(storageKey) || 'null'); } catch { return null; }
    };
    async function call(path, data) {
      const headers = { 'Content-Type': 'application/json' };
      if (boot.nonce) headers['X-WP-Nonce'] = boot.nonce;
      const response = await fetch(boot.endpoint + path, { method: 'POST', credentials: 'same-origin',
        cache: 'no-store', headers, body: JSON.stringify(data) });
      let result;
      try { result = await response.json(); } catch { throw new Error('Die Antwort konnte nicht gelesen werden. Bitte den bestehenden Vorgang erneut prüfen.'); }
      if (!response.ok && !result.status) throw new Error(result.message || 'Die Verbindung ist fehlgeschlagen. Bitte später erneut versuchen.');
      return result;
    }
    const quantity = (input) => input.type === 'checkbox' ? Number(input.checked) : Number(input.value);
    function selection() {
      return { anreise: field('anreise').value, abreise: field('abreise').value,
        positionen: [...resourceInputs].filter(([, input]) => quantity(input) > 0)
          .map(([mietbereichId, input]) => ({ mietbereichId, anzahl: quantity(input) })) };
    }
    function invalidate() {
      checked = ''; availability.textContent = ''; submit.disabled = true;
    }
    function ages() {
      const youth = Number(field('anzahlJugendliche').value) > 0;
      for (const name of ['jugendAlterVon', 'jugendAlterBis']) {
        field(name).disabled = !youth; field(name).required = youth;
        if (!youth) field(name).value = '';
      }
      field('jugendAlterBis').setCustomValidity(youth && field('jugendAlterVon').value !== '' && field('jugendAlterBis').value !== ''
        && Number(field('jugendAlterVon').value) > Number(field('jugendAlterBis').value) ? 'Das Höchstalter muss mindestens dem Mindestalter entsprechen.' : '');
      const y = field('anzahlJugendliche').value, a = field('anzahlErwachsene').value;
      root.querySelector('.kc-hp-total').textContent = y !== '' && a !== '' ? 'Teilnehmer gesamt: ' + (Number(y) + Number(a)) : '';
    }
    function emailCheck() {
      field('emailWiederholung').setCustomValidity(field('email').value.trim() !== field('emailWiederholung').value.trim()
        ? 'Die E-Mail-Adressen müssen übereinstimmen.' : '');
    }
    function resultAction(label, callback) {
      let button = root.querySelector('.kc-hp-result-action');
      if (!button) { button = document.createElement('button'); button.type = 'button'; button.className = 'kc-hp-result-action'; root.append(button); }
      button.textContent = label; button.onclick = callback; button.disabled = false;
      return button;
    }
    function finish(result) {
      const number = result.buchungsnummer ? ' Buchungsnummer: ' + result.buchungsnummer + '.' : '';
      text(result.message + number, result.status === 'failed');
      if (result.status === 'confirmed' || result.status === 'preview') {
        pending = null;
        saveLocal({ done: true, message: result.message + number });
        form.hidden = true;
        resultAction('Eine weitere Anfrage beginnen', () => {
          try { sessionStorage.removeItem(storageKey); } catch { /* optional */ }
          location.reload();
        });
      } else {
        form.hidden = true;
        resultAction('Diesen Vorgang erneut prüfen', () => sendPending());
      }
    }
    async function sendPending() {
      if (busy || !pending) return;
      busy = true;
      const action = root.querySelector('.kc-hp-result-action'); if (action) action.disabled = true;
      text('Der bestehende Vorgang wird geprüft. Bitte nicht als neue Anfrage absenden.');
      try {
        // Ein frisches Sitzungsticket ändert weder Anfrage-ID noch fachliche Daten der Wiederholung.
        session = await call('session', {});
        const result = await call('anfrage', { ...pending, ticket: session.ticket });
        finish(result);
      } catch (error) {
        form.hidden = true;
        text(error.message + ' Deine Angaben bleiben für die Wiederholung in diesem Browser-Tab erhalten. Bitte nicht als neue Anfrage absenden.', true);
        resultAction('Diesen Vorgang erneut prüfen', () => sendPending());
      } finally {
        busy = false;
        const button = root.querySelector('.kc-hp-result-action'); if (button) button.disabled = false;
      }
    }
    try {
      session = await call('session', {});
      const catalog = session.catalog;
      if (!catalog || !Array.isArray(catalog.bereiche) || !catalog.bereiche.length) throw new Error('Für diese Homepage sind noch keine Mietbereiche verfügbar. Bitte kontaktiere die Vermietung.');
      root.querySelector('.kc-hp-object').textContent = catalog.mietobjekt;
      root.querySelector('.kc-hp-times').textContent = 'Anreise ab ' + catalog.anreisezeit + ' Uhr · Abreise bis ' + catalog.abreisezeit + ' Uhr';
      for (const [selector, url] of [['.kc-hp-rules', catalog.hausregelnUrl], ['.kc-hp-privacy', catalog.datenschutzUrl]]) {
        const parsed = new URL(url);
        if (parsed.protocol !== 'https:' || parsed.username || parsed.password) throw new Error('Die Formularhinweise sind noch nicht vollständig eingerichtet.');
        root.querySelector(selector).href = parsed.href;
      }
      const today = new Intl.DateTimeFormat('sv-SE', { timeZone: 'Europe/Berlin', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date());
      const followingDay = (value) => {
        const date = new Date(value + 'T12:00:00Z');
        if (Number.isNaN(date.valueOf()) || date.toISOString().slice(0,10) !== value) return '';
        date.setUTCDate(date.getUTCDate() + 1);
        return date.toISOString().slice(0,10);
      };
      field('anreise').min = today; field('abreise').min = followingDay(today);
      field('anzahlJugendliche').required = !!catalog.teilnehmerPflicht;
      field('anzahlErwachsene').required = !!catalog.teilnehmerPflicht;
      for (const b of catalog.bereiche) {
        const row = document.createElement('div'); row.className = 'kc-hp-resource';
        const info = document.createElement('div');
        const heading = document.createElement('strong'); heading.textContent = b.bezeichnung; info.append(heading);
        const detail = document.createElement('p');
        const priceLabel = b.berechnungsart === 'TAG' ? 'Preis pro Übernachtung'
          : b.berechnungsart === 'BUCHUNG' ? (Number(b.bestand) === 1 ? 'Preis pro Buchung' : 'Einzelpreis pro Buchung')
          : 'Einzelpreis (' + String(b.berechnungsart).toLowerCase() + ')';
        detail.textContent = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(b.einzelpreis)
          + ' · ' + priceLabel; info.append(detail);
        if (b.beschreibung) { const p = document.createElement('p'); p.textContent = b.beschreibung; info.append(p); }
        const single = Number(b.bestand) === 1;
        const label = document.createElement('label'); label.textContent = single ? 'Auswählen' : 'Anzahl';
        const input = document.createElement('input');
        if (single) {
          input.type = 'checkbox'; input.value = '1'; label.className = 'kc-hp-resource-choice';
        } else {
          input.type = 'number'; input.min = '0'; input.max = String(b.bestand); input.step = '1'; input.value = '0';
        }
        input.setAttribute('aria-label', (single ? 'Auswählen ' : 'Anzahl ') + b.bezeichnung); label.append(input); resourceInputs.set(b.id, input);
        input.addEventListener('input', () => {
          invalidate();
          // Voraussetzungen rekursiv ergänzen, ohne zyklische Regeln endlos zu verfolgen.
          const visited = new Set();
          function requireId(id) {
            if (visited.has(id)) return; visited.add(id);
            const required = catalog.bereiche.find((item) => item.id === id);
            for (const other of required?.erforderlicheIds || []) {
              const item = resourceInputs.get(other);
              if (item && quantity(item) < 1) {
                if (item.type === 'checkbox') item.checked = true;
                else item.value = '1';
              }
              requireId(other);
            }
          }
          if (quantity(input) > 0) requireId(b.id);
        });
        row.append(info, label); resources.append(row);
      }
      // Kalenderlinks belegen nur Felder vor; Verfügbarkeit und Einwilligungen bleiben erforderlich.
      const params = new URLSearchParams(location.search);
      const arrival = params.get('kc_anreise');
      if (arrival && /^\d{4}-\d{2}-\d{2}$/.test(arrival) && arrival >= today) {
        const parsed = new Date(arrival + 'T12:00:00Z');
        if (!Number.isNaN(parsed.valueOf()) && parsed.toISOString().slice(0,10) === arrival) {
          field('anreise').value = arrival; parsed.setUTCDate(parsed.getUTCDate()+1);
          field('abreise').value = parsed.toISOString().slice(0,10); field('abreise').min = followingDay(arrival);
          const chosen = resourceInputs.get(Number(params.get('kc_mietbereich')));
          if (chosen) {
            if (chosen.type === 'checkbox') chosen.checked = true; else chosen.value = '1';
            chosen.dispatchEvent(new Event('input', {bubbles:true}));
          }
        }
      }
      for (const name of ['anreise', 'abreise']) field(name).addEventListener('input', () => {
        field('abreise').setCustomValidity(''); invalidate();
        const earliest = followingDay(field('anreise').value || today);
        if (earliest) {
          field('abreise').min = earliest;
          if (name === 'anreise' && field('abreise').value && field('abreise').value < earliest) field('abreise').value = earliest;
        }
      });
      ['anzahlJugendliche', 'anzahlErwachsene', 'jugendAlterVon', 'jugendAlterBis'].forEach((name) => field(name).addEventListener('input', ages));
      ['email', 'emailWiederholung'].forEach((name) => field(name).addEventListener('input', emailCheck));
      check.addEventListener('click', async () => {
        const selected = selection();
        field('abreise').setCustomValidity(selected.abreise && selected.anreise && selected.abreise <= selected.anreise ? 'Bitte mindestens eine Übernachtung wählen. Die Abreise muss frühestens am Folgetag liegen.' : '');
        if (!field('anreise').reportValidity() || !field('abreise').reportValidity()) return;
        if (!selected.positionen.length) { availability.textContent = 'Bitte mindestens einen Mietbereich auswählen.'; return; }
        if ([...resourceInputs.values()].some((input) => !input.reportValidity())) return;
        check.disabled = true; submit.disabled = true; availability.textContent = 'Verfügbarkeit wird geprüft …';
        try {
          const answer = await call('verfuegbarkeit', { ticket: session.ticket, auswahl: selected });
          if (JSON.stringify(selected) !== JSON.stringify(selection())) return;
          checked = answer.verfuegbar ? JSON.stringify(selected) : '';
          availability.textContent = answer.hinweis; submit.disabled = !answer.verfuegbar;
        } catch (error) { availability.textContent = error.message; }
        finally { check.disabled = false; }
      });
      form.addEventListener('submit', async (event) => {
        event.preventDefault();
        ages(); emailCheck();
        if (!form.reportValidity() || busy) return;
        const selected = selection();
        if (JSON.stringify(selected) !== checked) { text('Bitte zuerst die aktuelle Auswahl auf Verfügbarkeit prüfen.', true); return; }
        const data = { anfrageId: crypto.randomUUID(), konfigurationVersion: catalog.konfigurationVersion, auswahl: selected };
        ['vorname', 'name', 'email', 'telefon', 'strasse', 'plz', 'ort', 'veranstalter', 'bemerkung', 'website'].forEach((name) => { data[name] = field(name).value.trim(); });
        data.countryCode = field('countryCode').value.toUpperCase();
        ['anzahlJugendliche', 'anzahlErwachsene', 'jugendAlterVon', 'jugendAlterBis'].forEach((name) => { data[name] = field(name).value === '' ? null : Number(field(name).value); });
        data.hausregelnAkzeptiert = field('hausregelnAkzeptiert').checked;
        data.datenschutzGelesen = field('datenschutzGelesen').checked;
        pending = data;
        if (session.mode !== 'preview') saveLocal({ payload: pending });
        form.hidden = true;
        await sendPending();
      });
      const stored = readLocal();
      if (stored?.done) {
        text(stored.message); form.hidden = true;
        resultAction('Eine weitere Anfrage beginnen', () => { try { sessionStorage.removeItem(storageKey); } catch {} location.reload(); });
      } else if (stored?.payload?.anfrageId) {
        pending = stored.payload; form.hidden = true;
        text('In diesem Browser-Tab gibt es bereits einen übermittelten Vorgang. Bitte diesen erneut prüfen und keine zweite Anfrage anlegen.');
        resultAction('Diesen Vorgang erneut prüfen', () => sendPending());
      } else {
        form.hidden = false;
        if (arrival && field('anreise').value === arrival) root.scrollIntoView({block:'start'});
        text(session.mode === 'preview' ? 'Administratoren-Vorschau: Verfügbarkeit wird gelesen. Absenden legt keine Buchung an und verschickt keine E-Mails.' : 'Bitte zuerst Zeitraum und Mietumfang auswählen.');
      }
    } catch (error) {
      text(error.message, true);
      resultAction('Formular erneut laden', () => location.reload());
    }
  });
})();
