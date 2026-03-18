<template>
  <div class="wl-page">

    <!-- Hero header -->
    <div class="wl-hero">
      <div class="hero-bg-orb orb1"></div>
      <div class="hero-bg-orb orb2"></div>
      <div class="hero-content">
        <div class="hero-icon-wrap">
          <span class="hero-icon">♥</span>
          <div class="hero-icon-ring"></div>
        </div>
        <div>
          <h1 class="hero-title">Danh sách yêu thích</h1>
          <p class="hero-sub">
            <span class="hero-count">{{ wishCourses.length }}</span>
            khóa học đang chờ bạn
          </p>
        </div>
      </div>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="wl-grid">
      <div v-for="i in 3" :key="i" class="wl-card sk-card">
        <div class="sk-shimmer sk-img"></div>
        <div class="sk-body">
          <div class="sk-shimmer sk-line w80"></div>
          <div class="sk-shimmer sk-line w60"></div>
          <div class="sk-shimmer sk-line w40"></div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="!wishCourses.length" class="empty-wrap">
      <div class="empty-card">
        <div class="empty-hearts">
          <span class="eh eh1">🤍</span>
          <span class="eh eh2">🤍</span>
          <span class="eh eh3">🤍</span>
        </div>
        <h3 class="empty-title">Chưa có khóa học nào</h3>
        <p class="empty-sub">Hover vào khóa học và nhấn 🤍 để lưu lại đây</p>
        <router-link to="/courses" class="empty-btn">
          <span>Khám phá ngay</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </router-link>
      </div>
    </div>

    <!-- Cards grid -->
    <div v-else class="wl-grid">
      <div
          v-for="(course, idx) in wishCourses"
          :key="course.id"
          class="wl-card"
          :style="{ '--delay': idx * 0.07 + 's' }"
      >
        <!-- Gradient accent bar -->
        <div class="card-accent-bar" :style="{ background: gradients[idx % gradients.length] }"></div>



        <!-- Top: icon + badges -->
        <div class="card-top">
          <div class="card-icon-wrap" :style="{ background: iconBgs[idx % iconBgs.length] }">
            <span class="card-icon">{{ icons[idx % icons.length] }}</span>
          </div>
          <div class="card-badges">
            <span class="badge-tc">{{ course.credits }} TC</span>
            <span :class="['badge-status', course.status === 'ACTIVE' ? 'active' : 'inactive']">
              {{ course.status === 'ACTIVE' ? 'Active' : course.status }}
            </span>
          </div>
        </div>

        <!-- Content -->
        <div class="card-body">
          <h3 class="card-title">{{ course.title }}</h3>
          <p class="card-desc">{{ course.description || 'Khóa học chất lượng cao từ EduFlow.' }}</p>
        </div>

        <!-- Price -->
        <div class="card-price-row">
          <span v-if="course.price > 0" class="price-tag">
            {{ formatPrice(course.price) }}
          </span>
          <span v-else class="price-free">Miễn phí</span>
        </div>

        <!-- Instructor + stats -->
        <div class="card-meta">
          <div class="meta-inst">
            <div class="inst-avatar">{{ (course.instructor || '?')[0].toUpperCase() }}</div>
            <span>{{ course.instructor || 'Chưa xác định' }}</span>
          </div>
          <div class="meta-stats">
            <span class="stat">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" style="color:#f59e0b"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
              {{ course.avgRating ?? '—' }}
            </span>
            <span class="stat-dot"></span>
            <span class="stat">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              {{ course.enrollCount || 0 }}
            </span>
          </div>
        </div>

        <!-- Footer actions -->
        <div class="card-footer">
          <button class="btn-remove-wish" @click="removeWish(course.id)" title="Xóa khỏi yêu thích">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.593c-5.63-5.539-11-10.297-11-14.402 0-3.791 3.068-5.191 5.281-5.191 1.312 0 4.151.501 5.719 4.457 1.59-3.968 4.464-4.447 5.726-4.447 2.54 0 5.274 1.621 5.274 5.181 0 4.069-5.136 8.625-11 14.402z"/></svg>
          </button>
          <router-link :to="`/courses/${course.id}`" class="btn-detail">Chi tiết</router-link>
          <template v-if="!auth.isAdmin && course.status === 'ACTIVE'">
            <button v-if="isEnrolled(course.id)" class="btn-enrolled" disabled>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              Đã đăng ký
            </button>
            <button
                v-else
                @click="enrollCourse(course)"
                class="btn-enroll"
                :disabled="enrolling === course.id"
                :style="{ background: gradients[idx % gradients.length] }"
            >
              <span v-if="enrolling === course.id" class="btn-spinner"></span>
              <span v-else>{{ course.price > 0 ? '💳 ' + formatPrice(course.price) : '🚀 Đăng ký' }}</span>
            </button>
          </template>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <Transition name="toast">
      <div v-if="toast" :class="['toast', toast.type]">
        <span class="toast-icon">{{ toast.type === 'success' ? '✓' : '✕' }}</span>
        {{ toast.msg }}
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth     = useAuthStore()
const router   = useRouter()
const loading  = ref(false)
const allCourses   = ref([])
const enrolledIds  = ref([])
const enrolling    = ref(null)
const toast        = ref(null)

const icons    = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']
const iconBgs  = ['#eff6ff','#fdf4ff','#f0fdf4','#fff7ed','#fef2f2','#f0f9ff']
const gradients = [
  'linear-gradient(135deg,#667eea,#764ba2)',
  'linear-gradient(135deg,#f093fb,#f5576c)',
  'linear-gradient(135deg,#4facfe,#00f2fe)',
  'linear-gradient(135deg,#43e97b,#38f9d7)',
  'linear-gradient(135deg,#fa709a,#fee140)',
  'linear-gradient(135deg,#a18cd1,#fbc2eb)',
]

const _wlRaw = JSON.parse(localStorage.getItem('wl') || '[]').map(Number).filter(Boolean)
const wishSet = ref(new Set(_wlRaw))

const wishCourses = computed(() =>
    allCourses.value.filter(c => wishSet.value.has(Number(c.id)))
)

function showToast(msg, type='success') {
  toast.value = { msg, type }
  setTimeout(() => toast.value = null, 3000)
}

function formatPrice(price) {
  if (!price) return 'Miễn phí'
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(price)
}

function isEnrolled(id) {
  return enrolledIds.value.includes(Number(id))
}

function removeWish(id) {
  wishSet.value.delete(Number(id))
  localStorage.setItem('wl', JSON.stringify([...wishSet.value]))
  window.dispatchEvent(new Event('localStorageUpdated'))
  showToast('Đã xóa khỏi yêu thích')
}

async function enrollCourse(course) {
  enrolling.value = course.id
  try {
    // FIX: dùng price làm nguồn sự thật
    if (course.price > 0) {
      const r = await api.post(`/payment/create?courseId=${course.id}`)
      router.push({ path: '/payment/confirm', query: {
          txnRef: r.data.txnRef, amount: r.data.amount,
          courseId: r.data.courseId, courseTitle: r.data.courseTitle
        }})
      return
    }
    await api.post(`/enrollments/enroll?courseId=${course.id}`)
    enrolledIds.value.push(Number(course.id))
    showToast('🎉 Đăng ký thành công!')
  } catch(e) {
    showToast(e.response?.data?.error || 'Đăng ký thất bại!', 'error')
  } finally { enrolling.value = null }
}

onMounted(async () => {
  loading.value = true
  try {
    const url = auth.isAdmin ? '/courses' : '/courses/active'
    const [cr, er] = await Promise.all([
      api.get(url),
      auth.user?.id ? api.get(`/enrollments/user/${auth.user.id}`) : Promise.resolve({ data: [] })
    ])
    allCourses.value  = Array.isArray(cr.data) ? cr.data : []
    enrolledIds.value = er.data.map(e => Number(e.course?.id)).filter(Boolean)
  } catch(e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<style scoped>
/* ── Page ── */
.wl-page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 4rem;
}

/* ── Hero ── */
.wl-hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #1e3a5f 100%);
  padding: 3rem 2.5rem 2.5rem;
  margin-bottom: 2.5rem;
}
.hero-bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.25;
  pointer-events: none;
}
.orb1 {
  width: 320px; height: 320px;
  background: #818cf8;
  top: -100px; right: -60px;
}
.orb2 {
  width: 200px; height: 200px;
  background: #f472b6;
  bottom: -80px; left: 20%;
}
.hero-content {
  position: relative;
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 1.25rem;
}
.hero-icon-wrap {
  position: relative;
  flex-shrink: 0;
}
.hero-icon {
  font-size: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px; height: 64px;
  background: linear-gradient(135deg, #f43f5e, #ec4899);
  border-radius: 18px;
  color: #fff;
  box-shadow: 0 8px 32px rgba(244, 63, 94, 0.4);
  animation: heartPulse 2.5s ease-in-out infinite;
}
@keyframes heartPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.08); }
}
.hero-icon-ring {
  position: absolute;
  inset: -6px;
  border-radius: 24px;
  border: 2px solid rgba(244, 63, 94, 0.3);
  animation: ringPulse 2.5s ease-in-out infinite;
}
@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.12); opacity: 0.15; }
}
.hero-title {
  font-size: 1.75rem;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.02em;
  margin-bottom: .3rem;
}
.hero-sub {
  font-size: .95rem;
  color: rgba(255,255,255,0.6);
  display: flex;
  align-items: center;
  gap: .4rem;
}
.hero-count {
  font-size: 1.1rem;
  font-weight: 800;
  color: #f472b6;
}

/* ── Grid ── */
.wl-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.4rem;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 2.5rem;
}
@media (max-width: 1024px) { .wl-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px)  { .wl-grid { grid-template-columns: 1fr; padding: 0 1rem; } }

/* ── Card ── */
.wl-card {
  background: var(--surface);
  border-radius: 20px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  border: 1px solid var(--border);
  transition: transform .25s cubic-bezier(.34,1.56,.64,1), box-shadow .25s ease;
  animation: cardIn .45s cubic-bezier(.34,1.56,.64,1) both;
  animation-delay: var(--delay, 0s);
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(24px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}
.wl-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 0 20px 60px rgba(0,0,0,.12);
  border-color: transparent;
}

/* Accent bar */
.card-accent-bar {
  height: 4px;
  width: 100%;
}

/* Remove button */
.btn-remove-wish {
  width: 36px; height: 36px;
  border-radius: 10px;
  background: #fff0f3;
  border: 1.5px solid #fecdd3;
  color: #f43f5e;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all .2s;
  flex-shrink: 0;
}
.btn-remove-wish:hover {
  background: #f43f5e;
  color: #fff;
  border-color: #f43f5e;
  transform: scale(1.08);
}

/* Card content */
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.2rem 1.2rem .5rem;
}
.card-icon-wrap {
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
}
.card-icon { font-size: 1.75rem; line-height: 1; }
.card-badges { display: flex; flex-direction: column; align-items: flex-end; gap: .35rem; }
.badge-tc {
  font-size: .65rem; font-weight: 800;
  background: var(--accent-light); color: var(--accent);
  padding: .2rem .5rem; border-radius: 100px;
  letter-spacing: .03em;
}
.badge-status {
  font-size: .62rem; font-weight: 700;
  padding: .18rem .5rem; border-radius: 100px;
}
.badge-status.active  { background: #d1fae5; color: #059669; }
.badge-status.inactive { background: #f1f5f9; color: #94a3b8; }

.card-body { padding: .5rem 1.2rem; flex: 1; }
.card-title {
  font-size: 1rem; font-weight: 800;
  color: var(--text);
  line-height: 1.35;
  margin-bottom: .4rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-desc {
  font-size: .78rem;
  color: var(--muted);
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-price-row {
  padding: .6rem 1.2rem;
}
.price-tag {
  font-size: 1.1rem;
  font-weight: 800;
  color: var(--accent);
  letter-spacing: -0.01em;
}
.price-free {
  font-size: .9rem;
  font-weight: 700;
  color: #059669;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: .5rem 1.2rem;
  border-top: 1px solid var(--border);
  margin: 0 0 .2rem;
}
.meta-inst {
  display: flex; align-items: center; gap: .5rem;
  font-size: .75rem; color: var(--muted);
}
.inst-avatar {
  width: 22px; height: 22px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: .65rem; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.meta-stats {
  display: flex; align-items: center; gap: .5rem;
  font-size: .74rem; color: var(--muted);
}
.stat { display: flex; align-items: center; gap: .25rem; }
.stat-dot {
  width: 3px; height: 3px;
  border-radius: 50%;
  background: var(--border);
}

/* Footer */
.card-footer {
  display: flex;
  gap: .6rem;
  padding: .9rem 1.2rem 1.2rem;
}
.btn-detail {
  flex: 1;
  display: flex; align-items: center; justify-content: center;
  padding: .55rem .9rem;
  border-radius: 10px;
  background: var(--surface2);
  border: 1.5px solid var(--border);
  font-size: .8rem; font-weight: 700;
  color: var(--text2);
  text-decoration: none;
  transition: all .18s;
}
.btn-detail:hover { border-color: var(--accent); color: var(--accent); }

.btn-enroll {
  flex: 2;
  display: flex; align-items: center; justify-content: center; gap: .4rem;
  padding: .55rem .9rem;
  border-radius: 10px;
  border: none;
  color: #fff;
  font-size: .8rem; font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: opacity .18s, transform .18s;
  white-space: nowrap;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,.2);
}
.btn-enroll:hover:not(:disabled) { opacity: .88; transform: translateY(-1px); }
.btn-enroll:disabled { opacity: .6; cursor: not-allowed; }

.btn-enrolled {
  flex: 2;
  display: flex; align-items: center; justify-content: center; gap: .35rem;
  padding: .55rem .9rem;
  border-radius: 10px;
  background: #d1fae5;
  border: 1.5px solid #6ee7b7;
  color: #059669;
  font-size: .8rem; font-weight: 700;
  font-family: inherit;
  cursor: default;
}

.btn-spinner {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Empty state ── */
.empty-wrap {
  display: flex;
  justify-content: center;
  padding: 4rem 2rem;
}
.empty-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 24px;
  padding: 3.5rem 3rem;
  text-align: center;
  max-width: 420px;
  width: 100%;
}
.empty-hearts {
  display: flex;
  justify-content: center;
  gap: .5rem;
  margin-bottom: 1.5rem;
}
.eh { font-size: 2rem; animation: floatHeart 3s ease-in-out infinite; }
.eh1 { animation-delay: 0s; opacity: .3; }
.eh2 { animation-delay: .5s; opacity: .6; font-size: 2.6rem; }
.eh3 { animation-delay: 1s; opacity: .3; }
@keyframes floatHeart {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
.empty-title {
  font-size: 1.15rem; font-weight: 800;
  color: var(--text); margin-bottom: .5rem;
}
.empty-sub {
  font-size: .85rem; color: var(--muted);
  line-height: 1.6; margin-bottom: 1.5rem;
}
.empty-btn {
  display: inline-flex; align-items: center; gap: .5rem;
  padding: .7rem 1.6rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 12px;
  font-size: .88rem; font-weight: 700;
  text-decoration: none;
  transition: opacity .18s, transform .18s;
  box-shadow: 0 4px 20px rgba(102,126,234,.4);
}
.empty-btn:hover { opacity: .88; transform: translateY(-2px); }

/* ── Skeleton ── */
.sk-card { pointer-events: none; }
.sk-img {
  height: 120px;
  border-radius: 0;
}
.sk-body { padding: 1.2rem; display: flex; flex-direction: column; gap: .75rem; }
.sk-line { height: 14px; border-radius: 7px; }
.w80 { width: 80%; }
.w60 { width: 60%; }
.w40 { width: 40%; }
.sk-shimmer {
  background: linear-gradient(90deg, #e2e8f0 25%, #f8fafc 50%, #e2e8f0 75%);
  background-size: 300% 100%;
  animation: shimmer 1.6s infinite;
  border-radius: 10px;
}
@keyframes shimmer { to { background-position: -300% 0; } }

/* ── Toast ── */
.toast {
  position: fixed; bottom: 2rem; right: 2rem;
  display: flex; align-items: center; gap: .6rem;
  padding: .75rem 1.2rem;
  border-radius: 12px;
  font-size: .84rem; font-weight: 600;
  z-index: 9999;
  box-shadow: 0 8px 30px rgba(0,0,0,.12);
}
.toast.success { background: #fff; color: #059669; border: 1.5px solid #6ee7b7; }
.toast.error   { background: #fff; color: #dc2626; border: 1.5px solid #fca5a5; }
.toast-icon {
  width: 20px; height: 20px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: .72rem; font-weight: 800; color: #fff;
}
.toast.success .toast-icon { background: #059669; }
.toast.error   .toast-icon { background: #dc2626; }

.toast-enter-active { transition: all .3s cubic-bezier(.34,1.56,.64,1); }
.toast-leave-active { transition: all .2s ease; }
.toast-enter-from   { transform: translateX(110%); opacity: 0; }
.toast-leave-to     { transform: translateX(110%); opacity: 0; }
</style>