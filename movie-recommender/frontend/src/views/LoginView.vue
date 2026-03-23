<template>
  <div class="login-scene">
    <!-- 放映机光束背景 -->
    <div class="projector-beam"></div>
    <div class="film-grain"></div>

    <div class="login-card">
      <div class="card-sprocket top"></div>

      <div class="login-header">
        <div class="projector-lens">
          <div class="lens-ring"></div>
          <div class="lens-core"></div>
        </div>
        <h1>Movie<span class="ai-text">AI</span></h1>
        <p class="tagline">智能电影推荐引擎</p>
      </div>

      <el-form :model="form" @submit.prevent="handleSubmit" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
        </el-form-item>
        <el-form-item v-if="!isLogin">
          <el-input v-model="form.email" placeholder="邮箱" size="large" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码"
                    size="large" :prefix-icon="Lock" show-password />
        </el-form-item>
        <button type="submit" class="film-btn" :disabled="loading">
          <span class="btn-light"></span>
          {{ isLogin ? '开始放映' : '注册账号' }}
        </button>
      </el-form>

      <div class="switch-row">
        <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
        <a @click="isLogin = !isLogin">{{ isLogin ? '立即注册' : '去登录' }}</a>
      </div>

      <div class="card-sprocket bottom"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Message, Lock } from '@element-plus/icons-vue'
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
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-scene {
  display: flex; justify-content: center; align-items: center;
  min-height: 100vh; position: relative;
  background: radial-gradient(ellipse at 50% 30%, #1a1726 0%, #0d0c11 70%);
  overflow: hidden;
}

/* 放映机光束 */
.projector-beam {
  position: absolute; top: -10%; left: 50%; transform: translateX(-50%);
  width: 500px; height: 120vh;
  background: conic-gradient(from 170deg at 50% 0%,
    transparent 0deg, rgba(219,161,72,0.03) 10deg,
    rgba(219,161,72,0.06) 20deg, rgba(219,161,72,0.03) 30deg, transparent 40deg);
  pointer-events: none;
}

/* 胶片颗粒 */
.film-grain {
  position: absolute; inset: 0; pointer-events: none; opacity: 0.3; mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.5'/%3E%3C/svg%3E");
  background-size: 128px;
}

.login-card {
  position: relative; z-index: 1; width: 400px; padding: 44px 36px;
  background: var(--bg-film);
  border: 1px solid var(--border-bright);
  border-radius: 2px;
  box-shadow: 0 0 80px rgba(219,161,72,0.06), 0 30px 60px rgba(0,0,0,0.5);
}

/* 齿孔装饰 */
.card-sprocket {
  position: absolute; left: 50%; transform: translateX(-50%);
  display: flex; gap: 16px;
}
.card-sprocket.top { top: -1px; }
.card-sprocket.bottom { bottom: -1px; }
.card-sprocket::before, .card-sprocket::after,
.card-sprocket { display: flex; gap: 14px; }
.card-sprocket.top::before, .card-sprocket.top::after,
.card-sprocket.bottom::before, .card-sprocket.bottom::after {
  content: '';
  width: 12px; height: 8px; border-radius: 1px;
  background: var(--bg-void);
  border: 1px solid var(--border-bright);
  display: block;
}

/* 放映机镜头 */
.login-header { text-align: center; margin-bottom: 32px; }
.projector-lens {
  width: 64px; height: 64px; margin: 0 auto 18px;
  position: relative; display: flex; align-items: center; justify-content: center;
}
.lens-ring {
  position: absolute; inset: 0; border-radius: 50%;
  border: 2px solid var(--film-gold);
  animation: lens-spin 12s linear infinite;
  background: conic-gradient(var(--film-gold), transparent, var(--ai-blue), transparent, var(--film-gold));
  -webkit-mask: radial-gradient(transparent 55%, #000 57%);
  mask: radial-gradient(transparent 55%, #000 57%);
}
@keyframes lens-spin { to { transform: rotate(360deg); } }

.lens-core {
  width: 28px; height: 28px; border-radius: 50%;
  background: radial-gradient(circle, var(--ai-blue) 0%, var(--film-gold) 60%, transparent 70%);
  box-shadow: 0 0 20px var(--ai-glow), 0 0 40px rgba(219,161,72,0.15);
  animation: core-pulse 3s ease-in-out infinite;
}
@keyframes core-pulse {
  0%,100% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.1); opacity: 1; }
}

.login-header h1 {
  font-size: 32px; font-weight: 800; color: var(--text-1);
  letter-spacing: -0.03em;
}
.ai-text {
  color: var(--ai-blue);
  text-shadow: 0 0 18px var(--ai-glow);
}
.tagline { color: var(--text-2); font-size: 13px; margin-top: 4px; letter-spacing: 0.5px; }

/* 表单 */
.login-form :deep(.el-input__wrapper) {
  background: var(--bg-frame); border-radius: 2px;
  box-shadow: inset 0 0 0 1px var(--border);
  transition: box-shadow 0.2s;
}
.login-form :deep(.el-input__wrapper:hover) { box-shadow: inset 0 0 0 1px var(--border-bright); }
.login-form :deep(.el-input__wrapper.is-focus) { box-shadow: inset 0 0 0 1px var(--film-gold), 0 0 12px rgba(219,161,72,0.08); }
.login-form :deep(.el-input__inner) { color: var(--text-1); }
.login-form :deep(.el-input__inner::placeholder) { color: var(--text-3); }
.login-form :deep(.el-input__prefix .el-icon) { color: var(--text-3); }
.login-form :deep(.el-form-item) { margin-bottom: 18px; }

/* 按钮 */
.film-btn {
  width: 100%; padding: 14px; border: 1px solid var(--film-gold);
  background: transparent; color: var(--film-gold);
  font-family: 'Syne', sans-serif; font-size: 15px; font-weight: 600;
  letter-spacing: 0.5px; cursor: pointer; position: relative;
  overflow: hidden; transition: all 0.3s;
}
.film-btn:hover { background: var(--film-gold); color: var(--bg-void); }
.film-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-light {
  position: absolute; top: 0; left: -100%; width: 60%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.08), transparent);
  transition: left 0.5s;
}
.film-btn:hover .btn-light { left: 120%; }

.switch-row { text-align: center; margin-top: 22px; font-size: 13px; color: var(--text-2); }
.switch-row a {
  color: var(--film-gold); cursor: pointer; margin-left: 4px;
  border-bottom: 1px solid transparent; transition: border-color 0.2s;
}
.switch-row a:hover { border-color: var(--film-gold); }
</style>
