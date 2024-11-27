// src/types/pinia-plugin-persistedstate.d.ts
import 'pinia';
import { PersistenceOptions } from 'pinia-plugin-persistedstate';

declare module 'pinia' {
  // 扩展 DefineStoreOptionsBase 接口
  export interface DefineStoreOptionsBase<S, Store> {
    persist?: boolean | PersistenceOptions | PersistenceOptions[];
  }
}
