<template>
  <div class="qa-wrap">
    <div class="qa-hd">
      <span class="qa-title">💬 Hỏi đáp</span>
      <span class="qa-count">{{ questions.length }} câu hỏi</span>
      <button v-if="loading" class="qa-refresh" disabled>⟳</button>
      <button v-else class="qa-refresh" @click="load" title="Làm mới">⟳</button>
    </div>

    <!-- Form đặt câu hỏi -->
    <div v-if="auth.isLoggedIn" class="ask-form">
      <div class="av" :style="auth.user?.avatar ? `background:url(${auth.user.avatar}) center/cover` : ''">
        {{ auth.user?.avatar ? '' : (auth.user?.username||'?')[0].toUpperCase() }}
      </div>
      <div class="ask-right">
        <textarea v-model="newQ" class="ask-input" :rows="askOpen?3:1"
                  placeholder="Đặt câu hỏi về bài học này..."
                  @focus="askOpen=true" maxlength="2000"></textarea>
        <div v-if="askOpen" class="ask-actions">
          <span class="char">{{ newQ.length }}/2000</span>
          <button class="btn-ghost-xs" @click="newQ='';askOpen=false">Hủy</button>
          <button class="btn-accent-xs" @click="submitQ"
                  :disabled="!newQ.trim()||submitting">
            {{ submitting?'…':'Gửi câu hỏi' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Skeleton / Empty -->
    <div v-if="loading" class="qa-state">
      <div class="spinner-sm"></div> Đang tải...
    </div>
    <div v-else-if="!questions.length" class="qa-state">
      🙋 Chưa có câu hỏi nào. Hãy là người đầu tiên!
    </div>

    <!-- Questions -->
    <div class="q-list">
      <div v-for="q in questions" :key="q.id" class="q-item" :class="{resolved:q.resolved}">
        <!-- Header -->
        <div class="q-head">
          <div class="av-sm" :style="q.authorAvatar?`background:url(${q.authorAvatar}) center/cover`:''">
            {{ q.authorAvatar?'':(q.author||'?')[0].toUpperCase() }}
          </div>
          <span class="q-author">{{ q.author }}</span>
          <span class="q-time">{{ ago(q.createdAt) }}</span>
          <span v-if="q.resolved" class="badge-ok">✅ Đã giải quyết</span>
          <div class="q-acts">
            <button v-if="canManage(q)" @click="toggleResolve(q)" class="act-btn"
                    :title="q.resolved?'Bỏ giải quyết':'Đánh dấu đã giải quyết'">
              {{ q.resolved?'↩':'✓' }}
            </button>
            <button v-if="canDel(q.authorId)" @click="delQ(q.id)" class="act-btn red">🗑</button>
          </div>
        </div>
        <p class="q-body">{{ q.content }}</p>

        <!-- Replies -->
        <div v-if="q.replies?.length" class="reply-list">
          <div v-for="r in q.replies" :key="r.id" class="r-item" :class="{official:r.official}">
            <div class="av-sm" :style="r.authorAvatar?`background:url(${r.authorAvatar}) center/cover`:''">
              {{ r.authorAvatar?'':(r.author||'?')[0].toUpperCase() }}
            </div>
            <div class="r-body">
              <div class="r-meta">
                <span class="r-author">{{ r.author }}</span>
                <span v-if="r.official" class="badge-inst">
                  {{ r.role==='ADMIN'?'🛡 Admin':'👨‍🏫 Giảng viên' }}
                </span>
                <span class="q-time">{{ ago(r.createdAt) }}</span>
              </div>
              <p class="r-body-text">{{ r.content }}</p>
            </div>
            <button v-if="canDel(r.authorId)" @click="delR(r.id,q)" class="act-btn red sm">🗑</button>
          </div>
        </div>

        <!-- Reply form -->
        <div class="reply-wrap">
          <button v-if="!replyOpen[q.id]" @click="replyOpen[q.id]=true;replyText[q.id]=''"
                  class="btn-reply">💬 Trả lời</button>
          <div v-else class="rf">
            <textarea v-model="replyText[q.id]" class="rf-input" rows="2"
                      placeholder="Viết câu trả lời..." maxlength="1000"
                      :ref="el=>el&&el.focus()"></textarea>
            <div class="rf-acts">
              <button class="btn-ghost-xs" @click="replyOpen[q.id]=false">Hủy</button>
              <button class="btn-accent-xs" @click="submitReply(q)"
                      :disabled="!replyText[q.id]?.trim()||replySubmit[q.id]">
                {{ replySubmit[q.id]?'…':'Gửi' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const props = defineProps({ lessonId: { type: Number, required: true } })
const auth  = useAuthStore()

const questions    = ref([])
const loading      = ref(false)
const newQ         = ref('')
const askOpen      = ref(false)
const submitting   = ref(false)
const replyOpen    = reactive({})
const replyText    = reactive({})
const replySubmit  = reactive({})

const role = auth.user?.role
const uid  = auth.user?.id

function canManage(q) {
  return uid === q.authorId || role === 'ADMIN' || role === 'INSTRUCTOR'
}
function canDel(authorId) {
  return uid === authorId || role === 'ADMIN'
}

function ago(iso) {
  if (!iso) return ''
  const m = Math.floor((Date.now() - new Date(iso)) / 60000)
  if (m < 1) return 'vừa xong'
  if (m < 60) return `${m} phút trước`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h} giờ trước`
  return `${Math.floor(h/24)} ngày trước`
}

async function load() {
  loading.value = true
  try {
    const r = await api.get(`/lessons/${props.lessonId}/questions`)
    questions.value = Array.isArray(r.data) ? r.data : []
  } catch(e) { console.error('[QA]', e) }
  finally { loading.value = false }
}

async function submitQ() {
  if (!newQ.value.trim()) return
  submitting.value = true
  try {
    const r = await api.post(`/lessons/${props.lessonId}/questions`, { content: newQ.value.trim() })
    questions.value.unshift(r.data)
    newQ.value = ''; askOpen.value = false
  } catch(e) { alert(e.response?.data?.error || 'Lỗi!') }
  finally { submitting.value = false }
}

async function toggleResolve(q) {
  try {
    const r = await api.patch(`/questions/${q.id}/resolve`)
    q.resolved = r.data.resolved
  } catch {}
}

async function delQ(qId) {
  if (!confirm('Xóa câu hỏi này?')) return
  try {
    await api.delete(`/questions/${qId}`)
    questions.value = questions.value.filter(q => q.id !== qId)
  } catch(e) { alert(e.response?.data?.error || 'Lỗi!') }
}

async function submitReply(q) {
  const content = replyText[q.id]?.trim()
  if (!content) return
  replySubmit[q.id] = true
  try {
    const r = await api.post(`/questions/${q.id}/replies`, { content })
    if (!q.replies) q.replies = []
    q.replies.push(r.data)
    if (r.data.official) q.resolved = true
    replyOpen[q.id] = false; replyText[q.id] = ''
  } catch(e) { alert(e.response?.data?.error || 'Lỗi!') }
  finally { replySubmit[q.id] = false }
}

async function delR(rId, q) {
  if (!confirm('Xóa câu trả lời này?')) return
  try {
    await api.delete(`/replies/${rId}`)
    q.replies = q.replies.filter(r => r.id !== rId)
  } catch(e) { alert(e.response?.data?.error || 'Lỗi!') }
}

onMounted(load)
</script>

<style scoped>
.qa-wrap { margin-top:1.5rem; border-top:1.5px solid var(--border); padding-top:1.2rem; }
.qa-hd { display:flex; align-items:center; gap:.6rem; margin-bottom:1rem; }
.qa-title { font-size:.92rem; font-weight:700; }
.qa-count { font-size:.74rem; color:var(--muted); background:var(--surface2); border:1px solid var(--border); padding:.12rem .5rem; border-radius:100px; }
.qa-refresh { background:none; border:none; cursor:pointer; color:var(--muted); font-size:.9rem; padding:.2rem; border-radius:4px; }
.qa-refresh:hover { color:var(--accent); background:var(--accent-light); }
.qa-state { color:var(--muted); font-size:.83rem; display:flex; align-items:center; gap:.5rem; padding:.75rem 0; }
.spinner-sm { width:16px; height:16px; border:2px solid var(--border); border-top-color:var(--accent); border-radius:50%; animation:spin .6s linear infinite; flex-shrink:0; }
@keyframes spin { to { transform:rotate(360deg); } }

/* Avatar */
.av { width:34px; height:34px; border-radius:50%; background:linear-gradient(135deg,var(--accent),var(--purple)); color:#fff; font-size:.8rem; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; background-size:cover; background-position:center; }
.av-sm { width:26px; height:26px; border-radius:50%; background:linear-gradient(135deg,var(--accent),var(--purple)); color:#fff; font-size:.68rem; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; background-size:cover; }

/* Ask form */
.ask-form { display:flex; gap:.65rem; margin-bottom:1.1rem; }
.ask-right { flex:1; display:flex; flex-direction:column; gap:.4rem; }
.ask-input { width:100%; padding:.55rem .8rem; background:var(--surface); border:1.5px solid var(--border); border-radius:9px; color:var(--text); font-size:.84rem; font-family:inherit; resize:none; outline:none; transition:border-color .15s; box-sizing:border-box; }
.ask-input:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }
.ask-actions { display:flex; align-items:center; gap:.4rem; justify-content:flex-end; }
.char { font-size:.7rem; color:var(--muted); margin-right:auto; }

/* Question */
.q-list { display:flex; flex-direction:column; gap:.7rem; }
.q-item { background:var(--surface2); border:1.5px solid var(--border); border-radius:11px; padding:.9rem; transition:border-color .15s; }
.q-item.resolved { border-color:#a7f3d0; background:#f0fdf4; }
.q-item:hover { border-color:var(--accent); }
.q-head { display:flex; align-items:center; gap:.5rem; margin-bottom:.5rem; flex-wrap:wrap; }
.q-author { font-size:.81rem; font-weight:700; }
.q-time { font-size:.7rem; color:var(--muted); }
.badge-ok { font-size:.65rem; font-weight:700; padding:.1rem .45rem; border-radius:100px; background:var(--green-light); color:var(--green); border:1px solid #a7f3d0; }
.q-acts { display:flex; gap:.25rem; margin-left:auto; }
.q-body { font-size:.84rem; color:var(--text); line-height:1.65; margin:0 0 .7rem; white-space:pre-wrap; }

/* Replies */
.reply-list { padding-left:1rem; border-left:2px solid var(--border); display:flex; flex-direction:column; gap:.5rem; margin-bottom:.7rem; }
.r-item { display:flex; gap:.5rem; align-items:flex-start; }
.r-item.official { background:#f0fdf4; border-radius:8px; padding:.45rem; }
.r-body { flex:1; }
.r-meta { display:flex; align-items:center; gap:.4rem; margin-bottom:.2rem; flex-wrap:wrap; }
.r-author { font-size:.79rem; font-weight:700; }
.badge-inst { font-size:.63rem; font-weight:700; padding:.1rem .38rem; border-radius:100px; background:#dbeafe; color:var(--accent); }
.r-body-text { font-size:.82rem; color:var(--text); line-height:1.55; margin:0; white-space:pre-wrap; }

/* Action buttons */
.act-btn { width:24px; height:24px; display:flex; align-items:center; justify-content:center; border-radius:5px; background:none; border:1px solid var(--border); cursor:pointer; font-size:.72rem; color:var(--muted); transition:all .15s; }
.act-btn:hover { background:var(--accent-light); color:var(--accent); border-color:var(--accent); }
.act-btn.red:hover { background:var(--red-light); color:var(--red); border-color:#fca5a5; }
.act-btn.sm { width:20px; height:20px; font-size:.65rem; }

/* Reply form */
.reply-wrap { margin-top:.3rem; }
.btn-reply { background:none; border:none; cursor:pointer; font-size:.74rem; color:var(--muted); font-family:inherit; padding:.25rem 0; transition:color .15s; }
.btn-reply:hover { color:var(--accent); }
.rf { display:flex; flex-direction:column; gap:.4rem; }
.rf-input { width:100%; padding:.45rem .75rem; background:var(--surface); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.82rem; font-family:inherit; resize:none; outline:none; transition:border-color .15s; box-sizing:border-box; }
.rf-input:focus { border-color:var(--accent); }
.rf-acts { display:flex; justify-content:flex-end; gap:.4rem; }

/* Micro buttons */
.btn-ghost-xs { padding:.28rem .65rem; background:var(--surface); border:1.5px solid var(--border); border-radius:7px; font-size:.75rem; font-weight:600; cursor:pointer; font-family:inherit; color:var(--text2); transition:all .15s; }
.btn-ghost-xs:hover { border-color:var(--accent); color:var(--accent); }
.btn-accent-xs { padding:.28rem .65rem; background:var(--accent); border:none; border-radius:7px; font-size:.75rem; font-weight:600; cursor:pointer; font-family:inherit; color:#fff; transition:opacity .15s; }
.btn-accent-xs:disabled { opacity:.5; cursor:not-allowed; }
.btn-accent-xs:not(:disabled):hover { opacity:.88; }
</style>