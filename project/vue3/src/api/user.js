import request from '@/utils/request'

// 用户登录
export function login(data) {
    return request({
        url: '/user/login',
        method: 'post',
        data: {
            userName: data.userName,
            password: data.password
        }
    })
}

// 用户注册
export function register(data) {
    return request({
        url: '/user/register',
        method: 'post',
        data: {
            userName: data.userName,
            password: data.password,
            confirmPassword: data.confirmPassword,
            role: data.role
        }
    })
}

// 更新头像
export function updateAvatar(avatarUrl) {
    return request({
        url: '/user/updateAvatar',
        method: 'post',
        data: { avatar: avatarUrl }
    })
}

// 获取个人信息（UserCenter.vue 第292行需要的）
export function getUserProfile() {
    return request({
        url: '/user/profile',
        method: 'get'
    })
}

// 可选：获取其他用户公开信息
export function getUserInfo(userId) {
    return request({
        url: '/user/info',
        method: 'get',
        params: { userId }
    })
}