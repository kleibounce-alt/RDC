<template>
  <div class="login-container">
    <div class="login-box">
      <h2>闲鱼交易平台</h2>
      <form @submit.prevent="handleLogin">
        <div class="input-group">
          <input
              v-model="loginForm.userName"
              type="text"
              placeholder="用户名"
              required
          >
        </div>
        <div class="input-group">
          <input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              required
          >
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
      <p class="link">
        还没有账号？<a @click="emit('switch-to-register')">立即注册</a>
      </p>
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
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  width: 360px;
}
h2 { text-align: center; color: #333; margin-bottom: 30px; }
.input-group { margin-bottom: 20px; }
input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}
input:focus { outline: none; border-color: #667eea; }
button {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}
button:hover:not(:disabled) { background: #5568d3; }
button:disabled { background: #ccc; cursor: not-allowed; }
.error { color: #e74c3c; text-align: center; margin-top: 15px; font-size: 14px; }
.link { text-align: center; margin-top: 20px; color: #666; font-size: 14px; }
.link a { color: #667eea; cursor: pointer; text-decoration: underline; }
</style>