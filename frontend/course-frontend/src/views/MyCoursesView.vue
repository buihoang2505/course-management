<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>Khóa học của tôi</h1>
        <p>Theo dõi tiến độ học tập của bạn</p>
      </div>
    </div>

    <!-- Stats row -->
    <div v-if="enrollments.length" class="stats-row">
      <div class="stat-card"><div class="sn">{{ enrollments.length }}</div><div class="sl">Đã đăng ký</div></div>
      <div class="stat-card"><div class="sn green">{{ completedCount }}</div><div class="sl">Hoàn thành</div></div>
      <div class="stat-card"><div class="sn blue">{{ inProgressCount }}</div><div class="sl">Đang học</div></div>
      <div class="stat-card"><div class="sn yellow">{{ gradedCount }}</div><div class="sl">Có điểm</div></div>
    </div>

    <div v-if="loading" class="loading">⏳ Đang tải...</div>
    <div v-else-if="!enrollments.length" class="empty-state">
      <div class="empty-icon">📖</div>
      <p>Bạn chưa đăng ký khóa học nào.</p>
      <router-link to="/courses" class="btn btn-accent" style="margin-top:1rem">Khám phá khóa học →</router-link>
    </div>

    <!-- 2-column grid -->
    <div v-else class="enroll-grid">
      <div v-for="e in enrollments" :key="e.id" class="enroll-card">
        <div class="ec-top">
          <div class="ec-icon">{{ icon(e.id) }}</div>
          <div class="ec-info">
            <h3>{{ e.course?.title || 'Khóa học' }}</h3>
            <p>{{ e.course?.description }}</p>
          </div>
          <span :class="['badge', e.status==='ACTIVE'?'badge-blue':'badge-green']">{{ e.status }}</span>
        </div>

        <!-- Progress bar cố định chiều cao -->
        <div class="progress-section">
          <div class="progress-lbl">
            <span>Tiến độ học</span>
            <span class="progress-pct">{{ pct(e) }}%</span>
          </div>
          <div class="progress-track">
            <div class="progress-fill" :style="`width:${pct(e)}%`"></div>
          </div>
          <div class="progress-meta">{{ e.lessonsCompleted || 0 }} / {{ e.course?.totalLessons || '?' }} bài học</div>
        </div>

        <div class="ec-footer">
          <span class="ec-date">📅 {{ fmt(e.enrolledAt) }}</span>
          <div class="ec-btns">
            <router-link :to="`/courses/${e.course?.id}`" class="btn btn-ghost btn-sm">Xem bài học</router-link>
            <button @click="unenroll(e)" class="btn btn-danger btn-sm">Hủy</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="toast" :class="['toast', toast.type]">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const enrollments = ref([])
const loading = ref(true)
const toast = ref(null)

const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']
const icon = id => icons[id % icons.length]
const pct = e => {
  const total = e.course?.totalLessons || 0
  const done  = e.lessonsCompleted   || 0
  if (!total) return e.status === 'COMPLETED' ? 100 : 0
  return Math.round((done / total) * 100)
}
const fmt = dt => dt ? new Date(dt).toLocaleDateString('vi-VN') : '—'
const completedCount  = computed(() => enrollments.value.filter(e => e.status === 'COMPLETED').length)
const inProgressCount = computed(() => enrollments.value.filter(e => e.status === 'ACTIVE').length)
const gradedCount     = computed(() => enrollments.value.filter(e => e.grade).length)

function showToast(msg, type='success') { toast.value={msg,type}; setTimeout(()=>toast.value=null,3000) }

async function load() {
  loading.value = true
  try { enrollments.value = (await api.get(`/enrollments/user/${auth.user?.id}`)).data }
  catch(e) { console.error(e) }
  finally { loading.value = false }
}

async function unenroll(e) {
  if (!confirm('Bạn có chắc muốn hủy đăng ký?')) return
  try {
    await api.delete(`/enrollments/unenroll?userId=${auth.user?.id}&courseId=${e.course?.id}`)
    enrollments.value = enrollments.value.filter(en => en.id !== e.id)
    showToast('Đã hủy đăng ký!')
  } catch(err) { showToast(err.response?.data?.error || 'Hủy thất bại!', 'error') }
}

onMounted(load)
</script>

<style scoped>
.page { padding: 2rem 2.5rem; max-width: 1400px; margin: 0 auto; }
.page-header { margin-bottom: 1.5rem; }
.page-header h1 { font-size: 1.6rem; font-weight: 800; margin-bottom: .3rem; }
.page-header p  { color: var(--muted); font-size: .85rem; }

/* Stats */
.stats-row { display: grid; grid-template-columns: repeat(4,1fr); gap: .8rem; margin-bottom: 1.5rem; }
.stat-card { background: var(--surface); border: 1.5px solid var(--border); border-radius: 12px; padding: 1.1rem 1.4rem; text-align: center; box-shadow: var(--shadow-sm); }
.sn { font-size: 1.7rem; font-weight: 800; line-height: 1.2; }
.sl { font-size: .74rem; color: var(--muted); margin-top: .2rem; }
.sn.green  { color: var(--green); }
.sn.blue   { color: var(--accent); }
.sn.yellow { color: var(--yellow); }

/* 2-column grid */
.enroll-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}
@media (max-width: 900px) { .enroll-grid { grid-template-columns: 1fr; } }

.enroll-card {
  background: var(--surface);
  border: 1.5px solid var(--border);
  border-radius: 14px;
  padding: 1.4rem;
  display: flex; flex-direction: column; gap: 1rem;
  box-shadow: var(--shadow-sm);
  transition: border-color .15s;
}
.enroll-card:hover { border-color: var(--accent); }

.ec-top { display: flex; align-items: flex-start; gap: .9rem; }
.ec-icon { font-size: 1.9rem; flex-shrink: 0; line-height: 1; }
.ec-info { flex: 1; min-width: 0; }
.ec-info h3 { font-size: .9rem; font-weight: 700; margin-bottom: .2rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ec-info p  { font-size: .77rem; color: var(--muted); display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }

/* Progress – chiều cao cố định, không bị đẩy */
.progress-section { display: flex; flex-direction: column; gap: .35rem; }
.progress-lbl  { display: flex; justify-content: space-between; font-size: .77rem; color: var(--muted); }
.progress-pct  { color: var(--accent); font-weight: 600; }
.progress-track { height: 7px; background: var(--surface2); border: 1px solid var(--border); border-radius: 100px; overflow: hidden; }
.progress-fill  { height: 100%; background: linear-gradient(90deg, var(--accent), #60a5fa); border-radius: 100px; transition: width .7s ease; }
.progress-meta  { font-size: .72rem; color: var(--muted); }

.ec-footer { display: flex; justify-content: space-between; align-items: center; gap: .5rem; flex-wrap: wrap; margin-top: auto; }
.ec-date  { font-size: .77rem; color: var(--muted); }
.ec-btns  { display: flex; gap: .45rem; }
.btn-sm   { padding: .38rem .8rem; font-size: .78rem; }

.loading     { text-align: center; padding: 3rem; color: var(--muted); }
.empty-state { text-align: center; padding: 4rem 2rem; color: var(--muted); }
.empty-icon  { font-size: 2.5rem; margin-bottom: .8rem; opacity: .4; }
.empty-state p { font-size: .9rem; }

.toast { position:fixed; bottom:2rem; right:2rem; padding:.75rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .25s ease; border:1.5px solid; box-shadow:var(--shadow); }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light);   color:var(--red);   border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }
</style>