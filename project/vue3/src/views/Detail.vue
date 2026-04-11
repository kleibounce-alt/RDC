<template>
  <div class="detail-container">
    <div class="detail-box glass">
      <!-- 返回栏 -->
      <div class="back-bar">
        <div class="back-link" @click="goBack">
          <span>←</span> 返回首页
        </div>
        <div v-if="isAdmin" class="admin-badge-bar">
          <span class="admin-tag">管理员模式</span>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-wrapper">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else class="good-detail">
        <!-- 图片区域 -->
        <div class="image-section">
          <div class="main-image">
            <img v-if="good?.goodImage" :src="good.goodImage" :alt="good.goodName" @error="handleImageError">
            <div v-else class="no-image-placeholder">
              <span class="emoji-small">📷</span>
              <p>暂无图片</p>
            </div>
          </div>
        </div>

        <!-- 信息区域 -->
        <div class="info-section">
          <h1 class="title">{{ good?.goodName || '未知商品' }}</h1>
          <div class="price">¥{{ formatPrice(good?.goodPrice) }}</div>

          <!-- 卖家信息 -->
          <div v-if="sellerInfo && sellerInfo.userId" class="seller-info glass-dark">
            <div class="seller-row">
              <div class="seller-avatar">
                <img v-if="sellerInfo?.avatar" :src="sellerInfo.avatar" alt="avatar" @error="$event.target.style.display='none'">
                <span v-else>{{ sellerInfo?.userName?.[0] || '?' }}</span>
              </div>
              <div class="seller-detail">
                <div class="seller-name">{{ sellerInfo?.userName || '未知用户' }}</div>
                <div class="seller-meta">卖家ID: {{ sellerInfo?.userId }}</div>
              </div>
              <button v-if="canShowFollowButton" class="follow-btn" :class="{ 'is-following': isFollowing }" @click="toggleFollow">
                {{ isFollowing ? '已关注' : '+ 关注' }}
              </button>
            </div>
          </div>

          <div class="description glass-dark" v-if="good?.description">
            <h3>商品描述</h3>
            <p>{{ good.description }}</p>
          </div>

          <div class="meta-info">
            <span class="status-badge" :class="good?.sellingStatus">
              {{ formatStatus(good?.sellingStatus) }}
            </span>
            <span class="time">发布时间: {{ formatTime(good?.createTime) }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="actions">
            <!-- 购买按钮：普通用户可买自己的不可买，管理员可买任何（包括自己的，用于测试或自用） -->
            <button v-if="canBuy" class="buy-btn" @click="handleBuy" :disabled="buyLoading">
              <span v-if="!buyLoading">立即购买</span>
              <span v-else class="btn-spinner"></span>
            </button>
            <button v-else-if="isSelfGood && !isAdmin" class="buy-btn disabled" disabled>我的商品</button>
            <button v-else-if="good?.sellingStatus === '已出售' || good?.sellingStatus === 'sold'" class="buy-btn disabled" disabled>已售出</button>

            <!-- 管理员删除商品按钮 -->
            <button v-if="isAdmin" class="admin-delete-btn" @click="adminDeleteGood">
              🗑️ 管理员删除
            </button>

            <button class="fav-btn" :class="{ 'is-favorited': isFavorited }" @click="toggleFavorite">
              <span>{{ isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}</span>
            </button>
          </div>

          <!-- 管理员提示 -->
          <div v-if="isAdmin" class="admin-hint">
            <p>💡 管理员提示：您可以删除此商品、删除任何评论，也可以购买此商品进行测试</p>
          </div>
        </div>
      </div>

      <!-- 评论区域 -->
      <div class="comments-section">
        <h3 class="section-title">商品评论 ({{ comments.length }})</h3>

        <!-- 发布评论：管理员和普通登录用户都可以 -->
        <div v-if="isLogin" class="comment-form-wrapper">
          <div class="comment-form glass">
            <textarea v-model="newComment" placeholder="写下你的评论..." rows="3" maxlength="500"></textarea>
            <div class="comment-actions">
              <span class="char-count">{{ newComment.length }}/500</span>
              <button class="submit-comment" @click="submitComment" :disabled="!newComment.trim() || commentLoading">
                {{ commentLoading ? '发布中...' : '发布评论' }}
              </button>
            </div>
          </div>
        </div>

        <div v-else class="login-tip glass-dark">
          请<a @click="goLogin">登录</a>后参与评论
        </div>

        <!-- 评论列表 -->
        <div class="comments-list">
          <div v-if="!comments || comments.length === 0" class="empty-comments glass-dark">
            <div class="empty-icon emoji-small">💬</div>
            <p>暂无评论，快来抢沙发</p>
          </div>

          <template v-if="comments && comments.length > 0">
            <div
                v-for="comment in comments"
                :key="comment.commentId || comment.id"
                class="comment-item glass"
            >
              <div class="comment-header">
                <div class="comment-user">
                  <div class="user-avatar">
                    <img v-if="comment.avatar" :src="comment.avatar" alt="avatar" @error="$event.target.style.display='none'">
                    <span v-else>{{ (comment.userName || '?')[0] }}</span>
                  </div>
                  <span class="user-name">{{ comment.userName || '匿名用户' }}</span>
                  <span v-if="comment.userId === good?.sellerId" class="author-tag">卖家</span>
                  <span v-if="isAdmin && comment.userId !== currentUserId" class="user-tag">UID:{{ comment.userId }}</span>
                </div>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>

              <p class="comment-content">{{ comment.content || '无内容' }}</p>

              <div class="comment-footer">
                <button class="like-btn" :class="{ 'is-liked': comment.hasLiked }" @click="toggleLike(comment)">
                  {{ comment.hasLiked ? '👍' : '👍' }} {{ comment.likeCount || 0 }}
                </button>
                <!-- 删除按钮：本人或管理员可删除 -->
                <button v-if="canDeleteComment(comment)" class="delete-btn" @click="deleteComment(comment.commentId || comment.id)">
                  删除
                </button>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getGoodDetail, deleteGood as apiDeleteGood } from '@/api/good'
import { createOrder } from '@/api/order'
import { addFavorite, cancelFavorite } from '@/api/favorite'
import { addFollow, cancelFollow } from '@/api/follow'
import { addComment, getGoodComments, deleteComment as apiDeleteComment, likeComment, unlikeComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  goodId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['go-back', 'buy-success', 'go-user-center'])

const good = ref({})
const sellerInfo = ref(null)
const comments = ref([])
const newComment = ref('')
const isFavorited = ref(false)
const isFollowing = ref(false)
const buyLoading = ref(false)
const commentLoading = ref(false)
const currentUserId = ref(null)
const userInfo = ref({})
const loading = ref(true)

// 修复：从localStorage读取用户信息，包括角色
onMounted(() => {
  try {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      userInfo.value = JSON.parse(stored)
      currentUserId.value = userInfo.value.userId
      console.log('[Detail] 当前用户信息:', userInfo.value)
    }
  } catch (e) {
    console.error('解析用户信息失败:', e)
  }

  fetchDetail()
  fetchComments()
})

// 计算属性：是否为管理员（role === 1）
const isAdmin = computed(() => {
  return userInfo.value && userInfo.value.role === 1
})

const isLogin = computed(() => {
  return !!currentUserId.value
})

const isSelfGood = computed(() => {
  return currentUserId.value && good.value?.sellerId && currentUserId.value === good.value.sellerId
})

const canShowFollowButton = computed(() => {
  return isLogin.value && sellerInfo.value && sellerInfo.value.userId && sellerInfo.value.userId !== currentUserId.value
})

// 修复：管理员可以购买任何商品（包括自己的，用于测试），普通用户不能买自己的
const canBuy = computed(() => {
  const isSelling = good.value?.sellingStatus === '未出售' || good.value?.sellingStatus === 'selling' || good.value?.sellingStatus === 0
  if (isAdmin.value) return isSelling // 管理员可买任何在售的（包括自己的）
  return isSelling && !isSelfGood.value // 普通用户不能买自己的
})

// 获取商品详情
const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getGoodDetail(props.goodId)
    if (res.code === 200) {
      good.value = res.data || {}
      sellerInfo.value = res.data?.sellerInfo || null
      // 如果后端返回了当前用户信息则使用，否则使用localStorage的
      if (res.data?.currentUserId) {
        currentUserId.value = res.data.currentUserId
      }
      isFollowing.value = res.data?.sellerInfo?.isFollowing || false
      isFavorited.value = res.data?.isFavorited || false
    } else {
      ElMessage.error(res.message || '获取商品详情失败')
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const fetchComments = async () => {
  try {
    const res = await getGoodComments(props.goodId)
    if (res.code === 200) {
      let data = []
      if (Array.isArray(res.data)) {
        data = res.data
      } else if (res.data && typeof res.data === 'object') {
        data = res.data.list || res.data.comments || res.data.data || []
      }

      comments.value = data.map((item, index) => {
        if (!item || typeof item !== 'object') return null
        return {
          commentId: item.commentId || item.id || index,
          content: item.content || '',
          userId: item.userId,
          userName: item.userName || '匿名用户',
          avatar: item.avatar || '',
          createTime: item.createTime || new Date().toISOString(),
          likeCount: item.likeCount || 0,
          hasLiked: !!item.hasLiked
        }
      }).filter(item => item !== null)
    } else {
      comments.value = []
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    comments.value = []
  }
}

const formatPrice = (price) => {
  return price ? parseFloat(price).toFixed(2) : '0.00'
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
  if (!time) return '未知时间'
  if (typeof time === 'string') {
    if (time.includes('T')) {
      return new Date(time).toLocaleString()
    }
    return time
  }
  return '未知时间'
}

const handleImageError = (e) => {
  e.target.style.display = 'none'
}

const goBack = () => {
  emit('go-back')
}

const goLogin = () => {
  emit('go-back')
}

const toggleFollow = async () => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isFollowing.value) {
      await cancelFollow(sellerInfo.value.userId)
      ElMessage.success('取消关注成功')
    } else {
      await addFollow(sellerInfo.value.userId)
      ElMessage.success('关注成功')
    }
    isFollowing.value = !isFollowing.value
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const toggleFavorite = async () => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isFavorited.value) {
      await cancelFavorite(props.goodId)
      ElMessage.success('取消收藏成功')
    } else {
      await addFavorite(props.goodId)
      ElMessage.success('收藏成功')
    }
    isFavorited.value = !isFavorited.value
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 购买
const handleBuy = async () => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录')
    return
  }

  if (isSelfGood.value && !isAdmin.value) {
    ElMessage.warning('不能购买自己发布的商品')
    return
  }

  try {
    await ElMessageBox.confirm(
        `确定要购买"${good.value.goodName}"吗？\n\n价格：¥${formatPrice(good.value.goodPrice)}\n卖家：${sellerInfo.value?.userName || '未知'}`,
        '确认购买',
        {
          confirmButtonText: '确认购买',
          cancelButtonText: '取消',
          type: 'warning',
          customClass: 'buy-confirm-dialog',
          dangerouslyUseHTMLString: false,
          closeOnClickModal: false
        }
    )
  } catch (e) {
    return
  }

  buyLoading.value = true
  try {
    const res = await createOrder(props.goodId)
    if (res.code === 200) {
      ElMessageBox.alert(
          `购买成功！\n\n商品：${good.value.goodName}\n金额：¥${formatPrice(good.value.goodPrice)}\n订单状态：等待卖家确认`,
          '购买成功',
          {
            confirmButtonText: '查看我的订单',
            cancelButtonText: '留在当前页面',
            showCancelButton: true,
            type: 'success',
            customClass: 'buy-success-dialog',
            callback: (action) => {
              if (action === 'confirm') {
                emit('go-user-center')
              }
            }
          }
      )
      emit('buy-success', res.data)
      await fetchDetail()
    } else {
      ElMessage.error(res.message || '购买失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '购买失败，余额不足或商品已被购买')
  } finally {
    buyLoading.value = false
  }
}

// 管理员删除商品
const adminDeleteGood = async () => {
  if (!isAdmin.value) {
    ElMessage.error('无权操作')
    return
  }

  try {
    await ElMessageBox.confirm(
        `确定要删除商品"${good.value.goodName}"吗？\n\n此操作不可恢复，商品将被逻辑删除（下架处理）。`,
        '管理员删除确认',
        {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'error',
          customClass: 'admin-delete-dialog'
        }
    )

    const res = await apiDeleteGood(props.goodId)
    if (res.code === 200) {
      ElMessage.success('商品已删除（下架）')
      setTimeout(() => {
        emit('go-back')
      }, 1500)
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  const content = newComment.value.trim()
  commentLoading.value = true

  try {
    const res = await addComment({
      goodId: props.goodId,
      content: content
    })

    if (res.code === 200) {
      ElMessage.success('评论成功')
      newComment.value = ''
      await fetchComments()
    } else {
      ElMessage.error(res.message || '评论失败')
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error(error.response?.data?.message || '评论失败')
  } finally {
    commentLoading.value = false
  }
}

const deleteComment = async (commentId) => {
  if (!commentId) {
    ElMessage.warning('评论ID无效')
    return
  }

  const isSelfComment = comments.value.find(c => (c.commentId || c.id) === commentId)?.userId === currentUserId.value

  try {
    await ElMessageBox.confirm(
        isAdmin.value && !isSelfComment ? '管理员确定要删除这条评论吗？' : '确定要删除这条评论吗？',
        '删除确认',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const res = await apiDeleteComment(commentId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await fetchComments()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 修复：管理员可以删除任何评论，普通用户只能删除自己的
const canDeleteComment = (comment) => {
  const commentId = comment.commentId || comment.id
  const commentUserId = comment.userId

  if (!commentId) return false

  // 管理员可删任何评论
  if (isAdmin.value) return true

  // 普通用户只能删自己的
  return commentUserId && currentUserId.value && commentUserId === currentUserId.value
}

const toggleLike = async (comment) => {
  if (!isLogin.value) {
    ElMessage.warning('请先登录')
    return
  }

  const commentId = comment.commentId || comment.id
  if (!commentId) {
    ElMessage.error('评论ID无效')
    return
  }

  try {
    if (comment.hasLiked) {
      const res = await unlikeComment(commentId)
      if (res.code === 200) {
        comment.hasLiked = false
        comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
      }
    } else {
      const res = await likeComment(commentId)
      if (res.code === 200) {
        comment.hasLiked = true
        comment.likeCount = (comment.likeCount || 0) + 1
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
    await fetchComments()
  }
}
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  padding: 24px;
  background: var(--gradient-bg, linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%));
  overflow-x: hidden;
}

.detail-box {
  max-width: 1000px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
  overflow: hidden;
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

.back-bar {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  font-weight: 600;
  cursor: pointer;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s;
}

.back-link:hover {
  background: white;
  transform: translateX(-4px);
}

.admin-badge-bar {
  display: flex;
  align-items: center;
}

.admin-tag {
  padding: 4px 12px;
  background: #ef4444;
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
}

.good-detail {
  display: flex;
  min-height: 500px;
}

.image-section {
  width: 50%;
  padding: 32px;
  background: rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #9ca3af;
  font-size: 18px;
}

.no-image-placeholder span {
  font-size: 48px;
  margin-bottom: 8px;
}

.info-section {
  width: 50%;
  padding: 32px;
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 16px;
  line-height: 1.3;
}

.price {
  font-size: 36px;
  color: #ff5000;
  font-weight: 800;
  margin-bottom: 24px;
  text-shadow: 0 2px 4px rgba(255, 80, 0, 0.2);
}

.seller-info {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
}

.seller-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.seller-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  overflow: hidden;
  flex-shrink: 0;
}

.seller-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.seller-detail {
  flex: 1;
}

.seller-name {
  font-weight: 600;
  font-size: 16px;
  color: #1f2937;
}

.seller-meta {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.follow-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.follow-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.follow-btn.is-following {
  background: rgba(156, 163, 175, 0.8);
}

.description {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.description h3 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #1f2937;
}

.description p {
  color: #4b5563;
  line-height: 1.6;
  white-space: pre-wrap;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-badge.已出售, .status-badge.sold {
  background: rgba(156, 163, 175, 0.1);
  color: #6b7280;
  border-color: rgba(156, 163, 175, 0.2);
}

.time {
  color: #9ca3af;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: auto;
  padding-top: 24px;
  flex-wrap: wrap;
}

.buy-btn {
  flex: 2;
  min-width: 120px;
  padding: 16px 32px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
  color: white;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(255, 107, 107, 0.3);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.buy-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(255, 107, 107, 0.4);
}

.buy-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.buy-btn.disabled {
  background: #ccc;
  box-shadow: none;
}

.admin-delete-btn {
  flex: 1;
  min-width: 120px;
  padding: 16px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 2px solid #ef4444;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.admin-delete-btn:hover {
  background: #ef4444;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.btn-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

.fav-btn {
  flex: 1;
  min-width: 100px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.fav-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.fav-btn.is-favorited {
  background: #ff5000;
  color: white;
  border-color: #ff5000;
}

.admin-hint {
  margin-top: 16px;
  padding: 12px;
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: 8px;
  color: #b45309;
  font-size: 13px;
}

.comments-section {
  padding: 32px;
  background: rgba(255, 255, 255, 0.4);
  border-top: 1px solid rgba(255, 255, 255, 0.5);
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 24px;
  color: #1f2937;
}

.comment-form-wrapper {
  margin-bottom: 24px;
  border-radius: 16px;
  overflow: hidden;
}

.comment-form {
  background: rgba(255, 255, 255, 0.7);
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
}

.comment-form textarea {
  width: 100%;
  padding: 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(102, 126, 234, 0.1);
  resize: vertical;
  min-height: 100px;
  margin-bottom: 12px;
  font-family: inherit;
  font-size: 15px;
}

.comment-form textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  color: #9ca3af;
  font-size: 14px;
}

.submit-comment {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-comment:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.submit-comment:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #ccc;
}

.login-tip {
  background: rgba(255, 255, 255, 0.5);
  padding: 20px;
  text-align: center;
  border-radius: 12px;
  margin-bottom: 24px;
  color: #6b7280;
}

.login-tip a {
  color: #667eea;
  cursor: pointer;
  font-weight: 600;
  text-decoration: underline;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-comments {
  text-align: center;
  padding: 60px 40px;
  color: #9ca3af;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 16px;
}

.comment-item {
  background: rgba(255, 255, 255, 0.7);
  padding: 20px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s;
}

.comment-item:hover {
  transform: translateX(4px);
  background: rgba(255, 255, 255, 0.9);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  flex-shrink: 0;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-weight: 600;
  color: #1f2937;
}

.author-tag {
  padding: 2px 8px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.user-tag {
  padding: 2px 6px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-radius: 4px;
  font-size: 10px;
}

.comment-time {
  font-size: 12px;
  color: #9ca3af;
}

.comment-content {
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 12px;
  word-break: break-all;
  font-size: 15px;
}

.comment-footer {
  display: flex;
  gap: 16px;
  align-items: center;
}

.like-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0,0,0,0.1);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #6b7280;
}

.like-btn:hover {
  background: rgba(255, 255, 255, 0.9);
}

.like-btn.is-liked {
  background: rgba(255, 80, 0, 0.1);
  color: #ff5000;
  border-color: #ff5000;
}

.delete-btn {
  padding: 6px 12px;
  background: transparent;
  color: #ef4444;
  border: 1px solid #ef4444;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  margin-left: auto;
}

.delete-btn:hover {
  background: #ef4444;
  color: white;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .detail-container {
    padding: 12px;
  }

  .good-detail {
    flex-direction: column;
  }

  .image-section, .info-section {
    width: 100%;
  }

  .main-image {
    height: 300px;
  }

  .price {
    font-size: 28px;
  }

  .actions {
    flex-direction: column;
  }

  .buy-btn, .admin-delete-btn, .fav-btn {
    flex: none;
    width: 100%;
  }
}
</style>