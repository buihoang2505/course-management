import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
    { path: '/',            component: () => import('../views/LandingView.vue'),   meta: { landing: true } },

    { path: '/login',       component: () => import('../views/LoginView.vue'),       meta: { guest: true } },
    { path: '/register',    component: () => import('../views/RegisterView.vue'),    meta: { guest: true } },

    { path: '/courses',     component: () => import('../views/CoursesView.vue'),     meta: { requiresAuth: true } },
    { path: '/courses/:id', component: () => import('../views/CourseDetailView.vue'),meta: { requiresAuth: true } },
    { path: '/my-courses',  component: () => import('../views/MyCoursesView.vue'),   meta: { requiresAuth: true } },
    { path: '/grades',      component: () => import('../views/GradesView.vue'),      meta: { requiresAuth: true } },
    { path: '/profile',     component: () => import('../views/ProfileView.vue'),     meta: { requiresAuth: true } },

    { path: '/admin',       component: () => import('../views/AdminView.vue'),       meta: { requiresAuth: true, adminOnly: true } },
    { path: '/instructor',       component: () => import('../views/InstructorView.vue'),       meta: { requiresAuth: true, instructorOnly: true } },
    { path: '/apply-instructor', component: () => import('../views/ApplyInstructorView.vue'), meta: { requiresAuth: true, studentOnly: true } },

    { path: '/certificates',    component: () => import('../views/CertificatesView.vue'),    meta: { requiresAuth: true } },
    { path: '/leaderboard',     component: () => import('../views/LeaderboardView.vue'),     meta: { requiresAuth: true } },
    { path: '/verify/:code',    component: () => import('../views/CertificateVerifyView.vue'), meta: { public: true } },

    // 404 — phải là route CUỐI CÙNG
    { path: '/checkout/success', component: () => import('../views/CheckoutSuccessView.vue'), meta: { requiresAuth: true } },
    { path: '/payment/result',  component: () => import('../views/PaymentResultView.vue'),  meta: { public: true } },
    { path: '/payment/confirm', component: () => import('../views/PaymentConfirmView.vue'), meta: { requiresAuth: true } },
    { path: '/cart',            component: () => import('../views/CartView.vue'),            meta: { requiresAuth: true } },
    { path: '/wishlist',        component: () => import('../views/WishlistView.vue'),        meta: { requiresAuth: true } },
    { path: '/:pathMatch(.*)*',  component: () => import('../views/NotFoundView.vue'),          meta: { public: true } },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const auth = useAuthStore()

    if (to.meta.requiresAuth && !auth.isLoggedIn) {
        next('/login')
    }
    else if (to.meta.guest && auth.isLoggedIn) {
        next('/courses')
    }
    else if (to.meta.adminOnly && !auth.isAdmin) {
        next('/courses')
    }
    else if (to.meta.instructorOnly && auth.user?.role !== 'INSTRUCTOR' && !auth.isAdmin) {
        next('/courses')
    }
    else if (to.meta.studentOnly) {
        if (auth.user?.role === 'INSTRUCTOR') return next('/instructor')
        if (auth.isAdmin) return next('/admin')
        next()
    }
    else {
        next()
    }
})

export default router