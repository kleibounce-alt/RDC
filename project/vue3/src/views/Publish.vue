<template>
  <div class="publish-container">
    <div class="publish-box glass">
      <!-- 头部 -->
      <div class="header-bar">
        <button class="back-btn" @click="goBack">
          <span>←</span> 返回
        </button>
        <h2>
          <span class="title-icon">📦</span>
          发布闲置
        </h2>
        <div class="placeholder"></div>
      </div>

      <div v-if="loading" class="loading-wrapper">
        <div class="spinner"></div>
        <p>发布中...</p>
      </div>

      <form v-else class="publish-form" @submit.prevent="handleSubmit">
        <!-- 图片上传 -->
        <div class="form-section">
          <label class="section-label">
            <span class="required">*</span>
            商品图片
          </label>
          <ImageUpload v-model="form.image" placeholder="点击上传商品图片" />
        </div>

        <!-- 基本信息 -->
        <div class="form-section">
          <label class="section-label">
            <span class="required">*</span>
            商品名称
          </label>
          <input
              v-model="form.goodName"
              type="text"
              placeholder="给宝贝起个名字，比如：九成新iPhone 13"
              maxlength="50"
              required
          >
          <span class="char-count">{{ form.goodName.length }}/50</span>
        </div>

        <div class="form-row">
          <div class="form-section half">
            <label class="section-label">
              <span class="required">*</span>
              价格 (¥)
            </label>
            <input
                v-model.number="form.price"
                type="number"
                step="0.01"
                min="0.01"
                placeholder="0.00"
                required
            >
          </div>
        </div>

        <!-- ★★★ 标签选择区域（支持用户添加新标签）★★★ -->
        <div class="form-section">
          <label class="section-label">
            <span class="required">*</span>
            商品标签
            <span class="tag-hint">(至少选择1个，最多3个；无合适标签可自己添加)</span>
          </label>

          <!-- 标签加载中 -->
          <div v-if="tagLoading" class="tag-loading">
            <span class="small-spinner"></span> 加载标签中...
          </div>

          <!-- 已有标签列表 -->
          <div v-else class="tags-container">
            <div class="existing-tags">
              <div class="tags-header-row">
                <span class="sub-label">选择已有标签：</span>
                <button
                    type="button"
                    class="btn-add-new"
                    @click="showAddTagInput = true"
                    v-if="!showAddTagInput"
                >
                  <span>+</span> 添加新标签
                </button>
              </div>

              <div v-if="tagList.length > 0" class="tag-select-area">
                <div
                    v-for="tag in tagList"
                    :key="tag.tagId"
                    class="tag-option"
                    :class="{
                      'selected': selectedTags.includes(tag.tagId),
                      'disabled': selectedTags.length >= 3 && !selectedTags.includes(tag.tagId)
                    }"
                    @click="toggleTag(tag.tagId)"
                >
                  <span class="tag-icon">🏷️</span>
                  {{ tag.tagName }}
                  <span v-if="selectedTags.includes(tag.tagId)" class="check-icon">✓</span>
                </div>
              </div>

              <div v-else class="no-tags-hint">
                <p>暂无已有标签，快来创建第一个吧！</p>
              </div>
            </div>

            <!-- 添加新标签输入区域 -->
            <div v-if="showAddTagInput" class="add-tag-section glass-dark">
              <div class="add-tag-header">
                <span>🏷️ 创建新标签</span>
                <button type="button" class="btn-close" @click="showAddTagInput = false">×</button>
              </div>
              <div class="add-tag-row">
                <input
                    v-model="newTagName"
                    type="text"
                    placeholder="输入标签名称，如：数码、书籍、美妆..."
                    maxlength="20"
                    @keyup.enter="handleCreateTag"
                >
                <button
                    type="button"
                    class="btn-create-tag"
                    @click="handleCreateTag"
                    :disabled="!newTagName.trim() || creatingTag"
                >
                  <span v-if="creatingTag" class="mini-spinner"></span>
                  <span v-else>创建</span>
                </button>
              </div>
              <p class="add-tag-hint">按回车键快速创建，创建后自动选中</p>
              <p v-if="createTagError" class="error-text">{{ createTagError }}</p>
            </div>

            <!-- 已选标签展示 -->
            <div v-if="selectedTags.length > 0" class="selected-tags-preview">
              <span class="preview-label">已选择 {{ selectedTags.length }}/3：</span>
              <span
                  v-for="tagId in selectedTags"
                  :key="tagId"
                  class="selected-tag"
                  :class="{ 'is-new': isNewTag(tagId) }"
              >
                {{ getTagName(tagId) }}
                <button type="button" class="remove-tag" @click="toggleTag(tagId)">×</button>
              </span>
            </div>
          </div>

          <p v-if="tagError" class="error-text">{{ tagError }}</p>
        </div>

        <!-- 商品描述 -->
        <div class="form-section">
          <label class="section-label">商品描述</label>
          <textarea
              v-model="form.description"
              rows="4"
              placeholder="描述一下宝贝的品牌、规格、新旧程度、入手渠道等..."
              maxlength="500"
          ></textarea>
          <span class="char-count">{{ form.description.length }}/500</span>
        </div>

        <!-- 发布按钮 -->
        <div class="form-actions">
          <button type="button" class="btn-secondary" @click="goBack">
            取消
          </button>
          <button
              type="submit"
              class="btn-primary"
              :disabled="submitting || selectedTags.length === 0"
          >
            <span v-if="submitting" class="btn-spinner"></span>
            <span v-else>立即发布</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { publishGood } from '@/api/good'
import { getAllTags, createTag } from '@/api/tag'
import ImageUpload from '@/components/ImageUpload.vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['publish-success', 'go-home'])

const form = reactive({
  goodName: '',
  image: '',
  price: null,
  description: ''
})

const selectedTags = ref([])    // 选中的标签ID数组
const tagList = ref([])         // 所有已有标签列表
const newTagIds = ref([])       // 记录本次新创建的标签ID，用于样式区分
const tagLoading = ref(false)
const tagError = ref('')
const submitting = ref(false)
const loading = ref(false)

// 添加新标签相关
const showAddTagInput = ref(false)
const newTagName = ref('')
const creatingTag = ref(false)
const createTagError = ref('')

// 获取标签列表
const fetchTags = async () => {
  tagLoading.value = true
  try {
    const res = await getAllTags()
    if (res.code === 200) {
      tagList.value = res.data || []
    } else {
      tagError.value = '获取标签列表失败'
    }
  } catch (error) {
    console.error('获取标签失败:', error)
    tagError.value = '获取标签列表失败，请刷新重试'
  } finally {
    tagLoading.value = false
  }
}

onMounted(() => {
  fetchTags()
})

// 切换标签选择
const toggleTag = (tagId) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    // 已选中，则取消
    selectedTags.value.splice(index, 1)
    tagError.value = ''
  } else {
    // 未选中，则添加（最多3个）
    if (selectedTags.value.length >= 3) {
      tagError.value = '最多只能选择3个标签'
      return
    }
    selectedTags.value.push(tagId)
    tagError.value = ''
  }
}

// 获取标签名称
const getTagName = (tagId) => {
  const tag = tagList.value.find(t => t.tagId === tagId)
  return tag ? tag.tagName : ''
}

// 判断是否是新创建的标签
const isNewTag = (tagId) => {
  return newTagIds.value.includes(tagId)
}

// 创建新标签
const handleCreateTag = async () => {
  const name = newTagName.value.trim()
  if (!name) {
    createTagError.value = '请输入标签名称'
    return
  }

  if (name.length > 20) {
    createTagError.value = '标签名称不能超过20个字符'
    return
  }

  creatingTag.value = true
  createTagError.value = ''

  try {
    const res = await createTag(name)
    if (res.code === 200) {
      const newTag = res.data
      // 添加到标签列表
      tagList.value.push(newTag)
      // 自动选中新标签
      selectedTags.value.push(newTag.tagId)
      // 记录为新创建的标签（用于样式标记）
      newTagIds.value.push(newTag.tagId)

      // 清空输入框并关闭
      newTagName.value = ''
      showAddTagInput.value = false
      tagError.value = ''

      ElMessage.success('标签创建成功并已选中')
    } else {
      createTagError.value = res.message || '创建失败'
    }
  } catch (error) {
    console.error('创建标签失败:', error)
    createTagError.value = error.response?.data?.message || '创建标签失败'
  } finally {
    creatingTag.value = false
  }
}

// 校验表单
const validateForm = () => {
  if (!form.goodName.trim()) {
    ElMessage.warning('请输入商品名称')
    return false
  }
  if (!form.price || form.price <= 0) {
    ElMessage.warning('请输入有效的价格')
    return false
  }
  if (selectedTags.value.length === 0) {
    ElMessage.warning('请至少选择一个商品标签')
    tagError.value = '请至少选择一个标签'
    return false
  }
  if (selectedTags.value.length > 3) {
    ElMessage.warning('最多只能选择3个标签')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true
  try {
    const res = await publishGood({
      ...form,
      tagIds: selectedTags.value  // 传入选中的标签（包括新创建的）
    })

    if (res.code === 200) {
      ElMessage.success('发布成功！')
      emit('publish-success')
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error(error.response?.data?.message || '发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  emit('go-home')
}
</script>

<style scoped>
.publish-container {
  min-height: 100vh;
  padding: 24px;
  background: var(--gradient-bg, linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%));
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.publish-box {
  width: 100%;
  max-width: 640px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  overflow: hidden;
  margin-top: 20px;
}

.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.4);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  color: #4b5563;
  transition: all 0.3s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateX(-4px);
}

.header-bar h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.title-icon {
  font-size: 24px;
}

.placeholder {
  width: 80px;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px;
  color: #6b7280;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.publish-form {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row .half {
  flex: 1;
}

.section-label {
  font-weight: 600;
  color: #374151;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.required {
  color: #ef4444;
  font-weight: 700;
}

.tag-hint {
  font-weight: normal;
  color: #9ca3af;
  font-size: 13px;
  margin-left: 8px;
}

input, textarea {
  padding: 14px 18px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  font-size: 15px;
  transition: all 0.3s;
  color: #1f2937;
}

input:focus, textarea:focus {
  outline: none;
  border-color: #667eea;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

textarea {
  resize: vertical;
  min-height: 100px;
}

.char-count {
  text-align: right;
  font-size: 13px;
  color: #9ca3af;
}

/* 标签容器 */
.tags-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tag-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px;
  color: #6b7280;
}

.small-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.tags-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.sub-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.btn-add-new {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border: 1px dashed #667eea;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-add-new:hover {
  background: #667eea;
  color: white;
  border-style: solid;
}

.tag-select-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 4px;
}

.tag-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 2px solid rgba(102, 126, 234, 0.15);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #4b5563;
  user-select: none;
}

.tag-option:hover:not(.disabled) {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.tag-option.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.tag-option.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background: rgba(156, 163, 175, 0.2);
}

.tag-icon {
  font-size: 16px;
}

.check-icon {
  font-size: 14px;
  font-weight: 700;
  margin-left: 4px;
}

.no-tags-hint {
  padding: 20px;
  text-align: center;
  color: #9ca3af;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  border: 1px dashed rgba(156, 163, 175, 0.3);
}

/* 添加新标签区域 */
.add-tag-section {
  padding: 16px;
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.3);
  background: rgba(255, 255, 255, 0.6);
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.add-tag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
  color: #1f2937;
}

.btn-close {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(156, 163, 175, 0.2);
  border: none;
  color: #6b7280;
  cursor: pointer;
  font-size: 20px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.btn-close:hover {
  background: #ef4444;
  color: white;
}

.add-tag-row {
  display: flex;
  gap: 10px;
}

.add-tag-row input {
  flex: 1;
}

.btn-create-tag {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.btn-create-tag:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-create-tag:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.mini-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

.add-tag-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 6px;
}

/* 已选标签预览 */
.selected-tags-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
}

.preview-label {
  font-size: 14px;
  color: #6b7280;
}

.selected-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  background: rgba(102, 126, 234, 0.15);
  color: #667eea;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  animation: popIn 0.3s ease;
}

.selected-tag.is-new {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px dashed #10b981;
}

@keyframes popIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.remove-tag {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.2);
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  padding: 0;
  transition: all 0.2s;
}

.selected-tag.is-new .remove-tag {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.remove-tag:hover {
  background: #ef4444;
  color: white;
}

.error-text {
  color: #ef4444;
  font-size: 14px;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.5);
}

.btn-secondary {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: #667eea;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: rgba(102, 126, 234, 0.1);
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

@media (max-width: 640px) {
  .publish-container {
    padding: 0;
  }

  .publish-box {
    border-radius: 0;
    min-height: 100vh;
    margin-top: 0;
  }

  .header-bar {
    padding: 16px;
  }

  .header-bar h2 {
    font-size: 18px;
  }

  .publish-form {
    padding: 20px;
  }

  .form-row {
    flex-direction: column;
  }

  .add-tag-row {
    flex-direction: column;
  }

  .btn-create-tag {
    width: 100%;
    justify-content: center;
  }
}
</style>