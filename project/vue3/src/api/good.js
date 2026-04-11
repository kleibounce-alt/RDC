import request from '@/utils/request'

// 获取随机推荐商品
export function getRandomGoods(limit = 8) {
    return request({
        url: '/good/random',
        method: 'get',
        params: { limit }
    })
}

// 发布商品
export function publishGood(data) {
    return request({
        url: '/good/publish',
        method: 'post',
        data: {
            goodName: data.goodName,
            image: data.image,
            price: data.price,
            description: data.description
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

// ★★★ 新增的删除商品方法 ★★★
export function deleteGood(goodId) {
    return request({
        url: '/good/delete',
        method: 'post',
        data: { goodId }
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