<template>
  <div class="page">
    <div class="page-header">
      <h1>Hồ sơ cá nhân</h1>
      <p>Quản lý thông tin tài khoản của bạn</p>
    </div>

    <div class="profile-shell">
      <!-- Sidebar -->
      <div class="profile-side">
        <div class="avatar-card">
          <!-- Avatar: ảnh thật hoặc initials fallback -->
          <div class="avatar-wrap">
            <img v-if="avatarUrl" :src="avatarUrl" class="big-avatar-img"
                 alt="avatar" @click="showLightbox=true" title="Xem ảnh"/>
            <div v-else class="big-avatar">{{ initials }}</div>
            <!-- Nút upload overlay -->
            <label class="avatar-upload-btn" title="Đổi ảnh đại diện">
              <input type="file" accept="image/jpeg,image/png,image/webp"
                     style="display:none" @change="onAvatarChange"/>
              <svg width="14" height="14" fill="none" stroke="currentColor"
                   stroke-width="2.5" viewBox="0 0 24 24">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
            </label>
            <!-- Uploading spinner -->
            <div v-if="uploadingAvatar" class="avatar-uploading">
              <div class="av-spin"></div>
            </div>
          </div>

          <!-- Lightbox -->
          <Teleport to="body">
            <div v-if="showLightbox" class="lightbox" @click="showLightbox=false">
              <button class="lb-close" @click="showLightbox=false">✕</button>
              <img :src="avatarUrl" class="lb-img" @click.stop alt="avatar full"/>
            </div>
          </Teleport>
          <div v-if="avatarMsg" :class="['av-msg', avatarMsg.type]">
            {{ avatarMsg.text }}
          </div>
          <div class="av-name">{{ auth.user?.fullName || auth.user?.username }}</div>
          <span :class="['role-badge', auth.isAdmin?'role-admin':auth.isInstructor?'role-instructor':'role-student']">
            {{ auth.isAdmin ? '⚙️ Admin' : auth.isInstructor ? '👨‍🏫 Giảng viên' : '🎓 Học viên' }}
          </span>
          <div class="av-email">{{ auth.user?.email||'—' }}</div>
        </div>

        <div class="side-stats">
          <template v-if="loadingStats">
            <div v-for="i in 3" :key="i" class="sk sk-stat"></div>
          </template>
          <!-- Stats cho INSTRUCTOR -->
          <template v-else-if="auth.isInstructor">
            <div class="ss-item">
              <div class="ss-n" style="color:var(--accent)">{{ myStats.totalCourses }}</div>
              <div class="ss-l">Khóa học</div>
            </div>
            <div class="ss-item">
              <div class="ss-n" style="color:var(--green)">{{ myStats.activeCourses }}</div>
              <div class="ss-l">Đang active</div>
            </div>
            <div class="ss-item">
              <div class="ss-n" style="color:var(--purple)">{{ myStats.totalStudents }}</div>
              <div class="ss-l">Học viên</div>
            </div>
          </template>
          <!-- Stats cho STUDENT -->
          <template v-else>
            <div class="ss-item">
              <div class="ss-n">{{ myStats.totalEnrolled }}</div>
              <div class="ss-l">Khóa đã học</div>
            </div>
            <div class="ss-item">
              <div class="ss-n" style="color:var(--green)">{{ myStats.completed }}</div>
              <div class="ss-l">Hoàn thành</div>
            </div>
            <div class="ss-item">
              <div class="ss-n" style="color:var(--accent)">{{ myStats.avgScore||'—' }}</div>
              <div class="ss-l">Điểm TB</div>
            </div>
          </template>
        </div>
      </div>

      <!-- Main panel — CỐ ĐỊNH chiều cao -->
      <div class="profile-main">
        <div class="ptabs">
          <button :class="['ptab', {active:tab==='info'}]"    @click="tab='info'">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            Thông tin
          </button>
          <button :class="['ptab', {active:tab==='password'}]" @click="tab='password'">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            Đổi mật khẩu
          </button>
        </div>

        <!-- TAB CONTENT — scroll bên trong, không dãn -->
        <div class="tab-scroll">

          <!-- Thông tin -->
          <div v-show="tab==='info'" class="tab-pane">

            <!-- Tên đăng nhập: editable; Email: readonly -->
            <div class="readonly-section">
              <div class="rs-label">Thông tin tài khoản</div>
              <div class="f2">
                <div class="form-group" :class="{error:usernameError}">
                  <label>Tên đăng nhập</label>
                  <div class="iw">
                    <input v-model="usernameForm" type="text" placeholder="username" disabled style="opacity:.55;cursor:not-allowed;background:var(--surface2)"/>
                  </div>
                  <span v-if="usernameError" class="err">{{ usernameError }}</span>
                  <span v-else class="field-hint">Dùng để đăng nhập — chỉ chứa chữ, số, dấu _</span>
                </div>
                <div class="form-group">
                  <label>Email <span class="rs-lock">🔒</span></label>
                  <div class="readonly-f">{{ auth.user?.email }}</div>
                </div>
              </div>
              <div style="display:flex;align-items:center;gap:.7rem;margin-top:.5rem">
                <button v-if="false" @click="saveUsername"
                        class="btn btn-sm btn-accent" :disabled="savingUsername">
                  {{ savingUsername ? 'Đang lưu...' : '💾 Lưu tên đăng nhập' }}
                </button>
                <div v-if="usernameMsg" :class="['inline-msg', usernameMsg.type]">
                  {{ usernameMsg.text }}
                </div>
              </div>
            </div>

            <div class="section-divider"></div>

            <!-- Editable: thông tin cá nhân -->
            <form @submit.prevent="saveInfo" novalidate>
              <div class="rs-label" style="margin-bottom:.9rem">Thông tin cá nhân</div>

              <div class="f2">
                <div class="form-group" :class="{error:infoErrors.fullName, ok:isInfoOk('fullName')}">
                  <label>Họ và tên</label>
                  <div class="iw">
                    <input v-model="infoForm.fullName" @blur="touchInfo('fullName')" @input="onInfoInput('fullName')"
                           type="text" placeholder="Nguyễn Văn A"/>
                    <svg v-if="isInfoOk('fullName')" class="i-check" width="13" height="13" fill="none" stroke="#059669" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                  </div>
                  <span v-if="infoErrors.fullName" class="err">{{ infoErrors.fullName }}</span>
                </div>
                <div class="form-group">
                  <label>Số điện thoại</label>
                  <div class="iw">
                    <input v-model="infoForm.phone" type="text" placeholder="0912 345 678"/>
                  </div>
                </div>
              </div>

              <div class="form-group">
                <label>Giới thiệu bản thân</label>
                <textarea v-model="infoForm.bio" rows="3"
                          placeholder="Viết vài dòng giới thiệu về bạn..."></textarea>
              </div>

              <div v-if="infoMsg" :class="['msg-box', infoMsg.type]">
                <svg v-if="infoMsg.type==='success'" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                <svg v-else width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                {{ infoMsg.text }}
              </div>

              <div class="form-actions">
                <button type="submit" class="btn btn-save" :disabled="savingInfo">
                  <svg v-if="!savingInfo" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
                  <div v-else class="btn-spin"></div>
                  {{ savingInfo ? 'Đang lưu...' : 'Lưu thay đổi' }}
                </button>
              </div>
            </form>
          </div>

          <!-- Đổi mật khẩu -->
          <div v-show="tab==='password'" class="tab-pane">
            <form @submit.prevent="savePassword" novalidate>
              <div class="form-group" :class="{error:pwdErrors.current}">
                <label>Mật khẩu hiện tại *</label>
                <div class="iw">
                  <input v-model="pwdForm.current" @blur="touchPwd('current')" @input="onPwdInput('current')"
                         :type="showCurrent?'text':'password'" placeholder="Nhập mật khẩu hiện tại"/>
                  <button type="button" class="eye-btn" @click="showCurrent=!showCurrent" tabindex="-1">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                      <path v-if="!showCurrent" d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle v-if="!showCurrent" cx="12" cy="12" r="3"/>
                      <path v-if="showCurrent" d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line v-if="showCurrent" x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  </button>
                </div>
                <span v-if="pwdErrors.current" class="err">{{ pwdErrors.current }}</span>
              </div>

              <div class="form-group" :class="{error:pwdErrors.newpwd}">
                <label>Mật khẩu mới *</label>
                <div class="iw">
                  <input v-model="pwdForm.newpwd" @blur="touchPwd('newpwd')" @input="onPwdInput('newpwd')"
                         :type="showNew?'text':'password'" placeholder="Ít nhất 8 ký tự"/>
                  <button type="button" class="eye-btn" @click="showNew=!showNew" tabindex="-1">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                      <path v-if="!showNew" d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle v-if="!showNew" cx="12" cy="12" r="3"/>
                      <path v-if="showNew" d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line v-if="showNew" x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  </button>
                </div>
                <div v-if="pwdForm.newpwd" class="strength-row">
                  <div class="str-bars">
                    <div v-for="i in 4" :key="i" :class="['str-bar', {on:strength.score>=i}, `s${strength.score}`]"></div>
                  </div>
                  <span :class="['str-lbl', `s${strength.score}`]">{{ strength.label }}</span>
                </div>
                <span v-if="pwdErrors.newpwd" class="err">{{ pwdErrors.newpwd }}</span>
              </div>

              <div class="form-group" :class="{error:pwdErrors.confirmpwd}">
                <label>Xác nhận mật khẩu mới *</label>
                <div class="iw">
                  <input v-model="pwdForm.confirmpwd" @blur="touchPwd('confirmpwd')" @input="onPwdInput('confirmpwd')"
                         :type="showConfirm?'text':'password'" placeholder="Nhập lại mật khẩu mới"/>
                  <button type="button" class="eye-btn" @click="showConfirm=!showConfirm" tabindex="-1">
                    <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                      <path v-if="!showConfirm" d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle v-if="!showConfirm" cx="12" cy="12" r="3"/>
                      <path v-if="showConfirm" d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line v-if="showConfirm" x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  </button>
                </div>
                <span v-if="pwdErrors.confirmpwd" class="err">{{ pwdErrors.confirmpwd }}</span>
              </div>

              <div v-if="pwdMsg" :class="['msg-box', pwdMsg.type]">
                <svg v-if="pwdMsg.type==='success'" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                <svg v-else width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/></svg>
                {{ pwdMsg.text }}
              </div>

              <div class="form-actions">
                <button type="submit" class="btn btn-save btn-pwd" :disabled="savingPwd || !isPwdValid">
                  <svg v-if="!savingPwd" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  <div v-else class="btn-spin"></div>
                  {{ savingPwd ? 'Đang lưu...' : 'Đổi mật khẩu' }}
                </button>
              </div>
            </form>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const tab  = ref('info')

const initials  = computed(() => (auth.user?.username||'U').slice(0,2).toUpperCase())
const avatarUrl = ref('')
const uploadingAvatar = ref(false)
const avatarMsg = ref(null)
const showLightbox = ref(false)

async function onAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    avatarMsg.value = { type: 'error', text: 'Ảnh tối đa 5MB!' }
    return
  }
  // Preview ngay lập tức
  avatarUrl.value = URL.createObjectURL(file)
  uploadingAvatar.value = true
  avatarMsg.value = null
  try {
    const fd = new FormData()
    fd.append('file', file)
    const r = await api.post(`/upload/avatar?userId=${auth.user?.id}`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    avatarUrl.value = r.data.avatarUrl
    auth.setAvatar(r.data.avatarUrl)   // ← cập nhật navbar ngay
    avatarMsg.value = { type: 'success', text: 'Cập nhật ảnh thành công!' }
    setTimeout(() => avatarMsg.value = null, 3000)
  } catch(err) {
    avatarMsg.value = { type: 'error', text: err.response?.data?.error || 'Upload thất bại!' }
    avatarUrl.value = ''   // revert preview
  } finally {
    uploadingAvatar.value = false
    e.target.value = ''
  }
}

const myStats      = reactive({ totalEnrolled:0, completed:0, avgScore:null, totalCourses:0, activeCourses:0, totalStudents:0 })
const loadingStats = ref(true)

async function loadStats() {
  try {
    if (auth.isInstructor) {
      // INSTRUCTOR: load dashboard stats
      const r = await api.get('/instructor/dashboard')
      myStats.totalCourses   = r.data.totalCourses   ?? 0
      myStats.activeCourses  = r.data.activeCourses  ?? 0
      myStats.totalStudents  = r.data.totalStudents  ?? 0
    } else {
      // STUDENT / ADMIN: load enrollment stats
      const r = await api.get(`/enrollments/user/${auth.user?.id}`)
      const all = r.data
      myStats.totalEnrolled = all.length
      myStats.completed     = all.filter(e=>e.status==='COMPLETED').length
      const graded = all.filter(e=>e.grade?.score!=null)
      myStats.avgScore = graded.length ? (graded.reduce((s,e)=>s+e.grade.score,0)/graded.length).toFixed(1) : null
    }
  } catch {} finally { loadingStats.value=false }
}

// ── Info form ──
const infoForm    = reactive({ fullName:'', phone:'', bio:'' })
const infoErrors  = reactive({ fullName:'' })
const infoTouched = reactive({ fullName:false })
const savingInfo  = ref(false)
const infoMsg     = ref(null)
const loadingProfile = ref(true)
// ── Username form ──
const usernameForm   = ref(auth.user?.username || '')
const usernameError  = ref('')
const savingUsername = ref(false)
const usernameMsg    = ref(null)

async function loadProfile() {
  try {
    const r = await api.get(`/users/${auth.user?.id}/profile`)
    infoForm.fullName = r.data.fullName || ''
    infoForm.phone    = r.data.phone    || ''
    infoForm.bio      = r.data.bio      || ''
    avatarUrl.value   = r.data.avatar   || ''   // ← load avatar
  } catch {} finally { loadingProfile.value = false }
}

const infoValidators = {
  fullName: v => v.trim().length > 0 && v.trim().length < 2 ? 'Ít nhất 2 ký tự' : ''
}
const touchInfo   = f => { infoTouched[f]=true; infoErrors[f]=infoValidators[f]?.(infoForm[f])??'' }
const onInfoInput = f => { if(infoTouched[f]) infoErrors[f]=infoValidators[f]?.(infoForm[f])??''; infoMsg.value=null }
const isInfoOk    = f => infoTouched[f] && !infoErrors[f] && infoForm[f]

async function saveInfo() {
  touchInfo('fullName')
  if(infoErrors.fullName) return
  savingInfo.value=true; infoMsg.value=null
  try {
    await api.put(`/users/${auth.user?.id}/profile`, {
      fullName: infoForm.fullName.trim(),
      phone:    infoForm.phone.trim(),
      bio:      infoForm.bio.trim()
    })
    // Cập nhật fullName trong store để navbar hiển thị ngay
    auth.setFullName(infoForm.fullName.trim())
    infoMsg.value = { type:'success', text:'Cập nhật thông tin thành công!' }
  } catch(e) {
    infoMsg.value = { type:'error', text: e.response?.data?.error||'Cập nhật thất bại!' }
  } finally { savingInfo.value=false }
}

async function saveUsername() {
  const val = usernameForm.value.trim()
  if (!val) { usernameError.value = 'Tên đăng nhập không được để trống!'; return }
  if (!/^[a-zA-Z0-9_]{3,30}$/.test(val)) {
    usernameError.value = 'Chỉ được dùng chữ, số và dấu _ (3–30 ký tự)'
    return
  }
  savingUsername.value = true; usernameMsg.value = null
  try {
    await api.patch(`/users/${auth.user?.id}/username`, { username: val })
    // Cập nhật store
    auth.user.username = val
    localStorage.setItem('user', JSON.stringify(auth.user))
    usernameMsg.value = { type: 'success', text: '✅ Đã đổi tên đăng nhập!' }
    setTimeout(() => usernameMsg.value = null, 3000)
  } catch(e) {
    usernameError.value = e.response?.data?.error || 'Tên đăng nhập đã được sử dụng!'
  } finally { savingUsername.value = false }
}

// ── Password form ──
const pwdForm     = reactive({ current:'', newpwd:'', confirmpwd:'' })
const pwdErrors   = reactive({ current:'', newpwd:'', confirmpwd:'' })
const pwdTouched  = reactive({ current:false, newpwd:false, confirmpwd:false })
const showCurrent = ref(false); const showNew=ref(false); const showConfirm=ref(false)
const savingPwd   = ref(false)
const pwdMsg      = ref(null)

const strength = computed(() => {
  const p=pwdForm.newpwd; if(!p) return{score:0,label:''}
  let s=0; if(p.length>=8)s++; if(/[A-Z]/.test(p))s++; if(/[0-9]/.test(p))s++; if(/[^A-Za-z0-9]/.test(p))s++
  return{score:s,label:['','Yếu','Trung bình','Khá','Mạnh'][s]}
})

const pwdValidators = {
  current:    v => v?'':'Vui lòng nhập mật khẩu hiện tại',
  newpwd:     v => !v?'Vui lòng nhập mật khẩu mới': v.length<8?'Ít nhất 8 ký tự': v===pwdForm.current?'Phải khác mật khẩu cũ':'',
  confirmpwd: v => !v?'Vui lòng xác nhận': v!==pwdForm.newpwd?'Không khớp':''
}
const touchPwd   = f => { pwdTouched[f]=true; pwdErrors[f]=pwdValidators[f](pwdForm[f]) }
const onPwdInput = f => { if(pwdTouched[f]) pwdErrors[f]=pwdValidators[f](pwdForm[f]); pwdMsg.value=null }
const isPwdValid = computed(() => pwdForm.current && pwdForm.newpwd.length>=8 && pwdForm.confirmpwd===pwdForm.newpwd)

async function savePassword() {
  let ok=true
  for(const f of ['current','newpwd','confirmpwd']) { touchPwd(f); if(pwdErrors[f]) ok=false }
  if(!ok) return
  savingPwd.value=true; pwdMsg.value=null
  try {
    await api.put(`/users/${auth.user?.id}/password`, { currentPassword:pwdForm.current, newPassword:pwdForm.newpwd })
    pwdMsg.value={type:'success',text:'Đổi mật khẩu thành công!'}
    pwdForm.current=''; pwdForm.newpwd=''; pwdForm.confirmpwd=''
    Object.keys(pwdTouched).forEach(k=>pwdTouched[k]=false)
  } catch(e) {
    pwdMsg.value={type:'error', text: e.response?.data?.error||'Mật khẩu hiện tại không đúng!'}
  } finally { savingPwd.value=false }
}

onMounted(() => { loadStats(); loadProfile() })
</script>

<style scoped>
.page { padding:2rem 2.5rem; max-width:1000px; margin:0 auto; }
.page-header { margin-bottom:1.3rem; }
.page-header h1 { font-size:1.6rem; font-weight:800; margin-bottom:.3rem; }
.page-header p  { color:var(--muted); font-size:.85rem; }

.profile-shell { display:grid; grid-template-columns:240px 1fr; gap:1.2rem; align-items:start; }
@media(max-width:760px){ .profile-shell { grid-template-columns:1fr; } }

/* Sidebar */
.profile-side { display:flex; flex-direction:column; gap:.9rem; }
.avatar-card  { background:var(--surface); border:1.5px solid var(--border); border-radius:16px; padding:1.8rem 1.4rem; text-align:center; box-shadow:var(--shadow-sm); display:flex; flex-direction:column; align-items:center; gap:.6rem; }
.big-avatar   { width:68px; height:68px; border-radius:50%; background:linear-gradient(135deg,var(--accent),var(--purple)); color:#fff; font-size:1.55rem; font-weight:800; display:flex; align-items:center; justify-content:center; }
/* ── Avatar upload ── */
.avatar-wrap {
  position: relative;
  width: 68px; height: 68px;
}
.big-avatar-img {
  width: 68px; height: 68px;
  border-radius: 50%;
  object-fit: cover;
  border: 2.5px solid var(--border);
}
.big-avatar {
  width: 68px; height: 68px; border-radius: 50%;
  background: linear-gradient(135deg,var(--accent),var(--purple));
  color: #fff; font-size: 1.55rem; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
}
.avatar-upload-btn {
  position: absolute; bottom: 0; right: 0;
  width: 24px; height: 24px; border-radius: 50%;
  background: var(--accent); color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; border: 2px solid var(--surface);
  transition: background .15s;
}
.avatar-upload-btn:hover { background: var(--accent-dark); }
.avatar-uploading {
  position: absolute; inset: 0; border-radius: 50%;
  background: rgba(0,0,0,.45);
  display: flex; align-items: center; justify-content: center;
}
.av-spin {
  width: 22px; height: 22px;
  border: 2.5px solid rgba(255,255,255,.3);
  border-top-color: #fff;
  border-radius: 50%; animation: spin .7s linear infinite;
}
.av-msg {
  font-size: .72rem; font-weight: 600;
  margin-top: .35rem; border-radius: 7px;
  padding: .2rem .6rem;
}
.av-msg.success { color: var(--green); background: var(--green-light); }
.av-msg.error   { color: var(--red);   background: var(--red-light); }

.av-name      { font-size:1rem; font-weight:800; }
.role-badge   { font-size:.72rem; font-weight:700; padding:.22rem .7rem; border-radius:100px; }
.role-admin   { background:var(--purple-light); color:var(--purple); border:1px solid #ddd6fe; }
.role-student    { background:var(--accent-light);  color:var(--accent);  border:1px solid #bfdbfe; }
.role-instructor { background:#d1fae5; color:#059669; border:1px solid #a7f3d0; }
.av-email     { font-size:.76rem; color:var(--muted); word-break:break-all; }

.side-stats { background:var(--surface); border:1.5px solid var(--border); border-radius:14px; padding:1.1rem; display:grid; grid-template-columns:repeat(3,1fr); gap:.5rem; box-shadow:var(--shadow-sm); }
.ss-item { text-align:center; }
.ss-n { font-size:1.4rem; font-weight:800; line-height:1.1; }
.ss-l { font-size:.69rem; color:var(--muted); margin-top:.2rem; }

/* Skeleton */
.sk { background:linear-gradient(90deg,#e2e8f0 25%,#f1f5f9 50%,#e2e8f0 75%); background-size:200% 100%; animation:shimmer 1.4s infinite; border-radius:7px; }
@keyframes shimmer { to{background-position:-200% 0} }
.sk-stat { height:52px; border-radius:10px; }

/* ── MAIN PANEL CỐ ĐỊNH ── */
.profile-main {
  background: var(--surface);
  border: 1.5px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  height: 520px;          /* CỐ ĐỊNH */
  display: flex;
  flex-direction: column;
}

.ptabs { display:flex; border-bottom:1.5px solid var(--border); flex-shrink:0; }
.ptab  { flex:1; display:flex; align-items:center; justify-content:center; gap:.45rem; padding:.85rem; border:none; background:transparent; font-size:.84rem; font-weight:600; color:var(--muted); cursor:pointer; transition:all .15s; font-family:'Plus Jakarta Sans',sans-serif; border-bottom:2.5px solid transparent; }
.ptab:hover  { background:var(--surface2); color:var(--text2); }
.ptab.active { color:var(--accent); border-bottom-color:var(--accent); background:var(--accent-light); }

/* Tab scroll — phần còn lại scroll bên trong */
.tab-scroll { flex:1; overflow-y:auto; min-height:0; }
.tab-pane   { padding:1.8rem; }

/* Form */
.form-group { margin-bottom:1.1rem; }
.form-group label { display:block; font-size:.73rem; font-weight:700; color:var(--muted); margin-bottom:.38rem; text-transform:uppercase; letter-spacing:.06em; }
.iw { position:relative; display:flex; align-items:center; }
.i-check { position:absolute; right:.85rem; pointer-events:none; flex-shrink:0; }
.eye-btn { position:absolute; right:.85rem; background:none; border:none; cursor:pointer; color:var(--muted); display:flex; padding:0; transition:color .15s; }
.eye-btn:hover { color:var(--text); }
.iw input { width:100%; padding:.68rem .9rem; background:var(--surface); border:1.5px solid var(--border); border-radius:9px; color:var(--text); font-size:.87rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; transition:border-color .15s,box-shadow .15s; }
.iw input:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }
.form-group.error .iw input { border-color:var(--red); box-shadow:0 0 0 3px var(--red-light); }
.form-group.ok    .iw input { border-color:var(--green); }
.iw input[type="password"] { padding-right:2.6rem; }

.readonly-f { padding:.68rem .9rem; background:var(--surface2); border:1.5px solid var(--border); border-radius:9px; font-size:.87rem; color:var(--muted); }
.err { display:block; font-size:.73rem; color:var(--red); margin-top:.3rem; font-weight:500; }

.f2 { display:grid; grid-template-columns:1fr 1fr; gap:0 1.2rem; }
@media(max-width:600px) { .f2 { grid-template-columns:1fr; } }

/* Strength */
.strength-row { display:flex; align-items:center; gap:.6rem; margin-top:.45rem; }
.str-bars { display:flex; gap:.3rem; flex:1; }
.str-bar  { height:4px; flex:1; border-radius:100px; background:var(--border); transition:background .3s; }
.str-bar.on.s1 { background:var(--red); }
.str-bar.on.s2 { background:var(--yellow); }
.str-bar.on.s3 { background:#3b82f6; }
.str-bar.on.s4 { background:var(--green); }
.str-lbl { font-size:.72rem; font-weight:700; white-space:nowrap; }
.str-lbl.s1{color:var(--red)} .str-lbl.s2{color:var(--yellow)} .str-lbl.s3{color:#3b82f6} .str-lbl.s4{color:var(--green)}

/* Message box */
.msg-box { display:flex; align-items:center; gap:.5rem; border-radius:9px; padding:.7rem .95rem; font-size:.82rem; font-weight:500; margin-bottom:.8rem; }
.msg-box.success { background:var(--green-light); color:var(--green); border:1px solid #a7f3d0; }
.msg-box.error   { background:var(--red-light);   color:var(--red);   border:1px solid #fca5a5; }

/* ── BUTTONS ── */
.form-actions { display:flex; justify-content:flex-start; margin-top:1.4rem; }

.btn { display:inline-flex; align-items:center; gap:.45rem; padding:.6rem 1.3rem; border-radius:9px; font-size:.85rem; font-weight:700; cursor:pointer; border:none; transition:all .2s; font-family:'Plus Jakarta Sans',sans-serif; white-space:nowrap; }

.btn-save {
  background: var(--accent);
  color: #fff;
  box-shadow: 0 2px 8px rgba(37,99,235,.25);
}
.btn-save:hover:not(:disabled) {
  background: var(--accent-dark);
  box-shadow: 0 4px 14px rgba(37,99,235,.35);
  transform: translateY(-1px);
}
.btn-save:disabled { opacity:.5; cursor:not-allowed; transform:none; box-shadow:none; }

/* Đổi mật khẩu = màu khác cho dễ phân biệt */
.btn-pwd { background:linear-gradient(135deg,#7c3aed,#2563eb); }
.btn-pwd:hover:not(:disabled) { background:linear-gradient(135deg,#6d28d9,#1d4ed8); box-shadow:0 4px 14px rgba(124,58,237,.35); }

.btn-spin { width:16px; height:16px; border:2px solid rgba(255,255,255,.4); border-top-color:#fff; border-radius:50%; animation:spin .7s linear infinite; flex-shrink:0; }
@keyframes spin { to{transform:rotate(360deg)} }

/* ── Readonly section ── */
.readonly-section { margin-bottom:.6rem; }
.rs-label { font-size:.73rem; font-weight:700; color:var(--muted); text-transform:uppercase; letter-spacing:.06em; margin-bottom:.7rem; display:flex; align-items:center; gap:.5rem; }
.rs-lock  { font-size:.7rem; font-weight:500; color:var(--muted); background:var(--surface2); padding:.15rem .55rem; border-radius:100px; border:1px solid var(--border); }
.section-divider { height:1px; background:var(--border); margin:1.2rem 0; }

/* ── Textarea ── */
.tab-pane textarea {
  width:100%; padding:.68rem .9rem;
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:9px; color:var(--text); font-size:.87rem;
  outline:none; font-family:'Plus Jakarta Sans',sans-serif;
  transition:border-color .15s,box-shadow .15s;
  resize:vertical; min-height:80px; box-sizing:border-box;
}
.tab-pane textarea:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }

/* ── Fix height for more content ── */
.profile-main { height: auto; min-height: 520px; }

/* ── Avatar click cursor ── */
.big-avatar-img { cursor: zoom-in; }

/* ── Lightbox ── */
.lightbox {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,.85);
  display: flex; align-items: center; justify-content: center;
  animation: lbFade .15s ease;
}
@keyframes lbFade { from{opacity:0} to{opacity:1} }
.lb-img {
  max-width: 90vw; max-height: 90vh;
  border-radius: 12px;
  box-shadow: 0 24px 80px rgba(0,0,0,.6);
  object-fit: contain;
}
.lb-close {
  position: absolute; top: 20px; right: 24px;
  background: rgba(255,255,255,.15); border: none;
  color: #fff; font-size: 1.2rem; width: 38px; height: 38px;
  border-radius: 50%; cursor: pointer; display: flex;
  align-items: center; justify-content: center;
  transition: background .15s;
}
.lb-close:hover { background: rgba(255,255,255,.3); }

.inline-msg { font-size:.78rem; font-weight:600; }
.inline-msg.success { color:#059669; }
.inline-msg.error   { color:#dc2626; }
.field-hint { font-size:.74rem; color:var(--muted); }

/* ═══════════════════════════════════════════════
   RESPONSIVE ADDITIONS
   ═══════════════════════════════════════════════ */
@media(max-width:760px) {
  .page { padding:1rem .9rem; }
  .page-header h1 { font-size:1.3rem; }

  /* Avatar card */
  .avatar-card { padding:1.2rem 1rem; }

  /* Stats row */
  .stats-row { grid-template-columns:repeat(3,1fr); gap:.5rem; }
  .stat-n { font-size:1.2rem; }

  /* Tab nav */
  .tab-nav { gap:.25rem; }
  .tab-btn { padding:.5rem .85rem; font-size:.8rem; }

  /* Form card */
  .form-card { padding:1.2rem 1rem; }
  .section-divider { margin:1rem 0; }
}

@media(max-width:600px) {
  .f2 { grid-template-columns:1fr; }
  .profile-shell { gap:.8rem; }

  /* Password strength */
  .strength-row { flex-wrap:wrap; }

  /* Readonly section */
  .readonly-section { padding:.9rem 1rem; }
  .rs-label { font-size:.78rem; }

  /* Tabs full width */
  .tab-nav { width:100%; }
  .tab-btn { flex:1; justify-content:center; text-align:center; }

  /* Stats compact */
  .stats-row { gap:.35rem; }
  .stat-card { padding:.65rem .5rem; }
  .stat-n    { font-size:1.1rem; }
  .stat-l    { font-size:.65rem; }
}
</style>