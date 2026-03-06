import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(null)
  const username = ref('')

  function setLogin(data) {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    localStorage.setItem('token', data.token)
  }

  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    localStorage.removeItem('token')
  }

  return { token, userId, username, setLogin, logout }
})
