<template>
  <div class="auth-page">
    <div class="particles">
      <span v-for="n in 30" :key="n"></span>
    </div>
    <div class="auth-card" :class="{ success: success }">
        <div class="auth-logo">🎓</div>
        <h1>Đăng nhập</h1>
        <p class="auth-sub">Chào mừng bạn trở lại!</p>

      <transition name="shake">
        <div v-if="error" class="alert-error">
          <span class="error-icon">⚠</span>
          {{ error }}
        </div>
      </transition>

        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>Email</label>
            <input v-model="form.email" type="email" placeholder="you@example.com" required />
          </div>
          <div class="form-group">
            <label>Password</label>
            <input v-model="form.password" type="password" placeholder="••••••••" required />
          </div>
          <button type="submit" class="btn-primary" :disabled="loading">
            <span v-if="!loading">Đăng nhập</span>
            <span v-else class="spinner"></span>
          </button>
        </form>

        <p class="auth-footer">
          Chưa có tài khoản?
          <router-link to="/register">Đăng ký ngay</router-link>
        </p>
      </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const success = ref(false)

const form = ref({ email: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  loading.value = true
  error.value = ''
  try {
    await auth.login(form.value.email, form.value.password)

    success.value = true

    setTimeout(() => {
      router.push('/courses')
    }, 1000)

  } catch (e) {
    error.value =
        e.response?.data?.message ||
        e.response?.data?.error ||
        'Email hoặc password không đúng!'

    setTimeout(() => {
      error.value = ''
    }, 1000)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== PAGE ===== */
.auth-page {
  position: fixed;     /* 🔥 quan trọng */
  inset: 0;            /* phủ toàn màn hình */
  display: flex;
  align-items: center;
  justify-content: center;

  overflow: hidden;

  background:
      radial-gradient(circle at 20% 30%, rgba(99,102,241,0.25), transparent 50%),
      radial-gradient(circle at 80% 70%, rgba(56,189,248,0.25), transparent 50%),
      linear-gradient(135deg, #c7d2fe 0%, #bae6fd 100%);
}

/* ===== PARTICLES ===== */
.particles {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.particles span {
  position: absolute;
  width: 5px;
  height: 5px;
  background: rgba(37, 99, 235, 0.15);
  border-radius: 50%;
  animation: moveParticle 12s linear infinite;
}

.particles span:nth-child(odd) {
  width: 7px;
  height: 7px;
  animation-duration: 18s;
}

@keyframes moveParticle {
  from {
    transform: translateY(100vh) translateX(0);
  }
  to {
    transform: translateY(-10vh) translateX(40px);
  }
}

/* ===== CARD ===== */
.auth-card {
  position: relative;
  z-index: 2;

  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);

  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.6);

  border-radius: 20px;
  padding: 2.5rem;
  width: 100%;
  max-width: 420px;

  box-shadow: 0 15px 40px rgba(30, 41, 59, 0.12);

  animation: fadeSlideUp 0.7s ease forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes fadeSlideUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ===== LOGO ===== */
.auth-logo {
  font-size: 2.5rem;
  text-align: center;
  margin-bottom: 1rem;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
  100% { transform: translateY(0); }
}

/* ===== TEXT ===== */
h1 {
  text-align: center;
  font-size: 1.6rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.3rem;
}

.auth-sub {
  text-align: center;
  color: #64748b;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

/* ===== FORM ===== */
.form-group {
  margin-bottom: 1.1rem;
}

.form-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
  margin-bottom: 0.4rem;
}

.form-group input {
  width: 100%;
  padding: 0.7rem 1rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.9rem;
  transition: all 0.25s ease;
  outline: none;
  background: white;
}

.form-group input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
  transform: translateY(-1px);
}

/* ===== BUTTON ===== */
.btn-primary {
  width: 100%;
  padding: 0.8rem;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 0.5rem;
  transition: all 0.2s ease;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.25);
}

.btn-primary:active:not(:disabled) {
  transform: scale(0.97);
}

.btn-primary:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}

/* ===== SPINNER ===== */
.spinner {
  width: 18px;
  height: 18px;
  border: 3px solid rgba(255,255,255,0.3);
  border-top: 3px solid white;
  border-radius: 50%;
  display: inline-block;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== ERROR ===== */
.alert-error {
  display: flex;
  align-items: center;
  gap: 8px;

  background: #fef2f2;
  color: #dc2626;

  padding: 0.8rem 1rem;
  border-radius: 12px;
  font-size: 0.85rem;
  margin-bottom: 1rem;

  border: 1px solid #fecaca;
  box-shadow: 0 4px 12px rgba(220,38,38,0.1);
}

.error-icon {
  font-size: 1rem;
}

/* Shake animation */
.shake-enter-active {
  animation: shake 0.4s ease;
}

@keyframes shake {
  0% { transform: translateX(0); }
  25% { transform: translateX(-6px); }
  50% { transform: translateX(6px); }
  75% { transform: translateX(-4px); }
  100% { transform: translateX(0); }
}

/* ===== FOOTER ===== */
.auth-footer {
  text-align: center;
  margin-top: 1.3rem;
  font-size: 0.85rem;
  color: #64748b;
}

.auth-footer a {
  color: #2563eb;
  font-weight: 600;
  text-decoration: none;
}

/* ===== SUCCESS ===== */
.auth-card.success {
  animation: successPulse 0.7s ease;
}

@keyframes successPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); box-shadow: 0 0 35px rgba(34,197,94,0.4); }
  100% { transform: scale(1); }
}
</style>