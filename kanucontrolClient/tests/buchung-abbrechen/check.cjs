const assert=require('node:assert/strict');
const {chromium}=require(process.env.KC_PLAYWRIGHT_MODULE||'playwright');
(async()=>{const browser=await chromium.launch({headless:true,channel:'chrome'});try{
 const page=await browser.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));await page.goto('http://127.0.0.1:18768/test');
 const mieter=page.getByRole('combobox',{name:'Mieter',exact:true});
 async function name(value){await page.waitForFunction(v=>document.querySelector('input[role=combobox]')?.value===v,value);assert.equal(await mieter.inputValue(),value);}
 async function edit(){await page.getByRole('button',{name:'Ändern',exact:true}).click();}
 async function cancel(){await page.getByRole('button',{name:'Abbrechen',exact:true}).click();}
 async function select(){await mieter.fill('Neu');await page.getByRole('option',{name:'Neu, Berta',exact:true}).click();await name('Neu, Berta');}
 await page.locator('#buchung-mieter-kopf').click();await name('Original, Anna');await edit();await select();await cancel();await name('Original, Anna');assert.equal(await page.evaluate(()=>window.__saveCalls.length),0);console.log('PASS Mieterwechsel abbrechen zeigt wieder den ursprünglichen Mieter und speichert nichts.');
 await edit();assert.equal(await mieter.inputValue(),'Original, Anna');await page.getByRole('button',{name:'Speichern',exact:true}).click();assert.equal(await page.evaluate(()=>window.__saveCalls.at(-1).mieterId),1);console.log('PASS Nach erneutem Bearbeiten wird die ursprüngliche Mieter-ID gespeichert.');
 await edit();await mieter.fill('Neu');await cancel();await name('Original, Anna');console.log('PASS Auch bloßer Suchtext wird beim Abbrechen verworfen.');
 await edit();await mieter.hover();await mieter.locator('xpath=ancestor::*[contains(@class,"MuiAutocomplete-root")]').locator('.MuiAutocomplete-clearIndicator').click();assert.equal(await mieter.inputValue(),'');await cancel();await name('Original, Anna');console.log('PASS Gelöschte Auswahl wird beim Abbrechen wiederhergestellt.');
 await edit();await select();await page.getByRole('button',{name:'Speichern',exact:true}).click();await name('Neu, Berta');assert.equal(await page.evaluate(()=>window.__saveCalls.at(-1).mieterId),2);console.log('PASS Bewusstes Speichern übernimmt den neuen Mieter.');
 await edit();await mieter.fill('Original');await page.getByRole('option',{name:'Original, Anna',exact:true}).click();await cancel();await name('Neu, Berta');console.log('PASS Nach gespeichertem Mieterwechsel stellt Abbrechen den neuen gespeicherten Stand wieder her.');
 assert.deepEqual(errors,[]);
}finally{await browser.close()}})().catch(e=>{console.error(e);process.exit(1)});
