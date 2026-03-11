<template>
  <div class="page">

    <!-- SKELETON -->
    <template v-if="loading">
      <div class="sk sk-back"></div>
      <div class="hero-card">
        <div class="sk-hero-left">
          <div class="sk sk-icon"></div>
          <div class="sk-hero-info">
            <div class="sk sk-badge"></div>
            <div class="sk sk-title"></div>
            <div class="sk sk-desc"></div>
            <div class="sk sk-desc short"></div>
          </div>
        </div>
        <div class="sk sk-btn"></div>
      </div>
      <div class="lessons-shell">
        <div class="lessons-hd"><div class="sk sk-h2"></div></div>
        <div class="sk-lessons">
          <div v-for="i in 5" :key="i" class="sk-lesson-row">
            <div class="sk sk-num"></div>
            <div class="sk sk-lname"></div>
          </div>
        </div>
      </div>
    </template>

    <template v-else-if="course">
      <!-- ── NÚT QUAY LẠI ── -->
      <button @click="$router.back()" class="btn-back">
        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/></svg>
        <span>Quay lại</span>
      </button>

      <!-- Hero -->
      <div class="hero-card">
        <div class="hero-left">
          <div class="hero-icon">{{ courseIcon }}</div>
          <div class="hero-info">
            <div class="hero-badges">
              <span class="badge badge-blue">{{ course.credits }} tín chỉ</span>
              <span :class="['badge', course.status==='ACTIVE'?'badge-green':'badge-gray']">{{ course.status }}</span>
              <span v-if="courseAvgRating" class="badge badge-star">⭐ {{ courseAvgRating }}</span>
            </div>
            <h1>{{ course.title }}</h1>
            <p class="hero-desc">{{ course.description }}</p>
            <div class="hero-inst"><span class="inst-dot"></span>{{ course.instructor||'Chưa xác định' }}</div>
          </div>
        </div>
        <div class="hero-action">
          <div v-if="isEnrolled" class="enrolled-box">
            <div class="enrolled-check">
              <svg width="13" height="13" fill="none" stroke="#fff" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <div>
              <div class="enrolled-t">Đã đăng ký</div>
              <div class="enrolled-s">Bạn có thể xem tất cả bài học</div>
            </div>
          </div>
          <template v-else-if="!auth.isAdmin">
            <button v-if="course.status === 'ACTIVE'" @click="enroll" class="btn btn-enroll" :disabled="enrolling">
              {{ enrolling ? '...' : '🚀 Đăng ký ngay' }}
            </button>
            <div v-else-if="course.status === 'INACTIVE'" class="enroll-closed">🔒 Khóa học đã đóng tuyển sinh</div>
            <div v-else-if="course.status === 'DRAFT'"    class="enroll-closed enroll-draft">📝 Khóa học chưa công khai</div>
          </template>
        </div>
      </div>

      <!-- Progress bar -->
      <div v-if="isEnrolled" class="progress-card">
        <div class="prog-top">
          <div class="prog-info">
            <span class="prog-label">Tiến độ học tập</span>
            <span v-if="progress.courseCompleted" class="completed-badge">🎉 Hoàn thành!</span>
          </div>
          <span class="prog-pct" :class="{done:progress.courseCompleted}">{{ progress.percentage }}%</span>
        </div>
        <div class="prog-track">
          <div class="prog-fill" :style="`width:${progress.percentage}%`" :class="{done:progress.courseCompleted}"></div>
        </div>
        <div class="prog-meta">{{ progress.completedLessons }}/{{ progress.totalLessons }} bài học</div>
      </div>

      <!-- Lessons accordion -->
      <div class="lessons-shell">
        <div class="lessons-hd">
          <h2>📖 Nội dung khóa học</h2>
          <span class="lcount">{{ lessons.length }} bài học</span>
        </div>

        <div v-if="!lessons.length" class="center-state">
          <div class="empty-ico">📝</div><p>Chưa có bài học nào.</p>
        </div>

        <div v-else class="accordion">
          <div v-for="l in lessons" :key="l.id" class="acc-item">

            <div :class="['acc-hd', { 'is-open': expandedId===l.id, 'is-locked': !isEnrolled, 'is-done': isDone(l.id) }]"
                 @click="isEnrolled && toggle(l.id)">
              <div :class="['acc-num', { 'num-done': isDone(l.id), 'num-open': expandedId===l.id && !isDone(l.id) }]">
                <svg v-if="isDone(l.id)" width="12" height="12" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                <span v-else>{{ l.orderNum }}</span>
              </div>
              <div class="acc-title">{{ l.title }}</div>

              <!-- Quiz badge trong header -->
              <span v-if="isEnrolled && quizMap[l.id]" class="quiz-badge-sm">
                🧩 Quiz
                <span v-if="bestScoreMap[l.id] !== undefined"
                      :class="['qbs', bestScoreMap[l.id] >= quizMap[l.id].passingScore ? 'qbs-pass' : 'qbs-fail']">
                  {{ bestScoreMap[l.id] }}%
                </span>
              </span>

              <span v-if="!isEnrolled" class="lock-ico">🔒</span>
              <template v-else>
                <span v-if="isDone(l.id)" class="done-chip">✓ Đã học</span>
                <svg :class="['chevron', { 'ch-open': expandedId===l.id }]" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </template>
            </div>

            <!-- ACC BODY -->
            <div class="acc-body" :style="expandedId===l.id ? 'max-height:900px' : 'max-height:0'">
              <div class="acc-inner">
                <p>{{ l.content||'Không có nội dung.' }}</p>
                <!-- Video player — tự detect YouTube vs Cloudinary -->
                <div v-if="l.videoUrl" class="lesson-video">
                  <VideoPlayer :url="l.videoUrl" />
                </div>

                <!-- ── QUIZ BLOCK ──────────────────────── -->
                <div v-if="isEnrolled && quizMap[l.id]" class="quiz-block">
                  <div class="qb-header">
                    <div class="qb-info">
                      <span class="qb-icon">🧩</span>
                      <div>
                        <div class="qb-title">{{ quizMap[l.id].title }}</div>
                        <div class="qb-meta">
                          <span>{{ quizMap[l.id].questionCount }} câu</span>
                          <span class="qb-dot">·</span>
                          <span>Đạt: {{ quizMap[l.id].passingScore }}%</span>
                          <span v-if="quizMap[l.id].timeLimitMinutes" class="qb-dot">·</span>
                          <span v-if="quizMap[l.id].timeLimitMinutes">⏱ {{ quizMap[l.id].timeLimitMinutes }} phút</span>
                          <span v-if="quizMap[l.id].maxAttempts" class="qb-dot">·</span>
                          <span v-if="quizMap[l.id].maxAttempts">Tối đa {{ quizMap[l.id].maxAttempts }} lần</span>
                        </div>
                      </div>
                    </div>

                    <!-- Điểm tốt nhất -->
                    <div v-if="bestScoreMap[l.id] !== undefined" class="qb-best">
                      <div :class="['qb-score', bestScoreMap[l.id] >= quizMap[l.id].passingScore ? 'score-pass' : 'score-fail']">
                        {{ bestScoreMap[l.id] }}<span class="score-unit">/100</span>
                      </div>
                      <div class="qb-best-label">Điểm cao nhất</div>
                    </div>
                  </div>

                  <!-- Lịch sử làm bài (nếu có) -->
                  <div v-if="attemptHistoryMap[l.id]?.length" class="qb-history">
                    <div class="qbh-title">Lịch sử:</div>
                    <div class="qbh-list">
                      <span v-for="(att, ai) in attemptHistoryMap[l.id].slice(0,5)" :key="att.id"
                            :class="['qbh-chip', att.passed ? 'chip-pass' : 'chip-fail']"
                            @click="viewAttemptResult(att.id)"
                            title="Xem kết quả">
                        Lần {{ ai+1 }}: {{ att.score }}%
                      </span>
                    </div>
                  </div>

                  <button class="btn-start-quiz" @click.stop="startQuiz(l.id)"
                          :disabled="quizLoading === l.id">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polygon points="5 3 19 12 5 21 5 3"/></svg>
                    {{ quizLoading===l.id ? 'Đang tải...' : (bestScoreMap[l.id] !== undefined ? 'Làm lại quiz' : 'Bắt đầu quiz') }}
                  </button>
                </div>

                <!-- ── LESSON ACTIONS ──────────────────── -->
                <div v-if="isEnrolled" class="lesson-actions">
                  <button v-if="!isDone(l.id)"
                          @click.stop="markComplete(l.id)"
                          :disabled="marking===l.id"
                          class="btn-complete">
                    <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                    <span>{{ marking===l.id ? 'Đang lưu...' : 'Đánh dấu hoàn thành' }}</span>
                  </button>
                  <button v-else
                          @click.stop="unmarkComplete(l.id)"
                          :disabled="marking===l.id"
                          class="btn-unmark">
                    <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                    <span>{{ marking===l.id ? '...' : 'Bỏ hoàn thành' }}</span>
                  </button>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
      <!-- Review section -->
      <CourseReview
          :course-id="course.id"
          :is-enrolled="isEnrolled"
          @stats-updated="s => courseAvgRating = s.avgRating"
      />

    </template>

    <!-- ── QUIZ PLAYER MODAL ──────────────────────────── -->
    <QuizPlayer
        v-if="activeQuiz && showPlayer"
        :quiz="activeQuiz"
        :lessonId="activeQuizLessonId"
        @close="showPlayer = false"
        @done="onQuizDone"
    />

    <!-- ── QUIZ RESULT MODAL ──────────────────────────── -->
    <QuizResult
        v-if="quizResult && showResult"
        :result="quizResult"
        :choicesMap="activeChoicesMap"
        :canRetry="canRetryActive"
        @close="showResult = false"
        @retry="retryQuiz"
    />

    <div v-if="toast" :class="['toast', toast.type]">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'
import QuizPlayer from '../components/QuizPlayer.vue'
import QuizResult from '../components/QuizResult.vue'
import VideoPlayer from '../components/VideoPlayer.vue'
import CourseReview from '../components/CourseReview.vue'

const route = useRoute()
const auth  = useAuthStore()

// ── Core state ────────────────────────────────────────────
const course     = ref(null)
const lessons    = ref([])
const loading    = ref(true)
const enrolling  = ref(false)
const isEnrolled = ref(false)
const expandedId = ref(null)
const marking    = ref(null)
const toast      = ref(null)
const progress   = reactive({ percentage:0, completedLessons:0, totalLessons:0, courseCompleted:false, completedLessonIds:[] })

// ── Quiz state ────────────────────────────────────────────
const quizMap          = ref({})   // lessonId → QuizDTO (metadata, no answers)
const bestScoreMap     = ref({})   // lessonId → best score number
const attemptHistoryMap= ref({})   // lessonId → AttemptSummary[]
const quizLoading      = ref(null) // lessonId being loaded
const activeQuiz       = ref(null) // quiz đang mở
const activeQuizLessonId = ref(null)
const showPlayer       = ref(false)
const quizResult       = ref(null)
const showResult       = ref(false)
const activeChoicesMap = ref({})   // questionId → choices[] (để QuizResult hiển thị text)
const canRetryActive   = ref(true)

// ── Computed ──────────────────────────────────────────────
const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']
const courseIcon = computed(() => icons[route.params.id % icons.length])
const isDone = id => progress.completedLessonIds.includes(id)

// ── Helpers ───────────────────────────────────────────────
function showToast(m, t='success') { toast.value={msg:m,type:t}; setTimeout(()=>toast.value=null,3500) }
function toggle(id) { expandedId.value = expandedId.value===id ? null : id }
function applyProgress(d) {
  progress.percentage         = d.percentage         ?? 0
  progress.completedLessons   = d.completedLessons   ?? 0
  progress.totalLessons       = d.totalLessons       ?? 0
  progress.courseCompleted    = d.courseCompleted     ?? false
  progress.completedLessonIds = d.completedLessonIds ?? []
}

// ── Data fetching ─────────────────────────────────────────
async function fetchCourse() {
  const [cr, lr] = await Promise.all([
    api.get(`/courses/${route.params.id}`),
    api.get(`/lessons/course/${route.params.id}`)
  ])
  course.value  = cr.data
  lessons.value = lr.data || []
}

async function checkEnrolled() {
  try {
    const r = await api.get(`/enrollments/user/${auth.user?.id}`)
    isEnrolled.value = r.data.some(e => e.course?.id == route.params.id)
  } catch {}
}

async function fetchProgress() {
  if (!isEnrolled.value) return
  try {
    const r = await api.get(`/progress?userId=${auth.user?.id}&courseId=${route.params.id}`)
    applyProgress(r.data)
  } catch {}
}

// Lấy quiz metadata cho tất cả bài học (gọi sau khi enroll)
async function fetchAllQuizMeta() {
  if (!isEnrolled.value || !lessons.value.length) return
  await Promise.allSettled(
      lessons.value.map(l => fetchQuizMeta(l.id))
  )
}

async function fetchQuizMeta(lessonId) {
  try {
    const r = await api.get(`/quizzes/lesson/${lessonId}/meta`)
    if (r.data && r.data.id) {
      quizMap.value = { ...quizMap.value, [lessonId]: r.data }
      await fetchAttemptHistory(lessonId, r.data.id)
    }
  } catch {}
}

async function fetchAttemptHistory(lessonId, quizId) {
  try {
    const r = await api.get(`/quizzes/${quizId}/attempts?userId=${auth.user?.id}`)
    attemptHistoryMap.value = { ...attemptHistoryMap.value, [lessonId]: r.data }
    if (r.data.length) {
      const best = Math.max(...r.data.map(a => a.score))
      bestScoreMap.value = { ...bestScoreMap.value, [lessonId]: best }
    }
  } catch {}
}

// ── Enroll ────────────────────────────────────────────────
async function enroll() {
  enrolling.value = true
  try {
    await api.post(`/enrollments/enroll?userId=${auth.user?.id}&courseId=${route.params.id}`)
    isEnrolled.value = true
    await fetchProgress()
    await fetchAllQuizMeta()
    showToast('🎉 Đăng ký thành công!')
  } catch(e) { showToast(e.response?.data?.error||'Thất bại!', 'error') }
  finally { enrolling.value = false }
}

// ── Progress actions ──────────────────────────────────────
async function markComplete(lessonId) {
  marking.value = lessonId
  try {
    const r = await api.post(`/progress/complete?userId=${auth.user?.id}&lessonId=${lessonId}`)
    applyProgress(r.data)
    showToast(r.data.courseCompleted ? '🎉 Hoàn thành khóa học!' : `✅ (${r.data.completedLessons}/${r.data.totalLessons} bài)`)
  } catch(e) { showToast(e.response?.data?.error||'Lỗi!', 'error') }
  finally { marking.value = null }
}

async function unmarkComplete(lessonId) {
  marking.value = lessonId
  try {
    const r = await api.delete(`/progress/complete?userId=${auth.user?.id}&lessonId=${lessonId}`)
    applyProgress(r.data)
    showToast('↩ Đã bỏ đánh dấu')
  } catch(e) { showToast(e.response?.data?.error||'Lỗi!', 'error') }
  finally { marking.value = null }
}

// ── Quiz actions ──────────────────────────────────────────
async function startQuiz(lessonId) {
  quizLoading.value = lessonId
  try {
    const r = await api.get(`/quizzes/lesson/${lessonId}/start?userId=${auth.user?.id}`)
    activeQuiz.value = r.data
    activeQuizLessonId.value = lessonId
    const map = {}
    r.data.questions.forEach(q => { map[q.id] = q.choices })
    activeChoicesMap.value = map
    const quiz = quizMap.value[lessonId]
    if (quiz?.maxAttempts) {
      canRetryActive.value = (attemptHistoryMap.value[lessonId] || []).length < quiz.maxAttempts
    } else {
      canRetryActive.value = true
    }
    showPlayer.value = true
    quizResult.value = null
  } catch(e) {
    showToast(e.response?.data?.error || 'Không thể tải quiz!', 'error')
  } finally {
    quizLoading.value = null
  }
}

function onQuizDone(result, choicesMap) {
  showPlayer.value = false
  quizResult.value = result
  showResult.value = true
  if (choicesMap) activeChoicesMap.value = choicesMap

  // Cập nhật lịch sử & best score
  const lessonId = activeQuizLessonId.value
  if (lessonId) {
    const prev = attemptHistoryMap.value[lessonId] || []
    attemptHistoryMap.value = {
      ...attemptHistoryMap.value,
      [lessonId]: [result, ...prev]
    }
    const best = Math.max(result.score, bestScoreMap.value[lessonId] ?? 0)
    bestScoreMap.value = { ...bestScoreMap.value, [lessonId]: best }
  }

  if (result.passed) showToast(`🎉 Xuất sắc! Điểm: ${result.score}/100`)
  else               showToast(`Điểm: ${result.score}/100 — Chưa đạt. Hãy thử lại!`, 'error')
}

async function viewAttemptResult(attemptId) {
  try {
    const r = await api.get(`/quizzes/attempts/${attemptId}/result`)
    quizResult.value = r.data
    showResult.value = true
  } catch(e) {
    showToast('Không thể tải kết quả!', 'error')
  }
}

function retryQuiz() {
  showResult.value = false
  if (activeQuiz.value) showPlayer.value = true
}

// ── Mount ─────────────────────────────────────────────────
onMounted(async () => {
  loading.value = true
  await Promise.all([fetchCourse(), checkEnrolled()])
  await fetchProgress()
  await fetchAllQuizMeta()
  loading.value = false
})
</script>

<style scoped>
.page { padding:2rem 2.5rem; max-width:960px; margin:0 auto; }
@media(max-width:700px) {
  .page { padding:1rem 1rem; }
  .hero-card { flex-direction:column; gap:1rem; padding:1.1rem; }
  .hero-left { flex-direction:column; align-items:flex-start; }
  .hero-action { width:100%; }
  .btn-enroll, .enrolled-box { width:100%; justify-content:center; }
  .hero-info h1 { font-size:1.05rem; }
}

/* ── SKELETON ── */
.sk { background:linear-gradient(90deg,#e2e8f0 25%,#f1f5f9 50%,#e2e8f0 75%); background-size:200% 100%; animation:shimmer 1.4s infinite; border-radius:8px; }
@keyframes shimmer { to{background-position:-200% 0} }
.sk-back  { width:90px; height:22px; margin-bottom:1.2rem; }
.sk-icon  { width:52px; height:52px; border-radius:12px; flex-shrink:0; }
.sk-badge { width:80px; height:18px; margin-bottom:.5rem; }
.sk-title { width:260px; height:28px; margin-bottom:.5rem; }
.sk-desc  { width:100%; height:14px; margin-bottom:.35rem; }
.sk-desc.short { width:60%; }
.sk-btn   { width:130px; height:42px; border-radius:9px; flex-shrink:0; }
.sk-h2    { width:180px; height:20px; }
.sk-num   { width:26px; height:26px; border-radius:7px; flex-shrink:0; }
.sk-lname { flex:1; height:16px; border-radius:6px; }
.sk-hero-left { display:flex; gap:1.1rem; flex:1; }
.sk-hero-info { flex:1; }
.sk-lesson-row { display:flex; align-items:center; gap:1rem; padding:0 1.4rem; height:52px; border-bottom:1px solid var(--border); }
.sk-lesson-row:last-child { border-bottom:none; }

/* ── BACK BTN ── */
.btn-back { display:inline-flex; align-items:center; gap:.45rem; padding:.45rem .9rem .45rem .7rem; border:1.5px solid var(--border); border-radius:100px; background:var(--surface); color:var(--muted); font-size:.83rem; font-weight:600; cursor:pointer; transition:all .18s; margin-bottom:1.2rem; font-family:'Plus Jakarta Sans',sans-serif; box-shadow:var(--shadow-sm); }
.btn-back:hover { border-color:var(--accent); color:var(--accent); background:var(--accent-light); transform:translateX(-2px); }

/* ── HERO ── */
.hero-card { background:var(--surface); border:1.5px solid var(--border); border-radius:16px; padding:1.6rem; display:flex; justify-content:space-between; align-items:flex-start; gap:1.4rem; margin-bottom:1rem; box-shadow:var(--shadow-sm); flex-wrap:wrap; }
.hero-left { display:flex; gap:1.1rem; flex:1; align-items:flex-start; }
.hero-icon { font-size:2.6rem; flex-shrink:0; line-height:1; }
.hero-badges { display:flex; gap:.4rem; margin-bottom:.45rem; flex-wrap:wrap; }
.hero-info h1 { font-size:1.25rem; font-weight:800; margin-bottom:.35rem; line-height:1.3; background:linear-gradient(135deg,#1e40af,#4f46e5); -webkit-background-clip:text; -webkit-text-fill-color:transparent; }
.hero-desc { font-size:.83rem; color:var(--muted); line-height:1.6; margin-bottom:.45rem; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
.hero-inst { display:flex; align-items:center; gap:.45rem; font-size:.79rem; color:var(--muted); }
.inst-dot { width:7px; height:7px; border-radius:50%; background:var(--green); flex-shrink:0; }
.hero-action { flex-shrink:0; display:flex; align-items:center; }
.enrolled-box { display:flex; align-items:center; gap:.75rem; background:var(--green-light); border:1.5px solid #a7f3d0; border-radius:10px; padding:.8rem 1rem; }
.enrolled-check { width:26px; height:26px; border-radius:50%; background:var(--green); display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.enrolled-t { font-size:.83rem; font-weight:700; color:var(--green); }
.enrolled-s { font-size:.72rem; color:var(--muted); margin-top:.08rem; }
.btn { display:inline-flex; align-items:center; gap:.35rem; border-radius:9px; font-size:.84rem; font-weight:700; cursor:pointer; border:none; transition:all .18s; font-family:'Plus Jakarta Sans',sans-serif; }
.btn-enroll { padding:.58rem 1.3rem; background:var(--accent); color:#fff; box-shadow:0 2px 8px rgba(37,99,235,.25); }
.btn-enroll:hover:not(:disabled) { background:var(--accent-dark); box-shadow:0 4px 14px rgba(37,99,235,.35); transform:translateY(-1px); }
.btn-enroll:disabled { opacity:.5; cursor:not-allowed; transform:none; }
.enroll-closed { font-size:.78rem; font-weight:600; padding:.5rem .9rem; border-radius:9px; background:var(--surface2); color:var(--muted); border:1.5px solid var(--border); }
.enroll-draft { color:var(--yellow); background:var(--yellow-light); border-color:#fde68a; }

/* ── PROGRESS ── */
.progress-card { background:var(--surface); border:1.5px solid var(--border); border-radius:12px; padding:1rem 1.4rem; margin-bottom:1rem; box-shadow:var(--shadow-sm); }
.prog-top  { display:flex; justify-content:space-between; align-items:center; margin-bottom:.5rem; }
.prog-info { display:flex; align-items:center; gap:.7rem; }
.prog-label { font-size:.82rem; font-weight:600; color:var(--text2); }
.prog-pct   { font-size:.88rem; font-weight:800; color:var(--accent); }
.prog-pct.done { color:var(--green); }
.prog-track { height:8px; background:var(--surface2); border:1px solid var(--border); border-radius:100px; overflow:hidden; }
.prog-fill  { height:100%; background:linear-gradient(90deg,var(--accent),#60a5fa); border-radius:100px; transition:width .7s ease; }
.prog-fill.done { background:linear-gradient(90deg,var(--green),#34d399); }
.prog-meta  { font-size:.74rem; color:var(--muted); margin-top:.4rem; }
.completed-badge { font-size:.73rem; font-weight:700; padding:.18rem .6rem; border-radius:100px; background:var(--green-light); color:var(--green); border:1px solid #a7f3d0; }

/* ── ACCORDION ── */
.lessons-shell { background:var(--surface); border:1.5px solid var(--border); border-radius:16px; overflow:hidden; box-shadow:var(--shadow-sm); display:flex; flex-direction:column; max-height:620px; }
.lessons-hd { display:flex; align-items:center; gap:.8rem; padding:1rem 1.4rem; border-bottom:1.5px solid var(--border); flex-shrink:0; height:54px; }
.lessons-hd h2 { font-size:.97rem; font-weight:700; }
.lcount { font-size:.71rem; color:var(--muted); background:var(--surface2); border:1px solid var(--border); padding:.14rem .55rem; border-radius:100px; }
.accordion { overflow-y:auto; flex:1; min-height:0; }
.acc-item { border-bottom:1px solid var(--border); }
.acc-item:last-child { border-bottom:none; }
.acc-hd { display:flex; align-items:center; gap:.85rem; padding:0 1.4rem; height:52px; cursor:pointer; transition:background .15s; user-select:none; }
.acc-hd.is-locked { cursor:default; opacity:.72; }
.acc-hd.is-done   { background:#f0fdf4; }
.acc-hd.is-open   { background:var(--accent-light); border-left:3px solid var(--accent); padding-left:calc(1.4rem - 3px); }
.acc-hd.is-done.is-open { background:#dcfce7; border-left-color:var(--green); }
.acc-hd:hover:not(.is-locked):not(.is-done) { background:var(--surface2); }
.acc-hd.is-done:hover { background:#e7fef0; }
.acc-num { width:25px; height:25px; border-radius:7px; background:var(--surface2); border:1px solid var(--border); display:flex; align-items:center; justify-content:center; font-size:.75rem; font-weight:700; color:var(--muted); flex-shrink:0; transition:all .15s; }
.acc-num.num-done { background:var(--green); border-color:var(--green); color:#fff; }
.acc-num.num-open { background:var(--accent); border-color:var(--accent); color:#fff; }
.acc-title { flex:1; font-size:.87rem; font-weight:600; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; text-align:left; color:var(--text2); }
.lock-ico  { font-size:.78rem; }
.done-chip { font-size:.67rem; font-weight:700; padding:.15rem .5rem; border-radius:100px; background:var(--green-light); color:var(--green); border:1px solid #a7f3d0; flex-shrink:0; }
.chevron   { color:var(--muted); flex-shrink:0; transition:transform .25s; }
.ch-open   { transform:rotate(180deg); color:var(--accent); }

/* Quiz badge trong accordion header */
.quiz-badge-sm { display:inline-flex; align-items:center; gap:.3rem; font-size:.66rem; font-weight:700; padding:.15rem .5rem; border-radius:100px; background:var(--purple-light); color:var(--purple); border:1px solid #c4b5fd; flex-shrink:0; white-space:nowrap; }
.qbs { padding:.08rem .35rem; border-radius:100px; font-size:.64rem; }
.qbs-pass { background:var(--green); color:#fff; }
.qbs-fail { background:var(--red);   color:#fff; }

.acc-body  { overflow:hidden; transition:max-height .3s ease; }
.acc-inner { padding:.85rem 1.4rem 1rem 4rem; border-top:1px solid var(--border); }
.acc-inner > p { font-size:.83rem; color:var(--muted); line-height:1.7; margin-bottom:.6rem; }
.lesson-video { margin: .75rem 0; border-radius: 10px; overflow: hidden; }


/* ── QUIZ BLOCK ── */
.quiz-block {
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1.5px solid #c4b5fd; border-radius: 12px;
  padding: .9rem 1rem; margin-bottom: .9rem;
  display: flex; flex-direction: column; gap: .7rem;
}
.qb-header { display:flex; align-items:center; justify-content:space-between; gap:.8rem; }
.qb-info   { display:flex; align-items:center; gap:.7rem; flex:1; min-width:0; }
.qb-icon   { font-size:1.5rem; flex-shrink:0; }
.qb-title  { font-size:.88rem; font-weight:700; color:var(--purple); margin-bottom:.15rem; }
.qb-meta   { font-size:.74rem; color:var(--muted); display:flex; align-items:center; gap:.35rem; flex-wrap:wrap; }
.qb-dot    { opacity:.4; }
.qb-best   { text-align:center; flex-shrink:0; }
.qb-score  { font-size:1.5rem; font-weight:900; line-height:1; }
.score-pass { color:var(--green); }
.score-fail { color:var(--red); }
.score-unit { font-size:.75rem; font-weight:600; opacity:.7; }
.qb-best-label { font-size:.66rem; color:var(--muted); margin-top:.1rem; }

/* Lịch sử làm bài */
.qb-history { display:flex; align-items:center; gap:.6rem; flex-wrap:wrap; }
.qbh-title  { font-size:.72rem; color:var(--muted); font-weight:600; }
.qbh-list   { display:flex; gap:.35rem; flex-wrap:wrap; }
.qbh-chip   { font-size:.7rem; font-weight:700; padding:.2rem .55rem; border-radius:100px; cursor:pointer; transition:all .15s; }
.chip-pass  { background:var(--green-light); color:var(--green); border:1px solid #a7f3d0; }
.chip-fail  { background:var(--red-light);   color:var(--red);   border:1px solid #fca5a5; }
.qbh-chip:hover { transform:translateY(-1px); box-shadow:var(--shadow-sm); }

.btn-start-quiz {
  display: inline-flex; align-items: center; gap: .5rem;
  align-self: flex-start;
  padding: .5rem 1.1rem; border-radius: 9px; border: none;
  background: linear-gradient(135deg, var(--purple), #6d28d9);
  color: #fff; cursor: pointer; font-size: .82rem; font-weight: 700;
  font-family: 'Plus Jakarta Sans', sans-serif; transition: all .18s;
  box-shadow: 0 2px 8px rgba(124,58,237,.3);
}
.btn-start-quiz:hover:not(:disabled) { transform:translateY(-1px); box-shadow:0 4px 14px rgba(124,58,237,.4); }
.btn-start-quiz:disabled { opacity:.65; cursor:not-allowed; transform:none; }

/* ── LESSON ACTIONS ── */
.lesson-actions { margin-top:.8rem; padding-top:.75rem; border-top:1px dashed var(--border); display:flex; gap:.5rem; flex-wrap:wrap; }
.btn-complete { display:inline-flex; align-items:center; gap:.45rem; padding:.48rem 1.05rem; border-radius:8px; background:var(--green); color:#fff; border:none; font-size:.8rem; font-weight:700; cursor:pointer; transition:all .18s; font-family:'Plus Jakarta Sans',sans-serif; box-shadow:0 2px 6px rgba(5,150,105,.2); }
.btn-complete:hover:not(:disabled) { background:#047857; transform:translateY(-1px); }
.btn-complete:disabled { opacity:.55; cursor:not-allowed; }
.btn-unmark { display:inline-flex; align-items:center; gap:.45rem; padding:.48rem 1.05rem; border-radius:8px; background:#fff3f3; color:var(--red); border:1.5px solid #fca5a5; font-size:.8rem; font-weight:700; cursor:pointer; transition:all .18s; font-family:'Plus Jakarta Sans',sans-serif; }
.btn-unmark:hover:not(:disabled) { background:var(--red); color:#fff; border-color:var(--red); transform:translateY(-1px); }
.btn-unmark:disabled { opacity:.5; cursor:not-allowed; }

/* ── MISC ── */
.badge { font-size:.67rem; font-weight:700; padding:.18rem .55rem; border-radius:100px; }
.badge-green { background:var(--green-light); color:var(--green); border:1px solid #a7f3d0; }
.badge-blue  { background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; }
.badge-gray  { background:#f1f5f9; color:var(--muted); border:1px solid var(--border); }
.center-state { display:flex; flex-direction:column; align-items:center; justify-content:center; gap:.6rem; color:var(--muted); padding:2.5rem; }
.empty-ico { font-size:2rem; opacity:.4; }
.toast { position:fixed; bottom:2rem; right:2rem; padding:.72rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:2000; animation:toastIn .25s ease; border:1.5px solid; box-shadow:var(--shadow); }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light);   color:var(--red);   border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }
.badge-star { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; }
</style>