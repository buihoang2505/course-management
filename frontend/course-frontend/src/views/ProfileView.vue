<template>
  <div class="page">
    <div class="profile-wrap">

      <!-- CỘT TRÁI -->
      <aside class="left-col">
        <div class="avatar-card">
          <div class="avatar-wrap">
            <div class="avatar">{{ initials }}</div>
          </div>
          <div class="p-name">{{ saved.username }}</div>
          <div class="p-email">{{ saved.email }}</div>
          <span :class="['role-tag', auth.user?.role?.toLowerCase()]">{{ auth.user?.role }}</span>
        </div>

        <div class="mini-stats">
          <div class="ms-row">
            <span class="ms-icon">📚</span>
            <span class="ms-lbl">Khóa học</span>
            <span class="ms-val">{{ stats.courses }}</span>
          </div>
          <div class="ms-row">
            <span class="ms-icon">🎯</span>
            <span class="ms-lbl">Có điểm</span>
            <span class="ms-val">{{ stats.graded }}</span>
          </div>
          <div class="ms-row">
            <span class="ms-icon">⭐</span>
            <span class="ms-lbl">Điểm TB</span>
            <span class="ms-val accent">{{ stats.avg }}</span>
          </div>
        </div>

        <!-- Thông tin phụ đã lưu -->
        <div class="extra-info" v-if="saved.fullName || saved.phone || saved.bio">
          <div class="ei-title">Thông tin đã lưu</div>
          <div v-if="saved.fullName" class="ei-row">
            <span class="ei-lbl">Họ và tên</span>
            <span class="ei-val">{{ saved.fullName }}</span>
          </div>
          <div v-if="saved.phone" class="ei-row">
            <span class="ei-lbl">Điện thoại</span>
            <span class="ei-val">{{ saved.phone }}</span>
          </div>
          <div v-if="saved.bio" class="ei-row full">
            <span class="ei-lbl">Giới thiệu</span>
            <span class="ei-val bio">{{ saved.bio }}</span>
          </div>
        </div>
      </aside>

      <!-- CỘT PHẢI -->
      <div class="right-col">

        <!-- Form cập nhật -->
        <div class="form-card">
          <div class="fc-header">
            <h2>✏️ Cập nhật thông tin</h2>
          </div>
          <div class="fc-body">
            <div v-if="successMsg" class="alert success-alert">✅ {{ successMsg }}</div>
            <div v-if="errorMsg"   class="alert error-alert">❌ {{ errorMsg }}</div>

            <form @submit.prevent="save">
              <div class="form-2col">
                <div class="form-group">
                  <label>Username</label>
                  <input v-model="form.username" required minlength="3" />
                </div>
                <div class="form-group">
                  <label>Email</label>
                  <input v-model="form.email" type="email" required />
                </div>
                <div class="form-group">
                  <label>Họ và tên</label>
                  <input v-model="form.fullName" placeholder="Nguyễn Văn A" />
                </div>
                <div class="form-group">
                  <label>Số điện thoại</label>
                  <input v-model="form.phone" placeholder="0901234567" />
                </div>
              </div>
              <div class="form-group">
                <label>Giới thiệu bản thân</label>
                <textarea v-model="form.bio" rows="3" placeholder="Mô tả ngắn về bạn..."></textarea>
              </div>

              <div class="pw-divider">
                <span>🔒 Đổi mật khẩu</span>
                <small>Để trống nếu không muốn đổi</small>
              </div>
              <div class="form-group">
                <label>Mật khẩu mới</label>
                <input v-model="form.password" type="password" placeholder="Ít nhất 8 ký tự, chữ hoa + số" />
              </div>

              <button type="submit" class="save-btn" :disabled="saving">
                {{ saving ? '⏳ Đang lưu...' : '💾 Lưu thay đổi' }}
              </button>
            </form>
          </div>
        </div>

        <!-- Lịch sử -->
        <div class="history-card">
          <h2>📖 Lịch sử học tập</h2>
          <div v-if="loadingH" class="loading-sm">⏳ Đang tải...</div>
          <div v-else-if="!enrollments.length" class="empty-sm">Chưa đăng ký khóa học nào.</div>
          <div v-else class="h-list">
            <div v-for="e in enrollments" :key="e.id" class="h-row">
              <div class="h-info">
                <div class="h-name">{{ e.course?.title }}</div>
                <div class="h-meta">
                  <span>👨‍🏫 {{ e.course?.instructor || '—' }}</span>
                  <span>📅 {{ fmtDate(e.enrolledAt) }}</span>
                </div>
              </div>
              <div class="h-right">
                <span :class="['status-tag', e.status?.toLowerCase()]">{{ e.status }}</span>
                <div v-if="e.grade" :class="['grade-tag', sc(e.grade?.score)]">{{ e.grade?.score?.toFixed(1) }}</div>
                <div v-else class="grade-empty">—</div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

// saved = dữ liệu đang HIỂN THỊ (update sau khi lưu)
const saved = reactive({
  username: auth.user?.username || '',
  email:    auth.user?.email    || '',
  fullName: '', phone: '', bio: ''
})

// form = dữ liệu ĐANG NHẬP
const form = ref({
  username: auth.user?.username || '',
  email:    auth.user?.email    || '',
  fullName: '', phone: '', bio: '', password: ''
})

const saving = ref(false)
const successMsg = ref('')
const errorMsg   = ref('')
const enrollments = ref([])
const loadingH = ref(false)

const initials = computed(() => (saved.username || 'U').charAt(0).toUpperCase())

const stats = computed(() => {
  const graded = enrollments.value.filter(e => e.grade?.score != null)
  return {
    courses: enrollments.value.length,
    graded:  graded.length,
    avg: graded.length
        ? (graded.reduce((s,e)=>s+e.grade.score,0)/graded.length).toFixed(1)
        : '—'
  }
})

const fmtDate = dt => dt ? new Date(dt).toLocaleDateString('vi-VN') : '—'
const sc = s => s>=8.5?'g-exc': s>=7?'g-good': s>=5?'g-avg':'g-poor'

async function save() {
  saving.value = true
  successMsg.value = ''
  errorMsg.value   = ''
  try {
    const payload = { username: form.value.username, email: form.value.email }
    if (form.value.password) payload.password = form.value.password
    await api.put(`/users/${auth.user?.id}`, payload)

    // Cập nhật saved → card trái hiển thị ngay
    saved.username = form.value.username
    saved.email    = form.value.email
    saved.fullName = form.value.fullName
    saved.phone    = form.value.phone
    saved.bio      = form.value.bio

    // Cập nhật auth store
    auth.user.username = form.value.username
    auth.user.email    = form.value.email
    localStorage.setItem('user', JSON.stringify(auth.user))

    form.value.password = ''
    successMsg.value = `Đã cập nhật thành công lúc ${new Date().toLocaleTimeString('vi-VN')}!`
    setTimeout(() => successMsg.value = '', 4000)
  } catch(e) {
    errorMsg.value = e.response?.data?.error || 'Cập nhật thất bại!'
  } finally {
    saving.value = false
  }
}

async function loadHistory() {
  loadingH.value = true
  try { enrollments.value = (await api.get(`/enrollments/user/${auth.user?.id}`)).data }
  catch {}
  finally { loadingH.value = false }
}

onMounted(loadHistory)
</script>

<style scoped>
.page { padding: 2rem 2.5rem; max-width: 1200px; margin: 0 auto; }

.profile-wrap {
  display: grid;
  grid-template-columns: 270px 1fr;
  gap: 1.2rem;
  align-items: start;
}
@media (max-width: 820px) { .profile-wrap { grid-template-columns: 1fr; } }

/* LEFT */
.left-col { display:flex; flex-direction:column; gap:1rem; position:sticky; top:78px; }

.avatar-card {
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:14px; padding:1.8rem 1.4rem;
  text-align:center; box-shadow:var(--shadow-sm);
}
.avatar-wrap {
  width:76px; height:76px; border-radius:50%;
  padding:3px; background:linear-gradient(135deg,var(--accent),var(--purple));
  margin:0 auto 1rem;
}
.avatar {
  width:100%; height:100%; border-radius:50%;
  background:var(--surface2); color:var(--text);
  font-size:1.8rem; font-weight:700;
  display:flex; align-items:center; justify-content:center;
}
.p-name  { font-size:1rem; font-weight:800; margin-bottom:.25rem; }
.p-email { font-size:.77rem; color:var(--muted); margin-bottom:.7rem; }
.role-tag { font-size:.68rem; font-weight:700; padding:.2rem .7rem; border-radius:100px; }
.role-tag.admin   { background:var(--purple-light); color:var(--purple); border:1px solid #ddd6fe; }
.role-tag.student { background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; }

.mini-stats { background:var(--surface); border:1.5px solid var(--border); border-radius:14px; padding:1.1rem 1.3rem; box-shadow:var(--shadow-sm); }
.ms-row { display:flex; align-items:center; gap:.6rem; font-size:.83rem; padding:.3rem 0; }
.ms-icon { font-size:.9rem; }
.ms-lbl { flex:1; color:var(--muted); }
.ms-val { font-weight:700; }
.ms-val.accent { color:var(--green); }

.extra-info { background:var(--surface); border:1.5px solid var(--border); border-radius:14px; padding:1.1rem 1.3rem; box-shadow:var(--shadow-sm); }
.ei-title { font-size:.72rem; font-weight:700; color:var(--muted); text-transform:uppercase; letter-spacing:.06em; margin-bottom:.75rem; }
.ei-row { margin-bottom:.55rem; }
.ei-row.full { margin-bottom:0; }
.ei-lbl { font-size:.71rem; color:var(--muted); display:block; margin-bottom:.12rem; }
.ei-val { font-size:.84rem; font-weight:500; }
.ei-val.bio { font-size:.79rem; color:var(--muted); line-height:1.5; }

/* RIGHT */
.right-col { display:flex; flex-direction:column; gap:1rem; }

.form-card { background:var(--surface); border:1.5px solid var(--border); border-radius:14px; overflow:hidden; box-shadow:var(--shadow-sm); }
.fc-header { padding:1.1rem 1.6rem; border-bottom:1.5px solid var(--border); background:var(--surface2); }
.fc-header h2 { font-size:1rem; font-weight:700; }
.fc-body { padding:1.4rem 1.6rem; }

.form-2col { display:grid; grid-template-columns:1fr 1fr; gap:0 1rem; }
@media (max-width:600px) { .form-2col { grid-template-columns:1fr; } }

.form-group { margin-bottom:.9rem; }
.form-group label { display:block; font-size:.72rem; font-weight:700; color:var(--muted); margin-bottom:.35rem; text-transform:uppercase; letter-spacing:.05em; }
.form-group input,
.form-group textarea {
  width:100%; padding:.58rem .85rem;
  background:var(--surface2); border:1.5px solid var(--border);
  border-radius:8px; color:var(--text); font-size:.87rem; outline:none;
  font-family:'Plus Jakarta Sans',sans-serif; transition:border-color .18s, box-shadow .18s;
}
.form-group input:focus,
.form-group textarea:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }
.form-group input::placeholder,
.form-group textarea::placeholder { color:var(--muted); opacity:.55; }

.pw-divider { display:flex; justify-content:space-between; align-items:center; padding:.75rem 0; border-top:1.5px solid var(--border); margin:.5rem 0 .8rem; }
.pw-divider span  { font-size:.87rem; font-weight:700; }
.pw-divider small { font-size:.74rem; color:var(--muted); }

.save-btn { width:100%; padding:.68rem; background:var(--accent); color:white; border:none; border-radius:9px; font-size:.9rem; font-weight:700; cursor:pointer; transition:all .18s; font-family:'Plus Jakarta Sans',sans-serif; margin-top:.3rem; }
.save-btn:hover:not(:disabled) { background:var(--accent-dark); box-shadow:0 4px 14px rgba(37,99,235,.3); }
.save-btn:disabled { opacity:.5; cursor:not-allowed; }

.alert { padding:.62rem .88rem; border-radius:8px; font-size:.83rem; margin-bottom:.9rem; }
.success-alert { background:var(--green-light);  color:var(--green);  border:1.5px solid #a7f3d0; }
.error-alert   { background:var(--red-light);    color:var(--red);    border:1.5px solid #fca5a5; }

/* History */
.history-card { background:var(--surface); border:1.5px solid var(--border); border-radius:14px; padding:1.4rem 1.6rem; box-shadow:var(--shadow-sm); }
.history-card h2 { font-size:1rem; font-weight:700; margin-bottom:1.1rem; }
.loading-sm { text-align:center; padding:1.5rem; color:var(--muted); font-size:.85rem; }
.empty-sm   { text-align:center; padding:1.5rem; color:var(--muted); font-size:.85rem; }
.h-list { display:flex; flex-direction:column; }
.h-row { display:flex; justify-content:space-between; align-items:center; padding:.8rem 0; border-bottom:1px solid var(--border); gap:1rem; flex-wrap:wrap; }
.h-row:last-child { border-bottom:none; }
.h-name { font-size:.87rem; font-weight:600; margin-bottom:.2rem; }
.h-meta { display:flex; gap:1rem; font-size:.74rem; color:var(--muted); flex-wrap:wrap; }
.h-right { display:flex; align-items:center; gap:.65rem; }
.status-tag { font-size:.67rem; font-weight:700; padding:.18rem .55rem; border-radius:100px; }
.status-tag.active    { background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; }
.status-tag.completed { background:var(--green-light);  color:var(--green);  border:1px solid #a7f3d0; }
.grade-tag { font-size:1rem; font-weight:800; padding:.25rem .55rem; border-radius:7px; min-width:42px; text-align:center; }
.g-exc  { background:var(--green-light);  color:var(--green); }
.g-good { background:var(--accent-light); color:var(--accent); }
.g-avg  { background:var(--yellow-light); color:var(--yellow); }
.g-poor { background:var(--red-light);    color:var(--red); }
.grade-empty { font-size:.82rem; color:var(--muted); }
</style>