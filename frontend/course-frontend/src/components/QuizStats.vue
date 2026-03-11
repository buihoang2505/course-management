<template>
  <div class="stats-wrap">

    <!-- Loading -->
    <div v-if="loading" class="stats-loading">
      <div class="sk-block" v-for="i in 3" :key="i"></div>
    </div>

    <template v-else-if="stats">
      <!-- ── KPI ROW ──────────────────────────────────── -->
      <div class="kpi-row">
        <div class="kpi-card">
          <div class="kpi-icon">📋</div>
          <div class="kpi-val">{{ stats.totalAttempts }}</div>
          <div class="kpi-label">Lượt làm</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon">👥</div>
          <div class="kpi-val">{{ stats.uniqueStudents }}</div>
          <div class="kpi-label">Học sinh</div>
        </div>
        <div class="kpi-card kpi-accent">
          <div class="kpi-icon">📊</div>
          <div class="kpi-val">{{ stats.averageScore }}</div>
          <div class="kpi-label">Điểm TB</div>
        </div>
        <div class="kpi-card" :class="stats.passRate >= 60 ? 'kpi-green' : 'kpi-red'">
          <div class="kpi-icon">🎯</div>
          <div class="kpi-val">{{ stats.passRate }}%</div>
          <div class="kpi-label">Tỉ lệ đạt</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon">⬆️</div>
          <div class="kpi-val">{{ stats.highestScore }}</div>
          <div class="kpi-label">Cao nhất</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-icon">⬇️</div>
          <div class="kpi-val">{{ stats.lowestScore }}</div>
          <div class="kpi-label">Thấp nhất</div>
        </div>
      </div>

      <!-- ── PHÂN BỐ ĐIỂM ─────────────────────────────── -->
      <div class="stat-section">
        <div class="ss-hd">📈 Phân bố điểm số</div>
        <div class="dist-chart">
          <div v-for="(count, i) in stats.scoreDistribution" :key="i" class="dist-col">
            <div class="dist-bar-wrap">
              <span v-if="count > 0" class="dist-count">{{ count }}</span>
              <div
                  class="dist-bar"
                  :style="`height: ${maxDist > 0 ? (count/maxDist)*100 : 0}%`"
                  :class="i >= 6 ? 'bar-pass' : 'bar-fail'"
              ></div>
            </div>
            <div class="dist-label">{{ i*10 }}–{{ i*10+10 }}</div>
          </div>
        </div>
        <div class="dist-legend">
          <span class="legend-fail">■ Chưa đạt (&lt;60)</span>
          <span class="legend-pass">■ Đạt (≥60)</span>
        </div>
      </div>

      <!-- ── TỈ LỆ ĐÚNG TỪNG CÂU ─────────────────────── -->
      <div class="stat-section">
        <div class="ss-hd">🎯 Tỉ lệ đúng từng câu hỏi</div>
        <div class="q-stats-list">
          <div v-for="(qs, i) in stats.questionStats" :key="qs.questionId" class="qs-row">
            <div class="qs-num">{{ i + 1 }}</div>
            <div class="qs-content">
              <div class="qs-text">{{ qs.questionContent }}</div>
              <div class="qs-bar-wrap">
                <div class="qs-bar-track">
                  <div
                      class="qs-bar-fill"
                      :style="`width: ${qs.correctRate.toFixed(0)}%`"
                      :class="qs.correctRate >= 70 ? 'fill-good' : qs.correctRate >= 40 ? 'fill-mid' : 'fill-bad'"
                  ></div>
                </div>
                <!-- FIX: correctRate giờ là 0–100, bỏ nhân *100 -->
                <span class="qs-pct">{{ qs.correctRate.toFixed(0) }}%</span>
                <span class="qs-detail">{{ qs.correctCount }}/{{ qs.totalAnswered }}</span>
              </div>
            </div>
            <!-- FIX: so sánh với 70/40 thay vì 0.7/0.4 -->
            <div :class="['qs-diff', qs.correctRate >= 70 ? 'diff-easy' : qs.correctRate >= 40 ? 'diff-mid' : 'diff-hard']">
              {{ qs.correctRate >= 70 ? 'Dễ' : qs.correctRate >= 40 ? 'TB' : 'Khó' }}
            </div>
          </div>

          <div v-if="!stats.questionStats?.length" class="empty-stat">
            Chưa có học sinh nào làm bài quiz này.
          </div>
        </div>
      </div>
    </template>

    <div v-else class="empty-stat">Không tìm thấy thống kê cho quiz này.</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../services/api'

const props = defineProps({
  quizId: { type: Number, required: true }
})

const stats   = ref(null)
const loading = ref(true)

const maxDist = computed(() =>
    stats.value ? Math.max(...stats.value.scoreDistribution, 1) : 1
)

onMounted(async () => {
  try {
    const r = await api.get(`/quizzes/${props.quizId}/stats`)
    stats.value = r.data
  } catch(e) {
    console.error('Lỗi tải stats:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stats-wrap { display:flex; flex-direction:column; gap:1.2rem; padding:.5rem 0; }

/* ── SKELETON ── */
.stats-loading { display:flex; flex-direction:column; gap:1rem; }
.sk-block { height:120px; border-radius:12px; background:linear-gradient(90deg,#e2e8f0 25%,#f1f5f9 50%,#e2e8f0 75%); background-size:200% 100%; animation:shimmer 1.4s infinite; }
@keyframes shimmer { to{background-position:-200% 0} }

/* ── KPI ROW ── */
.kpi-row { display:grid; grid-template-columns:repeat(6,1fr); gap:.7rem; }
@media(max-width:700px) { .kpi-row { grid-template-columns:repeat(3,1fr); } }

.kpi-card {
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:12px; padding:.9rem .7rem; text-align:center;
  transition:all .15s;
}
.kpi-card:hover { transform:translateY(-2px); box-shadow:var(--shadow); }
.kpi-accent { background:var(--accent-light); border-color:#bfdbfe; }
.kpi-green  { background:var(--green-light);  border-color:#a7f3d0; }
.kpi-red    { background:var(--red-light);    border-color:#fca5a5; }
.kpi-icon   { font-size:1.2rem; margin-bottom:.3rem; }
.kpi-val    { font-size:1.5rem; font-weight:900; color:var(--text); line-height:1; margin-bottom:.25rem; }
.kpi-label  { font-size:.7rem; color:var(--muted); font-weight:600; }

/* ── SECTION ── */
.stat-section {
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:12px; overflow:hidden;
}
.ss-hd {
  font-size:.85rem; font-weight:700; padding:.75rem 1rem;
  border-bottom:1.5px solid var(--border); background:var(--surface2);
}

/* ── DISTRIBUTION CHART ── */
.dist-chart {
  display:flex; align-items:flex-end; gap:.4rem;
  padding:1rem 1rem .5rem; height:160px;
}
.dist-col { flex:1; display:flex; flex-direction:column; align-items:center; gap:.3rem; height:100%; }
.dist-bar-wrap { flex:1; width:100%; display:flex; flex-direction:column; justify-content:flex-end; align-items:center; position:relative; }
.dist-count { font-size:.65rem; font-weight:700; color:var(--text2); position:absolute; top:-18px; }
.dist-bar { width:100%; border-radius:4px 4px 0 0; min-height:2px; transition:height .6s ease; }
.bar-pass { background:linear-gradient(180deg,#34d399,var(--green)); }
.bar-fail { background:linear-gradient(180deg,#f87171,var(--red)); }
.dist-label { font-size:.6rem; color:var(--muted); white-space:nowrap; }
.dist-legend { display:flex; gap:1rem; padding:.5rem 1rem .75rem; justify-content:center; }
.legend-fail { font-size:.72rem; color:var(--red);   font-weight:600; }
.legend-pass { font-size:.72rem; color:var(--green); font-weight:600; }

/* ── QUESTION STATS ── */
.q-stats-list { padding:.75rem 1rem; display:flex; flex-direction:column; gap:.65rem; }
.qs-row { display:flex; align-items:center; gap:.85rem; }
.qs-num {
  width:24px; height:24px; border-radius:7px; flex-shrink:0;
  background:var(--accent-light); color:var(--accent);
  font-size:.72rem; font-weight:800;
  display:flex; align-items:center; justify-content:center;
}
.qs-content { flex:1; min-width:0; }
.qs-text  { font-size:.8rem; color:var(--text2); font-weight:500; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin-bottom:.3rem; }
.qs-bar-wrap { display:flex; align-items:center; gap:.6rem; }
.qs-bar-track { flex:1; height:7px; background:var(--border); border-radius:100px; overflow:hidden; }
.qs-bar-fill  { height:100%; border-radius:100px; transition:width .7s ease; }
.fill-good { background:linear-gradient(90deg,var(--green),#34d399); }
.fill-mid  { background:linear-gradient(90deg,var(--yellow),#fbbf24); }
.fill-bad  { background:linear-gradient(90deg,var(--red),#f87171); }
.qs-pct    { font-size:.75rem; font-weight:800; color:var(--text2); width:32px; text-align:right; flex-shrink:0; }
.qs-detail { font-size:.68rem; color:var(--muted); flex-shrink:0; }
.qs-diff   { font-size:.65rem; font-weight:800; padding:.15rem .45rem; border-radius:100px; flex-shrink:0; }
.diff-easy { background:var(--green-light); color:var(--green); }
.diff-mid  { background:var(--yellow-light); color:var(--yellow); }
.diff-hard { background:var(--red-light);   color:var(--red); }

.empty-stat { text-align:center; padding:2rem; color:var(--muted); font-size:.85rem; }
</style>