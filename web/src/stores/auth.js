import { defineStore } from 'pinia'
import client from '@/api/client'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || ''
  }),
  actions: {
    async login(username, password) {
      const { data } = await client.post('/auth/login', { username, password })
      this.setSession(data)
    },
    async register(username, password) {
      const { data } = await client.post('/auth/register', { username, password })
      this.setSession(data)
    },
    setSession(data) {
      this.token = data.token
      this.username = data.username
      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
    },
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }
})
