import { c as createCDNLoader, l as loadIcon } from './shared/preset-icons.e13a0979.mjs';
import { createPresetIcons, combineLoaders } from './core.mjs';
import 'ofetch';
import '@unocss/core';

async function createNodeLoader() {
  try {
    return await import('@iconify/utils/lib/loader/node-loader').then((i) => i?.loadNodeIcon);
  } catch {
  }
  try {
    return require("@iconify/utils/lib/loader/node-loader.cjs").loadNodeIcon;
  } catch {
  }
}
const presetIcons = createPresetIcons(async (options) => {
  const {
    cdn
  } = options;
  const loaders = [];
  const isNode = typeof process !== "undefined" && process.stdout && !process.versions.deno;
  const isVSCode = isNode && !!process.env.VSCODE_CWD;
  const isESLint = isNode && !!process.env.ESLINT;
  if (isNode && !isVSCode && !isESLint) {
    const nodeLoader = await createNodeLoader();
    if (nodeLoader !== void 0)
      loaders.push(nodeLoader);
  }
  if (cdn)
    loaders.push(createCDNLoader(cdn));
  loaders.push(loadIcon);
  return combineLoaders(loaders);
});

export { combineLoaders, createPresetIcons, presetIcons as default, presetIcons };
