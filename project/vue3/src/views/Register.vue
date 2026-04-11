<template>
  <div class="register-container">
    <!-- 背景装饰 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="register-box glass">
      <div class="register-header">
        <div class="logo">📝</div>
        <h2 class="text-gradient">创建账号</h2>
        <p class="subtitle">加入闲鱼交易平台</p>
      </div>

      <form @submit.prevent="handleRegister" class="register-form">
        <div class="input-group">
          <div class="input-icon">👤</div>
          <input
              v-model="form.userName"
              type="text"
              placeholder="用户名（至少3位）"
              required
              minlength="3"
          >
        </div>

        <div class="input-group">
          <div class="input-icon">🔒</div>
          <input
              v-model="form.password"
              type="password"
              placeholder="密码（至少6位）"
              required
              minlength="6"
          >
        </div>

        <div class="input-group">
          <div class="input-icon">🔐</div>
          <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              required
          >
        </div>

        <!-- 角色选择玻璃拟态卡片 -->
        <div class="role-section">
          <label class="section-label">选择角色</label>
          <div class="role-options">
            <div
                class="role-card"
                :class="{ active: form.role === 0 }"
                @click="form.role = 0"
            >
              <div class="role-icon">🙋</div>
              <div class="role-info">
                <span class="role-name">普通用户</span>
                <span class="role-desc">买家/卖家</span>
              </div>
              <div class="check-mark" v-if="form.role === 0">✓</div>
            </div>

            <div
                class="role-card"
                :class="{ active: form.role === 1 }"
                @click="form.role = 1"
            >
              <div class="role-icon">👮</div>
              <div class="role-info">
                <span class="role-name">管理员</span>
                <span class="role-desc">平台管理</span>
              </div>
              <div class="check-mark" v-if="form.role === 1">✓</div>
            </div>
          </div>
        </div>

        <button
            type="submit"
            class="btn-primary"
            :disabled="loading"
        >
          <span v-if="!loading">立即注册</span>
          <span v-else class="spinner"></span>
        </button>
      </form>

      <div v-if="errorMsg" class="error-toast">
        <span>⚠️</span> {{ errorMsg }}
      </div>

      <div v-if="successMsg" class="success-toast">
        <span>✅</span> {{ successMsg }}
      </div>

      <div class="register-footer">
        <div class="divider">
          <span>已有账号？</span>
        </div>
        <button class="btn-secondary" @click="emit('switch-to-login')">
          去登录
        </button>
      </div>
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
  confirmPassword: '',
  role: 0
})

const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const handleRegister = async () => {
  // 表单验证
  if (form.password !== form.confirmPassword) {
    errorMsg.value = '两次密码不一致'
    return
  }

  if (form.userName.length < 3) {
    errorMsg.value = '用户名至少3位'
    return
  }

  if (form.password.length < 6) {
    errorMsg.value = '密码至少6位'
    return
  }

  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    const res = await register({
      userName: form.userName,
      password: form.password,
      confirmPassword: form.confirmPassword,
      role: form.role
    })

    if (res.code === 200 && res.data.success) {
      const roleText = form.role === 1 ? '管理员' : '普通用户'
      successMsg.value = `注册成功！身份：${roleText}`

      setTimeout(() => {
        alert(`注册成功！\n用户名：${form.userName}\n身份：${roleText}`)
        emit('register-success')
      }, 500)
    } else {
      errorMsg.value = res.message || '注册失败'
    }
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '注册失败，请检查网络'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
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
  right: -100px;
  border-radius: 50%;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  bottom: -50px;
  left: -50px;
  border-radius: 50%;
  animation-delay: -5s;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  animation-delay: -2s;
}

/* 注册框玻璃拟态 */
.register-box {
  width: 100%;
  max-width: 480px;
  padding: 40px;
  border-radius: 24px;
  animation: fadeIn 0.6s ease;
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  max-height: 90vh;
  overflow-y: auto;
}

.register-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo {
  font-size: 48px;
  margin-bottom: 12px;
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

.register-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  padding: 12px 16px 12px 48px;
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

/* 角色选择区域 */
.role-section {
  margin: 8px 0;
}

.section-label {
  display: block;
  margin-bottom: 12px;
  color: #374151;
  font-size: 14px;
  font-weight: 600;
  padding-left: 4px;
}

.role-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.role-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 16px;
  background: rgba(255, 255, 255, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  backdrop-filter: blur(10px);
}

.role-card:hover {
  background: rgba(255, 255, 255, 0.7);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.role-card.active {
  background: rgba(102, 126, 234, 0.15);
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.role-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.role-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.role-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.role-desc {
  font-size: 12px;
  color: #6b7280;
}

.check-mark {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  animation: scaleIn 0.3s ease;
}

@keyframes scaleIn {
  from { transform: scale(0); }
  to { transform: scale(1); }
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-toast {
  margin-top: 12px;
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

.success-toast {
  margin-top: 12px;
  padding: 12px 16px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 12px;
  color: #10b981;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: fadeIn 0.5s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.register-footer {
  margin-top: 24px;
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

@media (max-width: 480px) {
  .register-box {
    padding: 24px;
    margin: 10px;
  }

  .role-options {
    gap: 8px;
  }

  .role-card {
    padding: 16px 12px;
  }

  h2 {
    font-size: 24px;
  }
}

@media (max-width: 360px) {
  .role-options {
    grid-template-columns: 1fr;
  }
}
</style>