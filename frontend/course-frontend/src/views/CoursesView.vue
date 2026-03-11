<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">
          {{ auth.isAdmin ? '📋 Tất cả khóa học' : '🔍 Khám phá khóa học' }}
        </h1>
        <p class="page-sub">{{ filteredCourses.length }} khóa học</p>
      </div>
    </div>

    <!-- ── Thanh tìm kiếm + bộ lọc ── -->
    <div class="filter-bar">
      <!-- Search -->
      <div class="search-wrap">
        <svg class="search-icon" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input v-model="keyword" placeholder="Tìm theo tên, giảng viên..." class="search-input"/>
        <button v-if="keyword" class="search-clear" @click="keyword=''">✕</button>
      </div>

      <!-- Lọc trạng thái (chỉ admin) -->
      <div v-if="auth.isAdmin" class="filter-group">
        <button v-for="s in statusOptions" :key="s.val"
                :class="['filter-btn', { active: statusFilter===s.val }]"
                @click="statusFilter=s.val">
          {{ s.label }}
        </button>
      </div>

      <!-- Sort -->
      <div class="sort-wrap">
        <select v-model="sortBy" class="sort-select">
          <option value="default">Mặc định</option>
          <option value="title_asc">Tên A→Z</option>
          <option value="title_desc">Tên Z→A</option>
          <option value="credits_asc">Tín chỉ tăng dần</option>
          <option value="credits_desc">Tín chỉ giảm dần</option>
        </select>
      </div>

      <!-- Nút reset -->
      <button v-if="hasActiveFilter" class="reset-btn" @click="resetFilters">
        ↺ Đặt lại
      </button>
    </div>

    <!-- Active filter chips -->
    <div v-if="hasActiveFilter" class="filter-chips">
      <span v-if="keyword" class="chip">
        🔍 "{{ keyword }}" <button @click="keyword=''">✕</button>
      </span>
      <span v-if="statusFilter!=='ALL'" class="chip">
        {{ statusOptions.find(s=>s.val===statusFilter)?.label }}
        <button @click="statusFilter='ALL'">✕</button>
      </span>
      <span v-if="sortBy!=='default'" class="chip">
        ↕ {{ sortOptions[sortBy] }} <button @click="sortBy='default'">✕</button>
      </span>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="courses-grid">
      <div v-for="i in 6" :key="i" class="course-card sk-card">
        <div class="sk-top">
          <div class="sk sk-icon"></div>
          <div class="sk sk-badge"></div>
        </div>
        <div class="sk sk-title"></div>
        <div class="sk sk-desc"></div>
        <div class="sk sk-desc short"></div>
        <div class="sk sk-footer"></div>
      </div>
    </div>

    <!-- Empty -->
    <div v-else-if="!pagedCourses.length" class="empty-state">
      <div class="empty-icon">📚</div>
      <p v-if="hasActiveFilter">Không tìm thấy khóa học phù hợp</p>
      <p v-else>Chưa có khóa học nào</p>
      <button v-if="hasActiveFilter" class="reset-btn mt" @click="resetFilters">↺ Xóa bộ lọc</button>
    </div>

    <!-- Grid -->
    <div v-else class="courses-grid">
      <div v-for="(course, idx) in pagedCourses" :key="course.id"
           class="course-card" :style="`animation-delay:${idx * 0.04}s`"
           :class="{ 'card-inactive': course.status === 'INACTIVE', 'card-draft': course.status === 'DRAFT' }">

        <div class="card-top">
          <span class="course-emoji">{{ courseIcon(idx) }}</span>
          <div class="card-badges">
            <span class="badge badge-blue">{{ course.credits }} TC</span>
            <span :class="['badge', statusBadgeClass(course.status)]">{{ course.status }}</span>
          </div>
        </div>

        <!-- Highlight từ khóa tìm kiếm trong title -->
        <h3 class="course-title"
            :class="keyword ? 'title-plain' : titleColorClass(course.status)"
            v-html="highlight(course.title)"></h3>
        <p class="course-desc">{{ course.description || 'Chưa có mô tả.' }}</p>

        <div class="course-inst">
          <span :class="['inst-dot', { 'dot-gray': course.status !== 'ACTIVE' }]"></span>
          <span>{{ course.instructor || 'Chưa xác định' }}</span>
        </div>

        <div v-if="!auth.isAdmin && course.status === 'INACTIVE' && isEnrolled(course.id)" class="inactive-notice">
          ⚠️ Khóa học đã đóng — Bạn vẫn có thể xem nội dung
        </div>

        <div class="card-footer">
          <span v-if="course.status === 'DRAFT' && auth.isAdmin" class="draft-tag">🔒 Chưa công khai</span>
          <router-link :to="`/courses/${course.id}`" class="btn btn-outline">Chi tiết</router-link>
          <template v-if="!auth.isAdmin">
            <button v-if="isEnrolled(course.id)" class="btn btn-enrolled" disabled>✓ Đã đăng ký</button>
            <button v-else-if="course.status === 'ACTIVE'" @click="enrollCourse(course.id)"
                    class="btn btn-accent" :disabled="enrolling === course.id">
              {{ enrolling === course.id ? '...' : 'Đăng ký' }}
            </button>
            <button v-else-if="course.status === 'INACTIVE'" class="btn btn-disabled" disabled>Đã đóng</button>
          </template>
        </div>
      </div>
    </div>

    <!-- ── Phân trang ── -->
    <div v-if="totalPages > 1" class="pagination">
      <button class="pg-btn" :disabled="page===1" @click="page--">‹</button>
      <button v-for="p in pageNumbers" :key="p"
              :class="['pg-btn', { active: p===page, ellipsis: p==='...' }]"
              :disabled="p==='...'"
              @click="p !== '...' && (page=p)">{{ p }}</button>
      <button class="pg-btn" :disabled="page===totalPages" @click="page++">›</button>
    </div>

    <div v-if="toast" :class="['toast', toast.type]">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth      = useAuthStore()
const allCourses  = ref([])
const enrolledIds = ref([])
const loading   = ref(true)
const keyword   = ref('')
const sortBy    = ref('default')
const statusFilter = ref('ALL')
const enrolling = ref(null)
const toast     = ref(null)
const page      = ref(1)
const PAGE_SIZE = 9

const statusOptions = [
  { val: 'ALL',      label: 'Tất cả' },
  { val: 'ACTIVE',   label: '✅ Active' },
  { val: 'INACTIVE', label: '⏸ Inactive' },
  { val: 'DRAFT',    label: '📝 Draft' },
]

const sortOptions = {
  title_asc: 'Tên A→Z', title_desc: 'Tên Z→A',
  credits_asc: 'TC tăng', credits_desc: 'TC giảm'
}

const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']
const courseIcon = idx => icons[idx % icons.length]
const isEnrolled = courseId => enrolledIds.value.includes(Number(courseId))

function statusBadgeClass(s) {
  return { ACTIVE:'badge-green', INACTIVE:'badge-gray', DRAFT:'badge-yellow' }[s] || 'badge-gray'
}
function titleColorClass(s) {
  return { ACTIVE:'title-active', INACTIVE:'title-inactive', DRAFT:'title-draft' }[s] || ''
}

// Highlight từ khóa trong text
function highlight(text) {
  if (!keyword.value.trim() || !text) return text
  const escaped = keyword.value.trim().replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark>$1</mark>')
}

const hasActiveFilter = computed(() =>
    keyword.value.trim() || statusFilter.value !== 'ALL' || sortBy.value !== 'default'
)

function resetFilters() {
  keyword.value = ''
  statusFilter.value = 'ALL'
  sortBy.value = 'default'
  page.value = 1
}

// Reset page khi filter thay đổi
watch([keyword, statusFilter, sortBy], () => { page.value = 1 })

const filteredCourses = computed(() => {
  let list = [...allCourses.value]

  // Lọc status (admin only)
  if (auth.isAdmin && statusFilter.value !== 'ALL') {
    list = list.filter(c => c.status === statusFilter.value)
  }

  // Tìm kiếm
  const kw = keyword.value.trim().toLowerCase()
  if (kw) {
    list = list.filter(c =>
        c.title?.toLowerCase().includes(kw) ||
        c.instructor?.toLowerCase().includes(kw) ||
        c.description?.toLowerCase().includes(kw)
    )
  }

  // Sort
  if (sortBy.value === 'title_asc')     list.sort((a,b) => a.title.localeCompare(b.title))
  if (sortBy.value === 'title_desc')    list.sort((a,b) => b.title.localeCompare(a.title))
  if (sortBy.value === 'credits_asc')   list.sort((a,b) => (a.credits||0) - (b.credits||0))
  if (sortBy.value === 'credits_desc')  list.sort((a,b) => (b.credits||0) - (a.credits||0))

  return list
})

// Phân trang
const totalPages = computed(() => Math.ceil(filteredCourses.value.length / PAGE_SIZE))
const pagedCourses = computed(() => {
  const start = (page.value - 1) * PAGE_SIZE
  return filteredCourses.value.slice(start, start + PAGE_SIZE)
})

const pageNumbers = computed(() => {
  const total = totalPages.value
  const cur   = page.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const pages = [1]
  if (cur > 3) pages.push('...')
  for (let i = Math.max(2, cur-1); i <= Math.min(total-1, cur+1); i++) pages.push(i)
  if (cur < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

function showToast(msg, type = 'success') {
  toast.value = { msg, type }
  setTimeout(() => toast.value = null, 3000)
}

async function fetchCourses() {
  loading.value = true
  try {
    const url = auth.isAdmin ? '/courses' : '/courses/active'
    const res = await api.get(url)
    allCourses.value = res.data
  } catch {
    showToast('Không thể tải danh sách khóa học!', 'error')
  } finally {
    loading.value = false
  }
}

async function fetchEnrolledCourses() {
  if (auth.isAdmin || !auth.user?.id) return
  try {
    const res = await api.get(`/enrollments/user/${auth.user.id}`)
    enrolledIds.value = res.data.map(e => Number(e.course?.id)).filter(Boolean)
  } catch {}
}

async function enrollCourse(courseId) {
  enrolling.value = courseId
  try {
    await api.post(`/enrollments/enroll?userId=${auth.user?.id}&courseId=${courseId}`)
    enrolledIds.value.push(Number(courseId))
    showToast('🎉 Đăng ký khóa học thành công!')
  } catch (e) {
    showToast(e.response?.data?.error || 'Đăng ký thất bại!', 'error')
  } finally {
    enrolling.value = null
  }
}

onMounted(async () => {
  await Promise.all([fetchCourses(), fetchEnrolledCourses()])
})
</script>

<style scoped>
.page { padding: 2rem 2.5rem; max-width: 1280px; margin: 0 auto; }
@media(max-width:600px) { .page { padding:1rem; } }

.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 1.2rem; gap: 1rem; flex-wrap: wrap;
}
.page-title {
  font-size: 1.35rem; font-weight: 800; color: var(--text);
  background: linear-gradient(135deg, #1e40af, #7c3aed);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  margin-bottom: .2rem;
}
.page-sub { font-size: .82rem; color: var(--muted); }

/* ── Filter bar ── */
.filter-bar {
  display: flex; align-items: center; gap: .75rem;
  flex-wrap: wrap; margin-bottom: 1rem;
  background: var(--surface); border: 1.5px solid var(--border);
  border-radius: 14px; padding: .8rem 1rem;
}

/* Search */
.search-wrap { position: relative; flex: 1; min-width: 200px; max-width: 320px; }
.search-icon {
  position: absolute; left: .85rem; top: 50%; transform: translateY(-50%);
  color: var(--muted); pointer-events: none;
}
.search-input {
  width: 100%; padding: .5rem 2.2rem .5rem 2.4rem;
  background: var(--surface2); border: 1.5px solid var(--border);
  border-radius: 9px; color: var(--text); font-size: .84rem;
  outline: none; font-family: 'Plus Jakarta Sans', sans-serif;
  transition: border-color .15s, box-shadow .15s; box-sizing: border-box;
}
.search-input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px var(--accent-light); }
.search-input::placeholder { color: var(--muted); }
.search-clear {
  position: absolute; right: .6rem; top: 50%; transform: translateY(-50%);
  background: none; border: none; color: var(--muted); cursor: pointer;
  font-size: .8rem; padding: .1rem .3rem; border-radius: 4px;
  line-height: 1;
}
.search-clear:hover { color: var(--text); }

/* Status filter */
.filter-group { display: flex; gap: .3rem; flex-wrap: wrap; }
.filter-btn {
  padding: .38rem .75rem; border-radius: 8px; border: 1.5 px solid var(--border);
  background: var(--surface2); color: var(--muted);
  font-size: .75rem; font-weight: 600; cursor: pointer;
  font-family: 'Plus Jakarta Sans', sans-serif; transition: all .15s;
}
.filter-btn:hover { border-color: var(--accent); color: var(--accent); }
.filter-btn.active { background: var(--accent); color: #fff; border-color: var(--accent); }

/* Sort */
.sort-select {
  padding: .42rem .8rem; border-radius: 9px;
  border: 1.5 px solid var(--border); background: var(--surface2);
  color: var(--text); font-size: .8rem; cursor: pointer;
  font-family: 'Plus Jakarta Sans', sans-serif; outline: none;
  transition: border-color .15s;
}
.sort-select:focus { border-color: var(--accent); }

/* Reset */
.reset-btn {
  padding: .38rem .8rem; border-radius: 8px;
  border: 1.5 px solid var(--border); background: none;
  color: var(--muted); font-size: .77rem; font-weight: 600;
  cursor: pointer; font-family: 'Plus Jakarta Sans', sans-serif;
  transition: all .15s; white-space: nowrap;
}
.reset-btn:hover { border-color: var(--red); color: var(--red); }
.reset-btn.mt { margin-top: .5rem; }

/* Filter chips */
.filter-chips { display: flex; gap: .4rem; flex-wrap: wrap; margin-bottom: .8rem; }
.chip {
  display: inline-flex; align-items: center; gap: .3rem;
  background: var(--accent-light); color: var(--accent);
  border: 1px solid #bfdbfe; border-radius: 100px;
  font-size: .72rem; font-weight: 600; padding: .2rem .65rem;
}
.chip button {
  background: none; border: none; color: var(--accent);
  cursor: pointer; font-size: .72rem; line-height: 1;
  padding: 0; margin: 0;
}
.chip button:hover { color: var(--accent-dark); }

/* Grid */
.courses-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.2rem;
}
@media (max-width: 1024px) { .courses-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px)  { .courses-grid { grid-template-columns: 1fr; } }

/* Card */
.course-card {
  background: var(--surface); border: 1.5px solid var(--border);
  border-radius: 16px; padding: 1.4rem;
  display: flex; flex-direction: column; gap: .7rem;
  transition: all .22s; animation: fadeUp .4s ease both; position: relative;
}
.course-card:hover {
  border-color: var(--accent); transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(37,99,235,.1);
}
.card-inactive { border-color: #e2e8f0; opacity: .82; }
.card-inactive:hover { border-color: var(--border2); box-shadow: 0 6px 20px rgba(0,0,0,.06); }
.card-draft { border-style: dashed; border-color: var(--yellow); background: #fffbf0; }

@keyframes fadeUp {
  from { opacity:0; transform:translateY(14px); }
  to   { opacity:1; transform:translateY(0); }
}

.card-top { display: flex; justify-content: space-between; align-items: flex-start; }
.course-emoji { font-size: 1.85rem; line-height: 1; }
.card-badges { display: flex; flex-direction: column; align-items: flex-end; gap: .3rem; }

.course-title { font-size: .97rem; font-weight: 800; line-height: 1.35; }
.title-active {
  background: linear-gradient(135deg, #1e40af, #4f46e5);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.title-inactive { color: var(--muted); }
.title-draft    { color: var(--yellow); }

/* Highlight search match */
:deep(mark) {
  background: #fbbf24; color: #1e1e1e;
  border-radius: 3px; padding: 0 2px;
  font-weight: 700;
}

/* Title khi đang search — dùng màu plain thay gradient để mark hiện đúng */
.title-plain {
  color: var(--text);
  background: none;
  -webkit-text-fill-color: unset;
}

.course-desc {
  font-size: .8rem; color: var(--muted); line-height: 1.65;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; flex: 1;
}
.course-inst {
  display: flex; align-items: center; gap: .42rem;
  font-size: .77rem; color: var(--muted);
}
.inst-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--green); flex-shrink: 0; }
.dot-gray { background: var(--border2); }
.inactive-notice {
  font-size: .73rem; color: var(--yellow); background: var(--yellow-light);
  border: 1px solid #fde68a; border-radius: 7px; padding: .35rem .6rem;
}
.draft-tag { font-size: .73rem; color: var(--yellow); font-weight: 600; margin-right: auto; }
.card-footer { display: flex; align-items: center; gap: .5rem; margin-top: .2rem; }

.btn {
  display: inline-flex; align-items: center; justify-content: center;
  border-radius: 8px; font-size: .8rem; font-weight: 700;
  cursor: pointer; border: none; transition: all .18s;
  font-family: 'Plus Jakarta Sans', sans-serif; padding: .45rem .95rem; white-space: nowrap;
}
.btn-outline { background: var(--surface2); border: 1.5px solid var(--border); color: var(--text2); flex: 1; }
.btn-outline:hover { border-color: var(--accent); color: var(--accent); background: var(--accent-light); }
.btn-accent { background: var(--accent); color: #fff; flex: 1; box-shadow: 0 2px 8px rgba(37,99,235,.25); }
.btn-accent:hover:not(:disabled) { background: var(--accent-dark); box-shadow: 0 4px 14px rgba(37,99,235,.35); transform: translateY(-1px); }
.btn-accent:disabled { opacity: .5; cursor: not-allowed; transform: none; }
.btn-enrolled { background: var(--green-light); color: var(--green); border: 1.5px solid #a7f3d0; flex: 1; cursor: default; }
.btn-disabled { background: var(--surface2); color: var(--muted); border: 1.5px solid var(--border); flex: 1; cursor: not-allowed; opacity: .6; }

.badge { font-size: .64rem; font-weight: 700; padding: .15rem .52rem; border-radius: 100px; }
.badge-green  { background: var(--green-light);  color: var(--green);  border: 1px solid #a7f3d0; }
.badge-blue   { background: var(--accent-light); color: var(--accent); border: 1px solid #bfdbfe; }
.badge-gray   { background: #f1f5f9; color: var(--muted); border: 1px solid var(--border); }
.badge-yellow { background: var(--yellow-light); color: var(--yellow); border: 1px solid #fde68a; }

/* Skeleton */
.sk-card { pointer-events: none; }
.sk { background: linear-gradient(90deg,#e2e8f0 25%,#f1f5f9 50%,#e2e8f0 75%); background-size:200% 100%; animation: shimmer 1.4s infinite; border-radius:8px; }
@keyframes shimmer { to{background-position:-200% 0} }
.sk-top { display:flex; justify-content:space-between; align-items:center; }
.sk-icon  { width:40px; height:40px; border-radius:10px; }
.sk-badge { width:60px; height:18px; }
.sk-title { width:80%; height:20px; }
.sk-desc  { width:100%; height:13px; }
.sk-desc.short { width:65%; }
.sk-footer{ width:100%; height:34px; border-radius:8px; }

/* Phân trang */
.pagination {
  display: flex; justify-content: center; align-items: center;
  gap: .35rem; margin-top: 2rem; flex-wrap: wrap;
}
.pg-btn {
  min-width: 36px; height: 36px; padding: 0 .5rem;
  border-radius: 9px; border: 1.5px solid var(--border);
  background: var(--surface); color: var(--text2);
  font-size: .82rem; font-weight: 600; cursor: pointer;
  font-family: 'Plus Jakarta Sans', sans-serif; transition: all .15s;
  display: flex; align-items: center; justify-content: center;
}
.pg-btn:hover:not(:disabled):not(.ellipsis) {
  border-color: var(--accent); color: var(--accent); background: var(--accent-light);
}
.pg-btn.active {
  background: var(--accent); color: #fff; border-color: var(--accent);
}
.pg-btn:disabled { opacity: .4; cursor: not-allowed; }
.pg-btn.ellipsis { cursor: default; border-color: transparent; background: none; }

/* Empty */
.empty-state { display:flex; flex-direction:column; align-items:center; padding:4rem 2rem; gap:.8rem; color:var(--muted); }
.empty-icon  { font-size:3rem; opacity:.4; }
.empty-state p { font-size:.9rem; }

/* Toast */
.toast { position:fixed; bottom:2rem; right:2rem; padding:.72rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .25s ease; border:1.5px solid; box-shadow:var(--shadow); }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light);   color:var(--red);   border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }

@media(max-width:640px) {
  .filter-bar { padding:.6rem .75rem; gap:.5rem; }
  .search-wrap { max-width:100%; }
  .filter-group { display:none; }
}
</style>