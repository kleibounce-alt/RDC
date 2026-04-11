<template>
  <div class="home">
    <!-- 玻璃态头部 -->
    <header class="header glass">
      <div class="header-content">
        <div class="brand">
          <span class="brand-icon">🐟</span>
          <h1>闲鱼交易平台</h1>
        </div>

        <div class="user-section" v-if="userInfo && userInfo.userId">
          <div class="user-card" @click="goUserCenter">
            <div class="avatar" v-if="userInfo.avatar">
              <img :src="userInfo.avatar" alt="avatar" @error="handleAvatarError">
            </div>
            <div class="avatar placeholder" v-else>
              {{ userInfo.userName ? userInfo.userName[0].toUpperCase() : '?' }}
            </div>
            <span class="username">{{ userInfo.userName || '用户' }}</span>
            <span v-if="isAdmin" class="admin-badge">管理员</span>
            <span class="arrow">→</span>
          </div>
          <button class="btn-icon" @click="logout" title="退出">
            🚪
          </button>
        </div>
      </div>
    </header>

    <div class="container">
      <!-- 搜索栏 -->
      <div class="search-section glass">
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索心仪的商品..."
              @keyup.enter="handleSearch"
          >
          <button class="btn-search" @click="handleSearch">
            搜索
          </button>
        </div>
        <button
            v-if="isSearchMode"
            class="btn-reset"
            @click="resetSearch"
        >
          返回推荐
        </button>
      </div>

      <!-- 标签筛选栏 - 新增 -->
      <div class="tags-section glass" v-if="!isSearchMode">
        <div class="tags-header">
          <span class="tags-icon">🏷️</span>
          <span class="tags-title">按标签筛选：</span>
          <button
              class="btn-clear-tag"
              v-if="selectedTag"
              @click="clearTagFilter"
          >
            清除筛选
          </button>
        </div>
        <div class="tags-list">
          <button
              v-for="tag in tagList"
              :key="tag.tagId"
              class="tag-btn"
              :class="{ 'active': selectedTag === tag.tagId }"
              @click="handleTagSelect(tag.tagId)"
          >
            {{ tag.tagName }}
          </button>
          <div v-if="tagLoading" class="tag-loading">加载中...</div>
        </div>
      </div>

      <!-- 标题栏 -->
      <div class="section-header">
        <h2>
          <span class="title-icon">{{ isSearchMode ? '🔍' : (selectedTag ? '🏷️' : '✨') }}</span>
          {{ isSearchMode ? '搜索结果' : (selectedTag ? currentTagName : '精选推荐') }}
        </h2>
        <button class="btn-publish glass" @click="emit('switch-to-publish')">
          <span>+</span>
          发布商品
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-grid">
        <div v-for="i in 8" :key="i" class="skeleton-card glass"></div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="goodsList.length === 0" class="empty-state glass">
        <div class="empty-icon">{{ isSearchMode ? '🔍' : '📦' }}</div>
        <h3>{{ isSearchMode ? '没有找到相关商品' : '暂无商品' }}</h3>
        <p>{{ isSearchMode ? '换个关键词试试' : '成为第一个发布者吧！' }}</p>
        <button v-if="!isSearchMode && !selectedTag" class="btn-primary" @click="emit('switch-to-publish')">
          立即发布
        </button>
        <button v-if="selectedTag" class="btn-primary" @click="clearTagFilter">
          查看全部商品
        </button>
      </div>

      <!-- 商品网格 -->
      <div v-else class="goods-grid">
        <div
            v-for="(item, index) in goodsList"
            :key="item.id"
            class="goods-card glass card-hover"
            :style="{ animationDelay: `${index * 0.05}s` }"
            @click="viewDetail(item.id)"
        >
          <div class="image-wrapper">
            <div v-if="!item.goodImage || !item.goodImage.trim()" class="no-image glass-dark">
              <span>📷</span>
              <p>暂无图片</p>
            </div>
            <img
                v-else
                :src="item.goodImage"
                :alt="item.goodName"
                loading="lazy"
                @error="hideImage($event)"
            >
            <div class="price-tag">
              ¥{{ formatPrice(item.goodPrice) }}
            </div>
            <div class="status-badge" :class="item.sellingStatus">
              {{ formatStatus(item.sellingStatus) }}
            </div>
          </div>

          <div class="goods-info">
            <h3 class="title">{{ item.goodName }}</h3>
            <p class="description">{{ item.description || '暂无描述' }}</p>

            <!-- 卖家信息 -->
            <div class="seller-row" v-if="item.sellerInfo || item.sellerId">
              <div class="mini-avatar">
                <img v-if="item.sellerInfo?.avatar" :src="item.sellerInfo.avatar" alt="avatar" @error="$event.target.style.display='none'">
                <span v-else>{{ (item.sellerInfo?.userName || '?')[0] }}</span>
              </div>
              <span class="seller-name">{{ item.sellerInfo?.userName || ('卖家' + item.sellerId) }}</span>
            </div>

            <div class="meta">
              <span class="time">刚刚</span>
              <span class="views">👀 0</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getRandomGoods, searchGoods, getGoodsByTag } from '@/api/good'
import { getAllTags } from '@/api/tag'

const emit = defineEmits(['switch-to-publish', 'logout', 'view-detail', 'go-user-center'])

const goodsList = ref([])
const loading = ref(true)
const userInfo = ref({})
const searchKeyword = ref('')
const isSearchMode = ref(false)

// 标签相关 - 新增
const tagList = ref([])
const selectedTag = ref(null)
const tagLoading = ref(false)

// 计算属性：管理员判断
const isAdmin = computed(() => {
  return userInfo.value && userInfo.value.role === 1
})

// 计算当前选中的标签名称
const currentTagName = computed(() => {
  const tag = tagList.value.find(t => t.tagId === selectedTag.value)
  return tag ? tag.tagName : '标签商品'
})

onMounted(() => {
  loadUserInfo()
  fetchGoods()
  fetchTags() // 加载标签列表
})

const loadUserInfo = () => {
  try {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      const parsed = JSON.parse(stored)
      userInfo.value = {
        userId: parsed.userId || parsed.id,
        userName: parsed.userName || parsed.username || '用户',
        avatar: parsed.avatar || parsed.userAvatar || '',
        role: parsed.role || 0
      }
      console.log('[Home] 用户信息加载成功:', userInfo.value)
    }
  } catch (error) {
    console.error('[Home] 解析用户信息失败:', error)
    userInfo.value = {}
  }
}

// 获取标签列表
const fetchTags = async () => {
  tagLoading.value = true
  try {
    const res = await getAllTags()
    if (res.code === 200) {
      tagList.value = res.data || []
    }
  } catch (error) {
    console.error('获取标签列表失败:', error)
  } finally {
    tagLoading.value = false
  }
}

// 处理标签选择
const handleTagSelect = async (tagId) => {
  if (selectedTag.value === tagId) return // 重复点击不刷新

  loading.value = true
  selectedTag.value = tagId
  isSearchMode.value = false
  searchKeyword.value = ''

  try {
    const res = await getGoodsByTag(tagId)
    if (res.code === 200) {
      goodsList.value = res.data || []
      // 处理卖家信息
      goodsList.value.forEach(item => {
        if (!item.sellerInfo && item.sellerId) {
          item.sellerInfo = {
            userName: '卖家' + item.sellerId,
            avatar: ''
          }
        }
      })
    } else {
      goodsList.value = []
    }
  } catch (error) {
    console.error('根据标签获取商品失败:', error)
    goodsList.value = []
  } finally {
    loading.value = false
  }
}

// 清除标签筛选
const clearTagFilter = () => {
  selectedTag.value = null
  fetchGoods()
}

const fetchGoods = async () => {
  loading.value = true
  isSearchMode.value = false
  searchKeyword.value = ''
  selectedTag.value = null

  try {
    const res = await getRandomGoods(8)
    if (res.code === 200) {
      goodsList.value = res.data || []
      // 处理卖家信息
      goodsList.value.forEach(item => {
        if (!item.sellerInfo && item.sellerId) {
          item.sellerInfo = {
            userName: '卖家' + item.sellerId,
            avatar: ''
          }
        }
      })
    }
  } catch (error) {
    console.error('获取商品失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    fetchGoods()
    return
  }

  loading.value = true
  isSearchMode.value = true
  selectedTag.value = null // 搜索时清除标签选择

  try {
    const res = await searchGoods(searchKeyword.value.trim())
    if (res.code === 200) {
      goodsList.value = res.data || []
    } else {
      goodsList.value = []
    }
  } catch (error) {
    console.error('搜索失败:', error)
    goodsList.value = []
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchKeyword.value = ''
  fetchGoods()
}

const formatPrice = (price) => {
  return price ? parseFloat(price).toFixed(2) : '0.00'
}

const formatStatus = (status) => {
  const map = {
    '未出售': '在售',
    '已出售': '已售',
    'offline': '下架'
  }
  return map[status] || status
}

const viewDetail = (id) => {
  emit('view-detail', id)
}

const hideImage = (e) => {
  const img = e.target
  const src = img.src
  const item = goodsList.value.find(g => g.goodImage === src)
  if (item) {
    item.goodImage = ''
  }
}

const handleAvatarError = () => {
  userInfo.value.avatar = null
}

const goUserCenter = () => {
  emit('go-user-center')
}

const logout = () => {
  localStorage.removeItem('userInfo')
  emit('logout')
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding-bottom: 40px;
}

/* 玻璃态头部 */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 16px 0;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  font-size: 32px;
}

.brand h1 {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.user-card:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.username {
  font-weight: 600;
  color: #1f2937;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-badge {
  padding: 2px 8px;
  background: #ef4444;
  color: white;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.arrow {
  color: #9ca3af;
  font-size: 18px;
}

.btn-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s;
}

.btn-icon:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: rotate(90deg);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 搜索栏 */
.search-section {
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 24px;
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 300px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.8);
  padding: 12px 20px;
  border-radius: 30px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s;
}

.search-box:focus-within {
  border-color: #667eea;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.search-icon {
  font-size: 20px;
  color: #9ca3af;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 16px;
  outline: none;
  color: #1f2937;
}

.btn-search {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-search:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.btn-reset {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: #667eea;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-reset:hover {
  background: rgba(102, 126, 234, 0.1);
}

/* 标签栏 - 新增 */
.tags-section {
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 24px;
}

.tags-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.tags-icon {
  font-size: 20px;
}

.tags-title {
  font-weight: 600;
  color: #1f2937;
  flex: 1;
}

.btn-clear-tag {
  padding: 4px 12px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-clear-tag:hover {
  background: #ef4444;
  color: white;
}

.tags-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.tag-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #4b5563;
  transition: all 0.3s;
  white-space: nowrap;
}

.tag-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
}

.tag-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.tag-loading {
  color: #9ca3af;
  font-size: 14px;
  padding: 8px;
}

/* 标题栏 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  color: #1f2937;
}

.title-icon {
  font-size: 24px;
}

.btn-publish {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 30px;
  color: #667eea;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-publish:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.2);
}

.btn-publish span {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

/* 加载状态 */
.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.skeleton-card {
  height: 360px;
  border-radius: 16px;
  background: linear-gradient(90deg, rgba(255,255,255,0.4) 25%, rgba(255,255,255,0.6) 50%, rgba(255,255,255,0.4) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 40px;
  border-radius: 24px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  color: #1f2937;
  margin-bottom: 8px;
}

.empty-state p {
  color: #6b7280;
  margin-bottom: 24px;
}

.btn-primary {
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 30px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

/* 商品网格 */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.goods-card {
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s;
  animation: fadeIn 0.5s ease-out forwards;
  opacity: 0;
}

@keyframes fadeIn {
  to {
    opacity: 1;
  }
}

.goods-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(31, 38, 135, 0.15);
}

.image-wrapper {
  position: relative;
  height: 200px;
  background: #f3f4f6;
  overflow: hidden;
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.goods-card:hover .image-wrapper img {
  transform: scale(1.05);
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 14px;
}

.no-image span {
  font-size: 48px;
  margin-bottom: 8px;
}

.price-tag {
  position: absolute;
  bottom: 12px;
  left: 12px;
  padding: 6px 14px;
  background: rgba(255, 80, 0, 0.9);
  color: white;
  border-radius: 20px;
  font-weight: 700;
  font-size: 16px;
  backdrop-filter: blur(4px);
}

.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(16, 185, 129, 0.9);
  color: white;
}

.status-badge.已出售, .status-badge.sold {
  background: rgba(156, 163, 175, 0.9);
}

.goods-info {
  padding: 20px;
}

.goods-info .title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.description {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  height: 42px;
}

.seller-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.mini-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.mini-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.seller-name {
  font-size: 13px;
  color: #6b7280;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #9ca3af;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }

  .brand h1 {
    font-size: 18px;
  }

  .container {
    padding: 0 16px;
  }

  .search-box {
    min-width: 100%;
  }

  .tags-list {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 8px;
    -webkit-overflow-scrolling: touch;
  }

  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}
</style>