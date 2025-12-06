// src/setupTests.js
import { describe, expect, it, vi, beforeEach } from 'vitest'
import { createApp } from 'vue'

// 全局测试设置
globalThis.describe = describe
globalThis.it = it
globalThis.expect = expect
globalThis.vi = vi
globalThis.beforeEach = beforeEach

// 模拟常用的全局函数或对象
vi.mock('axios', () => ({
  default: {
    get: vi.fn(() => Promise.resolve({ data: {} })),
    post: vi.fn(() => Promise.resolve({ data: {} })),
    put: vi.fn(() => Promise.resolve({ data: {} })),
    delete: vi.fn(() => Promise.resolve({ data: {} }))
  }
}))

// 模拟Vue Router
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
    replace: vi.fn()
  })),
  useRoute: vi.fn(() => ({
    params: {},
    query: {}
  }))
}))

// 模拟Pinia
vi.mock('pinia', () => ({
  defineStore: vi.fn(() => ({
    state: vi.fn(() => ({})),
    getters: {},
    actions: {}
  }))
}))
