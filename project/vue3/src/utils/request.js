import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api',  // 对应 vite.config.js 里的代理配置
    timeout: 10000    // 请求超时时间 10 秒
})

// 请求拦截器（在发送请求前做一些处理）
request.interceptors.request.use(
    config => {
        // 从 localStorage 获取 token，如果有就带上
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器（对返回的数据做统一处理）
request.interceptors.response.use(
    response => {
        // 直接返回后端的数据体
        return response.data
    },
    error => {
        // 统一处理错误，比如 401 未登录
        if (error.response && error.response.status === 401) {
            // 未登录，可以在这里跳转登录页或提示
            console.error('请先登录')
            // 可选：localStorage.removeItem('token')
            // 可选：window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export default request