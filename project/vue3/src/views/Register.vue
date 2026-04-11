<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册账号</h2>
      <form @submit.prevent="handleRegister">
        <div class="input-group">
          <input
              v-model="form.userName"
              type="text"
              placeholder="用户名"
              required
          >
        </div>
        <div class="input-group">
          <input
              v-model="form.password"
              type="password"
              placeholder="密码"
              required
          >
        </div>
        <div class="input-group">
          <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              required
          >
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
      <p class="link">
        已有账号？<a @click="emit('switch-to-login')">去登录</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { register } from '@/api/user'

const emit = defineEmits(['register-success', 'switch-to-login'])

const form = reactive({
  userName: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  if (form.password !== form.confirmPassword) {
    errorMsg.value = '两次密码不一致'
    return
  }

  loading.value = true
  errorMsg.value = ''

  try {
    const res = await register(form)
    if (res.code === 200 && res.data.success) {
      alert('注册成功！请登录')
      emit('register-success')
    } else {
      errorMsg.value = res.message || '注册失败'
    }
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-box {
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