import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:8080/api'
    // Không set Content-Type ở đây
    // JSON request: axios tự set application/json
    // FormData upload: axios tự set multipart/form-data + boundary
})

// ===== JWT INTERCEPTOR =====
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    // Chỉ set JSON khi body không phải FormData
    if (!(config.data instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json'
    }
    // Nếu là FormData: xóa Content-Type để axios tự set multipart + boundary
    else {
        delete config.headers['Content-Type']
    }
    return config
})

api.interceptors.response.use(
    response => response,
    error => {
        // Chỉ logout khi 401 (token hết hạn/không hợp lệ)
        // Không logout khi 403 (có thể là thiếu quyền với 1 endpoint cụ thể)
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)
export default api