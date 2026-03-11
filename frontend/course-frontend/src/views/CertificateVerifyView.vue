<template>
  <div class="verify-page">
    <div class="verify-card">

      <!-- LOADING -->
      <div v-if="loading" class="v-loading">
        <div class="spin"></div>
        <span>Đang xác thực...</span>
      </div>

      <!-- INVALID -->
      <div v-else-if="!cert" class="v-invalid">
        <div class="vi-icon">❌</div>
        <h2>Chứng chỉ không hợp lệ</h2>
        <p>Mã chứng chỉ <code>{{ $route.params.code }}</code> không tồn tại.</p>
        <router-link to="/" class="btn-home">Về trang chủ</router-link>
      </div>

      <!-- VALID -->
      <template v-else>
        <div class="v-badge valid">
          <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          Chứng chỉ hợp lệ
        </div>

        <!-- Card landscape giống hệt CertificatesView -->
        <div class="cert-wrapper">
          <div class="cert-doc">
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
        </div>

        <router-link to="/" class="btn-home">Về trang chủ EduFlow</router-link>
      </template>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute }       from 'vue-router'
import api                from '../services/api'

const route   = useRoute()
const cert    = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.get(`/certificates/verify/${route.params.code}`)
    cert.value = res.data
  } catch {
    cert.value = null
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
</script>

<style scoped>
/* ── PAGE ─────────────────────────────────────────────── */
.verify-page {
  min-height: 100vh;
  background: var(--bg);
  display: flex; align-items: center; justify-content: center;
  padding: 2rem;
}
.verify-card {
  width: 100%; max-width: 900px;
  display: flex; flex-direction: column; align-items: center; gap: 1.5rem;
}

/* ── LOADING ──────────────────────────────────────────── */
.v-loading {
  display: flex; flex-direction: column; align-items: center; gap: 1rem;
  color: var(--muted);
}
.spin {
  width: 36px; height: 36px;
  border: 3px solid var(--border); border-top-color: var(--accent);
  border-radius: 50%; animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── VALID BADGE ──────────────────────────────────────── */
.v-badge {
  display: inline-flex; align-items: center; gap: .5rem;
  padding: .45rem 1.1rem; border-radius: 100px;
  font-size: .82rem; font-weight: 700;
}
.v-badge.valid {
  background: var(--green-light); color: var(--green);
  border: 1.5px solid rgba(5,150,105,.25);
}

/* ── INVALID ──────────────────────────────────────────── */
.v-invalid       { text-align: center; }
.vi-icon         { font-size: 3rem; margin-bottom: 1rem; }
.v-invalid h2    { font-size: 1.3rem; font-weight: 800; margin-bottom: .5rem; }
.v-invalid p     { color: var(--muted); font-size: .9rem; margin-bottom: 1.5rem; }
.v-invalid code  { background: var(--surface2); padding: .15rem .4rem; border-radius: 4px; }

/* ── CERT WRAPPER ─────────────────────────────────────── */
.cert-wrapper      { width: 100%; border-radius: 20px; overflow: hidden; box-shadow: 0 8px 40px rgba(0,0,0,.1); }
.cert-doc          { background: #fdfcf8; padding: 20px; }
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
.accent-logo  { display: flex; flex-direction: column; align-items: center; gap: .3rem; }
.al-icon      { font-size: 1.8rem; }
.al-name      { font-size: .95rem; font-weight: 800; color: #fff; letter-spacing: -.02em; }
.accent-label { font-size: .58rem; font-weight: 800; letter-spacing: .13em; color: rgba(255,255,255,.65); text-align: center; line-height: 1.7; }
.seal-circle  { width: 68px; height: 68px; border-radius: 50%; border: 2.5px solid #c8a96e; background: rgba(255,255,255,.07); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: .12rem; }
.seal-star    { color: #f0d080; font-size: 1.3rem; line-height: 1; }
.seal-text    { font-size: .42rem; font-weight: 800; letter-spacing: .12em; color: rgba(255,255,255,.75); }

/* ── RIGHT CONTENT ────────────────────────────────────── */
.cert-right-content {
  flex: 1; padding: 1.8rem 2rem 1.5rem 2rem;
  display: flex; flex-direction: column; justify-content: space-between;
  background: linear-gradient(135deg, #fdfcf8, #f8f6f0);
  border-radius: 0 2px 2px 0;
}
.crc-presented { font-size: .6rem; font-weight: 800; letter-spacing: .2em; color: #9ca3af; text-transform: uppercase; margin-bottom: .5rem; }
.crc-name      { font-size: 2.1rem; font-weight: 800; color: #1e3a5f; letter-spacing: -.03em; line-height: 1.1; margin-bottom: .3rem; }
.crc-desc      { font-size: .82rem; color: #6b7280; margin-bottom: .3rem; }
.crc-course    { font-size: 1.2rem; font-weight: 700; color: #2563eb; letter-spacing: -.02em; }

.crc-divider     { display: flex; align-items: center; gap: .8rem; margin: 1rem 0; }
.divider-line    { flex: 1; height: 1px; }
.divider-line:first-child { background: linear-gradient(90deg,  #c8a96e, transparent); }
.divider-line:last-child  { background: linear-gradient(270deg, #c8a96e, transparent); }
.divider-diamond { color: #c8a96e; font-size: .65rem; flex-shrink: 0; }

.crc-stats           { display: flex; gap: 1rem; margin-bottom: 1rem; }
.crc-stat            { flex: 1; text-align: center; padding: .6rem .5rem; background: rgba(255,255,255,.85); border: 1px solid #e5e7eb; border-radius: 10px; }
.crc-stat.highlight  { background: linear-gradient(135deg, #eff6ff, #dbeafe); border-color: #bfdbfe; }
.cs-num { font-size: 1.05rem; font-weight: 800; color: #1e3a5f; line-height: 1; margin-bottom: .25rem; }
.cs-lbl { font-size: .58rem; color: #9ca3af; text-transform: uppercase; letter-spacing: .07em; line-height: 1.4; }

.crc-footer     { display: flex; justify-content: space-between; align-items: flex-end; }
.sig-line       { width: 110px; height: 1.5px; background: #1e3a5f; margin-bottom: .3rem; }
.sig-name       { font-size: .75rem; font-weight: 700; color: #1e3a5f; }
.sig-title      { font-size: .62rem; color: #9ca3af; }
.crc-code-block { text-align: right; }
.cb-label       { font-size: .58rem; color: #9ca3af; text-transform: uppercase; letter-spacing: .08em; margin-bottom: .2rem; }
.cb-code        { font-family: monospace; font-size: .65rem; font-weight: 700; color: #374151; letter-spacing: .04em; }

/* ── BTN ──────────────────────────────────────────────── */
.btn-home {
  display: inline-flex; align-items: center;
  padding: .6rem 1.4rem; border-radius: 10px;
  background: var(--accent); color: #fff;
  font-size: .88rem; font-weight: 700;
  text-decoration: none; transition: all .15s;
}
.btn-home:hover { background: var(--accent-dark); }

/* ── RESPONSIVE ───────────────────────────────────────── */
@media(max-width: 680px) {
  .verify-page       { padding: 1rem; align-items: flex-start; padding-top: 2rem; }
  .cert-border-inner { flex-direction: column; }
  .cert-left-accent  { width: 100%; flex-direction: row; justify-content: space-around; padding: 1rem; min-height: unset; border-radius: 2px 2px 0 0; }
  .accent-bar        { top: unset; bottom: 0; left: 0; right: 0; width: 100%; height: 5px; }
  .cert-right-content { padding: 1.2rem; }
  .crc-name          { font-size: 1.5rem; }
  .crc-stats         { flex-direction: column; gap: .5rem; }
  .crc-footer        { flex-direction: column; gap: .75rem; align-items: flex-start; }
}
</style>