<template>
  <div class="pc-page">
    <!-- Background decoration -->
    <div class="bg-mesh"></div>

    <div class="pc-container">

      <!-- ── PROCESSING ── -->
      <div v-if="confirming" class="state-card">
        <div class="state-orb processing">
          <div class="orb-ring"></div>
          <div class="orb-ring ring2"></div>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
            <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
          </svg>
        </div>
        <h2 class="state-title">Đang xử lý thanh toán</h2>
        <p class="state-sub">Vui lòng không đóng trang này...</p>
        <div class="progress-bar"><div class="progress-fill"></div></div>
      </div>

      <!-- ── SUCCESS ── -->
      <div v-else-if="done" class="state-card success-card">
        <div class="confetti-wrap">
          <span v-for="i in 12" :key="i" class="confetti-dot" :style="confettiStyle(i)"></span>
        </div>
        <div class="state-orb success">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <h2 class="state-title">Thanh toán thành công!</h2>
        <p class="state-sub">Khóa học đã được thêm vào tài khoản của bạn</p>

        <div class="receipt-box">
          <div class="receipt-row">
            <span class="receipt-label">Khóa học</span>
            <strong class="receipt-val">{{ route.query.courseTitle }}</strong>
          </div>
          <div class="receipt-divider"></div>
          <div class="receipt-row">
            <span class="receipt-label">Số tiền</span>
            <strong class="receipt-price">{{ fmt(route.query.amount) }}</strong>
          </div>
          <div class="receipt-row">
            <span class="receipt-label">Mã giao dịch</span>
            <code class="receipt-code">{{ String(route.query.txnRef || '').slice(0,20) }}</code>
          </div>
        </div>

        <div class="success-actions">
          <router-link :to="`/courses/${courseId}`" class="btn-primary">
            Vào học ngay
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </router-link>
          <router-link to="/my-courses" class="btn-secondary">Khóa học của tôi</router-link>
        </div>
      </div>

      <!-- ── CONFIRM FORM ── -->
      <div v-else class="confirm-layout">

        <!-- Left: Order summary -->
        <div class="order-panel">
          <div class="order-header">
            <div class="order-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2"/><path d="M1 10h22"/></svg>
            </div>
            <div>
              <h2 class="order-title">Xác nhận thanh toán</h2>
              <p class="order-sub">Kiểm tra thông tin trước khi thanh toán</p>
            </div>
          </div>

          <div class="course-preview">
            <div class="course-preview-icon">📚</div>
            <div class="course-preview-info">
              <div class="cp-label">Khóa học</div>
              <div class="cp-name">{{ route.query.courseTitle }}</div>
            </div>
          </div>

          <div class="amount-display">
            <div class="amount-label">Tổng thanh toán</div>
            <div class="amount-value">{{ fmt(route.query.amount) }}</div>
          </div>

          <div class="security-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            Giao dịch được mã hóa SSL 256-bit
          </div>
        </div>

        <!-- Right: Payment method -->
        <div class="method-panel">
          <div class="method-panel-title">Phương thức thanh toán</div>

          <div class="method-list">
            <label
                v-for="m in methods" :key="m.id"
                :class="['method-item', { active: selectedMethod === m.id }]"
            >
              <input type="radio" v-model="selectedMethod" :value="m.id" hidden/>
              <div class="method-icon-wrap">
                <img v-if="m.img" :src="m.img" :alt="m.name" class="method-img" onerror="this.style.display='none'"/>
                <span v-else class="method-emoji">{{ m.icon }}</span>
              </div>
              <div class="method-info">
                <span class="method-name">{{ m.name }}</span>
                <span class="method-desc">{{ m.desc }}</span>
              </div>
              <div class="method-radio">
                <div v-if="selectedMethod === m.id" class="radio-dot"></div>
              </div>
            </label>
          </div>

          <div v-if="error" class="err-box">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {{ error }}
          </div>

          <div class="panel-actions">
            <button class="btn-back" @click="router.back()">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 5l-7 7 7 7"/></svg>
              Quay lại
            </button>
            <button class="btn-pay" @click="confirm" :disabled="!selectedMethod">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              Thanh toán {{ fmt(route.query.amount) }}
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'
import { useCartStore } from '../stores/cart'

const route     = useRoute()
const cartStore = useCartStore()
const router    = useRouter()

const confirming     = ref(false)
const done           = ref(false)
const error          = ref('')
const selectedMethod = ref('bank')
const courseId = ref(route.query.courseId || (route.query.courseIds ? JSON.parse(route.query.courseIds)[0] : null))

const methods = [
  { id: 'bank',   icon: '🏦', name: 'Thẻ ngân hàng nội địa', desc: 'ATM / Internet Banking' },
  { id: 'visa',   icon: '💳', name: 'Thẻ quốc tế',           desc: 'Visa / Mastercard / JCB' },
  { id: 'wallet', icon: '📱', name: 'Ví điện tử',            desc: 'MoMo / ZaloPay / VNPay QR' },
]

function fmt(amount) {
  if (!amount) return ''
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(amount))
}

function confettiStyle(i) {
  const colors = ['#f43f5e','#f59e0b','#10b981','#3b82f6','#8b5cf6','#ec4899']
  const angle = (i / 12) * 360
  const dist = 60 + Math.random() * 40
  return {
    background: colors[i % colors.length],
    transform: `rotate(${angle}deg) translateX(${dist}px)`,
    animationDelay: `${i * 0.05}s`,
  }
}

async function confirm() {
  const singleTxn = route.query.txnRef
  const multiTxns = route.query.txnRefs ? JSON.parse(route.query.txnRefs) : null
  const txnList   = multiTxns || (singleTxn ? [singleTxn] : null)

  if (!txnList?.length) { error.value = 'Thiếu thông tin giao dịch!'; return }

  confirming.value = true
  error.value = ''
  try {
    const confirmedCourseIds = []
    for (const txn of txnList) {
      const r = await api.post(`/payment/confirm?txnRef=${txn}`)
      if (r.data.success && r.data.courseId) confirmedCourseIds.push(r.data.courseId)
    }
    if (!confirmedCourseIds.length) { error.value = 'Thanh toán thất bại!'; return }
    confirmedCourseIds.forEach(id => cartStore.remove(Number(id)))
    if (cartStore.count === 0 || cartStore.items.every(c => confirmedCourseIds.includes(Number(c.id)))) {
      cartStore.clear()
    }
    courseId.value = confirmedCourseIds[0]
    done.value = true
  } catch (e) {
    error.value = e.response?.data?.error || 'Lỗi thanh toán. Vui lòng thử lại!'
  } finally {
    confirming.value = false
  }
}
</script>

<style scoped>
/* ── Page ── */
.pc-page {
  min-height: 100vh;
  background: #f8faff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  position: relative;
  overflow: hidden;
}

.bg-mesh {
  position: fixed;
  inset: 0;
  background:
      radial-gradient(ellipse 80% 60% at 20% 10%, rgba(99,102,241,.08) 0%, transparent 60%),
      radial-gradient(ellipse 60% 50% at 80% 80%, rgba(244,63,94,.06) 0%, transparent 60%);
  pointer-events: none;
}

.pc-container {
  position: relative;
  width: 100%;
  max-width: 860px;
}

/* ── State cards (processing / success) ── */
.state-card {
  background: #fff;
  border-radius: 24px;
  padding: 3.5rem 2.5rem;
  text-align: center;
  box-shadow: 0 4px 40px rgba(0,0,0,.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  max-width: 480px;
  margin: 0 auto;
}

/* Processing orb */
.state-orb {
  width: 80px; height: 80px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  position: relative;
  margin-bottom: .5rem;
}
.state-orb.processing {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  animation: spin 2s linear infinite;
}
.state-orb.success {
  background: linear-gradient(135deg, #10b981, #059669);
  animation: successPop .5s cubic-bezier(.34,1.56,.64,1);
}
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes successPop { from { transform: scale(0); opacity:0; } to { transform:scale(1); opacity:1; } }

.orb-ring {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  border: 2px solid rgba(99,102,241,.25);
  animation: ringExpand 2s ease-out infinite;
}
.ring2 { inset: -16px; animation-delay: .7s; }
@keyframes ringExpand {
  0%   { opacity: .6; transform: scale(.9); }
  100% { opacity: 0;  transform: scale(1.3); }
}

.state-title { font-size: 1.4rem; font-weight: 800; color: #0f172a; margin: 0; }
.state-sub   { font-size: .9rem; color: #64748b; margin: 0; }

/* Progress bar */
.progress-bar {
  width: 100%;
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
  margin-top: .5rem;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 2px;
  animation: progressAnim 2s ease-in-out infinite;
}
@keyframes progressAnim {
  0%   { width: 0%; margin-left: 0; }
  50%  { width: 70%; }
  100% { width: 0%; margin-left: 100%; }
}

/* Success card extras */
.success-card { position: relative; overflow: hidden; }
.confetti-wrap {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.confetti-dot {
  position: absolute;
  width: 8px; height: 8px;
  border-radius: 2px;
  animation: confettiFall .8s ease-out both;
}
@keyframes confettiFall {
  0%   { opacity: 1; transform: rotate(0deg) translateX(0); }
  100% { opacity: 0; }
}

.receipt-box {
  width: 100%;
  background: #f8faff;
  border: 1.5px solid #e2e8f0;
  border-radius: 16px;
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: .75rem;
}
.receipt-row   { display: flex; justify-content: space-between; align-items: center; font-size: .88rem; }
.receipt-label { color: #64748b; }
.receipt-val   { color: #0f172a; font-weight: 700; }
.receipt-price { color: #6366f1; font-size: 1.1rem; font-weight: 800; }
.receipt-code  {
  font-size: .72rem;
  background: #fff;
  border: 1px solid #e2e8f0;
  padding: .15rem .5rem;
  border-radius: 6px;
  color: #475569;
  font-family: monospace;
}
.receipt-divider { border: none; border-top: 1px dashed #e2e8f0; margin: .25rem 0; }

.success-actions {
  display: flex;
  flex-direction: column;
  gap: .6rem;
  width: 100%;
}
.btn-primary {
  display: flex; align-items: center; justify-content: center; gap: .5rem;
  padding: .8rem 1.5rem;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border-radius: 12px;
  font-weight: 700; font-size: .92rem;
  text-decoration: none;
  transition: opacity .18s, transform .18s;
  box-shadow: 0 4px 20px rgba(99,102,241,.35);
}
.btn-primary:hover { opacity: .88; transform: translateY(-1px); }
.btn-secondary {
  display: flex; align-items: center; justify-content: center;
  padding: .7rem 1.5rem;
  background: #fff;
  border: 1.5px solid #e2e8f0;
  color: #475569;
  border-radius: 12px;
  font-weight: 600; font-size: .88rem;
  text-decoration: none;
  transition: all .18s;
}
.btn-secondary:hover { border-color: #6366f1; color: #6366f1; }

/* ── Confirm layout (2 columns) ── */
.confirm-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  align-items: start;
}
@media (max-width: 680px) {
  .confirm-layout { grid-template-columns: 1fr; }
}

/* Order panel */
.order-panel {
  background: linear-gradient(145deg, #0f172a, #1e1b4b);
  border-radius: 24px;
  padding: 2rem;
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  position: relative;
  overflow: hidden;
}
.order-panel::before {
  content: '';
  position: absolute;
  width: 200px; height: 200px;
  border-radius: 50%;
  background: rgba(99,102,241,.2);
  filter: blur(40px);
  top: -60px; right: -60px;
}
.order-header { display: flex; align-items: center; gap: 1rem; position: relative; }
.order-icon {
  width: 48px; height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 20px rgba(99,102,241,.4);
}
.order-title { font-size: 1.15rem; font-weight: 800; margin-bottom: .2rem; }
.order-sub   { font-size: .8rem; color: rgba(255,255,255,.55); }

.course-preview {
  display: flex; align-items: center; gap: .85rem;
  background: rgba(255,255,255,.08);
  border: 1px solid rgba(255,255,255,.12);
  border-radius: 14px;
  padding: 1rem 1.1rem;
  position: relative;
}
.course-preview-icon { font-size: 1.75rem; flex-shrink: 0; }
.cp-label { font-size: .72rem; color: rgba(255,255,255,.5); margin-bottom: .2rem; }
.cp-name  { font-size: .95rem; font-weight: 700; line-height: 1.35; }

.amount-display {
  background: rgba(99,102,241,.2);
  border: 1px solid rgba(99,102,241,.3);
  border-radius: 14px;
  padding: 1.2rem 1.25rem;
}
.amount-label { font-size: .75rem; color: rgba(255,255,255,.55); margin-bottom: .4rem; }
.amount-value { font-size: 2rem; font-weight: 900; letter-spacing: -0.02em; color: #a5b4fc; }

.security-badge {
  display: flex; align-items: center; gap: .5rem;
  font-size: .74rem;
  color: rgba(255,255,255,.4);
}

/* Method panel */
.method-panel {
  background: #fff;
  border-radius: 24px;
  padding: 2rem;
  box-shadow: 0 4px 40px rgba(0,0,0,.07);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}
.method-panel-title {
  font-size: .75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .06em;
  color: #94a3b8;
}

.method-list { display: flex; flex-direction: column; gap: .65rem; }

.method-item {
  display: flex;
  align-items: center;
  gap: .85rem;
  padding: .9rem 1rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 14px;
  cursor: pointer;
  transition: all .18s;
}
.method-item:hover { border-color: #6366f1; background: #f5f3ff; }
.method-item.active {
  border-color: #6366f1;
  background: #f5f3ff;
  box-shadow: 0 0 0 3px rgba(99,102,241,.1);
}

.method-icon-wrap {
  width: 40px; height: 40px;
  border-radius: 10px;
  background: #f8faff;
  border: 1px solid #e2e8f0;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  font-size: 1.2rem;
}
.method-info { flex: 1; }
.method-name { display: block; font-size: .88rem; font-weight: 700; color: #0f172a; }
.method-desc { display: block; font-size: .74rem; color: #94a3b8; margin-top: .1rem; }

.method-radio {
  width: 20px; height: 20px;
  border-radius: 50%;
  border: 2px solid #cbd5e1;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  transition: border-color .18s;
}
.method-item.active .method-radio { border-color: #6366f1; }
.radio-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  background: #6366f1;
  animation: dotPop .2s cubic-bezier(.34,1.56,.64,1);
}
@keyframes dotPop { from { transform: scale(0); } to { transform: scale(1); } }

.err-box {
  display: flex; align-items: center; gap: .5rem;
  background: #fef2f2;
  border: 1.5px solid #fca5a5;
  border-radius: 10px;
  padding: .65rem .9rem;
  font-size: .83rem;
  color: #dc2626;
}

.panel-actions {
  display: flex;
  gap: .75rem;
  margin-top: .25rem;
}
.btn-back {
  display: flex; align-items: center; gap: .4rem;
  padding: .75rem 1.1rem;
  background: #f8faff;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  font-size: .85rem; font-weight: 600;
  color: #475569;
  cursor: pointer;
  font-family: inherit;
  transition: all .18s;
  white-space: nowrap;
}
.btn-back:hover { border-color: #6366f1; color: #6366f1; }

.btn-pay {
  flex: 1;
  display: flex; align-items: center; justify-content: center; gap: .5rem;
  padding: .75rem 1rem;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: .88rem; font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: opacity .18s, transform .18s;
  box-shadow: 0 4px 20px rgba(99,102,241,.35);
  white-space: nowrap;
}
.btn-pay:hover:not(:disabled) { opacity: .88; transform: translateY(-1px); }
.btn-pay:disabled { opacity: .5; cursor: not-allowed; }
</style>