// src/stores/cart.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'

export const useCartStore = defineStore('cart', () => {
    // Key riêng theo userId — tránh user A thừa hưởng cart của user B
    function cartKey() {
        try {
            const auth = useAuthStore()
            return auth.user?.id ? `cart_${auth.user.id}` : 'cart_guest'
        } catch {
            return 'cart_guest'
        }
    }

    const items = ref(JSON.parse(localStorage.getItem(cartKey()) || '[]'))

    function save() {
        localStorage.setItem(cartKey(), JSON.stringify(items.value))
    }

    // Reload items từ localStorage theo user hiện tại (gọi sau login)
    function reload() {
        items.value = JSON.parse(localStorage.getItem(cartKey()) || '[]')
    }

    function add(course) {
        if (!items.value.find(c => Number(c.id) === Number(course.id))) {
            items.value.push({
                id:          course.id,
                title:       course.title,
                instructor:  course.instructor,
                price:       course.price,
                isFree:      course.isFree,
                credits:     course.credits,
            })
            save()
        }
    }

    function remove(courseId) {
        // Ép kiểu Number để tránh '3' !== 3
        items.value = items.value.filter(c => Number(c.id) !== Number(courseId))
        save()
    }

    function clear() {
        items.value = []
        save()
    }

    function has(courseId) {
        return !!items.value.find(c => Number(c.id) === Number(courseId))
    }

    const count = computed(() => items.value.length)
    const total = computed(() => items.value.reduce((s, c) => s + (c.price || 0), 0))

    return { items, add, remove, clear, has, reload, count, total }
})