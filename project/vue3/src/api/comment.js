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
        params: { commentId }
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