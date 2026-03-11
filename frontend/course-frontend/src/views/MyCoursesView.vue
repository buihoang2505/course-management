<template>
  <div class="page">
    <div class="page-header">
      <h1>Khóa học của tôi</h1>
      <p>Theo dõi tiến độ học tập của bạn</p>
    </div>

    <div class="stats-row">
      <div class="stat-card" v-for="s in statCards" :key="s.label">
        <div :class="['sn', s.color]">
          <span v-if="!loading">{{ s.value }}</span>
          <div v-else class="sk sk-num"></div>
        </div>
        <div class="sl">{{ s.label }}</div>
      </div>
    </div>

    <!-- Filter bar -->
    <div class="filter-bar">
      <div class="search-wrap">
        <svg class="search-icon" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input v-model="keyword" placeholder="Tìm khóa học..." class="search-input"/>
        <button v-if="keyword" class="search-clear" @click="keyword=''">✕</button>
      </div>
      <div class="filter-group">
        <button v-for="f in filterOptions" :key="f.val"
                :class="['filter-btn', { active: statusFilter===f.val }]"
                @click="statusFilter=f.val">
          {{ f.label }}
        </button>
      </div>
      <select v-model="sortBy" class="sort-select">
        <option value="date_desc">Mới nhất</option>
        <option value="date_asc">Cũ nhất</option>
        <option value="progress_desc">Tiến độ cao</option>
        <option value="progress_asc">Tiến độ thấp</option>
        <option value="title_asc">Tên A→Z</option>
      </select>
    </div>

    <div class="content-panel">
      <!-- SKELETON -->
      <div v-if="loading" class="enroll-grid">
        <div v-for="i in 4" :key="i" class="enroll-card sk-card">
          <div class="sk-top">
            <div class="sk sk-icon"></div>
            <div class="sk-info"><div class="sk sk-title"></div><div class="sk sk-sub"></div></div>
            <div class="sk sk-badge"></div>
          </div>
          <div class="sk sk-prog"></div>
          <div class="sk-footer"><div class="sk sk-date"></div><div class="sk sk-btns"></div></div>
        </div>
      </div>

      <!-- EMPTY (chưa đăng ký) -->
      <div v-else-if="!enrollments.length" class="center-state">
        <div class="empty-icon">📖</div>
        <p>Bạn chưa đăng ký khóa học nào.</p>
        <router-link to="/courses" class="btn btn-accent" style="margin-top:1rem">Khám phá khóa học →</router-link>
      </div>

      <!-- EMPTY (có filter nhưng không có kết quả) -->
      <div v-else-if="!pagedEnrollments.length" class="center-state">
        <div class="empty-icon">🔍</div>
        <p>Không tìm thấy khóa học phù hợp</p>
        <button class="btn btn-ghost" style="margin-top:.8rem" @click="resetFilters">↺ Xóa bộ lọc</button>
      </div>

      <!-- LIST -->
      <div v-else>
        <div class="enroll-grid">
          <div v-for="e in pagedEnrollments" :key="e.id" class="enroll-card"
               :class="{ 'card-done': e.status==='COMPLETED' }">
            <div class="ec-top">
              <div class="ec-icon">{{ icon(e.id) }}</div>
              <div class="ec-info">
                <h3>{{ e.course?.title || 'Khóa học' }}</h3>
                <p>{{ e.course?.instructor || 'Chưa xác định' }}</p>
              </div>
              <span :class="['badge', statusBadge(e.status)]">{{ statusLabel(e.status) }}</span>
            </div>

            <div class="progress-wrap">
              <div class="progress-lbl">
                <span>Tiến độ</span>
                <span class="progress-pct" :class="{ 'pct-done': progressOf(e).courseCompleted }">
                  {{ progressOf(e).percentage }}%
                </span>
              </div>
              <div class="progress-track">
                <div class="progress-fill"
                     :class="{ 'fill-done': progressOf(e).courseCompleted }"
                     :style="`width:${progressOf(e).percentage}%`"></div>
              </div>
              <div class="prog-meta">
                {{ progressOf(e).completedLessons }}/{{ progressOf(e).totalLessons }} bài học
                <span v-if="progressOf(e).courseCompleted" class="done-chip">🎉 Hoàn thành</span>
              </div>
            </div>

            <div class="ec-footer">
              <span class="ec-date">📅 {{ fmt(e.enrolledAt) }}</span>
              <div class="ec-btns">
                <router-link :to="`/courses/${e.course?.id}`" class="btn btn-ghost btn-sm">Tiếp tục học</router-link>
                <button @click="unenroll(e)" class="btn btn-danger btn-sm">Hủy</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Phân trang -->
        <div v-if="totalPages > 1" class="pagination">
          <button class="pg-btn" :disabled="page===1" @click="page--">‹</button>
          <button v-for="p in pageNumbers" :key="p"
                  :class="['pg-btn', { active: p===page, ellipsis: p==='...' }]"
                  :disabled="p==='...'"
                  @click="p !== '...' && (page=p)">{{ p }}</button>
          <button class="pg-btn" :disabled="page===totalPages" @click="page++">›</button>
        </div>
      </div>
    </div>

    <div v-if="toast" :class="['toast', toast.type]">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const enrollments = ref([])
const progressMap = ref({})
const loading = ref(true)
const toast = ref(null)
const keyword = ref('')
const statusFilter = ref('ALL')
const sortBy = ref('date_desc')
const page = ref(1)
const PAGE_SIZE = 6

const filterOptions = [
  { val: 'ALL',       label: 'Tất cả' },
  { val: 'ACTIVE',    label: '📘 Đang học' },
  { val: 'COMPLETED', label: '✅ Hoàn thành' },
]

const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']
const icon  = id => icons[id % icons.length]
const fmt   = dt => dt ? new Date(dt).toLocaleDateString('vi-VN') : '—'
const progressOf = e => progressMap.value[e.course?.id] ?? { percentage:0, completedLessons:0, totalLessons:0, courseCompleted:false }
const statusLabel = s => ({ ACTIVE:'Đang học', COMPLETED:'Hoàn thành', INACTIVE:'Tạm dừng' }[s] ?? s)
const statusBadge = s => ({ ACTIVE:'badge-blue', COMPLETED:'badge-green', INACTIVE:'badge-gray' }[s] ?? 'badge-gray')

const completedCount  = computed(() => enrollments.value.filter(e => e.status==='COMPLETED').length)
const inProgressCount = computed(() => enrollments.value.filter(e => e.status==='ACTIVE').length)
const gradedCount     = computed(() => enrollments.value.filter(e => e.grade).length)
const statCards = computed(() => [
  { label:'Đã đăng ký',  value:enrollments.value.length, color:'' },
  { label:'Hoàn thành',  value:completedCount.value,     color:'green' },
  { label:'Đang học',    value:inProgressCount.value,    color:'blue' },
  { label:'Có điểm',     value:gradedCount.value,        color:'yellow' },
])

function resetFilters() { keyword.value=''; statusFilter.value='ALL'; sortBy.value='date_desc'; page.value=1 }
watch([keyword, statusFilter, sortBy], () => { page.value=1 })

const filteredEnrollments = computed(() => {
  let list = [...enrollments.value]
  if (statusFilter.value !== 'ALL') list = list.filter(e => e.status === statusFilter.value)
  const kw = keyword.value.trim().toLowerCase()
  if (kw) list = list.filter(e =>
      e.course?.title?.toLowerCase().includes(kw) ||
      e.course?.instructor?.toLowerCase().includes(kw)
  )
  if (sortBy.value === 'date_desc')     list.sort((a,b) => new Date(b.enrolledAt)-new Date(a.enrolledAt))
  if (sortBy.value === 'date_asc')      list.sort((a,b) => new Date(a.enrolledAt)-new Date(b.enrolledAt))
  if (sortBy.value === 'progress_desc') list.sort((a,b) => (progressOf(b).percentage||0)-(progressOf(a).percentage||0))
  if (sortBy.value === 'progress_asc')  list.sort((a,b) => (progressOf(a).percentage||0)-(progressOf(b).percentage||0))
  if (sortBy.value === 'title_asc')     list.sort((a,b) => (a.course?.title||'').localeCompare(b.course?.title||''))
  return list
})

const totalPages = computed(() => Math.ceil(filteredEnrollments.value.length / PAGE_SIZE))
const pagedEnrollments = computed(() => {
  const start = (page.value-1)*PAGE_SIZE
  return filteredEnrollments.value.slice(start, start+PAGE_SIZE)
})
const pageNumbers = computed(() => {
  const total=totalPages.value, cur=page.value
  if (total<=7) return Array.from({length:total},(_,i)=>i+1)
  const pages=[1]
  if (cur>3) pages.push('...')
  for(let i=Math.max(2,cur-1);i<=Math.min(total-1,cur+1);i++) pages.push(i)
  if(cur<total-2) pages.push('...')
  pages.push(total)
  return pages
})

function showToast(msg, type='success') { toast.value={msg,type}; setTimeout(()=>toast.value=null,3000) }

async function loadProgress(courseId) {
  try { const r = await api.get(`/progress?userId=${auth.user?.id}&courseId=${courseId}`); progressMap.value[courseId]=r.data } catch {}
}

async function load() {
  loading.value=true
  try {
    const r = await api.get(`/enrollments/user/${auth.user?.id}`)
    enrollments.value=r.data
    await Promise.all(enrollments.value.filter(e=>e.course?.id).map(e=>loadProgress(e.course.id)))
  } catch(e){ console.error(e) }
  finally { loading.value=false }
}

async function unenroll(e) {
  if (!confirm('Bạn có chắc muốn hủy đăng ký?')) return
  try {
    await api.delete(`/enrollments/unenroll?userId=${auth.user?.id}&courseId=${e.course?.id}`)
    enrollments.value=enrollments.value.filter(en=>en.id!==e.id)
    showToast('Đã hủy đăng ký!')
  } catch(err) { showToast(err.response?.data?.error||'Hủy thất bại!','error') }
}

onMounted(load)
</script>

<style scoped>
.page { padding:2rem 2.5rem; max-width:1400px; margin:0 auto; }
@media(max-width:600px) { .page { padding:1rem; } }
.page-header { margin-bottom:1.2rem; }
.page-header h1 { font-size:1.6rem; font-weight:800; margin-bottom:.3rem; }
.page-header p  { color:var(--muted); font-size:.85rem; }

/* Stats */
.stats-row { display:grid; grid-template-columns:repeat(4,1fr); gap:.8rem; margin-bottom:1rem; }
@media(max-width:700px) { .stats-row { grid-template-columns:repeat(2,1fr); } }
.stat-card { background:var(--surface); border:1.5px solid var(--border); border-radius:12px; padding:1rem 1.4rem; text-align:center; height:84px; display:flex; flex-direction:column; justify-content:center; }
.sn { font-size:1.7rem; font-weight:800; line-height:1; min-height:32px; display:flex; align-items:center; justify-content:center; }
.sl { font-size:.73rem; color:var(--muted); margin-top:.3rem; }
.sn.green  { color:var(--green); }
.sn.blue   { color:var(--accent); }
.sn.yellow { color:var(--yellow); }

/* Filter bar */
.filter-bar { display:flex; align-items:center; gap:.6rem; flex-wrap:wrap; margin-bottom:1rem; background:var(--surface); border:1.5px solid var(--border); border-radius:12px; padding:.7rem 1rem; }
.search-wrap { position:relative; flex:1; min-width:180px; max-width:280px; }
.search-icon { position:absolute; left:.8rem; top:50%; transform:translateY(-50%); color:var(--muted); pointer-events:none; }
.search-input { width:100%; padding:.45rem 2rem .45rem 2.2rem; background:var(--surface2); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.83rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; transition:border-color .15s; box-sizing:border-box; }
.search-input:focus { border-color:var(--accent); }
.search-clear { position:absolute; right:.5rem; top:50%; transform:translateY(-50%); background:none; border:none; color:var(--muted); cursor:pointer; font-size:.75rem; padding:0; }
.filter-group { display:flex; gap:.3rem; flex-wrap:wrap; }
.filter-btn { padding:.35rem .7rem; border-radius:7px; border:1.5px solid var(--border); background:var(--surface2); color:var(--muted); font-size:.75rem; font-weight:600; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; }
.filter-btn.active { background:var(--accent); color:#fff; border-color:var(--accent); }
.sort-select { padding:.38rem .75rem; border-radius:8px; border:1.5px solid var(--border); background:var(--surface2); color:var(--text); font-size:.79rem; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; outline:none; }

/* Panel */
.content-panel { background:var(--surface); border:1.5px solid var(--border); border-radius:14px; min-height:400px; padding:1.3rem; }
.center-state { display:flex; flex-direction:column; align-items:center; justify-content:center; min-height:300px; color:var(--muted); gap:.6rem; }
.empty-icon { font-size:2.5rem; opacity:.4; }
.center-state p { font-size:.9rem; }

/* Grid */
.enroll-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:1rem; }
@media(max-width:900px) { .enroll-grid { grid-template-columns:1fr; } }

/* Card */
.enroll-card { background:var(--surface2); border:1.5px solid var(--border); border-radius:12px; padding:1.1rem 1.2rem; height:190px; display:flex; flex-direction:column; gap:.7rem; transition:border-color .15s; overflow:hidden; }
.enroll-card:hover { border-color:var(--accent); }
.enroll-card.card-done { border-color:#a7f3d0; background:#f0fdf4; }
.enroll-card.card-done:hover { border-color:var(--green); }
.ec-top  { display:flex; align-items:flex-start; gap:.8rem; flex-shrink:0; }
.ec-icon { font-size:1.6rem; flex-shrink:0; line-height:1; }
.ec-info { flex:1; min-width:0; }
.ec-info h3 { font-size:.87rem; font-weight:700; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.ec-info p  { font-size:.75rem; color:var(--muted); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.progress-wrap { flex-shrink:0; }
.progress-lbl  { display:flex; justify-content:space-between; font-size:.74rem; color:var(--muted); margin-bottom:.3rem; }
.progress-pct  { color:var(--accent); font-weight:700; }
.progress-pct.pct-done { color:var(--green); }
.progress-track { height:6px; background:var(--border); border-radius:100px; overflow:hidden; }
.progress-fill  { height:100%; background:linear-gradient(90deg,var(--accent),#60a5fa); border-radius:100px; transition:width .7s ease; }
.progress-fill.fill-done { background:linear-gradient(90deg,var(--green),#34d399); }
.prog-meta { font-size:.72rem; color:var(--muted); margin-top:.3rem; display:flex; align-items:center; gap:.5rem; }
.done-chip { font-size:.67rem; font-weight:700; padding:.14rem .5rem; border-radius:100px; background:var(--green-light); color:var(--green); border:1px solid #a7f3d0; }
.ec-footer { display:flex; justify-content:space-between; align-items:center; margin-top:auto; flex-shrink:0; }
.ec-date   { font-size:.74rem; color:var(--muted); }
.ec-btns   { display:flex; gap:.4rem; }
.badge { font-size:.66rem; font-weight:700; padding:.18rem .55rem; border-radius:100px; flex-shrink:0; }
.badge-green { background:var(--green-light); color:var(--green); border:1px solid #a7f3d0; }
.badge-blue  { background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; }
.badge-gray  { background:#f1f5f9; color:var(--muted); border:1px solid var(--border); }

/* Pagination */
.pagination { display:flex; justify-content:center; align-items:center; gap:.3rem; margin-top:1.2rem; flex-wrap:wrap; }
.pg-btn { min-width:34px; height:34px; padding:0 .4rem; border-radius:8px; border:1.5px solid var(--border); background:var(--surface); color:var(--text2); font-size:.8rem; font-weight:600; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; display:flex; align-items:center; justify-content:center; }
.pg-btn:hover:not(:disabled):not(.ellipsis) { border-color:var(--accent); color:var(--accent); background:var(--accent-light); }
.pg-btn.active { background:var(--accent); color:#fff; border-color:var(--accent); }
.pg-btn:disabled { opacity:.4; cursor:not-allowed; }
.pg-btn.ellipsis { cursor:default; border-color:transparent; background:none; }

/* Buttons */
.btn { display:inline-flex; align-items:center; gap:.35rem; padding:.48rem 1rem; border-radius:8px; font-size:.83rem; font-weight:600; cursor:pointer; border:none; transition:all .15s; font-family:'Plus Jakarta Sans',sans-serif; text-decoration:none; white-space:nowrap; }
.btn-sm { padding:.35rem .75rem; font-size:.76rem; }
.btn-accent { background:var(--accent); color:#fff; }
.btn-accent:hover { background:var(--accent-dark); }
.btn-ghost  { background:var(--surface); color:var(--text2); border:1.5px solid var(--border); }
.btn-ghost:hover { border-color:var(--accent); color:var(--accent); }
.btn-danger { background:var(--red-light); color:var(--red); border:1px solid #fca5a5; }
.btn-danger:hover { background:#fecaca; }

/* Skeleton */
.sk { background:linear-gradient(90deg,#e2e8f0 25%,#f1f5f9 50%,#e2e8f0 75%); background-size:200% 100%; animation:shimmer 1.4s infinite; border-radius:7px; }
@keyframes shimmer { to { background-position:-200% 0; } }
.sk-icon { width:36px; height:36px; border-radius:10px; flex-shrink:0; }
.sk-title { width:160px; height:15px; margin-bottom:.35rem; }
.sk-sub   { width:100px; height:12px; }
.sk-badge { width:60px; height:18px; border-radius:100px; }
.sk-prog  { width:100%; height:6px; border-radius:100px; }
.sk-date  { width:80px; height:12px; }
.sk-btns  { width:140px; height:28px; border-radius:7px; }
.sk-num   { width:32px; height:28px; border-radius:6px; margin:0 auto .25rem; }
.sk-card  { pointer-events:none; }
.sk-top   { display:flex; align-items:center; gap:.8rem; }
.sk-info  { flex:1; }
.sk-footer { display:flex; justify-content:space-between; align-items:center; margin-top:auto; }

.toast { position:fixed; bottom:2rem; right:2rem; padding:.72rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .25s ease; border:1.5px solid; }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light); color:var(--red); border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }
</style>