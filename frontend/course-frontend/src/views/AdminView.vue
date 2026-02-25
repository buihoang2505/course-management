<template>
  <div class="page">
    <div class="page-header">
      <h1>Quản lý hệ thống</h1>
      <p>Admin Dashboard</p>
    </div>

    <!-- Tabs -->
    <div class="tabs">
      <button v-for="t in tabs" :key="t.key"
              :class="['tab', { active: activeTab===t.key }]"
              @click="switchTab(t.key)">
        <span class="tab-icon">{{ t.icon }}</span>{{ t.label }}
      </button>
    </div>

    <!-- Content area – min-height cố định để không bị nhảy -->
    <div class="content-area">

      <!-- ── COURSES ── -->
      <template v-if="activeTab==='courses'">
        <div class="sec-bar">
          <h2>Danh sách khóa học</h2>
          <button @click="openCourseModal()" class="btn btn-accent">+ Thêm khóa học</button>
        </div>
        <div v-if="loadingCourses" class="loading">⏳ Đang tải...</div>
        <div v-else class="tbl-wrap">
          <table class="tbl">
            <thead><tr><th>ID</th><th>Tên khóa học</th><th>Giảng viên</th><th>TC</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
            <tr v-for="c in courses" :key="c.id">
              <td class="id-col">#{{ c.id }}</td>
              <td><div class="ct">{{ c.title }}</div><div class="cs">{{ c.description }}</div></td>
              <td>{{ c.instructor||'—' }}</td>
              <td><span class="badge badge-blue">{{ c.credits }}</span></td>
              <td><span :class="['badge',c.status==='ACTIVE'?'badge-green':'badge-gray']">{{ c.status }}</span></td>
              <td><div class="acts">
                <button @click="openLessonManager(c)" class="ab">📖 Bài học</button>
                <button @click="openCourseModal(c)" class="ab">✏️</button>
                <button @click="delCourse(c.id)" class="ab ab-red">🗑️</button>
              </div></td>
            </tr>
            </tbody>
          </table>
        </div>
      </template>

      <!-- ── LESSONS ── -->
      <template v-if="activeTab==='lessons'">
        <div class="sec-bar">
          <div>
            <h2>Bài học — {{ selCourse?.title || 'Chọn khóa học' }}</h2>
            <p class="sec-sub">Thêm, sửa, xóa bài học</p>
          </div>
          <div class="bar-r">
            <select v-model="lessonCid" @change="loadLessons" class="sel">
              <option value="">-- Chọn khóa học --</option>
              <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.title }}</option>
            </select>
            <button v-if="lessonCid" @click="openLessonModal()" class="btn btn-accent">+ Thêm</button>
          </div>
        </div>
        <div v-if="loadingLessons" class="loading">⏳</div>
        <div v-else-if="!lessonCid" class="empty-state"><div class="empty-icon">📖</div><p>Chọn khóa học</p></div>
        <div v-else-if="!lessons.length" class="empty-state"><div class="empty-icon">📝</div><p>Chưa có bài học</p></div>
        <div v-else class="lesson-list">
          <div v-for="l in lessons" :key="l.id" class="lcard">
            <div class="l-num">{{ l.orderNum }}</div>
            <div class="l-body">
              <div class="l-title">{{ l.title }}</div>
              <!-- Fix: content cố định max-height, không đẩy card -->
              <div class="l-content">{{ l.content }}</div>
              <a v-if="l.videoUrl" :href="l.videoUrl" target="_blank" class="l-video">🎥 {{ l.videoUrl }}</a>
            </div>
            <div class="acts">
              <button @click="openLessonModal(l)" class="ab">✏️ Sửa</button>
              <button @click="delLesson(l.id)" class="ab ab-red">🗑️</button>
            </div>
          </div>
        </div>
      </template>

      <!-- ── USERS ── -->
      <template v-if="activeTab==='users'">
        <div class="sec-bar"><h2>Danh sách học viên</h2></div>
        <div v-if="loadingUsers" class="loading">⏳</div>
        <div v-else class="tbl-wrap">
          <table class="tbl">
            <thead><tr><th>ID</th><th>Username</th><th>Email</th><th>Vai trò</th><th>Thao tác</th></tr></thead>
            <tbody>
            <tr v-for="u in users" :key="u.id">
              <td class="id-col">#{{ u.id }}</td>
              <td><strong>{{ u.username }}</strong></td>
              <td class="muted-col">{{ u.email }}</td>
              <td><span :class="['badge',u.role==='ADMIN'?'badge-purple':'badge-blue']">{{ u.role }}</span></td>
              <td><button @click="delUser(u.id)" class="ab ab-red">🗑️ Xóa</button></td>
            </tr>
            </tbody>
          </table>
        </div>
      </template>

      <!-- ── ENROLLMENTS + GRADES ── -->
      <template v-if="activeTab==='enrollments'">
        <div class="sec-bar">
          <div>
            <h2>Đăng ký & Điểm số</h2>
            <p class="sec-sub">Chọn khóa học để xem học viên và nhập điểm</p>
          </div>
          <select v-model="enrollCid" @change="loadEnrollments" class="sel sel-lg">
            <option value="">-- Chọn khóa học --</option>
            <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.title }}</option>
          </select>
        </div>

        <div v-if="!enrollCid" class="empty-state"><div class="empty-icon">📋</div><p>Chọn khóa học</p></div>
        <div v-else-if="loadingEnroll" class="loading">⏳</div>
        <div v-else-if="!enrollments.length" class="empty-state"><div class="empty-icon">👥</div><p>Chưa có học viên nào đăng ký</p></div>
        <div v-else>
          <div class="stats-bar">
            <div class="sb-it"><span class="sb-n">{{ enrollments.length }}</span><span class="sb-l">Học viên</span></div>
            <div class="sb-it"><span class="sb-n green">{{ enrollments.filter(e=>e.grade).length }}</span><span class="sb-l">Có điểm</span></div>
            <div class="sb-it"><span class="sb-n yellow">{{ enrollments.filter(e=>!e.grade).length }}</span><span class="sb-l">Chưa có điểm</span></div>
            <div v-if="avgEnroll" class="sb-it"><span class="sb-n blue">{{ avgEnroll }}</span><span class="sb-l">Điểm TB</span></div>
          </div>
          <div class="tbl-wrap">
            <table class="tbl">
              <thead><tr><th>#</th><th>Học viên</th><th>Email</th><th>Ngày ĐK</th><th>Điểm</th><th>Nhận xét</th><th>Thao tác</th></tr></thead>
              <tbody>
              <tr v-for="(e,i) in enrollments" :key="e.id">
                <td class="id-col">{{ i+1 }}</td>
                <td>
                  <div class="ucell">
                    <div class="uav">{{ (e.user?.username||'?').charAt(0).toUpperCase() }}</div>
                    <div>
                      <div class="uname">{{ e.user?.username || '—' }}</div>
                      <div class="uid">ID #{{ e.user?.id }}</div>
                    </div>
                  </div>
                </td>
                <td class="muted-col">{{ e.user?.email || '—' }}</td>
                <td class="muted-col">{{ fmt(e.enrolledAt) }}</td>
                <td>
                  <span v-if="e.grade" :class="['schip', sc(e.grade.score)]">{{ e.grade.score?.toFixed(1) }}</span>
                  <span v-else class="no-score">—</span>
                </td>
                <td class="fb-col">{{ e.grade?.feedback||'—' }}</td>
                <td><button @click="openGradeModal(e)" class="ab">{{ e.grade?'✏️ Sửa':'📝 Nhập' }}</button></td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>

    </div><!-- /content-area -->

    <!-- MODAL: Course -->
    <div v-if="showCM" class="modal-overlay" @click.self="showCM=false">
      <div class="modal">
        <h2>{{ editC?.id?'✏️ Sửa':'➕ Thêm' }} khóa học</h2>
        <form @submit.prevent="saveCourse">
          <div class="form-group"><label>Tên *</label><input v-model="cf.title" required /></div>
          <div class="form-group"><label>Mô tả</label><textarea v-model="cf.description" rows="2"></textarea></div>
          <div class="f2"><div class="form-group"><label>Giảng viên</label><input v-model="cf.instructor"/></div>
            <div class="form-group"><label>Tín chỉ *</label><input v-model.number="cf.credits" type="number" min="1" required/></div></div>
          <div class="form-group"><label>Trạng thái</label><select v-model="cf.status"><option>ACTIVE</option><option>INACTIVE</option><option>DRAFT</option></select></div>
          <div class="modal-actions"><button type="button" @click="showCM=false" class="btn btn-ghost">Hủy</button><button type="submit" class="btn btn-accent" :disabled="saving">{{ saving?'...':'Lưu' }}</button></div>
        </form>
      </div>
    </div>

    <!-- MODAL: Lesson -->
    <div v-if="showLM" class="modal-overlay" @click.self="showLM=false">
      <div class="modal">
        <h2>{{ editL?.id?'✏️ Sửa':'➕ Thêm' }} bài học</h2>
        <form @submit.prevent="saveLesson">
          <div class="form-group"><label>Tiêu đề *</label><input v-model="lf.title" required /></div>
          <div class="form-group"><label>Nội dung</label><textarea v-model="lf.content" rows="4"></textarea></div>
          <div class="f2"><div class="form-group"><label>Thứ tự *</label><input v-model.number="lf.orderNum" type="number" min="1" required/></div>
            <div class="form-group"><label>Video URL</label><input v-model="lf.videoUrl" placeholder="https://..."/></div></div>
          <div class="modal-actions"><button type="button" @click="showLM=false" class="btn btn-ghost">Hủy</button><button type="submit" class="btn btn-accent" :disabled="saving">{{ saving?'...':'Lưu' }}</button></div>
        </form>
      </div>
    </div>

    <!-- MODAL: Grade – student info nổi bật -->
    <div v-if="showGM" class="modal-overlay" @click.self="showGM=false">
      <div class="modal">
        <h2>📝 {{ gradingE?.grade?'Sửa điểm':'Nhập điểm' }}</h2>
        <div class="student-box">
          <div class="sb-av">{{ (gradingE?.user?.username||'?').charAt(0).toUpperCase() }}</div>
          <div class="sb-info">
            <div class="sb-name">{{ gradingE?.user?.username }}</div>
            <div class="sb-email">{{ gradingE?.user?.email }}</div>
          </div>
          <div v-if="gradingE?.grade" class="sb-old">Điểm cũ: <strong>{{ gradingE.grade.score }}</strong></div>
        </div>
        <form @submit.prevent="saveGrade">
          <div class="form-group">
            <label>Điểm (0–10) *</label>
            <div class="score-row">
              <input v-model.number="gf.score" type="number" min="0" max="10" step="0.1" required placeholder="VD: 8.5" style="flex:1"/>
              <div v-if="gf.score!==''" :class="['sp', sc(gf.score)]">{{ gf.score }}</div>
            </div>
          </div>
          <div class="form-group"><label>Nhận xét</label><textarea v-model="gf.feedback" rows="3" placeholder="Nhận xét..."></textarea></div>
          <div class="modal-actions"><button type="button" @click="showGM=false" class="btn btn-ghost">Hủy</button><button type="submit" class="btn btn-accent" :disabled="saving">{{ saving?'...':'Lưu điểm' }}</button></div>
        </form>
      </div>
    </div>

    <div v-if="toast" :class="['toast',toast.type]">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../services/api'

const activeTab = ref('courses')
const tabs = [
  { key:'courses',     icon:'📚', label:'Khóa học' },
  { key:'lessons',     icon:'📖', label:'Bài học' },
  { key:'users',       icon:'👥', label:'Học viên' },
  { key:'enrollments', icon:'📋', label:'Đăng ký & Điểm' },
]

const courses=ref([]); const lessons=ref([]); const users=ref([]); const enrollments=ref([])
const loadingCourses=ref(false); const loadingLessons=ref(false); const loadingUsers=ref(false); const loadingEnroll=ref(false)
const enrollCid=ref(''); const lessonCid=ref(''); const selCourse=ref(null)
const showCM=ref(false); const showLM=ref(false); const showGM=ref(false)
const editC=ref(null); const editL=ref(null); const gradingE=ref(null)
const saving=ref(false)
const cf=ref({title:'',description:'',instructor:'',credits:3,status:'ACTIVE'})
const lf=ref({title:'',content:'',orderNum:1,videoUrl:''})
const gf=ref({score:'',feedback:''})
const toast=ref(null)

const avgEnroll = computed(()=>{
  const g=enrollments.value.filter(e=>e.grade?.score!=null)
  if(!g.length) return null
  return (g.reduce((s,e)=>s+e.grade.score,0)/g.length).toFixed(1)
})

const showToast=(msg,type='success')=>{ toast.value={msg,type}; setTimeout(()=>toast.value=null,3000) }
const fmt=dt=>dt?new Date(dt).toLocaleDateString('vi-VN'):'—'
const sc=s=>s>=8.5?'sc-exc':s>=7?'sc-good':s>=5?'sc-avg':'sc-poor'

function switchTab(key){ activeTab.value=key; if(key==='users'&&!users.value.length) loadUsers() }

async function loadCourses(){
  loadingCourses.value=true
  try{ courses.value=(await api.get('/courses')).data } catch{ showToast('Lỗi!','error') }
  finally{ loadingCourses.value=false }
}
function openCourseModal(c=null){
  editC.value=c
  cf.value=c?{title:c.title,description:c.description,instructor:c.instructor,credits:c.credits,status:c.status}:{title:'',description:'',instructor:'',credits:3,status:'ACTIVE'}
  showCM.value=true
}
async function saveCourse(){
  saving.value=true
  try{
    editC.value?.id ? await api.put(`/courses/${editC.value.id}`,cf.value) : await api.post('/courses',cf.value)
    showToast(editC.value?.id?'Cập nhật thành công!':'Thêm khóa học thành công!')
    showCM.value=false; loadCourses()
  } catch(e){ showToast(e.response?.data?.error||'Lỗi!','error') }
  finally{ saving.value=false }
}
async function delCourse(id){
  if(!confirm('Xóa khóa học này?')) return
  try{ await api.delete(`/courses/${id}`); showToast('Đã xóa!'); loadCourses() }
  catch{ showToast('Xóa thất bại!','error') }
}

function openLessonManager(c){ selCourse.value=c; lessonCid.value=c.id; activeTab.value='lessons'; loadLessons() }
async function loadLessons(){
  if(!lessonCid.value) return
  selCourse.value=courses.value.find(c=>c.id==lessonCid.value)
  loadingLessons.value=true
  try{ lessons.value=(await api.get(`/lessons/course/${lessonCid.value}`)).data }
  catch{ showToast('Lỗi tải bài học!','error') }
  finally{ loadingLessons.value=false }
}
function openLessonModal(l=null){
  editL.value=l
  lf.value=l?{title:l.title,content:l.content,orderNum:l.orderNum,videoUrl:l.videoUrl||''}:{title:'',content:'',orderNum:lessons.value.length+1,videoUrl:''}
  showLM.value=true
}
async function saveLesson(){
  saving.value=true
  try{
    editL.value?.id ? await api.put(`/lessons/${editL.value.id}`,lf.value) : await api.post(`/lessons?courseId=${lessonCid.value}`,lf.value)
    showToast('Lưu bài học thành công!'); showLM.value=false; loadLessons()
  } catch{ showToast('Lỗi!','error') }
  finally{ saving.value=false }
}
async function delLesson(id){
  if(!confirm('Xóa bài học?')) return
  try{ await api.delete(`/lessons/${id}`); showToast('Đã xóa!'); loadLessons() }
  catch{ showToast('Thất bại!','error') }
}

async function loadUsers(){
  loadingUsers.value=true
  try{ users.value=(await api.get('/users')).data } catch{}
  finally{ loadingUsers.value=false }
}
async function delUser(id){
  if(!confirm('Xóa user?')) return
  try{ await api.delete(`/users/${id}`); showToast('Đã xóa!'); loadUsers() }
  catch{ showToast('Thất bại!','error') }
}

async function loadEnrollments(){
  if(!enrollCid.value) return
  loadingEnroll.value=true
  try{ enrollments.value=(await api.get(`/enrollments/course/${enrollCid.value}`)).data }
  catch{} finally{ loadingEnroll.value=false }
}

function openGradeModal(e){ gradingE.value=e; gf.value={score:e.grade?.score??'',feedback:e.grade?.feedback??''}; showGM.value=true }
async function saveGrade(){
  saving.value=true
  try{
    await api.post('/grades/assign',{enrollmentId:gradingE.value.id,score:gf.value.score,feedback:gf.value.feedback})
    showToast(`✅ Đã lưu điểm ${gf.value.score} cho ${gradingE.value.user?.username}!`)
    showGM.value=false; loadEnrollments()
  } catch{ showToast('Lỗi nhập điểm!','error') }
  finally{ saving.value=false }
}

onMounted(loadCourses)
</script>

<style scoped>
.page { padding: 2rem 2.5rem; max-width: 1400px; margin: 0 auto; }
.page-header { margin-bottom: 1.5rem; }
.page-header h1 { font-size: 1.6rem; font-weight: 800; margin-bottom: .3rem; }
.page-header p  { color: var(--muted); font-size: .85rem; }

/* Tabs */
.tabs { display:flex; gap:.25rem; margin-bottom:1.2rem; background:var(--surface); padding:.35rem; border-radius:12px; width:fit-content; border:1.5px solid var(--border); box-shadow:var(--shadow-sm); }
.tab { display:flex; align-items:center; gap:.4rem; padding:.44rem 1rem; border:none; background:transparent; border-radius:8px; cursor:pointer; font-size:.82rem; font-weight:500; color:var(--muted); transition:all .15s; font-family:'Plus Jakarta Sans',sans-serif; white-space:nowrap; }
.tab:hover { background:var(--surface2); color:var(--text2); }
.tab.active { background:var(--accent); color:#fff; font-weight:700; box-shadow:0 2px 8px rgba(37,99,235,.3); }
.tab-icon { font-size:.9rem; }

/* Content area – min-height để không nhảy khi đổi tab */
.content-area {
  min-height: 400px;
  background: var(--surface);
  border: 1.5px solid var(--border);
  border-radius: 14px;
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
}

.sec-bar { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:1.2rem; gap:1rem; flex-wrap:wrap; }
.sec-bar h2 { font-size:1.05rem; font-weight:700; }
.sec-sub { font-size:.77rem; color:var(--muted); margin-top:.2rem; }
.bar-r { display:flex; gap:.6rem; align-items:center; flex-wrap:wrap; }

/* Table */
.tbl-wrap { border-radius:10px; overflow:hidden; border:1.5px solid var(--border); }
.tbl { width:100%; border-collapse:collapse; font-size:.83rem; background:var(--surface); }
.tbl th { background:#f8fafc; color:var(--muted); padding:.62rem 1rem; text-align:left; font-size:.71rem; font-weight:700; text-transform:uppercase; letter-spacing:.06em; border-bottom:1.5px solid var(--border); white-space:nowrap; }
.tbl td { padding:.78rem 1rem; border-bottom:1px solid var(--border); vertical-align:middle; }
.tbl tr:last-child td { border-bottom:none; }
.tbl tr:hover td { background:#f8fafc; }
.id-col   { color:var(--muted); font-size:.75rem; font-family:monospace; }
.muted-col { font-size:.8rem; color:var(--muted); }
.fb-col   { font-size:.78rem; color:var(--muted); max-width:140px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.ct { font-weight:600; margin-bottom:.12rem; }
.cs { font-size:.75rem; color:var(--muted); max-width:240px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }

/* User cell */
.ucell { display:flex; align-items:center; gap:.6rem; }
.uav   { width:32px; height:32px; border-radius:50%; background:linear-gradient(135deg,var(--accent),var(--purple)); color:#fff; font-size:.8rem; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.uname { font-weight:700; font-size:.85rem; line-height:1.2; }
.uid   { font-size:.7rem; color:var(--muted); }

/* Action buttons */
.acts { display:flex; gap:.35rem; flex-wrap:wrap; }
.ab   { padding:.3rem .7rem; font-size:.75rem; font-weight:600; border-radius:6px; cursor:pointer; border:1.5px solid var(--border); background:var(--surface); color:var(--text2); font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; white-space:nowrap; }
.ab:hover      { border-color:var(--accent); color:var(--accent); background:var(--accent-light); }
.ab.ab-red:hover { border-color:var(--red); color:var(--red); background:var(--red-light); }

.sel { padding:.48rem .85rem; background:var(--surface); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.83rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; box-shadow:var(--shadow-sm); }
.sel:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }
.sel-lg { min-width:260px; }

.badge { font-size:.68rem; font-weight:700; padding:.2rem .6rem; border-radius:100px; }
.badge-green  { background:var(--green-light);  color:var(--green);  border:1px solid #a7f3d0; }
.badge-blue   { background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; }
.badge-purple { background:var(--purple-light); color:var(--purple); border:1px solid #ddd6fe; }
.badge-gray   { background:#f1f5f9; color:var(--muted); border:1px solid var(--border); }

/* Score */
.schip { font-size:.84rem; font-weight:800; padding:.22rem .6rem; border-radius:7px; }
.sc-exc  { background:var(--green-light);  color:var(--green); }
.sc-good { background:var(--accent-light); color:var(--accent); }
.sc-avg  { background:var(--yellow-light); color:var(--yellow); }
.sc-poor { background:var(--red-light);    color:var(--red); }
.no-score { color:var(--muted); font-size:.82rem; }

/* Stats bar */
.stats-bar { display:flex; gap:1rem; background:var(--surface2); border:1.5px solid var(--border); border-radius:10px; padding:.9rem 1.2rem; margin-bottom:1rem; flex-wrap:wrap; }
.sb-it { display:flex; flex-direction:column; align-items:center; min-width:70px; }
.sb-n  { font-size:1.4rem; font-weight:800; line-height:1.2; }
.sb-n.green  { color:var(--green); }
.sb-n.yellow { color:var(--yellow); }
.sb-n.blue   { color:var(--accent); }
.sb-l { font-size:.7rem; color:var(--muted); margin-top:.12rem; }

/* Lesson list – content cố định height */
.lesson-list { display:flex; flex-direction:column; gap:.6rem; }
.lcard { background:var(--surface2); border:1.5px solid var(--border); border-radius:10px; padding:.95rem 1.1rem; display:flex; align-items:flex-start; gap:.9rem; transition:border-color .15s; }
.lcard:hover { border-color:var(--accent); }
.l-num   { width:28px; height:28px; border-radius:7px; background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; display:flex; align-items:center; justify-content:center; font-size:.78rem; font-weight:700; flex-shrink:0; }
.l-body  { flex:1; min-width:0; }
.l-title { font-size:.88rem; font-weight:600; margin-bottom:.2rem; }
/* Fix: cố định max-height và overflow để không làm thay đổi kích thước card */
.l-content { font-size:.77rem; color:var(--muted); line-height:1.5; max-height:2.4em; overflow:hidden; text-overflow:ellipsis; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; }
.l-video   { font-size:.73rem; color:var(--accent); margin-top:.25rem; display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }

/* Grade modal student box */
.student-box { display:flex; align-items:center; gap:.9rem; background:var(--accent-light); border:1.5px solid #bfdbfe; border-radius:10px; padding:.85rem 1.1rem; margin-bottom:1.3rem; }
.sb-av { width:40px; height:40px; border-radius:50%; background:linear-gradient(135deg,var(--accent),var(--purple)); color:#fff; font-size:1rem; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.sb-name  { font-size:.95rem; font-weight:700; }
.sb-email { font-size:.77rem; color:var(--muted); }
.sb-old   { margin-left:auto; font-size:.8rem; background:var(--surface); padding:.3rem .7rem; border-radius:7px; border:1px solid var(--border); white-space:nowrap; flex-shrink:0; }
.score-row { display:flex; align-items:center; gap:.8rem; }
.sp { font-size:1.3rem; font-weight:800; padding:.3rem .7rem; border-radius:8px; min-width:54px; text-align:center; }
.f2 { display:grid; grid-template-columns:1fr 1fr; gap:0 1rem; }

.loading     { text-align:center; padding:2.5rem; color:var(--muted); }
.empty-state { text-align:center; padding:3rem 2rem; color:var(--muted); }
.empty-icon  { font-size:2rem; margin-bottom:.6rem; opacity:.4; }
.empty-state p { font-size:.88rem; }

.toast { position:fixed; bottom:2rem; right:2rem; padding:.75rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .25s ease; border:1.5px solid; box-shadow:var(--shadow); }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light);   color:var(--red);   border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }

.btn { display:inline-flex; align-items:center; gap:.4rem; padding:.52rem 1.1rem; border-radius:8px; font-size:.84rem; font-weight:600; cursor:pointer; border:none; transition:all .15s; font-family:'Plus Jakarta Sans',sans-serif; }
.btn-accent { background:var(--accent); color:#fff; }
.btn-accent:hover:not(:disabled) { background:var(--accent-dark); box-shadow:0 4px 12px rgba(37,99,235,.25); }
.btn-accent:disabled { opacity:.5; cursor:not-allowed; }
.btn-ghost { background:var(--surface); color:var(--text2); border:1.5px solid var(--border); }
.btn-ghost:hover { border-color:var(--accent); color:var(--accent); }
</style>