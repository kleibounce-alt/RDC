import request from '@/utils/request'

export function addFavorite(goodId) {
    return request({
        url: '/favorite/add',
        method: 'post',
        data: { goodId }
    })
}

// 新增的取消收藏
export function cancelFavorite(goodId) {
    return request({
        url: '/favorite/cancel',
        method: 'post',
        data: { goodId }
    })
}