<template>
  <div class="home">
    <header class="header">
      <h1>闲鱼交易平台</h1>
      <div class="user-info" v-if="userInfo">
        <span @click="goUserCenter" style="cursor: pointer; color: #667eea;">欢迎, {{ userInfo.userName }}</span>
        <button @click="logout">退出</button>
      </div>
    </header>

    <div class="container">
      <div class="search-bar">
        <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索商品名称..."
            class="search-input"
            @keyup.enter="handleSearch"
        >
        <button class="search-btn" @click="handleSearch">搜索</button>
        <button
            v-if="isSearchMode"
            class="reset-btn"
            @click="resetSearch"
        >
          返回推荐
        </button>
      </div>

      <div class="header-row">
        <h2>{{ isSearchMode ? '搜索结果' : '推荐商品' }}</h2>
        <button class="publish-btn" @click="emit('switch-to-publish')">+ 发布商品</button>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="goodsList.length === 0" class="empty">
        {{ isSearchMode ? '没有找到相关商品' : '暂无商品，快去发布一个吧！' }}
      </div>

      <div v-else class="goods-grid">
        <div
            v-for="item in goodsList"
            :key="item.id"
            class="goods-card"
            @click="viewDetail(item.id)"
        >
          <div class="goods-image">
            <img
                v-if="item.goodImage && item.goodImage.trim()"
                :src="item.goodImage"
                :alt="item.goodName"
                @error="hideImage($event)"
            >
            <div v-show="!item.goodImage || !item.goodImage.trim()" class="no-image-text">
              暂无图片
            </div>
          </div>

          <div class="goods-info">
            <h3 class="title">{{ item.goodName }}</h3>
            <p class="price">¥{{ formatPrice(item.goodPrice) }}</p>
            <p class="desc">{{ item.description || '暂无描述' }}</p>
            <p class="status" :class="item.sellingStatus">
              {{ formatStatus(item.sellingStatus) }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRandomGoods, searchGoods } from '@/api/good'

const emit = defineEmits(['switch-to-publish', 'logout', 'view-detail', 'go-user-center'])

const goodsList = ref([])
const loading = ref(true)
const userInfo = ref(null)
const searchKeyword = ref('')
const isSearchMode = ref(false)

onMounted(() => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
  fetchGoods()
})

const fetchGoods = async () => {
  loading.value = true
  isSearchMode.value = false
  searchKeyword.value = ''
  try {
    const res = await getRandomGoods(8)
    if (res.code === 200) {
      goodsList.value = res.data || []
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
    '未出售': '出售中',
    '已出售': '已售出',
    'offline': '已下架'
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

const goUserCenter = () => {
  emit('go-user-center')
}

const logout = () => {
  localStorage.removeItem('userInfo')
  emit('logout')
}
</script>

<style scoped>
.home { min-height: 100vh; background: #f5f5f5; }
.header {
  background: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.header h1 { font-size: 20px; color: #333; }
.user-info { display: flex; align-items: center; gap: 15px; }
.container { max-width: 1200px; margin: 20px auto; padding: 0 20px; }
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.search-input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}
.search-input:focus { outline: none; border-color: #667eea; }
.search-btn {
  padding: 10px 25px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.search-btn:hover { background: #5568d3; }
.reset-btn {
  padding: 10px 20px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.reset-btn:hover { background: #e0e0e0; }
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.publish-btn {
  padding: 10px 20px;
  background: #52c41a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.publish-btn:hover { background: #45a616; }
.loading, .empty { text-align: center; padding: 40px; color: #999; font-size: 16px; }
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}
.goods-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s;
  cursor: pointer;
}
.goods-card:hover { transform: translateY(-4px); box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.goods-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #e0e0e0 0%, #f0f0f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.goods-image img { width: 100%; height: 100%; object-fit: cover; }
.no-image-text { color: #999; font-size: 14px; text-align: center; }
.goods-info { padding: 15px; }
.title {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.price { font-size: 20px; color: #ff5000; font-weight: bold; margin-bottom: 8px; }
.desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.status { font-size: 12px; padding: 4px 8px; border-radius: 4px; display: inline-block; }
.status.未出售 { background: #e6f7e6; color: #52c41a; }
.status.已出售 { background: #f5f5f5; color: #999; }
button {
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
button:hover { background: #5568d3; }
</style>