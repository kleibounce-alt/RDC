import request from '@/utils/request'

// 获取随机推荐商品
export function getRandomGoods(limit = 8) {
    return request({
        url: '/good/random',
        method: 'get',
        params: { limit }
    })
}

// 发布商品（带标签）
export function publishGood(data) {
    return request({
        url: '/good/publish',
        method: 'post',
        data: {
            goodName: data.goodName,
            image: data.image,
            price: data.price,
            description: data.description,
            tagIds: data.tagIds  // 新增：标签ID数组
        }
    })
}

// 获取商品详情
export function getGoodDetail(id) {
    return request({
        url: '/good/detail',
        method: 'get',
        params: { id }
    })
}

// 搜索商品
export function searchGoods(keyword) {
    return request({
        url: '/good/search',
        method: 'get',
        params: { keyword }
    })
}

// 删除商品
export function deleteGood(goodId) {
    return request({
        url: '/good/delete',
        method: 'post',
        data: { goodId }
    })
}

// 获取我发布的商品（UserCenter.vue 需要的）
export function getMyGoods() {
    return request({
        url: '/user/good',
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

// 获取商品的标签列表（可选，用于编辑时回显）
export function getGoodTags(goodId) {
    return request({
        url: '/good/tags',
        method: 'get',
        params: { goodId }
    })
}

// 图片上传（form-data）
export function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/upload/image',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}