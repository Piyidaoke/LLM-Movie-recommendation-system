<template>
  <div class="sidebar">
    <!-- 左侧齿孔条 -->
    <div class="sprocket-strip">
      <div class="sprocket-hole" v-for="n in 20" :key="n"></div>
    </div>

    <div class="sidebar-inner">
      <div class="sidebar-brand">
        <div class="brand-lens">
          <div class="lens-dot"></div>
        </div>
        <span class="brand-text">Movie<span class="brand-ai">AI</span></span>
      </div>

      <button class="new-chat-btn" @click="$emit('new-chat')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        新对话
      </button>

      <div class="conv-section">
        <div class="section-label">放映记录</div>
        <div class="conversation-list">
          <div v-for="conv in conversations" :key="conv.conversationId"
               class="conv-item" :class="{ active: conv.conversationId === currentId }"
               @click="$emit('select', conv.conversationId)">
            <div class="conv-indicator"></div>
            <span class="conv-title">{{ conv.title }}</span>
            <button class="del-btn" @click.stop="$emit('delete', conv.conversationId)">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div v-if="!conversations.length" class="empty-conv">暂无放映记录</div>
        </div>
      </div>

      <div class="sidebar-footer">
        <button class="foot-btn" @click="$router.push('/favorites')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
          我的收藏
        </button>
        <button class="foot-btn exit-btn" @click="$emit('logout')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          退出
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps(['conversations', 'currentId'])
defineEmits(['new-chat', 'select', 'delete', 'logout'])
</script>

<style scoped>
.sidebar {
  width: 268px; height: 100vh; display: flex;
  background: var(--bg-film);
  border-right: 1px solid var(--border);
}

/* 胶片齿孔条 */
.sprocket-strip {
  width: 22px; flex-shrink: 0;
  background: var(--bg-void);
  border-right: 1px solid var(--border);
  display: flex; flex-direction: column;
  align-items: center; padding: 12px 0; gap: 10px;
  overflow: hidden;
}
.sprocket-hole {
  width: 10px; height: 7px; flex-shrink: 0;
  border: 1px solid var(--sprocket);
  border-radius: 1px;
  background: var(--bg-void);
}

.sidebar-inner {
  flex: 1; display: flex; flex-direction: column;
  padding: 16px 14px; min-width: 0;
}

.sidebar-brand {
  display: flex; align-items: center; gap: 10px;
  padding: 2px 0; margin-bottom: 18px;
}
.brand-lens {
  width: 30px; height: 30px; border-radius: 50%;
  border: 2px solid var(--film-gold);
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.lens-dot {
  width: 10px; height: 10px; border-radius: 50%;
  background: var(--ai-blue);
  box-shadow: 0 0 10px var(--ai-glow);
  animation: dot-breathe 3s ease-in-out infinite;
}
@keyframes dot-breathe {
  0%,100% { opacity: 0.7; transform: scale(0.9); }
  50% { opacity: 1; transform: scale(1.1); }
}
.brand-text { font-family: 'Syne', sans-serif; font-size: 18px; font-weight: 700; color: var(--text-1); }
.brand-ai { color: var(--ai-blue); }

/* 新对话按钮 */
.new-chat-btn {
  display: flex; align-items: center; justify-content: center; gap: 7px;
  width: 100%; padding: 11px;
  background: transparent;
  border: 1px dashed var(--border-bright);
  color: var(--film-gold); font-size: 13px; font-weight: 500;
  cursor: pointer; transition: all 0.2s;
  font-family: 'Instrument Sans', sans-serif;
}
.new-chat-btn:hover {
  background: var(--film-warm);
  border-style: solid;
}

/* 对话列表 */
.conv-section { flex: 1; display: flex; flex-direction: column; margin-top: 16px; min-height: 0; }
.section-label {
  font-size: 10px; font-weight: 600; text-transform: uppercase;
  letter-spacing: 1.2px; color: var(--text-3);
  padding: 0 4px; margin-bottom: 8px;
}
.conversation-list { flex: 1; overflow-y: auto; }

.conv-item {
  display: flex; align-items: center; gap: 8px;
  padding: 9px 8px; cursor: pointer;
  border-left: 2px solid transparent;
  transition: all 0.15s; margin-bottom: 1px;
}
.conv-item:hover { background: var(--bg-hover); border-left-color: var(--text-3); }
.conv-item.active { background: var(--bg-active); border-left-color: var(--film-gold); }
.conv-indicator {
  width: 5px; height: 5px; border-radius: 50%; flex-shrink: 0;
  background: var(--text-3); transition: background 0.15s;
}
.conv-item.active .conv-indicator { background: var(--film-gold); box-shadow: 0 0 6px rgba(219,161,72,0.4); }
.conv-title {
  flex: 1; font-size: 12.5px; color: var(--text-2);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  transition: color 0.15s;
}
.conv-item.active .conv-title { color: var(--text-1); }
.del-btn {
  opacity: 0; background: none; border: none;
  color: var(--text-3); cursor: pointer; padding: 3px;
  transition: all 0.15s; flex-shrink: 0;
}
.del-btn:hover { color: #e85d5d; }
.conv-item:hover .del-btn { opacity: 1; }
.empty-conv { font-size: 12px; color: var(--text-3); text-align: center; padding: 24px 0; }

/* 底部 */
.sidebar-footer {
  border-top: 1px solid var(--border);
  padding-top: 10px; margin-top: 8px;
  display: flex; flex-direction: column; gap: 2px;
}
.foot-btn {
  display: flex; align-items: center; gap: 8px;
  width: 100%; padding: 9px 8px;
  background: none; border: none; color: var(--text-2);
  font-size: 12.5px; cursor: pointer; transition: all 0.15s;
  font-family: 'Instrument Sans', sans-serif;
}
.foot-btn:hover { color: var(--film-gold); background: var(--film-warm); }
.exit-btn:hover { color: #e85d5d; background: rgba(232,93,93,0.06); }
</style>
