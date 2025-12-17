
// 环境变量处理
// 优先使用 build 注入的环境变量，如果没有则使用默认值
// 注意：在小程序环境中，process.env 可能会被替换为具体的值，或者为空
const ENV_BASE_URL = process.env.VUE_APP_SERVER_BASE_URL;

// 默认服务器地址 (作为兜底)
const DEFAULT_SERVER_URL = 'http://162.14.106.139:8080';

export const SERVER_BASE_URL = ENV_BASE_URL || DEFAULT_SERVER_URL;
