<template>
  <div class="chat-bubble" :class="role">
    <div class="bubble-content" v-html="renderedContent"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps(['role', 'content'])

const renderedContent = computed(() => {
  return marked.parse(props.content || '', { breaks: true })
})
</script>

<style scoped>
.chat-bubble { margin: 12px 0; display: flex; }
.chat-bubble.user { justify-content: flex-end; }
.chat-bubble.assistant { justify-content: flex-start; }
.bubble-content {
  max-width: 70%; padding: 12px 16px; border-radius: 12px;
  line-height: 1.6; word-break: break-word;
}
.user .bubble-content { background: #409eff; color: #fff; border-radius: 12px 12px 0 12px; }
.assistant .bubble-content { background: #f0f2f5; color: #333; border-radius: 12px 12px 12px 0; }
</style>
