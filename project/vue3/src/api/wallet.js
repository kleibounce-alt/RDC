import request from '@/utils/request'

// 查询钱包余额
export function getWalletBalance() {
    return request({
        url: '/wallet/balance',
        method: 'get'
    })
}

export function rechargeWallet(amount) {
    return request({
        url: '/wallet/recharge',
        method: 'post',
        data: { amount }
    })
}