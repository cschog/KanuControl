import assert from 'node:assert/strict';
const {chromium}=await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const browser=await chromium.launch({headless:true,...(process.env.CHROME_PATH?{executablePath:process.env.CHROME_PATH}:{})});
try {
 const page=await browser.newPage({viewport:{width:1280,height:1000}});const errors=[];page.on('pageerror',e=>errors.push(e.message));
 const source={id:4,name:'Vorlage 2027',typ:'JEM',kjfpAktiv:true,fahrtenAktiv:true,beschreibung:'Beschreibung',beginnDatum:'2027-07-01',endeDatum:'2027-07-10',beginnZeit:'10:00',endeZeit:'18:00',verein:{id:1,name:'Kanuverein'},leiter:{id:2,name:'Muster',vorname:'Max'},unterkunftsart:{id:3,bezeichnung:'Zelt',name:'Zelt'},verpflegungsmodell:{id:5,bezeichnung:'Vollpension',name:'Vollpension'},countryCode:'FR',plz:'07150',ort:'Vallon',beitragsstrukturId:6,individuelleGebuehren:true,standardGebuehr:80,scope:'VEREIN',programmentwurfTitel:'Programm',programmentwurfInhalt:'<p><strong>Start</strong></p>',aktiv:true};
 let failed=false;const requests=[];
 await page.route('**/api/**',async r=>{
  const request=r.request(),url=new URL(request.url());if(!url.pathname.startsWith('/api/'))return r.continue();
  if(url.pathname==='/api/veranstaltungen' && request.method()==='POST') {
   const payload=request.postDataJSON();requests.push(payload);
   if(!failed){failed=true;return r.fulfill({status:500,json:{message:'Test: Speichern fehlgeschlagen'}});}
   return r.fulfill({json:{data:{...source,...payload,id:99},warnings:[]}});
  }
  if(url.pathname==='/api/veranstaltungen')return r.fulfill({json:{content:[{...source,vereinName:'Kanuverein',vereinAbk:'KV',leiterName:'Muster',leiterVorname:'Max'}]}});
  if(url.pathname==='/api/veranstaltungen/4')return r.fulfill({json:source});
  if(url.pathname==='/api/beitragsstrukturen')return r.fulfill({json:[{id:6,name:'Beiträge'}]});
  return r.fulfill({json:[]});
 });
 await page.goto(process.env.TEST_URL || 'http://127.0.0.1:5177/tests/veranstaltung-kopie/index.html');
 await page.getByText('Vorlage 2027',{exact:true}).click();
 await page.getByRole('button',{name:'Kopieren',exact:true}).click();
 await page.getByRole('heading',{name:'Veranstaltung mit Planung kopieren'}).waitFor();
 assert.equal(await page.getByRole('textbox',{name:'Bezeichnung',exact:true}).last().inputValue(),'Vorlage 2027 Kopie');
 await page.getByRole('button',{name:'Anlegen',exact:true}).click();
 await page.getByText('Test: Speichern fehlgeschlagen',{exact:true}).waitFor();
 await page.getByRole('button',{name:'OK',exact:true}).last().click();
 await page.getByRole('heading',{name:'Veranstaltung mit Planung kopieren'}).waitFor();
 assert.equal(requests.length,1);
 const payload=requests[0];
 for(const [key,value] of Object.entries({kopieVonVeranstaltungId:4,kjfpAktiv:true,fahrtenAktiv:true,beschreibung:'Beschreibung',unterkunftsartId:3,verpflegungsmodellId:5,countryCode:'FR',plz:'07150',ort:'Vallon',beitragsstrukturId:6,individuelleGebuehren:true,standardGebuehr:80,scope:'VEREIN',programmentwurfTitel:'Programm',programmentwurfInhalt:'<p><strong>Start</strong></p>'}))assert.deepEqual(payload[key],value,key);
 await page.getByRole('button',{name:'Anlegen',exact:true}).click();
 await page.getByRole('heading',{name:'Veranstaltung mit Planung kopieren'}).waitFor({state:'hidden'});
 assert.equal(requests.length,2);assert.deepEqual(errors,[]);
 console.log('PASS: Kopieren aus echter Veranstaltungsansicht, vollständige Payload inklusive Planungsquelle; Fehler erhält Dialog, erfolgreicher Wiederholversuch schließt ihn.');
}finally{await browser.close();}
