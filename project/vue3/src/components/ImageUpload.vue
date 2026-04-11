<template>
  <div class="image-upload">
    <!-- 预览区域 -->
    <div v-if="previewUrl" class="preview-box">
      <img :src="previewUrl" alt="预览" @error="handleImageError" @load="handleImageLoad">
      <div v-if="imageError" class="image-error">图片加载失败</div>
      <div class="preview-actions">
        <button type="button" class="remove-btn" @click="removeImage">删除</button>
      </div>
    </div>

    <!-- 上传区域 -->
    <div v-else class="upload-box" @click="triggerUpload">
      <input
          ref="fileInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleFileChange"
      >
      <div class="upload-icon">+</div>
      <div class="upload-text">{{ loading ? '上传中...' : placeholder }}</div>
      <div v-if="loading" class="progress-bar">
        <div class="progress" :style="{ width: progress + '%' }"></div>
      </div>
    </div>

    <p v-if="error" class="error-text">{{ error }}</p>
    <p class="hint">支持 JPG、PNG，最大 5MB</p>
    <p v-if="previewUrl && !imageError" class="url-hint">URL: {{ previewUrl.substring(0, 50) }}...</p>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import request from '@/utils/request'

const props = defineProps({
  modelValue: String,
  placeholder: {
    type: String,
    default: '点击上传图片'
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'error'])

const fileInput = ref(null)
const loading = ref(false)
const progress = ref(0)
const error = ref('')
const previewUrl = ref(props.modelValue || '')
const imageError = ref(false)

watch(() => props.modelValue, (newVal) => {
  previewUrl.value = newVal || ''
  imageError.value = false
})

const triggerUpload = () => {
  if (loading.value) return
  fileInput.value?.click()
}

const handleImageError = () => {
  imageError.value = true
  error.value = '图片无法显示，请检查网络或联系管理员'
}

const handleImageLoad = () => {
  imageError.value = false
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    error.value = '请选择图片文件'
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    error.value = '图片大小不能超过 5MB'
    return
  }

  error.value = ''
  imageError.value = false
  loading.value = true
  progress.value = 0

  try {
    // 先显示本地预览，让用户知道选中了
    const localPreview = URL.createObjectURL(file)
    previewUrl.value = localPreview

    const formData = new FormData()
    formData.append('file', file)

    const progressTimer = setInterval(() => {
      if (progress.value < 90) progress.value += 10
    }, 200)

    const res = await request({
      url: '/upload/image',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    clearInterval(progressTimer)
    progress.value = 100

    if (res.code === 200 && res.data?.url) {
      // 释放本地预览URL，避免内存泄漏
      URL.revokeObjectURL(localPreview)

      previewUrl.value = res.data.url
      emit('update:modelValue', res.data.url)
      emit('success', res.data.url)
      setTimeout(() => {
        loading.value = false
        progress.value = 0
      }, 500)
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '上传失败'
    previewUrl.value = ''
    emit('update:modelValue', '')
    emit('error', error.value)
    loading.value = false
  } finally {
    e.target.value = ''
  }
}

const removeImage = () => {
  previewUrl.value = ''
  imageError.value = false
  error.value = ''
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload {
  width: 100%;
}

.preview-box {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid rgba(102, 126, 234, 0.3);
  background: #f5f5f5;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.preview-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ff4d4f;
  font-size: 14px;
  background: rgba(255,255,255,0.9);
  padding: 8px 16px;
  border-radius: 4px;
}

.preview-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  padding: 10px;
  text-align: center;
}

.remove-btn {
  padding: 6px 16px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.remove-btn:hover {
  background: #ff7875;
  transform: scale(1.05);
}

.upload-box {
  width: 200px;
  height: 200px;
  border: 2px dashed rgba(102, 126, 234, 0.4);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.6);
}

.upload-box:hover {
  border-color: #667eea;
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.2);
}

.upload-icon {
  font-size: 48px;
  color: #667eea;
  margin-bottom: 12px;
  font-weight: 300;
  transition: all 0.3s;
}

.upload-box:hover .upload-icon {
  transform: scale(1.1);
}

.upload-text {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: rgba(102, 126, 234, 0.1);
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.3s;
}

.error-text {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.hint {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}

.url-hint {
  color: #52c41a;
  font-size: 11px;
  margin-top: 4px;
  word-break: break-all;
  background: rgba(82, 196, 26, 0.1);
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
}
</style>