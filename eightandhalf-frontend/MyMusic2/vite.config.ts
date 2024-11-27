import { ConfigEnv, UserConfig, loadEnv, defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
// 定义组件名称
import DefineOptions from "unplugin-vue-define-options/vite";
import path from "path";


import Icons from "unplugin-icons/vite";


// 自动导入规则配置


// 使用本地svg
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";

// import UnoCSS from "unocss/vite";

// 获取当前文件的绝对路径
const pathSrc = path.resolve(__dirname, "src");

export default defineConfig(({ mode }: ConfigEnv): UserConfig => {
  // 获取环境变量env
  // 根据当前工作目录中的 `mode` 加载 .env 文件
  // 设置第三个参数为 '' 来加载所有环境变量，而不管是否有 `VITE_` 前缀。
  const env = loadEnv(mode, process.cwd());

  return {
    // 配置路径别名
    resolve: {
      alias: {
        "@": pathSrc,
      },
    },
    css: {
      // css预处理器配置
      preprocessorOptions: {
        // 定义全局scss变量
        scss: {
          javascriptEnabled: true,
          additionalData: `
            @use "@/styles/variables.scss" as *;
          `,
        },
      },
    },
    server: {
      // 指定服务器应该监听哪个 IP 地址。 如果将此设置为 0.0.0.0 或者 true 将监听所有地址，包括局域网和公网地址
      host: "0.0.0.0",
      port: Number(env.VITE_APP_PORT),
      // 是否自动打开浏览器
      open: true,
      // proxy: {
      //   // 反向代理解决跨域
      //   // env.VITE_APP_BASE_API：本地地址
      //   [env.VITE_APP_BASE_API]: {
      //     // env.VITE_APP_TARGET_URL：请求地址，到时候会将本地地址替换成请求地址
      //     target: env.VITE_APP_TARGET_URL,
      //     // 为true时会将请求头地址改为target地址
      //     changeOrigin: true,
      //     rewrite: (path) =>
      //       path.replace(
      //         new RegExp("^" + env.VITE_APP_BASE_API),
      //         env.VITE_APP_TARGET_BASE_URL
      //       ), // 替换 /dev-api 为 target 接口地址
      //   },
      // },
    },
    plugins: [
      // 需要加载该插件来解析.vue单文件组件
      vue(),
      // UnoCSS({
      //   // 手动指定unocss.config.ts配置文件位置
      //   configFile: './unocss.config.ts'
      // }),
      DefineOptions(),
      // 使用unplugin-element-plus对使用到的组件样式进行按需导入
     
     
    
      Icons({
        // 自动安装图标库
        autoInstall: true,
      }),
      // 配置svg使用规则(本地svg)
      createSvgIconsPlugin({
        // 指定本地svg图标的文件夹路径
        iconDirs: [path.resolve(pathSrc, "assets/icons")],
        // 指定symbolId格式
        symbolId: "icon-[dir]-[name]",
      }),
    ],
    // 预加载项目必需的组件
    optimizeDeps: {
      include: [
        "vue",
        "vue-router",
        "pinia",
        "axios",
        "@vueuse/core",
        "path-to-regexp",
      ],
    },
    // 构建
    build: {
      // 每次运行时，Vite 都会检查 vite.config.ts 文件的修改时间，并将该时间戳添加到文件名中。
      // 这有助于在文件名中记录 Vite 的构建时间，以便在构建失败时进行调试。如果你希望在构建过程中禁用此功能，timestap: false,即可
      // timestap: false,
      chunkSizeWarningLimit: 2000, // 消除打包大小超过500kb警告
      minify: "terser", // Vite 2.6.x 以上需要配置 minify: "terser", terserOptions 才能生效
      terserOptions: {
        compress: {
          keep_infinity: true, // 防止 Infinity 被压缩成 1/0，这可能会导致 Chrome 上的性能问题
          drop_console: true, // 生产环境去除 console
          drop_debugger: true, // 生产环境去除 debugger
        },
        format: {
          comments: false, // 删除注释
        },
      },
      rollupOptions: {
        // 增加后既正常
        treeshake: false,
      }
    },
  };
});
