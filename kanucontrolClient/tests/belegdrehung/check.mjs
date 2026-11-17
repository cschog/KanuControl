import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
const {chromium}=await import(process.env.PLAYWRIGHT_MODULE||'playwright');
const browser=await chromium.launch({headless:true,...(process.env.CHROME_PATH?{executablePath:process.env.CHROME_PATH}:{})});
try {
 const page=await browser.newPage({viewport:{width:1100,height:900}}), errors=[];page.on('pageerror',e=>errors.push(e.message));
 const png=await readFile(new URL('./beleg.png',import.meta.url));
 let state={drehungen:[0,0],version:0}, fail=true, writes=[];
 await page.route('**/api/**',r=>{
  const req=r.request(),path=new URL(req.url()).pathname;if(!path.startsWith('/api/'))return r.continue();
  if(path.endsWith('/ausrichtung')) {
   if(req.method()==='PUT') {
    const data=req.postDataJSON();writes.push(data);
    if(fail){fail=false;return r.fulfill({status:500,json:{message:'Test: Speichern fehlgeschlagen'}});}
    state={...data,version:state.version+1};
   }
   return r.fulfill({json:state});
  }
  if(path.includes('/vorschau/'))return r.fulfill({body:png,contentType:'image/png'});
  if(path.endsWith('/9'))return r.fulfill({body:Buffer.from('%PDF-1.7'),contentType:'application/pdf'});
  return r.fulfill({json:[{id:9,reihenfolge:1,originalDateiname:'beleg.pdf',mimeType:'application/pdf',dateigroesse:1234,createdAt:'2026-11-16T10:00:00Z'}]});
 });
 const url=process.env.TEST_URL||'http://127.0.0.1:5177/tests/belegdrehung/index.html';
 await page.goto(url);const open=()=>page.getByRole('button',{name:'Anzeigen: beleg.pdf'}).click();await open();
 const right=page.getByRole('button',{name:'90° rechts drehen'}),left=page.getByRole('button',{name:'90° links drehen'});
 await right.waitFor();await page.getByRole('img',{name:'Beleg Seite 1, Drehung 0 Grad'}).waitFor();
 await right.click();await right.click();await page.getByRole('status').filter({hasText:'180°'}).waitFor();
 assert.equal(await page.getByRole('button',{name:'Herunterladen',exact:true}).isDisabled(),true);
 await page.getByRole('combobox',{name:/^Seite/}).click();await page.getByRole('option',{name:'Seite 2 von 2'}).click();
 await page.getByRole('img',{name:'Beleg Seite 2, Drehung 0 Grad'}).waitFor();await right.click();
 await page.getByRole('button',{name:'Drehungen speichern'}).click();await page.getByText('Test: Speichern fehlgeschlagen',{exact:true}).waitFor();
 assert.deepEqual(state.drehungen,[0,0]);
 await page.getByRole('button',{name:'Drehungen speichern'}).click();
 await page.getByRole('button',{name:'Schließen',exact:true}).waitFor();assert.deepEqual(state,{drehungen:[180,90],version:1});
 const download=page.waitForEvent('download');await page.getByRole('button',{name:'Herunterladen',exact:true}).click();assert.equal((await download).suggestedFilename(),'beleg.pdf');
 await page.getByRole('button',{name:'Schließen',exact:true}).click();await open();
 await page.getByRole('img',{name:'Beleg Seite 1, Drehung 180 Grad'}).waitFor();
 for(let i=0;i<4;i++)await right.click();assert.equal(await page.getByRole('button',{name:'Drehungen speichern'}).isDisabled(),true);
 await left.click();await page.getByRole('status').filter({hasText:'90°'}).waitFor();
 page.once('dialog',d=>d.dismiss());await page.getByRole('button',{name:'Abbrechen',exact:true}).click();assert.equal(await page.getByRole('dialog').count(),1);
 page.once('dialog',d=>d.accept());await page.getByRole('button',{name:'Abbrechen',exact:true}).click();await open();
 await page.getByRole('img',{name:'Beleg Seite 1, Drehung 180 Grad'}).waitFor();assert.equal(writes.length,2);
 for(const width of [1100,375]){await page.setViewportSize({width,height:900});await page.waitForTimeout(350);await page.screenshot({animations:'disabled',path:`/tmp/kc-belegdrehung-${width}.png`,fullPage:true});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);}
 await page.goto(url+'?readonly');await open();await page.getByRole('img',{name:'Beleg Seite 1, Drehung 180 Grad'}).waitFor();
 assert.equal(await right.count(),0);assert.equal(await page.getByRole('button',{name:'Drehungen speichern'}).count(),0);assert.deepEqual(errors,[]);
 console.log('PASS: 2x90°, 4x90°, Seiten unabhängig, Speichern/Fehler/Wiederöffnen, Abbrechen, Download, Nur-Lesen, 375/1100px.');
}finally{await browser.close();}
