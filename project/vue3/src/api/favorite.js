import request from '@/utils/request'

// 添加收藏
export function addFavorite(goodId) {
    return request({
        url: '/favorite/add',
        method: 'post',
        data: { goodId }
    })
}

// 取消收藏
export function cancelFavorite(goodId) {
    return request({
        url: '/favorite/cancel',
        method: 'post',
        data: { goodId }
    })
}

// 获取我的收藏列表（UserCenter.vue 第295行需要的）
export function getMyFavorites() {
    return request({
        url: '/user/favorite',
        method: 'get'
    })
}

// 检查是否已收藏（可选）
export function isFavorited(goodId) {
    return request({
        url: '/favorite/status',
        method: 'get',
        params: { goodId }
    })
}