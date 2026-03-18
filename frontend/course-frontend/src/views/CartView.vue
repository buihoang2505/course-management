<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">🛒 Giỏ hàng</h1>
      <p class="page-sub">{{ cartStore.count }} khóa học</p>
    </div>

    <div v-if="loading" class="empty-state"><div class="spinner"></div></div>

    <div v-else-if="apiError" class="empty-state">
      <div class="empty-icon">⚠️</div>
      <h3>Không thể tải giỏ hàng</h3>
      <p>Lỗi kết nối đến server, vui lòng thử lại</p>
      <button @click="() => { apiError = false; $router.go(0) }" class="btn btn-accent" style="margin-top:1rem">Thử lại</button>
    </div>

    <div v-else-if="!cartCourses.length" class="empty-state">
      <div class="empty-icon">🛒</div>
      <h3>Giỏ hàng trống</h3>
      <p>Thêm khóa học vào giỏ để thanh toán sau</p>
      <router-link to="/courses" class="btn btn-accent" style="margin-top:1rem">Khám phá khóa học</router-link>
    </div>

    <div v-else class="cart-layout">
      <!-- Danh sách -->
      <div class="cart-list">
        <div v-for="course in cartCourses" :key="course.id" class="cart-item">
          <span class="ci-icon">{{ icons[course.id % icons.length] }}</span>
          <div class="ci-info">
            <div class="ci-title">{{ course.title }}</div>
            <div class="ci-inst">{{ course.instructor || 'Chưa xác định' }}</div>
            <div class="ci-stats">
              <span v-if="course.avgRating">⭐ {{ course.avgRating }}</span>
              <span>📖 {{ course.credits }} TC</span>
            </div>
          </div>
          <div class="ci-right">
            <div class="ci-price">
              <span v-if="!course.isFree && course.price > 0" class="price-paid">{{ fmt(course.price) }}</span>
              <span v-else class="price-free">Miễn phí</span>
            </div>
            <button @click="removeCart(course.id)" class="ci-remove" title="Xóa">✕</button>
          </div>
        </div>
      </div>

      <!-- Tổng kết -->
      <div class="cart-summary">
        <div class="cs-title">Tổng đơn hàng</div>
        <div class="cs-rows">
          <div v-for="c in cartCourses" :key="c.id" class="cs-row">
            <span class="cs-name">{{ c.title }}</span>
            <span>{{ c.isFree || !c.price ? 'Miễn phí' : fmt(c.price) }}</span>
          </div>
        </div>
        <div class="cs-total">
          <span>Tổng cộng</span>
          <strong class="cs-total-price">{{ fmt(totalPrice) }}</strong>
        </div>
        <button v-if="paidItems.length" @click="checkoutAll" class="btn btn-accent btn-checkout"
                :disabled="checking">
          {{ checking ? '...' : '💳 Thanh toán (' + paidItems.length + ' khóa)' }}
        </button>
        <div v-if="freeItems.length" class="cs-free-note">
          {{ freeItems.length }} khóa miễn phí sẽ được đăng ký riêng
        </div>
        <button v-if="freeItems.length" @click="enrollFree" class="btn btn-outline btn-checkout"
                :disabled="enrollingFree">
          {{ enrollingFree ? '...' : '🚀 Đăng ký ' + freeItems.length + ' khóa miễn phí' }}
        </button>
      </div>
    </div>

    <div v-if="toast" :class="['toast', toast.type]">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'

const auth      = useAuthStore()
const cartStore = useCartStore()
const router  = useRouter()
const loading = ref(false)
const allCourses  = ref([])
const apiError    = ref(false)
const checking    = ref(false)
const enrollingFree = ref(false)
const toast = ref(null)
const icons = ['🖥️','📐','🔬','📊','🎨','⚙️','🌐','📱','🤖','🧮']


const cartCourses = computed(() =>
    allCourses.value.filter(c => cartStore.has(Number(c.id)))
)
const paidItems = computed(() => cartCourses.value.filter(c => !c.isFree && c.price > 0))
const freeItems = computed(() => cartCourses.value.filter(c =>  c.isFree || !c.price))
const totalPrice = computed(() => paidItems.value.reduce((s, c) => s + (c.price || 0), 0))

function fmt(price) {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}
function showToast(msg, type = 'success') {
  toast.value = { msg, type }
  setTimeout(() => toast.value = null, 3000)
}
function removeCart(id) {
  // B3 FIX: dùng cartStore thay vì localStorage trực tiếp
  cartStore.remove(Number(id))
  showToast('Đã xóa khỏi giỏ hàng')
}

async function checkoutAll() {
  if (!paidItems.value.length) return
  checking.value = true
  try {
    // B2 FIX: tạo Payment cho TẤT CẢ paid items, không chỉ item đầu
    const payments = []
    for (const item of paidItems.value) {
      try {
        const r = await api.post(`/payment/create?courseId=${item.id}`)
        payments.push({
          txnRef:      r.data.txnRef,
          amount:      r.data.amount,
          courseId:    String(r.data.courseId),
          courseTitle: r.data.courseTitle
        })
      } catch (e) {
        // Đã enroll hoặc lỗi — bỏ qua item này
        console.warn('[Cart] skip item', item.id, e.response?.data?.error)
      }
    }
    if (!payments.length) { showToast('Không có khóa nào cần thanh toán!', 'error'); return }
    // Truyền mảng txnRefs sang confirm page (JSON encode)
    router.push({ path: '/payment/confirm', query: {
        txnRefs:    JSON.stringify(payments.map(p => p.txnRef)),
        amount:     payments.reduce((s, p) => s + Number(p.amount), 0),
        courseIds:  JSON.stringify(payments.map(p => p.courseId)),
        courseTitle: payments.length === 1 ? payments[0].courseTitle : `${payments.length} khóa học`
      }})
  } catch(e) {
    showToast(e.response?.data?.error || 'Lỗi thanh toán!', 'error')
  } finally { checking.value = false }
}

async function enrollFree() {
  // B3 FIX: dùng cartStore thay vì localStorage trực tiếp
  enrollingFree.value = true
  let success = 0
  for (const course of freeItems.value) {
    try {
      await api.post(`/enrollments/enroll?courseId=${course.id}`)
      cartStore.remove(Number(course.id))
      success++
    } catch {}
  }
  if (success) showToast(`🎉 Đã đăng ký ${success} khóa học!`)
  enrollingFree.value = false
}

onMounted(async () => {
  loading.value = true
  try {
    const cartIds = cartStore.items.map(c => Number(c.id))
    if (!cartIds.length) { loading.value = false; return }

    // Bước 1: validate-cart — gửi IDs lên server, nhận lại IDs còn ACTIVE
    const validRes = await api.post('/courses/validate-cart', cartIds)
    const validIds = new Set((validRes.data || []).map(Number))

    // Bước 2: xóa khỏi localStorage những id bị xóa/deactivate bởi admin
    cartStore.items
        .filter(c => !validIds.has(Number(c.id)))
        .forEach(c => cartStore.remove(Number(c.id)))

    // Bước 3: fetch chi tiết chỉ những course còn hợp lệ
    if (validIds.size > 0) {
      const url = auth.isAdmin ? '/courses' : '/courses/active'
      const r = await api.get(url)
      allCourses.value = Array.isArray(r.data)
          ? r.data.filter(c => validIds.has(Number(c.id)))
          : []
    }
  } catch {
    apiError.value = true
  }
  loading.value = false
})
</script>

<style scoped>
.page { padding:2rem 2.5rem; max-width:1100px; margin:0 auto; }
@media(max-width:600px){ .page{ padding:1rem; } }
.page-header { margin-bottom:1.5rem; }
.page-title  { font-size:1.35rem; font-weight:800; margin-bottom:.2rem; }
.page-sub    { font-size:.82rem; color:var(--muted); }

.cart-layout { display:grid; grid-template-columns:1fr 320px; gap:1.5rem; align-items:start; }
@media(max-width:768px){ .cart-layout{ grid-template-columns:1fr; } }

.cart-list { display:flex; flex-direction:column; gap:.75rem; }
.cart-item {
  display:flex; align-items:flex-start; gap:1rem;
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:14px; padding:1rem 1.1rem; transition:border-color .15s;
}
.cart-item:hover { border-color:var(--accent); }
.ci-icon  { font-size:1.8rem; flex-shrink:0; line-height:1.2; }
.ci-info  { flex:1; min-width:0; }
.ci-title { font-size:.9rem; font-weight:800; color:var(--text); margin-bottom:.2rem; }
.ci-inst  { font-size:.76rem; color:var(--muted); margin-bottom:.3rem; }
.ci-stats { display:flex; gap:.6rem; font-size:.73rem; color:var(--muted); }
.ci-right { display:flex; flex-direction:column; align-items:flex-end; gap:.4rem; flex-shrink:0; }
.price-paid { font-size:1rem; font-weight:800; color:var(--accent); }
.price-free { font-size:.85rem; font-weight:700; color:var(--green); }
.ci-remove { width:26px; height:26px; border-radius:50%; background:var(--surface2);
  border:1.5px solid var(--border); cursor:pointer; font-size:.72rem;
  color:var(--muted); display:flex; align-items:center; justify-content:center; transition:all .15s; }
.ci-remove:hover { background:var(--red-light); color:var(--red); border-color:#fca5a5; }

/* Summary */
.cart-summary {
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:16px; padding:1.3rem; position:sticky; top:80px;
  display:flex; flex-direction:column; gap:.75rem;
}
.cs-title { font-size:1.05rem; font-weight:800; }
.cs-rows  { display:flex; flex-direction:column; gap:.4rem; }
.cs-row   { display:flex; justify-content:space-between; font-size:.8rem; }
.cs-name  { color:var(--muted); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:180px; }
.cs-total { display:flex; justify-content:space-between; align-items:baseline;
  border-top:1.5px solid var(--border); padding-top:.65rem; }
.cs-total-price { font-size:1.35rem; font-weight:800; color:var(--accent); }
.btn-checkout { width:100%; padding:.65rem; font-size:.88rem; border-radius:10px; }
.cs-free-note { font-size:.74rem; color:var(--muted); text-align:center; }

.btn { display:inline-flex; align-items:center; justify-content:center; gap:.4rem;
  border-radius:10px; font-size:.86rem; font-weight:700; cursor:pointer;
  border:none; font-family:inherit; transition:all .15s; padding:.5rem 1rem; text-decoration:none; }
.btn-accent  { background:var(--accent); color:#fff; }
.btn-accent:hover:not(:disabled){ opacity:.88; }
.btn-accent:disabled { opacity:.5; cursor:not-allowed; }
.btn-outline { background:var(--surface2); color:var(--text); border:1.5px solid var(--border); }
.btn-outline:hover { border-color:var(--accent); color:var(--accent); }

.empty-state { display:flex; flex-direction:column; align-items:center; padding:5rem 2rem; gap:.75rem; color:var(--muted); text-align:center; }
.empty-icon  { font-size:4rem; opacity:.5; }
.empty-state h3 { font-size:1.1rem; font-weight:700; color:var(--text); margin:0; }
.empty-state p  { font-size:.85rem; margin:0; }
.spinner { width:32px; height:32px; border:3px solid var(--border); border-top-color:var(--accent); border-radius:50%; animation:spin .6s linear infinite; }
@keyframes spin { to{transform:rotate(360deg)} }

.toast { position:fixed; bottom:2rem; right:2rem; padding:.72rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .25s ease; border:1.5px solid; }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light);   color:var(--red);   border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }
</style>