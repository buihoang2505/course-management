import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
    { path: '/',            redirect: '/courses' },
    { path: '/login',       component: () => import('../views/LoginView.vue'),       meta: { guest: true } },
    { path: '/register',    component: () => import('../views/RegisterView.vue'),    meta: { guest: true } },
    { path: '/courses',     component: () => import('../views/CoursesView.vue'),     meta: { requiresAuth: true } },
    { path: '/courses/:id', component: () => import('../views/CourseDetailView.vue'),meta: { requiresAuth: true } },
    { path: '/my-courses',  component: () => import('../views/MyCoursesView.vue'),   meta: { requiresAuth: true } },
    { path: '/grades',      component: () => import('../views/GradesView.vue'),      meta: { requiresAuth: true } },
    { path: '/profile',     component: () => import('../views/ProfileView.vue'),     meta: { requiresAuth: true } },
    { path: '/admin',       component: () => import('../views/AdminView.vue'),       meta: { requiresAuth: true, adminOnly: true } },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const auth = useAuthStore()
    if (to.meta.requiresAuth && !auth.isLoggedIn) next('/login')
    else if (to.meta.guest && auth.isLoggedIn) next('/courses')
    else if (to.meta.adminOnly && !auth.isAdmin) next('/courses')
    else next()
})

export default router