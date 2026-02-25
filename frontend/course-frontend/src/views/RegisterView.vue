<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo">🎓</div>
      <h1>Đăng ký</h1>
      <p class="auth-sub">Tạo tài khoản mới</p>

      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>Username</label>
          <input v-model="form.username" type="text" placeholder="username" required minlength="3" />
        </div>
        <div class="form-group">
          <label>Email</label>
          <input v-model="form.email" type="email" placeholder="you@example.com" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="form.password" type="password" placeholder="Ít nhất 8 ký tự, có chữ hoa, số" required />
          <span class="hint">Ví dụ: MyPass@123</span>
        </div>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Đang đăng ký...' : 'Đăng ký' }}
        </button>
      </form>

      <p class="auth-footer">
        Đã có tài khoản?
        <router-link to="/login">Đăng nhập</router-link>
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

const form = ref({ username: '', email: '', password: '' })
const loading = ref(false)
const error = ref('')
const success = ref('')

async function handleRegister() {
  loading.value = true
  error.value = ''
  try {
    await auth.register(form.value.username, form.value.email, form.value.password)
    success.value = 'Đăng ký thành công! Đang chuyển hướng...'
    setTimeout(() => router.push('/courses'), 1000)
  } catch (e) {
    const data = e.response?.data
    if (typeof data === 'object' && data !== null) {
      // Lỗi validation từ @Valid
      error.value = Object.values(data).join(', ')
    } else {
      error.value = data?.error || 'Đăng ký thất bại!'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  padding: 1rem;
}
.auth-card {
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.auth-logo { font-size: 2.5rem; text-align: center; margin-bottom: 1rem; }
h1 { text-align: center; font-size: 1.5rem; font-weight: 700; color: #1e293b; margin-bottom: 0.3rem; }
.auth-sub { text-align: center; color: #94a3b8; font-size: 0.85rem; margin-bottom: 1.5rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; font-size: 0.85rem; font-weight: 600; color: #475569; margin-bottom: 0.4rem; }
.form-group input { width: 100%; padding: 0.65rem 0.9rem; border: 1.5px solid #e2e8f0; border-radius: 8px; font-size: 0.9rem; transition: border-color 0.2s; outline: none; }
.form-group input:focus { border-color: #38bdf8; }
.hint { font-size: 0.75rem; color: #94a3b8; margin-top: 0.25rem; display: block; }
.btn-primary { width: 100%; padding: 0.75rem; background: #2563eb; color: white; border: none; border-radius: 8px; font-size: 0.95rem; font-weight: 600; cursor: pointer; margin-top: 0.5rem; transition: background 0.2s; }
.btn-primary:hover:not(:disabled) { background: #1d4ed8; }
.btn-primary:disabled { background: #93c5fd; cursor: not-allowed; }
.alert-error { background: #fef2f2; color: #dc2626; padding: 0.65rem 0.9rem; border-radius: 8px; font-size: 0.85rem; margin-bottom: 1rem; border: 1px solid #fecaca; }
.alert-success { background: #f0fdf4; color: #16a34a; padding: 0.65rem 0.9rem; border-radius: 8px; font-size: 0.85rem; margin-bottom: 1rem; border: 1px solid #bbf7d0; }
.auth-footer { text-align: center; margin-top: 1.2rem; font-size: 0.85rem; color: #64748b; }
.auth-footer a { color: #2563eb; font-weight: 600; text-decoration: none; }
</style>