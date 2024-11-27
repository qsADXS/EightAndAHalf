import { resolve, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import VitePlugin from '@unocss/vite';
import { normalizePath } from 'vite';

const RESOLVED_ID_RE = /[\/\\]__uno(?:(_.*?))?\.css$/;

const UNO_INJECT_ID = "uno-astro";
const astroCSSKeyRE = /(\?|\&)lang\.css/;
function AstroVitePlugin(options) {
  const { injects, injectReset } = options;
  const resetInjectPath = injectReset ? injects[0] : void 0;
  let command;
  let root;
  let resetCSSInjected = false;
  const resolveCSSQueue = /* @__PURE__ */ new Set();
  return {
    name: "unocss:astro",
    enforce: "pre",
    configResolved(config) {
      root = config.root;
      command = config.command;
    },
    async resolveId(id, importer) {
      if (RESOLVED_ID_RE.test(id)) {
        return this.resolve(normalizePath(join(root, id)), importer, { skipSelf: true });
      }
      if (id === UNO_INJECT_ID) {
        if (injectReset) {
          resetCSSInjected = false;
        }
        return id;
      }
      if (injectReset && command === "serve" && astroCSSKeyRE.test(id) && !resetCSSInjected)
        return new Promise((resolve2) => resolveCSSQueue.add(() => resolve2()));
      if (importer?.endsWith(UNO_INJECT_ID) && command === "serve") {
        const resolved = await this.resolve(id, importer, { skipSelf: true });
        if (resolved) {
          const fsPath = resolved.id;
          if (injectReset) {
            if (resetInjectPath.includes(id)) {
              setTimeout(() => {
                resolveCSSQueue.forEach((res) => {
                  res();
                  resolveCSSQueue.delete(res);
                });
              });
              resetCSSInjected = true;
            } else if (id.includes(".css") && !resetCSSInjected) {
              return new Promise((resolve2) => {
                resolveCSSQueue.add(() => {
                  resolve2(fsPath);
                });
              });
            }
          }
          return fsPath;
        }
      }
    },
    load(id) {
      if (id.endsWith(UNO_INJECT_ID))
        return injects.join("\n");
    }
  };
}
function UnoCSSAstroIntegration(options = {}, defaults) {
  const {
    injectEntry = true,
    injectReset = false,
    injectExtra = []
  } = options;
  return {
    name: "unocss",
    hooks: {
      "astro:config:setup": async ({ config, updateConfig, injectScript }) => {
        var _a;
        const source = resolve(fileURLToPath(config.srcDir), "components/**/*").replace(/\\/g, "/");
        options.content || (options.content = {});
        (_a = options.content).filesystem || (_a.filesystem = []);
        options.content.filesystem.push(source);
        const injects = [];
        if (injectReset) {
          const resetPath = typeof injectReset === "string" ? injectReset : "@unocss/reset/tailwind.css";
          injects.push(`import ${JSON.stringify(resetPath)}`);
        }
        if (injectEntry) {
          injects.push(typeof injectEntry === "string" ? injectEntry : 'import "uno.css"');
        }
        if (injectExtra.length > 0)
          injects.push(...injectExtra);
        updateConfig({
          vite: {
            plugins: [AstroVitePlugin({
              injectReset: !!injectReset,
              injects
            }), ...VitePlugin(options, defaults)]
          }
        });
        if (injects?.length)
          injectScript("page-ssr", `import ${JSON.stringify(UNO_INJECT_ID)}`);
      }
    }
  };
}

export { UnoCSSAstroIntegration as default };
