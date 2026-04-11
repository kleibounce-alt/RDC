import request from '@/utils/request'

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

export function register(data) {
    return request({
        url: '/user/register',
        method: 'post',
        data: {
            userName: data.userName,
            password: data.password,
            confirmPassword: data.confirmPassword,
            role: 0
        }
    })
}