const assert=require('node:assert/strict');
const {chromium}=require(process.env.KC_PLAYWRIGHT_MODULE||'playwright');
(async()=>{const browser=await chromium.launch({headless:true,channel:'chrome'});try{
const page=await browser.newPage({viewport:{width:1280,height:1000}});page.setDefaultTimeout(8000);const errors=[];page.on('pageerror',e=>errors.push(e.message));
const heads=['mieter','aufenthalt','miete','zahlung','verbrauch','schaeden','intern'];
const expanded=async()=>{const result=[];for(const id of heads)if(await page.locator(`#buchung-${id}-kopf`).getAttribute('aria-expanded')==='true')result.push(id);return result;};
const head=id=>page.locator(`#buchung-${id}-kopf`);
const all=()=>page.getByRole('button',{name:'Alle aufklappen',exact:true}).click();
const close=()=>page.getByRole('button',{name:'Alle zuklappen',exact:true}).click();
await page.goto('http://127.0.0.1:18769/test');await head('aufenthalt').waitFor();
assert.deepEqual(await expanded(),['aufenthalt']);assert.equal(await page.locator('[id$="-kopf"]').count(),7);
assert.match(await head('miete').innerText(),/480,00/);console.log('PASS sieben Bereiche, Bearbeitungsstart und gespeicherter Preis-/Rabatt-Snapshot.');
await page.getByRole('button',{name:'Ändern',exact:true}).click();await all();
await page.getByLabel('Anzahl Erwachsene',{exact:true}).fill('0');await page.getByLabel('Bemerkung',{exact:true}).fill('Ungespeicherte Notiz');
await page.getByLabel('Frühere Buchungsnummer',{exact:true}).fill('2025-30');
await close();assert.match(await head('aufenthalt').innerText(),/Jugendliche nicht erfasst · Erwachsene 0/);assert.match(await head('intern').innerText(),/Bemerkung vorhanden.*2025-30/s);
for(const width of [375,390,768,1280,1920]){
 await page.setViewportSize({width,height:1000});await all();await page.waitForTimeout(350);
 assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true,`overflow ${width}`);
 await page.screenshot({path:`/private/tmp/kc-structure-${width}.png`,fullPage:true});
 await close();assert.match(await head('intern').innerText(),/2025-30/);
}
// Open navigation / constrained form width independent of viewport.
await page.setViewportSize({width:1280,height:1000});await page.locator('body>div').evaluate(el=>{el.style.marginLeft='280px';el.style.maxWidth='calc(100% - 280px)';});await all();
assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);await page.screenshot({path:'/private/tmp/kc-structure-navigation.png',fullPage:true});
await page.locator('body>div').evaluate(el=>{el.style.marginLeft='0';el.style.maxWidth='none';});
assert.equal(await page.getByLabel('Bemerkung',{exact:true}).inputValue(),'Ungespeicherte Notiz');assert.equal(await page.getByLabel('Anzahl Erwachsene',{exact:true}).inputValue(),'0');console.log('PASS Breiten 375/390/768/1280/1920 und Navigation ohne horizontales Scrollen; Eingaben bleiben erhalten.');
await page.getByLabel('Anzahl Jugendliche',{exact:true}).fill('2');await close();await page.getByRole('button',{name:'Speichern',exact:true}).click();await page.waitForTimeout(400);
assert.equal(await head('aufenthalt').getAttribute('aria-expanded'),'true');assert.match(await head('aufenthalt').innerText(),/Fehler:/);assert.equal(await page.evaluate(()=>window.__saveCalls.length),0);
await page.getByLabel(/Alter ungefähr von/).fill('8');await page.getByLabel(/Alter ungefähr bis/).fill('12');
await page.getByRole('button',{name:'Speichern',exact:true}).click();await page.waitForFunction(()=>window.__saveCalls.length===1,null,{timeout:8000});assert.equal(await page.evaluate(()=>window.__saveCalls[0].anzahlErwachsene),0);assert.equal(await page.evaluate(()=>window.__saveCalls[0].mietpreiseNeuBerechnen),undefined);console.log('PASS Fehler öffnen geschlossenen Bereich; normales Speichern bleibt unabhängig vom Abschluss.');
await page.getByRole('button',{name:'Abrechnung vorbereiten',exact:true}).first().click();await page.getByText('Abrechnung vorbereiten · gespeicherter Stand').waitFor();
assert.deepEqual(await expanded(),['verbrauch','schaeden']);assert.equal(await page.evaluate(()=>window.__mutations||0),0);assert.match(await page.locator('form').innerText(),/514,00/);assert.match(await page.locator('form').innerText(),/464,00/);
assert.match(await page.locator('#buchung-verbrauch-inhalt').innerText(),/42/);assert.match(await page.locator('#buchung-verbrauch-inhalt').innerText(),/2 Tage/);assert.match(await page.locator('#buchung-verbrauch-inhalt').innerText(),/Pauschalbetrag/);console.log('PASS Abrechnung öffnet Verbrauch/Schäden ohne Mutation, Kostenarten und Server-Zahlungssaldo.');
await page.getByRole('button',{name:'Ändern',exact:true}).click();await all();await page.getByLabel('Bemerkung',{exact:true}).fill('Verwerfen');await page.getByRole('button',{name:'Abbrechen',exact:true}).click();await page.getByRole('button',{name:'Ändern',exact:true}).click();
assert.equal(await page.getByLabel('Bemerkung',{exact:true}).inputValue(),'Ungespeicherte Notiz');await page.getByRole('button',{name:'Abbrechen',exact:true}).click();
await head('mieter').focus();await page.keyboard.press('Enter');assert.equal(await head('mieter').getAttribute('aria-expanded'),'false');console.log('PASS Abbrechen stellt gespeicherten Zustand her; Accordion per Tastatur bedienbar.');
await page.goto('http://127.0.0.1:18769/test?neu');await head('mieter').waitFor();assert.deepEqual(await expanded(),['mieter','aufenthalt']);assert.match(await head('aufenthalt').innerText(),/Teilnehmer nicht erfasst/);console.log('PASS Startzustand neue Buchung und neutrale fehlende Angaben.');
assert.deepEqual(errors,[]);
}finally{await browser.close()}})().catch(e=>{console.error(e);process.exit(1)});
