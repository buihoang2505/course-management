<template>
  <div class="page">
    <div class="wrap">

      <!-- HERO -->
      <div class="hero">
        <div class="hero-icon">🎓</div>
        <h1>Trở thành Giảng viên EduFlow</h1>
        <p>Chia sẻ kiến thức, tạo khóa học và truyền cảm hứng cho hàng nghìn học viên</p>
      </div>

      <!-- ĐƠN ĐÃ NỘP → hiển thị trạng thái -->
      <div v-if="myApp" class="status-card" :class="`s-${myApp.status.toLowerCase()}`">
        <div class="s-icon">
          {{ myApp.status==='PENDING' ? '⏳' : myApp.status==='APPROVED' ? '🎉' : '❌' }}
        </div>
        <div class="s-body">
          <div class="s-title">
            {{ myApp.status==='PENDING'
              ? 'Đơn đang chờ Admin xét duyệt'
              : myApp.status==='APPROVED'
                  ? 'Đơn của bạn đã được duyệt!'
                  : 'Đơn chưa được duyệt lần này' }}
          </div>
          <div class="s-meta">Nộp ngày {{ fmtDate(myApp.createdAt) }}</div>
          <div v-if="myApp.adminNote" class="s-note">💬 {{ myApp.adminNote }}</div>
          <div v-if="myApp.status==='APPROVED'" class="s-actions">
            <router-link to="/instructor" class="btn btn-accent">→ Vào trang Giảng viên</router-link>
          </div>
          <div v-if="myApp.status==='REJECTED'" class="s-hint">
            Bạn có thể nộp lại sau 30 ngày kể từ khi bị từ chối.
          </div>
        </div>
      </div>

      <!-- FORM NỘP ĐƠN -->
      <template v-else>

        <!-- Checklist điều kiện -->
        <div class="req-card">
          <div class="req-title">📋 Điều kiện cần có trước khi nộp đơn</div>
          <div class="req-list">
            <div :class="['req-row', req.fullName ? 'ok' : 'ng']">
              <span>{{ req.fullName ? '✅' : '❌' }}</span>
              <div>
                <div class="req-label">Họ và tên đầy đủ</div>
                <div class="req-sub">{{ req.fullName ? 'Đã cập nhật' : 'Chưa có — vào Hồ sơ để thêm' }}</div>
              </div>
            </div>
            <div :class="['req-row', req.bio ? 'ok' : 'ng']">
              <span>{{ req.bio ? '✅' : '❌' }}</span>
              <div>
                <div class="req-label">Giới thiệu bản thân (Bio)</div>
                <div class="req-sub">{{ req.bio ? 'Đã điền' : 'Chưa có — vào Hồ sơ để thêm' }}</div>
              </div>
            </div>
          </div>
          <router-link v-if="!canApply" to="/profile" class="req-link">→ Cập nhật hồ sơ ngay</router-link>
        </div>

        <!-- Form -->
        <div class="form-card">
          <div class="fc-title">✍️ Thông tin đơn đăng ký</div>

          <div class="fg" :class="{err: e.experience}">
            <label>Kinh nghiệm của bạn <span class="req-star">*</span></label>
            <textarea v-model="form.experience" rows="5"
                      placeholder="VD: 5 năm làm Java Backend Developer tại công ty X. 2 năm hướng dẫn intern về Spring Boot, REST API, Docker..."
                      @input="e.experience=''"></textarea>
            <span v-if="e.experience" class="err-txt">{{ e.experience }}</span>
          </div>

          <div class="fg">
            <label>Lĩnh vực chuyên môn</label>
            <input v-model="form.expertise" type="text"
                   placeholder="VD: Java, Spring Boot, Microservices, Docker"/>
            <span class="field-hint">Liệt kê các kỹ năng, phân cách bằng dấu phẩy</span>
          </div>

          <div class="fg">
            <label>Portfolio / LinkedIn / GitHub <span class="opt">(không bắt buộc)</span></label>
            <input v-model="form.portfolioUrl" type="text"
                   placeholder="github.com/username  hoặc  linkedin.com/in/..."/>
          </div>

          <div v-if="submitErr" class="msg-err">⚠️ {{ submitErr }}</div>

          <div class="fc-footer">
            <button class="btn btn-ghost" @click="$router.back()">← Quay lại</button>
            <button class="btn btn-accent" @click="submit"
                    :disabled="submitting || !canApply">
              {{ submitting ? 'Đang nộp...' : '📨 Nộp đơn' }}
            </button>
          </div>
          <p v-if="!canApply" class="cant-note">⚠️ Vui lòng hoàn thiện hồ sơ trước khi nộp đơn</p>
        </div>

      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth   = useAuthStore()
const router = useRouter()

const myApp      = ref(null)
const req        = ref({ fullName: false, bio: false })
const canApply   = computed(() => req.value.fullName && req.value.bio)

const form = ref({ experience: '', expertise: '', portfolioUrl: '' })
const e    = ref({ experience: '' })
const submitErr  = ref('')
const submitting = ref(false)

const fmtDate = s => new Date(s).toLocaleDateString('vi-VN')

async function load() {
  try {
    // Kiểm tra đơn hiện tại
    const { data } = await api.get(`/applications/instructor/my?userId=${auth.user.id}`)
    if (data?.status) { myApp.value = data; return }

    // Kiểm tra profile
    const { data: p } = await api.get(`/users/${auth.user.id}/profile`)
    req.value.fullName = !!(p?.fullName?.trim())
    req.value.bio      = !!(p?.bio?.trim())
  } catch {}
}

async function submit() {
  e.value.experience = ''; submitErr.value = ''
  if (!form.value.experience.trim()) {
    e.value.experience = 'Vui lòng mô tả kinh nghiệm của bạn!'
    return
  }
  submitting.value = true
  try {
    await api.post('/applications/instructor/apply', {
      userId:       String(auth.user.id),
      experience:   form.value.experience.trim(),
      expertise:    form.value.expertise.trim(),
      portfolioUrl: form.value.portfolioUrl.trim()
    })
    await load()          // reload → sẽ show status PENDING
  } catch (err) {
    submitErr.value = err.response?.data?.error || 'Nộp đơn thất bại! Thử lại sau.'
  } finally { submitting.value = false }
}

onMounted(load)
</script>

<style scoped>
.page { min-height:100vh; background:var(--bg); padding:2rem 1rem; }
.wrap { max-width:700px; margin:0 auto; display:flex; flex-direction:column; gap:1.4rem; }

/* Hero */
.hero { text-align:center; padding:1.5rem 1rem 0.5rem; }
.hero-icon { font-size:3rem; line-height:1; margin-bottom:.6rem; }
.hero h1   { font-size:1.65rem; font-weight:800; color:var(--text); margin:0 0 .4rem; }
.hero p    { color:var(--muted); font-size:.92rem; }

/* Status card */
.status-card {
  display:flex; gap:1.2rem; align-items:flex-start;
  border-radius:16px; padding:1.4rem;
  border:2px solid var(--border); background:var(--surface);
}
.s-pending  { border-color:#fde68a; background:#fefce8; }
.s-approved { border-color:#a7f3d0; background:#ecfdf5; }
.s-rejected { border-color:#fca5a5; background:#fef2f2; }
.s-icon  { font-size:2.2rem; flex-shrink:0; }
.s-title { font-weight:700; font-size:1rem; color:var(--text); margin-bottom:.25rem; }
.s-meta  { font-size:.78rem; color:var(--muted); }
.s-note  { margin-top:.6rem; font-size:.82rem; background:rgba(0,0,0,.05);
  border-radius:8px; padding:.35rem .65rem; color:var(--text2); }
.s-hint  { margin-top:.6rem; font-size:.78rem; color:var(--muted); font-style:italic; }
.s-actions { margin-top:.8rem; }

/* Requirements */
.req-card {
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:14px; padding:1.2rem;
}
.req-title { font-weight:700; font-size:.88rem; margin-bottom:.85rem; }
.req-list  { display:flex; flex-direction:column; gap:.55rem; }
.req-row   {
  display:flex; align-items:flex-start; gap:.7rem;
  padding:.55rem .75rem; border-radius:10px;
}
.req-row.ok { background:#ecfdf5; }
.req-row.ng { background:#fef2f2; }
.req-label  { font-weight:600; font-size:.83rem; color:var(--text); }
.req-sub    { font-size:.75rem; color:var(--muted); }
.req-link   { display:inline-block; margin-top:.8rem; font-size:.82rem;
  font-weight:600; color:var(--accent); text-decoration:none; }
.req-link:hover { text-decoration:underline; }

/* Form card */
.form-card {
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:16px; padding:1.5rem;
}
.fc-title { font-weight:700; font-size:.95rem; margin-bottom:1.1rem; }

.fg { display:flex; flex-direction:column; gap:.35rem; margin-bottom:.95rem; }
.fg label { font-size:.82rem; font-weight:600; color:var(--text2); }
.fg input, .fg textarea {
  padding:.6rem .85rem;
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:9px; color:var(--text); font-size:.87rem;
  font-family:inherit; outline:none; resize:vertical;
  transition:border-color .15s, box-shadow .15s;
}
.fg input:focus, .fg textarea:focus {
  border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light);
}
.fg.err input, .fg.err textarea { border-color:var(--red); }
.err-txt    { font-size:.77rem; color:var(--red); font-weight:600; }
.field-hint { font-size:.74rem; color:var(--muted); }
.req-star   { color:var(--red); }
.opt        { font-weight:400; color:var(--muted); font-size:.76rem; }

.msg-err {
  font-size:.82rem; color:var(--red); font-weight:600;
  background:#fef2f2; border:1px solid #fca5a5;
  border-radius:9px; padding:.5rem .85rem; margin-bottom:.7rem;
}
.fc-footer {
  display:flex; justify-content:flex-end; gap:.7rem;
  margin-top:1.2rem; padding-top:1rem; border-top:1.5px solid var(--border);
}
.cant-note { font-size:.76rem; color:var(--muted); text-align:right; margin-top:.4rem; }

/* Buttons */
.btn {
  display:inline-flex; align-items:center; gap:.4rem;
  padding:.58rem 1.15rem; border-radius:10px; font-weight:700;
  font-size:.84rem; cursor:pointer; border:1.5px solid transparent;
  font-family:inherit; transition:all .15s; text-decoration:none;
}
.btn-accent { background:var(--accent); color:#fff; border-color:var(--accent); }
.btn-accent:hover:not(:disabled) { opacity:.88; }
.btn-accent:disabled { opacity:.45; cursor:not-allowed; }
.btn-ghost  { background:var(--surface2,#f1f5f9); color:var(--text2); border-color:var(--border); }
.btn-ghost:hover { border-color:var(--accent); color:var(--accent); }
</style>