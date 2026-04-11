import request from '@/utils/request'

// 获取所有标签列表
export function getAllTags() {
    return request({
        url: '/tag/list',
        method: 'get'
    })
}

// 根据标签查询商品
export function getGoodsByTag(tagId) {
    return request({
        url: '/good/tag',
        method: 'get',
        params: { tagId }
    })
}

// 创建新标签（用户可自定义添加）
export function createTag(tagName) {
    return request({
        url: '/tag/create',
        method: 'post',
        data: {
            tagName: tagName,
            // userId 由后端从 session 获取，前端可不传或传 null
        }
    })
}