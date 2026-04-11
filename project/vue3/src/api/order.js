import request from '@/utils/request'

// 创建订单（购买商品）
export function createOrder(goodId) {
    return request({
        url: '/order/create',
        method: 'post',
        data: {
            goodId: goodId
        }
    })
}