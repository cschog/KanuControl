import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { fileURLToPath } from "node:url";
import { realpathSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
const root = realpathSync(fileURLToPath(new URL("../..", import.meta.url)));
export default defineConfig({
  root,
  plugins: [react()],
  cacheDir: join(tmpdir(), "kanucontrol-programmentwurf-vite"),
  resolve: { alias: { "@": `${root}/src` } },
  optimizeDeps: { entries: ["tests/programmentwurf/index.html"] },
  server: { host: "127.0.0.1", port: 5177, fs: { allow: [root, realpathSync(`${root}/node_modules`)] } },
});
