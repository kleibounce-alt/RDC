<template>
  <div class="detail-container">
    <div class="detail-box">
      <div class="back-bar">
        <a @click="emit('go-back')" class="back-link">← 返回列表</a>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error">{{ error }}</div>

      <div v-else-if="good" class="good-detail">
        <div class="image-section">
          <div class="main-image">
            <img v-if="getField(good, 'image') || getField(good, 'goodImage')" :src="getField(good, 'image') || getField(good, 'goodImage')">
            <div v-else class="no-image">暂无图片</div>
          </div>
        </div>

        <div class="info-section">
          <h1 class="title">{{ getField(good, 'goodName') || getField(good, 'good_name') }}</h1>
          <p class="price">¥{{ formatPrice(getField(good, 'goodPrice') || getField(good, 'price')) }}</p>

          <div class="meta">
            <span class="status" :class="getStatusClass(getField(good, 'sellingStatus') || getField(good, 'selling_status') || getField(good, 'status'))">
              {{ formatStatus(getField(good, 'sellingStatus') || getField(good, 'selling_status') || getField(good, 'status')) }}
            </span>
            <span class="time">发布时间：{{ getField(good, 'createTime') || getField(good, 'create_time') || '未知' }}</span>
          </div>

          <div class="description">
            <h3>商品描述</h3>
            <p>{{ getField(good, 'description') || '暂无描述' }}</p>
          </div>

          <!-- 卖家信息 + 关注按钮 -->
          <div class="seller-info" v-if="sellerInfo">
            <h3>卖家信息</h3>
            <div class="seller-row">
              <div class="seller-avatar">{{ sellerInfo.userName?.[0] }}</div>
              <div class="seller-name">{{ sellerInfo.userName }}</div>
              <button
                  class="follow-btn"
                  :class="{ 'followed': isFollowing }"
                  @click="toggleFollow"
                  :disabled="followLoading"
              >
                {{ followLoading ? '处理中...' : (isFollowing ? '已关注' : '+ 关注') }}
              </button>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="actions">
            <button
                v-if="isSelling(getField(good, 'sellingStatus') || getField(good, 'selling_status') || getField(good, 'status'))"
                class="buy-btn"
                @click="handleBuy"
                :disabled="buying"
            >
              {{ buying ? '处理中...' : '立即购买' }}
            </button>
            <button v-else class="disabled-btn" disabled>
              {{ isSold(getField(good, 'sellingStatus') || getField(good, 'selling_status')) ? '已售出' : '已下架' }}
            </button>

            <!-- 收藏按钮 -->
            <button
                class="fav-btn"
                :class="{ 'favorited': isFavorited }"
                @click="toggleFavorite"
                :disabled="favLoading"
            >
              {{ favLoading ? '...' : (isFavorited ? '已收藏' : '收藏') }}
            </button>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <h3>商品评论</h3>

        <div class="comment-form" v-if="userInfo">
          <textarea
              v-model="newComment"
              rows="3"
              placeholder="发表你的评论..."
          ></textarea>
          <button @click="submitComment" :disabled="commentLoading">
            {{ commentLoading ? '发送中...' : '发表评论' }}
          </button>
        </div>

        <div class="comments-list">
          <div v-if="comments.length === 0" class="no-comments">暂无评论</div>
          <div v-for="comment in comments" :key="comment.commentId || comment.id" class="comment-item">
            <div class="comment-header">
              <span class="comment-user">{{ comment.userName }}</span>
              <span class="comment-time">{{ comment.createTime }}</span>
            </div>
            <p class="comment-content">{{ comment.content }}</p>
            <div class="comment-actions" v-if="isMyComment(comment)">
              <button class="delete-btn" @click="deleteMyComment(comment.commentId || comment.id)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGoodDetail } from '@/api/good'
import { createOrder } from '@/api/order'
import { getWalletBalance } from '@/api/wallet'
import { addComment, deleteComment, getGoodComments } from '@/api/comment'
import { addFavorite, cancelFavorite } from '@/api/favorite'
import { addFollow, cancelFollow } from '@/api/follow'

const props = defineProps(['goodId'])
const emit = defineEmits(['go-back', 'buy-success'])

const good = ref(null)
const sellerInfo = ref(null)
const loading = ref(true)
const error = ref('')
const buying = ref(false)
const comments = ref([])
const newComment = ref('')
const commentLoading = ref(false)
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

// 收藏相关
const isFavorited = ref(false)
const favLoading = ref(false)

// 关注相关
const isFollowing = ref(false)
const followLoading = ref(false)

const getField = (obj, fieldName) => {
  if (!obj) return undefined
  if (obj[fieldName] !== undefined) return obj[fieldName]
  const underline = fieldName.replace(/([A-Z])/g, '_$1').toLowerCase()
  if (obj[underline] !== undefined) return obj[underline]
  const camel = underline.replace(/_([a-z])/g, (match, p1) => p1.toUpperCase())
  if (obj[camel] !== undefined) return obj[camel]
  return undefined
}

onMounted(() => {
  fetchDetail()
  fetchComments()
  checkFavoriteStatus()
  checkFollowStatus()
})

const fetchDetail = async () => {
  if (!props.goodId) {
    error.value = '商品ID不存在'
    loading.value = false
    return
  }
  try {
    const res = await getGoodDetail(props.goodId)
    if (res.code === 200) {
      good.value = res.data
      // 假设后端返回的good中包含sellerInfo，或者你需要再调接口查卖家
      if (res.data.sellerInfo) {
        sellerInfo.value = res.data.sellerInfo
      } else if (res.data.sellerId || res.data.seller_id) {
        // 如果没有返回卖家信息，这里可以调接口查，或者先mock
        sellerInfo.value = {
          userId: res.data.sellerId || res.data.seller_id,
          userName: '卖家' + (res.data.sellerId || res.data.seller_id)
        }
      }
    } else {
      error.value = res.message || '商品不存在'
    }
  } catch (err) {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getGoodComments(props.goodId)
    if (res.code === 200) {
      comments.value = res.data || []
    }
  } catch (err) {
    console.error('获取评论失败:', err)
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  commentLoading.value = true
  try {
    const res = await addComment({
      goodId: props.goodId,
      content: newComment.value
    })
    if (res.code === 200) {
      newComment.value = ''
      fetchComments()
    } else {
      alert(res.message || '评论失败')
    }
  } catch (err) {
    alert('评论失败')
  } finally {
    commentLoading.value = false
  }
}

const isMyComment = (comment) => {
  return comment.userId === userInfo.value.userId
}

const deleteMyComment = async (id) => {
  if (!confirm('确定删除这条评论？')) return
  try {
    const res = await deleteComment(id)
    if (res.code === 200) {
      fetchComments()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (err) {
    alert('删除失败')
  }
}

// 检查是否已收藏
const checkFavoriteStatus = async () => {
  // 这里需要后端提供一个接口查是否已收藏，或者在前端本地判断
  // 暂时先默认未收藏，等用户点击
}

// 切换收藏状态
const toggleFavorite = async () => {
  if (!userInfo.value.userId) {
    alert('请先登录')
    return
  }
  favLoading.value = true
  try {
    if (isFavorited.value) {
      // 取消收藏
      const res = await cancelFavorite(props.goodId)
      if (res.code === 200) {
        isFavorited.value = false
        alert('已取消收藏')
      }
    } else {
      // 添加收藏
      const res = await addFavorite(props.goodId)
      if (res.code === 200) {
        isFavorited.value = true
        alert('收藏成功')
      }
    }
  } catch (err) {
    alert('操作失败')
  } finally {
    favLoading.value = false
  }
}

// 检查是否已关注
const checkFollowStatus = async () => {
  // 同样需要后端接口或本地判断
}

// 切换关注状态
const toggleFollow = async () => {
  if (!userInfo.value.userId) {
    alert('请先登录')
    return
  }
  if (!sellerInfo.value?.userId) return

  followLoading.value = true
  try {
    if (isFollowing.value) {
      const res = await cancelFollow(sellerInfo.value.userId)
      if (res.code === 200) {
        isFollowing.value = false
        alert('已取消关注')
      }
    } else {
      const res = await addFollow(sellerInfo.value.userId)
      if (res.code === 200) {
        isFollowing.value = true
        alert('关注成功')
      }
    }
  } catch (err) {
    alert('操作失败')
  } finally {
    followLoading.value = false
  }
}

const formatPrice = (price) => {
  if (!price) return '0.00'
  const num = parseFloat(price)
  return isNaN(num) ? '0.00' : num.toFixed(2)
}

const formatStatus = (status) => {
  const s = String(status || '').trim()
  if (s === '未出售' || s === '1' || s === 'selling') return '出售中'
  if (s === '已出售' || s === '2' || s === 'sold') return '已售出'
  if (s === '已下架' || s === '0' || s === 'offline') return '已下架'
  return s || '未知'
}

const getStatusClass = (status) => {
  const s = String(status || '').trim()
  if (s === '未出售' || s === '1' || s === 'selling') return 'selling'
  if (s === '已出售' || s === '2' || s === 'sold') return 'sold'
  if (s === '已下架' || s === '0' || s === 'offline') return 'offline'
  return ''
}

const isSelling = (status) => {
  const s = String(status || '').trim()
  return s === '未出售' || s === '1' || s === 'selling'
}

const isSold = (status) => {
  const s = String(status || '').trim()
  return s === '已出售' || s === '2' || s === 'sold'
}

const handleBuy = async () => {
  const price = getField(good.value, 'goodPrice') || getField(good.value, 'price')
  const name = getField(good.value, 'goodName') || getField(good.value, 'good_name')

  if (!confirm(`确认购买 "${name}" 吗？价格：¥${formatPrice(price)}`)) return

  buying.value = true
  try {
    const balanceRes = await getWalletBalance()
    if (balanceRes.code === 200) {
      const balance = balanceRes.data.balance || 0
      if (balance < parseFloat(price || 0)) {
        alert(`余额不足！当前余额：¥${formatPrice(balance)}`)
        buying.value = false
        return
      }
    }

    const orderRes = await createOrder(props.goodId)
    if (orderRes.code === 200) {
      alert('购买成功！')
      emit('buy-success', orderRes.data)
    } else {
      alert(orderRes.message || '购买失败')
    }
  } catch (err) {
    alert(err.response?.data?.message || '购买失败')
  } finally {
    buying.value = false
  }
}
</script>

<style scoped>
.detail-container { min-height: 100vh; background: #f5f5f5; padding: 20px; }
.detail-box { max-width: 1000px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }
.back-bar { padding: 15px 20px; border-bottom: 1px solid #eee; }
.back-link { color: #666; cursor: pointer; font-size: 14px; }
.loading, .error { text-align: center; padding: 60px 20px; color: #999; }
.error { color: #e74c3c; }
.good-detail { display: flex; min-height: 500px; }
.image-section { width: 50%; padding: 20px; border-right: 1px solid #eee; }
.main-image { width: 100%; height: 400px; background: #f5f5f5; border-radius: 8px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.main-image img { width: 100%; height: 100%; object-fit: cover; }
.no-image { color: #999; font-size: 16px; }
.info-section { width: 50%; padding: 30px; }
.title { font-size: 24px; color: #333; margin-bottom: 15px; }
.price { font-size: 32px; color: #ff5000; font-weight: bold; margin-bottom: 20px; }
.meta { margin-bottom: 20px; display: flex; gap: 15px; align-items: center; }
.status { padding: 4px 12px; border-radius: 4px; font-size: 14px; }
.status.selling { background: #e6f7e6; color: #52c41a; }
.status.sold { background: #f5f5f5; color: #999; }
.status.offline { background: #fff2f0; color: #ff4d4f; }
.time { color: #999; font-size: 14px; }
.description { margin-bottom: 20px; }
.description h3 { font-size: 16px; color: #333; margin-bottom: 10px; }
.description p { color: #666; line-height: 1.6; font-size: 14px; }

.seller-info { margin-bottom: 20px; padding: 15px; background: #f9f9f9; border-radius: 8px; }
.seller-info h3 { font-size: 16px; margin-bottom: 12px; color: #333; }
.seller-row { display: flex; align-items: center; gap: 12px; }
.seller-avatar { width: 40px; height: 40px; border-radius: 50%; background: #667eea; color: white; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.seller-name { flex: 1; font-weight: 500; color: #333; }
.follow-btn { padding: 6px 16px; background: #667eea; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
.follow-btn.followed { background: #999; }
.follow-btn:disabled { background: #ccc; }

.actions { display: flex; gap: 15px; margin-bottom: 20px; }
.buy-btn { flex: 1; padding: 15px; background: #ff5000; color: white; border: none; border-radius: 8px; font-size: 18px; cursor: pointer; }
.buy-btn:hover:not(:disabled) { background: #e64400; }
.buy-btn:disabled { background: #ccc; }
.disabled-btn { flex: 1; padding: 15px; background: #f5f5f5; color: #999; border: none; border-radius: 8px; font-size: 18px; cursor: not-allowed; }
.fav-btn { padding: 15px 30px; background: white; color: #667eea; border: 1px solid #667eea; border-radius: 8px; font-size: 16px; cursor: pointer; }
.fav-btn.favorited { background: #ff5000; color: white; border-color: #ff5000; }
.fav-btn:hover:not(:disabled) { background: #f0f0ff; }
.fav-btn.favorited:hover:not(:disabled) { background: #e64400; }

.comments-section { padding: 30px; border-top: 1px solid #eee; }
.comments-section h3 { margin-bottom: 20px; color: #333; }
.comment-form { margin-bottom: 30px; }
.comment-form textarea { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 4px; resize: vertical; font-family: inherit; margin-bottom: 10px; }
.comment-form button { padding: 8px 20px; background: #667eea; color: white; border: none; border-radius: 4px; cursor: pointer; }
.comment-form button:disabled { background: #ccc; }
.comments-list { display: flex; flex-direction: column; gap: 15px; }
.no-comments { text-align: center; color: #999; padding: 40px; }
.comment-item { padding: 15px; background: #f9f9f9; border-radius: 8px; }
.comment-header { margin-bottom: 8px; display: flex; gap: 15px; }
.comment-user { font-weight: bold; color: #667eea; }
.comment-time { color: #999; font-size: 12px; }
.comment-content { color: #333; line-height: 1.6; margin-bottom: 10px; }
.comment-actions { text-align: right; }
.delete-btn { padding: 4px 12px; background: white; border: 1px solid #ff4d4f; color: #ff4d4f; border-radius: 4px; cursor: pointer; font-size: 12px; }

@media (max-width: 768px) {
  .good-detail { flex-direction: column; }
  .image-section, .info-section { width: 100%; border-right: none; }
}
</style>