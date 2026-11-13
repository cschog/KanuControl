import assert from 'node:assert/strict';
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const browser=await chromium.launch({headless:true,...(process.env.CHROME_PATH ? {executablePath:process.env.CHROME_PATH} : {})});
try {
 const page=await browser.newPage({viewport:{width:1280,height:900}});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.route('**/api/**',r=>new URL(r.request().url()).pathname.startsWith('/api/')?r.fulfill({json:[]}):r.continue());
 await page.goto(process.env.TEST_URL || 'http://127.0.0.1:5177/tests/programmentwurf/index.html');
 const editor=page.getByRole('textbox',{name:'Programmtext',exact:true});
 // Bestandsdaten: Die API liefert beide optionalen Felder explizit als null.
 await page.locator('#state').waitFor();
 const loaded = JSON.parse(await page.locator('#state').textContent());
 assert.equal(loaded.programmentwurfTitel, '');
 assert.equal(loaded.programmentwurfInhalt, '');
 assert.deepEqual(await page.evaluate(() => window.programErrors(null, null)), []);
 assert.equal(await page.evaluate(() => window.programText(null)), '');
 await page.getByText('Noch kein Programmentwurf').click();
 await editor.fill('Hallo Welt');
 assert.equal(await page.getByRole('textbox',{name:'Überschrift'}).inputValue(),'Programmentwurf');
 const selectAll=async()=>editor.evaluate(el=>{el.focus();const range=document.createRange();range.selectNodeContents(el);const sel=window.getSelection();sel.removeAllRanges();sel.addRange(range);});
 const state=async()=>JSON.parse(await page.locator('#state').textContent());
 await selectAll();await page.getByRole('button',{name:'Fett (Strg+B)',exact:true}).click();
 await selectAll();await page.getByRole('button',{name:'Kursiv (Strg+I)',exact:true}).click();
 assert.match((await state()).programmentwurfInhalt, /<(strong|em)>/);
 assert.match((await state()).programmentwurfInhalt, /<em>/);
 await selectAll();await page.getByRole('button',{name:'Aufzählung',exact:true}).click();
 assert.match((await state()).programmentwurfInhalt, /<ul>/);
 await selectAll();await page.getByRole('button',{name:'Formatierung entfernen',exact:true}).click();
 assert.doesNotMatch((await state()).programmentwurfInhalt, /<(ul|ol|li|strong|em)>/);
 assert.match((await state()).programmentwurfInhalt, /Hallo Welt/);
 await selectAll();await page.getByRole('button',{name:'Nummerierte Liste',exact:true}).click();
 assert.match((await state()).programmentwurfInhalt, /<ol>/);
 await selectAll();await page.getByRole('button',{name:'Formatierung entfernen',exact:true}).click();
 await editor.fill('');
 await editor.evaluate(el=>{el.focus();const data=new DataTransfer();data.setData('text/html','<div style="font-family:Comic Sans;color:red">Grüße <b><i>Welt</i></b><br>Zweite Zeile<script>window.evil=1</script><img src="https://bad.invalid/x" onerror="window.evil=2"></div>');el.dispatchEvent(new ClipboardEvent('paste',{clipboardData:data,bubbles:true,cancelable:true}));});
 const html=(await state()).programmentwurfInhalt;
 assert.match(html,/<strong><em>Welt<\/em><\/strong>/);assert.doesNotMatch(html,/script|img|style=|onerror/);assert.equal(await page.evaluate(()=>window.evil),undefined);
 await page.getByRole('button',{name:/Programmentwurf Programmentwurf vorhanden/}).click();
 await page.getByRole('button',{name:/Programmentwurf Programmentwurf vorhanden/}).click();
 assert.equal((await state()).programmentwurfInhalt,html);
 await page.getByRole('textbox',{name:'Überschrift'}).fill('x'.repeat(201));
 await page.getByText('Die Überschrift darf höchstens 200 Zeichen enthalten und muss einzeilig sein.',{exact:true}).waitFor();
 assert.equal((await state()).programmentwurfTitel.length,201);
 await page.getByRole('textbox',{name:'Überschrift'}).fill('Programmentwurf');
 const limits=await page.evaluate(()=>({empty:window.programText('<p><br></p>'),count:Array.from(window.programText('<p>A<br>B</p>')).length,over:window.programErrors('','a'.repeat(50001)),at:window.programErrors('','a'.repeat(50000))}));
 assert.equal(limits.count,3);assert.equal(limits.at.length,0);assert.equal(limits.over.length,1);
 for(const width of [1280,375]) {await page.setViewportSize({width,height:900});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);await page.screenshot({path:`/tmp/kjfp-editor-${width}.png`,fullPage:true});}
 assert.deepEqual(errors,[]); console.log('PASS: formatting, lists, remove formatting, paste security, collapse state, title default, limits, 375/1280px layout');
} finally {await browser.close();}
