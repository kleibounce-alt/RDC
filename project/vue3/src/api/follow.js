import request from '@/utils/request'

export function addFollow(userId) {
    return request({
        url: '/follow/add',
        method: 'post',
        data: { userId }  // 对应 dto.followingId
    })
}

// 取消关注 - 调用你现有的 unfollow 接口
export function cancelFollow(userId) {
    return request({
        url: '/follow/cancel',
        method: 'post',
        data: { userId }  // 对应 dto.followingId
    })
}

export function getMyFollowing() {
    return request({
        url: '/user/follow',
        method: 'get'
    })
}