<template>
  <div class="checkout-page">

    <div v-if="!cart.length" class="empty-state">
      <p>Giỏ hàng trống. <router-link to="/courses">Quay lại mua sắm</router-link></p>
    </div>

    <template v-else>
      <h1 class="ck-title">Thanh toán</h1>

      <div class="ck-layout">
        <!-- LEFT: Địa chỉ + Phương thức -->
        <div class="ck-left">

          <!-- Billing -->
          <div class="ck-section">
            <h2 class="ck-sh">Địa chỉ thanh toán</h2>
            <div class="form-row">
              <div class="fg">
                <label>Họ và tên</label>
                <input v-model="billing.name" placeholder="Nguyễn Văn A"/>
              </div>
              <div class="fg">
                <label>Email</label>
                <input v-model="billing.email" type="email" placeholder="email@gmail.com" readonly
                       style="opacity:.6;cursor:not-allowed"/>
              </div>
            </div>
            <div class="fg">
              <label>Quốc gia</label>
              <select v-model="billing.country">
                <option value="VN">🇻🇳 Việt Nam</option>
                <option value="US">🇺🇸 United States</option>
                <option value="SG">🇸🇬 Singapore</option>
              </select>
            </div>
          </div>

          <!-- Payment method -->
          <div class="ck-section">
            <h2 class="ck-sh">Phương thức thanh toán <span class="secure-badge">🔒 Bảo mật</span></h2>

            <div class="method-tabs">
              <button v-for="m in methods" :key="m.id"
                      :class="['method-tab', { active: selectedMethod === m.id }]"
                      @click="selectedMethod = m.id">
                <span class="mt-icon">{{ m.icon }}</span>
                <span>{{ m.name }}</span>
                <div v-if="m.logos" class="mt-logos">
                  <span v-for="l in m.logos" :key="l" class="mt-logo">{{ l }}</span>
                </div>
              </button>
            </div>

            <!-- Card form -->
            <div v-if="selectedMethod === 'card'" class="card-form">
              <div class="fg">
                <label>Số thẻ</label>
                <div class="card-input-wrap">
                  <input v-model="card.number" placeholder="1234 5678 9012 3456"
                         maxlength="19" @input="formatCardNumber"/>
                  <span class="card-brand-icons">
                    <span>VISA</span><span>MC</span><span>JCB</span>
                  </span>
                </div>
              </div>
              <div class="form-row">
                <div class="fg">
                  <label>Ngày hết hạn</label>
                  <input v-model="card.expiry" placeholder="MM/YY" maxlength="5" @input="formatExpiry"/>
                </div>
                <div class="fg">
                  <label>CVC/CVV</label>
                  <input v-model="card.cvv" placeholder="123" maxlength="4" type="password"/>
                </div>
              </div>
              <div class="fg">
                <label>Tên chủ thẻ</label>
                <input v-model="card.name" placeholder="NGUYEN VAN A" style="text-transform:uppercase"/>
              </div>
              <label class="save-card">
                <input type="checkbox" v-model="card.save"/> Lưu thẻ cho lần sau
              </label>
            </div>

            <!-- Wallet -->
            <div v-if="selectedMethod === 'wallet'" class="wallet-options">
              <label v-for="w in wallets" :key="w.id"
                     :class="['wallet-item', { active: selectedWallet === w.id }]">
                <input type="radio" v-model="selectedWallet" :value="w.id" hidden/>
                <span class="w-icon">{{ w.icon }}</span>
                <span class="w-name">{{ w.name }}</span>
                <div v-if="selectedWallet === w.id" class="w-check">✓</div>
              </label>
            </div>

            <!-- Bank transfer -->
            <div v-if="selectedMethod === 'bank'" class="bank-info">
              <p class="bank-note">Chọn ngân hàng để thanh toán qua Internet Banking / QR Code</p>
              <div class="bank-list">
                <label v-for="b in banks" :key="b.id"
                       :class="['bank-item', { active: selectedBank === b.id }]">
                  <input type="radio" v-model="selectedBank" :value="b.id" hidden/>
                  <span class="b-icon">{{ b.icon }}</span>
                  <span class="b-name">{{ b.name }}</span>
                  <div v-if="selectedBank === b.id" class="w-check">✓</div>
                </label>
              </div>
            </div>
          </div>

          <div v-if="errorMsg" class="err-box">{{ errorMsg }}</div>

          <!-- Submit -->
          <button class="btn-pay-now" @click="pay" :disabled="paying">
            <span v-if="paying">⏳ Đang xử lý...</span>
            <span v-else>🔒 Thanh toán {{ fmt(totalPrice) }}</span>
          </button>

          <p class="ck-note">
            Bằng cách thanh toán, bạn đồng ý với <a href="#">Điều khoản dịch vụ</a> của EduFlow.
            <br/>🔒 Giao dịch được mã hóa SSL 256-bit.
          </p>
        </div>

        <!-- RIGHT: Order summary -->
        <div class="ck-right">
          <div class="order-box">
            <h3>Tóm tắt đơn hàng</h3>

            <div class="ob-items">
              <div v-for="item in cart" :key="item.id" class="ob-item">
                <div class="obi-icon">{{ courseIcon(item.id) }}</div>
                <div class="obi-info">
                  <div class="obi-title">{{ item.title }}</div>
                  <div class="obi-inst">{{ item.instructor || 'EduFlow' }}</div>
                </div>
                <div class="obi-price">{{ fmt(item.price) }}</div>
              </div>
            </div>

            <div class="ob-divider"></div>

            <div class="ob-row">
              <span>Tạm tính</span>
              <span>{{ fmt(totalPrice) }}</span>
            </div>
            <div class="ob-row">
              <span>Giảm giá</span>
              <span class="ob-discount">- {{ fmt(0) }}</span>
            </div>

            <div class="ob-divider"></div>

            <div class="ob-total">
              <strong>Tổng cộng</strong>
              <strong class="ob-total-price">{{ fmt(totalPrice) }}</strong>
            </div>

            <div class="guarantee-box">
              <span class="g-icon">🛡️</span>
              <div>
                <div class="g-title">Đảm bảo hoàn tiền 30 ngày</div>
                <div class="g-desc">Không hài lòng? Hoàn tiền 100% trong 30 ngày.</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'

const router    = useRouter()
const cartStore = useCartStore()
const auth      = useAuthStore()

const cart       = computed(() => cartStore.items)
const totalPrice = computed(() => cart.value.reduce((s, c) => s + (c.price || 0), 0))

// Form state
const selectedMethod = ref('card')
const selectedWallet = ref('momo')
const selectedBank   = ref('vcb')
const paying         = ref(false)
const errorMsg       = ref('')

const billing = ref({
  name:    auth.user?.fullName || auth.user?.username || '',
  email:   auth.user?.email || '',
  country: 'VN'
})

const card = ref({ number: '', expiry: '', cvv: '', name: '', save: true })

const methods = [
  { id: 'card',   icon: '💳', name: 'Thẻ', logos: ['VISA','MC','JCB'] },
  { id: 'wallet', icon: '📱', name: 'Ví điện tử' },
  { id: 'bank',   icon: '🏦', name: 'Internet Banking' },
]

const wallets = [
  { id: 'momo',    icon: '🟣', name: 'MoMo' },
  { id: 'zalopay', icon: '🔵', name: 'ZaloPay' },
  { id: 'vnpay',   icon: '🔴', name: 'VNPay QR' },
]

const banks = [
  { id: 'vcb',  icon: '🟢', name: 'Vietcombank' },
  { id: 'tcb',  icon: '🔴', name: 'Techcombank' },
  { id: 'acb',  icon: '🔵', name: 'ACB' },
  { id: 'bidv', icon: '🟡', name: 'BIDV' },
  { id: 'mb',   icon: '⚫', name: 'MB Bank' },
]

const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']
const courseIcon = id => icons[Number(id) % icons.length]

function fmt(amount) {
  if (!amount) return 'Miễn phí'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

function formatCardNumber(e) {
  let val = e.target.value.replace(/\D/g,'').slice(0,16)
  card.value.number = val.replace(/(.{4})/g,'$1 ').trim()
}

function formatExpiry(e) {
  let val = e.target.value.replace(/\D/g,'').slice(0,4)
  if (val.length >= 2) val = val.slice(0,2) + '/' + val.slice(2)
  card.value.expiry = val
}

async function pay() {
  errorMsg.value = ''
  if (!cart.value.length) return

  if (selectedMethod.value === 'card') {
    if (!card.value.number || !card.value.expiry || !card.value.cvv) {
      errorMsg.value = 'Vui lòng điền đầy đủ thông tin thẻ!'
      return
    }
  }

  paying.value = true
  try {
    // B5 FIX: tạo payments rồi redirect sang /payment/confirm — không tự confirm
    const payments = []
    for (const item of cart.value) {
      if (!item.isFree && item.price > 0) {
        try {
          const r = await api.post(`/payment/create?courseId=${item.id}`)
          payments.push({ txnRef: r.data.txnRef, amount: r.data.amount, courseId: String(r.data.courseId) })
        } catch (e) {
          console.warn('[Checkout] skip item', item.id, e.response?.data?.error)
        }
      }
    }
    if (!payments.length) {
      errorMsg.value = 'Không có khóa học nào cần thanh toán!'
      return
    }
    router.push({ path: '/payment/confirm', query: {
        txnRefs:   JSON.stringify(payments.map(p => p.txnRef)),
        courseIds: JSON.stringify(payments.map(p => p.courseId)),
        amount:    payments.reduce((s, p) => s + Number(p.amount), 0),
        courseTitle: payments.length === 1
            ? cart.value.find(c => String(c.id) === payments[0].courseId)?.title || ''
            : `${payments.length} khóa học`
      }})
  } catch (e) {
    errorMsg.value = e.response?.data?.error || 'Lỗi thanh toán. Vui lòng thử lại!'
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.checkout-page { padding: 2rem 2.5rem; max-width: 1100px; margin: 0 auto; }
.ck-title { font-size: 1.6rem; font-weight: 800; margin: 0 0 1.75rem; }

.ck-layout { display: grid; grid-template-columns: 1fr 360px; gap: 2.5rem; align-items: start; }
@media (max-width: 900px) { .ck-layout { grid-template-columns: 1fr; } }

/* Section */
.ck-section { background: var(--surface); border: 1.5px solid var(--border); border-radius: 16px; padding: 1.5rem; margin-bottom: 1.25rem; }
.ck-sh { font-size: 1rem; font-weight: 800; margin: 0 0 1.1rem; display: flex; align-items: center; gap: .6rem; }
.secure-badge { font-size: .72rem; font-weight: 600; background: #f0fdf4; color: var(--green); border: 1px solid #a7f3d0; padding: .15rem .5rem; border-radius: 100px; }

/* Forms */
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: .85rem; }
.fg { display: flex; flex-direction: column; gap: .3rem; margin-bottom: .85rem; }
.fg label { font-size: .78rem; font-weight: 700; color: var(--text2); }
.fg input, .fg select {
  padding: .58rem .85rem;
  background: var(--surface);
  border: 1.5px solid var(--border);
  border-radius: 9px;
  color: var(--text);
  font-size: .88rem;
  font-family: inherit;
  outline: none;
  transition: border-color .15s;
}
.fg input:focus, .fg select:focus { border-color: var(--accent); box-shadow: 0 0 0 3px var(--accent-light); }

/* Method tabs */
.method-tabs { display: flex; gap: .6rem; margin-bottom: 1.1rem; flex-wrap: wrap; }
.method-tab {
  display: flex; align-items: center; gap: .5rem;
  padding: .5rem 1rem; border: 1.5px solid var(--border);
  border-radius: 9px; background: var(--surface2);
  font-size: .83rem; font-weight: 600; cursor: pointer;
  font-family: inherit; color: var(--text); transition: all .15s;
}
.method-tab:hover { border-color: var(--accent); }
.method-tab.active { border-color: var(--accent); background: var(--accent-light); color: var(--accent); }
.mt-icon { font-size: 1rem; }
.mt-logos { display: flex; gap: .3rem; margin-left: .3rem; }
.mt-logo { font-size: .62rem; font-weight: 800; background: var(--border); padding: .08rem .3rem; border-radius: 3px; }

/* Card form */
.card-input-wrap { position: relative; }
.card-input-wrap input { width: 100%; box-sizing: border-box; padding-right: 90px; }
.card-brand-icons { position: absolute; right: .7rem; top: 50%; transform: translateY(-50%); display: flex; gap: .25rem; font-size: .6rem; font-weight: 800; color: var(--muted); }
.save-card { display: flex; align-items: center; gap: .5rem; font-size: .8rem; cursor: pointer; color: var(--text2); }
.save-card input { cursor: pointer; accent-color: var(--accent); }

/* Wallet / Bank */
.wallet-options, .bank-list { display: flex; flex-direction: column; gap: .5rem; }
.bank-note { font-size: .8rem; color: var(--muted); margin: 0 0 .75rem; }
.wallet-item, .bank-item {
  display: flex; align-items: center; gap: .65rem;
  padding: .65rem 1rem; border: 1.5px solid var(--border);
  border-radius: 10px; cursor: pointer; transition: all .15s;
}
.wallet-item:hover, .bank-item:hover { border-color: var(--accent); background: var(--accent-light); }
.wallet-item.active, .bank-item.active { border-color: var(--accent); background: var(--accent-light); }
.w-icon, .b-icon { font-size: 1.3rem; flex-shrink: 0; }
.w-name, .b-name { flex: 1; font-size: .86rem; font-weight: 600; }
.w-check { width: 22px; height: 22px; background: var(--accent); border-radius: 50%; color: #fff; font-size: .72rem; display: flex; align-items: center; justify-content: center; }

/* Pay button */
.btn-pay-now {
  width: 100%; padding: .85rem; font-size: 1rem; font-weight: 800;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff; border: none; border-radius: 12px; cursor: pointer;
  font-family: inherit; box-shadow: 0 4px 15px rgba(124,58,237,.4);
  transition: opacity .15s; margin-bottom: .75rem;
}
.btn-pay-now:hover:not(:disabled) { opacity: .9; }
.btn-pay-now:disabled { opacity: .6; cursor: not-allowed; }

.ck-note { font-size: .74rem; color: var(--muted); line-height: 1.7; text-align: center; }
.ck-note a { color: var(--accent); }

.err-box { background: #fee2e2; color: #dc2626; border-radius: 9px; padding: .65rem 1rem; font-size: .83rem; margin-bottom: .85rem; }

/* Order summary */
.order-box {
  background: var(--surface); border: 1.5px solid var(--border);
  border-radius: 16px; padding: 1.5rem; position: sticky; top: 1.5rem;
}
.order-box h3 { font-size: 1rem; font-weight: 800; margin: 0 0 1.1rem; }

.ob-items { display: flex; flex-direction: column; gap: .75rem; margin-bottom: 1rem; }
.ob-item  { display: flex; align-items: center; gap: .7rem; }
.obi-icon { width: 40px; height: 40px; border-radius: 9px; background: var(--accent-light); display: flex; align-items: center; justify-content: center; font-size: 1.3rem; flex-shrink: 0; }
.obi-info { flex: 1; min-width: 0; }
.obi-title { font-size: .82rem; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.obi-inst  { font-size: .72rem; color: var(--muted); }
.obi-price { font-size: .85rem; font-weight: 700; color: var(--accent); flex-shrink: 0; }

.ob-divider { border: none; border-top: 1.5px solid var(--border); margin: .85rem 0; }
.ob-row { display: flex; justify-content: space-between; font-size: .85rem; margin-bottom: .5rem; }
.ob-row span:first-child { color: var(--muted); }
.ob-discount { color: var(--green); }
.ob-total { display: flex; justify-content: space-between; align-items: center; font-size: .95rem; margin-top: .5rem; }
.ob-total-price { font-size: 1.25rem; color: var(--accent); }

/* Guarantee */
.guarantee-box {
  display: flex; gap: .65rem; align-items: flex-start;
  background: var(--surface2); border-radius: 10px;
  padding: .85rem; margin-top: 1.1rem;
}
.g-icon { font-size: 1.3rem; flex-shrink: 0; }
.g-title { font-size: .82rem; font-weight: 700; }
.g-desc  { font-size: .74rem; color: var(--muted); margin-top: .15rem; }

.empty-state { text-align: center; padding: 4rem; }
</style>