<template>
  <div class="pay-wrap">
    <div class="pay-card">

      <!-- Loading -->
      <div v-if="loading" class="pay-state">
        <div class="spinner-lg"></div>
        <p>Đang xác nhận thanh toán...</p>
      </div>

      <!-- Success -->
      <div v-else-if="result?.success" class="pay-state success">
        <div class="pay-icon">✅</div>
        <h2>Thanh toán thành công!</h2>
        <p>Bạn đã đăng ký khóa học thành công.</p>
        <div class="pay-info">
          <div class="pi-row"><span>Mã giao dịch</span><strong>{{ route.query.vnp_TxnRef }}</strong></div>
          <div class="pi-row"><span>Số tiền</span><strong>{{ formatPrice(route.query.vnp_Amount) }}</strong></div>
        </div>
        <div class="pay-btns">
          <router-link :to="`/courses/${result.courseId}`" class="btn btn-accent">
            Vào học ngay →
          </router-link>
          <router-link to="/my-courses" class="btn btn-ghost">Khóa học của tôi</router-link>
        </div>
      </div>

      <!-- Failed / Cancelled -->
      <div v-else class="pay-state failed">
        <div class="pay-icon">❌</div>
        <h2>Thanh toán thất bại</h2>
        <p>{{ errorMsg }}</p>
        <div class="pi-row" v-if="route.query.vnp_ResponseCode">
          <span>Mã lỗi VNPay</span>
          <strong>{{ route.query.vnp_ResponseCode }}</strong>
        </div>
        <div class="pay-btns">
          <button @click="retry" class="btn btn-accent">Thử lại</button>
          <router-link to="/courses" class="btn btn-ghost">Về trang khóa học</router-link>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'

const route  = useRoute()
const router = useRouter()
const loading = ref(true)
const result  = ref(null)
const errorMsg = ref('Giao dịch không thành công hoặc đã bị hủy.')

onMounted(async () => {
  try {
    // Gửi toàn bộ query params từ VNPay về backend xác nhận
    const params = Object.fromEntries(new URLSearchParams(window.location.search))
    const r = await api.get('/payment/vnpay-return', { params })
    result.value = r.data
    if (!r.data.success && r.data.message) {
      errorMsg.value = r.data.message
    }
  } catch (e) {
    result.value = { success: false }
    errorMsg.value = e.response?.data?.error || 'Lỗi xác nhận thanh toán.'
  } finally {
    loading.value = false
  }
})

function formatPrice(amountStr) {
  if (!amountStr) return ''
  // VNPay trả amount * 100
  const amount = parseInt(amountStr) / 100
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

function retry() {
  router.back()
}
</script>

<style scoped>
.pay-wrap  { min-height:80vh; display:flex; align-items:center; justify-content:center; padding:2rem; }
.pay-card  { background:var(--surface); border:1.5px solid var(--border); border-radius:20px; padding:2.5rem 2rem; max-width:460px; width:100%; text-align:center; }
.pay-state { display:flex; flex-direction:column; align-items:center; gap:1rem; }
.pay-icon  { font-size:3.5rem; line-height:1; }
.pay-state h2 { font-size:1.4rem; font-weight:700; margin:0; }
.pay-state p  { color:var(--muted); margin:0; font-size:.93rem; }
.pay-info  { background:var(--surface2); border-radius:10px; padding:.9rem 1.1rem; width:100%; display:flex; flex-direction:column; gap:.5rem; text-align:left; }
.pi-row    { display:flex; justify-content:space-between; font-size:.83rem; }
.pi-row span { color:var(--muted); }
.pay-btns  { display:flex; gap:.75rem; flex-wrap:wrap; justify-content:center; width:100%; }
.spinner-lg { width:44px; height:44px; border:3px solid var(--border); border-top-color:var(--accent); border-radius:50%; animation:spin .7s linear infinite; }
@keyframes spin { to { transform:rotate(360deg); } }
.success .pay-icon { filter:none; }
.failed  .pay-icon { filter:none; }
</style>