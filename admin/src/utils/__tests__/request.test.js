// src/utils/__tests__/request.test.js
import { describe, it, expect, vi } from 'vitest'
import request from '../request'

describe('request 工具函数', () => {
  it('应该存在', () => {
    expect(request).toBeDefined()
  })

  it('应该有 get 方法', () => {
    expect(request.get).toBeDefined()
    expect(typeof request.get).toBe('function')
  })

  it('应该有 post 方法', () => {
    expect(request.post).toBeDefined()
    expect(typeof request.post).toBe('function')
  })

  it('应该有 put 方法', () => {
    expect(request.put).toBeDefined()
    expect(typeof request.put).toBe('function')
  })

  it('应该有 delete 方法', () => {
    expect(request.delete).toBeDefined()
    expect(typeof request.delete).toBe('function')
  })
})
