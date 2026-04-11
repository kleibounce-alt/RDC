<template>
  <div class="user-center">
    <!-- 玻璃态头部 -->
    <header class="header glass">
      <div class="header-content">
        <div class="brand" @click="goHome">
          <span class="brand-icon emoji-small">🐠</span>
          <h1>个人中心</h1>
        </div>
        <div class="back-btn" @click="goHome">
          <span class="emoji-small">🏠</span> 返回首页
        </div>
      </div>
    </header>

    <div class="container">
      <!-- 用户信息卡片 -->
      <div class="user-card glass">
        <div class="user-main">
          <div class="avatar-section">
            <div class="avatar-large" @click="triggerAvatarUpload">
              <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="avatar">
              <span v-else class="avatar-text">{{ userNameFirstChar }}</span>
              <div class="avatar-mask">
                <span class="emoji-small">📷</span>
              </div>
            </div>
            <input
                ref="avatarInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleAvatarChange"
            >
          </div>

          <div class="user-info">
            <h2 class="user-name">
              {{ userInfo.userName || '用户' }}
              <span v-if="isAdmin" class="role-tag admin">管理员</span>
              <span v-else class="role-tag">普通用户</span>
            </h2>
            <p class="user-id">ID: {{ userInfo.userId || '-' }}</p>

            <!-- 钱包余额显示 -->
            <div class="wallet-section glass-dark">
              <div class="wallet-header">
                <span class="emoji-small">💰</span>
                <span class="wallet-title">我的钱包</span>
              </div>
              <div class="wallet-balance">
                <span class="currency">¥</span>
                <span class="amount">{{ formatBalance(walletBalance) }}</span>
              </div>
              <div class="wallet-actions">
                <button class="btn-recharge" @click="showRechargeDialog">
                  <span class="emoji-small">💎</span> 充值
                </button>
                <button class="btn-refresh" @click="refreshWallet" :disabled="walletLoading">
                  <span v-if="!walletLoading" class="emoji-small">🔄</span>
                  <span v-else class="spinner-small"></span>
                  刷新
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="stats-bar">
          <div class="stat-item" @click="activeTab = 'goods'">
            <span class="stat-num">{{ myGoods.length }}</span>
            <span class="stat-label">我的发布</span>
          </div>
          <div class="stat-item" @click="activeTab = 'favorites'">
            <span class="stat-num">{{ favorites.length }}</span>
            <span class="stat-label">我的收藏</span>
          </div>
          <div class="stat-item" @click="activeTab = 'following'">
            <span class="stat-num">{{ followings.length }}</span>
            <span class="stat-label">我的关注</span>
          </div>
          <div class="stat-item" @click="activeTab = 'orders'">
            <span class="stat-num">{{ orders.length }}</span>
            <span class="stat-label">我的订单</span>
          </div>
        </div>

        <!-- 退出登录按钮 - 修复图标过大问题 -->
        <div class="logout-section">
          <button class="btn-logout" @click="handleLogout">
            <span class="logout-icon">🚪</span>
            <span class="logout-text">退出登录</span>
          </button>
        </div>
      </div>

      <!-- 标签导航 -->
      <div class="tabs-nav glass">
        <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          {{ tab.label }}
        </button>
      </div>

      <!-- 内容区域 -->
      <div class="content-area glass">
        <!-- 我的发布 -->
        <div v-if="activeTab === 'goods'" class="tab-content">
          <div class="section-header">
            <h3>我的发布</h3>
            <button class="btn-publish" @click="goPublish">
              <span class="emoji-small">➕</span> 发布商品
            </button>
          </div>
          <div v-if="myGoods.length === 0" class="empty-state">
            <div class="empty-icon emoji-medium">📦</div>
            <p>还没有发布任何商品</p>
            <button class="btn-action" @click="goPublish">去发布</button>
          </div>
          <div v-else class="goods-grid">
            <div v-for="item in myGoods" :key="item.good?.id" class="good-card" @click="viewGood(item.good?.id)">
              <div class="good-image">
                <img v-if="item.good?.goodImage" :src="item.good.goodImage" alt="商品">
                <div v-else class="no-image">暂无图片</div>
              </div>
              <div class="good-info">
                <h4 class="good-title">{{ item.good?.goodName || '未知商品' }}</h4>
                <p class="good-price">¥{{ formatPrice(item.good?.goodPrice) }}</p>
                <span class="status-badge" :class="item.good?.sellingStatus">
                  {{ formatStatus(item.good?.sellingStatus) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeTab === 'favorites'" class="tab-content">
          <div class="section-header">
            <h3>我的收藏</h3>
          </div>
          <div v-if="favorites.length === 0" class="empty-state">
            <div class="empty-icon emoji-medium">⭐</div>
            <p>还没有收藏任何商品</p>
            <button class="btn-action" @click="goHome">去逛逛</button>
          </div>
          <div v-else class="goods-grid">
            <div v-for="item in favorites" :key="item.good?.id" class="good-card" @click="viewGood(item.good?.id)">
              <div class="good-image">
                <img v-if="item.good?.goodImage" :src="item.good.goodImage" alt="商品">
                <div v-else class="no-image">暂无图片</div>
              </div>
              <div class="good-info">
                <h4 class="good-title">{{ item.good?.goodName || '未知商品' }}</h4>
                <p class="good-price">¥{{ formatPrice(item.good?.goodPrice) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 我的关注 -->
        <div v-if="activeTab === 'following'" class="tab-content">
          <div class="section-header">
            <h3>我的关注</h3>
          </div>
          <div v-if="followings.length === 0" class="empty-state">
            <div class="empty-icon emoji-medium">👥</div>
            <p>还没有关注任何用户</p>
          </div>
          <div v-else class="user-list">
            <div v-for="item in followings" :key="item.followingUser?.id" class="user-item">
              <div class="user-avatar-small">
                <img v-if="item.followingUser?.avatar" :src="item.followingUser.avatar">
                <span v-else>{{ item.followingUser?.userName?.[0] || '?' }}</span>
              </div>
              <span class="user-name">{{ item.followingUser?.userName || '未知用户' }}</span>
              <button class="btn-unfollow" @click="cancelFollow(item.followingUser?.id)">取消关注</button>
            </div>
          </div>
        </div>

        <!-- 我的订单 -->
        <div v-if="activeTab === 'orders'" class="tab-content">
          <div class="section-header">
            <h3>我的订单</h3>
            <div class="order-filters">
              <button :class="{ active: orderType === 'buy' }" @click="orderType = 'buy'; fetchOrders()">购买的</button>
              <button :class="{ active: orderType === 'sell' }" @click="orderType = 'sell'; fetchOrders()">出售的</button>
            </div>
          </div>
          <div v-if="orders.length === 0" class="empty-state">
            <div class="empty-icon emoji-medium">📋</div>
            <p>暂无订单记录</p>
          </div>
          <div v-else class="order-list">
            <div v-for="order in orders" :key="order.orderId" class="order-item glass-dark">
              <div class="order-header">
                <span class="order-id">订单 #{{ order.orderId }}</span>
                <span class="order-status" :class="order.status">{{ order.statusName }}</span>
              </div>
              <div class="order-body">
                <div class="order-good">
                  <img v-if="order.goodImage" :src="order.goodImage" class="order-thumb">
                  <div class="order-info">
                    <p class="order-good-name">{{ order.goodName }}</p>
                    <p class="order-price">¥{{ formatPrice(order.price) }}</p>
                  </div>
                </div>
              </div>
              <div class="order-footer">
                <span class="order-time">{{ formatTime(order.createTime) }}</span>
                <div class="order-actions">
                  <button
                      v-if="orderType === 'sell' && order.status === 0"
                      class="btn-confirm"
                      @click="handleConfirmOrder(order.orderId)"
                  >
                    确认收款
                  </button>
                  <button
                      v-if="order.status === 0"
                      class="btn-cancel"
                      @click="handleCancelOrder(order.orderId)"
                  >
                    取消订单
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 充值对话框 -->
    <div v-if="showRecharge" class="dialog-overlay" @click.self="showRecharge = false">
      <div class="dialog glass">
        <h3>💎 钱包充值</h3>
        <p class="dialog-hint">当前余额: ¥{{ formatBalance(walletBalance) }}</p>
        <div class="amount-options">
          <button
              v-for="amount in rechargeAmounts"
              :key="amount"
              :class="['amount-btn', { active: rechargeAmount === amount }]"
              @click="rechargeAmount = amount"
          >
            ¥{{ amount }}
          </button>
        </div>
        <div class="custom-amount">
          <label>自定义金额:</label>
          <input
              type="number"
              v-model.number="customRechargeAmount"
              placeholder="输入金额"
              min="1"
              max="10000"
          >
        </div>
        <div class="dialog-actions">
          <button class="btn-secondary" @click="showRecharge = false">取消</button>
          <button
              class="btn-primary"
              @click="handleRecharge"
              :disabled="rechargeLoading || (!rechargeAmount && !customRechargeAmount)"
          >
            <span v-if="!rechargeLoading">确认充值</span>
            <span v-else class="spinner-small"></span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserProfile, updateAvatar } from '@/api/user'
import { getMyGoods } from '@/api/good'
import { getMyFavorites } from '@/api/favorite'
import { getMyFollowing } from '@/api/follow'
// 关键修复：给导入的API函数起别名，避免与本地定义的函数名冲突
import { getMyOrders, confirmOrder as apiConfirmOrder, cancelOrder as apiCancelOrder } from '@/api/order'
import { getWalletBalance, rechargeWallet } from '@/api/wallet'
import request from '@/utils/request'

const emit = defineEmits(['go-home', 'logout', 'view-good', 'go-publish'])

// 用户信息
const userInfo = ref({})
const isAdmin = computed(() => userInfo.value?.role === 1)
const userNameFirstChar = computed(() => {
  return (userInfo.value?.userName || '?')[0].toUpperCase()
})

// 标签页
const activeTab = ref('goods')
const tabs = [
  { key: 'goods', label: '我的发布', icon: '📦' },
  { key: 'favorites', label: '我的收藏', icon: '⭐' },
  { key: 'following', label: '我的关注', icon: '👥' },
  { key: 'orders', label: '我的订单', icon: '📋' }
]

// 数据列表
const myGoods = ref([])
const favorites = ref([])
const followings = ref([])
const orders = ref([])
const orderType = ref('buy')

// 钱包相关
const walletBalance = ref(0)
const walletLoading = ref(false)
const showRecharge = ref(false)
const rechargeAmount = ref(100)
const customRechargeAmount = ref('')
const rechargeLoading = ref(false)
const rechargeAmounts = [10, 50, 100, 200, 500]

// 头像上传
const avatarInput = ref(null)

onMounted(() => {
  // 从localStorage获取用户信息（用于快速显示）
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }

  // 加载详细数据
  fetchUserProfile()
  fetchWallet()
  fetchMyGoods()
  fetchFavorites()
  fetchFollowing()
  fetchOrders()
})

// 获取用户详情
const fetchUserProfile = async () => {
  try {
    const res = await getUserProfile()
    if (res.code === 200) {
      userInfo.value = res.data
      // 更新localStorage
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 获取钱包余额
const fetchWallet = async () => {
  walletLoading.value = true
  try {
    const res = await getWalletBalance()
    if (res.code === 200) {
      walletBalance.value = res.data?.balance || 0
    }
  } catch (error) {
    console.error('获取钱包失败:', error)
  } finally {
    walletLoading.value = false
  }
}

const refreshWallet = () => {
  fetchWallet()
  ElMessage.success('余额已刷新')
}

// 充值功能
const showRechargeDialog = () => {
  showRecharge.value = true
  rechargeAmount.value = 100
  customRechargeAmount.value = ''
}

const handleRecharge = async () => {
  const amount = customRechargeAmount.value || rechargeAmount.value
  if (!amount || amount <= 0) {
    ElMessage.warning('请输入有效金额')
    return
  }

  if (amount > 10000) {
    ElMessage.warning('单次充值不能超过10000元')
    return
  }

  rechargeLoading.value = true
  try {
    const res = await rechargeWallet(amount)
    if (res.code === 200) {
      ElMessage.success(`成功充值 ¥${amount}`)
      showRecharge.value = false
      await fetchWallet() // 刷新余额
    } else {
      ElMessage.error(res.message || '充值失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '充值失败，请重试')
  } finally {
    rechargeLoading.value = false
  }
}

// 获取我的发布
const fetchMyGoods = async () => {
  try {
    const res = await getMyGoods()
    if (res.code === 200) {
      myGoods.value = res.data?.goods || []
    }
  } catch (error) {
    console.error('获取我的商品失败:', error)
  }
}

// 获取收藏
const fetchFavorites = async () => {
  try {
    const res = await getMyFavorites()
    if (res.code === 200) {
      favorites.value = res.data?.favorites || []
    }
  } catch (error) {
    console.error('获取收藏失败:', error)
  }
}

// 获取关注
const fetchFollowing = async () => {
  try {
    const res = await getMyFollowing()
    if (res.code === 200) {
      followings.value = res.data?.followings || []
    }
  } catch (error) {
    console.error('获取关注失败:', error)
  }
}

// 获取订单
const fetchOrders = async () => {
  try {
    const res = await getMyOrders(orderType.value)
    if (res.code === 200) {
      orders.value = res.data || []
    }
  } catch (error) {
    console.error('获取订单失败:', error)
  }
}

// 关键修复：重命名本地函数，避免与导入的API函数名冲突
const handleConfirmOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认已收到款项？确认后交易将完成。', '确认收款', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 使用别名调用API
    const res = await apiConfirmOrder(orderId)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      fetchOrders()
      fetchWallet() // 刷新余额（卖家收款）
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

// 关键修复：重命名本地函数，避免与导入的API函数名冲突
const handleCancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
      type: 'warning'
    })

    // 使用别名调用API
    const res = await apiCancelOrder(orderId)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
      fetchWallet() // 刷新余额（退款）
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

// 取消关注
const cancelFollow = async (userId) => {
  if (!userId) return
  try {
    const res = await request({
      url: '/follow/cancel',
      method: 'post',
      data: { followingId: userId }
    })
    if (res.code === 200) {
      ElMessage.success('已取消关注')
      fetchFollowing()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 头像上传
const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)

    const uploadRes = await request({
      url: '/upload/image',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (uploadRes.code === 200 && uploadRes.data?.url) {
      // 更新头像
      const updateRes = await updateAvatar(uploadRes.data.url)
      if (updateRes.code === 200) {
        userInfo.value.avatar = uploadRes.data.url
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        ElMessage.success('头像更新成功')
      }
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
  e.target.value = ''
}

// 退出登录 - 修复图标过大问题
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'logout-confirm-dialog'
  }).then(() => {
    localStorage.removeItem('userInfo')
    emit('logout')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}

// 导航
const goHome = () => emit('go-home')
const goPublish = () => emit('go-publish')
const viewGood = (id) => {
  if (id) emit('view-good', id)
}

// 格式化
const formatPrice = (price) => {
  return price ? parseFloat(price).toFixed(2) : '0.00'
}

const formatBalance = (balance) => {
  if (!balance) return '0.00'
  return parseFloat(balance).toFixed(2)
}

const formatStatus = (status) => {
  const map = {
    '未出售': '在售',
    '已出售': '已售',
    'selling': '在售',
    'sold': '已售'
  }
  return map[status] || status || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') {
    return time.replace('T', ' ').substring(0, 16)
  }
  return '-'
}
</script>

<style scoped>
.user-center {
  min-height: 100vh;
  background: var(--gradient-bg, linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%));
  padding-bottom: 40px;
}

/* 头部样式 */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 16px 24px;
  margin-bottom: 24px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.3s;
}

.brand:hover {
  transform: translateX(-4px);
}

.brand h1 {
  font-size: 24px;
  color: #1f2937;
  font-weight: 700;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
  color: #667eea;
}

.back-btn:hover {
  background: white;
  transform: translateY(-2px);
}

/* 主容器 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 用户卡片 */
.user-card {
  padding: 32px;
  margin-bottom: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
}

.user-main {
  display: flex;
  gap: 32px;
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .user-main {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}

/* 头像区域 */
.avatar-section {
  flex-shrink: 0;
}

.avatar-large {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
}

.avatar-large:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.4);
}

.avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  font-size: 48px;
  font-weight: 700;
  color: white;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-large:hover .avatar-mask {
  opacity: 1;
}

/* 用户信息 */
.user-info {
  flex: 1;
}

.user-name {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.role-tag {
  font-size: 12px;
  padding: 4px 12px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-radius: 20px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.role-tag.admin {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.2);
}

.user-id {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 16px;
}

/* 钱包区域 */
.wallet-section {
  padding: 20px;
  border-radius: 16px;
  margin-top: 16px;
}

.wallet-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #6b7280;
}

.wallet-title {
  font-weight: 600;
  color: #1f2937;
}

.wallet-balance {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 16px;
}

.currency {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.amount {
  font-size: 36px;
  font-weight: 800;
  color: #1f2937;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.wallet-actions {
  display: flex;
  gap: 12px;
}

.btn-recharge {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-recharge:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.btn-refresh {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.6);
  color: #6b7280;
  border-radius: 12px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-refresh:hover:not(:disabled) {
  background: white;
  color: #667eea;
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(102, 126, 234, 0.3);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  display: inline-block;
}

/* 统计栏 */
.stats-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

@media (max-width: 768px) {
  .stats-bar {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-item {
  text-align: center;
  cursor: pointer;
  padding: 12px;
  border-radius: 12px;
  transition: all 0.3s;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-2px);
}

.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 800;
  color: #667eea;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

/* 退出登录区域 */
.logout-section {
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.6);
}

.btn-logout {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 2px solid rgba(239, 68, 68, 0.3);
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-logout:hover {
  background: #ef4444;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.logout-icon {
  font-size: 18px;
}

.logout-text {
  font-size: 16px;
}

/* 标签导航 */
.tabs-nav {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 8px;
  border-radius: 16px;
  overflow-x: auto;
}

.tab-btn {
  flex: 1;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  color: #6b7280;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-2px);
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 内容区域 */
.content-area {
  min-height: 400px;
  padding: 24px;
  border-radius: 24px;
}

.tab-content {
  animation: fadeIn 0.5s ease;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.btn-publish {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-publish:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}

.empty-icon {
  margin-bottom: 16px;
  opacity: 0.8;
}

.btn-action {
  margin-top: 16px;
  padding: 10px 24px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border: 2px solid rgba(102, 126, 234, 0.3);
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-action:hover {
  background: #667eea;
  color: white;
}

/* 商品网格 */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.good-card {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.good-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(31, 38, 135, 0.15);
}

.good-image {
  height: 160px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.good-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  color: #9ca3af;
  font-size: 14px;
}

.good-info {
  padding: 16px;
}

.good-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.good-price {
  font-size: 18px;
  font-weight: 700;
  color: #ff5000;
  margin-bottom: 8px;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.status-badge.已出售,
.status-badge.sold {
  background: rgba(156, 163, 175, 0.1);
  color: #6b7280;
}

/* 用户列表 */
.user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.user-avatar-small {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  overflow: hidden;
  flex-shrink: 0;
}

.user-avatar-small img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-item .user-name {
  flex: 1;
  font-weight: 600;
  color: #1f2937;
}

.btn-unfollow {
  padding: 8px 16px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-unfollow:hover {
  background: #ef4444;
  color: white;
}

/* 订单筛选 */
.order-filters {
  display: flex;
  gap: 8px;
}

.order-filters button {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.3s;
}

.order-filters button.active {
  background: #667eea;
  color: white;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  padding: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
}

.order-id {
  font-weight: 600;
  color: #1f2937;
}

.order-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.order-status[class*="0"] {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.order-status[class*="1"] {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.order-status[class*="2"] {
  background: rgba(156, 163, 175, 0.1);
  color: #6b7280;
}

.order-body {
  margin-bottom: 16px;
}

.order-good {
  display: flex;
  gap: 12px;
  align-items: center;
}

.order-thumb {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  object-fit: cover;
  background: #f3f4f6;
}

.order-info {
  flex: 1;
}

.order-good-name {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.order-price {
  color: #ff5000;
  font-weight: 700;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.6);
}

.order-time {
  color: #9ca3af;
  font-size: 14px;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.btn-confirm {
  padding: 8px 16px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.btn-cancel {
  padding: 8px 16px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel:hover {
  background: #ef4444;
  color: white;
}

/* 对话框 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.dialog {
  width: 100%;
  max-width: 400px;
  padding: 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.9);
  animation: fadeIn 0.3s ease;
}

.dialog h3 {
  font-size: 24px;
  margin-bottom: 8px;
  color: #1f2937;
  text-align: center;
}

.dialog-hint {
  text-align: center;
  color: #6b7280;
  margin-bottom: 24px;
}

.amount-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.amount-btn {
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  font-weight: 700;
  color: #1f2937;
  cursor: pointer;
  transition: all 0.3s;
}

.amount-btn:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.amount-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.custom-amount {
  margin-bottom: 24px;
}

.custom-amount label {
  display: block;
  margin-bottom: 8px;
  color: #6b7280;
  font-size: 14px;
}

.custom-amount input {
  width: 100%;
  padding: 12px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  font-size: 16px;
  text-align: center;
}

.dialog-actions {
  display: flex;
  gap: 12px;
}

.btn-secondary {
  flex: 1;
  padding: 12px;
  background: rgba(255, 255, 255, 0.8);
  color: #6b7280;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: white;
}

.btn-primary {
  flex: 2;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 动画 */
@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>