<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>{{ isLogin ? '登录' : '注册' }} — 电影推荐助手</h2>
      <el-form :model="form" @submit.prevent="handleSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item v-if="!isLogin">
          <el-input v-model="form.email" placeholder="邮箱" prefix-icon="Message" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码"
                    prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width:100%">
          {{ isLogin ? '登录' : '注册' }}
        </el-button>
      </el-form>
      <div class="switch-mode">
        <el-link @click="isLogin = !isLogin">
          {{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
        </el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const isLogin = ref(true)
const loading = ref(false)
const form = ref({ username: '', email: '', password: REDACTED })

async function handleSubmit() {
  loading.value = true
  try {
    if (isLogin.value) {
      const res = await login({ username: form.value.username, password: REDACTED })
      userStore.setLogin(res.data)
      ElMessage.success('登录成功')
      router.push('/chat')
    } else {
      await register(form.value)
      ElMessage.success('注册成功，请登录')
      isLogin.value = true
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background: #f0f2f5;
}
.login-card { width: 400px; padding: 20px; }
.switch-mode { text-align: center; margin-top: 16px; }
</style>
