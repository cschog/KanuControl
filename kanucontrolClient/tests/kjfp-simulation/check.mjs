import assert from 'node:assert/strict';
const {chromium}=await import(process.env.PLAYWRIGHT_MODULE||'playwright');
const browser=await chromium.launch({headless:true,...(process.env.CHROME_PATH?{executablePath:process.env.CHROME_PATH}:{})});
try {
 const page=await browser.newPage({viewport:{width:1280,height:1000}}),errors=[];page.on('pageerror',e=>errors.push(e.message));
 let saved={veranstaltung:{id:7,name:'Simulation',beginnDatum:'2027-07-01',endeDatum:'2027-07-05',typ:'JEM',tage:5,naechte:4,vereinKikZertifiziert:false},status:'IN_BEARBEITUNG',teilnehmer:10,mitarbeiter:2,teilnehmerBeitragUnter21Jahre:96,mitarbeiterBeitrag:115,unterkunftPreisProPersonUndNacht:20,verpflegungPreisProPersonUndTag:12,honorare:100,fahrkosten:80,verbrauchsmaterialProTag:3,kultur:40,miete:30,sonstigeKostenProTag:2,kikZertifiziert:false};
 let writes=[],fail=true,missing=false;
 await page.route('**/api/**',r=>{
  const req=r.request(),path=new URL(req.url()).pathname;if(!path.startsWith('/api/'))return r.continue();
  if(path==='/api/simulation/7') {
   if(missing)return r.fulfill({status:409,json:{error:'SIMULATION_NOT_READY',missing:['BEITRAGSSTRUKTUR','UNTERKUNFTSART']}});
   if(req.method()==='PUT') {writes.push(req.postDataJSON());if(fail){fail=false;return r.fulfill({status:500,json:{message:'Test: Speichern fehlgeschlagen'}});}saved=req.postDataJSON();return r.fulfill({status:204});}
   return r.fulfill({json:saved});
  }
  if(path==='/api/simulation')return r.fulfill({json:{kosten:1955,einnahmen:1690,saldo:-265,summeTeilnehmerbeitraege:1190,durchschnittlicherPersonenbeitrag:99.17,empfohlenerPersonenbeitrag:100,beitragsVorschlag:{teilnehmerBeitragUnter21Jahre:95,mitarbeiterBeitrag:125,durchschnittlicherPersonenbeitrag:100},positionen:[]}});
  return r.fulfill({json:[]});
 });
 const url=process.env.TEST_URL||'http://127.0.0.1:5177/tests/kjfp-simulation/index.html';await page.goto(url);
 const take=page.getByRole('button',{name:'Beitragsvorschlag übernehmen',exact:true});await take.waitFor().catch(async e=>{console.error(errors,await page.locator("body").innerText());throw e;});
 const field=label=>page.getByText(label,{exact:true}).locator('..').getByRole('spinbutton');
 await take.click();assert.equal(await take.isDisabled(),true);assert.equal(await field('TN-Beitrag (<21 Jahre)').inputValue(),'95');assert.equal(await field('MA-Beitrag').inputValue(),'125');
 await field('TN-Beitrag (<21 Jahre)').fill('90');assert.equal(await take.isEnabled(),true);await take.click();await field('MA-Beitrag').fill('130');assert.equal(await take.isEnabled(),true);await take.click();
 await page.getByRole('button',{name:'Simulation speichern',exact:true}).click();await page.getByText('Test: Speichern fehlgeschlagen',{exact:true}).waitFor();await page.getByRole('button',{name:'OK',exact:true}).click();assert.equal(saved.teilnehmerBeitragUnter21Jahre,96);
 await page.getByRole('button',{name:'Simulation speichern',exact:true}).click();await page.getByRole('button',{name:'Simulation gespeichert',exact:true}).waitFor();assert.equal(saved.teilnehmerBeitragUnter21Jahre,95);assert.equal(saved.mitarbeiterBeitrag,125);
 await page.reload();await take.waitFor();assert.equal(await field('MA-Beitrag').inputValue(),'125');assert.equal(writes.length,2);
 saved={...saved,status:'EINGEREICHT'};await page.reload();await take.waitFor();assert.equal(await take.isDisabled(),true);assert.equal(await page.getByRole('button',{name:'Planung eingereicht'}).isDisabled(),true);
 missing=true;await page.reload();await page.getByText('Simulation noch nicht möglich',{exact:true}).waitFor();assert.equal(await take.count(),0);assert.equal(writes.length,2);
 assert.deepEqual(errors,[]);console.log('PASS: Vorschlag nach TN-/MA-Änderung wieder aktiv; Speichern/Fehler/Wiederöffnen; Einreichung gesperrt; fehlende Voraussetzungen.');
}finally{await browser.close();}
