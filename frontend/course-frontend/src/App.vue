<template>
  <div id="app">
    <nav v-if="auth.isLoggedIn" class="navbar">
      <router-link to="/courses" class="nav-brand">
        <span class="brand-icon">🎓</span>
        <span>EduFlow</span>
      </router-link>

      <div class="nav-links">
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
        <router-link v-if="auth.isAdmin" to="/admin" class="nav-item admin-link">
          <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
          Quản lý
        </router-link>
      </div>

      <div class="nav-right">
        <router-link to="/profile" class="nav-user-btn">
          <div class="nav-avatar">{{ initials }}</div>
          <div class="nav-user-text">
            <span class="nav-uname">{{ auth.user?.username }}</span>
            <span class="nav-role">{{ auth.user?.role }}</span>
          </div>
        </router-link>
        <!-- Fix: dùng text thay vì SVG để tránh mất icon -->
        <button @click="handleLogout" class="btn-logout" title="Đăng xuất">
          <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline stroke-linecap="round" stroke-linejoin="round" points="16 17 21 12 16 7"/>
            <line stroke-linecap="round" x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
      </div>
    </nav>

    <main :class="{ 'with-nav': auth.isLoggedIn }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'
const auth = useAuthStore()
const router = useRouter()
const initials = computed(() => (auth.user?.username || 'U').charAt(0).toUpperCase())
function handleLogout() { auth.logout(); router.push('/login') }
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

:root {
  --bg:            #f0f4f8;
  --surface:       #ffffff;
  --surface2:      #f8fafc;
  --border:        #e2e8f0;
  --border2:       #cbd5e1;
  --text:          #0f172a;
  --text2:         #334155;
  --muted:         #64748b;
  --accent:        #2563eb;
  --accent-light:  #dbeafe;
  --accent-dark:   #1d4ed8;
  --green:         #059669;
  --green-light:   #d1fae5;
  --yellow:        #d97706;
  --yellow-light:  #fef3c7;
  --red:           #dc2626;
  --red-light:     #fee2e2;
  --purple:        #7c3aed;
  --purple-light:  #ede9fe;
  --shadow-sm:     0 1px 3px rgba(0,0,0,.08), 0 1px 2px rgba(0,0,0,.04);
  --shadow:        0 4px 12px rgba(0,0,0,.08), 0 2px 4px rgba(0,0,0,.04);
  --shadow-lg:     0 10px 30px rgba(0,0,0,.1),  0 4px 8px rgba(0,0,0,.05);
  --radius:        12px;
}

* { box-sizing: border-box; margin: 0; padding: 0; }
body {
  font-family: 'Plus Jakarta Sans', sans-serif;
  background: var(--bg);
  color: var(--text);
  min-height: 100vh;
  -webkit-font-smoothing: antialiased;
}

/* ── NAVBAR ── */
.navbar {
  background: rgba(255,255,255,.94);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border);
  padding: 0 2rem;
  height: 62px;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  position: fixed; top: 0; left: 0; right: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}
.nav-brand { display:flex; align-items:center; gap:.5rem; text-decoration:none; font-weight:800; font-size:1.05rem; color:var(--text); white-space:nowrap; }
.brand-icon { font-size:1.2rem; }
.nav-links  { display:flex; gap:.2rem; flex:1; }
.nav-item   { display:flex; align-items:center; gap:.45rem; padding:.42rem .85rem; border-radius:8px; color:var(--muted); text-decoration:none; font-size:.84rem; font-weight:500; transition:all .15s; }
.nav-item:hover                { background:var(--surface2); color:var(--text2); }
.nav-item.router-link-active   { background:var(--accent-light); color:var(--accent); font-weight:600; }
.admin-link.router-link-active { background:var(--purple-light); color:var(--purple); }
.nav-right  { display:flex; align-items:center; gap:.6rem; margin-left:auto; }
.nav-user-btn { display:flex; align-items:center; gap:.6rem; padding:.28rem .75rem .28rem .28rem; border-radius:100px; background:var(--surface2); border:1px solid var(--border); text-decoration:none; transition:all .15s; }
.nav-user-btn:hover { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }
.nav-avatar  { width:30px; height:30px; border-radius:50%; background:linear-gradient(135deg,var(--accent),var(--purple)); color:#fff; font-size:.78rem; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.nav-user-text { display:flex; flex-direction:column; }
.nav-uname { font-size:.78rem; font-weight:600; color:var(--text); line-height:1.2; }
.nav-role  { font-size:.62rem; color:var(--muted); text-transform:uppercase; letter-spacing:.05em; }
.btn-logout {
  width: 34px; height: 34px;
  border-radius: 8px;
  background: transparent;
  border: 1.5px solid var(--border);
  color: var(--muted);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
  flex-shrink: 0;
}
.btn-logout:hover { background:var(--red-light); border-color:var(--red); color:var(--red); }
.btn-logout svg   { display:block; flex-shrink:0; }

main { min-height:100vh; }
main.with-nav { padding-top:62px; }
.fade-enter-active,.fade-leave-active { transition:opacity .15s,transform .15s; }
.fade-enter-from { opacity:0; transform:translateY(6px); }
.fade-leave-to   { opacity:0; }

/* ── GLOBAL ── */
.page { padding:2rem 2.5rem; max-width:1400px; margin:0 auto; }
.page-header { margin-bottom:1.8rem; }
.page-header h1 { font-size:1.6rem; font-weight:800; margin-bottom:.3rem; }
.page-header p  { color:var(--muted); font-size:.85rem; }

.btn { display:inline-flex; align-items:center; gap:.4rem; padding:.52rem 1.1rem; border-radius:8px; font-size:.84rem; font-weight:600; cursor:pointer; border:none; transition:all .15s; font-family:'Plus Jakarta Sans',sans-serif; text-decoration:none; white-space:nowrap; }
.btn-accent { background:var(--accent); color:#fff; }
.btn-accent:hover:not(:disabled) { background:var(--accent-dark); box-shadow:0 4px 12px rgba(37,99,235,.3); }
.btn-accent:disabled { opacity:.5; cursor:not-allowed; }
.btn-ghost  { background:var(--surface); color:var(--text2); border:1.5px solid var(--border); }
.btn-ghost:hover  { border-color:var(--accent); color:var(--accent); }
.btn-danger { background:var(--red-light); color:var(--red); border:1px solid #fca5a5; }
.btn-danger:hover { background:#fecaca; }

.badge { font-size:.68rem; font-weight:700; padding:.2rem .6rem; border-radius:100px; letter-spacing:.02em; }
.badge-green  { background:var(--green-light);  color:var(--green);  border:1px solid #a7f3d0; }
.badge-blue   { background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; }
.badge-purple { background:var(--purple-light); color:var(--purple); border:1px solid #ddd6fe; }
.badge-gray   { background:#f1f5f9; color:var(--muted); border:1px solid var(--border); }
.badge-yellow { background:var(--yellow-light); color:var(--yellow); border:1px solid #fde68a; }

.form-group { margin-bottom:.95rem; }
.form-group label { display:block; font-size:.74rem; font-weight:700; color:var(--muted); margin-bottom:.38rem; text-transform:uppercase; letter-spacing:.05em; }
.form-group input,.form-group textarea,.form-group select { width:100%; padding:.6rem .88rem; background:var(--surface); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.87rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; transition:border-color .15s,box-shadow .15s; }
.form-group input:focus,.form-group textarea:focus,.form-group select:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }

.modal-overlay { position:fixed; inset:0; background:rgba(15,23,42,.45); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:200; padding:1rem; }
.modal { background:var(--surface); border:1px solid var(--border); border-radius:16px; padding:1.8rem; width:100%; max-width:500px; box-shadow:var(--shadow-lg); }
.modal h2 { font-size:1.1rem; font-weight:800; margin-bottom:1.3rem; }
.modal-actions { display:flex; gap:.6rem; justify-content:flex-end; margin-top:1.3rem; }

.loading     { text-align:center; padding:3rem; color:var(--muted); font-size:.88rem; }
.empty-state { text-align:center; padding:3.5rem 2rem; color:var(--muted); }
.empty-icon  { font-size:2.2rem; margin-bottom:.7rem; opacity:.4; }
.empty-state p { font-size:.88rem; }

.toast { position:fixed; bottom:2rem; right:2rem; padding:.75rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .25s ease; border:1.5px solid; box-shadow:var(--shadow); }
.toast.success { background:var(--green-light);  color:var(--green);  border-color:#6ee7b7; }
.toast.error   { background:var(--red-light);    color:var(--red);    border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }

::-webkit-scrollbar       { width:6px; height:6px; }
::-webkit-scrollbar-track { background:var(--bg); }
::-webkit-scrollbar-thumb { background:var(--border2); border-radius:3px; }
</style>