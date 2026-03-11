<template>
  <div class="review-section">

    <!-- Header -->
    <div class="review-header">
      <h2 class="review-title">⭐ Đánh giá khóa học</h2>
      <span class="review-count">{{ stats.totalReviews }} đánh giá</span>
    </div>

    <!-- Skeleton -->
    <div v-if="loadingReviews" class="sk-wrap">
      <div class="sk sk-stats"></div>
      <div class="sk sk-card-sm"></div>
      <div class="sk sk-card-sm"></div>
    </div>

    <template v-else>
      <!-- 2-col layout: danh sách trái, form phải -->
      <div class="review-body">

        <!-- CỘT TRÁI: Stats + danh sách -->
        <div class="review-left">
          <div v-if="stats.totalReviews > 0" class="stats-box">
            <div class="avg-block">
              <div class="avg-num">{{ stats.avgRating }}</div>
              <div class="avg-stars">
                <span v-for="i in 5" :key="i"
                      :class="['star-icon', i <= Math.round(stats.avgRating) ? 'filled' : '']">★</span>
              </div>
              <div class="avg-label">trên 5</div>
            </div>
            <div class="dist-block">
              <div v-for="star in [5,4,3,2,1]" :key="star" class="dist-row">
                <span class="dist-star">{{ star }}★</span>
                <div class="dist-bar-wrap">
                  <div class="dist-bar-fill"
                       :style="`width:${stats.totalReviews ? (stats.distribution[star]||0)/stats.totalReviews*100 : 0}%`">
                  </div>
                </div>
                <span class="dist-count">{{ stats.distribution[star] || 0 }}</span>
              </div>
            </div>
          </div>

          <div v-if="reviews.length" class="review-list">
            <div v-for="r in visibleReviews" :key="r.id" class="review-card">
              <div class="rc-avatar">
                <img v-if="r.avatar" :src="r.avatar" class="avatar-img" />
                <span v-else class="avatar-init">{{ r.username?.charAt(0)?.toUpperCase() }}</span>
              </div>
              <div class="rc-body">
                <div class="rc-top">
                  <span class="rc-username">{{ r.username }}</span>
                  <div class="rc-stars">
                    <span v-for="i in 5" :key="i" :class="['rs', i <= r.rating ? 'filled' : '']">★</span>
                  </div>
                  <span class="rc-date">{{ fmtDate(r.createdAt) }}</span>
                </div>
                <div v-if="r.comment" class="rc-comment">{{ r.comment }}</div>
              </div>
            </div>
            <button v-if="reviews.length > 4 && !showAll" @click="showAll=true" class="btn-show-more">
              Xem thêm {{ reviews.length - 4 }} đánh giá ↓
            </button>
          </div>

          <div v-else class="review-empty">
            <span class="empty-ico">💬</span>
            <span>Chưa có đánh giá nào. Hãy là người đầu tiên!</span>
          </div>
        </div>

        <!-- CỘT PHẢI: Form / preview -->
        <div class="review-right">

          <!-- Đã enroll -->
          <div v-if="isEnrolled" class="write-review">
            <div class="wr-label">
              {{ myReview ? '✏️ Đánh giá của bạn' : '📝 Viết đánh giá' }}
            </div>

            <!-- Preview review đã có -->
            <div v-if="myReview && !editing" class="my-review-preview">
              <div class="mrp-stars">
                <span v-for="i in 5" :key="i" :class="['mrp-star', i<=myReview.rating?'filled':'']">★</span>
                <span class="mrp-score">{{ myReview.rating }}/5 · {{ ratingLabels[myReview.rating] }}</span>
              </div>
              <p v-if="myReview.comment" class="mrp-comment">{{ myReview.comment }}</p>
              <p v-else class="mrp-no-comment">Không có nhận xét</p>
              <div class="mrp-actions">
                <button @click="editing=true" class="btn btn-edit-sm">✏️ Sửa</button>
                <button @click="deleteMyReview" class="btn btn-danger-sm" :disabled="saving">🗑 Xóa</button>
              </div>
            </div>

            <!-- Form nhập -->
            <div v-else>
              <div class="star-picker">
                <span v-for="i in 5" :key="i"
                      :class="['star-pick', i <= hoverStar || i <= form.rating ? 'filled' : '']"
                      @mouseenter="hoverStar=i" @mouseleave="hoverStar=0"
                      @click="form.rating=i">★</span>
                <span class="star-hint">{{ ratingLabel }}</span>
              </div>
              <textarea v-model="form.comment" class="review-textarea"
                        placeholder="Chia sẻ trải nghiệm học tập của bạn..."
                        rows="4" maxlength="1000"></textarea>
              <div class="wr-footer">
                <span class="char-count">{{ form.comment.length }}/1000</span>
                <div class="wr-btns">
                  <button v-if="editing" @click="cancelEdit" class="btn btn-ghost-sm">Hủy</button>
                  <button @click="submit" class="btn btn-accent-sm" :disabled="saving || !form.rating">
                    {{ saving ? '...' : myReview ? '✓ Cập nhật' : '✓ Gửi' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Chưa enroll -->
          <div v-else class="enroll-prompt">
            <span class="ep-icon">🔒</span>
            <p>Đăng ký khóa học để viết đánh giá</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'

const props = defineProps({
  courseId:   { type: Number, required: true },
  isEnrolled: { type: Boolean, default: false }
})
const emit = defineEmits(['stats-updated'])

const auth           = useAuthStore()
const reviews        = ref([])
const stats          = ref({ totalReviews: 0, avgRating: 0, distribution: {} })
const myReview       = ref(null)
const saving         = ref(false)
const loadingReviews = ref(true)
const showAll        = ref(false)
const hoverStar      = ref(0)
const editing        = ref(false)
const form           = ref({ rating: 0, comment: '' })

const ratingLabels   = ['', 'Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Xuất sắc']
const ratingLabel    = computed(() => ratingLabels[hoverStar.value || form.value.rating] || 'Chọn số sao')
const visibleReviews = computed(() => showAll.value ? reviews.value : reviews.value.slice(0, 4))
const fmtDate        = dt => dt ? new Date(dt).toLocaleDateString('vi-VN', { day:'2-digit', month:'2-digit', year:'numeric' }) : ''

// Load tất cả data 1 lần, song song
async function loadAll() {
  loadingReviews.value = true
  try {
    const requests = [
      api.get(`/reviews/course/${props.courseId}`),
      api.get(`/reviews/course/${props.courseId}/stats`),
      ...(props.isEnrolled && auth.user?.id
          ? [api.get(`/reviews/my?userId=${auth.user.id}&courseId=${props.courseId}`)]
          : [])
    ]

    const results = await Promise.allSettled(requests)

    if (results[0].status === 'fulfilled') reviews.value = results[0].value.data
    if (results[1].status === 'fulfilled') {
      stats.value = results[1].value.data
      emit('stats-updated', results[1].value.data)
    }
    if (results[2]?.status === 'fulfilled' && results[2].value.data) {
      myReview.value     = results[2].value.data
      form.value.rating  = results[2].value.data.rating
      form.value.comment = results[2].value.data.comment || ''
    }
  } catch(e) { console.error(e) }
  finally { loadingReviews.value = false }
}

async function reloadReviewsAndStats() {
  const [rv, st] = await Promise.all([
    api.get(`/reviews/course/${props.courseId}`),
    api.get(`/reviews/course/${props.courseId}/stats`)
  ])
  reviews.value = rv.data
  stats.value   = st.data
  emit('stats-updated', st.data)
}

function cancelEdit() {
  editing.value = false
  if (myReview.value) {
    form.value.rating  = myReview.value.rating
    form.value.comment = myReview.value.comment || ''
  }
}

async function submit() {
  if (!form.value.rating) return
  saving.value = true
  try {
    const r = await api.post('/reviews', {
      userId:   auth.user.id,
      courseId: props.courseId,
      rating:   form.value.rating,
      comment:  form.value.comment
    })
    myReview.value = r.data
    editing.value  = false
    await reloadReviewsAndStats()
  } catch(e) { console.error(e) }
  finally { saving.value = false }
}

async function deleteMyReview() {
  if (!confirm('Bạn có chắc muốn xóa đánh giá?')) return
  saving.value = true
  try {
    await api.delete(`/reviews/${myReview.value.id}?userId=${auth.user.id}`)
    myReview.value = null
    form.value     = { rating: 0, comment: '' }
    editing.value  = false
    await reloadReviewsAndStats()
  } catch(e) { console.error(e) }
  finally { saving.value = false }
}

// Khi user vừa enroll → reload để lấy myReview
watch(() => props.isEnrolled, (val) => { if (val) loadAll() })

onMounted(loadAll)
</script>

<style scoped>
.review-section {
  background: var(--surface); border: 1.5px solid var(--border);
  border-radius: 16px; padding: 1.4rem; box-shadow: var(--shadow-sm);
}

.review-header { display: flex; align-items: center; gap: .7rem; margin-bottom: 1.1rem; }
.review-title  { font-size: 1rem; font-weight: 800; }
.review-count  { font-size: .76rem; color: var(--muted); background: var(--surface2); border: 1.5px solid var(--border); padding: .16rem .52rem; border-radius: 100px; }

/* Skeleton */
.sk-wrap { display: flex; flex-direction: column; gap: .7rem; }
.sk { background: linear-gradient(90deg,#e2e8f0 25%,#f1f5f9 50%,#e2e8f0 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; border-radius: 10px; }
@keyframes shimmer { to { background-position: -200% 0; } }
.sk-stats   { height: 88px; }
.sk-card-sm { height: 62px; }

/* 2-col layout */
.review-body {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 1.1rem;
  align-items: start;
}
@media (max-width: 780px) {
  .review-body { grid-template-columns: 1fr; }
  .review-right { order: -1; }
}

/* LEFT */
.stats-box {
  display: flex; gap: 1rem; align-items: center;
  padding: .8rem .95rem; background: var(--surface2);
  border: 1.5px solid var(--border); border-radius: 12px;
  margin-bottom: .85rem; flex-wrap: wrap;
}
.avg-block { display: flex; flex-direction: column; align-items: center; min-width: 68px; }
.avg-num   { font-size: 2.2rem; font-weight: 800; line-height: 1; background: linear-gradient(135deg,#f59e0b,#ef4444); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.avg-stars { display: flex; gap: 1px; margin: .22rem 0 .08rem; }
.avg-label { font-size: .66rem; color: var(--muted); }
.star-icon { font-size: .95rem; color: #d1d5db; }
.star-icon.filled { color: #f59e0b; }

.dist-block { flex: 1; display: flex; flex-direction: column; gap: .26rem; min-width: 130px; }
.dist-row   { display: flex; align-items: center; gap: .4rem; }
.dist-star  { font-size: .68rem; color: var(--muted); width: 17px; text-align: right; flex-shrink: 0; }
.dist-bar-wrap { flex: 1; height: 5px; background: var(--border); border-radius: 100px; overflow: hidden; }
.dist-bar-fill { height: 100%; background: linear-gradient(90deg,#f59e0b,#fbbf24); border-radius: 100px; transition: width .6s ease; }
.dist-count { font-size: .66rem; color: var(--muted); width: 13px; text-align: right; flex-shrink: 0; }

.review-list { display: flex; flex-direction: column; gap: .6rem; }
.review-card { display: flex; gap: .7rem; padding: .75rem .85rem; background: var(--surface2); border-radius: 10px; border: 1.5px solid var(--border); transition: border-color .15s; }
.review-card:hover { border-color: #bfdbfe; }
.rc-avatar   { flex-shrink: 0; }
.avatar-img  { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; }
.avatar-init { width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg,var(--accent),#7c3aed); color: #fff; font-size: .82rem; font-weight: 700; display: flex; align-items: center; justify-content: center; }
.rc-body     { flex: 1; min-width: 0; }
.rc-top      { display: flex; align-items: center; gap: .4rem; flex-wrap: wrap; margin-bottom: .22rem; }
.rc-username { font-size: .79rem; font-weight: 700; }
.rc-stars    { display: flex; gap: 1px; }
.rs          { font-size: .78rem; color: #d1d5db; }
.rs.filled   { color: #f59e0b; }
.rc-date     { font-size: .67rem; color: var(--muted); margin-left: auto; }
.rc-comment  { font-size: .78rem; color: var(--text2); line-height: 1.55; }

.btn-show-more { width: 100%; padding: .42rem; border-radius: 8px; border: 1.5px dashed var(--border); background: none; color: var(--muted); font-size: .74rem; font-weight: 600; cursor: pointer; font-family: 'Plus Jakarta Sans',sans-serif; transition: all .15s; margin-top: .2rem; }
.btn-show-more:hover { border-color: var(--accent); color: var(--accent); background: var(--accent-light); }

.review-empty { display: flex; align-items: center; gap: .55rem; padding: 1rem .4rem; color: var(--muted); font-size: .81rem; }
.empty-ico    { font-size: 1.3rem; opacity: .5; }

/* RIGHT */
.write-review {
  background: linear-gradient(135deg,#eff6ff,#faf5ff);
  border: 1.5px solid #bfdbfe; border-radius: 12px;
  padding: 1rem 1.05rem; position: sticky; top: 78px;
}
.wr-label { font-size: .8rem; font-weight: 700; color: var(--accent); margin-bottom: .65rem; }

.my-review-preview { display: flex; flex-direction: column; gap: .45rem; }
.mrp-stars  { display: flex; align-items: center; gap: 1px; }
.mrp-star   { font-size: 1.25rem; color: #d1d5db; }
.mrp-star.filled { color: #f59e0b; }
.mrp-score  { font-size: .74rem; font-weight: 700; color: var(--muted); margin-left: .4rem; }
.mrp-comment    { font-size: .8rem; color: var(--text2); line-height: 1.55; background: var(--surface); border-radius: 8px; padding: .5rem .65rem; border: 1px solid var(--border); margin: 0; }
.mrp-no-comment { font-size: .75rem; color: var(--muted); font-style: italic; margin: 0; }
.mrp-actions    { display: flex; gap: .35rem; margin-top: .15rem; }

.star-picker { display: flex; align-items: center; gap: .03rem; margin-bottom: .6rem; }
.star-pick   { font-size: 1.6rem; cursor: pointer; color: #d1d5db; transition: color .1s, transform .1s; line-height: 1; user-select: none; }
.star-pick.filled { color: #f59e0b; }
.star-pick:hover  { transform: scale(1.15); }
.star-hint   { font-size: .73rem; color: var(--muted); margin-left: .4rem; font-weight: 600; min-width: 72px; }

.review-textarea { width: 100%; padding: .52rem .72rem; border: 1.5px solid var(--border); border-radius: 8px; background: var(--surface); color: var(--text); font-size: .8rem; font-family: 'Plus Jakarta Sans',sans-serif; resize: vertical; outline: none; transition: border-color .15s; box-sizing: border-box; line-height: 1.6; }
.review-textarea:focus { border-color: var(--accent); }
.wr-footer   { display: flex; align-items: center; justify-content: space-between; margin-top: .42rem; }
.char-count  { font-size: .68rem; color: var(--muted); }
.wr-btns     { display: flex; gap: .32rem; }

.enroll-prompt { display: flex; flex-direction: column; align-items: center; gap: .45rem; padding: 1.4rem 1rem; background: var(--surface2); border: 1.5px dashed var(--border); border-radius: 12px; text-align: center; color: var(--muted); font-size: .79rem; }
.ep-icon { font-size: 1.5rem; opacity: .45; }

/* Buttons */
.btn { display: inline-flex; align-items: center; gap: .28rem; border: none; cursor: pointer; font-family: 'Plus Jakarta Sans',sans-serif; font-weight: 600; transition: all .15s; border-radius: 7px; }
.btn-accent-sm  { padding: .34rem .82rem; font-size: .76rem; background: var(--accent); color: #fff; }
.btn-accent-sm:hover:not(:disabled)  { background: var(--accent-dark); }
.btn-accent-sm:disabled { opacity: .5; cursor: not-allowed; }
.btn-ghost-sm   { padding: .34rem .72rem; font-size: .76rem; background: var(--surface); color: var(--muted); border: 1.5px solid var(--border); }
.btn-ghost-sm:hover { border-color: var(--accent); color: var(--accent); }
.btn-edit-sm    { padding: .3rem .68rem; font-size: .73rem; background: var(--accent-light); color: var(--accent); border: 1.5px solid #bfdbfe; }
.btn-edit-sm:hover { background: var(--accent); color: #fff; }
.btn-danger-sm  { padding: .3rem .68rem; font-size: .73rem; background: var(--red-light); color: var(--red); border: 1px solid #fca5a5; }
.btn-danger-sm:hover:not(:disabled) { background: #fecaca; }
.btn-danger-sm:disabled { opacity: .5; cursor: not-allowed; }
</style>