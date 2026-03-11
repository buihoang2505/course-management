<template>
  <div class="cert-page">

    <!-- ── HEADER ──────────────────────────────────────── -->
    <div class="cert-header">
      <div class="cert-header-inner">
        <h1 class="cert-title">🏆 Chứng chỉ của tôi</h1>
        <p class="cert-sub">{{ certificates.length }} chứng chỉ đã đạt được</p>
      </div>
    </div>

    <!-- ── LOADING ─────────────────────────────────────── -->
    <div v-if="loading" class="cert-loading">
      <div class="spin"></div>
      <span>Đang tải chứng chỉ...</span>
    </div>

    <!-- ── EMPTY ───────────────────────────────────────── -->
    <div v-else-if="!certificates.length" class="cert-empty">
      <div class="empty-icon">🎓</div>
      <h3>Chưa có chứng chỉ nào</h3>
      <p>Hoàn thành 100% bài học và pass tất cả quiz để nhận chứng chỉ</p>
      <router-link to="/courses" class="btn-browse">Khám phá khóa học</router-link>
    </div>

    <!-- ── LIST ────────────────────────────────────────── -->
    <div v-else class="cert-list">
      <div v-for="cert in certificates" :key="cert.id" class="cert-wrapper">

        <!-- Chứng chỉ landscape -->
        <div :id="`cert-${cert.id}`" class="cert-doc">
          <div class="cert-border-outer">
            <div class="cert-border-inner">

              <!-- Left accent panel -->
              <div class="cert-left-accent">
                <div class="accent-bar"></div>
                <div class="accent-logo">
                  <span class="al-icon">🎓</span>
                  <span class="al-name">EduFlow</span>
                </div>
                <div class="accent-label">CERTIFICATE<br>OF COMPLETION</div>
                <div class="accent-seal">
                  <div class="seal-circle">
                    <span class="seal-star">★</span>
                    <span class="seal-text">VERIFIED</span>
                  </div>
                </div>
              </div>

              <!-- Right content -->
              <div class="cert-right-content">
                <div class="crc-top">
                  <div class="crc-presented">CHỨNG NHẬN TRAO CHO</div>
                  <h1 class="crc-name">{{ cert.studentName }}</h1>
                  <p class="crc-desc">đã hoàn thành xuất sắc khóa học</p>
                  <h2 class="crc-course">{{ cert.courseTitle }}</h2>
                </div>

                <div class="crc-divider">
                  <div class="divider-line"></div>
                  <div class="divider-diamond">◆</div>
                  <div class="divider-line"></div>
                </div>

                <div class="crc-stats">
                  <div class="crc-stat">
                    <div class="cs-num">{{ cert.totalLessons }}</div>
                    <div class="cs-lbl">Bài học<br>hoàn thành</div>
                  </div>
                  <div class="crc-stat highlight">
                    <div class="cs-num">{{ cert.averageQuizScore }}%</div>
                    <div class="cs-lbl">Điểm TB<br>quiz</div>
                  </div>
                  <div class="crc-stat">
                    <div class="cs-num">{{ formatDate(cert.issuedAt) }}</div>
                    <div class="cs-lbl">Ngày<br>cấp</div>
                  </div>
                </div>

                <div class="crc-footer">
                  <div class="crc-signature">
                    <div class="sig-line"></div>
                    <div class="sig-name">EduFlow Academy</div>
                    <div class="sig-title">Ban Giám Đốc</div>
                  </div>
                  <div class="crc-code-block">
                    <div class="cb-label">Mã xác thực</div>
                    <div class="cb-code">{{ cert.certificateCode }}</div>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="cert-actions">
          <button class="btn-pdf" @click="printCert(cert.id)">
            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
              <polyline points="6 9 6 2 18 2 18 9"/>
              <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/>
              <rect x="6" y="14" width="12" height="8"/>
            </svg>
            In / Tải PDF
          </button>
          <button class="btn-copy" @click="copyVerifyLink(cert)" :class="{ copied: copied === cert.id }">
            <svg v-if="copied !== cert.id" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
              <rect x="9" y="9" width="13" height="13" rx="2"/>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
            </svg>
            <svg v-else width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            {{ copied === cert.id ? 'Đã sao chép!' : 'Link xác thực' }}
          </button>
        </div>

      </div>
    </div>

    <!-- Print area (hidden — only shows on @media print) -->
    <div id="print-area"></div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore }   from '../stores/auth'
import api                from '../services/api'

const auth         = useAuthStore()
const certificates = ref([])
const loading      = ref(true)
const copied       = ref(null)

onMounted(async () => {
  try {
    const res = await api.get(`/certificates/my?userId=${auth.user.id}`)
    certificates.value = res.data
  } catch (e) {
    console.error('Failed to load certificates', e)
  } finally {
    loading.value = false
  }
})

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric'
  })
}

function printCert(certId) {
  const certEl    = document.getElementById(`cert-${certId}`)
  const printArea = document.getElementById('print-area')
  printArea.innerHTML = certEl.outerHTML
  window.print()
  setTimeout(() => { printArea.innerHTML = '' }, 1000)
}

function copyVerifyLink(cert) {
  const url = `${window.location.origin}/verify/${cert.certificateCode}`
  navigator.clipboard.writeText(url).then(() => {
    copied.value = cert.id
    setTimeout(() => { copied.value = null }, 2500)
  })
}
</script>

<style scoped>
.cert-page { min-height: 100vh; background: var(--bg); padding-bottom: 4rem; }

/* ── HEADER ───────────────────────────────────────────── */
.cert-header { background: var(--surface); border-bottom: 1px solid var(--border); padding: 2rem; }
.cert-header-inner { max-width: 1200px; margin: 0 auto; }
.cert-title { font-size: 1.5rem; font-weight: 800; color: var(--text); }
.cert-sub   { font-size: .9rem; color: var(--muted); margin-top: .25rem; }

/* ── LOADING / EMPTY ──────────────────────────────────── */
.cert-loading { display: flex; flex-direction: column; align-items: center; gap: 1rem; padding: 5rem; color: var(--muted); font-size: .9rem; }
.spin { width: 32px; height: 32px; border: 3px solid var(--border); border-top-color: var(--accent); border-radius: 50%; animation: spin .7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.cert-empty { text-align: center; padding: 5rem 2rem; color: var(--muted); }
.empty-icon  { font-size: 4rem; margin-bottom: 1rem; }
.cert-empty h3 { font-size: 1.2rem; color: var(--text); margin-bottom: .5rem; }
.cert-empty p  { font-size: .9rem; margin-bottom: 1.5rem; }
.btn-browse { display: inline-flex; padding: .65rem 1.5rem; background: var(--accent); color: #fff; border-radius: 10px; font-weight: 600; font-size: .88rem; text-decoration: none; transition: all .15s; }
.btn-browse:hover { background: var(--accent-dark); }

/* ── LIST ─────────────────────────────────────────────── */
.cert-list { max-width: 1200px; margin: 2.5rem auto; padding: 0 1rem; display: flex; flex-direction: column; gap: 2.5rem; }
@media(max-width:700px) {
  .cert-header { padding: 1.2rem 1rem; }
  .cert-doc { padding: 10px; }
  .crc-name { font-size: 1.3rem !important; }
  .cert-border-inner { flex-direction: column; }
  .cert-left-accent { flex-direction: row; min-width:unset !important; padding: .8rem !important; }
  .cert-actions { flex-direction: column; }
  .cert-actions button, .cert-actions a { width: 100%; justify-content: center; }
}

/* ── WRAPPER ──────────────────────────────────────────── */
.cert-wrapper { border-radius: 20px; overflow: hidden; box-shadow: 0 8px 40px rgba(0,0,0,.1); }

/* ── CERT DOCUMENT ────────────────────────────────────── */
.cert-doc { background: #fdfcf8; padding: 20px; }
.cert-border-outer { border: 3px solid #1e3a5f; border-radius: 8px; padding: 6px; }
.cert-border-inner { border: 1.5px solid #c8a96e; border-radius: 4px; display: flex; min-height: 300px; }

/* ── LEFT ACCENT ──────────────────────────────────────── */
.cert-left-accent {
  width: 190px; flex-shrink: 0;
  background: linear-gradient(175deg, #1e3a5f 0%, #2563eb 55%, #1e3a5f 100%);
  display: flex; flex-direction: column; align-items: center;
  justify-content: space-between; padding: 1.5rem 1rem;
  position: relative; border-radius: 2px 0 0 2px;
}
.accent-bar {
  position: absolute; right: 0; top: 0; bottom: 0; width: 6px;
  background: linear-gradient(180deg, #c8a96e, #f0d080, #c8a96e);
}
.accent-logo { display: flex; flex-direction: column; align-items: center; gap: .3rem; }
.al-icon { font-size: 1.8rem; }
.al-name { font-size: .95rem; font-weight: 800; color: #fff; letter-spacing: -.02em; }
.accent-label { font-size: .58rem; font-weight: 800; letter-spacing: .13em; color: rgba(255,255,255,.65); text-align: center; line-height: 1.7; }
.seal-circle { width: 68px; height: 68px; border-radius: 50%; border: 2.5px solid #c8a96e; background: rgba(255,255,255,.07); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: .12rem; }
.seal-star { color: #f0d080; font-size: 1.3rem; line-height: 1; }
.seal-text { font-size: .42rem; font-weight: 800; letter-spacing: .12em; color: rgba(255,255,255,.75); }

/* ── RIGHT CONTENT ────────────────────────────────────── */
.cert-right-content {
  flex: 1; padding: 1.8rem 2rem 1.5rem 2rem;
  display: flex; flex-direction: column; justify-content: space-between;
  background: linear-gradient(135deg, #fdfcf8, #f8f6f0);
  border-radius: 0 2px 2px 0;
}
.crc-presented { font-size: .6rem; font-weight: 800; letter-spacing: .2em; color: #9ca3af; text-transform: uppercase; margin-bottom: .5rem; }
.crc-name { font-size: 2.1rem; font-weight: 800; color: #1e3a5f; letter-spacing: -.03em; line-height: 1.1; margin-bottom: .3rem; }
.crc-desc { font-size: .82rem; color: #6b7280; margin-bottom: .3rem; }
.crc-course { font-size: 1.2rem; font-weight: 700; color: #2563eb; letter-spacing: -.02em; }

.crc-divider { display: flex; align-items: center; gap: .8rem; margin: 1rem 0; }
.divider-line { flex: 1; height: 1px; }
.divider-line:first-child { background: linear-gradient(90deg, #c8a96e, transparent); }
.divider-line:last-child  { background: linear-gradient(270deg, #c8a96e, transparent); }
.divider-diamond { color: #c8a96e; font-size: .65rem; flex-shrink: 0; }

.crc-stats { display: flex; gap: 1rem; margin-bottom: 1rem; }
.crc-stat { flex: 1; text-align: center; padding: .6rem .5rem; background: rgba(255,255,255,.85); border: 1px solid #e5e7eb; border-radius: 10px; }
.crc-stat.highlight { background: linear-gradient(135deg, #eff6ff, #dbeafe); border-color: #bfdbfe; }
.cs-num { font-size: 1.05rem; font-weight: 800; color: #1e3a5f; line-height: 1; margin-bottom: .25rem; }
.cs-lbl { font-size: .58rem; color: #9ca3af; text-transform: uppercase; letter-spacing: .07em; line-height: 1.4; }

.crc-footer { display: flex; justify-content: space-between; align-items: flex-end; }
.sig-line { width: 110px; height: 1.5px; background: #1e3a5f; margin-bottom: .3rem; }
.sig-name { font-size: .75rem; font-weight: 700; color: #1e3a5f; }
.sig-title { font-size: .62rem; color: #9ca3af; }
.crc-code-block { text-align: right; }
.cb-label { font-size: .58rem; color: #9ca3af; text-transform: uppercase; letter-spacing: .08em; margin-bottom: .2rem; }
.cb-code  { font-family: monospace; font-size: .65rem; font-weight: 700; color: #374151; letter-spacing: .04em; }

/* ── ACTIONS ──────────────────────────────────────────── */
.cert-actions { display: flex; gap: .75rem; padding: 1rem 1.5rem; background: var(--surface); border-top: 1px solid var(--border); }
.btn-pdf {
  display: inline-flex; align-items: center; gap: .5rem;
  padding: .6rem 1.4rem; border-radius: 10px; border: none;
  background: linear-gradient(135deg, #1e3a5f, #2563eb);
  color: #fff; font-size: .84rem; font-weight: 700;
  cursor: pointer; transition: all .15s; font-family: inherit;
}
.btn-pdf:hover { opacity: .9; transform: translateY(-1px); }
.btn-copy {
  display: inline-flex; align-items: center; gap: .5rem;
  padding: .6rem 1.4rem; border-radius: 10px;
  border: 1.5px solid var(--border); background: var(--surface);
  color: var(--text2); font-size: .84rem; font-weight: 600;
  cursor: pointer; transition: all .15s; font-family: inherit;
}
.btn-copy:hover { border-color: var(--accent); color: var(--accent); }
.btn-copy.copied { border-color: var(--green); color: var(--green); background: var(--green-light); }

/* ── PRINT AREA (hidden normally) ─────────────────────── */
#print-area { display: none; }
</style>

<!-- Print CSS — không scoped để apply toàn bộ page -->
<style>
@media print {
  @page { size: A4 landscape; margin: 8mm; }

  /* Ẩn tất cả mọi thứ */
  body * { visibility: hidden; }

  /* Chỉ hiện print-area */
  #print-area,
  #print-area * { visibility: visible; }

  #print-area {
    display: block !important;
    position: fixed;
    inset: 0;
    z-index: 99999;
    background: white;
    padding: 0;
  }

  #print-area .cert-doc {
    width: 100%;
    height: 100%;
  }

  /* Ẩn actions */
  .cert-actions { display: none !important; }
}
</style>