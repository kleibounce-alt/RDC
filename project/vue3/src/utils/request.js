import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000,
    withCredentials: true  // 关键：携带 Cookie，支持 Session
})

request.interceptors.response.use(
    response => {
        return response.data
    },
    error => {
        if (error.response && error.response.status === 401) {
            console.error('登录已过期，请重新登录')
        }
        return Promise.reject(error)
    }
)

export default request