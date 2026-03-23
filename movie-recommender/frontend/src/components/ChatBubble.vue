<template>
  <div class="chat-bubble" :class="role">
    <div class="bubble-content" v-html="html"></div>
  </div>
</template>

<script setup>
import { ref, watchEffect } from 'vue'
import { marked } from 'marked'

const props = defineProps(['role', 'content', 'streaming'])

marked.setOptions({ breaks: true, gfm: true })

const html = ref('')

watchEffect(() => {
  const raw = props.content || ''
  const parsed = marked.parse(raw)
  html.value = parsed
  // 调试：在浏览器控制台查看是否正确解析
  if (raw.length > 0 && raw.length % 200 === 0) {
    console.log('[ChatBubble] content length:', raw.length, 'html starts with:', parsed.substring(0, 80))
  }
})
</script>

<style>
.chat-bubble { margin: 12px 0; display: flex; }
.chat-bubble.user { justify-content: flex-end; }
.chat-bubble.assistant { justify-content: flex-start; }
.bubble-content {
  max-width: 70%; padding: 12px 16px; border-radius: 12px;
  line-height: 1.6; word-break: break-word;
}
.user .bubble-content { background: #409eff; color: #fff; border-radius: 12px 12px 0 12px; }
.assistant .bubble-content { background: #f0f2f5; color: #333; border-radius: 12px 12px 12px 0; }

.assistant .bubble-content h1,
.assistant .bubble-content h2,
.assistant .bubble-content h3,
.assistant .bubble-content h4 {
  margin: 14px 0 8px 0;
  font-weight: 600;
  line-height: 1.4;
}
.assistant .bubble-content h3 { font-size: 1.15em; }
.assistant .bubble-content h4 { font-size: 1.05em; }
.assistant .bubble-content p { margin: 6px 0; }
.assistant .bubble-content ul,
.assistant .bubble-content ol {
  margin: 6px 0;
  padding-left: 20px;
}
.assistant .bubble-content li { margin: 4px 0; }
.assistant .bubble-content hr {
  margin: 12px 0;
  border: none;
  border-top: 1px solid #ddd;
}
.assistant .bubble-content strong { font-weight: 700; }
.assistant .bubble-content code {
  background: #e8e8e8;
  padding: 2px 5px;
  border-radius: 3px;
  font-size: 0.9em;
}
</style>
