import request from '@/utils/request'

// POST /comment/publish
export function addComment(data) {
    return request({
        url: '/comment/publish',
        method: 'post',
        data: {
            goodId: data.goodId,
            content: data.content
        }
    })
}

// POST /comment/delete
export function deleteComment(commentId) {
    return request({
        url: '/comment/delete',
        method: 'post',
        data: { commentId }
    })
}

// GET /comment/list?goodId=xxx
export function getGoodComments(goodId) {
    return request({
        url: '/comment/list',
        method: 'get',
        params: { goodId }
    })
}

// POST /comment/like - 确保这个API存在
export function likeComment(commentId) {
    return request({
        url: '/comment/like',
        method: 'post',
        data: { commentId }
    })
}

// POST /comment/unlike - 确保这个API存在
export function unlikeComment(commentId) {
    return request({
        url: '/comment/unlike',
        method: 'post',
        data: { commentId }
    })
}