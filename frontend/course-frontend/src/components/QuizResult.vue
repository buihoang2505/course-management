<template>
  <div class="quiz-overlay" @click.self="$emit('close')">
    <div class="result-modal">

      <!-- ── HEADER KẾT QUẢ ────────────────────────────── -->
      <div :class="['result-hero', result.passed ? 'hero-pass' : 'hero-fail']">
        <div class="hero-emoji">{{ result.passed ? '🎉' : '😢' }}</div>
        <h2 class="hero-title">{{ result.passed ? 'Xuất sắc!' : 'Chưa đạt!' }}</h2>
        <p class="hero-sub">{{ result.passed ? 'Bạn đã vượt qua bài quiz' : 'Hãy ôn lại và thử lại nhé' }}</p>

        <!-- Score Circle -->
        <div class="score-ring">
          <svg width="120" height="120" viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="52" fill="none" stroke="rgba(255,255,255,.2)" stroke-width="8"/>
            <circle cx="60" cy="60" r="52" fill="none"
                    stroke="white" stroke-width="8"
                    stroke-linecap="round"
                    :stroke-dasharray="`${result.score * 3.267} 326.7`"
                    stroke-dashoffset="81.7"
                    style="transition: stroke-dasharray 1s ease"
            />
          </svg>
          <div class="score-text">
            <span class="score-num">{{ result.score }}</span>
            <span class="score-unit">/100</span>
          </div>
        </div>

        <!-- Stats row -->
        <div class="hero-stats">
          <div class="hstat">
            <div class="hstat-n">{{ result.correctCount }}</div>
            <div class="hstat-l">Câu đúng</div>
          </div>
          <div class="hstat-div"></div>
          <div class="hstat">
            <div class="hstat-n">{{ result.totalQuestions - result.correctCount }}</div>
            <div class="hstat-l">Câu sai</div>
          </div>
          <div class="hstat-div"></div>
          <div class="hstat">
            <div class="hstat-n">{{ formattedTime }}</div>
            <div class="hstat-l">Thời gian</div>
          </div>
        </div>
      </div>

      <!-- ── CHI TIẾT TỪNG CÂU ─────────────────────────── -->
      <div class="result-body">
        <div class="rb-hd">
          <span class="rb-title">📝 Chi tiết từng câu</span>
          <div class="rb-filters">
            <button :class="['rf-btn', filter==='all'?'rf-active':'']" @click="filter='all'">Tất cả</button>
            <button :class="['rf-btn', filter==='correct'?'rf-active':'']" @click="filter='correct'">
              ✅ Đúng ({{ correctAnswers }})
            </button>
            <button :class="['rf-btn', filter==='wrong'?'rf-active':'']" @click="filter='wrong'">
              ❌ Sai ({{ wrongAnswers }})
            </button>
          </div>
        </div>

        <div class="answers-list">
          <div
              v-for="(a, i) in filteredAnswers" :key="a.questionId"
              :class="['answer-card', a.isCorrect ? 'card-correct' : 'card-wrong']"
          >
            <div class="ac-header">
              <span :class="['ac-badge', a.isCorrect ? 'badge-correct' : 'badge-wrong']">
                {{ a.isCorrect ? '✓ Đúng' : '✗ Sai' }}
              </span>
              <span class="ac-num">Câu {{ result.answers.indexOf(a) + 1 }}</span>
            </div>

            <p class="ac-question">{{ a.questionContent }}</p>

            <!-- Lựa chọn với highlight -->
            <div class="ac-choices">
              <!-- Đáp án đúng -->
              <div class="ac-choice ac-right">
                <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                <span>Đáp án đúng: <strong>{{ getChoiceContent(a, a.correctChoiceId) }}</strong></span>
              </div>
              <!-- Câu học sinh chọn (nếu sai) -->
              <div v-if="!a.isCorrect && a.selectedChoiceId" class="ac-choice ac-wrong-choice">
                <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                <span>Bạn chọn: <strong>{{ getChoiceContent(a, a.selectedChoiceId) }}</strong></span>
              </div>
              <div v-if="!a.isCorrect && !a.selectedChoiceId" class="ac-choice ac-skip">
                <span>⚠️ Bạn đã bỏ qua câu này</span>
              </div>
            </div>

            <!-- Giải thích -->
            <div v-if="a.explanation" class="ac-explain">
              <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              <span>{{ a.explanation }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- ── ACTIONS ─────────────────────────────────────── -->
      <div class="result-footer">
        <button class="btn-retry" v-if="!result.passed && canRetry" @click="$emit('retry')">
          🔄 Làm lại
        </button>
        <button class="btn-done" @click="$emit('close')">
          {{ result.passed ? '🎓 Tiếp tục học' : 'Đóng' }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  result:   { type: Object, required: true },
  canRetry: { type: Boolean, default: true },
  // Map questionId → choices[] để lấy nội dung choice
  choicesMap: { type: Object, default: () => ({}) },
})
defineEmits(['close', 'retry'])

const filter = ref('all')

const formattedTime = computed(() => {
  const s = props.result.timeSpentSeconds || 0
  const m = Math.floor(s / 60)
  const sec = s % 60
  return m > 0 ? `${m}p${String(sec).padStart(2,'0')}s` : `${sec}s`
})

const correctAnswers = computed(() => props.result.answers.filter(a => a.isCorrect).length)
const wrongAnswers   = computed(() => props.result.answers.filter(a => !a.isCorrect).length)

const filteredAnswers = computed(() => {
  if (filter.value === 'correct') return props.result.answers.filter(a =>  a.isCorrect)
  if (filter.value === 'wrong')   return props.result.answers.filter(a => !a.isCorrect)
  return props.result.answers
})

// Lấy nội dung choice từ choicesMap hoặc tự tìm trong result
function getChoiceContent(answer, choiceId) {
  if (!choiceId) return '—'
  const map = props.choicesMap[answer.questionId]
  if (map) {
    const found = map.find(c => c.id === choiceId)
    if (found) return found.content
  }
  return `#${choiceId}`
}
</script>

<style scoped>
.quiz-overlay {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(15,23,42,.55); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 1rem; animation: fadeIn .2s ease;
}
@keyframes fadeIn { from { opacity:0 } to { opacity:1 } }

.result-modal {
  background: var(--surface); border-radius: 20px;
  width: 100%; max-width: 640px; max-height: 92vh;
  display: flex; flex-direction: column; overflow: hidden;
  box-shadow: 0 25px 60px rgba(0,0,0,.18);
  animation: slideUp .3s cubic-bezier(.34,1.56,.64,1);
}
@keyframes slideUp { from { transform:translateY(40px);opacity:0 } to { transform:translateY(0);opacity:1 } }

/* ── HERO ──────────────────────────────────────────────── */
.result-hero {
  flex-shrink: 0; padding: 2rem 1.5rem 1.5rem;
  text-align: center; position: relative; overflow: hidden;
}
.hero-pass { background: linear-gradient(135deg, #059669, #10b981); }
.hero-fail { background: linear-gradient(135deg, #dc2626, #ef4444); }

.hero-emoji { font-size: 2.2rem; margin-bottom: .3rem; }
.hero-title { font-size: 1.5rem; font-weight: 900; color: #fff; margin-bottom: .2rem; }
.hero-sub   { font-size: .85rem; color: rgba(255,255,255,.75); margin-bottom: 1.2rem; }

.score-ring {
  position: relative; width: 120px; height: 120px;
  margin: 0 auto 1.2rem;
}
.score-text {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.score-num  { font-size: 2rem; font-weight: 900; color: #fff; line-height: 1; }
.score-unit { font-size: .75rem; color: rgba(255,255,255,.7); font-weight: 600; }

.hero-stats {
  display: flex; align-items: center; justify-content: center;
  gap: 1.5rem; flex-wrap: wrap;
}
.hstat { text-align: center; }
.hstat-n { font-size: 1.4rem; font-weight: 900; color: #fff; }
.hstat-l { font-size: .72rem; color: rgba(255,255,255,.7); font-weight: 600; margin-top: .1rem; }
.hstat-div { width: 1px; height: 32px; background: rgba(255,255,255,.2); }

/* ── BODY ─────────────────────────────────────────────── */
.result-body { flex: 1; overflow-y: auto; padding: 1.2rem 1.5rem; }

.rb-hd {
  display: flex; align-items: center; justify-content: space-between;
  flex-wrap: wrap; gap: .6rem; margin-bottom: 1rem;
}
.rb-title { font-size: .9rem; font-weight: 700; color: var(--text); }
.rb-filters { display: flex; gap: .4rem; }
.rf-btn {
  padding: .28rem .65rem; border-radius: 7px; font-size: .75rem;
  font-weight: 600; border: 1.5px solid var(--border);
  background: none; cursor: pointer; color: var(--muted);
  font-family: inherit; transition: all .15s;
}
.rf-btn:hover  { background: var(--surface2); }
.rf-active     { background: var(--accent-light); color: var(--accent); border-color: var(--accent-light); }

.answers-list  { display: flex; flex-direction: column; gap: .75rem; }

/* ── ANSWER CARD ──────────────────────────────────────── */
.answer-card {
  border-radius: 12px; border: 1.5px solid;
  overflow: hidden; transition: all .15s;
}
.card-correct { border-color: #a7f3d0; background: #f0fdf4; }
.card-wrong   { border-color: #fecaca; background: #fff5f5; }

.ac-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: .6rem .9rem .3rem;
}
.ac-badge {
  font-size: .72rem; font-weight: 800; padding: .18rem .6rem;
  border-radius: 100px;
}
.badge-correct { background: #d1fae5; color: #059669; }
.badge-wrong   { background: #fee2e2; color: #dc2626; }
.ac-num { font-size: .72rem; color: var(--muted); font-weight: 600; }

.ac-question {
  font-size: .88rem; font-weight: 600; color: var(--text);
  padding: .1rem .9rem .6rem; line-height: 1.5;
}

.ac-choices { padding: 0 .9rem .6rem; display: flex; flex-direction: column; gap: .35rem; }
.ac-choice {
  display: flex; align-items: center; gap: .5rem;
  font-size: .82rem; padding: .4rem .6rem; border-radius: 8px;
}
.ac-right        { background: #d1fae5; color: #059669; }
.ac-wrong-choice { background: #fee2e2; color: #dc2626; }
.ac-skip         { background: var(--yellow-light); color: var(--yellow); }

.ac-explain {
  display: flex; align-items: flex-start; gap: .45rem;
  font-size: .8rem; color: var(--muted); line-height: 1.5;
  padding: .5rem .9rem .8rem; border-top: 1px solid;
  border-color: inherit;
}

/* ── FOOTER ───────────────────────────────────────────── */
.result-footer {
  flex-shrink: 0; padding: 1rem 1.5rem;
  border-top: 1.5px solid var(--border);
  display: flex; gap: .75rem; justify-content: flex-end;
}
.btn-retry {
  padding: .6rem 1.3rem; border-radius: 10px;
  border: 1.5px solid var(--border); background: none;
  cursor: pointer; font-size: .85rem; font-weight: 600;
  color: var(--text2); font-family: inherit; transition: all .15s;
}
.btn-retry:hover { background: var(--surface2); }
.btn-done {
  padding: .6rem 1.5rem; border-radius: 10px; border: none;
  background: linear-gradient(135deg, var(--accent), #7c3aed);
  color: #fff; cursor: pointer; font-size: .85rem; font-weight: 700;
  font-family: inherit; transition: all .15s;
  box-shadow: 0 2px 8px rgba(37,99,235,.3);
}
.btn-done:hover { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(37,99,235,.4); }
</style>