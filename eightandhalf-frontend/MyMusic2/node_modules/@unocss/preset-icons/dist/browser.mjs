import { c as createCDNLoader, l as loadIcon } from './shared/preset-icons.e13a0979.mjs';
import { createPresetIcons } from './core.mjs';
export { combineLoaders } from './core.mjs';
import 'ofetch';
import '@unocss/core';

const presetIcons = createPresetIcons(async (options) => {
  const { cdn } = options;
  if (cdn)
    return createCDNLoader(cdn);
  return loadIcon;
});

export { createPresetIcons, presetIcons as default, presetIcons };
