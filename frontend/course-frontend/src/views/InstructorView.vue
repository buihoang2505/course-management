<template>
  <div class="page">
    <div class="page-header">
      <h1>Giảng viên</h1>
      <p>Xin chào, <strong>{{ auth.user?.username }}</strong> — Quản lý khóa học và bài học của bạn</p>
    </div>

    <div class="admin-shell">

      <!-- ── SIDEBAR ── -->
      <div class="sidebar">
        <button v-for="t in tabs" :key="t.key"
                :class="['stab', { active: activeTab===t.key }]"
                @click="switchTab(t.key)">
          <span>{{ t.icon }}</span>
          <span class="stab-label">{{ t.label }}</span>
        </button>
      </div>

      <!-- ── PANEL ── -->
      <div class="panel">

        <!-- Panel header -->
        <div class="panel-hd">
          <template v-if="activeTab==='dashboard'">
            <span class="phd-title">📊 Tổng quan của tôi</span>
            <button @click="loadStats" class="btn btn-ghost btn-sm">↺ Làm mới</button>
          </template>

          <template v-else-if="activeTab==='courses'">
            <span class="phd-title">Khóa học của tôi</span>
            <button @click="openCourseModal()" class="btn btn-accent btn-sm">+ Thêm</button>
          </template>

          <template v-else-if="activeTab==='lessons'">
            <div>
              <span class="phd-title">Bài học</span>
              <span class="phd-sub"> — {{ selectedCourse?.title || 'Chọn khóa học' }}</span>
            </div>
            <div class="hd-right">
              <select v-model="lessonCourseId" @change="onLessonCourseChange" class="sel">
                <option value="">-- Chọn khóa học --</option>
                <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.title }}</option>
              </select>
              <button v-if="selectedCourse" @click="openLessonModal()" class="btn btn-accent btn-sm">+ Thêm</button>
            </div>
          </template>

          <template v-else-if="activeTab==='quiz'">
            <span class="phd-title">🧩 Quiz của tôi</span>
          </template>

          <template v-else-if="activeTab==='students'">
            <span class="phd-title">Học viên</span>
            <select v-model="studentCourseId" @change="onStudentCourseChange" class="sel sel-lg">
              <option value="">-- Chọn khóa học --</option>
              <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.title }}</option>
            </select>
          </template>

          <template v-else-if="activeTab==='qa'">
            <span class="phd-title">💬 Hỏi đáp học viên</span>
            <div class="hd-right">
              <button :class="['ab','ab-filter',!qaFilter&&'ab-filter-active']"
                      @click="qaFilter=false;loadQA()">⏳ Chưa trả lời</button>
              <button :class="['ab','ab-filter',qaFilter&&'ab-filter-active']"
                      @click="qaFilter=true;loadQA()">✅ Đã giải quyết</button>
            </div>
          </template>
        </div>

        <!-- Panel body -->
        <div class="panel-body">

          <!-- ════ DASHBOARD ════ -->
          <div v-show="activeTab==='dashboard'">
            <div v-if="loadingStats" class="center-state"><div class="spinner"></div><p>Đang tải...</p></div>
            <div v-else class="db-wrap">
              <!-- KPI cards -->
              <div class="kpi-row">
                <div class="kpi-card">
                  <div class="kpi-icon" style="background:#dbeafe">📚</div>
                  <div><div class="kpi-n">{{ stats.totalCourses ?? '—' }}</div><div class="kpi-l">Khóa học</div></div>
                </div>
                <div class="kpi-card">
                  <div class="kpi-icon" style="background:#d1fae5">✅</div>
                  <div><div class="kpi-n" style="color:var(--green)">{{ stats.activeCourses ?? '—' }}</div><div class="kpi-l">Đang active</div></div>
                </div>
                <div class="kpi-card">
                  <div class="kpi-icon" style="background:#ede9fe">👥</div>
                  <div><div class="kpi-n" style="color:var(--purple)">{{ stats.totalStudents ?? '—' }}</div><div class="kpi-l">Tổng học viên</div></div>
                </div>
                <div class="kpi-card">
                  <div class="kpi-icon" style="background:#fef9c3">📖</div>
                  <div><div class="kpi-n" style="color:#ca8a04">{{ stats.totalLessons ?? '—' }}</div><div class="kpi-l">Tổng bài học</div></div>
                </div>
              </div>
              <!-- Quick course table -->
              <div class="chart-card">
                <div class="chart-hd">📚 Danh sách khóa học</div>
                <div v-if="!courses.length" class="center-state" style="height:90px">
                  <p>Chưa có khóa học nào.</p>
                </div>
                <table v-else class="tbl">
                  <thead><tr><th>Tên khóa học</th><th>Trạng thái</th><th>Học viên</th><th>Bài học</th></tr></thead>
                  <tbody>
                  <tr v-for="c in courses" :key="c.id">
                    <td><div class="ct">{{ c.title }}</div></td>
                    <td><span :class="['badge', c.status==='ACTIVE'?'badge-green':c.status==='DRAFT'?'badge-yellow':'badge-gray']">{{ c.status }}</span></td>
                    <td><span class="badge badge-blue">{{ studentCounts[c.id] ?? '...' }}</span></td>
                    <td><span class="badge badge-purple">{{ lessonCounts[c.id] ?? '...' }}</span></td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- ════ COURSES ════ -->
          <div v-show="activeTab==='courses'" class="tab-inner">
            <div class="filter-bar">
              <div class="search-wrap">
                <svg class="search-ico" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="search" placeholder="Tìm khóa học..." class="filter-input"/>
              </div>
            </div>
            <div v-if="loadingCourses" class="center-state"><div class="spinner"></div></div>
            <div v-else-if="!filteredCourses.length" class="center-state">
              <div class="empty-ico">📭</div>
              <p>{{ courses.length ? 'Không tìm thấy kết quả' : 'Chưa có khóa học nào' }}</p>
            </div>
            <table v-else class="tbl">
              <thead><tr><th>ID</th><th>Tên khóa học</th><th>Giảng viên</th><th>TC</th><th>Bài học</th><th>Học viên</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
              <tbody>
              <tr v-for="c in filteredCourses" :key="c.id">
                <td class="id-col">#{{ c.id }}</td>
                <td>
                  <div class="ct">{{ c.title }}</div>
                  <div class="cs">{{ c.description?.slice(0,60) }}{{ c.description?.length>60?'...':'' }}</div>
                </td>
                <td>{{ c.instructor || '—' }}</td>
                <td><span class="badge badge-blue">{{ c.credits }}</span></td>
                <td>
                  <button class="ab" @click="goToLessons(c)">📖 {{ lessonCounts[c.id] ?? '...' }}</button>
                </td>
                <td>
                  <button class="ab" @click="goToStudents(c)">👥 {{ studentCounts[c.id] ?? '...' }}</button>
                </td>
                <td>
                  <span :class="['badge', c.status==='ACTIVE'?'badge-green':c.status==='DRAFT'?'badge-yellow':'badge-gray']">
                    {{ c.status }}
                  </span>
                </td>
                <td>
                  <div class="acts">
                    <button @click="openCourseModal(c)" class="ab">✏️ Sửa</button>
                    <button @click="deleteCourse(c)" class="ab ab-red">🗑️</button>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- ════ LESSONS ════ -->
          <div v-show="activeTab==='lessons'" class="tab-inner">
            <div v-if="!selectedCourse" class="center-state"><div class="empty-ico">📖</div><p>Chọn khóa học ở trên</p></div>
            <div v-else-if="loadingLessons" class="center-state"><div class="spinner"></div></div>
            <div v-else-if="!lessons.length" class="center-state">
              <div class="empty-ico">📝</div>
              <p>Chưa có bài học nào</p>
              <button class="btn btn-accent btn-sm" style="margin-top:.5rem" @click="openLessonModal()">+ Thêm bài học</button>
            </div>
            <div v-else class="lesson-list">
              <div v-for="l in lessons" :key="l.id" class="lcard">
                <div class="l-num">{{ l.orderNum }}</div>
                <div class="l-body">
                  <div class="l-title">{{ l.title }}</div>
                  <div class="l-meta">
                    <span v-if="l.videoUrl" class="l-tag l-tag-video">🎬 Có video</span>
                    <span v-if="l.content"  class="l-tag l-tag-blue">📄 Có nội dung</span>
                  </div>
                  <div v-if="l.content" class="l-content">{{ l.content.slice(0,100) }}{{ l.content.length>100?'...':'' }}</div>
                  <a v-if="l.videoUrl" :href="l.videoUrl" target="_blank" class="l-video">🎥 {{ l.videoUrl }}</a>
                </div>
                <div class="acts">
                  <button @click="openLessonModal(l)" class="ab">✏️ Sửa</button>
                  <button @click="deleteLesson(l)" class="ab ab-red">🗑️</button>
                </div>
              </div>
            </div>
          </div>

          <!-- ════ QUIZ ════ -->
          <div v-show="activeTab==='quiz'" class="tab-inner">
            <div v-if="!showQuizEditor">
              <div class="quiz-tab-actions">
                <div class="filter-bar">
                  <div class="search-wrap">
                    <svg class="search-ico" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                    <input v-model="quizSearch" placeholder="Tìm quiz theo tên..." class="filter-input"/>
                  </div>
                  <select v-model="quizCourseFilter" @change="onQuizCourseFilter" class="sel">
                    <option value="">-- Tất cả khóa học --</option>
                    <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.title }}</option>
                  </select>
                </div>
                <button @click="openQuizCreate" class="btn btn-accent btn-sm">+ Tạo Quiz mới</button>
              </div>
              <div v-if="loadingQuizzes" class="center-state"><div class="spinner"></div></div>
              <div v-else-if="!filteredQuizzes.length" class="center-state">
                <div class="empty-ico">🧩</div>
                <p>{{ quizList.length ? 'Không tìm thấy quiz phù hợp' : 'Chưa có quiz. Nhấn "Tạo Quiz mới"!' }}</p>
              </div>
              <table v-else class="tbl">
                <thead><tr><th>ID</th><th>Tiêu đề Quiz</th><th>Bài học</th><th>Khóa học</th><th>Câu hỏi</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                <tbody>
                <tr v-for="q in filteredQuizzes" :key="q.id">
                  <td class="id-col">#{{ q.id }}</td>
                  <td><div class="ct">{{ q.title }}</div><div class="cs">{{ q.description }}</div></td>
                  <td>{{ q.lessonTitle||'—' }}</td>
                  <td>{{ q.courseTitle||'—' }}</td>
                  <td><span class="badge badge-blue">{{ q.questionCount }} câu</span></td>
                  <td><span :class="['badge', q.isActive?'badge-green':'badge-gray']">{{ q.isActive?'Hoạt động':'Ẩn' }}</span></td>
                  <td><div class="acts">
                    <button @click="openQuizEdit(q)" class="ab">✏️ Sửa</button>
                    <button @click="delQuiz(q.id)" class="ab ab-red">🗑️</button>
                  </div></td>
                </tr>
                </tbody>
              </table>
            </div>
            <!-- Quiz Editor -->
            <div v-else>
              <div class="quiz-editor-topbar">
                <button @click="showQuizEditor=false" class="btn btn-ghost btn-sm">← Quay lại danh sách</button>
                <span class="phd-sub">{{ editingQuiz ? '✏️ Chỉnh sửa quiz' : '➕ Tạo quiz mới' }}</span>
              </div>
              <QuizEditor :existingQuiz="editingQuiz" @saved="onQuizSaved" @cancel="showQuizEditor=false"/>
            </div>
          </div>

          <!-- ════ STUDENTS ════ -->
          <div v-show="activeTab==='students'" class="tab-inner">
            <div v-if="!studentCourseId" class="center-state"><div class="empty-ico">👥</div><p>Chọn khóa học để xem học viên</p></div>
            <div v-else-if="loadingStudents" class="center-state"><div class="spinner"></div></div>
            <div v-else-if="!students.length" class="center-state"><div class="empty-ico">👤</div><p>Chưa có học viên đăng ký</p></div>
            <template v-else>
              <div class="mini-stats">
                <div class="ms-it"><span class="ms-n">{{ students.length }}</span><span class="ms-l">Tổng học viên</span></div>
              </div>
              <table class="tbl">
                <thead><tr><th>#</th><th>Học viên</th><th>Email</th><th>Ngày đăng ký</th></tr></thead>
                <tbody>
                <tr v-for="(s, idx) in students" :key="s.id">
                  <td class="id-col">{{ idx+1 }}</td>
                  <td>
                    <div class="ucell">
                      <div class="uav">{{ s.username?.slice(0,1).toUpperCase() }}</div>
                      <div class="uname">{{ s.username }}</div>
                    </div>
                  </td>
                  <td class="muted-col">{{ s.email }}</td>
                  <td class="muted-col">{{ s.enrolledAt ? new Date(s.enrolledAt).toLocaleDateString('vi-VN') : '—' }}</td>
                </tr>
                </tbody>
              </table>
            </template>
          </div>


          <!-- ════ Q&A INBOX ════ -->
          <div v-show="activeTab==='qa'" class="tab-inner">
            <div v-if="loadingQA" class="center-state"><div class="spinner"></div></div>
            <div v-else-if="!qaList.length" class="center-state">
              <div class="empty-ico">💬</div>
              <p>{{ qaFilter ? 'Không có câu hỏi đã giải quyết' : 'Tất cả câu hỏi đã được xử lý 🎉' }}</p>
            </div>
            <div v-else class="qa-inbox">
              <div v-for="q in qaList" :key="q.id" class="qi-card" :class="{resolved:q.resolved}">
                <div class="qi-head">
                  <div class="uav" style="width:28px;height:28px;font-size:.7rem;flex-shrink:0">
                    {{ (q.author||'?')[0].toUpperCase() }}
                  </div>
                  <div class="qi-meta">
                    <span class="uname">{{ q.author }}</span>
                    <span class="qi-ctx">📖 {{ q.lessonTitle }} &nbsp;·&nbsp; 📚 {{ q.courseTitle }}</span>
                    <span class="uid">{{ new Date(q.createdAt).toLocaleString('vi-VN') }}</span>
                  </div>
                  <div class="acts" style="margin-left:auto;flex-shrink:0">
                    <span v-if="q.resolved" class="badge badge-green" style="font-size:.65rem">✅ Xử lý</span>
                    <button @click="toggleQAResolve(q)" class="ab" style="font-size:.71rem"
                            :title="q.resolved?'Bỏ giải quyết':'Đánh dấu đã xử lý'">
                      {{ q.resolved ? '↩' : '✓' }}
                    </button>
                    <button @click="delQA(q.id)" class="ab ab-red" style="font-size:.71rem">🗑</button>
                  </div>
                </div>
                <p class="qi-body">{{ q.content }}</p>

                <div v-if="q.replies?.length" class="qi-replies">
                  <div v-for="r in q.replies" :key="r.id" class="qi-reply" :class="{official:r.official}">
                    <div class="uav" style="width:22px;height:22px;font-size:.62rem;flex-shrink:0;background:linear-gradient(135deg,#059669,#10b981)">
                      {{ (r.author||'?')[0].toUpperCase() }}
                    </div>
                    <div class="qi-rb">
                      <div style="display:flex;align-items:center;gap:.35rem;margin-bottom:.15rem;flex-wrap:wrap">
                        <span class="uname" style="font-size:.78rem">{{ r.author }}</span>
                        <span v-if="r.official" class="badge badge-blue" style="font-size:.62rem">
                          {{ r.role==='ADMIN'?'🛡 Admin':'👨‍🏫 GV' }}
                        </span>
                      </div>
                      <p class="qi-rtext">{{ r.content }}</p>
                    </div>
                  </div>
                </div>

                <div style="margin-top:.5rem">
                  <textarea v-if="qaReplyOpen[q.id]" v-model="qaReplyText[q.id]"
                            class="qi-textarea" rows="2"
                            placeholder="Viết câu trả lời của bạn..."
                            :ref="el => el && el.focus()"></textarea>
                  <div style="display:flex;gap:.4rem;margin-top:.35rem">
                    <button v-if="!qaReplyOpen[q.id]"
                            @click="qaReplyOpen[q.id]=true;qaReplyText[q.id]=''"
                            class="btn btn-accent btn-sm">✏️ Trả lời</button>
                    <template v-else>
                      <button class="btn btn-ghost btn-sm" @click="qaReplyOpen[q.id]=false">Hủy</button>
                      <button class="btn btn-accent btn-sm" @click="submitQAReply(q)"
                              :disabled="!qaReplyText[q.id]?.trim()||qaReplying[q.id]">
                        {{ qaReplying[q.id]?'…':'Gửi trả lời' }}
                      </button>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div><!-- /panel-body -->
      </div><!-- /panel -->
    </div><!-- /admin-shell -->

    <!-- ── MODAL KHÓA HỌC ── -->
    <div v-if="showCourseModal" class="modal-overlay" @click.self="showCourseModal=false">
      <div class="modal">
        <h2>{{ editingCourse ? '✏️ Sửa khóa học' : '➕ Tạo khóa học mới' }}</h2>
        <div class="form-group"><label>Tên khóa học *</label><input v-model="courseForm.title" placeholder="VD: Lập trình Java cơ bản"/></div>
        <div class="form-group"><label>Mô tả</label><textarea v-model="courseForm.description" rows="3" placeholder="Mô tả ngắn..."></textarea></div>
        <div class="f2">
          <div class="form-group"><label>Tên giảng viên</label><input v-model="courseForm.instructor" :placeholder="auth.user?.username"/></div>
          <div class="form-group"><label>Số tín chỉ *</label><input v-model.number="courseForm.credits" type="number" min="1" max="10"/></div>
        </div>
        <div class="form-group">
          <label>Trạng thái</label>
          <select v-model="courseForm.status">
            <option value="DRAFT">📝 Draft</option>
            <option value="ACTIVE">✅ Active</option>
            <option value="INACTIVE">⏸ Inactive</option>
          </select>
        </div>
        <!-- Giá khóa học -->
        <div class="form-group">
          <label style="display:flex;align-items:center;gap:.6rem;cursor:pointer;font-weight:600">
            <input type="checkbox" v-model="courseForm.isFree"
                   style="width:15px;height:15px;cursor:pointer;accent-color:var(--accent)"/>
            Khóa học miễn phí
          </label>
        </div>
        <div v-if="!courseForm.isFree" class="form-group">
          <label>Học phí (VNĐ) *</label>
          <input v-model.number="courseForm.price" type="number" min="1000" step="1000"
                 placeholder="VD: 299000"/>
          <div class="field-hint">Nhập số nguyên, đơn vị VNĐ. VD: 299000 = 299.000 đ</div>
        </div>
        <div v-if="modalError" class="msg-error">⚠️ {{ modalError }}</div>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showCourseModal=false">Hủy</button>
          <button class="btn btn-accent" @click="saveCourse" :disabled="saving">{{ saving ? '...' : (editingCourse ? 'Lưu thay đổi' : 'Tạo khóa học') }}</button>
        </div>
      </div>
    </div>

    <!-- ── MODAL BÀI HỌC ── -->
    <div v-if="showLessonModal" class="modal-overlay" @click.self="showLessonModal=false">
      <div class="modal">
        <h2>{{ editingLesson ? '✏️ Sửa bài học' : '➕ Thêm bài học mới' }}</h2>
        <div class="form-group"><label>Tiêu đề bài học *</label><input v-model="lessonForm.title" placeholder="VD: Bài 1: Giới thiệu Java"/></div>
        <div class="f2">
          <div class="form-group"><label>Thứ tự</label><input v-model.number="lessonForm.orderNum" type="number" min="1"/></div>
          <div class="form-group"><label>Video URL (tùy chọn)</label><input v-model="lessonForm.videoUrl" placeholder="https://youtube.com/..."/></div>
        </div>
        <div class="form-group"><label>Nội dung bài học</label><textarea v-model="lessonForm.content" rows="4" placeholder="Nội dung chi tiết, hướng dẫn..."></textarea></div>
        <div class="form-group">
          <label>Video bài học</label>
          <VideoUploader v-model="lessonForm.videoUrl" :lessonId="editingLesson?.id ?? null"/>
          <div v-if="!editingLesson?.id" class="field-hint">
            💡 Để upload file video, hãy lưu bài học trước, sau đó mở lại để upload.
          </div>
        </div>
        <div v-if="modalError" class="msg-error">⚠️ {{ modalError }}</div>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showLessonModal=false">Hủy</button>
          <button class="btn btn-accent" @click="saveLesson" :disabled="saving">{{ saving ? '...' : (editingLesson ? 'Lưu thay đổi' : 'Thêm bài học') }}</button>
        </div>
      </div>
    </div>

    <div v-if="toast.show" :class="['toast', toast.type]">{{ toast.text }}</div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import api from '../services/api'
import { useAuthStore } from '../stores/auth'
import VideoUploader from '../components/VideoUploader.vue'
import QuizEditor from '../components/QuizEditor.vue'

const auth = useAuthStore()

// ── Tabs ──
const activeTab = ref('dashboard')
const tabs = [
  { key: 'dashboard', icon: '📊', label: 'Dashboard' },
  { key: 'courses',   icon: '📚', label: 'Khóa học'  },
  { key: 'lessons',   icon: '📖', label: 'Bài học'   },
  { key: 'quiz',      icon: '❓', label: 'Quiz'      },
  { key: 'students',  icon: '👥', label: 'Học viên'  },
  { key: 'qa',        icon: '💬', label: 'Hỏi đáp'   },
]

// ── State ──
const courses        = ref([])
const lessons        = ref([])
const students       = ref([])
const selectedCourse = ref(null)
const search         = ref('')
const lessonCourseId  = ref('')
const studentCourseId = ref('')

const showQuizEditor  = ref(false)
// ── Q&A ──
const qaList      = ref([])
const loadingQA   = ref(false)
const qaFilter    = ref(false)
const qaReplyOpen = {}
const qaReplyText = {}
const qaReplying  = {}
const editingQuiz     = ref(null)
const quizList        = ref([])
const loadingQuizzes  = ref(false)
const quizSearch      = ref('')
const quizCourseFilter = ref('')

const loadingStats    = ref(false)
const loadingCourses  = ref(true)
const loadingLessons  = ref(false)
const loadingStudents = ref(false)

const stats         = reactive({})
const studentCounts = reactive({})
const lessonCounts  = reactive({})

const showCourseModal = ref(false)
const showLessonModal = ref(false)
const editingCourse   = ref(null)
const editingLesson   = ref(null)
const saving          = ref(false)
const modalError      = ref('')
const toast           = reactive({ show: false, text: '', type: 'success' })

const courseForm = reactive({ title: '', description: '', instructor: '', credits: 3, status: 'DRAFT', price: 0, isFree: true })
const lessonForm = reactive({ title: '', content: '', videoUrl: '', orderNum: 1 })

// ── Computed ──
const filteredCourses = computed(() =>
    courses.value.filter(c => !search.value || c.title.toLowerCase().includes(search.value.toLowerCase()))
)

const filteredQuizzes = computed(() => {
  const q = quizSearch.value.toLowerCase()
  return quizList.value.filter(quiz =>
      (!q || (quiz.title||'').toLowerCase().includes(q)) &&
      (!quizCourseFilter.value || quiz.courseId == quizCourseFilter.value)
  )
})

// ── Navigation helpers ──
async function loadQA() {
  loadingQA.value = true
  try {
    const r = await api.get(`/instructor/questions/inbox?resolved=${qaFilter.value}`)
    qaList.value = Array.isArray(r.data) ? r.data : []
  } catch { showToast('Lỗi tải câu hỏi!','error') }
  finally { loadingQA.value = false }
}
async function toggleQAResolve(q) {
  try {
    const r = await api.patch(`/questions/${q.id}/resolve`)
    q.resolved = r.data.resolved
    if (!qaFilter.value && q.resolved)
      setTimeout(() => { qaList.value = qaList.value.filter(x => x.id !== q.id) }, 700)
  } catch { showToast('Lỗi!','error') }
}
async function delQA(qId) {
  if (!confirm('Xóa câu hỏi này?')) return
  try {
    await api.delete(`/questions/${qId}`)
    qaList.value = qaList.value.filter(q => q.id !== qId)
    showToast('Đã xóa')
  } catch(e) { showToast(e.response?.data?.error||'Lỗi!','error') }
}
async function submitQAReply(q) {
  const content = (qaReplyText[q.id]||'').trim()
  if (!content) return
  qaReplying[q.id] = true
  try {
    const r = await api.post(`/questions/${q.id}/replies`, { content })
    if (!q.replies) q.replies = []
    q.replies.push(r.data)
    if (r.data.official) q.resolved = true
    qaReplyOpen[q.id] = false; qaReplyText[q.id] = ''
    showToast('Đã gửi câu trả lời!')
    if (!qaFilter.value && q.resolved)
      setTimeout(() => { qaList.value = qaList.value.filter(x => x.id !== q.id) }, 800)
  } catch(e) { showToast(e.response?.data?.error||'Lỗi!','error') }
  finally { qaReplying[q.id] = false }
}

function switchTab(key) {
  activeTab.value = key
  if (key === 'qa') loadQA()
  if (key === 'quiz') { showQuizEditor.value = false; loadQuizzes() }
}

function goToLessons(course) {
  selectedCourse.value = course
  lessonCourseId.value  = course.id
  activeTab.value = 'lessons'
  loadLessons()
}

function goToStudents(course) {
  studentCourseId.value = course.id
  activeTab.value = 'students'
  loadStudentsForCourse(course.id)
}

function onLessonCourseChange() {
  const c = courses.value.find(c => c.id == lessonCourseId.value)
  selectedCourse.value = c || null
  if (c) loadLessons()
  else lessons.value = []
}

function onStudentCourseChange() {
  if (studentCourseId.value) loadStudentsForCourse(studentCourseId.value)
  else students.value = []
}

// ── Load ──
async function loadStats() {
  loadingStats.value = true
  try { const r = await api.get('/instructor/dashboard'); Object.assign(stats, r.data) }
  catch {} finally { loadingStats.value = false }
}

async function loadCourses() {
  loadingCourses.value = true
  try {
    const r = await api.get('/instructor/courses')
    courses.value = r.data
    for (const c of r.data) {
      loadStudentCount(c.id)
      loadLessonCount(c.id)
    }
  } catch {} finally { loadingCourses.value = false }
}

async function loadStudentCount(courseId) {
  try { const r = await api.get(`/instructor/courses/${courseId}/students`); studentCounts[courseId] = r.data.total }
  catch { studentCounts[courseId] = 0 }
}

async function loadLessonCount(courseId) {
  try { const r = await api.get(`/instructor/courses/${courseId}/lessons`); lessonCounts[courseId] = r.data.length }
  catch { lessonCounts[courseId] = 0 }
}

async function loadLessons() {
  if (!selectedCourse.value) return
  loadingLessons.value = true
  try {
    const r = await api.get(`/instructor/courses/${selectedCourse.value.id}/lessons`)
    lessons.value = r.data.sort((a, b) => a.orderNum - b.orderNum)
  } catch { lessons.value = [] } finally { loadingLessons.value = false }
}

async function loadStudentsForCourse(courseId) {
  loadingStudents.value = true
  try {
    const r = await api.get(`/instructor/courses/${courseId}/students`)
    students.value = r.data.students
  } catch { students.value = [] } finally { loadingStudents.value = false }
}

// ── Course CRUD ──
function openCourseModal(course = null) {
  editingCourse.value = course
  modalError.value = ''
  if (course) {
    Object.assign(courseForm, { title: course.title, description: course.description || '', instructor: course.instructor || '', credits: course.credits || 3, status: course.status || 'DRAFT' , price: course.price || 0, isFree: course.isFree !== false })
  } else {
    Object.assign(courseForm, { title: '', description: '', instructor: auth.user?.username || '', credits: 3, status: 'DRAFT' , price: 0, isFree: true })
  }
  showCourseModal.value = true
}

async function saveCourse() {
  if (!courseForm.title.trim()) { modalError.value = 'Tên khóa học không được để trống!'; return }
  saving.value = true; modalError.value = ''
  try {
    if (editingCourse.value) {
      await api.put(`/instructor/courses/${editingCourse.value.id}`, { ...courseForm })
      showToast('Cập nhật khóa học thành công!')
    } else {
      await api.post('/instructor/courses', { ...courseForm })
      showToast('Tạo khóa học thành công!')
    }
    showCourseModal.value = false
    await loadCourses(); await loadStats()
  } catch (e) { modalError.value = e.response?.data?.error || 'Lỗi! Vui lòng thử lại.' }
  finally { saving.value = false }
}

async function deleteCourse(course) {
  if (!confirm(`Xóa khóa học "${course.title}"?\nTất cả bài học sẽ bị xóa!`)) return
  try {
    await api.delete(`/instructor/courses/${course.id}`)
    if (selectedCourse.value?.id === course.id) selectedCourse.value = null
    showToast('Đã xóa khóa học!')
    await loadCourses(); await loadStats()
  } catch (e) { showToast(e.response?.data?.error || 'Xóa thất bại!', 'error') }
}

// ── Lesson CRUD ──
function openLessonModal(lesson = null) {
  editingLesson.value = lesson
  modalError.value = ''
  if (lesson) {
    Object.assign(lessonForm, { title: lesson.title, content: lesson.content || '', videoUrl: lesson.videoUrl || '', orderNum: lesson.orderNum || 1 })
  } else {
    const nextOrder = lessons.value.length > 0 ? Math.max(...lessons.value.map(l => l.orderNum)) + 1 : 1
    Object.assign(lessonForm, { title: '', content: '', videoUrl: '', orderNum: nextOrder })
  }
  showLessonModal.value = true
}

async function saveLesson() {
  if (!lessonForm.title.trim()) { modalError.value = 'Tiêu đề bài học không được để trống!'; return }
  saving.value = true; modalError.value = ''
  try {
    if (editingLesson.value) {
      await api.put(`/instructor/lessons/${editingLesson.value.id}`, { ...lessonForm })
      showToast('Cập nhật bài học thành công!')
    } else {
      await api.post(`/instructor/courses/${selectedCourse.value.id}/lessons`, { ...lessonForm })
      showToast('Thêm bài học thành công!')
    }
    showLessonModal.value = false
    await loadLessons()
    loadLessonCount(selectedCourse.value.id)
    await loadStats()
  } catch (e) { modalError.value = e.response?.data?.error || 'Lỗi! Vui lòng thử lại.' }
  finally { saving.value = false }
}

async function deleteLesson(lesson) {
  if (!confirm(`Xóa bài học "${lesson.title}"?`)) return
  try {
    await api.delete(`/instructor/lessons/${lesson.id}`)
    showToast('Đã xóa bài học!')
    await loadLessons()
    loadLessonCount(selectedCourse.value.id)
    await loadStats()
  } catch (e) { showToast(e.response?.data?.error || 'Xóa thất bại!', 'error') }
}

// ── Quiz ──
async function loadQuizzes() {
  loadingQuizzes.value = true
  try {
    const url = quizCourseFilter.value
        ? `/quizzes/admin/course/${quizCourseFilter.value}`
        : '/quizzes/admin/all'
    quizList.value = (await api.get(url)).data || []
  } catch {} finally { loadingQuizzes.value = false }
}
function onQuizCourseFilter() { quizList.value = []; loadQuizzes() }
function openQuizCreate() { editingQuiz.value = null; showQuizEditor.value = true }
async function openQuizEdit(q) {
  try { editingQuiz.value = (await api.get(`/quizzes/${q.id}/admin/detail`)).data }
  catch { editingQuiz.value = q }
  showQuizEditor.value = true
}
async function delQuiz(id) {
  if (!confirm('Xóa quiz này?')) return
  try { await api.delete(`/quizzes/${id}`); showToast('Đã xóa quiz!'); loadQuizzes() }
  catch { showToast('Xóa thất bại!', 'error') }
}
function onQuizSaved() { showToast('Lưu quiz thành công!'); showQuizEditor.value = false; loadQuizzes() }

// ── Helpers ──
function showToast(text, type = 'success') {
  toast.text = text; toast.type = type; toast.show = true
  setTimeout(() => toast.show = false, 3000)
}

onMounted(() => { loadStats(); loadCourses() })
</script>

<style scoped>
/* ════ Layout — giống hệt AdminView ════ */
.page {
  padding: 0;
  max-width: 100%;
  margin: 0;
  height: calc(100vh - 62px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.page-header {
  flex-shrink: 0;
  padding: 1.2rem 2.5rem .8rem;
  border-bottom: 1.5px solid var(--border);
  background: var(--surface);
}
.page-header h1 { font-size:1.4rem; font-weight:800; margin-bottom:.15rem; }
.page-header p  { color:var(--muted); font-size:.82rem; }

.admin-shell {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: var(--surface);
}

/* ── Sidebar ── */
.sidebar {
  width: 160px; flex-shrink: 0;
  background: var(--surface2);
  border-right: 1.5px solid var(--border);
  display: flex; flex-direction: column;
  padding: .6rem .5rem; gap: .2rem;
}
.stab {
  display:flex; align-items:center; gap:.5rem;
  padding:.6rem .75rem; border-radius:8px; border:none;
  background:transparent; color:var(--muted); font-size:.81rem;
  font-weight:500; cursor:pointer; transition:all .15s;
  font-family:'Plus Jakarta Sans',sans-serif;
  width:100%; text-align:left; flex-shrink:0;
}
.stab:hover  { background:rgba(0,0,0,.04); color:var(--text2); }
.stab.active { background:var(--accent); color:#fff; font-weight:700; }
.stab-label  { white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }

/* ── Panel ── */
.panel {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column;
  height: 100%; overflow: hidden;
}
.panel-hd {
  flex-shrink: 0;
  height: 58px;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 1.4rem;
  border-bottom: 1.5px solid var(--border);
  gap: 1rem;
}
.phd-title { font-size:.95rem; font-weight:700; }
.phd-sub   { font-size:.84rem; color:var(--muted); }
.hd-right  { display:flex; gap:.6rem; align-items:center; }

.panel-body {
  flex: 1; min-height: 0;
  overflow-y: auto;
  padding: 1.1rem 1.4rem;
}

/* ── Dashboard ── */
.db-wrap { display:flex; flex-direction:column; gap:.9rem; }
.kpi-row { display:grid; grid-template-columns:repeat(4,1fr); gap:.7rem; }
.kpi-card {
  background:var(--surface2); border:1.5px solid var(--border);
  border-radius:12px; padding:.9rem 1rem;
  display:flex; align-items:center; gap:.75rem;
}
.kpi-icon { width:38px; height:38px; border-radius:9px; display:flex; align-items:center; justify-content:center; font-size:1.1rem; flex-shrink:0; }
.kpi-n { font-size:1.35rem; font-weight:800; line-height:1.1; }
.kpi-l { font-size:.7rem; color:var(--muted); margin-top:.1rem; }
.chart-card { background:var(--surface2); border:1.5px solid var(--border); border-radius:12px; padding:.9rem 1rem; }
.chart-hd   { font-size:.8rem; font-weight:700; color:var(--text2); margin-bottom:.65rem; }

/* ── Tab inner ── */
.tab-inner { display:flex; flex-direction:column; gap:.8rem; }

/* ── Filter bar ── */
.filter-bar { display:flex; gap:.6rem; flex-wrap:wrap; flex-shrink:0; }
.search-wrap  { position:relative; }
.search-ico   { position:absolute; left:.7rem; top:50%; transform:translateY(-50%); color:var(--muted); pointer-events:none; }
.filter-input {
  padding:.44rem .85rem .44rem 2.1rem;
  background:var(--surface); border:1.5px solid var(--border);
  border-radius:8px; color:var(--text); font-size:.82rem;
  outline:none; font-family:'Plus Jakarta Sans',sans-serif; width:240px;
}
.filter-input:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }

/* ── Table ── */
.tbl { width:100%; border-collapse:collapse; font-size:.83rem; }
.tbl th {
  background:#f8fafc; color:var(--muted); padding:.55rem .9rem;
  text-align:left; font-size:.7rem; font-weight:700;
  text-transform:uppercase; letter-spacing:.06em;
  border-bottom:1.5px solid var(--border); white-space:nowrap;
  position:sticky; top:0; z-index:1;
}
.tbl td { padding:.68rem .9rem; border-bottom:1px solid var(--border); vertical-align:middle; }
.tbl tr:last-child td { border-bottom:none; }
.tbl tr:hover td { background:#f8fafc; }
.id-col    { color:var(--muted); font-size:.74rem; font-family:monospace; white-space:nowrap; }
.muted-col { font-size:.79rem; color:var(--muted); }
.empty-cell{ text-align:center; padding:2rem; color:var(--muted); }
.ct { font-weight:600; font-size:.84rem; }
.cs { font-size:.73rem; color:var(--muted); max-width:200px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.ucell { display:flex; align-items:center; gap:.55rem; }
.uav   { width:28px; height:28px; border-radius:50%; background:linear-gradient(135deg,var(--accent),#7c3aed); color:#fff; font-size:.75rem; font-weight:700; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.uname { font-weight:700; font-size:.84rem; }

/* ── Action buttons ── */
.acts  { display:flex; gap:.28rem; flex-wrap:wrap; }
.ab {
  padding:.25rem .58rem; font-size:.73rem; font-weight:600;
  border-radius:6px; cursor:pointer;
  border:1.5px solid var(--border); background:var(--surface); color:var(--text2);
  font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; white-space:nowrap;
}
.ab:hover        { border-color:var(--accent); color:var(--accent); background:var(--accent-light); }
.ab.ab-red       { }
.ab.ab-red:hover { border-color:var(--red); color:var(--red); background:var(--red-light); }

/* ── Badges ── */
.badge        { font-size:.66rem; font-weight:700; padding:.17rem .52rem; border-radius:100px; }
.badge-green  { background:var(--green-light);  color:var(--green);  border:1px solid #a7f3d0; }
.badge-blue   { background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; }
.badge-purple { background:#f3e8ff; color:#7c3aed; border:1px solid #ddd6fe; }
.badge-yellow { background:#fef9c3; color:#a16207; border:1px solid #fde68a; }
.badge-gray   { background:#f1f5f9; color:var(--muted); border:1px solid var(--border); }

/* ── Lesson list ── */
.lesson-list { display:flex; flex-direction:column; gap:.5rem; }
.lcard {
  background:var(--surface2); border:1.5px solid var(--border);
  border-radius:10px; padding:.8rem 1rem;
  display:flex; align-items:flex-start; gap:.8rem;
}
.lcard:hover { border-color:var(--accent); }
.l-num {
  width:25px; height:25px; border-radius:7px;
  background:var(--accent-light); color:var(--accent);
  display:flex; align-items:center; justify-content:center;
  font-size:.74rem; font-weight:700; flex-shrink:0; margin-top:.1rem;
}
.l-body  { flex:1; min-width:0; }
.l-title { font-size:.86rem; font-weight:600; margin-bottom:.25rem; }
.l-meta  { display:flex; gap:.3rem; flex-wrap:wrap; margin-bottom:.25rem; }
.l-tag   { font-size:.65rem; font-weight:700; padding:.1rem .4rem; border-radius:100px; }
.l-tag-video { background:#dcfce7; color:#16a34a; }
.l-tag-blue  { background:#eff6ff; color:var(--accent); }
.l-content { font-size:.75rem; color:var(--muted); display:-webkit-box; -webkit-line-clamp:1; -webkit-box-orient:vertical; overflow:hidden; }
.l-video   { font-size:.72rem; color:var(--accent); display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; margin-top:.1rem; }

/* ── Mini stats ── */
.mini-stats {
  display:flex; align-items:center; gap:1.2rem;
  background:var(--surface2); border:1.5px solid var(--border);
  border-radius:10px; padding:.65rem 1rem; flex-shrink:0;
}
.ms-it { display:flex; flex-direction:column; align-items:center; }
.ms-n  { font-size:1.2rem; font-weight:800; }
.ms-l  { font-size:.67rem; color:var(--muted); margin-top:.1rem; }

/* ── Select ── */
.sel    { padding:.44rem .8rem; background:var(--surface); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.82rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; }
.sel:focus { border-color:var(--accent); }
.sel-lg { min-width:200px; }

/* ── Center state ── */
.center-state { display:flex; flex-direction:column; align-items:center; justify-content:center; height:260px; gap:.7rem; color:var(--muted); }
.spinner { width:28px; height:28px; border:3px solid var(--border); border-top-color:var(--accent); border-radius:50%; animation:spin .7s linear infinite; }
@keyframes spin { to{transform:rotate(360deg)} }
.empty-ico { font-size:2rem; opacity:.4; }
.center-state p { font-size:.87rem; }

/* ── Buttons ── */
.btn { display:inline-flex; align-items:center; gap:.35rem; padding:.48rem 1rem; border-radius:8px; font-size:.83rem; font-weight:600; cursor:pointer; border:none; transition:all .15s; font-family:'Plus Jakarta Sans',sans-serif; white-space:nowrap; }
.btn-sm { padding:.36rem .8rem; font-size:.78rem; }
.btn-accent { background:var(--accent); color:#fff; }
.btn-accent:hover:not(:disabled) { background:var(--accent-dark); }
.btn-accent:disabled { opacity:.5; cursor:not-allowed; }
.btn-ghost { background:var(--surface); color:var(--text2); border:1.5px solid var(--border); }
.btn-ghost:hover { border-color:var(--accent); color:var(--accent); }

/* ── Modal ── */
.modal-overlay { position:fixed; inset:0; background:rgba(15,23,42,.45); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:200; padding:1rem; }
.modal { background:var(--surface); border:1px solid var(--border); border-radius:16px; padding:1.8rem; width:100%; max-width:520px; box-shadow:var(--shadow-lg); }
.modal h2 { font-size:1.1rem; font-weight:800; margin-bottom:1.3rem; }
.modal-actions { display:flex; gap:.6rem; justify-content:flex-end; margin-top:1.3rem; }
.form-group { margin-bottom:.9rem; }
.form-group label { display:block; font-size:.73rem; font-weight:700; color:var(--muted); margin-bottom:.36rem; text-transform:uppercase; letter-spacing:.05em; }
.form-group input,
.form-group textarea,
.form-group select {
  width:100%; padding:.6rem .88rem; background:var(--surface);
  border:1.5px solid var(--border); border-radius:8px; color:var(--text);
  font-size:.87rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif;
  box-sizing: border-box;
}
.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }
.form-group textarea { resize:vertical; min-height:80px; }
.f2 { display:grid; grid-template-columns:1fr 1fr; gap:0 1rem; }
.field-hint { font-size:.75rem; color:var(--muted); margin-top:.4rem; background:var(--surface2); border:1px solid var(--border); border-radius:7px; padding:.4rem .7rem; }
.quiz-tab-actions { display:flex; align-items:center; justify-content:space-between; gap:1rem; margin-bottom:.75rem; flex-wrap:wrap; }
.quiz-editor-topbar { display:flex; align-items:center; gap:1rem; margin-bottom:1rem; padding-bottom:.75rem; border-bottom:1.5px solid var(--border); }
.msg-error { font-size:.8rem; color:var(--red); background:var(--red-light); border:1px solid #fca5a5; border-radius:8px; padding:.5rem .8rem; margin-bottom:.5rem; }

/* ── Toast ── */
.toast { position:fixed; bottom:2rem; right:2rem; padding:.72rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .25s ease; border:1.5px solid; box-shadow:var(--shadow); }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light);   color:var(--red);   border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }

/* Q&A Inbox */
.qa-inbox { display:flex; flex-direction:column; gap:.65rem; }
.qi-card  { background:var(--surface); border:1.5px solid var(--border); border-radius:11px; padding:.85rem 1rem; transition:border-color .15s; }
.qi-card:hover { border-color:var(--accent); }
.qi-card.resolved { border-color:#a7f3d0; background:#f0fdf4; opacity:.82; }
.qi-head  { display:flex; align-items:flex-start; gap:.55rem; margin-bottom:.5rem; flex-wrap:wrap; }
.qi-meta  { flex:1; min-width:0; display:flex; flex-direction:column; gap:.1rem; }
.qi-ctx   { font-size:.71rem; color:var(--muted); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.qi-body  { font-size:.83rem; line-height:1.6; margin:0 0 .6rem; white-space:pre-wrap; color:var(--text); }
.qi-replies { padding-left:.8rem; border-left:2px solid var(--border); display:flex; flex-direction:column; gap:.4rem; margin-bottom:.6rem; }
.qi-reply { display:flex; gap:.45rem; align-items:flex-start; }
.qi-reply.official { background:#f0fdf4; border-radius:6px; padding:.35rem .5rem; }
.qi-rb   { flex:1; }
.qi-rtext { font-size:.8rem; line-height:1.5; margin:0; white-space:pre-wrap; color:var(--text); }
.qi-textarea { width:100%; padding:.45rem .7rem; background:var(--surface); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.82rem; font-family:inherit; resize:none; outline:none; box-sizing:border-box; display:block; }
.qi-textarea:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }
.ab-filter-active { background:var(--accent) !important; color:#fff !important; border-color:var(--accent) !important; }

/* QA inbox — thêm classes còn thiếu */
.qi-ctx    { font-size:.71rem; color:var(--muted); margin-top:.1rem; }
.qi-acts   { display:flex; align-items:center; gap:.3rem; flex-shrink:0; margin-left:auto; }
.qi-time   { font-size:.7rem; color:var(--muted); }
.qi-rtext  { font-size:.81rem; color:var(--text); line-height:1.55; margin:0; white-space:pre-wrap; }
.qi-rmeta  { display:flex; align-items:center; gap:.4rem; margin-bottom:.18rem; flex-wrap:wrap; }
.qi-textarea { width:100%; padding:.45rem .75rem; background:var(--surface); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.82rem; font-family:'Plus Jakarta Sans',sans-serif; resize:none; outline:none; transition:border-color .15s; box-sizing:border-box; }
.qi-textarea:focus { border-color:var(--accent); }
.qi-reply-acts { display:flex; gap:.4rem; margin-top:.35rem; justify-content:flex-end; }

</style>