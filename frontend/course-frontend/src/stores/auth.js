import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../services/api'

export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token') || null)
    const user  = ref(JSON.parse(localStorage.getItem('user') || 'null'))

    const isLoggedIn    = computed(() => !!token.value)
    const isAdmin       = computed(() => user.value?.role === 'ADMIN')
    const isInstructor  = computed(() => user.value?.role === 'INSTRUCTOR')
    const canManage     = computed(() => user.value?.role === 'ADMIN' || user.value?.role === 'INSTRUCTOR')

    function login(data) {
        token.value = data.token
        user.value  = {
            id:       data.id,
            email:    data.email,
            username: data.username,
            role:     data.role,
            avatar:   data.avatar || ''
        }
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        // Fetch avatar sau login
        fetchAvatar(data.id)
    }

    function setAvatar(url) {
        if (user.value) {
            user.value = { ...user.value, avatar: url }
            localStorage.setItem('user', JSON.stringify(user.value))
        }
    }

    async function fetchAvatar(userId) {
        try {
            const res = await api.get(`/users/${userId}/profile`)
            if (res.data?.avatar || res.data?.fullName !== undefined) {
                if (user.value) {
                    user.value = {
                        ...user.value,
                        avatar:   res.data.avatar   || user.value.avatar || '',
                        fullName: res.data.fullName || user.value.fullName || ''
                    }
                    localStorage.setItem('user', JSON.stringify(user.value))
                }
            }
        } catch { /* ignore */ }
    }

    function setFullName(name) {
        if (user.value) {
            user.value = { ...user.value, fullName: name }
            localStorage.setItem('user', JSON.stringify(user.value))
        }
    }

    async function register(username, email, password) {
        const res = await api.post('/auth/register', { username, email, password })
        token.value = res.data.token
        user.value  = {
            id:       res.data.id,
            email:    res.data.email,
            username: res.data.username,
            role:     res.data.role
        }
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        return res.data
    }

    function logout() {
        token.value = null
        user.value  = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // Xóa cart khi logout — tránh user khác thừa hưởng giỏ hàng
        localStorage.removeItem('cart')
    }

    function refreshAvatar() {
        if (user.value?.id) fetchAvatar(user.value.id)
    }

    return { token, user, isLoggedIn, isAdmin, isInstructor, canManage, login, register, logout, setAvatar, setFullName, refreshAvatar }
})