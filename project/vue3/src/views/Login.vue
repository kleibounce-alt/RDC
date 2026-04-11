<template>
  <div class="login-container">
    <!-- 装饰背景元素 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="login-box glass">
      <div class="login-header">
        <div class="logo">🐟</div>
        <h2 class="text-gradient">闲鱼交易平台</h2>
        <p class="subtitle">安全便捷的二手交易</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="input-group">
          <div class="input-icon">👤</div>
          <input
              v-model="loginForm.userName"
              type="text"
              placeholder="用户名"
              required
          >
        </div>

        <div class="input-group">
          <div class="input-icon">🔒</div>
          <input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              required
          >
        </div>

        <button
            type="submit"
            class="btn-primary"
            :disabled="loading"
            :class="{ 'loading': loading }"
        >
          <span v-if="!loading">登 录</span>
          <span v-else class="spinner"></span>
        </button>
      </form>

      <div v-if="errorMsg" class="error-toast">
        <span>⚠️</span> {{ errorMsg }}
      </div>

      <div class="login-footer">
        <div class="divider">
          <span>还没有账号？</span>
        </div>
        <button class="btn-secondary" @click="emit('switch-to-register')">
          立即注册
        </button>
      </div>
    </div>

    <!-- 底部装饰 -->
    <div class="floating-icons">
      <span class="float-item" style="--i:1">📱</span>
      <span class="float-item" style="--i:2">💻</span>
      <span class="float-item" style="--i:3">📷</span>
      <span class="float-item" style="--i:4">👟</span>
      <span class="float-item" style="--i:5">🎮</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { login } from '@/api/user'

const emit = defineEmits(['login-success', 'switch-to-register'])

const loginForm = reactive({
  userName: '',
  password: ''
})

const loading = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  loading.value = true
  errorMsg.value = ''

  try {
    const res = await login(loginForm)
    if (res.code === 200 && res.data.success) {
      localStorage.setItem('userInfo', JSON.stringify({
        userId: res.data.userId,
        userName: res.data.userName,
        avatar: res.data.avatar,
        role: res.data.role
      }))
      emit('login-success')
    } else {
      errorMsg.value = res.message || '登录失败'
    }
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '网络错误'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰形状 */
.bg-shapes {
  position: fixed;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: -1;
  overflow: hidden;
}

.shape {
  position: absolute;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 10s infinite ease-in-out;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -100px;
  left: -100px;
  border-radius: 50%;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -50px;
  right: -50px;
  border-radius: 50%;
  animation-delay: -5s;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  animation-delay: -2s;
}

/* 登录框玻璃拟态 */
.login-box {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  border-radius: 24px;
  animation: fadeIn 0.6s ease;
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
}

.login-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  font-size: 64px;
  margin-bottom: 16px;
  animation: float 3s infinite ease-in-out;
  display: inline-block;
}

h2 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: #6b7280;
  font-size: 14px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  font-size: 18px;
  opacity: 0.6;
  z-index: 1;
}

.input-group input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border-radius: 12px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.6);
  border: 2px solid transparent;
  transition: all 0.2s ease;
  font-family: inherit;
}

.input-group input:focus {
  background: rgba(255, 255, 255, 0.9);
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.15);
  outline: none;
}

.btn-primary {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 8px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  position: relative;
  overflow: hidden;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

.error-toast {
  margin-top: 16px;
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 12px;
  color: #ef4444;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: shake 0.5s ease;
}

.login-footer {
  margin-top: 32px;
  text-align: center;
}

.divider {
  position: relative;
  margin-bottom: 16px;
  color: #9ca3af;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 30%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0,0,0,0.1), transparent);
}

.divider::before { left: 0; }
.divider::after { right: 0; }

.btn-secondary {
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.2s ease;
  cursor: pointer;
  font-size: 14px;
}

.btn-secondary:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

/* 底部浮动图标 */
.floating-icons {
  position: fixed;
  bottom: 40px;
  display: flex;
  gap: 20px;
  opacity: 0.6;
}

.float-item {
  font-size: 24px;
  animation: float 3s infinite ease-in-out;
  animation-delay: calc(var(--i) * 0.5s);
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
}

@media (max-width: 480px) {
  .login-box {
    padding: 32px 24px;
  }

  .floating-icons {
    display: none;
  }

  h2 {
    font-size: 24px;
  }
}
</style>