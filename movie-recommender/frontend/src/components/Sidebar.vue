<template>
  <div class="sidebar">
    <el-button type="primary" @click="$emit('new-chat')" style="width:100%;margin-bottom:16px">
      + 新对话
    </el-button>
    <div class="conversation-list">
      <div v-for="conv in conversations" :key="conv.conversationId"
           class="conv-item" :class="{ active: conv.conversationId === currentId }"
           @click="$emit('select', conv.conversationId)">
        <span class="conv-title">{{ conv.title }}</span>
        <el-icon class="delete-btn" @click.stop="$emit('delete', conv.conversationId)">
          <Delete />
        </el-icon>
      </div>
    </div>
    <div class="sidebar-footer">
      <el-button text @click="$router.push('/favorites')">我的收藏</el-button>
      <el-button text @click="$emit('logout')">退出</el-button>
    </div>
  </div>
</template>

<script setup>
import { Delete } from '@element-plus/icons-vue'
defineProps(['conversations', 'currentId'])
defineEmits(['new-chat', 'select', 'delete', 'logout'])
</script>

<style scoped>
.sidebar { width: 260px; height: 100vh; background: #f7f8fa; padding: 16px;
           display: flex; flex-direction: column; border-right: 1px solid #e4e7ed; }
.conversation-list { flex: 1; overflow-y: auto; }
.conv-item { padding: 10px 12px; border-radius: 8px; cursor: pointer; display: flex;
             justify-content: space-between; align-items: center; margin-bottom: 4px; }
.conv-item:hover, .conv-item.active { background: #e8eaf0; }
.conv-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.delete-btn { opacity: 0; }
.conv-item:hover .delete-btn { opacity: 1; }
.sidebar-footer { border-top: 1px solid #e4e7ed; padding-top: 12px; }
</style>
