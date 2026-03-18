<template>
  <div class="auth-page">
    <div class="bg-canvas">
      <div class="orb orb1"></div>
      <div class="orb orb2"></div>
      <div class="orb orb3"></div>
      <div class="grid-lines"></div>
    </div>

    <div class="left-panel">
      <div class="lp-content">
        <div class="lp-logo">
          <span class="lp-logo-icon">🎓</span>
          <span class="lp-logo-name">EduFlow</span>
        </div>
        <h1 class="lp-title">Nền tảng học tập<br><span class="lp-hl">thông minh</span><br>cho mọi người</h1>
        <p class="lp-sub">Khám phá hàng trăm khóa học chất lượng cao, theo dõi tiến độ và nhận chứng chỉ khi hoàn thành.</p>
        <div class="lp-stats">
          <div class="ls-item"><div class="ls-n">500+</div><div class="ls-l">Khóa học</div></div>
          <div class="ls-div"></div>
          <div class="ls-item"><div class="ls-n">12K+</div><div class="ls-l">Học viên</div></div>
          <div class="ls-div"></div>
          <div class="ls-item"><div class="ls-n">98%</div><div class="ls-l">Hài lòng</div></div>
        </div>
        <div class="lp-feats">
          <div class="lf-row"><div class="lf-dot"></div><span>Học mọi lúc, mọi nơi</span></div>
          <div class="lf-row"><div class="lf-dot"></div><span>Giảng viên chuyên nghiệp</span></div>
          <div class="lf-row"><div class="lf-dot"></div><span>Chứng chỉ được công nhận</span></div>
        </div>
      </div>
    </div>

    <div class="right-panel">
      <div class="form-card">
        <div class="fc-brand">
          <span>🎓</span><span class="fc-brand-name">EduFlow</span>
        </div>
        <h2 class="fc-title">Đăng nhập</h2>
        <p class="fc-sub">Nhập thông tin tài khoản để tiếp tục</p>

        <form @submit.prevent="handleLogin" novalidate class="fc-form">
          <!-- Username -->
          <div class="fg" :class="{fe: errors.username, fs: touched.username && !errors.username && form.username}">
            <label>Tên đăng nhập</label>
            <div class="iw">
              <span class="i-ico">
                <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </span>
              <input v-model="form.username" @blur="touch('username')" @input="onInput('username')"
                     type="text" placeholder="Nhập tên đăng nhập" autocomplete="off"/>
              <span v-if="touched.username && !errors.username && form.username" class="i-ok">
                <svg width="13" height="13" fill="none" stroke="#10b981" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
              </span>
            </div>
            <span v-if="errors.username" class="ferr">{{ errors.username }}</span>
          </div>

          <!-- Password -->
          <div class="fg" :class="{fe: errors.password, fs: touched.password && !errors.password && form.password}">
            <label>Mật khẩu</label>
            <div class="iw">
              <span class="i-ico">
                <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              </span>
              <input v-model="form.password" @blur="touch('password')" @input="onInput('password')"
                     :type="showPwd?'text':'password'" placeholder="Nhập mật khẩu" autocomplete="new-password"/>
              <button type="button" class="eye-btn" @click="showPwd=!showPwd" tabindex="-1">
                <svg v-if="!showPwd" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                <svg v-else width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
              </button>
            </div>
            <span v-if="errors.password" class="ferr">{{ errors.password }}</span>
          </div>

          <div v-if="serverError" class="server-err">
            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {{ serverError }}
          </div>

          <!-- KHÔNG có :disabled="!isValid" — luôn cho submit, validate bên trong hàm -->
          <button type="submit" class="btn-submit" :disabled="loading">
            <div v-if="loading" class="bspin"></div>
            <span v-else>Đăng nhập →</span>
          </button>
        </form>

        <p class="fc-foot">Chưa có tài khoản? <router-link to="/register" class="fc-link">Đăng ký ngay</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import api from '../services/api'

const router     = useRouter()
const route      = useRoute()
const auth       = useAuthStore()
const cartStore  = useCartStore()

const form        = reactive({ username: '', password: '' })
const serverError = ref('')

onMounted(() => {
  form.username = ''
  form.password = ''
  // Hiển thị thông báo nếu bị ban
  if (route.query.banned === '1') {
    serverError.value = '⛔ Tài khoản của bạn đã bị khóa. Vui lòng liên hệ admin!'
  }
})
const errors      = reactive({ username: '', password: '' })
const touched     = reactive({ username: false, password: false })
const showPwd     = ref(false)
const loading     = ref(false)

// Validation đơn giản — chỉ kiểm tra trống
const validators = {
  username: v => !v.trim() ? 'Vui lòng nhập tên đăng nhập' : '',
  password: v => !v        ? 'Vui lòng nhập mật khẩu'      : ''
}

const touch   = f => { touched[f] = true; errors[f] = validators[f](form[f]) }
const onInput = f => { if (touched[f]) errors[f] = validators[f](form[f]); serverError.value = '' }

async function handleLogin() {
  // Validate trước khi gửi
  let ok = true
  for (const f of ['username', 'password']) {
    touched[f] = true
    errors[f]  = validators[f](form[f])
    if (errors[f]) ok = false
  }
  if (!ok) return

  loading.value     = true
  serverError.value = ''
  try {
    const r = await api.post('/auth/login', {
      username: form.username.trim(),
      password: form.password
    })
    auth.login(r.data)
    cartStore.reload()   // Nạp cart của user vừa login (key riêng theo userId)
    router.push(auth.isAdmin ? '/admin' : '/courses')
  } catch (e) {
    serverError.value =
        e.response?.data?.message ||
        e.response?.data?.error   ||
        'Tên đăng nhập hoặc mật khẩu không đúng'
    form.password = ''
    touched.password = false
    errors.password  = ''
  } finally {
    loading.value = false
  }
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

/* BG */
.bg-canvas { position:absolute; inset:0; z-index:0; overflow:hidden; }
.orb { position:absolute; border-radius:50%; filter:blur(110px); animation:float 9s ease-in-out infinite; }
.orb1 { width:650px; height:650px; background:radial-gradient(circle,rgba(99,102,241,.4),transparent 70%); top:-220px; left:-120px; }
.orb2 { width:550px; height:550px; background:radial-gradient(circle,rgba(139,92,246,.32),transparent 70%); bottom:-180px; right:-100px; animation-delay:-3.5s; }
.orb3 { width:380px; height:380px; background:radial-gradient(circle,rgba(6,182,212,.22),transparent 70%); top:40%; left:38%; animation-delay:-6s; }
@keyframes float { 0%,100%{transform:translateY(0) scale(1)} 50%{transform:translateY(-28px) scale(1.04)} }
.grid-lines { position:absolute; inset:0; background-image:linear-gradient(rgba(255,255,255,.022) 1px,transparent 1px),linear-gradient(90deg,rgba(255,255,255,.022) 1px,transparent 1px); background-size:54px 54px; }

/* LEFT */
.left-panel { width:50%; display:flex; align-items:center; justify-content:center; padding:4rem 3rem; position:relative; z-index:1; }
@media(max-width:880px){ .left-panel { display:none; } }
.lp-content { max-width:460px; }
.lp-logo { display:flex; align-items:center; gap:.7rem; margin-bottom:3rem; }
.lp-logo-icon { font-size:2.2rem; }
.lp-logo-name { font-size:1.55rem; font-weight:800; color:#fff; letter-spacing:-.025em; }
.lp-title { font-size:clamp(1.9rem,3vw,2.75rem); font-weight:800; line-height:1.15; color:#fff; margin-bottom:1.2rem; letter-spacing:-.03em; }
.lp-hl { background:linear-gradient(135deg,#818cf8,#c084fc,#38bdf8); -webkit-background-clip:text; -webkit-text-fill-color:transparent; background-clip:text; }
.lp-sub { font-size:.95rem; color:rgba(255,255,255,.48); line-height:1.75; margin-bottom:2.5rem; }
.lp-stats { display:flex; align-items:center; gap:1.8rem; margin-bottom:2.5rem; }
.ls-item { text-align:center; }
.ls-n { font-size:1.75rem; font-weight:800; color:#fff; line-height:1; }
.ls-l { font-size:.7rem; color:rgba(255,255,255,.38); margin-top:.22rem; text-transform:uppercase; letter-spacing:.06em; }
.ls-div { width:1px; height:38px; background:rgba(255,255,255,.1); }
.lp-feats { display:flex; flex-direction:column; gap:.8rem; }
.lf-row { display:flex; align-items:center; gap:.75rem; }
.lf-dot { width:8px; height:8px; border-radius:50%; background:linear-gradient(135deg,#818cf8,#38bdf8); flex-shrink:0; }
.lf-row span { font-size:.87rem; color:rgba(255,255,255,.55); }

/* RIGHT */
.right-panel { width:50%; display:flex; align-items:center; justify-content:center; padding:2rem; position:relative; z-index:1; }
@media(max-width:880px){ .right-panel { width:100%; } }

/* CARD */
.form-card {
  width:100%; max-width:440px;
  background:rgba(255,255,255,.055);
  border:1px solid rgba(255,255,255,.1);
  border-radius:24px; padding:2.8rem 2.6rem;
  backdrop-filter:blur(24px);
  box-shadow:0 32px 80px rgba(0,0,0,.5),inset 0 1px 0 rgba(255,255,255,.08);
}
.fc-brand { display:flex; align-items:center; gap:.5rem; margin-bottom:1.8rem; }
.fc-brand span:first-child { font-size:1.3rem; }
.fc-brand-name { font-size:.92rem; font-weight:700; color:rgba(255,255,255,.38); }
.fc-title { font-size:1.7rem; font-weight:800; color:#fff; letter-spacing:-.025em; margin-bottom:.4rem; }
.fc-sub   { font-size:.875rem; color:rgba(255,255,255,.38); margin-bottom:2rem; }

/* FORM */
.fc-form { display:flex; flex-direction:column; gap:1.05rem; }
.fg label { display:block; font-size:.7rem; font-weight:700; color:rgba(255,255,255,.38); margin-bottom:.4rem; text-transform:uppercase; letter-spacing:.09em; }
.iw { position:relative; display:flex; align-items:center; }
.i-ico { position:absolute; left:.85rem; color:rgba(255,255,255,.28); display:flex; align-items:center; pointer-events:none; flex-shrink:0; }
.i-ok  { position:absolute; right:.85rem; display:flex; align-items:center; pointer-events:none; }
.eye-btn { position:absolute; right:.85rem; background:none; border:none; cursor:pointer; color:rgba(255,255,255,.3); display:flex; align-items:center; transition:color .15s; }
.eye-btn:hover { color:rgba(255,255,255,.7); }
.iw input { width:100%; padding:.8rem .9rem .8rem 2.65rem; background:rgba(255,255,255,.07); border:1.5px solid rgba(255,255,255,.09); border-radius:12px; color:#fff; font-size:.9rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; transition:all .2s; }
.iw input::placeholder { color:rgba(255,255,255,.2); }
.iw input:focus { border-color:rgba(129,140,248,.65); background:rgba(255,255,255,.1); box-shadow:0 0 0 3px rgba(129,140,248,.14); }
.iw input[type="password"] { padding-right:2.65rem; }
.fg.fe .iw input { border-color:rgba(248,113,113,.55); box-shadow:0 0 0 3px rgba(248,113,113,.1); }
.fg.fs .iw input { border-color:rgba(52,211,153,.45); }
.ferr { display:block; font-size:.72rem; color:#fca5a5; margin-top:.3rem; font-weight:500; }

.server-err { display:flex; align-items:center; gap:.6rem; background:rgba(248,113,113,.1); border:1px solid rgba(248,113,113,.22); border-radius:10px; padding:.7rem .95rem; font-size:.82rem; color:#fca5a5; font-weight:500; }

.btn-submit { width:100%; height:50px; background:linear-gradient(135deg,#6366f1,#8b5cf6); color:#fff; border:none; border-radius:13px; font-size:.95rem; font-weight:700; cursor:pointer; transition:all .2s; font-family:'Plus Jakarta Sans',sans-serif; display:flex; align-items:center; justify-content:center; margin-top:.5rem; box-shadow:0 4px 20px rgba(99,102,241,.4); position:relative; overflow:hidden; letter-spacing:.01em; }
.btn-submit::after { content:''; position:absolute; inset:0; background:linear-gradient(135deg,rgba(255,255,255,.12),transparent); opacity:0; transition:opacity .2s; }
.btn-submit:hover:not(:disabled)::after { opacity:1; }
.btn-submit:hover:not(:disabled) { transform:translateY(-2px); box-shadow:0 8px 30px rgba(99,102,241,.55); }
.btn-submit:active:not(:disabled) { transform:translateY(0); }
.btn-submit:disabled { opacity:.45; cursor:not-allowed; transform:none; }

.bspin { width:20px; height:20px; border:2.5px solid rgba(255,255,255,.3); border-top-color:#fff; border-radius:50%; animation:spin .7s linear infinite; }
@keyframes spin { to{transform:rotate(360deg)} }

.fc-foot { text-align:center; font-size:.855rem; color:rgba(255,255,255,.3); margin-top:1.7rem; }
.fc-link { color:#818cf8; font-weight:700; text-decoration:none; margin-left:.3rem; }
.fc-link:hover { color:#a5b4fc; }
</style>