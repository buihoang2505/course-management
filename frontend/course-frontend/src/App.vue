<template>
  <div id="app">
    <nav v-if="auth.isLoggedIn && !isLanding" class="navbar">
      <router-link to="/courses" class="nav-brand">
        <span class="brand-icon">🎓</span>
        <span>EduFlow</span>
      </router-link>

      <button class="hamburger" @click="mobileMenuOpen = !mobileMenuOpen" :class="{open: mobileMenuOpen}" aria-label="Menu">
        <span></span><span></span><span></span>
      </button>
      <div class="nav-links" :class="{'mobile-open': mobileMenuOpen}" @click="mobileMenuOpen=false">
        <!-- Student links (không phải admin, không phải instructor) -->
        <template v-if="!auth.isAdmin && auth.user?.role !== 'INSTRUCTOR'">
          <router-link to="/courses" class="nav-item">
            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
            Khóa học
          </router-link>
          <router-link to="/my-courses" class="nav-item">
            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg>
            Của tôi
          </router-link>
          <router-link to="/grades" class="nav-item">
            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            Điểm số
          </router-link>
          <router-link to="/certificates" class="nav-item">
            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="8" r="6"/><path d="M15.477 12.89L17 22l-5-3-5 3 1.523-9.11"/></svg>
            Chứng chỉ
          </router-link>
          <router-link to="/leaderboard" class="nav-item">
            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="18 20 18 10"/><polyline points="12 20 12 4"/><polyline points="6 20 6 14"/></svg>
            Xếp hạng
          </router-link>
        </template>


        <!-- Instructor link -->
        <router-link v-if="auth.user?.role === 'INSTRUCTOR'" to="/instructor" class="nav-item instructor-link">
          <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          Giảng viên
        </router-link>

        <!-- Admin link -->
        <router-link v-if="auth.isAdmin" to="/admin" class="nav-item admin-link">
          <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
          Quản lý
        </router-link>
      </div>

      <div class="nav-right">
        <!-- Notification Bell -->
        <div class="notif-wrap" ref="notifRef">
          <button class="notif-btn" @click="toggleNotif" :class="{ active: showNotif }" title="Thông báo">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
          </button>

          <Transition name="dropdown">
            <div v-if="showNotif" class="notif-dropdown">
              <div class="notif-hd">
                <div class="notif-hd-left">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
                  <span class="notif-hd-title">Thông báo</span>
                  <span v-if="unreadCount>0" class="notif-count-badge">{{ unreadCount }}</span>
                </div>
                <div class="notif-hd-actions">
                  <button v-if="unreadCount>0" @click="markAllRead" class="notif-read-all">Đã đọc tất cả</button>
                  <button v-if="notifications.length>0" @click="clearAll" class="notif-clear-all" title="Xóa tất cả">
                    <svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                    Xóa hết
                  </button>
                </div>
              </div>

              <div v-if="loadingNotif" class="notif-loading">
                <div class="spinner-sm"></div>
              </div>
              <div v-else-if="!notifications.length" class="notif-empty">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="opacity:.3"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
                <p>Chưa có thông báo nào</p>
              </div>
              <div v-else class="notif-list">
                <div v-for="n in notifications" :key="n.id"
                     :class="['notif-item', { unread: !n.read }]"
                     @click="clickNotif(n)">
                  <div :class="['notif-type-icon', `ni-${n.type?.toLowerCase()}`]">
                    {{ notifIcon(n.type) }}
                  </div>
                  <div class="notif-body">
                    <div class="notif-title">{{ n.title }}</div>
                    <div class="notif-msg">{{ n.message }}</div>
                    <div class="notif-time">{{ timeAgo(n.createdAt) }}</div>
                  </div>
                  <div v-if="!n.read" class="unread-dot"></div>
                </div>
              </div>
            </div>
          </Transition>
        </div>

        <!-- User -->
        <router-link to="/profile" class="nav-user-btn">
          <div class="nav-avatar">
            <img v-if="auth.user?.avatar" :src="auth.user.avatar" class="nav-av-img" alt="avatar"/>
            <span v-else>{{ initials }}</span>
          </div>
          <div class="nav-user-text">
            <span class="nav-uname">{{ auth.user?.fullName || auth.user?.username }}</span>
            <span class="nav-role">{{ auth.user?.role }}</span>
          </div>
        </router-link>

        <!-- Logout button -->
        <button @click="handleLogout" class="btn-logout" title="Đăng xuất">
          <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
      </div>
    </nav>

    <main :class="{ 'with-nav': auth.isLoggedIn && !isLanding, 'is-landing': isLanding }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useRouter, useRoute } from 'vue-router'
import api from './services/api'

const auth   = useAuthStore()
const router = useRouter()
const route  = useRoute()

const isLanding = computed(() => route.meta?.landing === true)

const initials = computed(() => (auth.user?.username || 'U').charAt(0).toUpperCase())
const handleLogout = () => { auth.logout(); router.push('/login') }

// ── Notifications ──
const showNotif     = ref(false)
const mobileMenuOpen = ref(false)
const notifications = ref([])
const unreadCount   = ref(0)
const loadingNotif  = ref(false)
const notifRef      = ref(null)
let pollTimer = null

const notifIcon = t => ({ GRADE:'📊', ENROLLMENT:'📚', COMPLETION:'🎉', SYSTEM:'📢' }[t] ?? '🔔')

function timeAgo(dt) {
  if (!dt) return ''
  const diff = Date.now() - new Date(dt).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1)  return 'Vừa xong'
  if (m < 60) return `${m} phút trước`
  const h = Math.floor(m / 60)
  return h < 24 ? `${h} giờ trước` : `${Math.floor(h/24)} ngày trước`
}

async function fetchUnreadCount() {
  if (!auth.user?.id) return
  try {
    const r = await api.get(`/notifications/unread-count?userId=${auth.user.id}`)
    unreadCount.value = r.data.count ?? 0
  } catch {}
}

async function fetchNotifications() {
  if (!auth.user?.id) return
  loadingNotif.value = true
  try {
    const r = await api.get(`/notifications?userId=${auth.user.id}`)
    notifications.value = r.data
  } catch {} finally { loadingNotif.value = false }
}

async function toggleNotif() {
  showNotif.value = !showNotif.value
  if (showNotif.value) await fetchNotifications()
}

async function markAllRead() {
  try {
    await api.patch(`/notifications/read-all?userId=${auth.user.id}`)
    notifications.value.forEach(n => n.read = true)
    unreadCount.value = 0
  } catch {}
}

async function clearAll() {
  try {
    await api.delete(`/notifications/clear?userId=${auth.user.id}`)
  } catch {}
  // Xóa local dù API có lỗi hay không
  notifications.value = []
  unreadCount.value   = 0
}

async function clickNotif(n) {
  if (!n.read) {
    try { await api.patch(`/notifications/${n.id}/read`); n.read=true; unreadCount.value=Math.max(0,unreadCount.value-1) } catch {}
  }
  showNotif.value = false
  if (n.link) router.push(n.link)
}

function handleClickOutside(e) {
  if (notifRef.value && !notifRef.value.contains(e.target)) showNotif.value=false
}

watch(() => auth.isLoggedIn, v => {
  if (v) { fetchUnreadCount(); pollTimer = setInterval(fetchUnreadCount, 30000) }
  else   { clearInterval(pollTimer); unreadCount.value=0 }
}, { immediate: true })

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  // Sync avatar mới nhất từ server khi app khởi động
  auth.refreshAvatar()
})
onUnmounted(() => { document.removeEventListener('click', handleClickOutside); clearInterval(pollTimer) })
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

:root {
  --bg:           #f0f4f8;
  --surface:      #ffffff;
  --surface2:     #f8fafc;
  --border:       #e2e8f0;
  --border2:      #cbd5e1;
  --text:         #0f172a;
  --text2:        #334155;
  --muted:        #64748b;
  --accent:       #2563eb;
  --accent-light: #dbeafe;
  --accent-dark:  #1d4ed8;
  --green:        #059669;
  --green-light:  #d1fae5;
  --yellow:       #d97706;
  --yellow-light: #fef3c7;
  --red:          #dc2626;
  --red-light:    #fee2e2;
  --purple:       #7c3aed;
  --purple-light: #ede9fe;
  --shadow-sm:    0 1px 3px rgba(0,0,0,.08),0 1px 2px rgba(0,0,0,.04);
  --shadow:       0 4px 12px rgba(0,0,0,.08),0 2px 4px rgba(0,0,0,.04);
  --shadow-lg:    0 10px 30px rgba(0,0,0,.1),0 4px 8px rgba(0,0,0,.05);
}

* { box-sizing:border-box; margin:0; padding:0; }
body { font-family:'Plus Jakarta Sans',sans-serif; background:var(--bg); color:var(--text); min-height:100vh; -webkit-font-smoothing:antialiased; }

/* ── NAVBAR ── */
.navbar {
  background: rgba(255,255,255,.94);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border);
  padding: 0 1.8rem;
  height: 62px;
  display: flex; align-items: center; gap: 1.2rem;
  position: fixed; top:0; left:0; right:0; z-index:100;
  box-shadow: var(--shadow-sm);
}
.nav-brand { display:flex; align-items:center; gap:.5rem; text-decoration:none; font-weight:800; font-size:1.05rem; color:var(--text); white-space:nowrap; flex-shrink:0; }
.brand-icon { font-size:1.25rem; }
.nav-links  { display:flex; gap:.15rem; flex:1; }
.nav-item   { display:flex; align-items:center; gap:.42rem; padding:.4rem .82rem; border-radius:8px; color:var(--muted); text-decoration:none; font-size:.84rem; font-weight:500; transition:all .15s; white-space:nowrap; }
.nav-item:hover              { background:var(--surface2); color:var(--text2); }
.nav-item.router-link-active { background:var(--accent-light); color:var(--accent); font-weight:600; }
.admin-link.router-link-active { background:var(--purple-light); color:var(--purple); }
.nav-right  { display:flex; align-items:center; gap:.55rem; margin-left:auto; flex-shrink:0; }

/* ── NOTIFICATION ── */
.notif-wrap { position:relative; }

.notif-btn {
  width: 36px; height: 36px;
  border-radius: 9px;
  background: transparent;
  border: 1.5px solid var(--border);
  color: var(--muted);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
  position: relative;
  flex-shrink: 0;
}
.notif-btn svg { display:block; flex-shrink:0; }
.notif-btn:hover { background:var(--accent-light); border-color:var(--accent); color:var(--accent); }
.notif-btn.active { background:var(--accent-light); border-color:var(--accent); color:var(--accent); }

.notif-badge {
  position: absolute; top: -6px; right: -6px;
  background: var(--red); color: #fff;
  font-size: .59rem; font-weight: 800;
  min-width: 17px; height: 17px; border-radius: 100px;
  display: flex; align-items: center; justify-content: center;
  padding: 0 4px; border: 2px solid #fff;
  pointer-events: none;
}

.notif-dropdown {
  position: absolute; top: calc(100% + 10px); right: 0;
  width: 340px;
  background: var(--surface);
  border: 1.5px solid var(--border);
  border-radius: 14px;
  box-shadow: var(--shadow-lg);
  z-index: 200;
  overflow: hidden;
}
.dropdown-enter-active { transition: opacity .15s, transform .15s; }
.dropdown-enter-from   { opacity:0; transform:translateY(-6px); }
.dropdown-leave-active { transition: opacity .1s; }
.dropdown-leave-to     { opacity:0; }

.notif-hd {
  display:flex; align-items:center; justify-content:space-between;
  padding:.85rem 1.1rem; border-bottom:1.5px solid var(--border);
}
.notif-hd-left { display:flex; align-items:center; gap:.45rem; color:var(--text2); }
.notif-hd-title { font-size:.88rem; font-weight:700; }
.notif-count-badge { font-size:.65rem; font-weight:800; padding:.1rem .42rem; border-radius:100px; background:var(--red); color:#fff; }
.notif-hd-actions { display:flex; align-items:center; gap:.3rem; }
.notif-read-all { font-size:.74rem; color:var(--accent); background:none; border:none; cursor:pointer; font-weight:600; font-family:'Plus Jakarta Sans',sans-serif; padding:.2rem .4rem; border-radius:5px; white-space:nowrap; }
.notif-read-all:hover { background:var(--accent-light); }
.notif-clear-all { display:flex; align-items:center; gap:.3rem; font-size:.74rem; color:var(--red); background:none; border:none; cursor:pointer; font-weight:600; font-family:'Plus Jakarta Sans',sans-serif; padding:.2rem .4rem; border-radius:5px; white-space:nowrap; }
.notif-clear-all:hover { background:var(--red-light); }

.notif-loading { display:flex; justify-content:center; padding:2rem; }
.spinner-sm { width:22px; height:22px; border:2.5px solid var(--border); border-top-color:var(--accent); border-radius:50%; animation:spin .7s linear infinite; }
@keyframes spin { to{transform:rotate(360deg)} }

.notif-empty { display:flex; flex-direction:column; align-items:center; padding:2.2rem; gap:.6rem; color:var(--muted); }
.notif-empty p { font-size:.82rem; }

.notif-list { max-height:360px; overflow-y:auto; }
.notif-item { display:flex; align-items:flex-start; gap:.8rem; padding:.85rem 1.1rem; cursor:pointer; transition:background .12s; border-bottom:1px solid var(--border); position:relative; }
.notif-item:last-child { border-bottom:none; }
.notif-item:hover       { background:var(--surface2); }
.notif-item.unread      { background:#f8faff; }
.notif-item.unread:hover{ background:#eff4ff; }

.notif-type-icon { width:34px; height:34px; border-radius:9px; display:flex; align-items:center; justify-content:center; font-size:1rem; flex-shrink:0; }
.ni-grade      { background:var(--accent-light); }
.ni-enrollment { background:var(--green-light); }
.ni-completion { background:var(--yellow-light); }
.ni-system     { background:var(--purple-light); }

.notif-body  { flex:1; min-width:0; text-align:left; }
.notif-title { font-size:.82rem; font-weight:700; margin-bottom:.18rem; text-align:left; }
.notif-msg   { font-size:.76rem; color:var(--muted); line-height:1.4; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; text-align:left; }
.notif-time  { font-size:.69rem; color:var(--border2); margin-top:.25rem; text-align:left; }
.unread-dot  { width:8px; height:8px; border-radius:50%; background:var(--accent); flex-shrink:0; margin-top:5px; }

/* ── USER BUTTON ── */
.nav-user-btn { display:flex; align-items:center; gap:.55rem; padding:.28rem .75rem .28rem .3rem; border-radius:100px; background:var(--surface2); border:1px solid var(--border); text-decoration:none; transition:all .15s; flex-shrink:0; }
.nav-user-btn:hover { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }
.nav-avatar  { width:30px; height:30px; border-radius:50%; background:linear-gradient(135deg,var(--accent),var(--purple)); color:#fff; font-size:.78rem; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; overflow:hidden; }
.nav-av-img  { width:30px; height:30px; border-radius:50%; object-fit:cover; display:block; }
.nav-user-text { display:flex; flex-direction:column; }
.nav-uname { font-size:.78rem; font-weight:600; color:var(--text); line-height:1.2; }
.nav-role  { font-size:.62rem; color:var(--muted); text-transform:uppercase; letter-spacing:.05em; }

/* ── LOGOUT ── */
.btn-logout {
  width: 36px; height: 36px;
  border-radius: 9px;
  background: transparent;
  border: 1.5px solid var(--border);
  color: var(--muted);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
  flex-shrink: 0;
}
.btn-logout svg { display:block; flex-shrink:0; }
.btn-logout:hover { background:var(--red-light); border-color:var(--red); color:var(--red); }

main { min-height:100vh; }
main.with-nav { padding-top:62px; }
/* Auth pages (login/register) phải full screen, không bị ảnh hưởng bởi layout */
main:not(.with-nav) { padding:0; overflow:hidden; }
/* Landing page: full width, no padding, no overflow restriction */
main.is-landing { padding:0; overflow:visible; width:100%; }

.fade-enter-active,.fade-leave-active { transition:opacity .15s,transform .15s; }
.fade-enter-from { opacity:0; transform:translateY(6px); }
.fade-leave-to   { opacity:0; }

/* Globals */
/* ── RESPONSIVE NAVBAR ── */
@media (max-width: 768px) {
  .hamburger {
    display: flex; flex-direction: column; justify-content: center;
    gap: 5px; width: 36px; height: 36px;
    background: none; border: 1.5px solid var(--border);
    border-radius: 8px; cursor: pointer; padding: 6px;
    flex-shrink: 0; order: -1;
  }
  .hamburger span {
    display: block; width: 100%; height: 2px;
    background: var(--text2); border-radius: 2px;
    transition: all .25s;
  }
  .hamburger.open span:nth-child(1) { transform: translateY(7px) rotate(45deg); }
  .hamburger.open span:nth-child(2) { opacity: 0; }
  .hamburger.open span:nth-child(3) { transform: translateY(-7px) rotate(-45deg); }

  .nav-links {
    display: none;
    position: fixed; top: 62px; left: 0; right: 0;
    background: rgba(255,255,255,.98);
    backdrop-filter: blur(16px);
    border-bottom: 1px solid var(--border);
    flex-direction: column; padding: .6rem;
    box-shadow: 0 8px 24px rgba(0,0,0,.1);
    z-index: 99;
  }
  .nav-links.mobile-open { display: flex; }
  .nav-item { width: 100%; padding: .65rem 1rem; font-size: .9rem; }

  .nav-user-btn .nav-user-text { display: none; }
  .notif-dropdown { width: 300px; right: -60px; }
}
@media (min-width: 769px) { .hamburger { display: none; } }

::-webkit-scrollbar       { width:5px; height:5px; }
::-webkit-scrollbar-track { background:transparent; }
::-webkit-scrollbar-thumb { background:var(--border2); border-radius:3px; }
</style>