<template>
  <div class="page">
    <div class="page-header">
      <div class="header-left">
        <h1>🏆 Bảng xếp hạng</h1>
        <p>Top học viên xuất sắc nhất hệ thống</p>
      </div>
      <div v-if="myRank" class="my-rank-badge">
        <span class="mr-label">Vị trí của bạn</span>
        <span class="mr-rank">#{{ myRank.rank }}</span>
        <span class="mr-pts">{{ myRank.totalPoints }} điểm</span>
      </div>
    </div>

    <!-- Skeleton -->
    <template v-if="loading">
      <!-- Podium skeleton -->
      <div class="podium-wrap">
        <div v-for="i in 3" :key="i" class="sk-podium-item">
          <div class="sk sk-av-lg"></div>
          <div class="sk sk-name"></div>
          <div class="sk sk-pts"></div>
        </div>
      </div>
      <div class="list-wrap">
        <div v-for="i in 7" :key="i" class="sk-row">
          <div class="sk sk-rank-num"></div>
          <div class="sk sk-av-sm"></div>
          <div class="sk sk-name-sm"></div>
          <div class="sk sk-score"></div>
        </div>
      </div>
    </template>

    <template v-else-if="sorted.length">
      <!-- ── PODIUM top 3 ── -->
      <div class="podium-wrap">
        <!-- 2nd -->
        <div class="podium-item p2" v-if="sorted[1]">
          <div class="podium-avatar-wrap">
            <img v-if="sorted[1].avatar" :src="sorted[1].avatar" class="podium-av" />
            <span v-else class="podium-av-init">{{ initials(sorted[1]) }}</span>
            <span class="podium-medal">🥈</span>
          </div>
          <div class="podium-name">{{ displayName(sorted[1]) }}</div>
          <div class="podium-pts">{{ sorted[1].totalPoints }} pts</div>
          <div class="podium-block pb2"></div>
        </div>
        <!-- 1st -->
        <div class="podium-item p1" v-if="sorted[0]">
          <div class="podium-crown">👑</div>
          <div class="podium-avatar-wrap">
            <img v-if="sorted[0].avatar" :src="sorted[0].avatar" class="podium-av" />
            <span v-else class="podium-av-init">{{ initials(sorted[0]) }}</span>
            <span class="podium-medal">🥇</span>
          </div>
          <div class="podium-name">{{ displayName(sorted[0]) }}</div>
          <div class="podium-pts">{{ sorted[0].totalPoints }} pts</div>
          <div class="podium-block pb1"></div>
        </div>
        <!-- 3rd -->
        <div class="podium-item p3" v-if="sorted[2]">
          <div class="podium-avatar-wrap">
            <img v-if="sorted[2].avatar" :src="sorted[2].avatar" class="podium-av" />
            <span v-else class="podium-av-init">{{ initials(sorted[2]) }}</span>
            <span class="podium-medal">🥉</span>
          </div>
          <div class="podium-name">{{ displayName(sorted[2]) }}</div>
          <div class="podium-pts">{{ sorted[2].totalPoints }} pts</div>
          <div class="podium-block pb3"></div>
        </div>
      </div>

      <!-- ── Danh sách từ #4 trở xuống ── -->
      <div class="list-wrap">
        <div v-for="u in sorted.slice(3)" :key="u.userId"
             :class="['lb-row', { 'is-me': u.userId === auth.user?.id }]">
          <div class="lb-rank">
            <span class="rank-num">#{{ u.rank }}</span>
          </div>
          <div class="lb-avatar">
            <img v-if="u.avatar" :src="u.avatar" class="lb-av-img" />
            <span v-else class="lb-av-init">{{ initials(u) }}</span>
          </div>
          <div class="lb-info">
            <div class="lb-name">
              {{ displayName(u) }}
              <span v-if="u.userId === auth.user?.id" class="you-chip">Bạn</span>
            </div>
            <div class="lb-meta">
              <span>📚 {{ u.totalCourses }} khóa</span>
              <span class="dot">·</span>
              <span>✅ {{ u.completedCourses }} hoàn thành</span>
              <span class="dot">·</span>
              <span>📖 {{ u.completedLessons }} bài</span>
            </div>
          </div>
          <div class="lb-scores">
            <div class="lb-main-score" :class="scoreClass(u.avgScore)">
              {{ u.avgScore > 0 ? u.avgScore.toFixed(1) : '—' }}
            </div>
            <div class="lb-pts">{{ u.totalPoints }} pts</div>
          </div>
          <div class="lb-progress-col">
            <div class="lb-prog-track">
              <div class="lb-prog-fill"
                   :style="`width:${sorted[0] ? Math.min(u.totalPoints/sorted[0].totalPoints*100,100) : 0}%`">
              </div>
            </div>
          </div>
        </div>

        <!-- My rank nếu ngoài top -->
        <div v-if="myRank && myRank.rank > sorted.length" class="lb-row is-me lb-my-sep">
          <div class="lb-rank"><span class="rank-num">#{{ myRank.rank }}</span></div>
          <div class="lb-avatar">
            <img v-if="myRank.avatar" :src="myRank.avatar" class="lb-av-img" />
            <span v-else class="lb-av-init">{{ initials(myRank) }}</span>
          </div>
          <div class="lb-info">
            <div class="lb-name">{{ displayName(myRank) }} <span class="you-chip">Bạn</span></div>
            <div class="lb-meta">
              <span>📚 {{ myRank.totalCourses }} khóa</span>
              <span class="dot">·</span>
              <span>✅ {{ myRank.completedCourses }} hoàn thành</span>
            </div>
          </div>
          <div class="lb-scores">
            <div class="lb-main-score" :class="scoreClass(myRank.avgScore)">
              {{ myRank.avgScore > 0 ? myRank.avgScore.toFixed(1) : '—' }}
            </div>
            <div class="lb-pts">{{ myRank.totalPoints }} pts</div>
          </div>
        </div>
      </div>

      <!-- Chú thích điểm -->
      <div class="pts-note">
        💡 Điểm tổng hợp = Điểm TB × 5 + Khóa hoàn thành × 10 + Số bài đã học
      </div>
    </template>

    <!-- Empty -->
    <div v-else class="center-state">
      <div class="empty-ico">🏆</div>
      <p>Chưa có dữ liệu xếp hạng.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth    = useAuthStore()
const data    = ref([])
const myRank  = ref(null)
const loading = ref(true)
const sorted = computed(() =>
    [...data.value]
        .sort((a,b) => b.totalPoints - a.totalPoints)
        .map((u, i) => ({ ...u, rank: i + 1 }))
)

const displayName = u => u.fullName?.trim() || u.username
const initials    = u => (displayName(u)).charAt(0).toUpperCase()
const scoreClass  = s => s >= 8.5 ? 'sc-exc' : s >= 7 ? 'sc-good' : s >= 5 ? 'sc-avg' : s > 0 ? 'sc-poor' : 'sc-none'

async function load() {
  loading.value = true
  try {
    const requests = [api.get('/leaderboard')]
    if (auth.user?.id) requests.push(api.get(`/leaderboard/me?userId=${auth.user.id}`))
    const [lb, me] = await Promise.allSettled(requests)
    if (lb.status === 'fulfilled')  data.value   = lb.value.data
    if (me?.status === 'fulfilled') myRank.value = me.value.data
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(load)
</script>

<style scoped>
.page { padding: 2rem 2.5rem; max-width: 900px; margin: 0 auto; }
@media(max-width:600px) { .page { padding: 1rem; } }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; flex-wrap: wrap; gap: .8rem; }
.header-left h1 { font-size: 1.6rem; font-weight: 800; margin-bottom: .2rem; }
.header-left p  { color: var(--muted); font-size: .85rem; }

.my-rank-badge { background: linear-gradient(135deg, var(--accent), #7c3aed); color: #fff; border-radius: 14px; padding: .7rem 1.1rem; display: flex; flex-direction: column; align-items: center; min-width: 100px; box-shadow: 0 4px 16px rgba(37,99,235,.3); }
.mr-label { font-size: .68rem; opacity: .85; }
.mr-rank  { font-size: 1.6rem; font-weight: 900; line-height: 1.1; }
.mr-pts   { font-size: .72rem; opacity: .85; }

/* ── PODIUM ── */
.podium-wrap { display: flex; align-items: flex-end; justify-content: center; gap: .8rem; margin-bottom: 1.5rem; margin-top: .8rem; padding: 1.5rem 1rem 0; }
.podium-item { display: flex; flex-direction: column; align-items: center; gap: .4rem; flex: 1; max-width: 180px; }
.podium-crown { font-size: 1.5rem; margin-bottom: -.3rem; }

.podium-avatar-wrap { position: relative; }
.podium-av      { border-radius: 50%; object-fit: cover; border: 3px solid #fff; box-shadow: 0 4px 14px rgba(0,0,0,.15); display: block; }
.podium-av-init { border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 800; color: #fff; border: 3px solid #fff; box-shadow: 0 4px 14px rgba(0,0,0,.15); }
.p1 .podium-av, .p1 .podium-av-init { width: 72px; height: 72px; font-size: 1.6rem; background: linear-gradient(135deg,#f59e0b,#ef4444); }
.p2 .podium-av, .p2 .podium-av-init { width: 58px; height: 58px; font-size: 1.3rem; background: linear-gradient(135deg,#94a3b8,#64748b); }
.p3 .podium-av, .p3 .podium-av-init { width: 52px; height: 52px; font-size: 1.1rem; background: linear-gradient(135deg,#b45309,#92400e); }

.podium-medal { position: absolute; bottom: -4px; right: -4px; font-size: 1.1rem; }
.podium-name  { font-size: .82rem; font-weight: 700; text-align: center; max-width: 120px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.podium-pts   { font-size: .75rem; color: var(--muted); font-weight: 600; }

.podium-block { width: 100%; border-radius: 10px 10px 0 0; }
.pb1 { height: 80px; background: linear-gradient(180deg,#fef3c7,#fde68a); border: 1.5px solid #fcd34d; }
.pb2 { height: 56px; background: linear-gradient(180deg,#f1f5f9,#e2e8f0); border: 1.5px solid #cbd5e1; }
.pb3 { height: 40px; background: linear-gradient(180deg,#fef3c7,#fde68a); filter: sepia(40%); border: 1.5px solid #d97706; opacity: .75; }

/* ── LIST ── */
.list-wrap { background: var(--surface); border: 1.5px solid var(--border); border-radius: 14px; overflow: hidden; box-shadow: var(--shadow-sm); }

.lb-row { display: flex; align-items: center; gap: .85rem; padding: .85rem 1.1rem; border-bottom: 1px solid var(--border); transition: background .15s; }
.lb-row:last-child { border-bottom: none; }
.lb-row:hover { background: var(--surface2); }
.lb-row.is-me { background: linear-gradient(135deg,#eff6ff,#faf5ff); border-left: 3px solid var(--accent); padding-left: calc(1.1rem - 3px); }
.lb-my-sep { border-top: 2px dashed var(--border); margin-top: .2rem; }

.lb-rank  { min-width: 36px; text-align: center; }
.rank-num { font-size: .82rem; font-weight: 800; color: var(--muted); }

.lb-avatar  { flex-shrink: 0; }
.lb-av-img  { width: 38px; height: 38px; border-radius: 50%; object-fit: cover; display: block; }
.lb-av-init { width: 38px; height: 38px; border-radius: 50%; background: linear-gradient(135deg,var(--accent),#7c3aed); color: #fff; font-size: .9rem; font-weight: 700; display: flex; align-items: center; justify-content: center; }

.lb-info    { flex: 1; min-width: 0; }
.lb-name    { font-size: .86rem; font-weight: 700; display: flex; align-items: center; gap: .4rem; }
.you-chip   { font-size: .65rem; font-weight: 700; padding: .1rem .45rem; border-radius: 100px; background: var(--accent); color: #fff; }
.lb-meta    { font-size: .72rem; color: var(--muted); display: flex; align-items: center; gap: .3rem; flex-wrap: wrap; margin-top: .1rem; }
.dot        { opacity: .4; }

.lb-scores        { display: flex; flex-direction: column; align-items: flex-end; gap: .1rem; flex-shrink: 0; }
.lb-main-score    { font-size: 1.1rem; font-weight: 800; padding: .22rem .55rem; border-radius: 8px; min-width: 48px; text-align: center; }
.lb-pts           { font-size: .69rem; color: var(--muted); font-weight: 600; }
.sc-exc  { background: var(--green-light);  color: var(--green); }
.sc-good { background: var(--accent-light); color: var(--accent); }
.sc-avg  { background: var(--yellow-light); color: var(--yellow); }
.sc-poor { background: var(--red-light);    color: var(--red); }
.sc-none { background: var(--surface2); color: var(--muted); }

.lb-progress-col { width: 80px; flex-shrink: 0; }
.lb-prog-track   { height: 5px; background: var(--border); border-radius: 100px; overflow: hidden; }
.lb-prog-fill    { height: 100%; background: linear-gradient(90deg, var(--accent), #7c3aed); border-radius: 100px; transition: width .6s ease; }

.pts-note { text-align: center; font-size: .74rem; color: var(--muted); margin-top: .9rem; padding: .6rem; background: var(--surface); border: 1.5px dashed var(--border); border-radius: 10px; }

/* Skeleton */
.sk { background: linear-gradient(90deg,#e2e8f0 25%,#f1f5f9 50%,#e2e8f0 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; border-radius: 8px; }
@keyframes shimmer { to { background-position: -200% 0; } }
.sk-av-lg    { width: 64px; height: 64px; border-radius: 50%; }
.sk-av-sm    { width: 38px; height: 38px; border-radius: 50%; flex-shrink: 0; }
.sk-name     { width: 80px; height: 13px; margin-top: .3rem; }
.sk-pts      { width: 50px; height: 11px; }
.sk-rank-num { width: 28px; height: 14px; flex-shrink: 0; }
.sk-name-sm  { flex: 1; height: 14px; }
.sk-score    { width: 44px; height: 32px; border-radius: 8px; flex-shrink: 0; }

.sk-podium-item { display: flex; flex-direction: column; align-items: center; gap: .4rem; flex: 1; max-width: 160px; padding-bottom: 40px; }
.sk-row { display: flex; align-items: center; gap: .85rem; padding: .85rem 1.1rem; border-bottom: 1px solid var(--border); }

.center-state { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; color: var(--muted); gap: .6rem; }
.empty-ico    { font-size: 2.5rem; opacity: .4; }
</style>