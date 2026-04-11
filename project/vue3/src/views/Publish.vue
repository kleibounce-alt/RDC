<template>
  <div class="publish-container">
    <div class="publish-box">
      <h2>发布商品</h2>
      <form @submit.prevent="handlePublish">
        <div class="input-group">
          <label>商品名称</label>
          <input
              v-model="form.goodName"
              type="text"
              placeholder="例如：iPhone 15 Pro"
              required
          >
        </div>

        <div class="input-group">
          <label>价格（元）</label>
          <input
              v-model="form.price"
              type="number"
              step="0.01"
              placeholder="例如：5999.00"
              required
          >
        </div>

        <div class="input-group">
          <label>商品图片 URL</label>
          <input
              v-model="form.image"
              type="text"
              placeholder="http://example.com/image.jpg"
          >
          <p class="hint">暂时只支持图片链接，文件上传后续添加</p>
        </div>

        <div class="input-group">
          <label>商品描述</label>
          <textarea
              v-model="form.description"
              rows="4"
              placeholder="描述一下商品的新旧程度、入手渠道、瑕疵等..."
          ></textarea>
        </div>

        <button type="submit" :disabled="loading">
          {{ loading ? '发布中...' : '立即发布' }}
        </button>
      </form>

      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
      <p v-if="successMsg" class="success">{{ successMsg }}</p>

      <p class="link">
        <a @click="emit('go-home')">返回首页</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { publishGood } from '@/api/good'

const emit = defineEmits(['publish-success', 'go-home'])

const form = reactive({
  goodName: '',
  price: '',
  image: '',
  description: ''
})

const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

onMounted(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (!userInfo) {
    alert('请先登录！')
  }
})

const handlePublish = async () => {
  const userInfo = localStorage.getItem('userInfo')
  if (!userInfo) {
    errorMsg.value = '请先登录'
    return
  }

  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    const res = await publishGood({
      goodName: form.goodName,
      price: parseFloat(form.price),
      image: form.image || '',
      description: form.description || ''
    })

    if (res.code === 200) {
      successMsg.value = '发布成功！'
      form.goodName = ''
      form.price = ''
      form.image = ''
      form.description = ''
      setTimeout(() => {
        emit('publish-success')
      }, 1000)
    } else {
      errorMsg.value = res.message || '发布失败'
    }
  } catch (error) {
    if (error.response?.status === 401) {
      errorMsg.value = '登录已过期，请重新登录'
    } else {
      errorMsg.value = error.response?.data?.message || '发布失败'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.publish-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 40px;
}
.publish-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  width: 500px;
}
h2 { text-align: center; color: #333; margin-bottom: 30px; }
.input-group { margin-bottom: 20px; }
label { display: block; margin-bottom: 8px; color: #555; font-size: 14px; }
input, textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}
input:focus, textarea:focus { outline: none; border-color: #667eea; }
textarea { resize: vertical; }
.hint { font-size: 12px; color: #999; margin-top: 5px; }
button {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 10px;
}
button:hover:not(:disabled) { background: #5568d3; }
button:disabled { background: #ccc; }
.error { color: #e74c3c; text-align: center; margin-top: 15px; }
.success { color: #52c41a; text-align: center; margin-top: 15px; }
.link { text-align: center; margin-top: 20px; }
.link a { color: #667eea; cursor: pointer; text-decoration: underline; }
</style>