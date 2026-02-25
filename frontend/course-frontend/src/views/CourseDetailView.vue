<template>
  <div class="page">
    <div v-if="loading" class="loading">⏳ Đang tải...</div>
    <div v-else-if="course">

      <button @click="$router.back()" class="btn-back">
        <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/></svg>
        Quay lại
      </button>

      <!-- Hero -->
      <div class="hero-card">
        <div class="hero-left">
          <div class="hero-icon">{{ courseIcon }}</div>
          <div class="hero-info">
            <div class="hero-badges">
              <span class="badge badge-blue">{{ course.credits }} tín chỉ</span>
              <span :class="['badge', course.status==='ACTIVE'?'badge-green':'badge-gray']">{{ course.status }}</span>
            </div>
            <h1>{{ course.title }}</h1>
            <p class="hero-desc">{{ course.description }}</p>
            <div class="hero-inst"><span class="inst-dot"></span>{{ course.instructor || 'Chưa xác định' }}</div>
          </div>
        </div>
        <div class="hero-right">
          <div v-if="isEnrolled" class="enrolled-box">
            <div class="enrolled-check">✓</div>
            <div>
              <div class="enrolled-title">Đã đăng ký</div>
              <div class="enrolled-sub">Bạn có thể xem tất cả bài học</div>
            </div>
          </div>
          <button v-else @click="enroll" class="btn btn-accent enroll-btn" :disabled="enrolling">
            {{ enrolling ? 'Đang đăng ký...' : '🚀 Đăng ký ngay' }}
          </button>
        </div>
      </div>

      <!-- Progress (nếu đã đăng ký) -->
      <div v-if="isEnrolled && lessons.length" class="progress-card">
        <div class="prog-lbl">
          <span>Tiến độ học tập</span>
          <span class="prog-pct">0/{{ lessons.length }} bài</span>
        </div>
        <div class="prog-track"><div class="prog-fill" style="width:0%"></div></div>
      </div>

      <!-- Lesson accordion -->
      <div class="lessons-card">
        <div class="lessons-header">
          <h2>📖 Nội dung khóa học</h2>
          <span class="lcount">{{ lessons.length }} bài học</span>
        </div>

        <div v-if="!lessons.length" class="empty-state">
          <div class="empty-icon">📝</div><p>Chưa có bài học nào.</p>
        </div>

        <div v-else class="accordion">
          <div v-for="l in lessons" :key="l.id"
               :class="['acc-item', { locked: !isEnrolled, open: expandedId===l.id }]">

            <!-- Header – chiều cao cố định -->
            <div class="acc-header" @click="isEnrolled && toggle(l.id)">
              <div class="acc-num">{{ l.orderNum }}</div>
              <div class="acc-title">{{ l.title }}</div>
              <div class="acc-right">
                <span v-if="!isEnrolled" class="lock-icon">🔒</span>
                <svg v-else :class="['chevron', { rotated: expandedId===l.id }]"
                     width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </div>
            </div>

            <!-- Body – dùng max-height transition để không thay đổi kích thước đột ngột -->
            <div class="acc-body" :class="{ expanded: expandedId===l.id }">
              <div class="acc-inner">
                <p>{{ l.content || 'Không có nội dung.' }}</p>
                <a v-if="l.videoUrl" :href="l.videoUrl" target="_blank" class="video-link">
                  🎥 Xem video bài học
                </a>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>

    <div v-if="toast" :class="['toast', toast.type]">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth  = useAuthStore()
const course = ref(null)
const lessons = ref([])
const loading = ref(true)
const enrolling = ref(false)
const isEnrolled = ref(false)
const expandedId = ref(null)
const toast = ref(null)

const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']
const courseIcon = computed(() => icons[route.params.id % icons.length])

function showToast(m,t='success'){ toast.value={msg:m,type:t}; setTimeout(()=>toast.value=null,3000) }
function toggle(id){ expandedId.value = expandedId.value===id ? null : id }

async function fetchCourse() {
  loading.value = true
  try {
    const courseRes = await api.get(`/courses/${route.params.id}`)
    course.value = courseRes.data

    const lessonRes = await api.get(`/lessons/course/${route.params.id}`)
    lessons.value = lessonRes.data || []

  } catch(e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function checkEnrolled() {
  try {
    const r = await api.get(`/enrollments/user/${auth.user?.id}`)
    isEnrolled.value = r.data.some(e => e.course?.id == route.params.id)
  } catch {}
}

async function enroll() {
  enrolling.value = true
  try {
    await api.post(`/enrollments/enroll?userId=${auth.user?.id}&courseId=${route.params.id}`)
    isEnrolled.value = true
    showToast('🎉 Đăng ký thành công! Bây giờ bạn có thể xem bài học.')
  } catch(e) {
    showToast(e.response?.data?.error || 'Đăng ký thất bại!', 'error')
  } finally { enrolling.value = false }
}

onMounted(async () => { await fetchCourse(); await checkEnrolled() })
</script>

<style scoped>
.page { padding: 2rem 2.5rem; max-width: 1000px; margin: 0 auto; }

.btn-back { display:inline-flex; align-items:center; gap:.4rem; background:transparent; border:none; color:var(--muted); cursor:pointer; font-size:.85rem; margin-bottom:1.3rem; padding:0; transition:color .15s; font-family:'Plus Jakarta Sans',sans-serif; }
.btn-back:hover { color:var(--text); }

/* Hero */
.hero-card { background:var(--surface); border:1.5px solid var(--border); border-radius:16px; padding:1.8rem; display:flex; justify-content:space-between; align-items:flex-start; gap:1.5rem; margin-bottom:.9rem; box-shadow:var(--shadow-sm); flex-wrap:wrap; }
.hero-left  { display:flex; gap:1.2rem; flex:1; align-items:flex-start; }
.hero-icon  { font-size:2.8rem; flex-shrink:0; line-height:1; }
.hero-badges { display:flex; gap:.5rem; margin-bottom:.5rem; flex-wrap:wrap; }
.hero-info h1 { font-size:1.35rem; font-weight:800; margin-bottom:.4rem; line-height:1.3; }
.hero-desc  { font-size:.84rem; color:var(--muted); margin-bottom:.5rem; line-height:1.6; }
.hero-inst  { display:flex; align-items:center; gap:.5rem; font-size:.8rem; color:var(--muted); }
.inst-dot   { width:7px; height:7px; border-radius:50%; background:var(--green); flex-shrink:0; }

.hero-right { flex-shrink:0; }
.enrolled-box { display:flex; align-items:center; gap:.8rem; background:var(--green-light); border:1.5px solid #a7f3d0; border-radius:10px; padding:.85rem 1.1rem; }
.enrolled-check { width:28px; height:28px; border-radius:50%; background:var(--green); color:#fff; display:flex; align-items:center; justify-content:center; font-weight:700; font-size:.85rem; flex-shrink:0; }
.enrolled-title { font-size:.85rem; font-weight:700; color:var(--green); }
.enrolled-sub   { font-size:.74rem; color:var(--muted); margin-top:.1rem; }
.enroll-btn { padding:.7rem 1.6rem; font-size:.88rem; }

/* Progress */
.progress-card { background:var(--surface); border:1.5px solid var(--border); border-radius:12px; padding:1.1rem 1.4rem; margin-bottom:.9rem; box-shadow:var(--shadow-sm); }
.prog-lbl  { display:flex; justify-content:space-between; font-size:.8rem; margin-bottom:.5rem; }
.prog-pct  { color:var(--accent); font-weight:600; }
.prog-track { height:7px; background:var(--surface2); border:1px solid var(--border); border-radius:100px; overflow:hidden; }
.prog-fill  { height:100%; background:linear-gradient(90deg,var(--accent),#60a5fa); border-radius:100px; transition:width .7s; }

/* Lessons accordion */
.lessons-card { background:var(--surface); border:1.5px solid var(--border); border-radius:16px; overflow:hidden; box-shadow:var(--shadow-sm); }
.lessons-header { display:flex; align-items:center; gap:1rem; padding:1.2rem 1.5rem; border-bottom:1.5px solid var(--border); }
.lessons-header h2 { font-size:1rem; font-weight:700; }
.lcount { font-size:.73rem; color:var(--muted); background:var(--surface2); border:1px solid var(--border); padding:.15rem .6rem; border-radius:100px; }

.accordion { display:flex; flex-direction:column; }

/* Item – header KHÔNG thay đổi kích thước */
.acc-item { border-bottom: 1px solid var(--border); }
.acc-item:last-child { border-bottom: none; }
.acc-item.locked { opacity: .7; }
.acc-item.open .acc-header { background: var(--accent-light); border-left: 3px solid var(--accent); }

.acc-header {
  display: flex;
  align-items: center;
  gap: .9rem;
  padding: .9rem 1.4rem;
  cursor: pointer;
  transition: background .15s;
  min-height: 52px; /* cố định chiều cao header */
  user-select: none;
}
.acc-item.locked .acc-header { cursor: default; }
.acc-header:hover:not(.locked) { background: var(--surface2); }

.acc-num   { width:26px; height:26px; border-radius:7px; background:var(--surface2); border:1px solid var(--border); display:flex; align-items:center; justify-content:center; font-size:.76rem; font-weight:700; color:var(--accent); flex-shrink:0; }
.acc-title { flex:1; font-size:.88rem; font-weight:600; }
.acc-right { flex-shrink:0; display:flex; align-items:center; }
.lock-icon { font-size:.8rem; color:var(--muted); }
.chevron   { color:var(--muted); transition:transform .25s; }
.chevron.rotated { transform:rotate(180deg); color:var(--accent); }

/* Body – dùng max-height để animate smooth, không thay đổi kích thước đột ngột */
.acc-body {
  max-height: 0;
  overflow: hidden;
  transition: max-height .3s ease;
}
.acc-body.expanded {
  max-height: 300px; /* đủ lớn cho nội dung bài học */
}
.acc-inner { padding: .8rem 1.4rem 1rem 3.8rem; }
.acc-inner p { font-size: .84rem; color: var(--muted); line-height: 1.7; margin-bottom: .5rem; }
.video-link { display:inline-flex; align-items:center; gap:.4rem; font-size:.8rem; color:var(--accent); font-weight:600; padding:.35rem .75rem; background:var(--accent-light); border:1px solid #bfdbfe; border-radius:7px; text-decoration:none; transition:all .15s; }
.video-link:hover { background:#bfdbfe; }

.loading     { text-align:center; padding:3rem; color:var(--muted); }
.empty-state { text-align:center; padding:2.5rem; color:var(--muted); }
.empty-icon  { font-size:2rem; margin-bottom:.5rem; opacity:.4; }
.empty-state p { font-size:.88rem; }

.badge { font-size:.68rem; font-weight:700; padding:.2rem .6rem; border-radius:100px; }
.badge-green  { background:var(--green-light);  color:var(--green);  border:1px solid #a7f3d0; }
.badge-blue   { background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; }
.badge-gray   { background:#f1f5f9; color:var(--muted); border:1px solid var(--border); }

.btn { display:inline-flex; align-items:center; gap:.4rem; padding:.52rem 1.1rem; border-radius:8px; font-size:.84rem; font-weight:600; cursor:pointer; border:none; transition:all .15s; font-family:'Plus Jakarta Sans',sans-serif; text-decoration:none; }
.btn-accent { background:var(--accent); color:#fff; }
.btn-accent:hover:not(:disabled) { background:var(--accent-dark); box-shadow:0 4px 12px rgba(37,99,235,.3); }
.btn-accent:disabled { opacity:.5; cursor:not-allowed; }

.toast { position:fixed; bottom:2rem; right:2rem; padding:.75rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .25s ease; border:1.5px solid; box-shadow:var(--shadow); }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light);   color:var(--red);   border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }
</style>