import {spawn} from 'node:child_process';
import {once} from 'node:events';
import {fileURLToPath} from 'node:url';
import {setTimeout as delay} from 'node:timers/promises';
const root=fileURLToPath(new URL('..',import.meta.url));
// Ein gemeinsamer lokaler Server; Tests laufen sequenziell und verwenden API-Fixtures.
const host='127.0.0.1',port=Number(process.env.KJFP_TEST_PORT||5177);
const server=spawn(process.execPath,['node_modules/vite/bin/vite.js','--config','tests/kjfp.vite.config.mts','--host',host,'--port',String(port),'--strictPort'],{cwd:root,stdio:['ignore','pipe','pipe']});
let output='';for(const stream of [server.stdout,server.stderr])stream.on('data',data=>{output+=data;});
try {
 let ready=false;
 for(let attempt=0;attempt<100;attempt++){
  if(server.exitCode!==null)throw new Error(`Lokaler Testserver beendet: ${output}`);
  if(output.includes(`http://${host}:${port}/`)){ready=true;break;}
  await delay(100);
 }
 if(!ready)throw new Error(`Testserver startet nicht: ${output}`);
 for(const test of ['programmentwurf','veranstaltung-kopie','belegdrehung','kjfp-simulation']) {
  console.log(`KJFP: ${test}`);
  const child=spawn(process.execPath,[`tests/${test}/check.mjs`],{cwd:root,stdio:'inherit',env:{...process.env,TEST_URL:`http://${host}:${port}/tests/${test}/index.html`}});
  const [code]=await once(child,'exit');if(code!==0)throw new Error(`${test} fehlgeschlagen (${code}).`);
 }
}finally{if(server.exitCode===null){server.kill('SIGTERM');await once(server,'exit');}}
