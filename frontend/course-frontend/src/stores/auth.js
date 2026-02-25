import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../services/api'

export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token') || null)
    const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.role === 'ADMIN')

    async function login(email, password) {
        const res = await api.post('/auth/login', { email, password })
        token.value = res.data.token
        user.value = {
            id: res.data.id,
            email: res.data.email,
            username: res.data.username,
            role: res.data.role
        }
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        return res.data
    }

    async function register(username, email, password) {
        const res = await api.post('/auth/register', { username, email, password })
        token.value = res.data.token
        user.value = {
            id: res.data.id,
            email: res.data.email,
            username: res.data.username,
            role: res.data.role
        }
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        return res.data
    }

    function logout() {
        token.value = null
        user.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    return { token, user, isLoggedIn, isAdmin, login, register, logout }
})