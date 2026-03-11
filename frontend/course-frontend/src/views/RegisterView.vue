<template>
  <div class="auth-page">
    <div class="bg-canvas">
      <div class="orb orb1"></div>
      <div class="orb orb2"></div>
      <div class="orb orb3"></div>
      <div class="grid-lines"></div>
    </div>

    <!-- Left panel -->
    <div class="left-panel">
      <div class="lp-content">
        <div class="lp-logo">
          <span class="lp-logo-icon">🎓</span>
          <span class="lp-logo-name">EduFlow</span>
        </div>
        <h1 class="lp-title">Bắt đầu hành trình<br><span class="lp-hl">học tập</span><br>của bạn hôm nay</h1>
        <p class="lp-sub">Tạo tài khoản miễn phí và khám phá hàng trăm khóa học từ các giảng viên hàng đầu.</p>

        <!-- Testimonial card -->
        <div class="lp-testimonial">
          <div class="lt-stars">★★★★★</div>
          <p class="lt-text">"EduFlow giúp tôi học lập trình từ đầu và tìm được việc làm chỉ sau 6 tháng. Tuyệt vời!"</p>
          <div class="lt-author">
            <div class="lt-av">TM</div>
            <div>
              <div class="lt-name">Trần Minh</div>
              <div class="lt-job">Junior Developer tại FPT</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Right panel: form -->
    <div class="right-panel">
      <div class="form-card">
        <div class="fc-brand">
          <span>🎓</span><span class="fc-brand-name">EduFlow</span>
        </div>
        <h2 class="fc-title">Tạo tài khoản</h2>
        <p class="fc-sub">Điền thông tin để đăng ký ngay, miễn phí</p>

        <form @submit.prevent="handleRegister" novalidate class="fc-form">

          <!-- Username -->
          <div class="fg" :class="{fe: errors.username, fs: isOk('username')}">
            <label>Tên đăng nhập *</label>
            <div class="iw">
              <span class="i-ico">
                <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </span>
              <input v-model="form.username" @blur="touch('username')" @input="onInput('username')"
                     type="text" placeholder="Chỉ dùng chữ, số, dấu _" autocomplete="off"/>
              <span v-if="isOk('username')" class="i-ok">
                <svg width="13" height="13" fill="none" stroke="#10b981" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
              </span>
            </div>
            <span v-if="errors.username" class="ferr">{{ errors.username }}</span>
          </div>

          <!-- Email -->
          <div class="fg" :class="{fe: errors.email, fs: isOk('email')}">
            <label>Email *</label>
            <div class="iw">
              <span class="i-ico">
                <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
              </span>
              <input v-model="form.email" @blur="touch('email')" @input="onInput('email')"
                     type="email" placeholder="email@example.com" autocomplete="off"/>
              <span v-if="isOk('email')" class="i-ok">
                <svg width="13" height="13" fill="none" stroke="#10b981" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
              </span>
            </div>
            <span v-if="errors.email" class="ferr">{{ errors.email }}</span>
          </div>

          <!-- Password -->
          <div class="fg" :class="{fe: errors.password}">
            <label>Mật khẩu *</label>
            <div class="iw">
              <span class="i-ico">
                <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              </span>
              <input v-model="form.password" @blur="touch('password')" @input="onInput('password')"
                     :type="showPwd?'text':'password'" placeholder="Ít nhất 8 ký tự" autocomplete="new-password"/>
              <button type="button" class="eye-btn" @click="showPwd=!showPwd" tabindex="-1">
                <svg v-if="!showPwd" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                <svg v-else width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              </button>
            </div>

            <!-- Strength meter -->
            <div v-if="form.password" class="strength">
              <div class="str-bars">
                <div v-for="i in 4" :key="i" :class="['sb', {on: strength.score>=i}, `s${strength.score}`]"></div>
              </div>
              <span :class="['str-lbl', `s${strength.score}`]">{{ strength.label }}</span>
            </div>
            <!-- Hints -->
            <div v-if="form.password" class="hints">
              <span :class="{ok: form.password.length>=8}">{{ form.password.length>=8?'✓':'·' }} 8+ ký tự</span>
              <span :class="{ok: /[A-Z]/.test(form.password)}">{{ /[A-Z]/.test(form.password)?'✓':'·' }} Chữ hoa</span>
              <span :class="{ok: /[0-9]/.test(form.password)}">{{ /[0-9]/.test(form.password)?'✓':'·' }} Số</span>
              <span :class="{ok: /[^A-Za-z0-9]/.test(form.password)}">{{ /[^A-Za-z0-9]/.test(form.password)?'✓':'·' }} Đặc biệt</span>
            </div>
            <span v-if="errors.password" class="ferr">{{ errors.password }}</span>
          </div>

          <!-- Confirm -->
          <div class="fg" :class="{fe: errors.confirm, fs: isOk('confirm')}">
            <label>Xác nhận mật khẩu *</label>
            <div class="iw">
              <span class="i-ico">
                <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              </span>
              <input v-model="form.confirm" @blur="touch('confirm')" @input="onInput('confirm')"
                     :type="showConfirm?'text':'password'" placeholder="Nhập lại mật khẩu" autocomplete="new-password"/>
              <button type="button" class="eye-btn" @click="showConfirm=!showConfirm" tabindex="-1">
                <svg v-if="!showConfirm" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                <svg v-else width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              </button>
              <span v-if="isOk('confirm')" class="i-ok" style="right:2.8rem">
                <svg width="13" height="13" fill="none" stroke="#10b981" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
              </span>
            </div>
            <span v-if="errors.confirm" class="ferr">{{ errors.confirm }}</span>
          </div>

          <div v-if="serverError" class="server-err">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/></svg>
            {{ serverError }}
          </div>
          <div v-if="success" class="server-ok">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
            Đăng ký thành công! Đang chuyển đến trang đăng nhập...
          </div>

          <button type="submit" class="btn-submit" :disabled="loading">
            <div v-if="loading" class="bspin"></div>
            <span v-else>Tạo tài khoản →</span>
          </button>
        </form>

        <p class="fc-foot">Đã có tài khoản? <router-link to="/login" class="fc-link">Đăng nhập</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'

const router = useRouter()

const form        = reactive({ username:'', email:'', password:'', confirm:'' })

onMounted(() => {
  form.username = ''
  form.email    = ''
  form.password = ''
  form.confirm  = ''
})
const errors      = reactive({ username:'', email:'', password:'', confirm:'' })
const touched     = reactive({ username:false, email:false, password:false, confirm:false })
const showPwd     = ref(false)
const showConfirm = ref(false)
const loading     = ref(false)
const serverError = ref('')
const success     = ref(false)

const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const vs = {
  username: v => !v.trim()?'Vui lòng nhập tên': v.trim().length<3?'Ít nhất 3 ký tự': !/^[a-zA-Z0-9_]+$/.test(v.trim())?'Chỉ dùng chữ, số và _':'',
  email:    v => !v.trim()?'Vui lòng nhập email': !emailRe.test(v.trim())?'Email không hợp lệ':'',
  password: v => !v?'Vui lòng nhập mật khẩu': v.length<8?'Ít nhất 8 ký tự':'',
  confirm:  v => !v?'Vui lòng xác nhận': v!==form.password?'Không khớp':''
}

const touch   = f => { touched[f]=true; errors[f]=vs[f](form[f]) }
const onInput = f => { if(touched[f]) errors[f]=vs[f](form[f]); serverError.value='' }
const isOk    = f => touched[f] && !errors[f] && form[f]

const strength = computed(() => {
  const p=form.password; if(!p) return{score:0,label:''}
  let s=0
  if(p.length>=8) s++
  if(/[A-Z]/.test(p)) s++
  if(/[0-9]/.test(p)) s++
  if(/[^A-Za-z0-9]/.test(p)) s++
  return{score:s,label:['','Yếu','Trung bình','Khá','Mạnh'][s]}
})

async function handleRegister() {
  let ok = true
  for (const f of ['username','email','password','confirm']) {
    touched[f] = true; errors[f] = vs[f](form[f])
    if (errors[f]) ok = false
  }
  if (!ok) return

  loading.value = true; serverError.value = ''
  try {
    await api.post('/auth/register', {
      username: form.username.trim(),
      email:    form.email.trim(),
      password: form.password
    })
    success.value = true
    setTimeout(() => router.push('/login'), 1500)
  } catch(e) {
    serverError.value = e.response?.data?.message || e.response?.data?.error || 'Tên đăng nhập hoặc email đã tồn tại!'
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  background: #0a0f1e;
  z-index: 0;
}

.bg-canvas { position:absolute; inset:0; z-index:0; overflow:hidden; }
.orb { position:absolute; border-radius:50%; filter:blur(110px); animation:float 9s ease-in-out infinite; }
.orb1 { width:650px; height:650px; background:radial-gradient(circle,rgba(16,185,129,.3),transparent 70%); top:-200px; left:-100px; }
.orb2 { width:550px; height:550px; background:radial-gradient(circle,rgba(99,102,241,.32),transparent 70%); bottom:-180px; right:-100px; animation-delay:-3s; }
.orb3 { width:380px; height:380px; background:radial-gradient(circle,rgba(245,158,11,.18),transparent 70%); top:35%; left:40%; animation-delay:-5.5s; }
@keyframes float { 0%,100%{transform:translateY(0) scale(1)} 50%{transform:translateY(-28px) scale(1.04)} }
.grid-lines { position:absolute; inset:0; background-image:linear-gradient(rgba(255,255,255,.02) 1px,transparent 1px),linear-gradient(90deg,rgba(255,255,255,.02) 1px,transparent 1px); background-size:54px 54px; }

.left-panel { width:48%; display:flex; align-items:center; justify-content:center; padding:4rem 3rem; position:relative; z-index:1; }
@media(max-width:900px){ .left-panel { display:none; } }
.lp-content { max-width:440px; }
.lp-logo { display:flex; align-items:center; gap:.7rem; margin-bottom:2.8rem; }
.lp-logo-icon { font-size:2.2rem; }
.lp-logo-name { font-size:1.55rem; font-weight:800; color:#fff; letter-spacing:-.025em; }
.lp-title { font-size:clamp(1.8rem,2.8vw,2.6rem); font-weight:800; line-height:1.15; color:#fff; margin-bottom:1.1rem; letter-spacing:-.03em; }
.lp-hl { background:linear-gradient(135deg,#34d399,#6ee7b7,#38bdf8); -webkit-background-clip:text; -webkit-text-fill-color:transparent; background-clip:text; }
.lp-sub { font-size:.93rem; color:rgba(255,255,255,.46); line-height:1.75; margin-bottom:2.5rem; }

/* Testimonial */
.lp-testimonial { background:rgba(255,255,255,.055); border:1px solid rgba(255,255,255,.09); border-radius:16px; padding:1.5rem; backdrop-filter:blur(10px); }
.lt-stars { color:#fbbf24; font-size:1.1rem; margin-bottom:.7rem; letter-spacing:.08em; }
.lt-text { font-size:.87rem; color:rgba(255,255,255,.65); line-height:1.7; margin-bottom:1rem; font-style:italic; }
.lt-author { display:flex; align-items:center; gap:.75rem; }
.lt-av { width:38px; height:38px; border-radius:50%; background:linear-gradient(135deg,#34d399,#6366f1); color:#fff; font-size:.8rem; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.lt-name { font-size:.84rem; font-weight:700; color:#fff; }
.lt-job  { font-size:.73rem; color:rgba(255,255,255,.38); margin-top:.1rem; }

.right-panel { flex:1; display:flex; align-items:center; justify-content:center; padding:2rem; position:relative; z-index:1; overflow-y:auto; }

.form-card { width:100%; max-width:430px; background:rgba(255,255,255,.055); border:1px solid rgba(255,255,255,.1); border-radius:24px; padding:2.4rem 2.5rem; backdrop-filter:blur(24px); box-shadow:0 32px 80px rgba(0,0,0,.5),inset 0 1px 0 rgba(255,255,255,.08); }
.fc-brand { display:flex; align-items:center; gap:.5rem; margin-bottom:1.5rem; }
.fc-brand span:first-child { font-size:1.25rem; }
.fc-brand-name { font-size:.9rem; font-weight:700; color:rgba(255,255,255,.35); }
.fc-title { font-size:1.6rem; font-weight:800; color:#fff; letter-spacing:-.025em; margin-bottom:.35rem; }
.fc-sub   { font-size:.86rem; color:rgba(255,255,255,.35); margin-bottom:1.7rem; }

.fc-form { display:flex; flex-direction:column; gap:.95rem; }
.fg label { display:block; font-size:.69rem; font-weight:700; color:rgba(255,255,255,.35); margin-bottom:.38rem; text-transform:uppercase; letter-spacing:.09em; }

.iw { position:relative; display:flex; align-items:center; }
.i-ico { position:absolute; left:.85rem; color:rgba(255,255,255,.27); display:flex; align-items:center; pointer-events:none; flex-shrink:0; }
.i-ok  { position:absolute; right:.85rem; display:flex; align-items:center; pointer-events:none; }
.eye-btn { position:absolute; right:.85rem; background:none; border:none; cursor:pointer; color:rgba(255,255,255,.28); display:flex; align-items:center; transition:color .15s; }
.eye-btn:hover { color:rgba(255,255,255,.7); }

.iw input { width:100%; padding:.76rem .9rem .76rem 2.6rem; background:rgba(255,255,255,.07); border:1.5px solid rgba(255,255,255,.08); border-radius:11px; color:#fff; font-size:.88rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; transition:all .2s; }
.iw input::placeholder { color:rgba(255,255,255,.18); }
.iw input:focus { border-color:rgba(52,211,153,.55); background:rgba(255,255,255,.09); box-shadow:0 0 0 3px rgba(52,211,153,.12); }
.iw input[type="password"] { padding-right:2.6rem; }
.fg.fe .iw input { border-color:rgba(248,113,113,.5); box-shadow:0 0 0 3px rgba(248,113,113,.1); }
.fg.fs .iw input { border-color:rgba(52,211,153,.45); }
.ferr { display:block; font-size:.71rem; color:#fca5a5; margin-top:.28rem; font-weight:500; }

/* Strength */
.strength { display:flex; align-items:center; gap:.55rem; margin-top:.45rem; }
.str-bars { display:flex; gap:.28rem; flex:1; }
.sb { height:4px; flex:1; border-radius:100px; background:rgba(255,255,255,.1); transition:background .28s; }
.sb.on.s1{background:#f87171} .sb.on.s2{background:#fbbf24} .sb.on.s3{background:#60a5fa} .sb.on.s4{background:#34d399}
.str-lbl { font-size:.71rem; font-weight:700; white-space:nowrap; }
.str-lbl.s1{color:#f87171} .str-lbl.s2{color:#fbbf24} .str-lbl.s3{color:#60a5fa} .str-lbl.s4{color:#34d399}
.hints { display:flex; flex-wrap:wrap; gap:.15rem .55rem; margin-top:.3rem; }
.hints span { font-size:.69rem; color:rgba(255,255,255,.28); transition:color .2s; }
.hints span.ok { color:#34d399; font-weight:600; }

.server-err { display:flex; align-items:center; gap:.55rem; background:rgba(248,113,113,.1); border:1px solid rgba(248,113,113,.2); border-radius:10px; padding:.68rem .9rem; font-size:.81rem; color:#fca5a5; font-weight:500; }
.server-ok  { display:flex; align-items:center; gap:.55rem; background:rgba(52,211,153,.1); border:1px solid rgba(52,211,153,.2); border-radius:10px; padding:.68rem .9rem; font-size:.81rem; color:#6ee7b7; font-weight:500; }

.btn-submit { width:100%; height:50px; background:linear-gradient(135deg,#10b981,#059669); color:#fff; border:none; border-radius:13px; font-size:.93rem; font-weight:700; cursor:pointer; transition:all .2s; font-family:'Plus Jakarta Sans',sans-serif; display:flex; align-items:center; justify-content:center; margin-top:.4rem; box-shadow:0 4px 20px rgba(16,185,129,.35); position:relative; overflow:hidden; letter-spacing:.01em; }
.btn-submit::after { content:''; position:absolute; inset:0; background:linear-gradient(135deg,rgba(255,255,255,.12),transparent); opacity:0; transition:opacity .2s; }
.btn-submit:hover:not(:disabled)::after { opacity:1; }
.btn-submit:hover:not(:disabled) { transform:translateY(-2px); box-shadow:0 8px 30px rgba(16,185,129,.5); }
.btn-submit:disabled { opacity:.45; cursor:not-allowed; transform:none; }

.bspin { width:20px; height:20px; border:2.5px solid rgba(255,255,255,.3); border-top-color:#fff; border-radius:50%; animation:spin .7s linear infinite; }
@keyframes spin { to{transform:rotate(360deg)} }

.fc-foot { text-align:center; font-size:.845rem; color:rgba(255,255,255,.28); margin-top:1.5rem; }
.fc-link { color:#34d399; font-weight:700; text-decoration:none; margin-left:.3rem; }
.fc-link:hover { color:#6ee7b7; }
</style>