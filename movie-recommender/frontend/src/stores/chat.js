import { defineStore } from 'pinia'
import { ref } from 'vue'
import http from '../api/http'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref([])
  const currentConversationId = ref(null)
  const messages = ref([])
  const recommendedMovies = ref([])

  async function loadConversations() {
    const res = await http.get('/conversations')
    conversations.value = res.data
  }

  async function loadMessages(conversationId) {
    currentConversationId.value = conversationId
    const res = await http.get(`/conversations/${conversationId}/messages`)
    messages.value = res.data
    recommendedMovies.value = []
  }

  async function deleteConversation(id) {
    await http.delete(`/conversations/${id}`)
    conversations.value = conversations.value.filter(c => c.conversationId !== id)
    if (currentConversationId.value === id) {
      currentConversationId.value = null
      messages.value = []
      recommendedMovies.value = []
    }
  }

  function addMessage(role, content) {
    messages.value.push({ role, content, createdAt: new Date().toISOString() })
  }

  function appendToLastAssistant(text) {
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') {
      last.content += text
    }
  }

  function setRecommendedMovies(movies) {
    recommendedMovies.value = movies
  }

  return {
    conversations, currentConversationId, messages, recommendedMovies,
    loadConversations, loadMessages, deleteConversation,
    addMessage, appendToLastAssistant, setRecommendedMovies
  }
})
