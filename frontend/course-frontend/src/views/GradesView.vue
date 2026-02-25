<template>
  <div class="page">
    <div class="page-header">
      <h1>📊 Điểm số của tôi</h1>
      <p>Kết quả học tập</p>
    </div>

    <div v-if="loading" class="loading">⏳ Đang tải...</div>

    <div v-else-if="!grades.length" class="empty-state">
      <div class="empty-icon">🎯</div>
      <p>Bạn chưa có điểm nào.</p>
    </div>

    <div v-else class="grades-layout">
      <!-- GPA card -->
      <div class="gpa-card">
        <div class="gpa-num">{{ avgScore }}</div>
        <div class="gpa-lbl">Điểm trung bình</div>
        <div class="gpa-bar">
          <div class="gpa-fill" :style="`width:${(avgScore/10)*100}%`"></div>
        </div>
        <div class="gpa-stats">
          <div class="gs-item"><span class="gs-n">{{ grades.length }}</span><span class="gs-l">Môn học</span></div>
          <div class="gs-item"><span class="gs-n">{{ grades.filter(g=>g.grade.score>=8.5).length }}</span><span class="gs-l">Giỏi</span></div>
          <div class="gs-item"><span class="gs-n">{{ grades.filter(g=>g.grade.score>=7&&g.grade.score<8.5).length }}</span><span class="gs-l">Khá</span></div>
          <div class="gs-item"><span class="gs-n">{{ grades.filter(g=>g.grade.score<7).length }}</span><span class="gs-l">TB/Yếu</span></div>
        </div>
      </div>

      <!-- Grade cards grid -->
      <div class="grades-grid">
        <div v-for="e in grades" :key="e.id" class="grade-card">
          <div class="gc-left">
            <div class="gc-icon">{{ icon(e.id) }}</div>
            <div class="gc-info">
              <div class="gc-title">{{ e.course?.title }}</div>
              <div class="gc-instructor">{{ e.course?.instructor || '—' }}</div>
              <div v-if="e.grade?.feedback" class="gc-feedback">💬 {{ e.grade.feedback }}</div>
              <div class="gc-date">📅 {{ fmt(e.grade?.gradedAt || e.enrolledAt) }}</div>
            </div>
          </div>
          <div class="gc-right">
            <div :class="['gc-score', sc(e.grade.score)]">{{ e.grade.score?.toFixed(1) }}</div>
            <div class="gc-rank">{{ rank(e.grade.score) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const allEnrollments = ref([])
const loading = ref(true)

const grades = computed(() => allEnrollments.value.filter(e => e.grade != null))
const avgScore = computed(() => {
  if (!grades.value.length) return '0.0'
  return (grades.value.reduce((s,e) => s + e.grade.score, 0) / grades.value.length).toFixed(1)
})

const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']
const icon = id => icons[id % icons.length]
const fmt  = dt => dt ? new Date(dt).toLocaleDateString('vi-VN') : '—'
const sc   = s => s>=8.5?'sc-exc': s>=7?'sc-good': s>=5?'sc-avg':'sc-poor'
const rank = s => s>=8.5?'Giỏi': s>=7?'Khá': s>=5?'Trung bình':'Yếu'

async function load() {
  loading.value = true
  try { allEnrollments.value = (await api.get(`/enrollments/user/${auth.user?.id}`)).data }
  catch(e) { console.error(e) }
  finally { loading.value = false }
}
onMounted(load)
</script>

<style scoped>
.page { padding: 2rem 2.5rem; max-width: 1400px; margin: 0 auto; }
.page-header { margin-bottom: 1.8rem; }
.page-header h1 { font-size: 1.6rem; font-weight: 800; margin-bottom: .3rem; }
.page-header p  { color: var(--muted); font-size: .85rem; }

.grades-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 1.2rem;
  align-items: start;
}
@media (max-width: 900px) { .grades-layout { grid-template-columns: 1fr; } }

/* GPA card */
.gpa-card {
  background: linear-gradient(135deg, var(--accent), #7c3aed);
  border-radius: 16px;
  padding: 2rem 1.5rem;
  color: #fff;
  text-align: center;
  position: sticky;
  top: 78px;
  box-shadow: 0 8px 24px rgba(37,99,235,.3);
}
.gpa-num  { font-size: 3rem; font-weight: 800; line-height: 1; margin-bottom: .4rem; }
.gpa-lbl  { font-size: .85rem; opacity: .85; margin-bottom: 1.2rem; }
.gpa-bar  { height: 6px; background: rgba(255,255,255,.25); border-radius: 100px; overflow: hidden; margin-bottom: 1.5rem; }
.gpa-fill { height: 100%; background: rgba(255,255,255,.8); border-radius: 100px; transition: width .8s; }
.gpa-stats { display: grid; grid-template-columns: repeat(2,1fr); gap: .6rem; }
.gs-item { background: rgba(255,255,255,.12); border-radius: 8px; padding: .5rem; }
.gs-n { display: block; font-size: 1.2rem; font-weight: 800; }
.gs-l { font-size: .7rem; opacity: .8; }

/* Grade grid – 2 columns */
.grades-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: .9rem;
}
@media (max-width: 1100px) { .grades-grid { grid-template-columns: 1fr; } }

.grade-card {
  background: var(--surface);
  border: 1.5px solid var(--border);
  border-radius: 14px;
  padding: 1.2rem 1.4rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  box-shadow: var(--shadow-sm);
  transition: border-color .15s, transform .15s;
}
.grade-card:hover { border-color: var(--accent); transform: translateY(-2px); }

.gc-left { display: flex; gap: .85rem; flex: 1; min-width: 0; align-items: flex-start; }
.gc-icon { font-size: 1.7rem; flex-shrink: 0; line-height: 1; }
.gc-info { flex: 1; min-width: 0; }
.gc-title      { font-size: .9rem; font-weight: 700; margin-bottom: .15rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.gc-instructor { font-size: .77rem; color: var(--muted); margin-bottom: .2rem; }
.gc-feedback   { font-size: .75rem; color: var(--text2); margin-bottom: .15rem; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.gc-date       { font-size: .73rem; color: var(--muted); }

.gc-right  { display: flex; flex-direction: column; align-items: center; flex-shrink: 0; }
.gc-score  { font-size: 1.6rem; font-weight: 800; padding: .4rem .7rem; border-radius: 10px; min-width: 60px; text-align: center; }
.gc-rank   { font-size: .7rem; color: var(--muted); margin-top: .3rem; font-weight: 600; }

.sc-exc  { background: var(--green-light);  color: var(--green); }
.sc-good { background: var(--accent-light); color: var(--accent); }
.sc-avg  { background: var(--yellow-light); color: var(--yellow); }
.sc-poor { background: var(--red-light);    color: var(--red); }

.loading     { text-align: center; padding: 3rem; color: var(--muted); }
.empty-state { text-align: center; padding: 4rem; color: var(--muted); }
.empty-icon  { font-size: 2.5rem; margin-bottom: .8rem; opacity: .4; }
</style>