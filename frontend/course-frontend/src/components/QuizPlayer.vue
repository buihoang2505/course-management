<template>
  <!-- Chặn click ra ngoài — không cho đóng quiz bằng cách click overlay -->
  <div class="quiz-overlay">
    <div class="quiz-modal">

      <!-- ── HEADER ─────────────────────────────────────── -->
      <div class="qm-header">
        <div class="qm-meta">
          <span class="qm-badge">🧩 Quiz</span>
          <h2 class="qm-title">{{ quiz.title }}</h2>
        </div>
        <div class="qm-hd-right">
          <div v-if="quiz.timeLimitMinutes" :class="['qm-timer', { 'timer-warn': timeLeft < 60, 'timer-danger': timeLeft < 30 }]">
            <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            {{ formattedTime }}
          </div>
          <!-- Nút X chỉ mở confirm thoát, không thoát ngay -->
          <button class="qm-close" @click="showExitWarning = true" title="Thoát quiz">✕</button>
        </div>
      </div>

      <!-- ── PROGRESS BAR ───────────────────────────────── -->
      <div class="qm-progress-wrap">
        <div class="qm-progress-bar" :style="`width:${((currentIdx+1)/quiz.questions.length)*100}%`"></div>
      </div>
      <div class="qm-prog-label">
        Câu <strong>{{ currentIdx + 1 }}</strong> / {{ quiz.questions.length }}
        <span class="answered-count">· {{ answeredCount }} đã trả lời</span>
      </div>

      <!-- ── QUESTION ────────────────────────────────────── -->
      <div class="qm-body">
        <transition name="slide-q" mode="out-in">
          <div :key="currentIdx" class="question-wrap">
            <div class="q-num">Câu {{ currentIdx + 1 }}</div>
            <p class="q-content">{{ currentQuestion.content }}</p>

            <div class="choices-list">
              <button
                  v-for="(c, ci) in currentQuestion.choices"
                  :key="c.id"
                  :class="['choice-btn', { 'choice-selected': answers[currentQuestion.id] === c.id }]"
                  @click="selectAnswer(currentQuestion.id, c.id)"
              >
                <span class="choice-letter">{{ ['A','B','C','D'][ci] }}</span>
                <span class="choice-text">{{ c.content }}</span>
                <svg v-if="answers[currentQuestion.id] === c.id"
                     class="choice-check" width="16" height="16" fill="none"
                     stroke="currentColor" stroke-width="3" viewBox="0 0 24 24">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              </button>
            </div>
          </div>
        </transition>
      </div>

      <!-- ── NAVIGATION ─────────────────────────────────── -->
      <div class="qm-footer">
        <div class="q-map">
          <button
              v-for="(q, i) in quiz.questions" :key="q.id"
              :class="['q-dot', {
              'dot-current':  i === currentIdx,
              'dot-answered': answers[q.id] !== undefined,
            }]"
              @click="currentIdx = i"
          >{{ i+1 }}</button>
        </div>

        <div class="qm-nav">
          <button class="btn-nav" :disabled="currentIdx === 0" @click="currentIdx--">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="15 18 9 12 15 6"/></svg>
            Trước
          </button>

          <button v-if="currentIdx < quiz.questions.length - 1"
                  class="btn-nav btn-next" @click="currentIdx++">
            Tiếp
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"/></svg>
          </button>

          <button v-else class="btn-submit" @click="confirmSubmit" :disabled="submitting">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
            {{ submitting ? 'Đang nộp...' : 'Nộp bài' }}
          </button>
        </div>
      </div>

      <!-- ── CONFIRM SUBMIT DIALOG ─────────────────────── -->
      <div v-if="showConfirm" class="confirm-overlay">
        <div class="confirm-box">
          <div class="confirm-icon">📋</div>
          <h3>Xác nhận nộp bài</h3>
          <p>Bạn đã trả lời <strong>{{ answeredCount }}/{{ quiz.questions.length }}</strong> câu.</p>
          <p v-if="answeredCount < quiz.questions.length" class="confirm-warn">
            ⚠️ Còn {{ quiz.questions.length - answeredCount }} câu chưa trả lời!
          </p>
          <div class="confirm-actions">
            <button class="btn-cancel" @click="showConfirm = false">Làm tiếp</button>
            <button class="btn-confirm-submit" @click="doSubmit" :disabled="submitting">
              {{ submitting ? 'Đang nộp...' : 'Nộp bài' }}
            </button>
          </div>
        </div>
      </div>

      <!-- ── CẢNH BÁO THOÁT (chống cheat) ──────────────── -->
      <div v-if="showExitWarning" class="confirm-overlay">
        <div class="confirm-box">
          <div class="confirm-icon">⚠️</div>
          <h3>Bạn có chắc muốn thoát?</h3>
          <p>Nếu thoát, <strong>lần làm bài này sẽ bị tính</strong> và bài chưa nộp sẽ được nộp tự động với đáp án hiện tại.</p>
          <p class="confirm-warn">Timer không dừng khi thoát!</p>
          <div class="confirm-actions">
            <button class="btn-cancel" @click="showExitWarning = false">Làm tiếp</button>
            <button class="btn-confirm-submit btn-danger" @click="forceSubmitAndClose" :disabled="submitting">
              {{ submitting ? 'Đang nộp...' : 'Nộp & Thoát' }}
            </button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const props = defineProps({
  quiz:     { type: Object, required: true },
  lessonId: { type: Number, required: true },
})
const emit = defineEmits(['close', 'done'])

const auth        = useAuthStore()
const currentIdx  = ref(0)
const answers     = ref({})
const submitting  = ref(false)
const showConfirm = ref(false)
const showExitWarning = ref(false)

// ── Timer — lưu startedAt vào sessionStorage để chống reload reset ──────
const STORAGE_KEY = `quiz_start_${props.quiz.id}_${auth.user.id}`

let savedStart = sessionStorage.getItem(STORAGE_KEY)
if (!savedStart) {
  savedStart = Date.now().toString()
  sessionStorage.setItem(STORAGE_KEY, savedStart)
}
const startedAt = parseInt(savedStart)

// Tính timeLeft dựa trên thời gian thực, không reset khi reload
const maxSeconds = (props.quiz.timeLimitMinutes || 0) * 60
const elapsed    = Math.floor((Date.now() - startedAt) / 1000)
const timeLeft   = ref(props.quiz.timeLimitMinutes
    ? Math.max(0, maxSeconds - elapsed)
    : 0)

let timerInterval = null
if (props.quiz.timeLimitMinutes) {
  if (timeLeft.value <= 0) {
    // Đã hết giờ ngay khi load (do reload cheat) → nộp luôn
    doSubmit()
  } else {
    timerInterval = setInterval(() => {
      timeLeft.value--
      if (timeLeft.value <= 0) {
        clearInterval(timerInterval)
        doSubmit()
      }
    }, 1000)
  }
}

const formattedTime = computed(() => {
  const m = Math.floor(timeLeft.value / 60)
  const s = timeLeft.value % 60
  return `${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
})

// ── Chống cheat: cảnh báo khi F5 / đóng tab ─────────────────────────
function handleBeforeUnload(e) {
  e.preventDefault()
  e.returnValue = 'Bạn đang làm quiz. Nếu rời trang, timer sẽ không dừng!'
  return e.returnValue
}

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  clearInterval(timerInterval)
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

// ── Computed ─────────────────────────────────────────────
const currentQuestion = computed(() => props.quiz.questions[currentIdx.value])
const answeredCount   = computed(() => Object.keys(answers.value).length)

// ── Actions ──────────────────────────────────────────────
function selectAnswer(questionId, choiceId) {
  answers.value = { ...answers.value, [questionId]: choiceId }
}

function confirmSubmit() {
  showConfirm.value = true
}

// Nộp bài + thoát (khi user chủ động thoát)
async function forceSubmitAndClose() {
  showExitWarning.value = false
  await doSubmit()
}

async function doSubmit() {
  showConfirm.value = false
  submitting.value  = true

  const timeSpent = Math.floor((Date.now() - startedAt) / 1000)

  const payload = {
    userId:           auth.user.id,
    timeSpentSeconds: timeSpent,
    answers: props.quiz.questions.map(q => ({
      questionId: q.id,
      choiceId:   answers.value[q.id] ?? null,
    }))
  }

  try {
    const res = await api.post(`/quizzes/${props.quiz.id}/submit`, payload)

    // Xóa timer khỏi sessionStorage sau khi nộp thành công
    sessionStorage.removeItem(STORAGE_KEY)

    // Gỡ beforeunload trước khi emit done
    window.removeEventListener('beforeunload', handleBeforeUnload)

    const choicesMap = {}
    props.quiz.questions.forEach(q => { choicesMap[q.id] = q.choices })
    emit('done', res.data, choicesMap)

  } catch(e) {
    alert(e.response?.data?.error || 'Nộp bài thất bại!')
    submitting.value = false
  }
}
</script>

<style scoped>
/* ── OVERLAY ─────────────────────────────────────────── */
.quiz-overlay {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(15,23,42,.55);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 1rem;
  animation: fadeIn .2s ease;
  /* cursor: default giúp không có hiệu ứng click ra ngoài */
}
@keyframes fadeIn { from { opacity:0 } to { opacity:1 } }

.quiz-modal {
  background: var(--surface);
  border-radius: 20px;
  width: 100%; max-width: 680px;
  max-height: 92vh;
  display: flex; flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 60px rgba(0,0,0,.18);
  animation: slideUp .3s cubic-bezier(.34,1.56,.64,1);
  position: relative;
}
@keyframes slideUp { from { transform:translateY(40px); opacity:0 } to { transform:translateY(0); opacity:1 } }

/* ── HEADER ───────────────────────────────────────────── */
.qm-header {
  display: flex; align-items: flex-start;
  justify-content: space-between; gap: 1rem;
  padding: 1.25rem 1.5rem 1rem;
  border-bottom: 1.5px solid var(--border);
  flex-shrink: 0;
}
.qm-badge {
  font-size: .7rem; font-weight: 700; letter-spacing: .05em;
  background: var(--accent-light); color: var(--accent);
  padding: .2rem .6rem; border-radius: 100px;
  margin-bottom: .35rem; display: inline-block;
}
.qm-title { font-size: 1.05rem; font-weight: 800; color: var(--text); line-height: 1.3; }
.qm-hd-right { display: flex; align-items: center; gap: .6rem; flex-shrink: 0; }

.qm-timer {
  display: flex; align-items: center; gap: .35rem;
  font-size: .82rem; font-weight: 700; font-variant-numeric: tabular-nums;
  color: var(--text2); background: var(--surface2);
  border: 1.5px solid var(--border); padding: .3rem .7rem;
  border-radius: 8px; transition: all .3s;
}
.timer-warn   { color: var(--yellow); border-color: var(--yellow-light); background: var(--yellow-light); }
.timer-danger { color: var(--red);    border-color: var(--red-light);    background: var(--red-light); animation: pulse .6s ease-in-out infinite alternate; }
@keyframes pulse { from { opacity:1 } to { opacity:.6 } }

.qm-close {
  width: 32px; height: 32px; border-radius: 8px;
  border: 1.5px solid var(--border); background: none;
  cursor: pointer; color: var(--muted); font-size: .95rem;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
}
.qm-close:hover { background: var(--red-light); color: var(--red); border-color: var(--red-light); }

/* ── PROGRESS ─────────────────────────────────────────── */
.qm-progress-wrap { height: 3px; background: var(--border); flex-shrink: 0; }
.qm-progress-bar {
  height: 100%; background: linear-gradient(90deg, var(--accent), #7c3aed);
  transition: width .35s ease; border-radius: 0 2px 2px 0;
}
.qm-prog-label { font-size: .75rem; color: var(--muted); padding: .5rem 1.5rem .3rem; flex-shrink: 0; }
.answered-count { color: var(--green); font-weight: 600; }

/* ── BODY ─────────────────────────────────────────────── */
.qm-body { flex: 1; overflow-y: auto; padding: 1.2rem 1.5rem; }
.question-wrap { display: flex; flex-direction: column; gap: .9rem; }
.q-num { font-size: .7rem; font-weight: 800; letter-spacing: .08em; color: var(--muted); text-transform: uppercase; }
.q-content { font-size: 1rem; font-weight: 600; line-height: 1.6; color: var(--text); }

/* ── CHOICES ──────────────────────────────────────────── */
.choices-list { display: flex; flex-direction: column; gap: .55rem; }
.choice-btn {
  display: flex; align-items: center; gap: .85rem;
  padding: .85rem 1rem; border-radius: 12px;
  border: 1.5px solid var(--border); background: var(--surface);
  cursor: pointer; text-align: left; width: 100%;
  transition: all .15s; color: var(--text);
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.choice-btn:hover { border-color: var(--accent); background: var(--accent-light); transform: translateX(3px); }
.choice-selected {
  border-color: var(--accent) !important;
  background: linear-gradient(135deg, #dbeafe, #ede9fe) !important;
  color: var(--accent);
}
.choice-letter {
  width: 28px; height: 28px; border-radius: 8px; flex-shrink: 0;
  background: var(--surface2); border: 1.5px solid var(--border);
  display: flex; align-items: center; justify-content: center;
  font-size: .78rem; font-weight: 800; color: var(--muted); transition: all .15s;
}
.choice-selected .choice-letter { background: var(--accent); color: #fff; border-color: var(--accent); }
.choice-text { flex: 1; font-size: .88rem; font-weight: 500; line-height: 1.45; }
@media(max-width:500px) {
  .quiz-modal { border-radius:0; max-height:100dvh; height:100dvh; }
  .quiz-overlay { padding:0; align-items:flex-start; }
  .qm-body { padding:.9rem 1rem; }
  .qm-footer { padding:.7rem 1rem; }
  .qm-header { padding:1rem 1rem .75rem; }
}
.choice-check { color: var(--accent); flex-shrink: 0; }

/* ── FOOTER ───────────────────────────────────────────── */
.qm-footer {
  flex-shrink: 0; border-top: 1.5px solid var(--border);
  padding: .9rem 1.5rem; display: flex; flex-direction: column; gap: .75rem;
}
.q-map { display: flex; flex-wrap: wrap; gap: .35rem; }
.q-dot {
  width: 28px; height: 28px; border-radius: 7px; font-size: .72rem;
  font-weight: 700; border: 1.5px solid var(--border);
  background: var(--surface2); cursor: pointer; color: var(--muted);
  transition: all .15s; font-family: inherit;
}
.dot-answered { background: var(--accent-light); border-color: var(--accent); color: var(--accent); }
.dot-current  { background: var(--accent); color: #fff; border-color: var(--accent); transform: scale(1.1); }

.qm-nav { display: flex; gap: .6rem; justify-content: flex-end; }
.btn-nav {
  display: flex; align-items: center; gap: .4rem;
  padding: .55rem 1.1rem; border-radius: 10px;
  border: 1.5px solid var(--border); background: var(--surface);
  cursor: pointer; font-size: .84rem; font-weight: 600;
  color: var(--text2); transition: all .15s; font-family: inherit;
}
.btn-nav:hover:not(:disabled) { background: var(--surface2); border-color: var(--border2); }
.btn-nav:disabled { opacity: .4; cursor: not-allowed; }
.btn-next { color: var(--accent); border-color: var(--accent-light); }
.btn-next:hover { background: var(--accent-light); }

.btn-submit {
  display: flex; align-items: center; gap: .45rem;
  padding: .55rem 1.3rem; border-radius: 10px; border: none;
  background: linear-gradient(135deg, var(--green), #10b981);
  color: #fff; cursor: pointer; font-size: .84rem; font-weight: 700;
  font-family: inherit; transition: all .15s; box-shadow: 0 2px 8px rgba(5,150,105,.3);
}
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(5,150,105,.4); }
.btn-submit:disabled { opacity: .7; cursor: not-allowed; transform: none; }

/* ── CONFIRM / EXIT DIALOG ────────────────────────────── */
.confirm-overlay {
  position: absolute; inset: 0; background: rgba(15,23,42,.5);
  border-radius: 20px; display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(3px); z-index: 10;
}
.confirm-box {
  background: var(--surface); border-radius: 16px; padding: 2rem;
  text-align: center; max-width: 320px; width: 100%;
  box-shadow: 0 20px 40px rgba(0,0,0,.15); animation: slideUp .25s ease;
}
.confirm-icon { font-size: 2.5rem; margin-bottom: .75rem; }
.confirm-box h3 { font-size: 1.1rem; font-weight: 800; margin-bottom: .5rem; }
.confirm-box p  { font-size: .88rem; color: var(--muted); margin-bottom: .4rem; }
.confirm-warn   { color: var(--yellow) !important; font-weight: 600; }
.confirm-actions { display: flex; gap: .75rem; margin-top: 1.2rem; }
.btn-cancel {
  flex: 1; padding: .6rem; border-radius: 10px;
  border: 1.5px solid var(--border); background: none;
  cursor: pointer; font-weight: 600; font-size: .85rem;
  font-family: inherit; color: var(--text2); transition: all .15s;
}
.btn-cancel:hover { background: var(--surface2); }
.btn-confirm-submit {
  flex: 1; padding: .6rem; border-radius: 10px; border: none;
  background: linear-gradient(135deg, var(--green), #10b981);
  color: #fff; cursor: pointer; font-weight: 700; font-size: .85rem;
  font-family: inherit; transition: all .15s;
}
.btn-confirm-submit:hover:not(:disabled) { opacity: .9; }
.btn-confirm-submit:disabled { opacity: .6; cursor: not-allowed; }
.btn-danger { background: linear-gradient(135deg, #ef4444, #dc2626) !important; }

/* ── TRANSITION ───────────────────────────────────────── */
.slide-q-enter-active, .slide-q-leave-active { transition: all .22s ease; }
.slide-q-enter-from { opacity:0; transform: translateX(20px); }
.slide-q-leave-to   { opacity:0; transform: translateX(-20px); }
</style>