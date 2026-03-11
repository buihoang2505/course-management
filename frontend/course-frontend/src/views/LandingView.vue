<template>
  <div class="landing">

    <!-- ── NAVBAR ────────────────────────────────────────── -->
    <nav class="land-nav" :class="{ scrolled: scrolled }">
      <div class="land-nav-inner">
        <router-link to="/" class="land-logo">
          <span class="land-logo-icon">🎓</span>
          <span>EduFlow</span>
        </router-link>
        <div class="land-nav-links">
          <a href="#features" @click.prevent="scrollTo('features')">Tính năng</a>
          <a href="#courses"  @click.prevent="scrollTo('courses')">Khóa học</a>
          <a href="#stats"    @click.prevent="scrollTo('stats')">Thống kê</a>
        </div>
        <div class="land-nav-actions">
          <router-link to="/login" class="btn-land-login">Đăng nhập</router-link>
          <router-link to="/register" class="btn-land-register">Bắt đầu miễn phí</router-link>
        </div>
      </div>
    </nav>

    <!-- ── HERO ───────────────────────────────────────────── -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-orb orb1"></div>
        <div class="hero-orb orb2"></div>
        <div class="hero-orb orb3"></div>
        <div class="hero-grid"></div>
      </div>

      <div class="hero-inner">
        <div class="hero-content">
          <div class="hero-badge">
            <span class="badge-dot"></span>
            Nền tảng học tập #1 Việt Nam
          </div>
          <h1 class="hero-title">
            Học <span class="hl-gradient">thông minh</span>,<br>
            Thành công <span class="hl-underline">nhanh hơn</span>
          </h1>
          <p class="hero-sub">
            Khám phá hàng trăm khóa học chất lượng cao từ các chuyên gia hàng đầu.
            Theo dõi tiến độ, làm quiz và nhận chứng chỉ khi hoàn thành.
          </p>
          <div class="hero-cta">
            <router-link to="/register" class="btn-hero-primary">
              <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
              Bắt đầu học ngay
            </router-link>
            <router-link to="/login" class="btn-hero-ghost">
              Đăng nhập
              <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"/></svg>
            </router-link>
          </div>
          <div class="hero-trust">
            <div class="trust-avatars">
              <div class="ta ta1">N</div>
              <div class="ta ta2">M</div>
              <div class="ta ta3">H</div>
              <div class="ta ta4">T</div>
            </div>
            <span class="trust-text"><strong>12,000+</strong> học viên đang học</span>
          </div>
        </div>

        <div class="hero-visual">
          <div class="hv-card hv-main">
            <div class="hvc-header">
              <div class="hvc-dots"><span></span><span></span><span></span></div>
              <span class="hvc-title">Tiến độ học tập</span>
            </div>
            <div class="hvc-body">
              <div class="progress-item" v-for="p in progressItems" :key="p.label">
                <div class="pi-top">
                  <span class="pi-label">{{ p.label }}</span>
                  <span class="pi-pct">{{ p.pct }}%</span>
                </div>
                <div class="pi-bar">
                  <div class="pi-fill" :style="`width:${p.pct}%;background:${p.color}`"></div>
                </div>
              </div>
            </div>
          </div>

          <div class="hv-bottom-row">
            <div class="hv-card hv-badge-card">
              <div class="hbc-icon">🏆</div>
              <div class="hbc-text">
                <div class="hbc-title">Chứng chỉ đạt được</div>
                <div class="hbc-sub">Java cơ bản</div>
              </div>
            </div>

            <div class="hv-card hv-score-card">
              <div class="hsc-score">9.2</div>
              <div class="hsc-label">Điểm quiz</div>
              <div class="hsc-stars">⭐⭐⭐⭐⭐</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Scroll indicator -->
      <div class="scroll-hint" @click="scrollTo('features')">
        <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="6 9 12 15 18 9"/></svg>
      </div>
    </section>

    <!-- ── LOGOS / PARTNERS ──────────────────────────────── -->
    <section class="partners">
      <div class="partners-inner">
        <span class="partners-label">Được tin dùng bởi</span>
        <div class="partners-logos">
          <span v-for="p in partners" :key="p" class="partner-chip">{{ p }}</span>
        </div>
      </div>
    </section>

    <!-- ── STATS ──────────────────────────────────────────── -->
    <section id="stats" class="stats-section">
      <div class="stats-grid">
        <div class="stat-card" v-for="s in stats" :key="s.label">
          <div class="stat-icon">{{ s.icon }}</div>
          <div class="stat-num">{{ s.num }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </section>

    <!-- ── FEATURES ───────────────────────────────────────── -->
    <section id="features" class="features-section">
      <div class="section-inner">
        <div class="section-header">
          <div class="section-tag">Tính năng</div>
          <h2 class="section-title">Mọi thứ bạn cần để<br><span class="hl-gradient">học hiệu quả</span></h2>
          <p class="section-sub">Hệ thống được thiết kế để giúp bạn học tập tập trung, đo lường tiến độ và đạt kết quả tốt nhất.</p>
        </div>

        <div class="features-grid">
          <div class="feat-card" v-for="f in features" :key="f.title" :style="`--fc:${f.color}`">
            <div class="feat-icon">{{ f.icon }}</div>
            <h3 class="feat-title">{{ f.title }}</h3>
            <p class="feat-desc">{{ f.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ── COURSES PREVIEW ───────────────────────────────── -->
    <section id="courses" class="courses-section">
      <div class="section-inner">
        <div class="section-header">
          <div class="section-tag">Khóa học nổi bật</div>
          <h2 class="section-title">Bắt đầu với những<br><span class="hl-gradient">khóa học phổ biến</span></h2>
        </div>

        <div class="courses-grid">
          <div class="course-card" v-for="c in sampleCourses" :key="c.title">
            <div class="cc-thumb" :style="`background:${c.bg}`">
              <span class="cc-emoji">{{ c.emoji }}</span>
            </div>
            <div class="cc-body">
              <div class="cc-tag">{{ c.tag }}</div>
              <h3 class="cc-title">{{ c.title }}</h3>
              <p class="cc-desc">{{ c.desc }}</p>
              <div class="cc-meta">
                <span class="cc-lessons">📚 {{ c.lessons }} bài học</span>
                <span class="cc-credits">{{ c.credits }} tín chỉ</span>
              </div>
            </div>
          </div>
        </div>

        <div class="courses-cta">
          <router-link to="/register" class="btn-hero-primary">
            Xem tất cả khóa học →
          </router-link>
        </div>
      </div>
    </section>

    <!-- ── HOW IT WORKS ──────────────────────────────────── -->
    <section class="how-section">
      <div class="section-inner">
        <div class="section-header">
          <div class="section-tag">Cách hoạt động</div>
          <h2 class="section-title">Chỉ <span class="hl-gradient">3 bước</span> để bắt đầu</h2>
        </div>
        <div class="steps-grid">
          <div class="step-card" v-for="(s, i) in steps" :key="s.title">
            <div class="step-num">{{ i + 1 }}</div>
            <div class="step-icon">{{ s.icon }}</div>
            <h3 class="step-title">{{ s.title }}</h3>
            <p class="step-desc">{{ s.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ── TESTIMONIALS ──────────────────────────────────── -->
    <section class="testimonials-section">
      <div class="section-inner">
        <div class="section-header">
          <div class="section-tag">Học viên nói gì</div>
          <h2 class="section-title">Họ đã <span class="hl-gradient">thành công</span></h2>
        </div>
        <div class="testimonials-grid">
          <div class="testi-card" v-for="t in testimonials" :key="t.name">
            <div class="testi-stars">⭐⭐⭐⭐⭐</div>
            <p class="testi-text">"{{ t.text }}"</p>
            <div class="testi-author">
              <div class="testi-avatar" :style="`background:${t.color}`">{{ t.name[0] }}</div>
              <div>
                <div class="testi-name">{{ t.name }}</div>
                <div class="testi-role">{{ t.role }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── CTA BANNER ────────────────────────────────────── -->
    <section class="cta-section">
      <div class="cta-inner">
        <div class="cta-orb cta-orb1"></div>
        <div class="cta-orb cta-orb2"></div>
        <h2 class="cta-title">Sẵn sàng bắt đầu<br>hành trình học tập?</h2>
        <p class="cta-sub">Tham gia cùng 12,000+ học viên đang học mỗi ngày trên EduFlow</p>
        <router-link to="/register" class="btn-cta">
          Đăng ký miễn phí ngay
          <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="9 18 15 12 9 6"/></svg>
        </router-link>
      </div>
    </section>

    <!-- ── FOOTER ────────────────────────────────────────── -->
    <footer class="land-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="land-logo" style="font-size:1.1rem">
            <span>🎓</span><span>EduFlow</span>
          </div>
          <p class="footer-tagline">Nền tảng học tập thông minh cho mọi người.</p>
        </div>
        <div class="footer-links">
          <div class="fl-col">
            <div class="fl-title">Khóa học</div>
            <span>Lập trình Java</span>
            <span>Web Development</span>
            <span>Data Science</span>
            <span>DevOps</span>
          </div>
          <div class="fl-col">
            <div class="fl-title">Công ty</div>
            <span>Về chúng tôi</span>
            <span>Liên hệ</span>
            <span>Tuyển dụng</span>
            <span>Blog</span>
          </div>
          <div class="fl-col">
            <div class="fl-title">Hỗ trợ</div>
            <span>FAQ</span>
            <span>Chính sách</span>
            <span>Điều khoản</span>
            <span>Bảo mật</span>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2026 EduFlow. All rights reserved.</span>
        <div class="footer-socials">
          <a href="#" class="social-btn" title="Facebook">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/></svg>
          </a>
          <a href="#" class="social-btn" title="Zalo">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15v-4H7l5-8v4h4l-5 8z"/></svg>
          </a>
          <a href="#" class="social-btn" title="YouTube">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M22.54 6.42a2.78 2.78 0 0 0-1.95-1.96C18.88 4 12 4 12 4s-6.88 0-8.59.46a2.78 2.78 0 0 0-1.95 1.96A29 29 0 0 0 1 12a29 29 0 0 0 .46 5.58A2.78 2.78 0 0 0 3.41 19.6C5.12 20 12 20 12 20s6.88 0 8.59-.46a2.78 2.78 0 0 0 1.95-1.95A29 29 0 0 0 23 12a29 29 0 0 0-.46-5.58zM9.75 15.02V8.98L15.5 12l-5.75 3.02z"/></svg>
          </a>
        </div>
      </div>
    </footer>

    <!-- ── CHAT BUBBLE ───────────────────────────────────── -->
    <div class="chat-bubbles">
      <div class="chat-bubble-menu" v-if="showChatMenu">
        <a href="https://zalo.me/your-zalo" target="_blank" class="cb-item cb-zalo" title="Zalo">
          <svg width="20" height="20" viewBox="0 0 50 50" fill="currentColor"><text y="38" font-size="38">💬</text></svg>
          <span>Zalo</span>
        </a>
        <a href="https://m.me/your-facebook" target="_blank" class="cb-item cb-fb" title="Messenger">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.26 2 11.5c0 2.94 1.4 5.56 3.6 7.37V22l3.3-1.82c.88.25 1.82.38 2.8.38 5.52 0 10-4.26 10-9.5S17.52 2 12 2zm1.1 12.81l-2.54-2.71-4.96 2.71 5.46-5.81 2.6 2.71 4.9-2.71-5.46 5.81z"/></svg>
          <span>Messenger</span>
        </a>
        <a href="mailto:support@eduflow.vn" class="cb-item cb-email" title="Email">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
          <span>Email</span>
        </a>
      </div>
      <button class="chat-main-btn" @click="showChatMenu = !showChatMenu" :class="{ open: showChatMenu }">
        <svg v-if="!showChatMenu" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const scrolled     = ref(false)
const showChatMenu = ref(false)

const progressItems = [
  { label: 'Java cơ bản', pct: 85, color: '#2563eb' },
  { label: 'OOP nâng cao', pct: 60, color: '#7c3aed' },
  { label: 'Spring Boot', pct: 30, color: '#059669' },
]

const partners = ['FPT Software', 'VNG', 'Tiki', 'Momo', 'VinAI', 'Shopee']

const stats = [
  { icon: '📚', num: '500+',  label: 'Khóa học' },
  { icon: '👨‍🎓', num: '12K+', label: 'Học viên' },
  { icon: '🏆', num: '98%',  label: 'Hài lòng' },
  { icon: '👨‍🏫', num: '50+',  label: 'Giảng viên' },
]

const features = [
  { icon: '🎯', title: 'Học theo lộ trình', desc: 'Các khóa học được sắp xếp theo thứ tự từ cơ bản đến nâng cao, phù hợp với mọi trình độ.', color: '#2563eb' },
  { icon: '📊', title: 'Theo dõi tiến độ', desc: 'Xem tiến độ học tập theo thời gian thực, biết mình đang ở đâu và cần làm gì tiếp theo.', color: '#7c3aed' },
  { icon: '🧩', title: 'Quiz tương tác', desc: 'Kiểm tra kiến thức sau mỗi bài học với hệ thống quiz thông minh, tự động chấm điểm.', color: '#059669' },
  { icon: '🏅', title: 'Chứng chỉ số', desc: 'Nhận chứng chỉ hoàn thành khóa học được xác nhận và chia sẻ trên LinkedIn, CV.', color: '#d97706' },
  { icon: '📱', title: 'Học mọi nơi', desc: 'Giao diện responsive, học trên máy tính, tablet hay điện thoại đều mượt mà.', color: '#dc2626' },
  { icon: '🔔', title: 'Thông báo thông minh', desc: 'Nhận nhắc nhở học tập, cập nhật khóa học và thông báo kết quả quiz tức thì.', color: '#0891b2' },
]

const sampleCourses = [
  { emoji: '☕', bg: 'linear-gradient(135deg,#f97316,#ea580c)', tag: 'Lập trình', title: 'Java cơ bản và OOP', desc: 'Nền tảng Java từ zero, hiểu rõ OOP với ví dụ thực tế.', lessons: 7, credits: 3 },
  { emoji: '🌐', bg: 'linear-gradient(135deg,#3b82f6,#2563eb)', tag: 'Web', title: 'Spring Boot REST API', desc: 'Xây dựng REST API chuyên nghiệp với Spring Boot & JPA.', lessons: 12, credits: 4 },
  { emoji: '⚛️', bg: 'linear-gradient(135deg,#06b6d4,#0284c7)', tag: 'Frontend', title: 'Vue.js từ A đến Z', desc: 'Học Vue 3 Composition API, Pinia, Vue Router thực chiến.', lessons: 10, credits: 3 },
  { emoji: '🗄️', bg: 'linear-gradient(135deg,#8b5cf6,#7c3aed)', tag: 'Database', title: 'MySQL nâng cao', desc: 'Query tối ưu, indexing, transaction và stored procedures.', lessons: 8, credits: 2 },
  { emoji: '🐳', bg: 'linear-gradient(135deg,#10b981,#059669)', tag: 'DevOps', title: 'Docker & Kubernetes', desc: 'Container hóa ứng dụng và orchestration với K8s.', lessons: 9, credits: 3 },
  { emoji: '🤖', bg: 'linear-gradient(135deg,#f59e0b,#d97706)', tag: 'AI/ML', title: 'Python cho Data Science', desc: 'Pandas, NumPy, Matplotlib và Machine Learning cơ bản.', lessons: 15, credits: 4 },
]

const steps = [
  { icon: '📝', title: 'Tạo tài khoản', desc: 'Đăng ký miễn phí trong vòng 30 giây, không cần thẻ tín dụng.' },
  { icon: '🎓', title: 'Chọn khóa học', desc: 'Duyệt qua hàng trăm khóa học và chọn lộ trình phù hợp với mục tiêu của bạn.' },
  { icon: '🏆', title: 'Học & Nhận chứng chỉ', desc: 'Hoàn thành bài học, làm quiz và nhận chứng chỉ được công nhận.' },
]

const testimonials = [
  { name: 'Nguyễn Minh', role: 'Backend Developer @ FPT', text: 'EduFlow giúp tôi hệ thống lại kiến thức Java và tìm được công việc mơ ước chỉ sau 3 tháng học.', color: '#2563eb' },
  { name: 'Trần Hoa', role: 'Sinh viên CNTT', text: 'Giao diện đẹp, dễ dùng, quiz sau mỗi bài giúp tôi nhớ kiến thức lâu hơn rất nhiều.', color: '#7c3aed' },
  { name: 'Lê Hoàng', role: 'Fullstack Dev @ Tiki', text: 'Tôi đã hoàn thành 5 khóa học trên EduFlow. Chất lượng content rất cao và giảng viên rất nhiệt tình.', color: '#059669' },
]

function scrollTo(id) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}

function onScroll() {
  scrolled.value = window.scrollY > 50
}

onMounted(()   => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

/* ── BASE ─────────────────────────────────────────────── */
.landing {
  font-family: 'Plus Jakarta Sans', sans-serif;
  background: #050b18;
  color: #e2e8f0;
  overflow-x: hidden;
  width: 100%;
  min-height: 100vh;
  position: relative;
}
/* ── NAVBAR ───────────────────────────────────────────── */
.land-nav {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  padding: 1.1rem 0;
  transition: all .3s;
}
.land-nav.scrolled {
  background: rgba(5,11,24,.92);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255,255,255,.07);
  padding: .7rem 0;
  box-shadow: 0 4px 30px rgba(0,0,0,.3);
}
.land-nav-inner {
  max-width: 1200px; margin: 0 auto;
  padding: 0 2rem;
  display: flex; align-items: center; gap: 2rem;
}
.land-logo {
  display: flex; align-items: center; gap: .5rem;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-weight: 800; font-size: 1.2rem;
  color: #fff; text-decoration: none;
  white-space: nowrap;
}
.land-logo-icon { font-size: 1.4rem; }
.land-nav-links {
  display: flex; gap: .3rem; flex: 1;
}
.land-nav-links a {
  color: rgba(255,255,255,.6);
  text-decoration: none; font-size: .88rem; font-weight: 500;
  padding: .4rem .9rem; border-radius: 8px;
  transition: all .15s;
}
.land-nav-links a:hover { color: #fff; background: rgba(255,255,255,.08); }
.land-nav-actions { display: flex; gap: .6rem; margin-left: auto; }
.btn-land-login {
  padding: .45rem 1.1rem; border-radius: 9px;
  border: 1px solid rgba(255,255,255,.2);
  color: rgba(255,255,255,.8); background: none;
  font-size: .85rem; font-weight: 600; text-decoration: none;
  transition: all .15s;
}
.btn-land-login:hover { border-color: rgba(255,255,255,.5); color: #fff; }
.btn-land-register {
  padding: .45rem 1.2rem; border-radius: 9px;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff; font-size: .85rem; font-weight: 700;
  text-decoration: none; transition: all .2s;
  box-shadow: 0 2px 12px rgba(37,99,235,.4);
}
.btn-land-register:hover { transform: translateY(-1px); box-shadow: 0 4px 20px rgba(37,99,235,.5); }

/* ── HERO ─────────────────────────────────────────────── */
.hero {
  min-height: 100vh;
  width:100%;
  display: flex; align-items: center;
  position: relative; overflow: hidden;
  padding: 8rem 2rem 6rem;
  justify-content:center;
}
.hero-bg {
  position: absolute; inset: 0; z-index: 0;
}
.hero-orb {
  position: absolute; border-radius: 50%;
  filter: blur(80px); opacity: .35;
}
.orb1 { width:600px; height:600px; background:#2563eb; top:-200px; right:-100px; animation: orb-float 8s ease-in-out infinite; }
.orb2 { width:400px; height:400px; background:#7c3aed; bottom:-100px; left:-100px; animation: orb-float 10s ease-in-out infinite reverse; }
.orb3 { width:300px; height:300px; background:#059669; top:50%; left:40%; animation: orb-float 12s ease-in-out infinite 2s; }
@keyframes orb-float { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-30px)} }

.hero-grid {
  position: absolute; inset: 0;
  background-image: linear-gradient(rgba(255,255,255,.03) 1px, transparent 1px),
  linear-gradient(90deg, rgba(255,255,255,.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

.hero-inner {
  position: relative; z-index: 1;
  max-width: 1200px; margin: 0 auto;
  display: grid; grid-template-columns: 1fr 1fr;
  gap: 4rem; align-items: center; width: 100%;
}

.hero-badge {
  display: inline-flex; align-items: center; gap: .5rem;
  background: rgba(37,99,235,.2); border: 1px solid rgba(37,99,235,.4);
  color: #93c5fd; font-size: .78rem; font-weight: 600;
  padding: .35rem .85rem; border-radius: 100px; margin-bottom: 1.5rem;
}
.badge-dot { width: 7px; height: 7px; border-radius: 50%; background: #60a5fa; animation: pulse-dot 2s ease-in-out infinite; }
@keyframes pulse-dot { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:.5;transform:scale(.8)} }

.hero-title {
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 3.6rem; font-weight: 800; line-height: 1.1;
  color: #fff; margin-bottom: 1.2rem;
}
.hl-gradient {
  background: linear-gradient(135deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.hl-underline {
  position: relative;
  background: linear-gradient(135deg, #34d399, #10b981);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}

.hero-sub {
  color: rgba(255,255,255,.6); font-size: 1.05rem; line-height: 1.7;
  margin-bottom: 2rem; max-width: 500px;
}
.hero-cta { display: flex; gap: .9rem; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; }
.btn-hero-primary {
  display: inline-flex; align-items: center; gap: .5rem;
  padding: .75rem 1.6rem; border-radius: 12px;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff; font-weight: 700; font-size: .95rem;
  text-decoration: none; transition: all .2s;
  box-shadow: 0 4px 20px rgba(37,99,235,.4);
}
.btn-hero-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 30px rgba(37,99,235,.5); }
.btn-hero-ghost {
  display: inline-flex; align-items: center; gap: .4rem;
  padding: .75rem 1.3rem; border-radius: 12px;
  border: 1px solid rgba(255,255,255,.2);
  color: rgba(255,255,255,.8); font-weight: 600; font-size: .95rem;
  text-decoration: none; transition: all .15s;
}
.btn-hero-ghost:hover { border-color: rgba(255,255,255,.5); color: #fff; background: rgba(255,255,255,.05); }

.hero-trust { display: flex; align-items: center; gap: .75rem; }
.trust-avatars { display: flex; }
.ta {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: .75rem; font-weight: 700; color: #fff;
  border: 2px solid #050b18; margin-right: -8px;
}
.ta1 { background: #2563eb; }
.ta2 { background: #7c3aed; }
.ta3 { background: #059669; }
.ta4 { background: #d97706; }
.trust-text { font-size: .82rem; color: rgba(255,255,255,.6); margin-left: 16px; }
.trust-text strong { color: #fff; }

/* ── HERO VISUAL ─────────────────────────────────────── */
@keyframes float-card { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-10px)} }

.hero-visual {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  justify-content: center;
}

/* Base card */
.hv-card {
  background: rgba(255,255,255,.06);
  backdrop-filter: blur(24px);
  border: 1px solid rgba(255,255,255,.12);
  border-radius: 20px;
  padding: 1.25rem;
  box-shadow: 0 16px 48px rgba(0,0,0,.35);
}

/* Card 1: Tiến độ học tập */
.hv-main { animation: float-card 6s ease-in-out infinite; }

/* Row 2: Chứng chỉ + Điểm quiz cạnh nhau */
.hv-bottom-row {
  display: flex;
  gap: 1rem;
}

/* Card 2: Chứng chỉ */
.hv-badge-card {
  display: flex;
  align-items: center;
  gap: .85rem;
  flex: 1;
  animation: float-card 6s ease-in-out infinite 1s;
}
.hbc-icon  { font-size: 2rem; flex-shrink: 0; }
.hbc-title { font-size: .82rem; font-weight: 700; color: #fff; line-height: 1.3; }
.hbc-sub   { font-size: .72rem; color: rgba(255,255,255,.5); margin-top: .15rem; }

/* Card 3: Điểm quiz */
.hv-score-card {
  text-align: center;
  flex-shrink: 0;
  min-width: 115px;
  padding: 1.1rem 1.25rem;
  animation: float-card 6s ease-in-out infinite .5s;
}
.hsc-score { font-size: 2.2rem; font-weight: 800; color: #34d399; line-height: 1; }
.hsc-label { font-size: .72rem; color: rgba(255,255,255,.5); margin: .3rem 0 .2rem; }
.hsc-stars { font-size: .72rem; }

/* Progress */
.hvc-header { display: flex; align-items: center; gap: .6rem; margin-bottom: 1rem; }
.hvc-dots { display: flex; gap: .3rem; }
.hvc-dots span { width: 10px; height: 10px; border-radius: 50%; background: rgba(255,255,255,.2); }
.hvc-dots span:first-child { background: #f87171; }
.hvc-dots span:nth-child(2) { background: #fbbf24; }
.hvc-dots span:last-child   { background: #34d399; }
.hvc-title { font-size: .8rem; color: rgba(255,255,255,.5); font-weight: 600; }

.progress-item { margin-bottom: .85rem; }
.progress-item:last-child { margin-bottom: 0; }
.pi-top   { display: flex; justify-content: space-between; margin-bottom: .3rem; }
.pi-label { font-size: .78rem; color: rgba(255,255,255,.7); }
.pi-pct   { font-size: .78rem; font-weight: 700; color: #fff; }
.pi-bar   { height: 6px; background: rgba(255,255,255,.1); border-radius: 100px; overflow: hidden; }
.pi-fill  { height: 100%; border-radius: 100px; transition: width 1s ease; }

.scroll-hint {
  position: absolute; bottom: 2rem; left: 50%; transform: translateX(-50%);
  color: rgba(255,255,255,.3); cursor: pointer;
  animation: bounce 2s ease-in-out infinite;
}
@keyframes bounce { 0%,100%{transform:translateX(-50%) translateY(0)} 50%{transform:translateX(-50%) translateY(8px)} }

/* ── PARTNERS ─────────────────────────────────────────── */
.partners {
  padding: 2rem 2rem;
  border-top: 1px solid rgba(255,255,255,.06);
  border-bottom: 1px solid rgba(255,255,255,.06);
  background: rgba(255,255,255,.02);
}
.partners-inner {
  max-width: 1200px; margin: 0 auto;
  display: flex; align-items: center; gap: 2rem; flex-wrap: wrap;
}
.partners-label { font-size: .78rem; color: rgba(255,255,255,.35); white-space: nowrap; flex-shrink: 0; }
.partners-logos { display: flex; gap: .75rem; flex-wrap: wrap; }
.partner-chip {
  font-size: .8rem; font-weight: 600;
  color: rgba(255,255,255,.4); padding: .35rem .9rem;
  border: 1px solid rgba(255,255,255,.08);
  border-radius: 100px; transition: all .15s;
}
.partner-chip:hover { color: rgba(255,255,255,.7); border-color: rgba(255,255,255,.2); }

/* ── STATS ────────────────────────────────────────────── */
.stats-section {
  padding: 5rem 2rem;
  background: linear-gradient(135deg, rgba(37,99,235,.08), rgba(124,58,237,.08));
}
.stats-grid {
  max-width: 900px; margin: 0 auto;
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.5rem;
}
.stat-card {
  text-align: center; padding: 2rem 1rem;
  background: rgba(255,255,255,.04);
  border: 1px solid rgba(255,255,255,.08);
  border-radius: 16px; transition: all .2s;
}
.stat-card:hover { background: rgba(255,255,255,.07); transform: translateY(-4px); }
.stat-icon { font-size: 2rem; margin-bottom: .5rem; }
.stat-num { font-family: 'Plus Jakarta Sans', sans-serif; font-size: 2.2rem; font-weight: 800; color: #fff; }
.stat-label { font-size: .82rem; color: rgba(255,255,255,.5); margin-top: .25rem; }

/* ── SECTIONS COMMON ──────────────────────────────────── */
.section-inner { max-width: 1200px; margin: 0 auto; padding: 0 2rem; }
.section-header { text-align: center; margin-bottom: 3.5rem; }
.section-tag {
  display: inline-block;
  font-size: .75rem; font-weight: 700; letter-spacing: .1em; text-transform: uppercase;
  color: #60a5fa; background: rgba(37,99,235,.15);
  border: 1px solid rgba(37,99,235,.3);
  padding: .3rem .85rem; border-radius: 100px; margin-bottom: 1rem;
}
.section-title {
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 2.6rem; font-weight: 800; line-height: 1.15;
  color: #fff; margin-bottom: .75rem;
}
.section-sub { font-size: .95rem; color: rgba(255,255,255,.5); max-width: 520px; margin: 0 auto; line-height: 1.7; }

/* ── FEATURES ─────────────────────────────────────────── */
.features-section { padding: 6rem 2rem; }
.features-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem;
}
.feat-card {
  padding: 1.8rem; border-radius: 18px;
  background: rgba(255,255,255,.03);
  border: 1px solid rgba(255,255,255,.07);
  transition: all .25s; cursor: default;
}
.feat-card:hover {
  background: rgba(255,255,255,.06);
  border-color: var(--fc, #2563eb);
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0,0,0,.2);
}
.feat-icon { font-size: 2.2rem; margin-bottom: 1rem; }
.feat-title { font-family: 'Plus Jakarta Sans', sans-serif; font-size: 1rem; font-weight: 700; color: #fff; margin-bottom: .5rem; }
.feat-desc { font-size: .84rem; color: rgba(255,255,255,.5); line-height: 1.65; }

/* ── COURSES ──────────────────────────────────────────── */
.courses-section { padding: 6rem 2rem; background: rgba(255,255,255,.02); }
.courses-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; margin-bottom: 2.5rem;
}
.course-card {
  border-radius: 18px; overflow: hidden;
  background: rgba(255,255,255,.04);
  border: 1px solid rgba(255,255,255,.08);
  transition: all .25s;
}
.course-card:hover { transform: translateY(-6px); box-shadow: 0 20px 50px rgba(0,0,0,.3); border-color: rgba(255,255,255,.15); }
.cc-thumb {
  height: 140px; display: flex; align-items: center; justify-content: center;
}
.cc-emoji { font-size: 3.5rem; filter: drop-shadow(0 4px 8px rgba(0,0,0,.3)); }
.cc-body { padding: 1.25rem; }
.cc-tag { font-size: .68rem; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; color: #60a5fa; margin-bottom: .5rem; }
.cc-title { font-family: 'Plus Jakarta Sans', sans-serif; font-size: .95rem; font-weight: 700; color: #fff; margin-bottom: .4rem; }
.cc-desc { font-size: .78rem; color: rgba(255,255,255,.5); line-height: 1.55; margin-bottom: .85rem; }
.cc-meta { display: flex; justify-content: space-between; font-size: .74rem; color: rgba(255,255,255,.4); }
.cc-credits { font-weight: 600; color: #a78bfa; }
.courses-cta { text-align: center; }

/* ── HOW IT WORKS ─────────────────────────────────────── */
.how-section { padding: 6rem 2rem; }
.steps-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 2rem; position: relative; }
.steps-grid::before {
  content: '';
  position: absolute; top: 35px; left: 15%; right: 15%; height: 2px;
  background: linear-gradient(90deg, #2563eb, #7c3aed);
  opacity: .3;
}
.step-card {
  text-align: center; padding: 2rem 1.5rem;
  background: rgba(255,255,255,.03);
  border: 1px solid rgba(255,255,255,.07);
  border-radius: 18px;
}
.step-num {
  width: 40px; height: 40px; border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff; font-weight: 800; font-size: .95rem;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 1rem;
}
.step-icon { font-size: 2rem; margin-bottom: .75rem; }
.step-title { font-family: 'Plus Jakarta Sans', sans-serif; font-size: 1rem; font-weight: 700; color: #fff; margin-bottom: .5rem; }
.step-desc { font-size: .82rem; color: rgba(255,255,255,.5); line-height: 1.65; }

/* ── TESTIMONIALS ─────────────────────────────────────── */
.testimonials-section { padding: 6rem 2rem; background: rgba(255,255,255,.02); }
.testimonials-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
.testi-card {
  padding: 1.8rem;
  background: rgba(255,255,255,.04);
  border: 1px solid rgba(255,255,255,.08);
  border-radius: 18px; transition: all .2s;
}
.testi-card:hover { background: rgba(255,255,255,.06); transform: translateY(-4px); }
.testi-stars { font-size: .85rem; margin-bottom: .85rem; }
.testi-text { font-size: .88rem; color: rgba(255,255,255,.7); line-height: 1.7; margin-bottom: 1.25rem; font-style: italic; }
.testi-author { display: flex; align-items: center; gap: .75rem; }
.testi-avatar {
  width: 38px; height: 38px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; color: #fff; font-size: .9rem; flex-shrink: 0;
}
.testi-name { font-size: .85rem; font-weight: 700; color: #fff; }
.testi-role { font-size: .74rem; color: rgba(255,255,255,.45); margin-top: .1rem; }

/* ── CTA BANNER ───────────────────────────────────────── */
.cta-section {
  padding: 6rem 2rem; position: relative; overflow: hidden;
  background: linear-gradient(135deg, rgba(37,99,235,.15), rgba(124,58,237,.15));
  border-top: 1px solid rgba(255,255,255,.08);
  border-bottom: 1px solid rgba(255,255,255,.08);
}
.cta-inner { position: relative; z-index: 1; text-align: center; max-width: 600px; margin: 0 auto; }
.cta-orb {
  position: absolute; border-radius: 50%; filter: blur(60px); opacity: .2;
}
.cta-orb1 { width: 400px; height: 400px; background: #2563eb; top: -100px; left: -100px; }
.cta-orb2 { width: 300px; height: 300px; background: #7c3aed; bottom: -100px; right: -50px; }
.cta-title { font-family: 'Plus Jakarta Sans', sans-serif; font-size: 2.8rem; font-weight: 800; color: #fff; line-height: 1.2; margin-bottom: 1rem; }
.cta-sub { font-size: .95rem; color: rgba(255,255,255,.55); margin-bottom: 2rem; line-height: 1.7; }
.btn-cta {
  display: inline-flex; align-items: center; gap: .5rem;
  padding: .9rem 2.2rem; border-radius: 14px;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff; font-weight: 700; font-size: 1rem;
  text-decoration: none; transition: all .2s;
  box-shadow: 0 4px 24px rgba(37,99,235,.4);
}
.btn-cta:hover { transform: translateY(-2px); box-shadow: 0 8px 36px rgba(37,99,235,.5); }

/* ── FOOTER ───────────────────────────────────────────── */
.land-footer {
  background: rgba(0,0,0,.4); padding: 4rem 2rem 2rem;
  border-top: 1px solid rgba(255,255,255,.06);
}
.footer-inner {
  max-width: 1200px; margin: 0 auto;
  display: grid; grid-template-columns: 1.5fr 2fr; gap: 4rem;
  margin-bottom: 3rem;
}
.footer-tagline { font-size: .84rem; color: rgba(255,255,255,.4); margin-top: .6rem; line-height: 1.6; }
.footer-links { display: grid; grid-template-columns: repeat(3, 1fr); gap: 2rem; }
.fl-col { display: flex; flex-direction: column; gap: .5rem; }
.fl-title { font-size: .8rem; font-weight: 700; color: rgba(255,255,255,.7); margin-bottom: .35rem; text-transform: uppercase; letter-spacing: .07em; }
.fl-col span { font-size: .82rem; color: rgba(255,255,255,.4); cursor: pointer; transition: color .15s; }
.fl-col span:hover { color: rgba(255,255,255,.8); }
.footer-bottom {
  max-width: 1200px; margin: 0 auto;
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 1.5rem; border-top: 1px solid rgba(255,255,255,.06);
  font-size: .8rem; color: rgba(255,255,255,.3);
}
.footer-socials { display: flex; gap: .5rem; }
.social-btn {
  width: 34px; height: 34px; border-radius: 8px;
  background: rgba(255,255,255,.06); border: 1px solid rgba(255,255,255,.1);
  color: rgba(255,255,255,.5); display: flex; align-items: center; justify-content: center;
  text-decoration: none; transition: all .15s;
}
.social-btn:hover { background: rgba(255,255,255,.12); color: #fff; }

/* ── CHAT BUBBLES ─────────────────────────────────────── */
.chat-bubbles {
  position: fixed; bottom: 2rem; right: 2rem; z-index: 999;
  display: flex; flex-direction: column; align-items: flex-end; gap: .6rem;
}
.chat-bubble-menu {
  display: flex; flex-direction: column; gap: .5rem; align-items: flex-end;
  animation: slideUpFade .25s ease;
}
@keyframes slideUpFade { from{opacity:0;transform:translateY(10px)} to{opacity:1;transform:translateY(0)} }
.cb-item {
  display: flex; align-items: center; gap: .6rem;
  padding: .55rem 1rem .55rem .7rem; border-radius: 100px;
  color: #fff; text-decoration: none; font-size: .82rem; font-weight: 600;
  transition: all .15s; box-shadow: 0 4px 16px rgba(0,0,0,.3);
}
.cb-item:hover { transform: translateX(-4px); }
.cb-zalo    { background: #0068ff; }
.cb-fb      { background: #0084ff; }
.cb-email   { background: #059669; }
.chat-main-btn {
  width: 54px; height: 54px; border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  border: none; color: #fff; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 20px rgba(37,99,235,.5); transition: all .2s;
}
.chat-main-btn:hover { transform: scale(1.08); box-shadow: 0 6px 28px rgba(37,99,235,.6); }
.chat-main-btn.open { background: linear-gradient(135deg, #ef4444, #dc2626); }

/* ── RESPONSIVE ───────────────────────────────────────── */
@media (max-width: 900px) {
  .hero-inner { grid-template-columns: 1fr; }
  .hero-visual { display: none; }
  .hero-title { font-size: 2.6rem; }
  .features-grid, .courses-grid, .testimonials-grid, .steps-grid { grid-template-columns: 1fr 1fr; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .footer-inner { grid-template-columns: 1fr; gap: 2rem; }
  .steps-grid::before { display: none; }
}
@media (max-width: 600px) {
  .features-grid, .courses-grid, .testimonials-grid, .steps-grid { grid-template-columns: 1fr; }
  .land-nav-links { display: none; }
  .hero-title { font-size: 2rem; }
  .section-title { font-size: 1.9rem; }
  .cta-title { font-size: 2rem; }
}

@media (max-width: 480px) {
  .hero { padding: 6rem 1rem 4rem; }
  .hero-title { font-size: 1.85rem; }
  .hero-cta { flex-direction: column; }
  .btn-hero-primary, .btn-hero-ghost { width: 100%; justify-content: center; }
  .land-nav-actions .btn-land-register { display: none; }
  .section-title { font-size: 1.6rem; }
  .cta-title { font-size: 1.7rem; }
  .footer-links { grid-template-columns: 1fr 1fr; }
  .footer-bottom { flex-direction: column; gap: .75rem; text-align: center; }
  .land-nav-inner { padding: 0 1rem; }
}
</style>