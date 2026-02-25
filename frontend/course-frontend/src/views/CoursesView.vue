<template>
  <div class="page">
    <div class="courses-header">
      <div>
        <h1>Khóa học</h1>
        <p>Khám phá và đăng ký các khóa học</p>
      </div>
      <div class="search-wrap">
        <svg class="search-icon" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input v-model="keyword" @input="searchCourses" placeholder="Tìm kiếm khóa học..." class="search-input"/>
      </div>
    </div>

    <div v-if="loading" class="loading">⏳ Đang tải...</div>
    <div v-else-if="!courses.length" class="empty-state">
      <div class="empty-icon">📚</div><p>Không tìm thấy khóa học nào</p>
    </div>

    <div v-else class="courses-grid">
      <div v-for="(c, i) in courses" :key="c.id" class="course-card"
           :style="`animation-delay:${i*0.05}s`">
        <div class="card-top">
          <div class="course-emoji">{{ icons[i % icons.length] }}</div>
          <span class="credits-pill">{{ c.credits }} TC</span>
        </div>
        <h3>{{ c.title }}</h3>
        <p class="desc">{{ c.description || 'Chưa có mô tả.' }}</p>
        <div class="instructor-row">
          <span class="dot"></span>{{ c.instructor || 'Chưa xác định' }}
        </div>
        <div class="card-footer">
          <router-link :to="`/courses/${c.id}`" class="btn-outline">Chi tiết</router-link>
          <button @click="enrollCourse(c.id)" class="btn-solid"
                  :disabled="enrolling === c.id">
            {{ enrolling === c.id ? '...' : '+ Đăng ký' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="toast" :class="['toast', toast.type]">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const courses = ref([])
const loading = ref(true)
const keyword = ref('')
const enrolling = ref(null)
const toast = ref(null)
const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']

function showToast(msg, type='success') {
  toast.value = { msg, type }
  setTimeout(() => toast.value = null, 3000)
}
async function fetchCourses() {
  loading.value = true
  try { courses.value = (await api.get('/courses/active')).data }
  catch { showToast('Không thể tải danh sách!', 'error') }
  finally { loading.value = false }
}
async function searchCourses() {
  if (!keyword.value.trim()) return fetchCourses()
  try { courses.value = (await api.get(`/courses/search?keyword=${keyword.value}`)).data }
  catch {}
}
async function enrollCourse(id) {
  enrolling.value = id
  try {
    await api.post(`/enrollments/enroll?userId=${auth.user?.id}&courseId=${id}`)
    showToast('🎉 Đăng ký thành công!')
  } catch(e) {
    showToast(e.response?.data?.error || 'Đăng ký thất bại!', 'error')
  } finally { enrolling.value = null }
}
onMounted(fetchCourses)
</script>

<style scoped>
.page { padding: 2rem 2.5rem; max-width: 1400px; margin: 0 auto; }

.courses-header {
  display: flex; justify-content: space-between;
  align-items: center; margin-bottom: 1.8rem;
  gap: 1rem; flex-wrap: wrap;
}
.courses-header h1 { font-size: 1.6rem; font-weight: 800; margin-bottom: .3rem; }
.courses-header p  { color: var(--muted); font-size: .85rem; }

.search-wrap { position: relative; }
.search-icon { position: absolute; left: .85rem; top: 50%; transform: translateY(-50%); color: var(--muted); pointer-events: none; }
.search-input {
  padding: .58rem 1rem .58rem 2.4rem;
  background: var(--surface); border: 1.5px solid var(--border);
  border-radius: 10px; color: var(--text);
  font-size: .85rem; width: 280px; outline: none;
  font-family: 'Plus Jakarta Sans', sans-serif;
  box-shadow: var(--shadow-sm);
  transition: border-color .18s, box-shadow .18s;
}
.search-input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px var(--accent-light); }
.search-input::placeholder { color: var(--muted); }

/* ── 3-column grid ── */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.1rem;
}
@media (max-width: 1100px) { .courses-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 650px)  { .courses-grid { grid-template-columns: 1fr; } }

.course-card {
  background: var(--surface);
  border: 1.5px solid var(--border);
  border-radius: 14px;
  padding: 1.4rem;
  display: flex; flex-direction: column; gap: .7rem;
  box-shadow: var(--shadow-sm);
  animation: fadeUp .4s ease both;
  transition: border-color .2s, transform .2s, box-shadow .2s;
}
.course-card:hover {
  border-color: var(--accent);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(37,99,235,.12);
}
@keyframes fadeUp { from { opacity:0; transform:translateY(12px); } to { opacity:1; transform:translateY(0); } }

.card-top { display: flex; justify-content: space-between; align-items: flex-start; }
.course-emoji { font-size: 2rem; line-height: 1; }
.credits-pill {
  font-size: .68rem; font-weight: 700;
  padding: .22rem .6rem; border-radius: 100px;
  background: var(--accent-light); color: var(--accent);
  border: 1px solid #bfdbfe;
}

.course-card h3 { font-size: .92rem; font-weight: 700; line-height: 1.4; color: var(--text); }
.desc { font-size: .79rem; color: var(--muted); line-height: 1.6; flex: 1; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.instructor-row { display: flex; align-items: center; gap: .5rem; font-size: .77rem; color: var(--muted); }
.dot { width: 7px; height: 7px; border-radius: 50%; background: var(--green); flex-shrink: 0; }

.card-footer { display: flex; gap: .6rem; margin-top: .2rem; }
.btn-outline {
  flex: 1; padding: .44rem .8rem; font-size: .79rem; font-weight: 600;
  background: transparent; color: var(--accent);
  border: 1.5px solid #bfdbfe; border-radius: 8px;
  cursor: pointer; text-align: center; text-decoration: none;
  transition: all .18s; display: flex; align-items: center; justify-content: center;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.btn-outline:hover { background: var(--accent-light); border-color: var(--accent); }
.btn-solid {
  flex: 1; padding: .44rem .8rem; font-size: .79rem; font-weight: 600;
  background: var(--accent); color: white;
  border: none; border-radius: 8px; cursor: pointer;
  transition: all .18s; font-family: 'Plus Jakarta Sans', sans-serif;
}
.btn-solid:hover:not(:disabled) { background: var(--accent-dark); box-shadow: 0 4px 10px rgba(37,99,235,.25); }
.btn-solid:disabled { opacity: .5; cursor: not-allowed; }

.loading { text-align:center; padding:3rem; color:var(--muted); }
.empty-state { text-align:center; padding:4rem; color:var(--muted); }
.empty-icon { font-size:2.5rem; margin-bottom:.8rem; opacity:.4; }

.toast { position:fixed; bottom:2rem; right:2rem; padding:.75rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .3s ease; border:1.5px solid; box-shadow:var(--shadow); }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light); color:var(--red); border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }
</style>