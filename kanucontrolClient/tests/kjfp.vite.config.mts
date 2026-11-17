import {defineConfig} from "vite";
import react from "@vitejs/plugin-react";
import {fileURLToPath} from "node:url";
import {realpathSync} from "node:fs";
import {tmpdir} from "node:os";
import {join} from "node:path";
import {createHash} from "node:crypto";
const root=realpathSync(fileURLToPath(new URL("..",import.meta.url)));
const port=Number(process.env.KJFP_TEST_PORT||5177);
export default defineConfig({
  root,plugins:[react()],
  cacheDir:join(tmpdir(),`kanucontrol-kjfp-${createHash('sha256').update(root).digest('hex').slice(0,12)}-${port}`),
  resolve:{alias:{"@":`${root}/src`},dedupe:["react","react-dom"]},
  optimizeDeps:{entries:["programmentwurf","veranstaltung-kopie","belegdrehung","kjfp-simulation"].map(name=>`tests/${name}/index.html`)},
  server:{host:"127.0.0.1",port,strictPort:true,fs:{allow:[root,realpathSync(`${root}/node_modules`)]}},
});
