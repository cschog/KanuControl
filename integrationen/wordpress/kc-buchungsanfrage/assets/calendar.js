(() => {
  'use strict';
  const boot = window.kcCalendar;
  if (!boot) return;
  const today = new Intl.DateTimeFormat('sv-SE', {timeZone:'Europe/Berlin', year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date());
  document.querySelectorAll('.kc-calendar').forEach((root) => {
    let month = today.slice(0,7), area = null, serial = 0;
    const grid = root.querySelector('.kc-calendar-grid');
    const message = root.querySelector('.kc-calendar-message');
    const labels = {FREI:'Frei',RESERVIERT:'Reserviert',BELEGT:'Belegt',GESPERRT:'Gesperrt'};
    async function load() {
      const current = ++serial;
      grid.replaceChildren(); message.textContent = 'Belegung wird geladen …';
      root.setAttribute('aria-busy','true');
      const date = new Date(month + '-01T12:00:00Z');
      root.querySelector('.kc-calendar-month').textContent = new Intl.DateTimeFormat('de-DE',{month:'long',year:'numeric',timeZone:'UTC'}).format(date);
      try {
        const headers = {'Content-Type':'application/json'}; if (boot.nonce) headers['X-WP-Nonce'] = boot.nonce;
        const response = await fetch(boot.endpoint,{method:'POST',credentials:'same-origin',cache:'no-store',headers,
          body:JSON.stringify({monat:month,...(area === null ? {} : {mietbereichId:area})})});
        const data = await response.json();
        if (current !== serial) return;
        if (!response.ok) throw new Error(data.message || 'Der Kalender ist gerade nicht erreichbar. Bitte erneut versuchen.');
        if (!Array.isArray(data.tage) || data.monat !== month) throw new Error('Die Kalenderantwort konnte nicht gelesen werden.');
        area = data.mietbereichId;
        const timed = data.tage.every(day => Array.isArray(day.belegungen));
        root.classList.toggle('kc-calendar-timed', timed);
        root.querySelector('.kc-calendar-note').textContent = timed ? 'Zeitbalken zeigen die in KC gespeicherten Belegungszeiten von 0 bis 24 Uhr. Bei mehreren verfügbaren Einheiten kann trotz einer Belegung noch Platz vorhanden sein. Der Anfragestatus gilt für die Übernachtung ab ' + data.catalog.anreisezeit + ' Uhr bis ' + data.catalog.abreisezeit + ' Uhr am Folgetag.' : 'Die Tagesanzeige gilt jeweils für eine Übernachtung: Anreise ab ' + data.catalog.anreisezeit
          + ' Uhr bis zur Abreise am Folgetag um ' + data.catalog.abreisezeit + ' Uhr. Erforderliche zusätzliche Mietbereiche sind berücksichtigt. Buchungsnummern stehen bei den zugehörigen Belegungen.';
        for (const day of ['Mo','Di','Mi','Do','Fr','Sa','So']) { const el=document.createElement('div');el.className='kc-calendar-weekday';el.textContent=day;grid.append(el); }
        for (let i=0;i<(date.getUTCDay()+6)%7;i++) { const el=document.createElement('div');el.className='kc-calendar-empty';el.setAttribute('aria-hidden','true');grid.append(el); }
        for (const day of data.tage) {
          const available = day.status === 'FREI' && day.datum >= today;
          const cell=document.createElement(available ? 'a' : 'div'); cell.className='kc-calendar-day';cell.dataset.status=day.status;
          if (available) {
            const url = new URL(root.dataset.anfrageUrl,location.href);
            if (!['http:','https:'].includes(url.protocol)) throw new Error('Die Adresse des Anfrageformulars ist ungültig.');
            url.searchParams.set('kc_anreise',day.datum);url.searchParams.set('kc_mietbereich',String(area));cell.href=url.href;
          }
          const number=document.createElement('strong');number.textContent=String(Number(day.datum.slice(-2)));cell.append(number);
          const status=document.createElement('span');status.textContent=labels[day.status] || 'Unbekannt';if (!timed) cell.append(status);
          if (!timed && day.verfuegbar > 1) {const capacity=document.createElement('span');capacity.textContent=day.verfuegbar+' verfügbar';cell.append(capacity);}
          if (timed) renderOccupancies(cell, day);
          for (const id of timed ? [] : (day.buchungsnummern || [])) {const booking=document.createElement('small');booking.textContent=id;cell.append(booking);}
          cell.setAttribute('aria-label',(timed ? Array.from(cell.querySelectorAll('.kc-calendar-occupancy')).map(entry => entry.getAttribute('aria-label')).join('; ') + '. ' : '') + day.datum + ': ' + status.textContent + (day.buchungsnummern?.length ? ', Buchungen '+day.buchungsnummern.join(', ') : ''));
          grid.append(cell);
        }
        message.textContent='Aktuelle Belegung aus KC.';
      } catch (error) { if (current === serial) { grid.replaceChildren();message.textContent=error.message; } }
      finally { if(current === serial) root.removeAttribute('aria-busy'); }
    }
    function renderOccupancies(cell, day) {
      for (const item of day.belegungen) {
        // KC supplies local wall-clock times; do not shift these through the browser timezone.
        const match = value => typeof value === 'string' && value.match(/^(\d{4}-\d{2}-\d{2})T(\d{2}):(\d{2})(?::\d{2}(?:\.\d+)?)?$/);
        const from = match(item.von), to = match(item.bis);
        if (!from || !to || !['BELEGT','RESERVIERT','GESPERRT'].includes(item.status)) continue;
        const start = from[1] < day.datum ? 0 : Number(from[2])*60 + Number(from[3]);
        const end = to[1] > day.datum ? 1440 : Number(to[2])*60 + Number(to[3]);
        if (start < 0 || end > 1440 || end <= start) continue;
        const entry = document.createElement('div'); entry.className = 'kc-calendar-occupancy';
        const caption = document.createElement('small');
        const time = minutes => String(Math.floor(minutes/60)).padStart(2,'0') + ':' + String(minutes%60).padStart(2,'0');
        const description = time(start) + '–' + time(end) + ' Uhr · ' + labels[item.status]
          + (item.buchungsnummer ? ' · ' + item.buchungsnummer : '')
          + (item.status !== 'GESPERRT' && typeof item.mietername === 'string' && item.mietername.trim() ? ' · ' + item.mietername.trim() : '');
        caption.textContent = item.status === 'GESPERRT' ? 'Gesperrt' : item.buchungsnummer || '';
        entry.title = description;
        entry.setAttribute('aria-label', description);
        const track = document.createElement('div'); track.className = 'kc-calendar-track'; track.setAttribute('aria-hidden','true');
        const bar = document.createElement('span'); bar.dataset.status = item.status;
        bar.style.left = (start/1440*100) + '%'; bar.style.width = ((end-start)/1440*100) + '%';
        track.append(bar); entry.append(caption,track); cell.append(entry);
      }
    }
    function navigate(delta) {
      const date=new Date(month+'-01T12:00:00Z');date.setUTCMonth(date.getUTCMonth()+delta);
      if(date.getUTCFullYear()<2000 || date.getUTCFullYear()>2100) return;
      month=date.toISOString().slice(0,7);load();
    }
    root.querySelector('.kc-calendar-prev').addEventListener('click',()=>navigate(-1));
    root.querySelector('.kc-calendar-next').addEventListener('click',()=>navigate(1));
    load();
  });
})();
