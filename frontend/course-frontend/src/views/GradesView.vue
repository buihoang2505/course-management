<template>
  <div class="page">
    <div class="page-header">
      <h1>📊 Điểm số của tôi</h1>
      <p>Kết quả học tập</p>
    </div>

    <div class="grades-shell">
      <!-- GPA SIDEBAR -->
      <div class="gpa-side">
        <div class="gpa-card">
          <template v-if="loading">
            <div class="sk sk-gpa-num"></div>
            <div class="sk sk-gpa-lbl"></div>
            <div class="sk sk-gpa-bar"></div>
            <div class="gpa-stats">
              <div v-for="i in 4" :key="i" class="gs-item">
                <div class="sk sk-gs-n"></div><div class="sk sk-gs-l"></div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="gpa-num">{{ avgScore }}</div>
            <div class="gpa-lbl">Điểm trung bình</div>
            <div class="gpa-bar"><div class="gpa-fill" :style="`width:${(avgScore/10)*100}%`"></div></div>
            <div class="gpa-stats">
              <div class="gs-item"><span class="gs-n">{{ grades.length }}</span><span class="gs-l">Môn học</span></div>
              <div class="gs-item"><span class="gs-n">{{ grades.filter(g=>g.grade?.score>=8.5).length }}</span><span class="gs-l">Giỏi</span></div>
              <div class="gs-item"><span class="gs-n">{{ grades.filter(g=>g.grade?.score>=7&&g.grade?.score<8.5).length }}</span><span class="gs-l">Khá</span></div>
              <div class="gs-item"><span class="gs-n">{{ grades.filter(g=>g.grade?.score<7).length }}</span><span class="gs-l">TB/Yếu</span></div>
            </div>
          </template>
        </div>
      </div>

      <!-- GRADES PANEL -->
      <div class="grades-panel">

        <!-- Filter bar -->
        <div class="filter-bar">
          <div class="search-wrap">
            <svg class="search-icon" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input v-model="keyword" placeholder="Tìm khóa học..." class="search-input"/>
            <button v-if="keyword" class="search-clear" @click="keyword=''">✕</button>
          </div>
          <div class="filter-group">
            <button v-for="f in rankFilters" :key="f.val"
                    :class="['filter-btn', { active: rankFilter===f.val }]"
                    @click="rankFilter=f.val">{{ f.label }}</button>
          </div>
          <select v-model="sortBy" class="sort-select">
            <option value="score_desc">Điểm cao nhất</option>
            <option value="score_asc">Điểm thấp nhất</option>
            <option value="date_desc">Mới nhất</option>
            <option value="title_asc">Tên A→Z</option>
          </select>
        </div>

        <!-- SKELETON -->
        <div v-if="loading" class="grades-grid">
          <div v-for="i in 4" :key="i" class="grade-card sk-card">
            <div class="gc-left">
              <div class="sk sk-gc-icon"></div>
              <div class="gc-info">
                <div class="sk sk-gc-title"></div>
                <div class="sk sk-gc-meta"></div>
                <div class="sk sk-gc-date"></div>
              </div>
            </div>
            <div class="sk sk-gc-score"></div>
          </div>
        </div>

        <!-- EMPTY -->
        <div v-else-if="!grades.length" class="center-state">
          <div class="empty-ico">🎯</div>
          <p>Bạn chưa có điểm nào.</p>
        </div>
        <div v-else-if="!filteredGrades.length" class="center-state">
          <div class="empty-ico">🔍</div>
          <p>Không tìm thấy kết quả phù hợp</p>
          <button class="btn-reset" @click="resetFilters">↺ Xóa bộ lọc</button>
        </div>

        <!-- LIST -->
        <div v-else>
          <div class="grades-grid">
            <div v-for="e in pagedGrades" :key="e.id" class="grade-card">
              <div class="gc-left">
                <div class="gc-icon">{{ icon(e.id) }}</div>
                <div class="gc-info">
                  <div class="gc-title">{{ e.course?.title }}</div>
                  <div class="gc-meta">{{ e.course?.instructor || '—' }}</div>
                  <div v-if="e.grade?.feedback" class="gc-fb">💬 {{ e.grade.feedback }}</div>
                  <div v-else-if="!e.grade" class="gc-fb" style="color:var(--muted)">Chưa có điểm — hoàn thành quiz để nhận điểm</div>
                  <div class="gc-date">📅 {{ fmt(e.enrolledAt) }}</div>
                </div>
              </div>
              <div class="gc-right">
                <div v-if="e.grade?.score != null" :class="['gc-score', sc(e.grade.score)]">{{ e.grade.score.toFixed(1) }}</div>
                <div v-else class="gc-score sc-none">—</div>
                <div class="gc-rank">{{ e.grade?.score != null ? rank(e.grade.score) : 'Chưa có' }}</div>
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
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const allEnrollments = ref([])
const loading = ref(true)
const keyword = ref('')
const rankFilter = ref('ALL')
const sortBy = ref('score_desc')
const page = ref(1)
const PAGE_SIZE = 6

const rankFilters = [
  { val:'ALL',   label:'Tất cả' },
  { val:'exc',   label:'🏆 Giỏi' },
  { val:'good',  label:'👍 Khá' },
  { val:'avg',   label:'📘 TB' },
  { val:'poor',  label:'⚠️ Yếu' },
]

const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']
const icon  = id => icons[id % icons.length]
const fmt   = dt => dt ? new Date(dt).toLocaleDateString('vi-VN') : '—'
const sc    = s => s>=8.5?'sc-exc': s>=7?'sc-good': s>=5?'sc-avg':'sc-poor'
const rank  = s => s>=8.5?'Giỏi': s>=7?'Khá': s>=5?'Trung bình':'Yếu'
const rankKey = s => s>=8.5?'exc': s>=7?'good': s>=5?'avg':'poor'

const grades = computed(() => allEnrollments.value.filter(e => e.grade != null))
const avgScore = computed(() => {
  if (!grades.value.length) return '0.0'
  return (grades.value.reduce((s,e)=>s+e.grade.score,0)/grades.value.length).toFixed(1)
})

function resetFilters() { keyword.value=''; rankFilter.value='ALL'; sortBy.value='score_desc'; page.value=1 }
watch([keyword, rankFilter, sortBy], () => { page.value=1 })

const filteredGrades = computed(() => {
  let list = [...grades.value]
  if (rankFilter.value !== 'ALL') list = list.filter(e => rankKey(e.grade.score) === rankFilter.value)
  const kw = keyword.value.trim().toLowerCase()
  if (kw) list = list.filter(e =>
      e.course?.title?.toLowerCase().includes(kw) ||
      e.course?.instructor?.toLowerCase().includes(kw)
  )
  if (sortBy.value === 'score_desc') list.sort((a,b) => b.grade.score - a.grade.score)
  if (sortBy.value === 'score_asc')  list.sort((a,b) => a.grade.score - b.grade.score)
  if (sortBy.value === 'date_desc')  list.sort((a,b) => new Date(b.enrolledAt)-new Date(a.enrolledAt))
  if (sortBy.value === 'title_asc')  list.sort((a,b) => (a.course?.title||'').localeCompare(b.course?.title||''))
  return list
})

const totalPages = computed(() => Math.ceil(filteredGrades.value.length / PAGE_SIZE))
const pagedGrades = computed(() => {
  const start = (page.value-1)*PAGE_SIZE
  return filteredGrades.value.slice(start, start+PAGE_SIZE)
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

async function load() {
  loading.value=true
  try { allEnrollments.value=(await api.get(`/enrollments/user/${auth.user?.id}`)).data }
  catch(e){ console.error(e) }
  finally { loading.value=false }
}
onMounted(load)
</script>

<style scoped>
.page { padding:2rem 2.5rem; max-width:1400px; margin:0 auto; }
.page-header { margin-bottom:1.2rem; }
.page-header h1 { font-size:1.6rem; font-weight:800; margin-bottom:.3rem; }
.page-header p  { color:var(--muted); font-size:.85rem; }

.sk { background:linear-gradient(90deg,#e2e8f0 25%,#f1f5f9 50%,#e2e8f0 75%); background-size:200% 100%; animation:shimmer 1.4s infinite; border-radius:7px; }
@keyframes shimmer { to { background-position:-200% 0; } }
.sk-gpa-num { width:80px; height:48px; border-radius:10px; margin:0 auto .5rem; background:rgba(255,255,255,.25); animation:shimmer 1.4s infinite; }
.sk-gpa-lbl { width:120px; height:14px; border-radius:6px; margin:0 auto .8rem; background:rgba(255,255,255,.2); animation:shimmer 1.4s infinite; }
.sk-gpa-bar { width:100%; height:5px; border-radius:100px; margin-bottom:1rem; background:rgba(255,255,255,.25); animation:shimmer 1.4s infinite; }
.sk-gs-n    { width:28px; height:22px; border-radius:5px; margin:0 auto .25rem; background:rgba(255,255,255,.2); animation:shimmer 1.4s infinite; }
.sk-gs-l    { width:36px; height:10px; border-radius:4px; margin:0 auto; background:rgba(255,255,255,.15); animation:shimmer 1.4s infinite; }
.sk-gc-icon  { width:32px; height:32px; border-radius:8px; flex-shrink:0; }
.sk-gc-title { width:140px; height:14px; margin-bottom:.3rem; }
.sk-gc-meta  { width:90px; height:11px; margin-bottom:.25rem; }
.sk-gc-date  { width:70px; height:10px; }
.sk-gc-score { width:52px; height:52px; border-radius:10px; flex-shrink:0; }
.sk-card { pointer-events:none; }

.grades-shell { display:grid; grid-template-columns:240px 1fr; gap:1.2rem; align-items:start; }
@media(max-width:860px) { .grades-shell { grid-template-columns:1fr; } }

.gpa-side { position:sticky; top:78px; }
.gpa-card { background:linear-gradient(145deg,var(--accent),#7c3aed); border-radius:16px; padding:1.8rem 1.4rem; color:#fff; text-align:center; box-shadow:0 8px 24px rgba(37,99,235,.3); display:flex; flex-direction:column; justify-content:center; gap:.8rem; }
.gpa-num  { font-size:3rem; font-weight:800; line-height:1; }
.gpa-lbl  { font-size:.82rem; opacity:.85; }
.gpa-bar  { height:5px; background:rgba(255,255,255,.25); border-radius:100px; overflow:hidden; }
.gpa-fill { height:100%; background:rgba(255,255,255,.8); border-radius:100px; transition:width .8s ease; }
.gpa-stats { display:grid; grid-template-columns:1fr 1fr; gap:.5rem; margin-top:.3rem; }
.gs-item { background:rgba(255,255,255,.12); border-radius:8px; padding:.5rem; }
.gs-n { display:block; font-size:1.2rem; font-weight:800; }
.gs-l { font-size:.68rem; opacity:.8; }
@media(max-width:860px) { .gpa-side { position:static; } }

/* Filter bar */
.filter-bar { display:flex; align-items:center; gap:.5rem; flex-wrap:wrap; margin-bottom:.9rem; background:var(--surface2); border:1.5px solid var(--border); border-radius:10px; padding:.6rem .9rem; }
.search-wrap { position:relative; flex:1; min-width:160px; max-width:240px; }
.search-icon { position:absolute; left:.7rem; top:50%; transform:translateY(-50%); color:var(--muted); pointer-events:none; }
.search-input { width:100%; padding:.4rem 1.8rem .4rem 2rem; background:var(--surface); border:1.5px solid var(--border); border-radius:7px; color:var(--text); font-size:.81rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; transition:border-color .15s; box-sizing:border-box; }
.search-input:focus { border-color:var(--accent); }
.search-clear { position:absolute; right:.45rem; top:50%; transform:translateY(-50%); background:none; border:none; color:var(--muted); cursor:pointer; font-size:.72rem; padding:0; }
.filter-group { display:flex; gap:.25rem; flex-wrap:wrap; }
.filter-btn { padding:.3rem .6rem; border-radius:6px; border:1.5px solid var(--border); background:var(--surface); color:var(--muted); font-size:.72rem; font-weight:600; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; }
.filter-btn.active { background:var(--accent); color:#fff; border-color:var(--accent); }
.sort-select { padding:.33rem .7rem; border-radius:7px; border:1.5px solid var(--border); background:var(--surface); color:var(--text); font-size:.77rem; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; outline:none; }

.grades-panel { background:var(--surface); border:1.5px solid var(--border); border-radius:14px; padding:1.1rem; box-shadow:var(--shadow-sm); }
.center-state { display:flex; flex-direction:column; align-items:center; justify-content:center; min-height:250px; color:var(--muted); gap:.6rem; }
.empty-ico { font-size:2.5rem; opacity:.4; }
.center-state p { font-size:.9rem; }
.btn-reset { margin-top:.5rem; padding:.35rem .8rem; border-radius:7px; border:1.5px solid var(--border); background:none; color:var(--muted); font-size:.77rem; font-weight:600; cursor:pointer; }
.btn-reset:hover { border-color:var(--red); color:var(--red); }

.grades-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:.8rem; }
@media(max-width:1100px) { .grades-grid { grid-template-columns:1fr; } }

.grade-card { background:var(--surface2); border:1.5px solid var(--border); border-radius:12px; padding:1rem 1.1rem; display:flex; justify-content:space-between; align-items:center; gap:.8rem; min-height:100px; transition:border-color .15s, transform .15s; }
.grade-card:hover { border-color:var(--accent); transform:translateY(-2px); }
.gc-left { display:flex; gap:.75rem; flex:1; min-width:0; align-items:flex-start; }
.gc-icon { font-size:1.6rem; flex-shrink:0; line-height:1; }
.gc-info { flex:1; min-width:0; display:flex; flex-direction:column; gap:.15rem; }
.gc-title { font-size:.87rem; font-weight:700; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.gc-meta  { font-size:.74rem; color:var(--muted); }
.gc-fb    { font-size:.73rem; color:var(--text2); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.gc-date  { font-size:.71rem; color:var(--muted); }
.gc-right { display:flex; flex-direction:column; align-items:center; flex-shrink:0; gap:.25rem; }
.sc-none  { background:var(--surface2); color:var(--muted); }
.gc-score { font-size:1.5rem; font-weight:800; padding:.35rem .6rem; border-radius:9px; min-width:56px; text-align:center; }
.gc-rank  { font-size:.68rem; color:var(--muted); font-weight:600; }
.sc-exc  { background:var(--green-light);  color:var(--green); }
.sc-good { background:var(--accent-light); color:var(--accent); }
.sc-avg  { background:var(--yellow-light); color:var(--yellow); }
.sc-poor { background:var(--red-light);    color:var(--red); }

.pagination { display:flex; justify-content:center; align-items:center; gap:.3rem; margin-top:1rem; flex-wrap:wrap; }
.pg-btn { min-width:34px; height:34px; padding:0 .4rem; border-radius:8px; border:1.5px solid var(--border); background:var(--surface); color:var(--text2); font-size:.8rem; font-weight:600; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; display:flex; align-items:center; justify-content:center; }
.pg-btn:hover:not(:disabled):not(.ellipsis) { border-color:var(--accent); color:var(--accent); background:var(--accent-light); }
.pg-btn.active { background:var(--accent); color:#fff; border-color:var(--accent); }
.pg-btn:disabled { opacity:.4; cursor:not-allowed; }
.pg-btn.ellipsis { cursor:default; border-color:transparent; background:none; }
</style>