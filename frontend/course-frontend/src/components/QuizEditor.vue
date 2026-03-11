<template>
  <div class="editor-wrap">

    <!-- ── CHỌN BÀI HỌC (chỉ hiện khi tạo mới) ───────── -->
    <div v-if="!isEdit" class="editor-section">
      <div class="es-hd">
        <span class="es-icon">📖</span>
        <h3 class="es-title">Gắn quiz vào bài học</h3>
      </div>
      <div class="form-grid" style="padding:1rem 1.2rem">
        <div class="fg-col-2">
          <label class="flabel">Khóa học <span class="req">*</span></label>
          <select v-model="selectedCourseId" @change="onCourseChange" class="finput">
            <option value="">-- Chọn khóa học --</option>
            <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.title }}</option>
          </select>
        </div>
        <div class="fg-col-2">
          <label class="flabel">Bài học <span class="req">*</span></label>
          <select v-model="selectedLessonId" class="finput" :disabled="!selectedCourseId || loadingLessons">
            <option value="">
              {{ loadingLessons ? 'Đang tải...' : (selectedCourseId ? '-- Chọn bài học --' : 'Chọn khóa học trước') }}
            </option>
            <option v-for="l in lessons" :key="l.id" :value="l.id">
              {{ l.orderNum }}. {{ l.title }}
              <template v-if="l.hasQuiz"> (đã có quiz)</template>
            </option>
          </select>
          <p v-if="selectedLesson?.hasQuiz" class="warn-text">
            ⚠️ Bài học này đã có quiz. Tạo mới sẽ ghi đè quiz cũ!
          </p>
        </div>

        <!-- Preview bài học đã chọn -->
        <div v-if="selectedLesson" class="fg-col-2 lesson-preview">
          <div class="lp-icon">📝</div>
          <div class="lp-info">
            <div class="lp-title">{{ selectedLesson.title }}</div>
            <div class="lp-sub">Bài {{ selectedLesson.orderNum }} · {{ selectedCourse?.title }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Hiển thị info bài học khi đang edit -->
    <div v-else class="editor-section">
      <div class="es-hd">
        <span class="es-icon">📖</span>
        <h3 class="es-title">Bài học đính kèm</h3>
      </div>
      <div class="lesson-preview" style="padding:1rem 1.2rem">
        <div class="lp-icon">📝</div>
        <div class="lp-info">
          <div class="lp-title">{{ existingQuiz?.lessonTitle || 'Không rõ bài học' }}</div>
          <div class="lp-sub">{{ existingQuiz?.courseTitle || '' }}</div>
        </div>
      </div>
    </div>

    <!-- ── QUIZ INFO ────────────────────────────────────── -->
    <div class="editor-section">
      <div class="es-hd">
        <span class="es-icon">⚙️</span>
        <h3 class="es-title">Cài đặt Quiz</h3>
      </div>
      <div class="form-grid">
        <div class="fg-col-2">
          <label class="flabel">Tiêu đề quiz <span class="req">*</span></label>
          <input v-model="form.title" class="finput" placeholder="VD: Kiểm tra chương 1"/>
        </div>
        <div class="fg-col-2">
          <label class="flabel">Mô tả</label>
          <input v-model="form.description" class="finput" placeholder="Mô tả ngắn về bài quiz"/>
        </div>
        <div class="fg-col-1">
          <label class="flabel">Giới hạn thời gian (phút)</label>
          <input v-model.number="form.timeLimitMinutes" class="finput" type="number" min="1" placeholder="Để trống = không giới hạn"/>
        </div>
        <div class="fg-col-1">
          <label class="flabel">Điểm đạt (%)</label>
          <input v-model.number="form.passingScore" class="finput" type="number" min="0" max="100" placeholder="60"/>
        </div>
        <div class="fg-col-1">
          <label class="flabel">Số lần làm tối đa</label>
          <input v-model.number="form.maxAttempts" class="finput" type="number" min="1" placeholder="Để trống = không giới hạn"/>
        </div>
        <div class="fg-col-1">
          <label class="flabel">Trạng thái</label>
          <select v-model="form.isActive" class="finput">
            <option :value="true">✅ Hoạt động</option>
            <option :value="false">⏸ Ẩn</option>
          </select>
        </div>
      </div>
    </div>

    <!-- ── QUESTIONS ─────────────────────────────────────── -->
    <div class="editor-section">
      <div class="es-hd">
        <div class="es-hd-left">
          <span class="es-icon">📝</span>
          <h3 class="es-title">Câu hỏi <span class="q-count">{{ questions.length }}</span></h3>
        </div>
        <button class="btn-add-q" @click="addQuestion">
          <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Thêm câu hỏi
        </button>
      </div>

      <div v-if="!questions.length" class="empty-q">
        <div class="eq-icon">🧩</div>
        <p>Chưa có câu hỏi nào. Nhấn "Thêm câu hỏi" để bắt đầu!</p>
      </div>

      <div class="questions-list">
        <div v-for="(q, qi) in questions" :key="q._id" class="q-card">

          <!-- Q Header -->
          <div class="qc-header" @click="q._open = !q._open">
            <div class="qc-hd-left">
              <div class="qc-num">{{ qi + 1 }}</div>
              <div class="qc-preview">{{ q.content || 'Câu hỏi chưa điền...' }}</div>
            </div>
            <div class="qc-hd-right">
              <span :class="['qc-valid', isQuestionValid(q) ? 'valid-ok' : 'valid-no']">
                {{ isQuestionValid(q) ? '✓' : '!' }}
              </span>
              <button class="btn-del-q" @click.stop="removeQuestion(qi)">🗑️</button>
              <svg :class="['qc-chevron', q._open ? 'ch-open' : '']" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="6 9 12 15 18 9"/></svg>
            </div>
          </div>

          <!-- Q Body -->
          <div class="qc-body" :style="q._open ? 'max-height:800px' : 'max-height:0'">
            <div class="qc-inner">

              <!-- Nội dung câu hỏi -->
              <div class="field-wrap">
                <label class="flabel">Nội dung câu hỏi <span class="req">*</span></label>
                <textarea v-model="q.content" class="ftextarea" rows="2"
                          placeholder="Nhập nội dung câu hỏi..."></textarea>
              </div>

              <!-- Giải thích -->
              <div class="field-wrap">
                <label class="flabel">Giải thích đáp án</label>
                <textarea v-model="q.explanation" class="ftextarea" rows="1"
                          placeholder="Giải thích tại sao đáp án này đúng (hiển thị sau khi làm bài)..."></textarea>
              </div>

              <!-- Điểm -->
              <div class="field-wrap" style="max-width:140px">
                <label class="flabel">Điểm</label>
                <input v-model.number="q.points" type="number" min="1" class="finput"/>
              </div>

              <!-- Choices -->
              <div class="choices-editor">
                <div class="ce-hd">
                  <label class="flabel">Các lựa chọn <span class="req">*</span></label>
                  <span class="ce-hint">Click ⭕ để chọn đáp án đúng</span>
                </div>

                <div v-for="(c, ci) in q.choices" :key="ci" class="choice-row">
                  <button
                      :class="['choice-radio', c.isCorrect ? 'radio-correct' : '']"
                      @click="setCorrect(q, ci)"
                      :title="c.isCorrect ? 'Đáp án đúng' : 'Đặt làm đáp án đúng'"
                  >
                    <svg v-if="c.isCorrect" width="12" height="12" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                    <span v-else class="choice-letter-label">{{ ['A','B','C','D'][ci] }}</span>
                  </button>
                  <input
                      v-model="c.content"
                      :class="['finput', 'choice-input', c.isCorrect ? 'input-correct' : '']"
                      :placeholder="`Lựa chọn ${['A','B','C','D'][ci]}...`"
                  />
                  <button v-if="q.choices.length > 2" class="btn-del-choice" @click="removeChoice(q, ci)">✕</button>
                </div>

                <button v-if="q.choices.length < 6" class="btn-add-choice" @click="addChoice(q)">
                  + Thêm lựa chọn
                </button>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ── VALIDATION SUMMARY ─────────────────────────── -->
    <div v-if="errors.length" class="validation-box">
      <div v-for="e in errors" :key="e" class="val-error">⚠️ {{ e }}</div>
    </div>

    <!-- ── ACTIONS ────────────────────────────────────── -->
    <div class="editor-actions">
      <button class="btn-cancel-editor" @click="$emit('cancel')">Hủy</button>
      <button class="btn-save" @click="save" :disabled="saving || !!errors.length">
        <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
        {{ saving ? 'Đang lưu...' : (isEdit ? 'Cập nhật Quiz' : 'Tạo Quiz') }}
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import api from '../services/api'

const props = defineProps({
  lessonId:     { type: Number, default: null },    // truyền sẵn nếu biết (tùy chọn)
  existingQuiz: { type: Object, default: null },    // null = tạo mới
})
const emit = defineEmits(['saved', 'cancel'])

const isEdit = computed(() => !!props.existingQuiz)
const saving = ref(false)
let _id = 0

// ── Chọn khóa học / bài học (khi tạo mới) ───────────────
const courses        = ref([])
const lessons        = ref([])
const loadingLessons = ref(false)
const selectedCourseId = ref('')
const selectedLessonId = ref(props.lessonId || '')

const selectedCourse = computed(() => courses.value.find(c => c.id == selectedCourseId.value))
const selectedLesson = computed(() => lessons.value.find(l => l.id == selectedLessonId.value))

onMounted(async () => {
  if (isEdit.value) return  // edit mode không cần load
  try {
    const res = await api.get('/courses')
    courses.value = res.data?.content || res.data || []
  } catch(e) {
    console.error('Không tải được danh sách khóa học', e)
  }

  // Nếu có lessonId truyền sẵn → tìm course tương ứng
  if (props.lessonId) {
    selectedLessonId.value = props.lessonId
  }
})

async function onCourseChange() {
  selectedLessonId.value = ''
  lessons.value = []
  if (!selectedCourseId.value) return
  loadingLessons.value = true
  try {
    const res = await api.get(`/lessons/course/${selectedCourseId.value}`)
    // Đánh dấu lesson nào đã có quiz (nếu API hỗ trợ)
    lessons.value = res.data || []
  } catch(e) {
    console.error('Không tải được danh sách bài học', e)
  } finally {
    loadingLessons.value = false
  }
}

// ── Form state ────────────────────────────────────────────
const form = reactive({
  title:             props.existingQuiz?.title            || '',
  description:       props.existingQuiz?.description      || '',
  timeLimitMinutes:  props.existingQuiz?.timeLimitMinutes || null,
  passingScore:      props.existingQuiz?.passingScore     || 60,
  maxAttempts:       props.existingQuiz?.maxAttempts      || null,
  isActive:          props.existingQuiz?.isActive         ?? true,
})

// Load existing questions hoặc tạo mới
const questions = ref(
    props.existingQuiz?.questions?.map(q => ({
      _id: ++_id, _open: false,
      id: q.id,
      content:     q.content || '',
      explanation: q.explanation || '',
      points:      q.points || 1,
      // QuestionAdminDTO dùng isCorrect (boolean primitive)
      choices: q.choices?.map(c => ({
        id: c.id,
        content: c.content,
        isCorrect: c.isCorrect === true || c.correct === true  // tương thích cả 2 field name
      })) || defaultChoices(),
    })) || []
)

function defaultChoices() {
  return [
    { content: '', isCorrect: false },
    { content: '', isCorrect: false },
    { content: '', isCorrect: false },
    { content: '', isCorrect: false },
  ]
}

// ── Question management ───────────────────────────────────
function addQuestion() {
  questions.value.push({
    _id: ++_id, _open: true,
    content: '', explanation: '', points: 1,
    choices: defaultChoices(),
  })
}

async function removeQuestion(idx) {
  const q = questions.value[idx]
  if (!confirm('Xóa câu hỏi này?')) return

  // Nếu câu hỏi đã tồn tại trên server (có id) → xóa ngay qua API
  if (q.id) {
    try {
      await api.delete(`/quizzes/questions/${q.id}`)
    } catch(e) {
      alert(e.response?.data?.error || 'Xóa câu hỏi thất bại!')
      return
    }
  }
  questions.value.splice(idx, 1)
}

function addChoice(q) {
  q.choices.push({ content: '', isCorrect: false })
}

function removeChoice(q, ci) {
  q.choices.splice(ci, 1)
}

function setCorrect(q, ci) {
  q.choices.forEach((c, i) => c.isCorrect = i === ci)
}

function isQuestionValid(q) {
  return q.content.trim() &&
      q.choices.length >= 2 &&
      q.choices.every(c => c.content.trim()) &&
      q.choices.filter(c => c.isCorrect).length === 1
}

// ── Validation ────────────────────────────────────────────
const errors = computed(() => {
  const errs = []
  // Khi tạo mới: bắt buộc chọn bài học
  if (!isEdit.value && !selectedLessonId.value) {
    errs.push('Vui lòng chọn bài học để gắn quiz')
  }
  if (!form.title.trim()) errs.push('Tiêu đề quiz không được để trống')
  if (!questions.value.length) errs.push('Quiz phải có ít nhất 1 câu hỏi')
  questions.value.forEach((q, i) => {
    if (!q.content.trim()) errs.push(`Câu ${i+1}: Chưa nhập nội dung`)
    if (!q.choices.every(c => c.content.trim())) errs.push(`Câu ${i+1}: Các lựa chọn chưa đủ nội dung`)
    if (q.choices.filter(c => c.isCorrect).length !== 1) errs.push(`Câu ${i+1}: Phải chọn đúng 1 đáp án đúng`)
  })
  return errs
})

// ── Save ─────────────────────────────────────────────────
async function save() {
  if (errors.value.length) return
  saving.value = true
  try {
    let quizId
    const lessonIdToUse = isEdit.value ? props.existingQuiz.lessonId : selectedLessonId.value

    const quizPayload = {
      title:            form.title,
      description:      form.description || null,
      timeLimitMinutes: form.timeLimitMinutes || null,
      passingScore:     form.passingScore || 60,
      maxAttempts:      form.maxAttempts || null,
      isActive:         form.isActive,
    }

    if (isEdit.value) {
      await api.put(`/quizzes/${props.existingQuiz.id}`, quizPayload)
      quizId = props.existingQuiz.id
    } else {
      const res = await api.post(`/quizzes?lessonId=${lessonIdToUse}`, quizPayload)
      quizId = res.data.id
    }

    // Lưu từng câu hỏi — AddQuestionRequest format
    for (const q of questions.value) {
      const payload = {
        content:     q.content,
        explanation: q.explanation || null,
        points:      q.points || 1,
        choices:     q.choices.map(c => ({ content: c.content, isCorrect: c.isCorrect }))
      }
      if (isEdit.value && q.id) {
        await api.put(`/quizzes/questions/${q.id}`, payload)
      } else {
        await api.post(`/quizzes/${quizId}/questions`, payload)
      }
    }

    emit('saved', quizId)
  } catch(e) {
    alert(e.response?.data?.error || 'Lưu thất bại!')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.editor-wrap { display: flex; flex-direction: column; gap: 1.2rem; }

/* ── SECTION ──────────────────────────────────────────── */
.editor-section {
  background: var(--surface); border: 1.5px solid var(--border);
  border-radius: 14px; overflow: hidden;
}
.es-hd {
  display: flex; align-items: center; justify-content: space-between;
  padding: .9rem 1.2rem; border-bottom: 1.5px solid var(--border);
  background: var(--surface2);
}
.es-hd-left { display: flex; align-items: center; gap: .5rem; }
.es-icon { font-size: 1rem; }
.es-title { font-size: .92rem; font-weight: 700; color: var(--text); }
.q-count {
  display: inline-flex; align-items: center; justify-content: center;
  width: 20px; height: 20px; border-radius: 100px;
  background: var(--accent-light); color: var(--accent);
  font-size: .72rem; font-weight: 800; margin-left: .3rem;
}

/* ── LESSON PREVIEW ───────────────────────────────────── */
.lesson-preview {
  display: flex; align-items: center; gap: .85rem;
  background: var(--accent-light); border: 1.5px solid #bfdbfe;
  border-radius: 10px; padding: .8rem 1rem;
}
.lp-icon { font-size: 1.4rem; flex-shrink: 0; }
.lp-title { font-size: .88rem; font-weight: 700; color: var(--accent); }
.lp-sub   { font-size: .74rem; color: var(--muted); margin-top: .1rem; }

.warn-text {
  font-size: .76rem; color: var(--yellow); font-weight: 600;
  margin-top: .4rem;
}

/* ── FORM GRID ────────────────────────────────────────── */
.form-grid {
  display: grid; grid-template-columns: 1fr 1fr;
  gap: .9rem; padding: 1rem 1.2rem;
}
.fg-col-2 { grid-column: span 2; }
.fg-col-1 { grid-column: span 1; }
@media (max-width: 580px) { .form-grid { grid-template-columns: 1fr; } .fg-col-2,.fg-col-1 { grid-column: span 1; } }
@media(max-width:500px) {
  .editor-actions { flex-direction:column; }
  .btn-save, .btn-cancel-editor { width:100%; justify-content:center; }
  .choice-row { flex-wrap:wrap; }
  .choice-input { min-width:0; }
}

.flabel { display: block; font-size: .76rem; font-weight: 700; color: var(--muted); margin-bottom: .35rem; }
.req { color: var(--red); }
.finput, .ftextarea {
  width: 100%; padding: .6rem .85rem; border-radius: 9px;
  border: 1.5px solid var(--border); background: var(--bg);
  color: var(--text); font-size: .87rem; outline: none;
  font-family: 'Plus Jakarta Sans', sans-serif; transition: border-color .15s;
  box-sizing: border-box;
}
.finput:focus, .ftextarea:focus { border-color: var(--accent); }
.ftextarea { resize: vertical; }
.finput:disabled { opacity: .5; cursor: not-allowed; }

/* ── QUESTION CARD ────────────────────────────────────── */
.questions-list { padding: .75rem 1.2rem; display: flex; flex-direction: column; gap: .75rem; }

.q-card { border: 1.5px solid var(--border); border-radius: 12px; overflow: hidden; }

.qc-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: .75rem 1rem; cursor: pointer; background: var(--surface);
  gap: .75rem; transition: background .15s;
}
.qc-header:hover { background: var(--surface2); }
.qc-hd-left { display: flex; align-items: center; gap: .75rem; flex: 1; min-width: 0; }
.qc-num {
  width: 26px; height: 26px; border-radius: 7px; flex-shrink: 0;
  background: var(--accent-light); color: var(--accent);
  display: flex; align-items: center; justify-content: center;
  font-size: .78rem; font-weight: 800;
}
.qc-preview { font-size: .84rem; color: var(--text2); font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.qc-hd-right { display: flex; align-items: center; gap: .5rem; flex-shrink: 0; }
.qc-valid { width: 20px; height: 20px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: .72rem; font-weight: 800; }
.valid-ok { background: var(--green-light); color: var(--green); }
.valid-no { background: var(--red-light); color: var(--red); }
.btn-del-q { background: none; border: none; cursor: pointer; font-size: .85rem; padding: .2rem; opacity: .5; transition: opacity .15s; }
.btn-del-q:hover { opacity: 1; }
.qc-chevron { color: var(--muted); transition: transform .2s; flex-shrink: 0; }
.ch-open { transform: rotate(180deg); }

.qc-body { overflow: hidden; transition: max-height .3s ease; }
.qc-inner { padding: 1rem; border-top: 1.5px solid var(--border); display: flex; flex-direction: column; gap: .75rem; }

.field-wrap { display: flex; flex-direction: column; }

/* ── CHOICES EDITOR ───────────────────────────────────── */
.choices-editor { display: flex; flex-direction: column; gap: .5rem; }
.ce-hd { display: flex; align-items: center; justify-content: space-between; }
.ce-hint { font-size: .72rem; color: var(--muted); }

.choice-row { display: flex; align-items: center; gap: .6rem; }
.choice-radio {
  width: 30px; height: 30px; border-radius: 8px; flex-shrink: 0;
  border: 1.5px solid var(--border); background: var(--surface2);
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all .15s; color: var(--muted);
}
.choice-radio:hover  { border-color: var(--accent); }
.radio-correct { background: var(--green); border-color: var(--green); color: #fff; }
.choice-letter-label { font-size: .72rem; font-weight: 800; }

.choice-input { flex: 1; }
.input-correct { border-color: var(--green); background: #f0fdf4; }

.btn-del-choice {
  background: none; border: none; cursor: pointer; color: var(--muted);
  font-size: .8rem; width: 24px; flex-shrink: 0; transition: color .15s;
}
.btn-del-choice:hover { color: var(--red); }

.btn-add-choice {
  align-self: flex-start; font-size: .78rem; font-weight: 600;
  color: var(--accent); background: var(--accent-light); border: none;
  padding: .35rem .75rem; border-radius: 7px; cursor: pointer;
  font-family: inherit; transition: all .15s;
}
.btn-add-choice:hover { opacity: .85; }

/* ── ADD QUESTION ─────────────────────────────────────── */
.btn-add-q {
  display: flex; align-items: center; gap: .4rem;
  padding: .45rem .9rem; border-radius: 9px;
  border: 1.5px solid var(--accent); color: var(--accent);
  background: var(--accent-light); cursor: pointer;
  font-size: .8rem; font-weight: 700; font-family: inherit; transition: all .15s;
}
.btn-add-q:hover { background: var(--accent); color: #fff; }

.empty-q {
  text-align: center; padding: 2.5rem 1rem;
  color: var(--muted); font-size: .88rem;
}
.eq-icon { font-size: 2.5rem; margin-bottom: .5rem; }

/* ── VALIDATION ───────────────────────────────────────── */
.validation-box {
  background: var(--red-light); border: 1.5px solid #fca5a5;
  border-radius: 10px; padding: .8rem 1rem;
  display: flex; flex-direction: column; gap: .3rem;
}
.val-error { font-size: .82rem; color: var(--red); font-weight: 600; }

/* ── ACTIONS ──────────────────────────────────────────── */
.editor-actions {
  display: flex; justify-content: flex-end; gap: .75rem;
  padding-bottom: .5rem;
}
.btn-cancel-editor {
  padding: .6rem 1.3rem; border-radius: 10px;
  border: 1.5px solid var(--border); background: none;
  cursor: pointer; font-size: .85rem; font-weight: 600;
  color: var(--text2); font-family: inherit; transition: all .15s;
}
.btn-cancel-editor:hover { background: var(--surface2); }
.btn-save {
  display: flex; align-items: center; gap: .45rem;
  padding: .6rem 1.5rem; border-radius: 10px; border: none;
  background: linear-gradient(135deg, var(--accent), #7c3aed);
  color: #fff; cursor: pointer; font-size: .85rem; font-weight: 700;
  font-family: inherit; transition: all .15s;
  box-shadow: 0 2px 8px rgba(37,99,235,.25);
}
.btn-save:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(37,99,235,.4); }
.btn-save:disabled { opacity: .5; cursor: not-allowed; transform: none; }
</style>