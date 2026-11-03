const path=require('node:path');
const client=path.resolve(__dirname, '../..');
const {createServer}=require(path.join(client,'node_modules/vite'));
const mocks={
 'auth':`export const isAdmin=()=>true;`,
 'context':`export const useVermietungContext=()=>({mietobjekt:{id:7}});`,
 'objekte':`export const getMietobjekte=async()=>[{id:7,bezeichnung:'Test-Bootshaus'}];`,
 'bereiche':`export const getMietbereiche=async()=>[{id:10,bezeichnung:'EG',mietbar:true,direktbuchungAktiv:true}];`,
 'config':`const empty=()=>({version:null,aktiv:false,technischerClientId:null,bezeichnung:'Homepage',homepageUrl:null,datenschutzUrl:null,hausregelnUrl:null,empfaengerEmail:null,gastBestaetigung:true,verwaltungsBenachrichtigung:false,teilnehmerPflicht:true,erlaubteMietbereichIds:[]}); export const getHomepageAnbindung=async()=>JSON.parse(localStorage.getItem('saved')||'null')||empty(); export const saveHomepageAnbindung=async(id,data)=>{window.__saveCalls=(window.__saveCalls||0)+1; if(window.__serverError)throw new Error('Test: Speichern fehlgeschlagen');const saved={...data,version:(data.version??-1)+1}; localStorage.setItem('saved',JSON.stringify(saved));return saved;};`
};
const replacements={'@/core/auth/useTenant':'auth','@/vermietung/context/VermietungContext':'context','@/vermietung/api/mietobjektApi':'objekte','@/vermietung/api/mietbereichApi':'bereiche','@/vermietung/api/homepageAnbindungApi':'config'};
(async()=>{const server=await createServer({configFile:false,root:client,server:{host:'127.0.0.1',port:18766,strictPort:true},resolve:{alias:{'@':path.join(client,'src')}},plugins:[{
 name:'isolated-component-test',enforce:'pre',resolveId(id){if(id.startsWith('virtual:'))return '\0'+id;},load(id){if(id==='\0virtual:entry')return `import React from 'react';import{createRoot}from'react-dom/client';import Component from '@/vermietung/components/verwaltung/HomepageAnbindungVerwaltung';createRoot(document.getElementById('root')).render(React.createElement(Component));`;if(id.startsWith('\0virtual:'))return mocks[id.slice('\0virtual:'.length)];},
 transform(code,id){if(id.endsWith('HomepageAnbindungVerwaltung.tsx')){for(const [from,to]of Object.entries(replacements))code=code.replaceAll('"'+from+'"','"virtual:'+to+'"');return code;}},
 configureServer(server){server.middlewares.use((req,res,next)=>{if(req.url==='/test'){res.setHeader('Content-Type','text/html');res.end('<html><body><div id="root"></div><script type="module" src="/@id/virtual:entry"></script></body></html>');}else next();});}
 } ]});await server.listen();console.log('Testserver http://127.0.0.1:18766/test');})();
