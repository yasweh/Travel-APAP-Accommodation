import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCounterStore } from '@/stores/counter'

describe('Counter Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  describe('Initial State', () => {
    it('should have count of 0 initially', () => {
      const store = useCounterStore()
      expect(store.count).toBe(0)
    })
  })

  describe('Getters', () => {
    it('doubleCount returns count * 2', () => {
      const store = useCounterStore()
      expect(store.doubleCount).toBe(0)
    })

    it('doubleCount updates when count changes', () => {
      const store = useCounterStore()
      store.increment()
      expect(store.doubleCount).toBe(2)
    })
  })

  describe('Actions', () => {
    it('increment increases count by 1', () => {
      const store = useCounterStore()
      store.increment()
      expect(store.count).toBe(1)
    })

    it('multiple increments work correctly', () => {
      const store = useCounterStore()
      store.increment()
      store.increment()
      store.increment()
      expect(store.count).toBe(3)
      expect(store.doubleCount).toBe(6)
    })
  })
})
