import axios from 'axios'

const request = axios.create({
    baseURL: '/api',  // 所有请求加 /api 前缀，被上面 rewrite 去掉后转发到后端
    timeout: 10000,
    withCredentials: true
})

request.interceptors.response.use(
    response => {
        return response.data
    },
    error => {
        if (error.response && error.response.status === 401) {
            console.error('登录已过期')
        }
        return Promise.reject(error)
    }
)

export default request