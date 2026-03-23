<template>
  <div class="chat-layout">
    <Sidebar :conversations="chatStore.conversations"
             :currentId="chatStore.currentConversationId"
             @new-chat="newChat" @select="selectConversation"
             @delete="deleteConversation" @logout="handleLogout" />

    <div class="chat-main">
      <div class="film-grain-overlay"></div>

      <!-- 欢迎页 -->
      <div v-if="!chatStore.messages.length && !loading" class="welcome">
        <div class="welcome-projector">
          <div class="proj-ring"><div class="proj-core"></div></div>
          <div class="proj-beam"></div>
        </div>
        <h2>今晚想看什么？</h2>
        <p class="welcome-sub">描述你的心情或偏好，AI 为你选片放映</p>
        <div class="quick-chips">
          <button v-for="tag in quickTags" :key="tag" class="chip" @click="quickAsk(tag)">
            <span class="chip-dot"></span>{{ tag }}
          </button>
        </div>
      </div>

      <!-- 消息区 -->
      <div v-else class="messages" ref="messagesRef">
        <div class="messages-track">
          <div v-for="(msg, i) in chatStore.messages" :key="i" class="msg-row" :class="msg.role">
            <div class="msg-avatar" :class="msg.role">
              <!-- AI: 放映镜头 -->
              <div v-if="msg.role === 'assistant'" class="avatar-lens">
                <div class="avatar-dot"></div>
              </div>
              <!-- 用户 -->
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <div class="msg-body">
              <div class="msg-who">{{ msg.role === 'assistant' ? 'MovieAI' : '你' }}</div>
              <div class="msg-text" v-html="renderMarkdown(msg.content)"></div>
            </div>
          </div>

          <!-- 等待动画 -->
          <div v-if="loading && !streaming" class="msg-row assistant">
            <div class="msg-avatar assistant">
              <div class="avatar-lens"><div class="avatar-dot"></div></div>
            </div>
            <div class="msg-body">
              <div class="msg-who">MovieAI</div>
              <div class="typing-reel">
                <span class="reel-dot"></span><span class="reel-dot"></span><span class="reel-dot"></span>
              </div>
            </div>
          </div>

          <!-- 推荐电影卡片 -->
          <div v-if="chatStore.recommendedMovies.length" class="reco-strip">
            <div class="reco-label">
              <span class="reco-line"></span>
              <span>数据库匹配 · 点击胶片收藏</span>
              <span class="reco-line"></span>
            </div>
            <div class="reco-scroll">
              <div v-for="movie in chatStore.recommendedMovies" :key="movie.movieId"
                   class="film-card" @click="handleFavorite(movie.movieId)">
                <div class="film-frame">
                  <div class="frame-holes top"><i/><i/><i/></div>
                  <div class="frame-img">
                    <img v-if="movie.posterUrl" :src="movie.posterUrl" />
                    <div v-else class="no-poster">NO POSTER</div>
                    <div class="frame-hover" :class="{ saved: favorited[movie.movieId] }">
                      <svg width="22" height="22" viewBox="0 0 24 24"
                           :fill="favorited[movie.movieId] ? '#dba148' : 'none'"
                           :stroke="favorited[movie.movieId] ? '#dba148' : '#fff'" stroke-width="2">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                      </svg>
                      <span>{{ favorited[movie.movieId] ? '已收藏' : '收藏' }}</span>
                    </div>
                  </div>
                  <div class="frame-holes bottom"><i/><i/><i/></div>
                </div>
                <div class="film-card-meta">
                  <div class="fc-title">{{ movie.title }}</div>
                  <div class="fc-sub">
                    <span v-if="movie.voteAverage" class="fc-score">{{ movie.voteAverage }}</span>
                    <span v-if="movie.genres" class="fc-genre">{{ movie.genres }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-dock">
        <div class="input-bar" :class="{ focused: inputFocused }">
          <div class="bar-glow"></div>
          <input
            v-model="input"
            @keyup.enter="sendMessage"
            @focus="inputFocused = true"
            @blur="inputFocused = false"
            :disabled="loading"
            placeholder="描述你想看的电影..."
            class="the-input"
          />
          <button class="send-btn" @click="sendMessage" :disabled="loading || !input.trim()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
        <div class="input-footnote">MovieAI · RAG 智能推荐引擎 · 优先匹配数据库电影</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { useUserStore } from '../stores/user'
import { createSSEConnection } from '../api/chat'
import { addFavorite, searchMovies } from '../api/favorite'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import Sidebar from '../components/Sidebar.vue'

marked.setOptions({ breaks: true, gfm: true })

const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()
const input = ref('')
const loading = ref(false)
const streaming = ref(false)
const inputFocused = ref(false)
const messagesRef = ref(null)
const favorited = reactive({})
const quickTags = ['推荐几部科幻片', '轻松搞笑的喜剧', '高分悬疑电影', '适合周末的家庭片', '经典动作片']

onMounted(() => { chatStore.loadConversations() })

function renderMarkdown(text) {
  if (!text) return ''
  return marked.parse(text)
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

async function extractAndSearchMovies(content) {
  const matches = content.match(/《([^》]+)》/g)
  if (!matches) return
  const titles = [...new Set(matches.map(m => m.slice(1, -1)))]
  const allMovies = [], seen = new Set()
  for (const title of titles) {
    try {
      const res = await searchMovies(title)
      if (res.data) for (const movie of res.data)
        if (!seen.has(movie.movieId)) { seen.add(movie.movieId); allMovies.push(movie) }
    } catch (e) { /* ignore */ }
  }
  if (allMovies.length) { chatStore.setRecommendedMovies(allMovies); scrollToBottom() }
}

function newChat() {
  chatStore.currentConversationId = null
  chatStore.messages = []
  chatStore.recommendedMovies = []
  Object.keys(favorited).forEach(k => delete favorited[k])
}

async function selectConversation(id) {
  await chatStore.loadMessages(id)
  chatStore.recommendedMovies = []
  Object.keys(favorited).forEach(k => delete favorited[k])
  scrollToBottom()
}

async function deleteConversation(id) { await chatStore.deleteConversation(id) }
function quickAsk(tag) { input.value = tag; sendMessage() }

function sendMessage() {
  if (!input.value.trim() || loading.value) return
  const message = input.value.trim()
  input.value = ''
  loading.value = true
  chatStore.recommendedMovies = []
  Object.keys(favorited).forEach(k => delete favorited[k])
  chatStore.addMessage('user', message)
  streaming.value = false
  scrollToBottom()

  createSSEConnection(
    chatStore.currentConversationId, message,
    (text) => {
      if (!streaming.value) { streaming.value = true; chatStore.addMessage('assistant', '') }
      chatStore.appendToLastAssistant(text); scrollToBottom()
    },
    () => {
      loading.value = false; streaming.value = false; chatStore.loadConversations()
      const last = chatStore.messages[chatStore.messages.length - 1]
      if (last?.role === 'assistant' && last.content) extractAndSearchMovies(last.content)
      scrollToBottom()
    },
    (movies) => { if (movies?.length) { chatStore.setRecommendedMovies(movies); scrollToBottom() } }
  )
}

async function handleFavorite(movieId) {
  if (favorited[movieId]) return
  try {
    await addFavorite(movieId); favorited[movieId] = true
    ElMessage({ message: '已收藏', type: 'success', duration: 1500 })
  } catch (e) {
    const msg = e.response?.data?.message || '收藏失败'
    if (msg.includes('已收藏')) { favorited[movieId] = true; ElMessage.info('已收藏') }
    else ElMessage.error(msg)
  }
}

function handleLogout() { userStore.logout(); router.push('/login') }
</script>

<style>
/* ===== Layout ===== */
.chat-layout { display: flex; height: 100vh; background: var(--bg-void); }
.chat-main { flex: 1; display: flex; flex-direction: column; position: relative; min-width: 0; }

/* 胶片颗粒叠加 */
.film-grain-overlay {
  position: absolute; inset: 0; pointer-events: none; z-index: 0;
  opacity: 0.18; mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='g'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23g)' opacity='0.6'/%3E%3C/svg%3E");
  background-size: 100px;
}

/* ===== Welcome ===== */
.welcome {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 40px; position: relative; z-index: 1;
}
.welcome-projector { position: relative; margin-bottom: 28px; }
.proj-ring {
  width: 80px; height: 80px; border-radius: 50%;
  border: 2px solid var(--film-gold);
  display: flex; align-items: center; justify-content: center;
  position: relative;
  background: conic-gradient(var(--film-gold), transparent 30%, var(--ai-blue), transparent 70%, var(--film-gold));
  -webkit-mask: radial-gradient(transparent 52%, #000 55%);
  mask: radial-gradient(transparent 52%, #000 55%);
  animation: ring-rotate 15s linear infinite;
}
@keyframes ring-rotate { to { transform: rotate(360deg); } }
.proj-core {
  position: absolute;
  width: 28px; height: 28px; border-radius: 50%;
  background: radial-gradient(circle, var(--ai-blue), var(--film-amber) 70%, transparent 100%);
  box-shadow: 0 0 30px var(--ai-glow), 0 0 50px rgba(219,161,72,0.12);
}
.proj-beam {
  position: absolute; top: 80px; left: 50%; transform: translateX(-50%);
  width: 2px; height: 40px;
  background: linear-gradient(to bottom, var(--film-gold), transparent);
  opacity: 0.4;
}
.welcome h2 { font-size: 28px; font-weight: 700; color: var(--text-1); margin-bottom: 6px; }
.welcome-sub { font-size: 14px; color: var(--text-2); margin-bottom: 32px; }

.quick-chips { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
.chip {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 18px; background: var(--bg-frame);
  border: 1px solid var(--border); color: var(--text-2);
  font-size: 13px; cursor: pointer; transition: all 0.2s;
  font-family: 'Instrument Sans', sans-serif;
}
.chip-dot {
  width: 5px; height: 5px; border-radius: 50%;
  background: var(--film-gold); opacity: 0.5;
  transition: opacity 0.2s;
}
.chip:hover {
  border-color: var(--film-gold); color: var(--film-gold);
  background: var(--film-warm);
}
.chip:hover .chip-dot { opacity: 1; }

/* ===== Messages ===== */
.messages { flex: 1; overflow-y: auto; position: relative; z-index: 1; }
.messages-track { max-width: 780px; margin: 0 auto; padding: 28px 24px 16px; }

.msg-row {
  display: flex; gap: 14px; padding: 18px 0;
  animation: msg-in 0.3s ease;
}
.msg-row + .msg-row { border-top: 1px solid var(--border); }
@keyframes msg-in { from { opacity: 0; transform: translateY(8px); } }

.msg-avatar {
  flex-shrink: 0; width: 34px; height: 34px;
  display: flex; align-items: center; justify-content: center;
}
.msg-avatar.user {
  border-radius: 50%; border: 1px solid var(--border-bright);
  color: var(--film-gold);
}
.msg-avatar.assistant { position: relative; }
.avatar-lens {
  width: 34px; height: 34px; border-radius: 50%;
  border: 1.5px solid var(--film-gold);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 0 12px rgba(219,161,72,0.1);
}
.avatar-dot {
  width: 10px; height: 10px; border-radius: 50%;
  background: var(--ai-blue);
  box-shadow: 0 0 8px var(--ai-glow);
}

.msg-who {
  font-family: 'Syne', sans-serif; font-size: 12px;
  font-weight: 600; color: var(--text-2); margin-bottom: 6px;
  letter-spacing: 0.3px;
}

.msg-text { font-size: 14.5px; line-height: 1.75; color: var(--text-1); }
.msg-text h1,.msg-text h2,.msg-text h3,.msg-text h4 {
  margin: 16px 0 8px; font-weight: 700; color: var(--film-warm);
}
.msg-text h3 { font-size: 1.05em; color: var(--film-gold); }
.msg-text p { margin: 5px 0; }
.msg-text ul,.msg-text ol { margin: 6px 0; padding-left: 20px; }
.msg-text li { margin: 3px 0; }
.msg-text strong { font-weight: 650; color: var(--text-1); }
.msg-text hr { margin: 14px 0; border: none; border-top: 1px solid var(--border); }
.msg-text code {
  background: var(--bg-cell); padding: 2px 6px; border-radius: 3px;
  font-size: 0.88em; color: var(--ai-blue);
  font-family: 'Menlo','Monaco',monospace;
}
.msg-text blockquote {
  margin: 8px 0; padding: 8px 14px;
  border-left: 2px solid var(--film-gold);
  background: var(--film-warm); color: var(--text-2);
}

/* 打字动画 */
.typing-reel { display: flex; gap: 6px; padding: 6px 0; }
.reel-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: var(--film-gold);
  animation: reel-bounce 1.4s ease-in-out infinite;
}
.reel-dot:nth-child(2) { animation-delay: 0.15s; }
.reel-dot:nth-child(3) { animation-delay: 0.3s; }
@keyframes reel-bounce {
  0%,80%,100% { opacity: 0.25; transform: scale(0.7); }
  40% { opacity: 1; transform: scale(1); }
}

/* ===== 电影卡片（胶片条风格）===== */
.reco-strip {
  margin: 20px 0 8px; padding: 20px;
  background: var(--bg-film);
  border: 1px solid var(--border);
}
.reco-label {
  display: flex; align-items: center; gap: 12px;
  font-size: 11px; color: var(--text-3); letter-spacing: 0.5px;
  margin-bottom: 16px; text-transform: uppercase; font-weight: 600;
}
.reco-line { flex: 1; height: 1px; background: var(--border); }
.reco-scroll {
  display: flex; gap: 16px; overflow-x: auto;
  padding-bottom: 6px; scroll-snap-type: x mandatory;
}

.film-card {
  flex-shrink: 0; width: 148px; cursor: pointer;
  transition: transform 0.25s; scroll-snap-align: start;
}
.film-card:hover { transform: translateY(-4px); }

.film-frame { background: var(--bg-void); border: 1px solid var(--border-bright); }
.frame-holes { display: flex; justify-content: space-around; padding: 4px 8px; }
.frame-holes i {
  display: block; width: 10px; height: 6px;
  border: 1px solid var(--sprocket); border-radius: 1px;
}
.frame-img { position: relative; width: 100%; aspect-ratio: 2/3; overflow: hidden; }
.frame-img img { width: 100%; height: 100%; object-fit: cover; display: block; }
.no-poster {
  width: 100%; height: 100%; display: flex;
  align-items: center; justify-content: center;
  background: var(--bg-frame); color: var(--text-3); font-size: 11px;
  letter-spacing: 1px;
}
.frame-hover {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.65);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 4px;
  opacity: 0; transition: opacity 0.2s;
  color: #fff; font-size: 11px; font-weight: 500;
}
.frame-hover.saved { opacity: 1; background: rgba(0,0,0,0.5); }
.film-card:hover .frame-hover { opacity: 1; }

.film-card-meta { padding: 8px 4px; }
.fc-title {
  font-size: 12px; font-weight: 600; color: var(--text-1);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.fc-sub { display: flex; align-items: center; gap: 6px; margin-top: 3px; }
.fc-score {
  font-size: 11px; font-weight: 700; color: var(--film-gold);
  background: rgba(219,161,72,0.1);
  padding: 0 5px; border-radius: 2px;
}
.fc-genre {
  font-size: 10px; color: var(--text-3);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

/* ===== Input Dock ===== */
.input-dock {
  position: relative; z-index: 1;
  padding: 14px 24px 18px;
  display: flex; flex-direction: column; align-items: center;
}
.input-bar {
  display: flex; align-items: center;
  width: 100%; max-width: 780px;
  background: var(--bg-film);
  border: 1px solid var(--border);
  padding: 5px 5px 5px 20px;
  position: relative; overflow: hidden;
  transition: border-color 0.3s;
}
.input-bar.focused { border-color: var(--film-gold); }
.bar-glow {
  position: absolute; inset: 0; pointer-events: none;
  opacity: 0; transition: opacity 0.3s;
  background: linear-gradient(90deg, rgba(219,161,72,0.04), rgba(86,200,216,0.03), rgba(219,161,72,0.04));
}
.input-bar.focused .bar-glow { opacity: 1; }

.the-input {
  flex: 1; border: none; outline: none; background: none;
  color: var(--text-1); font-size: 14px; padding: 10px 0;
  font-family: 'Instrument Sans', sans-serif;
}
.the-input::placeholder { color: var(--text-3); }

.send-btn {
  width: 40px; height: 40px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  background: var(--film-gold); border: none;
  color: var(--bg-void); cursor: pointer;
  transition: all 0.2s;
}
.send-btn:hover:not(:disabled) { background: var(--film-amber); transform: scale(1.05); }
.send-btn:disabled { opacity: 0.3; cursor: not-allowed; }

.input-footnote {
  margin-top: 8px; font-size: 10px; color: var(--text-3);
  letter-spacing: 0.4px;
}
</style>
