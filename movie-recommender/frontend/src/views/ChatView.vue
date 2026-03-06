<template>
  <div class="chat-layout">
    <Sidebar :conversations="chatStore.conversations"
             :currentId="chatStore.currentConversationId"
             @new-chat="newChat" @select="selectConversation"
             @delete="deleteConversation" @logout="handleLogout" />
    <div class="chat-main">
      <div class="messages" ref="messagesRef">
        <ChatBubble v-for="(msg, i) in chatStore.messages" :key="i"
                    :role="msg.role" :content="msg.content" />
        <div v-if="loading" class="typing-indicator">AI 正在思考...</div>
      </div>
      <div class="input-area">
        <el-input v-model="input" placeholder="输入你想看的电影类型、心情或任何描述..."
                  @keyup.enter="sendMessage" :disabled="loading">
          <template #append>
            <el-button @click="sendMessage" :loading="loading" type="primary">发送</el-button>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { useUserStore } from '../stores/user'
import { createSSEConnection } from '../api/chat'
import Sidebar from '../components/Sidebar.vue'
import ChatBubble from '../components/ChatBubble.vue'

const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()
const input = ref('')
const loading = ref(false)
const messagesRef = ref(null)

onMounted(() => { chatStore.loadConversations() })

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

function newChat() {
  chatStore.currentConversationId = null
  chatStore.messages = []
}

async function selectConversation(id) {
  await chatStore.loadMessages(id)
  scrollToBottom()
}

async function deleteConversation(id) {
  await chatStore.deleteConversation(id)
}

function sendMessage() {
  if (!input.value.trim() || loading.value) return
  const message = input.value.trim()
  input.value = ''
  loading.value = true

  chatStore.addMessage('user', message)
  chatStore.addMessage('assistant', '')
  scrollToBottom()

  createSSEConnection(
    chatStore.currentConversationId,
    message,
    (text) => {
      chatStore.appendToLastAssistant(text)
      scrollToBottom()
    },
    () => {
      loading.value = false
      chatStore.loadConversations()
      scrollToBottom()
    }
  )
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.chat-layout { display: flex; height: 100vh; }
.chat-main { flex: 1; display: flex; flex-direction: column; }
.messages { flex: 1; overflow-y: auto; padding: 24px; }
.input-area { padding: 16px 24px; border-top: 1px solid #e4e7ed; }
.typing-indicator { color: #999; padding: 8px 0; }
</style>
