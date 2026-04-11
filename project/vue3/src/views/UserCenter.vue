<template>
  <div class="user-center">
    <header class="header">
      <div class="header-left">
        <a @click="emit('go-home')" class="back-link">← 返回首页</a>
        <h1>个人中心</h1>
      </div>
      <div class="user-info">
        <span>欢迎, {{ userInfo?.userName }}</span>
        <button @click="logout">退出</button>
      </div>
    </header>

    <div class="container">
      <aside class="sidebar">
        <div class="menu-item" :class="{ active: currentTab === 'profile' }" @click="currentTab = 'profile'">
          基本信息
        </div>
        <div class="menu-item" :class="{ active: currentTab === 'goods' }" @click="currentTab = 'goods'">
          我发布的商品
        </div>
        <div class="menu-item" :class="{ active: currentTab === 'favorites' }" @click="currentTab = 'favorites'">
          我的收藏
        </div>
        <div class="menu-item" :class="{ active: currentTab === 'following' }" @click="currentTab = 'following'">
          我的关注
        </div>
        <div class="menu-item" :class="{ active: currentTab === 'wallet' }" @click="currentTab = 'wallet'">
          我的钱包
        </div>
      </aside>

      <main class="content">
        <!-- 基本信息 -->
        <div v-if="currentTab === 'profile'" class="tab-content">
          <h2>基本信息</h2>
          <div class="profile-card">
            <div class="info-row">
              <label>用户名：</label>
              <span>{{ userInfo?.userName }}</span>
            </div>
            <div class="info-row">
              <label>角色：</label>
              <span>{{ userInfo?.role === 1 ? '管理员' : '普通用户' }}</span>
            </div>
            <div class="info-row">
              <label>用户ID：</label>
              <span>{{ userInfo?.userId }}</span>
            </div>
          </div>
        </div>

        <!-- 我发布的商品 -->
        <div v-if="currentTab === 'goods'" class="tab-content">
          <h2>我发布的商品</h2>
          <div v-if="myGoods.length === 0" class="empty">还没有发布商品</div>
          <div v-else class="goods-list">
            <div v-for="item in myGoods" :key="item.good?.id || item.good?.Id" class="goods-item">
              <div class="item-image">
                <img v-if="item.good?.goodImage" :src="item.good.goodImage">
                <div v-else class="no-image">暂无图片</div>
              </div>
              <div class="item-info">
                <h4>{{ item.good?.goodName }}</h4>
                <p class="price">¥{{ formatPrice(item.good?.goodPrice) }}</p>
                <p class="status" :class="item.good?.sellingStatus">{{ formatStatus(item.good?.sellingStatus) }}</p>
              </div>
              <div class="item-actions">
                <button class="edit-btn" @click="editGood(item.good)">编辑</button>
                <button class="del-btn" @click="deleteMyGood(item.good?.id)">删除</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 我的收藏 -->
        <div v-if="currentTab === 'favorites'" class="tab-content">
          <h2>我的收藏</h2>
          <div v-if="favorites.length === 0" class="empty">暂无收藏</div>
          <div v-else class="goods-grid">
            <div v-for="item in favorites" :key="item.favoriteId || item.good?.id" class="goods-card" @click="viewGood(item.good?.id)">
              <div class="card-image">
                <img v-if="item.good?.goodImage" :src="item.good.goodImage">
                <div v-else class="no-image">暂无图片</div>
              </div>
              <div class="card-info">
                <h4>{{ item.good?.goodName }}</h4>
                <p class="price">¥{{ formatPrice(item.good?.goodPrice) }}</p>
                <button class="unfav-btn" @click.stop="handleCancelFav(item.good?.id)">取消收藏</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 我的关注 -->
        <div v-if="currentTab === 'following'" class="tab-content">
          <h2>我的关注</h2>
          <div v-if="following.length === 0" class="empty">暂无关注</div>
          <div v-else class="user-list">
            <div v-for="item in following" :key="item.followingUser?.id" class="user-item">
              <div class="avatar">{{ item.followingUser?.userName?.[0] }}</div>
              <div class="user-name">{{ item.followingUser?.userName }}</div>
              <button class="unfollow-btn" @click="handleCancelFollow(item.followingUser?.id)">取消关注</button>
            </div>
          </div>
        </div>

        <!-- 我的钱包 -->
        <div v-if="currentTab === 'wallet'" class="tab-content">
          <h2>我的钱包</h2>
          <div class="wallet-card">
            <div class="balance">
              <span class="label">当前余额</span>
              <span class="amount">¥{{ formatPrice(walletBalance) }}</span>
            </div>
            <div class="recharge">
              <input v-model="rechargeAmount" type="number" placeholder="输入充值金额">
              <button @click="handleRecharge">充值</button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWalletBalance, rechargeWallet } from '@/api/wallet'
import { cancelFavorite } from '@/api/favorite'
import { cancelFollow } from '@/api/follow'  // 导入的API函数
import { deleteGood } from '@/api/good'

const emit = defineEmits(['go-home', 'logout', 'view-good', 'edit-good'])
const currentTab = ref('profile')
const userInfo = ref(null)
const myGoods = ref([])
const favorites = ref([])
const following = ref([])
const walletBalance = ref(0)
const rechargeAmount = ref('')

onMounted(() => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
    loadAllData()
  }
})

const loadAllData = async () => {
  await Promise.all([
    loadMyGoods(),
    loadFavorites(),
    loadFollowing(),
    loadWallet()
  ])
}

const loadMyGoods = async () => {
  try {
    const res = await fetch('/api/user/good', { credentials: 'include' }).then(r => r.json())
    if (res.code === 200 && res.data) {
      myGoods.value = res.data.goods || []
    }
  } catch (e) {
    console.error('加载商品失败:', e)
  }
}

const loadFavorites = async () => {
  try {
    const res = await fetch('/api/user/favorite', { credentials: 'include' }).then(r => r.json())
    if (res.code === 200 && res.data) {
      favorites.value = res.data.favorites || []
    }
  } catch (e) {
    console.error('加载收藏失败:', e)
  }
}

const loadFollowing = async () => {
  try {
    const res = await fetch('/api/user/follow', { credentials: 'include' }).then(r => r.json())
    if (res.code === 200 && res.data) {
      following.value = res.data.followings || []
    }
  } catch (e) {
    console.error('加载关注失败:', e)
  }
}

const loadWallet = async () => {
  const res = await getWalletBalance()
  if (res.code === 200) {
    walletBalance.value = res.data?.balance || 0
  }
}

const formatPrice = (price) => {
  return price ? parseFloat(price).toFixed(2) : '0.00'
}

const formatStatus = (status) => {
  const map = { '未出售': '出售中', '已出售': '已售出' }
  return map[status] || status
}

const editGood = (good) => {
  emit('edit-good', good)
}

const deleteMyGood = async (goodId) => {
  if (!confirm('确定删除该商品吗？')) return
  try {
    const res = await deleteGood(goodId)
    if (res.code === 200) {
      alert('删除成功')
      loadMyGoods()
    }
  } catch (e) {
    alert('删除失败')
  }
}

const viewGood = (id) => {
  emit('view-good', id)
}

// 改名：handleCancelFav，避免冲突
const handleCancelFav = async (goodId) => {
  if (!confirm('确定取消收藏？')) return
  try {
    const res = await cancelFavorite(goodId)
    if (res.code === 200) {
      alert('已取消收藏')
      loadFavorites()
    }
  } catch (e) {
    alert('操作失败')
  }
}

// 改名：handleCancelFollow，避免与导入的 cancelFollow 冲突
const handleCancelFollow = async (userId) => {
  if (!confirm('确定取消关注？')) return
  try {
    const res = await cancelFollow(userId)  // 调用导入的API函数
    if (res.code === 200) {
      alert('已取消关注')
      loadFollowing()
    }
  } catch (e) {
    alert('操作失败')
  }
}

const handleRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    alert('请输入有效金额')
    return
  }
  try {
    const res = await rechargeWallet(parseFloat(rechargeAmount.value))
    if (res.code === 200) {
      alert('充值成功')
      rechargeAmount.value = ''
      loadWallet()
    }
  } catch (e) {
    alert('充值失败')
  }
}

const logout = () => {
  localStorage.removeItem('userInfo')
  emit('logout')
}
</script>

<style scoped>
.user-center { min-height: 100vh; background: #f5f5f5; }
.header {
  background: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.header-left { display: flex; align-items: center; gap: 20px; }
.back-link { color: #667eea; cursor: pointer; font-size: 14px; }
.header h1 { font-size: 20px; color: #333; margin: 0; }
.user-info { display: flex; align-items: center; gap: 15px; }
.container {
  max-width: 1200px;
  margin: 20px auto;
  display: flex;
  gap: 20px;
  padding: 0 20px;
}
.sidebar {
  width: 200px;
  background: white;
  border-radius: 8px;
  padding: 10px 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.menu-item {
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}
.menu-item:hover { background: #f5f5f5; }
.menu-item.active {
  background: #f0f0ff;
  color: #667eea;
  border-left-color: #667eea;
}
.content {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  min-height: 500px;
}
.tab-content h2 { margin-bottom: 20px; color: #333; font-size: 20px; }
.empty { text-align: center; color: #999; padding: 40px; }
.profile-card { padding: 20px; background: #f9f9f9; border-radius: 8px; }
.info-row { margin-bottom: 15px; display: flex; }
.info-row label { width: 100px; color: #666; }
.info-row span { color: #333; font-weight: 500; }

.goods-list { display: flex; flex-direction: column; gap: 15px; }
.goods-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  gap: 15px;
}
.item-image {
  width: 80px;
  height: 80px;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.item-image img { width: 100%; height: 100%; object-fit: cover; }
.no-image { color: #999; font-size: 12px; }
.item-info { flex: 1; }
.item-info h4 { margin-bottom: 8px; color: #333; }
.price { color: #ff5000; font-weight: bold; font-size: 16px; margin-bottom: 4px; }
.status { font-size: 12px; padding: 2px 8px; border-radius: 4px; display: inline-block; }
.status.未出售 { background: #e6f7e6; color: #52c41a; }
.status.已出售 { background: #f5f5f5; color: #999; }
.item-actions { display: flex; gap: 10px; }
.edit-btn, .del-btn { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-size: 12px; }
.edit-btn { background: #667eea; color: white; }
.del-btn { background: #ff4d4f; color: white; }

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}
.goods-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}
.goods-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.card-image {
  height: 150px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-image img { width: 100%; height: 100%; object-fit: cover; }
.card-info { padding: 12px; }
.card-info h4 { font-size: 14px; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.unfav-btn {
  width: 100%;
  padding: 6px;
  background: white;
  border: 1px solid #ff4d4f;
  color: #ff4d4f;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-top: 8px;
}

.user-list { display: flex; flex-direction: column; gap: 10px; }
.user-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  gap: 15px;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.user-name { flex: 1; color: #333; }
.unfollow-btn {
  padding: 6px 12px;
  background: white;
  border: 1px solid #999;
  color: #666;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.wallet-card { padding: 30px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 8px; color: white; }
.balance { margin-bottom: 30px; }
.balance .label { display: block; font-size: 14px; margin-bottom: 10px; opacity: 0.9; }
.balance .amount { font-size: 36px; font-weight: bold; }
.recharge { display: flex; gap: 10px; }
.recharge input {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
}
.recharge button {
  padding: 10px 20px;
  background: white;
  color: #667eea;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

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