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

// 获取我购买的订单（买家视角）
export function getMyPurchases() {
    return request({
        url: '/order/purchases',
        method: 'get'
    })
}

// 获取我出售的订单（卖家视角）
export function getMySales() {
    return request({
        url: '/order/sales',
        method: 'get'
    })
}

// 统一获取订单（根据类型）
export function getMyOrders(type = 'buy') {
    // type: 'buy' 购买, 'sell' 出售
    if (type === 'buy') {
        return getMyPurchases()
    } else {
        return getMySales()
    }
}

// 确认订单（卖家确认收款，交易完成）
export function confirmOrder(orderId) {
    return request({
        url: '/order/confirm',
        method: 'post',
        data: {
            orderId: orderId
        }
    })
}

// 取消订单（买家或卖家取消，退款给买家）
export function cancelOrder(orderId) {
    return request({
        url: '/order/cancel',
        method: 'post',
        data: {
            orderId: orderId
        }
    })
}