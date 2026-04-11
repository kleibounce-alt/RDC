import request from '@/utils/request'

// 关注用户
export function addFollow(userId) {
    return request({
        url: '/follow/add',
        method: 'post',
        data: { followingId: userId }
    })
}

// 取消关注
export function cancelFollow(userId) {
    return request({
        url: '/follow/cancel',
        method: 'post',
        data: { followingId: userId }
    })
}

// 获取我的关注列表（UserCenter.vue 需要的）
export function getMyFollowing() {
    return request({
        url: '/user/follow',
        method: 'get'
    })
}