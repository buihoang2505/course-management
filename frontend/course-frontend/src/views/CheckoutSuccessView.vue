<template>
  <div class="success-wrap">
    <div class="success-card">
      <div class="s-icon">🎉</div>
      <h1>Thanh toán thành công!</h1>
      <p>{{ route.query.count }} khóa học đã được thêm vào tài khoản của bạn.</p>
      <div class="s-amount">{{ fmt(route.query.total) }}</div>
      <div class="s-btns">
        <router-link to="/my-courses" class="btn btn-accent">📚 Xem khóa học của tôi</router-link>
        <router-link to="/courses" class="btn btn-ghost">Tiếp tục mua sắm</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
function fmt(amount) {
  if (!amount) return ''
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(amount))
}
</script>

<style scoped>
.success-wrap { min-height:80vh; display:flex; align-items:center; justify-content:center; padding:2rem; }
.success-card { background:var(--surface); border:1.5px solid var(--border); border-radius:20px; padding:3rem 2.5rem; max-width:460px; width:100%; text-align:center; box-shadow:0 8px 30px rgba(0,0,0,.08); }
.s-icon { font-size:4rem; margin-bottom:.75rem; }
h1 { font-size:1.5rem; font-weight:800; margin:0 0 .6rem; }
p  { color:var(--muted); margin:0 0 1rem; }
.s-amount { font-size:1.8rem; font-weight:800; color:var(--accent); margin-bottom:1.5rem; }
.s-btns { display:flex; flex-direction:column; gap:.65rem; }
.btn { display:flex; align-items:center; justify-content:center; gap:.4rem; padding:.7rem 1.5rem; border-radius:11px; font-size:.9rem; font-weight:700; cursor:pointer; border:none; font-family:inherit; text-decoration:none; transition:all .15s; }
.btn-accent { background:var(--accent); color:#fff; }
.btn-accent:hover { opacity:.88; }
.btn-ghost  { background:var(--surface2); color:var(--text); border:1.5px solid var(--border); }
.btn-ghost:hover { border-color:var(--accent); color:var(--accent); }
</style>