<template>
  <div class="page">
    <div class="page-header">
      <h1>Quản lý hệ thống</h1>
      <p>Admin Dashboard</p>
    </div>

    <div class="admin-shell">
      <div class="sidebar">
        <button v-for="t in tabs" :key="t.key"
                :class="['stab', { active: activeTab===t.key }]"
                @click="switchTab(t.key)">
          <span>{{ t.icon }}</span><span class="stab-label">{{ t.label }}</span>
        </button>
      </div>

      <div class="panel">
        <div class="panel-hd">
          <template v-if="activeTab==='dashboard'">
            <span class="phd-title">📊 Tổng quan hệ thống</span>
            <button @click="loadStats" class="btn btn-ghost btn-sm">↺ Làm mới</button>
          </template>
          <template v-else-if="activeTab==='courses'">
            <span class="phd-title">Danh sách khóa học</span>
            <button @click="openCourseModal()" class="btn btn-accent btn-sm">+ Thêm</button>
          </template>
          <template v-else-if="activeTab==='lessons'">
            <div><span class="phd-title">Bài học</span><span class="phd-sub"> — {{ selCourse?.title||'Chọn khóa học' }}</span></div>
            <div class="hd-right">
              <select v-model="lessonCid" @change="loadLessons" class="sel">
                <option value="">-- Chọn khóa học --</option>
                <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.title }}</option>
              </select>
              <button v-if="lessonCid" @click="openLessonModal()" class="btn btn-accent btn-sm">+ Thêm</button>
            </div>
          </template>
          <template v-else-if="activeTab==='users'">
            <span class="phd-title">Danh sách học viên</span>
          </template>
          <template v-else-if="activeTab==='quiz'">
            <span class="phd-title">🧩 Quản lý Quiz</span>
            <button v-if="!showQuizEditor" @click="openQuizCreate" class="btn btn-accent btn-sm">+ Tạo Quiz</button>
          </template>
          <template v-else-if="activeTab==='certificates'">
            <span class="phd-title">🏆 Quản lý chứng chỉ</span>
          </template>
          <template v-else-if="activeTab==='grades'">
            <span class="phd-title">Điểm số tự động</span>
            <select v-model="enrollCid" @change="loadEnrollments" class="sel sel-lg">
              <option value="">-- Chọn khóa học --</option>
              <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.title }}</option>
            </select>
          </template>
        </div>

        <div class="panel-body">

          <!-- DASHBOARD -->
          <div v-show="activeTab==='dashboard'">
            <div v-if="loadingStats" class="center-state"><div class="spinner"></div><p>Đang tải...</p></div>
            <div v-else-if="stats" class="db-wrap">
              <div class="kpi-row">
                <div class="kpi-card" v-for="k in kpiCards" :key="k.label">
                  <div class="kpi-icon" :style="`background:${k.bg}`">{{ k.icon }}</div>
                  <div><div class="kpi-n">{{ k.value }}</div><div class="kpi-l">{{ k.label }}</div></div>
                </div>
              </div>
              <div class="chart-row">
                <div class="chart-card chart-lg">
                  <div class="chart-hd">📈 Đăng ký theo tháng ({{ currentYear }})</div>
                  <div class="chart-box"><canvas id="adminLineChart"></canvas></div>
                </div>
                <div class="chart-card chart-sm">
                  <div class="chart-hd">🎯 Phân bổ điểm</div>
                  <div class="chart-box-sq"><canvas id="adminDonutChart"></canvas></div>
                  <div class="score-legend">
                    <div class="sl-item"><span class="sl-dot" style="background:#059669"></span>Giỏi ≥8.5</div>
                    <div class="sl-item"><span class="sl-dot" style="background:#2563eb"></span>Khá 7–8.4</div>
                    <div class="sl-item"><span class="sl-dot" style="background:#d97706"></span>TB 5–6.9</div>
                    <div class="sl-item"><span class="sl-dot" style="background:#dc2626"></span>Yếu &lt;5</div>
                  </div>
                </div>
              </div>
              <div class="chart-card">
                <div class="chart-hd">🏆 Top 5 khóa học</div>
                <div class="chart-box-bar"><canvas id="adminBarChart"></canvas></div>
              </div>
            </div>
          </div>

          <!-- COURSES -->
          <div v-show="activeTab==='courses'" class="tab-inner">
            <div class="filter-bar">
              <div class="search-wrap">
                <svg class="search-ico" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="courseSearch" placeholder="Tìm khóa học..." class="filter-input"/>
              </div>
              <select v-model="courseStatusFilter" class="sel">
                <option value="">Tất cả</option><option>ACTIVE</option><option>INACTIVE</option><option>DRAFT</option>
              </select>
            </div>
            <div v-if="loadingCourses" class="center-state"><div class="spinner"></div></div>
            <table v-else class="tbl">
              <thead><tr><th>ID</th><th>Tên khóa học</th><th>Giảng viên</th><th>TC</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
              <tbody>
              <tr v-if="!filteredCourses.length"><td colspan="6" class="empty-cell">Không tìm thấy kết quả</td></tr>
              <tr v-for="c in pagedCourses" :key="c.id">
                <td class="id-col">#{{ c.id }}</td>
                <td><div class="ct">{{ c.title }}</div><div class="cs">{{ c.description }}</div></td>
                <td>{{ c.instructor||'—' }}</td>
                <td><span class="badge badge-blue">{{ c.credits }}</span></td>
                <td><span :class="['badge', c.status==='ACTIVE'?'badge-green':'badge-gray']">{{ c.status }}</span></td>
                <td><div class="acts">
                  <button @click="openLessonManager(c)" class="ab">📖 Bài học</button>
                  <button @click="openCourseModal(c)" class="ab">✏️ Sửa</button>
                  <button @click="delCourse(c.id)" class="ab ab-red">🗑️</button>
                </div></td>
              </tr>
              </tbody>
            </table>
            <!-- Course pagination -->
            <div v-if="totalCoursePages>1" class="tbl-pagination">
              <button class="pg-btn" :disabled="coursePage===1" @click="coursePage--">‹</button>
              <button v-for="p in pgNums(totalCoursePages,coursePage)" :key="p"
                      :class="['pg-btn',{active:p===coursePage,ellipsis:p==='...'}]"
                      :disabled="p==='...'" @click="p!=='...'&&(coursePage=p)">{{p}}</button>
              <button class="pg-btn" :disabled="coursePage===totalCoursePages" @click="coursePage++">›</button>
              <span class="pg-info">{{ filteredCourses.length }} khóa học</span>
            </div>
          </div>

          <!-- LESSONS -->
          <div v-show="activeTab==='lessons'" class="tab-inner">
            <div v-if="!lessonCid" class="center-state"><div class="empty-ico">📖</div><p>Chọn khóa học</p></div>
            <div v-else-if="loadingLessons" class="center-state"><div class="spinner"></div></div>
            <div v-else-if="!lessons.length" class="center-state"><div class="empty-ico">📝</div><p>Chưa có bài học</p></div>
            <div v-else class="lesson-list">
              <div v-for="l in lessons" :key="l.id" class="lcard">
                <div class="l-num">{{ l.orderNum }}</div>
                <div class="l-body">
                  <div class="l-title">{{ l.title }}</div>
                  <div class="l-content">{{ l.content }}</div>
                  <a v-if="l.videoUrl" :href="l.videoUrl" target="_blank" class="l-video">🎥 {{ l.videoUrl }}</a>
                </div>
                <div class="acts">
                  <button @click="openLessonModal(l)" class="ab">✏️</button>
                  <button @click="delLesson(l.id)" class="ab ab-red">🗑️</button>
                </div>
              </div>
            </div>
          </div>

          <div v-show="activeTab==='quiz'" class="tab-inner">
            <!-- Quiz list -->
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
                <p>{{ quizList.length ? 'Không tìm thấy quiz phù hợp' : 'Chưa có quiz nào. Nhấn "Tạo Quiz mới" để bắt đầu!' }}</p>
              </div>
              <table v-else class="tbl">
                <thead><tr><th>ID</th><th>Tiêu đề Quiz</th><th>Bài học</th><th>Khóa học</th><th>Câu hỏi</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                <tbody>
                <tr v-for="q in filteredQuizzes" :key="q.id">
                  <td class="id-col">#{{ q.id }}</td>
                  <td>
                    <div class="ct">{{ q.title }}</div>
                    <div class="cs">{{ q.description }}</div>
                  </td>
                  <td>{{ q.lessonTitle || '—' }}</td>
                  <td>{{ q.courseTitle || '—' }}</td>
                  <td><span class="badge badge-blue">{{ q.questionCount }} câu</span></td>
                  <td>
                      <span :class="['badge', q.isActive ? 'badge-green' : 'badge-gray']">
                        {{ q.isActive ? 'Hoạt động' : 'Ẩn' }}
                      </span>
                  </td>
                  <td>
                    <div class="acts">
                      <button @click="openQuizEdit(q)" class="ab">✏️ Sửa</button>
                      <button @click="delQuiz(q.id)" class="ab ab-red">🗑️</button>
                    </div>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>

            <!-- Quiz Editor form -->
            <div v-else>
              <div class="quiz-editor-topbar">
                <button @click="showQuizEditor=false" class="btn btn-ghost btn-sm">← Quay lại danh sách</button>
                <span class="phd-sub">{{ editingQuiz ? '✏️ Chỉnh sửa quiz' : '➕ Tạo quiz mới' }}</span>
              </div>
              <QuizEditor
                  :existingQuiz="editingQuiz"
                  @saved="onQuizSaved"
                  @cancel="showQuizEditor=false"
              />
            </div>
          </div>
          <!-- USERS -->
          <div v-show="activeTab==='users'" class="tab-inner">
            <!-- User stats bar -->
            <div class="user-stats-row">
              <div class="ust-item"><span class="ust-n">{{ userStats.total }}</span><span class="ust-l">Tổng</span></div>
              <div class="ust-item"><span class="ust-n" style="color:var(--accent)">{{ userStats.students }}</span><span class="ust-l">Học viên</span></div>
              <div class="ust-item"><span class="ust-n" style="color:var(--purple)">{{ userStats.admins }}</span><span class="ust-l">Admin</span></div>
              <div class="ust-item"><span class="ust-n" style="color:#059669">{{ userStats.instructors }}</span><span class="ust-l">Giảng viên</span></div>
              <div class="ust-item"><span class="ust-n" style="color:var(--red)">{{ userStats.banned }}</span><span class="ust-l">Bị ban</span></div>
            </div>
            <div class="filter-bar">
              <div class="search-wrap">
                <svg class="search-ico" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="userSearch" placeholder="Tìm tên, email..." class="filter-input"/>
              </div>
              <select v-model="userRoleFilter" class="sel">
                <option value="">Tất cả role</option>
                <option value="STUDENT">STUDENT</option>
                <option value="INSTRUCTOR">INSTRUCTOR</option>
                <option value="ADMIN">ADMIN</option>
                <option value="BANNED">BANNED</option>
              </select>
              <button @click="loadUsers" class="btn btn-ghost btn-sm">↺ Làm mới</button>
            </div>
            <div v-if="loadingUsers" class="center-state"><div class="spinner"></div></div>
            <table v-else class="tbl">
              <thead><tr><th>ID</th><th>Học viên</th><th>Email</th><th>Vai trò</th><th>Ngày tạo</th><th>Thao tác</th></tr></thead>
              <tbody>
              <tr v-if="!filteredUsers.length"><td colspan="6" class="empty-cell">Không tìm thấy</td></tr>
              <tr v-for="u in pagedUsers" :key="u.id" :class="u.role==='BANNED' ? 'row-banned' : ''">
                <td class="id-col">#{{ u.id }}</td>
                <td>
                  <div class="ucell">
                    <div class="uav">{{ (u.username||'?').charAt(0).toUpperCase() }}</div>
                    <div class="uname">{{ u.username }}</div>
                  </div>
                </td>
                <td class="muted-col">{{ u.email }}</td>
                <td>
                  <span :class="['badge',
                    u.role==='ADMIN'?'badge-purple':
                    u.role==='INSTRUCTOR'?'badge-green':
                    u.role==='BANNED'?'badge-red':'badge-blue']">{{ u.role }}</span>
                </td>
                <td class="muted-col">{{ u.createdAt ? new Date(u.createdAt).toLocaleDateString('vi-VN') : '—' }}</td>
                <td>
                  <div class="action-btns">
                    <!-- Đổi role -->
                    <select v-if="u.role !== 'ADMIN' && u.role !== 'BANNED'" class="role-sel"
                            :value="u.role"
                            @change="changeRole(u.id, $event.target.value)">
                      <option value="STUDENT">Student</option>
                      <option value="INSTRUCTOR">Instructor</option>

                    </select>
                    <!-- Hạ cấp ADMIN → STUDENT — ẩn nếu chỉ còn 1 ADMIN -->
                    <button v-if="u.role === 'ADMIN' && userStats.admins > 1"
                            @click="downgradeAdmin(u.id)"
                            class="ab ab-orange" title="Hạ cấp về Student">⬇️ Hạ cấp</button>
                    <span v-if="u.role === 'ADMIN' && userStats.admins <= 1"
                          class="last-admin-badge" title="ADMIN duy nhất — không thể hạ cấp">🔒 Duy nhất</span>
                    <!-- Ban / Unban -->
                    <button v-if="u.role !== 'ADMIN' && u.role !== 'BANNED'"
                            @click="banUser(u.id)" class="ab ab-orange" title="Ban user">🚫</button>
                    <button v-if="u.role === 'BANNED'"
                            @click="unbanUser(u.id)" class="ab ab-green" title="Unban">✅</button>
                    <!-- Xóa -->
                    <button v-if="u.role !== 'ADMIN'" @click="delUser(u.id)" class="ab ab-red" title="Xóa">🗑️</button>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
            <!-- Pagination giữ nguyên -->
          </div>

          <!-- GRADES -->
          <div v-show="activeTab==='grades'" class="tab-inner">
            <div v-if="!enrollCid" class="center-state"><div class="empty-ico">🎯</div><p>Chọn khóa học</p></div>
            <div v-else-if="loadingEnroll" class="center-state"><div class="spinner"></div></div>
            <div v-else-if="!enrollments.length" class="center-state"><div class="empty-ico">👥</div><p>Chưa có học viên</p></div>
            <template v-else>
              <div class="grade-info-box">
                <span class="gib-icon">ℹ️</span>
                <span>Điểm được tính tự động: <strong>% bài học hoàn thành × 10</strong>. VD: hoàn thành 70% → điểm 7.0</span>
              </div>
              <div class="mini-stats">
                <div class="ms-it"><span class="ms-n">{{ enrollments.length }}</span><span class="ms-l">Học viên</span></div>
                <div class="ms-it"><span class="ms-n" style="color:var(--green)">{{ enrollments.filter(e=>e.grade?.score!=null).length }}</span><span class="ms-l">Đã tính điểm</span></div>
                <div v-if="avgEnroll" class="ms-it"><span class="ms-n" style="color:var(--accent)">{{ avgEnroll }}</span><span class="ms-l">Điểm TB</span></div>
                <button @click="recalcAll" class="btn btn-accent btn-sm" :disabled="calcingAll" style="margin-left:auto">
                  {{ calcingAll ? '...' : '⟳ Tính tất cả' }}
                </button>
              </div>
              <table class="tbl">
                <thead><tr><th>#</th><th>Học viên</th><th>Tiến độ</th><th>Bài xong/Tổng</th><th>Điểm tự động</th><th>Thao tác</th></tr></thead>
                <tbody>
                <tr v-for="(e,i) in enrollments" :key="e.id">
                  <td class="id-col">{{ i+1 }}</td>
                  <td>
                    <div class="ucell">
                      <div class="uav">{{ (e.user?.username||'?').charAt(0).toUpperCase() }}</div>
                      <div><div class="uname">{{ e.user?.username||'—' }}</div><div class="uid">{{ e.user?.email }}</div></div>
                    </div>
                  </td>
                  <td>
                    <div class="prog-mini">
                      <div class="prog-mini-track"><div class="prog-mini-fill" :style="`width:${progressOf(e)}%`" :class="{ done: progressOf(e)===100 }"></div></div>
                      <span class="prog-mini-pct">{{ progressOf(e) }}%</span>
                    </div>
                  </td>
                  <td class="muted-col">{{ completedOf(e) }} / {{ totalLessonsCount }}</td>
                  <td>
                    <span v-if="e.grade?.score!=null" :class="['schip', sc(e.grade.score)]">{{ e.grade.score?.toFixed(1) }}</span>
                    <span v-else class="no-score">—</span>
                  </td>
                  <td>
                    <button @click="recalcGrade(e)" class="btn btn-accent btn-sm" :disabled="calcingId===e.id">
                      {{ calcingId===e.id ? '...' : '⟳ Tính' }}
                    </button>
                  </td>
                </tr>
                </tbody>
              </table>
            </template>
          </div>

          <!-- CERTIFICATES -->
          <div v-show="activeTab==='certificates'" class="tab-inner">
            <div class="filter-bar">
              <div class="search-wrap">
                <svg class="search-ico" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="certSearch" placeholder="Tìm tên học viên, khóa học..." class="filter-input"/>
              </div>
              <select v-model="certCourseFilter" class="sel" style="min-width:160px">
                <option value="">Tất cả khóa học</option>
                <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.title }}</option>
              </select>
              <button @click="loadCerts" class="btn btn-ghost btn-sm">↺ Làm mới</button>
            </div>

            <!-- Stats row -->
            <div v-if="certStats" class="mini-stats" style="margin-bottom:.75rem">
              <div class="ms-it"><span class="ms-n" style="color:var(--yellow)">🏆 {{ certStats.total }}</span><span class="ms-l">Tổng chứng chỉ</span></div>
              <div v-for="s in certStats.byCourse.slice(0,3)" :key="s.courseTitle" class="ms-it">
                <span class="ms-n" style="color:var(--accent)">{{ s.count }}</span>
                <span class="ms-l">{{ s.courseTitle }}</span>
              </div>
            </div>

            <div v-if="loadingCerts" class="center-state"><div class="spinner"></div></div>
            <div v-else-if="!filteredCerts.length" class="center-state">
              <div class="empty-ico">🏆</div>
              <p>{{ certList.length ? 'Không tìm thấy chứng chỉ phù hợp' : 'Chưa có chứng chỉ nào' }}</p>
            </div>
            <table v-else class="tbl">
              <thead>
              <tr>
                <th>ID</th>
                <th>Học viên</th>
                <th>Khóa học</th>
                <th>Điểm TB Quiz</th>
                <th>Ngày cấp</th>
                <th>Mã chứng chỉ</th>
                <th>Thao tác</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="!filteredCerts.length"><td colspan="7" class="empty-cell">Không tìm thấy</td></tr>
              <tr v-for="c in filteredCerts" :key="c.id">
                <td class="id-col">#{{ c.id }}</td>
                <td>
                  <div class="ucell">
                    <div class="uav">{{ (c.studentName||'?').charAt(0).toUpperCase() }}</div>
                    <div class="uname">{{ c.studentName }}</div>
                  </div>
                </td>
                <td><span class="ct">{{ c.courseTitle }}</span></td>
                <td>
                    <span :class="['schip', sc(c.averageQuizScore/10)]">
                      {{ c.averageQuizScore }}%
                    </span>
                </td>
                <td class="muted-col">{{ formatDate(c.issuedAt) }}</td>
                <td>
                  <a :href="`/verify/${c.certificateCode}`" target="_blank"
                     class="cert-code-link" :title="c.certificateCode">
                    {{ c.certificateCode?.slice(0,8) }}...
                  </a>
                </td>
                <td>
                  <button @click="revokeCert(c)" class="ab ab-red" title="Thu hồi chứng chỉ">🗑️ Thu hồi</button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- REVIEWS TAB -->
          <div v-show="activeTab==='reviews'" class="tab-inner">

            <!-- Stats tổng quan -->
            <div v-if="!loadingReviews && allReviews.length" class="rv-stats-row">
              <div class="rv-stat">
                <span class="rv-stat-n">{{ allReviews.length }}</span>
                <span class="rv-stat-l">Tổng đánh giá</span>
              </div>
              <div class="rv-stat">
                <span class="rv-stat-n" style="color:#f59e0b">{{ reviewAvg }}</span>
                <span class="rv-stat-l">Điểm TB</span>
              </div>
              <div v-for="s in [5,4,3,2,1]" :key="s" class="rv-stat rv-stat-star">
                <div class="rv-star-bar-wrap">
                  <div class="rv-star-bar-fill"
                       :style="`width:${allReviews.length ? (allReviews.filter(r=>r.rating===s).length/allReviews.length*100) : 0}%`">
                  </div>
                </div>
                <span class="rv-star-label">{{ s }}★ <b>{{ allReviews.filter(r=>r.rating===s).length }}</b></span>
              </div>
            </div>

            <!-- Filter bar -->
            <div class="filter-bar" style="margin-bottom:.85rem">
              <div class="search-wrap">
                <svg class="search-ico" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="reviewSearch" placeholder="Tìm học viên, khóa học, nội dung..." class="filter-input"/>
              </div>
              <div class="rv-star-btns">
                <button :class="['rv-star-btn', reviewRatingFilter==='' ? 'active' : '']"
                        @click="reviewRatingFilter=''">Tất cả</button>
                <button v-for="s in [5,4,3,2,1]" :key="s"
                        :class="['rv-star-btn', reviewRatingFilter==s ? 'active s'+s : '']"
                        @click="reviewRatingFilter = reviewRatingFilter==s ? '' : s">
                  {{ '★'.repeat(s) }}
                </button>
              </div>
              <button @click="loadReviews" class="btn btn-ghost btn-sm" style="margin-left:auto">↺ Làm mới</button>
            </div>

            <div v-if="loadingReviews" class="center-state"><div class="spinner"></div></div>
            <div v-else-if="!filteredReviews.length" class="center-state">
              <div class="empty-ico">⭐</div>
              <p>{{ allReviews.length ? 'Không tìm thấy đánh giá phù hợp' : 'Chưa có đánh giá nào' }}</p>
            </div>

            <!-- Card grid -->
            <div v-else class="rv-grid">
              <div v-for="r in filteredReviews" :key="r.id" class="rv-card">
                <div class="rv-card-top">
                  <div class="rv-user">
                    <div class="rv-av-wrap">
                      <img v-if="r.avatar" :src="r.avatar" class="rv-av-img" />
                      <div v-else class="rv-av">{{ (r.username||'?').charAt(0).toUpperCase() }}</div>
                    </div>
                    <div class="rv-user-info">
                      <div class="rv-username">{{ r.username }}</div>
                      <div class="rv-course">{{ r.courseTitle }}</div>
                    </div>
                  </div>
                  <button @click="deleteReview(r.id)" class="rv-del-btn" title="Xóa đánh giá">✕</button>
                </div>
                <div class="rv-stars-row">
                  <span v-for="i in 5" :key="i" :class="['rv-s', i<=r.rating?'filled':'']">★</span>
                  <span class="rv-score-label">{{ r.rating }}/5</span>
                  <span class="rv-date">{{ formatDate(r.createdAt) }}</span>
                </div>
                <p v-if="r.comment" class="rv-comment">{{ r.comment }}</p>
                <p v-else class="rv-no-comment">— Không có nhận xét —</p>
              </div>
            </div>
          </div>

          <!-- NOTIFICATIONS TAB -->
          <div v-show="activeTab==='notifications'" class="tab-inner">

            <!-- Stats + Broadcast row -->
            <div class="notif-top-row">
              <!-- Stats mini -->
              <div class="notif-stats">
                <div class="nst"><span class="nst-n">{{ notifTotal }}</span><span class="nst-l">Tổng</span></div>
                <div class="nst nst-sys"><span class="nst-n">📢</span><span class="nst-l">System</span></div>
                <div class="nst nst-grade"><span class="nst-n">🎯</span><span class="nst-l">Grade</span></div>
                <div class="nst nst-enroll"><span class="nst-n">📚</span><span class="nst-l">Enroll</span></div>
                <div class="nst nst-complete"><span class="nst-n">🎉</span><span class="nst-l">Done</span></div>
              </div>
              <!-- Broadcast button -->
              <button class="btn-broadcast" @click="showBroadcast=!showBroadcast">
                <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M22 2 11 13"/><path d="m22 2-7 20-4-9-9-4 20-7z"/></svg>
                Gửi thông báo
                <span class="bc-arr">{{ showBroadcast ? '▲' : '▼' }}</span>
              </button>
            </div>

            <!-- Broadcast form -->
            <div v-if="showBroadcast" class="bc-panel">
              <div class="bc-panel-title">📢 Tạo thông báo mới</div>
              <div class="bc-grid">
                <div class="bc-field">
                  <label class="bc-label">Tiêu đề <span class="req">*</span></label>
                  <input v-model="broadcastForm.title" placeholder="VD: Thông báo bảo trì hệ thống" class="bc-input"/>
                </div>
                <div class="bc-field">
                  <label class="bc-label">Đường dẫn</label>
                  <input v-model="broadcastForm.link" placeholder="/courses" class="bc-input"/>
                </div>
                <div class="bc-field bc-full">
                  <label class="bc-label">Nội dung <span class="req">*</span></label>
                  <textarea v-model="broadcastForm.message" placeholder="Nội dung chi tiết..." class="bc-textarea" rows="2"></textarea>
                </div>
                <div class="bc-field">
                  <label class="bc-label">Gửi đến (User ID)</label>
                  <input v-model="broadcastForm.userId" placeholder="Để trống = gửi tất cả" class="bc-input" type="number"/>
                </div>
                <div class="bc-field bc-send-wrap">
                  <button @click="broadcast" class="btn-send" :disabled="broadcasting">
                    <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M22 2 11 13"/><path d="m22 2-7 20-4-9-9-4 20-7z"/></svg>
                    {{ broadcasting ? 'Đang gửi...' : 'Gửi thông báo' }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Filter bar -->
            <div class="filter-bar">
              <div class="search-wrap">
                <svg class="search-ico" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                <input v-model="notifSearch" placeholder="Tìm tiêu đề, nội dung..." class="filter-input"/>
              </div>
              <!-- Type filter buttons -->
              <div class="notif-type-btns">
                <button :class="['ntb', notifTypeFilter===''?'ntb-active':'']" @click="notifTypeFilter=''">Tất cả</button>
                <button :class="['ntb','ntb-sys', notifTypeFilter==='SYSTEM'?'ntb-active':'']" @click="notifTypeFilter=notifTypeFilter==='SYSTEM'?'':'SYSTEM'">📢 System</button>
                <button :class="['ntb','ntb-grade', notifTypeFilter==='GRADE'?'ntb-active':'']" @click="notifTypeFilter=notifTypeFilter==='GRADE'?'':'GRADE'">🎯 Grade</button>
                <button :class="['ntb','ntb-enroll', notifTypeFilter==='ENROLLMENT'?'ntb-active':'']" @click="notifTypeFilter=notifTypeFilter==='ENROLLMENT'?'':'ENROLLMENT'">📚 Enroll</button>
                <button :class="['ntb','ntb-done', notifTypeFilter==='COMPLETION'?'ntb-active':'']" @click="notifTypeFilter=notifTypeFilter==='COMPLETION'?'':'COMPLETION'">🎉 Done</button>
              </div>
              <button @click="loadNotifs(0)" class="btn btn-ghost btn-sm" style="margin-left:auto">↺</button>
              <button v-if="notifTotal>0" @click="clearAllNotifs" class="btn-clear-all">🗑️ Xóa hết</button>
            </div>

            <!-- Loading -->
            <div v-if="loadingNotifs" class="center-state"><div class="spinner"></div></div>

            <!-- Empty -->
            <div v-else-if="!allNotifs.length" class="notif-empty">
              <div class="notif-empty-ico">🔔</div>
              <div class="notif-empty-text">Không tìm thấy thông báo nào</div>
              <div class="notif-empty-sub">Thử thay đổi bộ lọc hoặc gửi thông báo mới</div>
            </div>

            <!-- List (table layout) -->
            <div v-else class="notif-list">
              <div v-for="n in allNotifs" :key="n.id"
                   :class="['notif-row', n.read ? 'nr-read' : 'nr-unread']">
                <div class="nr-type">
                  <span :class="['nr-type-dot', 'dot-'+(n.type||'').toLowerCase()]"></span>
                  <span :class="['nr-type-label', 'nt-'+(n.type||'').toLowerCase()]">{{ n.type }}</span>
                </div>
                <div class="nr-main">
                  <div class="nr-title">{{ n.title }}</div>
                  <div class="nr-msg">{{ n.message }}</div>
                </div>
                <div class="nr-meta">
                  <span class="nr-user">👤 #{{ n.user?.id || '?' }}</span>
                  <span class="nr-date">{{ formatDate(n.createdAt) }}</span>
                </div>
                <div class="nr-status">
                  <span :class="['nr-read-dot', n.read ? 'dot-read' : 'dot-unread']">
                    {{ n.read ? '✓' : '●' }}
                  </span>
                </div>
                <button @click="deleteNotif(n.id)" class="nr-del">✕</button>
              </div>
            </div>

            <!-- Pagination -->
            <div v-if="notifTotalPages > 1" class="notif-pagination">
              <button :disabled="notifPage===0" @click="loadNotifs(notifPage-1)" class="npg-btn">‹ Trước</button>
              <div class="npg-pages">
                <span v-for="p in notifTotalPages" :key="p"
                      :class="['npg-num', p-1===notifPage ? 'npg-active' : '']"
                      @click="loadNotifs(p-1)">{{ p }}</span>
              </div>
              <button :disabled="notifPage>=notifTotalPages-1" @click="loadNotifs(notifPage+1)" class="npg-btn">Sau ›</button>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- Course Modal -->
    <div v-if="showCM" class="modal-overlay" @click.self="showCM=false">
      <div class="modal">
        <h2>{{ editC?.id?'✏️ Sửa':'➕ Thêm' }} khóa học</h2>
        <form @submit.prevent="saveCourse">
          <div class="form-group"><label>Tên *</label><input v-model="cf.title" required/></div>
          <div class="form-group"><label>Mô tả</label><textarea v-model="cf.description" rows="2"></textarea></div>
          <div class="f2">
            <div class="form-group"><label>Giảng viên</label><input v-model="cf.instructor"/></div>
            <div class="form-group"><label>Tín chỉ *</label><input v-model.number="cf.credits" type="number" min="1" required/></div>
          </div>
          <div class="form-group"><label>Trạng thái</label><select v-model="cf.status"><option>ACTIVE</option><option>INACTIVE</option><option>DRAFT</option></select></div>
          <div class="modal-actions">
            <button type="button" @click="showCM=false" class="btn btn-ghost">Hủy</button>
            <button type="submit" class="btn btn-accent" :disabled="saving">{{ saving?'...':'Lưu' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Lesson Modal -->
    <div v-if="showLM" class="modal-overlay" @click.self="showLM=false">
      <div class="modal">
        <h2>{{ editL?.id?'✏️ Sửa':'➕ Thêm' }} bài học</h2>
        <form @submit.prevent="saveLesson">
          <div class="form-group"><label>Tiêu đề *</label><input v-model="lf.title" required/></div>
          <div class="form-group"><label>Nội dung</label><textarea v-model="lf.content" rows="4"></textarea></div>
          <div class="f2">
            <div class="form-group"><label>Thứ tự *</label><input v-model.number="lf.orderNum" type="number" min="1" required/></div>
            <div class="form-group"><label>Video URL</label><input v-model="lf.videoUrl" placeholder="https://..."/></div>
          </div>
          <div class="form-group">
            <label>Video bài học</label>
            <VideoUploader v-model="lf.videoUrl" :lessonId="editL?.id ?? null"/>
            <div v-if="!editL?.id" class="field-hint">
              💡 Để upload file video, hãy lưu bài học trước, sau đó mở lại để upload.
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showLM=false" class="btn btn-ghost">Hủy</button>
            <button type="submit" class="btn btn-accent" :disabled="saving">{{ saving?'...':'Lưu' }}</button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="toast" :class="['toast', toast.type]">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import api from '../services/api'
import QuizEditor from '../components/QuizEditor.vue'
import VideoUploader from '../components/VideoUploader.vue'

const activeTab  = ref('dashboard')
const currentYear = new Date().getFullYear()
const tabs = [
  { key:'dashboard',     icon:'📊', label:'Dashboard' },
  { key:'courses',       icon:'📚', label:'Khóa học' },
  { key:'lessons',       icon:'📖', label:'Bài học' },
  { key:'quiz',          icon:'❓', label:'Quiz' },
  { key:'users',         icon:'👥', label:'Học viên' },
  { key:'grades',        icon:'🎯', label:'Điểm số' },
  { key:'certificates',  icon:'🏆', label:'Chứng chỉ' },
  { key:'reviews',       icon:'⭐', label:'Đánh giá' },
  { key:'notifications', icon:'🔔', label:'Thông báo' },
]

const courses=ref([]); const lessons=ref([]); const users=ref([]); const enrollments=ref([])
const stats=ref(null); const progressData=ref({})
const loadingCourses=ref(false); const loadingLessons=ref(false)
const loadingUsers=ref(false); const loadingEnroll=ref(false); const loadingStats=ref(false)
const enrollCid=ref(''); const lessonCid=ref(''); const selCourse=ref(null)
const totalLessonsCount=ref(0)
const showCM=ref(false); const showLM=ref(false)
const showQuizEditor=ref(false); const editingQuiz=ref(null)
const quizList=ref([]); const loadingQuizzes=ref(false)
const quizSearch=ref(''); const quizCourseFilter=ref('')
const editC=ref(null); const editL=ref(null)
const saving=ref(false); const calcingId=ref(null); const calcingAll=ref(false)
const cf=ref({title:'',description:'',instructor:'',credits:3,status:'ACTIVE'})
const lf=ref({title:'',content:'',orderNum:1,videoUrl:''})
const toast=ref(null)
const courseSearch=ref(''); const courseStatusFilter=ref('')
// Certificates admin
const certList=ref([]); const loadingCerts=ref(false)
const certCourseFilter=ref(''); const certSearch=ref('')
// Reviews
const allReviews=ref([]); const loadingReviews=ref(false)
const reviewSearch=ref(''); const reviewRatingFilter=ref('')
const userSearch=ref(''); const userRoleFilter=ref('')
// Notifications admin
const allNotifs=ref([]); const loadingNotifs=ref(false)
const notifSearch=ref(''); const notifTypeFilter=ref('')
const notifPage=ref(0); const notifTotalPages=ref(0); const notifTotal=ref(0)
const NOTIF_PAGE_SIZE=30
const broadcastForm=ref({title:'',message:'',link:'/courses',userId:''})
const showBroadcast=ref(false); const broadcasting=ref(false)
// User management
const showUserModal=ref(false); const editingUser=ref(null)
// ── Pagination ──
const userPage=ref(1); const coursePage=ref(1); const enrollPage=ref(1)
const PAGE_SIZE=10

let chartInst = {}

const kpiCards = computed(() => !stats.value ? [] : [
  { icon:'👥', label:'Người dùng',  value:stats.value.totalUsers,           bg:'#dbeafe' },
  { icon:'📚', label:'Khóa học',    value:stats.value.totalCourses,         bg:'#d1fae5' },
  { icon:'📋', label:'Đăng ký',     value:stats.value.totalEnrollments,     bg:'#fef3c7' },
  { icon:'🎉', label:'Hoàn thành',  value:stats.value.completionRate+'%',   bg:'#ede9fe' },
  { icon:'⭐', label:'Điểm TB',     value:stats.value.avgScore,             bg:'#fce7f3' },
])
const avgEnroll = computed(() => {
  const g = enrollments.value.filter(e=>e.grade?.score!=null)
  return g.length ? (g.reduce((s,e)=>s+e.grade.score,0)/g.length).toFixed(1) : null
})
const filteredCourses = computed(() =>
    courses.value.filter(c => {
      const q = courseSearch.value.toLowerCase()
      return (!q || (c.title||'').toLowerCase().includes(q) || (c.instructor||'').toLowerCase().includes(q))
          && (!courseStatusFilter.value || c.status===courseStatusFilter.value)
    })
)
const filteredUsers = computed(() =>
    users.value.filter(u => {
      const q = userSearch.value.toLowerCase()
      return (!q || (u.username||'').toLowerCase().includes(q) || (u.email||'').toLowerCase().includes(q))
          && (!userRoleFilter.value || u.role===userRoleFilter.value)
    })
)

// Pagination computed
const totalUserPages   = computed(() => Math.ceil(filteredUsers.value.length / PAGE_SIZE))
const totalCoursePages = computed(() => Math.ceil(filteredCourses.value.length / PAGE_SIZE))
const totalEnrollPages = computed(() => Math.ceil(enrollments.value.length / PAGE_SIZE))
const pagedUsers    = computed(() => filteredUsers.value.slice((userPage.value-1)*PAGE_SIZE, userPage.value*PAGE_SIZE))
const pagedCourses  = computed(() => filteredCourses.value.slice((coursePage.value-1)*PAGE_SIZE, coursePage.value*PAGE_SIZE))
const pagedEnrollments = computed(() => enrollments.value.slice((enrollPage.value-1)*PAGE_SIZE, enrollPage.value*PAGE_SIZE))
function pgNums(total, cur) {
  if (total<=7) return Array.from({length:total},(_,i)=>i+1)
  const pages=[1]
  if (cur>3) pages.push('...')
  for(let i=Math.max(2,cur-1);i<=Math.min(total-1,cur+1);i++) pages.push(i)
  if(cur<total-2) pages.push('...')
  pages.push(total)
  return pages
}

// Thống kê user nhanh
const userStats = computed(() => {
  const total   = users.value.length
  const students = users.value.filter(u=>u.role==='STUDENT').length
  const admins   = users.value.filter(u=>u.role==='ADMIN').length
  const instructors = users.value.filter(u=>u.role==='INSTRUCTOR').length
  const banned   = users.value.filter(u=>u.role==='BANNED').length
  return { total, students, admins, instructors, banned }
})

// Notifications filter server-side — watch để reload khi thay filter


const reviewAvg = computed(() => {
  if (!allReviews.value.length) return '0.0'
  return (allReviews.value.reduce((s,r) => s + r.rating, 0) / allReviews.value.length).toFixed(1)
})

const filteredReviews = computed(() => {
  let list = [...allReviews.value]
  const q = reviewSearch.value.toLowerCase()
  if (q) list = list.filter(r =>
      r.username?.toLowerCase().includes(q) ||
      r.courseTitle?.toLowerCase().includes(q) ||
      r.comment?.toLowerCase().includes(q)
  )
  if (reviewRatingFilter.value) list = list.filter(r => r.rating == reviewRatingFilter.value)
  return list
})

const progressOf  = e => progressData.value[e.id]?.pct ?? 0
const completedOf = e => progressData.value[e.id]?.completed ?? 0
const showToast = (m,t='success') => { toast.value={msg:m,type:t}; setTimeout(()=>toast.value=null,3000) }
const sc = s => s>=8.5?'sc-exc':s>=7?'sc-good':s>=5?'sc-avg':'sc-poor'

// ── CHARTS ──
async function getChartLib() {
  if (window.Chart) {
    if (!window._chartRegistered) {
      // Chart from CDN - registerables may already be registered
      window._chartRegistered = true
    }
    return window.Chart
  }
  const mod = await import('https://cdn.jsdelivr.net/npm/chart.js@4.4.4/+esm')
  mod.Chart.register(...mod.registerables)
  window.Chart = mod.Chart
  return mod.Chart
}

async function renderCharts() {
  if (!stats.value) return
  await nextTick()
  // Small delay so v-show tab is fully visible and canvas has dimensions
  await new Promise(r => setTimeout(r, 150))

  let Chart
  try { Chart = await getChartLib() } catch(e) { console.error('Chart.js load failed', e); return }

  const d = (key, canvasId, config) => {
    if (chartInst[key]) { try { chartInst[key].destroy() } catch {} }
    const el = document.getElementById(canvasId)
    if (!el) return
    chartInst[key] = new Chart(el, config)
  }

  const months = stats.value.enrollmentsByMonth.map(m => m.month)
  const counts  = stats.value.enrollmentsByMonth.map(m => m.count)
  const dist    = stats.value.scoreDistribution
  const top     = stats.value.topCourses

  d('line', 'adminLineChart', {
    type: 'line',
    data: { labels: months, datasets: [{ label:'Đăng ký', data: counts, fill: true,
        backgroundColor: 'rgba(37,99,235,0.09)', borderColor: '#2563eb',
        borderWidth: 2.5, pointBackgroundColor: '#2563eb', pointRadius: 4, tension: 0.4 }]},
    options: { responsive:true, maintainAspectRatio:false,
      plugins:{ legend:{display:false} },
      scales:{ y:{ beginAtZero:true, ticks:{precision:0} } } }
  })

  d('donut', 'adminDonutChart', {
    type: 'doughnut',
    data: { labels:['Giỏi','Khá','Trung bình','Yếu'],
      datasets:[{ data:[dist.excellent,dist.good,dist.average,dist.poor],
        backgroundColor:['#059669','#2563eb','#d97706','#dc2626'],
        borderWidth:0, hoverOffset:6 }]},
    options: { responsive:true, maintainAspectRatio:false,
      plugins:{ legend:{display:false} }, cutout:'68%' }
  })

  d('bar', 'adminBarChart', {
    type: 'bar',
    data: { labels: top.map(c=>c.title.length>22?c.title.slice(0,22)+'…':c.title),
      datasets:[{ label:'Học viên', data:top.map(c=>c.enrollments),
        backgroundColor:'#dbeafe', borderColor:'#2563eb', borderWidth:2, borderRadius:6 }]},
    options: { responsive:true, maintainAspectRatio:false,
      plugins:{ legend:{display:false} },
      scales:{ y:{ beginAtZero:true, ticks:{precision:0} } } }
  })
}

function switchTab(key) {
  activeTab.value = key
  if (key==='users' && !users.value.length) loadUsers()
  if (key==='quiz') { showQuizEditor.value=false; loadQuizzes() }
  if (key==='dashboard') loadStats()
}

async function loadStats() {
  loadingStats.value=true; stats.value=null
  try {
    const r = await api.get('/admin/stats')
    stats.value = r.data
    renderCharts() // không await — render trong nền
  } catch { showToast('Lỗi tải thống kê!','error') }
  finally { loadingStats.value=false }
}

async function loadCourses() {
  loadingCourses.value=true
  try { courses.value=(await api.get('/courses')).data } catch {}
  finally { loadingCourses.value=false }
}
function openCourseModal(c=null) {
  editC.value=c
  cf.value=c?{title:c.title,description:c.description||'',instructor:c.instructor||'',credits:c.credits,status:c.status}
      :{title:'',description:'',instructor:'',credits:3,status:'ACTIVE'}
  showCM.value=true
}
async function saveCourse() {
  saving.value=true
  try {
    editC.value?.id?await api.put(`/courses/${editC.value.id}`,cf.value):await api.post('/courses',cf.value)
    showToast('Lưu thành công!'); showCM.value=false; loadCourses()
  } catch(e) { showToast(e.response?.data?.error||'Lỗi!','error') }
  finally { saving.value=false }
}
async function delCourse(id) {
  if(!confirm('Xóa khóa học?')) return
  try { await api.delete(`/courses/${id}`); showToast('Đã xóa!'); loadCourses() }
  catch { showToast('Thất bại!','error') }
}

function openLessonManager(c) { selCourse.value=c; lessonCid.value=c.id; activeTab.value='lessons'; loadLessons() }
async function loadLessons() {
  if(!lessonCid.value) return
  selCourse.value=courses.value.find(c=>c.id==lessonCid.value)||selCourse.value
  loadingLessons.value=true
  try { lessons.value=(await api.get(`/lessons/course/${lessonCid.value}`)).data }
  catch {} finally { loadingLessons.value=false }
}
function openLessonModal(l=null) {
  editL.value=l
  lf.value=l?{title:l.title,content:l.content||'',orderNum:l.orderNum,videoUrl:l.videoUrl||''}
      :{title:'',content:'',orderNum:lessons.value.length+1,videoUrl:''}
  showLM.value=true
}
async function saveLesson() {
  saving.value=true
  try {
    editL.value?.id?await api.put(`/lessons/${editL.value.id}`,lf.value):await api.post(`/lessons?courseId=${lessonCid.value}`,lf.value)
    showToast('Lưu bài học!'); showLM.value=false; loadLessons()
  } catch { showToast('Lỗi!','error') } finally { saving.value=false }
}
async function delLesson(id) {
  if(!confirm('Xóa bài học?')) return
  try { await api.delete(`/lessons/${id}`); showToast('Đã xóa!'); loadLessons() }
  catch { showToast('Thất bại!','error') }
}

async function loadUsers() {
  loadingUsers.value=true
  try {
    const res = await api.get('/users')
    users.value = res.data
  } catch(e) {
    showToast('Không tải được danh sách học viên: ' + (e.response?.status || e.message), 'error')
  } finally { loadingUsers.value=false }
}
async function delUser(id) {
  if(!confirm('Xóa user?')) return
  try { await api.delete(`/users/${id}`); showToast('Đã xóa!'); loadUsers() }
  catch { showToast('Thất bại!','error') }
}

async function loadEnrollments() {
  if(!enrollCid.value) return
  loadingEnroll.value=true; progressData.value={}
  try {
    const [enrRes, lesRes] = await Promise.all([
      api.get(`/enrollments/course/${enrollCid.value}`),
      api.get(`/lessons/course/${enrollCid.value}`)
    ])
    enrollments.value    = enrRes.data
    totalLessonsCount.value = lesRes.data.length

    await Promise.all(enrollments.value.map(async e => {
      try {
        const r = await api.get(`/progress?userId=${e.user?.id}&courseId=${enrollCid.value}`)
        progressData.value[e.id] = { pct:r.data.percentage, completed:r.data.completedLessons }
      } catch {}
    }))
  } catch {} finally { loadingEnroll.value=false }
}

// Tính điểm tự động: % tiến độ / 10
async function recalcGrade(e) {
  calcingId.value = e.id
  try {
    const pct   = progressData.value[e.id]?.pct ?? 0
    const score = parseFloat((pct / 10).toFixed(1))
    await api.post('/grades/assign', {
      enrollmentId: e.id, score,
      feedback: `Tính tự động – hoàn thành ${pct}% bài học`
    })
    if (!e.grade) e.grade = {}
    e.grade.score = score
    showToast(`✅ ${e.user?.username}: ${score} điểm`)
  } catch(err) { showToast(err.response?.data?.error||'Lỗi!','error') }
  finally { calcingId.value = null }
}

async function recalcAll() {
  if (!enrollments.value.length) return
  if (!confirm(`Tính lại điểm cho tất cả ${enrollments.value.length} học viên?`)) return
  calcingAll.value = true
  let ok=0, fail=0
  for (const e of enrollments.value) {
    try {
      const pct   = progressData.value[e.id]?.pct ?? 0
      const score = parseFloat((pct / 10).toFixed(1))
      await api.post('/grades/assign', { enrollmentId:e.id, score, feedback:`Tính tự động – ${pct}%` })
      if (!e.grade) e.grade = {}
      e.grade.score = score
      ok++
    } catch { fail++ }
  }
  showToast(`✅ Đã tính ${ok} điểm${fail?` (${fail} lỗi)`:''}`)
  calcingAll.value = false
}

const filteredQuizzes = computed(() => {
  const q = quizSearch.value.toLowerCase()
  return quizList.value.filter(quiz =>
      (!q || (quiz.title||'').toLowerCase().includes(q)) &&
      (!quizCourseFilter.value || quiz.courseId == quizCourseFilter.value)
  )
})

async function loadQuizzes() {
  loadingQuizzes.value = true
  try {
    // Gọi endpoint admin trực tiếp — nhanh, 1 request, lấy cả lessonTitle & courseTitle
    const url = quizCourseFilter.value
        ? `/quizzes/admin/course/${quizCourseFilter.value}`
        : '/quizzes/admin/all'
    const res = await api.get(url)
    quizList.value = res.data || []
  } catch(e) {
    console.error('Lỗi tải quiz:', e)
  } finally {
    loadingQuizzes.value = false
  }
}

function onQuizCourseFilter() {
  quizList.value = []
  loadQuizzes()
}

function openQuizCreate() {
  editingQuiz.value = null
  showQuizEditor.value = true
}

async function openQuizEdit(q) {
  // Gọi API lấy quiz đầy đủ (có questions + choices + isCorrect) trước khi mở editor
  try {
    const res = await api.get(`/quizzes/${q.id}/admin/detail`)
    editingQuiz.value = res.data
  } catch(e) {
    // Fallback: dùng data cũ nếu API lỗi
    editingQuiz.value = q
  }
  showQuizEditor.value = true
}

async function delQuiz(id) {
  if (!confirm('Xóa quiz này?')) return
  try {
    await api.delete(`/quizzes/${id}`)
    showToast('Đã xóa quiz!')
    loadQuizzes()
  } catch { showToast('Xóa thất bại!', 'error') }
}

function onQuizSaved() {
  showToast('Lưu quiz thành công!')
  showQuizEditor.value = false
  loadQuizzes()
}

// Certificates
const certStats = ref(null)
const filteredCerts = computed(() => {
  let list = certList.value
  if (certCourseFilter.value) list = list.filter(c => c.courseId == certCourseFilter.value)
  if (certSearch.value) {
    const q = certSearch.value.toLowerCase()
    list = list.filter(c =>
        c.studentName?.toLowerCase().includes(q) ||
        c.courseTitle?.toLowerCase().includes(q) ||
        c.certificateCode?.toLowerCase().includes(q)
    )
  }
  return list
})

async function loadReviews() {
  loadingReviews.value = true
  try { allReviews.value = (await api.get('/reviews/admin/all')).data }
  catch(e) { console.error(e) }
  finally { loadingReviews.value = false }
}

async function deleteReview(id) {
  if (!confirm('Xóa đánh giá này?')) return
  try {
    await api.delete(`/reviews/${id}?userId=0&admin=true`)
    allReviews.value = allReviews.value.filter(r => r.id !== id)
    showToast('Đã xóa đánh giá!')
  } catch(e) { showToast('Xóa thất bại!', 'error') }
}

async function loadCerts() {
  loadingCerts.value = true
  try {
    const [certsRes, statsRes] = await Promise.all([
      api.get('/certificates/admin/all'),
      api.get('/certificates/admin/stats'),
    ])
    certList.value  = certsRes.data || []
    certStats.value = statsRes.data
  } catch(e) {
    showToast('Tải chứng chỉ thất bại!', 'error')
  } finally {
    loadingCerts.value = false
  }
}

async function revokeCert(c) {
  if (!confirm(`Thu hồi chứng chỉ của "${c.studentName}" (${c.courseTitle})?\nHành động này không thể hoàn tác!`)) return
  try {
    await api.delete(`/certificates/admin/${c.id}`)
    certList.value = certList.value.filter(x => x.id !== c.id)
    if (certStats.value) certStats.value.total = Math.max(0, certStats.value.total - 1)
    showToast('Đã thu hồi chứng chỉ!')
  } catch(e) {
    showToast('Thu hồi thất bại!', 'error')
  }
}

function formatDate(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString('vi-VN', { day:'2-digit', month:'2-digit', year:'numeric' })
}

// Load certs khi switch sang tab
async function changeRole(userId, role) {
  // Guard: không cho phép trao quyền ADMIN qua dropdown
  if (role === 'ADMIN') return
  try {
    const res = await api.patch(`/users/${userId}/role?role=${role}`)
    const idx = users.value.findIndex(u=>u.id===userId)
    if (idx >= 0) users.value[idx] = res.data
    showToast(`Đã đổi role thành ${role}!`)
  } catch(e) { showToast(e.response?.data?.error||'Lỗi!','error') }
}

async function downgradeAdmin(userId) {
  const u = users.value.find(u=>u.id===userId)
  if (!confirm(`Hạ cấp "${u?.username}" từ ADMIN → STUDENT?\nHành động này sẽ thu hồi toàn bộ quyền quản trị.`)) return
  try {
    const res = await api.patch(`/users/${userId}/downgrade`)
    const idx = users.value.findIndex(u=>u.id===userId)
    if (idx >= 0) users.value[idx] = res.data
    showToast(`Đã hạ cấp ${u?.username} về STUDENT!`)
  } catch(e) { showToast(e.response?.data?.error||'Lỗi!','error') }
}

async function banUser(userId) {
  const u = users.value.find(u=>u.id===userId)
  if (!confirm(`Ban tài khoản "${u?.username}"?\nUser sẽ bị đăng xuất ngay lập tức và không thể đăng nhập lại.`)) return
  try {
    const res = await api.patch(`/users/${userId}/ban`)
    const idx = users.value.findIndex(u=>u.id===userId)
    if (idx >= 0) users.value[idx] = res.data
    showToast(`Đã ban ${u?.username}!`)
  } catch(e) { showToast(e.response?.data?.error||'Lỗi!','error') }
}

async function unbanUser(userId) {
  const u = users.value.find(u=>u.id===userId)
  if (!confirm(`Unban tài khoản "${u?.username}"?\nUser sẽ được trả về role STUDENT.`)) return
  try {
    const res = await api.patch(`/users/${userId}/unban`)
    const idx = users.value.findIndex(u=>u.id===userId)
    if (idx >= 0) users.value[idx] = res.data
    showToast(`Đã unban ${u?.username}!`)
  } catch(e) { showToast(e.response?.data?.error||'Lỗi!','error') }
}

async function loadNotifs(page=0) {
  loadingNotifs.value = true
  notifPage.value = page
  try {
    const params = new URLSearchParams({
      page, size: NOTIF_PAGE_SIZE,
      type: notifTypeFilter.value,
      search: notifSearch.value
    })
    const res = (await api.get(`/notifications/admin/all?${params}`)).data
    allNotifs.value       = Array.isArray(res.content) ? res.content : []
    notifTotalPages.value = res.totalPages    ?? 0
    notifTotal.value      = res.totalElements ?? 0
  } catch(e) {
    console.error(e)
    allNotifs.value = []
    notifTotalPages.value = 0
    notifTotal.value = 0
  }
  finally { loadingNotifs.value = false }
}

async function clearAllNotifs() {
  if (!confirm(`Xóa tất cả ${notifTotal.value} thông báo? Không thể hoàn tác!`)) return
  try {
    await api.delete('/notifications/admin/clear-all')
    showToast('Đã xóa tất cả thông báo!')
    loadNotifs()
  } catch { showToast('Xóa thất bại!','error') }
}

async function deleteNotif(id) {
  try {
    await api.delete(`/notifications/admin/${id}`)
    allNotifs.value = allNotifs.value.filter(n => n.id !== id)
    showToast('Đã xóa thông báo!')
  } catch { showToast('Lỗi!','error') }
}

async function broadcast() {
  if (!broadcastForm.value.title || !broadcastForm.value.message) {
    showToast('Vui lòng nhập tiêu đề và nội dung!','error'); return
  }
  broadcasting.value = true
  try {
    const payload = { ...broadcastForm.value }
    if (!payload.userId) delete payload.userId
    else payload.userId = Number(payload.userId)
    const res = await api.post('/notifications/admin/broadcast', payload)
    showToast(`✅ Đã gửi ${res.data.sent} thông báo!`)
    showBroadcast.value = false
    broadcastForm.value = { title:'', message:'', link:'/courses', userId:'' }
    loadNotifs()
  } catch { showToast('Gửi thất bại!','error') }
  finally { broadcasting.value = false }
}

watch(activeTab, v => {
  if (v === 'certificates' && !certList.value.length) loadCerts()
  if (v === 'reviews'      && !allReviews.value.length) loadReviews()
  if (v === 'notifications') loadNotifs(0)
  if (v === 'users'        && !users.value.length) loadUsers()
})
watch([notifSearch, notifTypeFilter], () => { if (activeTab.value === 'notifications') loadNotifs(0) })

onMounted(() => { loadCourses(); loadStats() })
</script>

<style scoped>
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

/* ── SHELL chiếm toàn bộ phần còn lại ── */
.admin-shell {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: var(--surface);
  border-top: none;
}

.sidebar {
  width: 160px; flex-shrink: 0;
  background: var(--surface2);
  border-right: 1.5px solid var(--border);
  display: flex; flex-direction: column;
  padding: .6rem .5rem; gap: .2rem;
  overflow: hidden;
}
.stab { display:flex; align-items:center; gap:.5rem; padding:.6rem .75rem; border-radius:8px; border:none; background:transparent; color:var(--muted); font-size:.81rem; font-weight:500; cursor:pointer; transition:all .15s; font-family:'Plus Jakarta Sans',sans-serif; width:100%; text-align:left; flex-shrink:0; }
.stab:hover  { background:rgba(0,0,0,.04); color:var(--text2); }
.stab.active { background:var(--accent); color:#fff; font-weight:700; }
.stab-label  { white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }

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

/* Panel body — scroll TRONG, không dãn ra ngoài */
.panel-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 1.1rem 1.4rem;
}

/* Dashboard */
.db-wrap { display:flex; flex-direction:column; gap:.9rem; }
.kpi-row { display:grid; grid-template-columns:repeat(5,1fr); gap:.7rem; }
.kpi-card { background:var(--surface2); border:1.5px solid var(--border); border-radius:12px; padding:.9rem 1rem; display:flex; align-items:center; gap:.75rem; }
.kpi-icon { width:38px; height:38px; border-radius:9px; display:flex; align-items:center; justify-content:center; font-size:1.1rem; flex-shrink:0; }
.kpi-n { font-size:1.35rem; font-weight:800; line-height:1.1; }
.kpi-l { font-size:.7rem; color:var(--muted); margin-top:.1rem; }

.chart-row { display:grid; grid-template-columns:1fr 240px; gap:.85rem; }
.chart-card { background:var(--surface2); border:1.5px solid var(--border); border-radius:12px; padding:.9rem 1rem; }
.chart-hd   { font-size:.8rem; font-weight:700; color:var(--text2); margin-bottom:.65rem; }

/* Canvas containers — PHẢI có height cố định, maintainAspectRatio:false */
.chart-box     { height: 155px; position: relative; }
.chart-box-sq  { height: 130px; position: relative; }
.chart-box-bar { height: 100px; position: relative; }

.score-legend { display:flex; flex-wrap:wrap; gap:.35rem .75rem; margin-top:.6rem; }
.sl-item { display:flex; align-items:center; gap:.32rem; font-size:.7rem; color:var(--muted); }
.sl-dot  { width:8px; height:8px; border-radius:50%; flex-shrink:0; }

/* Tab inner */
.tab-inner { display:flex; flex-direction:column; gap:.8rem; }

/* Filter */
.filter-bar { display:flex; gap:.6rem; flex-wrap:wrap; flex-shrink:0; }
.search-wrap  { position:relative; }
.search-ico   { position:absolute; left:.7rem; top:50%; transform:translateY(-50%); color:var(--muted); pointer-events:none; }
.filter-input { padding:.44rem .85rem .44rem 2.1rem; background:var(--surface); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.82rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; width:220px; }
.filter-input:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }

/* Table */
.tbl { width:100%; border-collapse:collapse; font-size:.83rem; }
.tbl th { background:#f8fafc; color:var(--muted); padding:.55rem .9rem; text-align:left; font-size:.7rem; font-weight:700; text-transform:uppercase; letter-spacing:.06em; border-bottom:1.5px solid var(--border); white-space:nowrap; position:sticky; top:0; z-index:1; }
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
.uid   { font-size:.7rem; color:var(--muted); }
.acts  { display:flex; gap:.28rem; flex-wrap:wrap; }
.ab    { padding:.25rem .58rem; font-size:.73rem; font-weight:600; border-radius:6px; cursor:pointer; border:1.5px solid var(--border); background:var(--surface); color:var(--text2); font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; white-space:nowrap; }
.ab:hover       { border-color:var(--accent); color:var(--accent); background:var(--accent-light); }
.ab.ab-red:hover{ border-color:var(--red); color:var(--red); background:var(--red-light); }

/* Progress mini */
.prog-mini       { display:flex; align-items:center; gap:.5rem; }
.prog-mini-track { width:70px; height:5px; background:var(--border); border-radius:100px; overflow:hidden; flex-shrink:0; }
.prog-mini-fill  { height:100%; background:var(--accent); border-radius:100px; transition:width .5s; }
.prog-mini-fill.done { background:var(--green); }
.prog-mini-pct   { font-size:.74rem; font-weight:700; color:var(--muted); }

/* Grade chips */
.schip   { font-size:.82rem; font-weight:800; padding:.2rem .55rem; border-radius:7px; }
.sc-exc  { background:var(--green-light);  color:var(--green); }
.sc-good { background:var(--accent-light); color:var(--accent); }
.sc-avg  { background:var(--yellow-light); color:var(--yellow); }
.sc-poor { background:var(--red-light);    color:var(--red); }
.no-score{ color:var(--muted); font-size:.8rem; }

/* Mini stats */
.mini-stats { display:flex; align-items:center; gap:1.2rem; background:var(--surface2); border:1.5px solid var(--border); border-radius:10px; padding:.65rem 1rem; flex-shrink:0; }
.ms-it { display:flex; flex-direction:column; align-items:center; }
.ms-n  { font-size:1.2rem; font-weight:800; }
.ms-l  { font-size:.67rem; color:var(--muted); margin-top:.1rem; }

/* Info box */
.grade-info-box { display:flex; align-items:center; gap:.6rem; background:var(--accent-light); border:1px solid #bfdbfe; border-radius:9px; padding:.65rem 1rem; font-size:.8rem; color:var(--accent); flex-shrink:0; }
.gib-icon { font-size:.9rem; flex-shrink:0; }

/* Lessons */
.lesson-list { display:flex; flex-direction:column; gap:.5rem; }
.lcard { background:var(--surface2); border:1.5px solid var(--border); border-radius:10px; padding:.8rem 1rem; display:flex; align-items:flex-start; gap:.8rem; }
.lcard:hover { border-color:var(--accent); }
.l-num { width:25px; height:25px; border-radius:7px; background:var(--accent-light); color:var(--accent); display:flex; align-items:center; justify-content:center; font-size:.74rem; font-weight:700; flex-shrink:0; margin-top:.1rem; }
.l-body  { flex:1; min-width:0; }
.l-title { font-size:.86rem; font-weight:600; margin-bottom:.15rem; }
.l-content { font-size:.75rem; color:var(--muted); display:-webkit-box; -webkit-line-clamp:1; -webkit-box-orient:vertical; overflow:hidden; }
.l-video { font-size:.72rem; color:var(--accent); display:block; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; margin-top:.1rem; }

/* Misc */
.sel    { padding:.44rem .8rem; background:var(--surface); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.82rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; }
.sel:focus { border-color:var(--accent); }
.sel-lg { min-width:200px; }
.badge { font-size:.66rem; font-weight:700; padding:.17rem .52rem; border-radius:100px; }
.badge-green  { background:var(--green-light);  color:var(--green);  border:1px solid #a7f3d0; }
.badge-blue   { background:var(--accent-light); color:var(--accent); border:1px solid #bfdbfe; }
.badge-purple { background:var(--purple-light); color:var(--purple); border:1px solid #ddd6fe; }
.badge-gray   { background:#f1f5f9; color:var(--muted); border:1px solid var(--border); }

.center-state { display:flex; flex-direction:column; align-items:center; justify-content:center; height:260px; gap:.7rem; color:var(--muted); }
.spinner { width:28px; height:28px; border:3px solid var(--border); border-top-color:var(--accent); border-radius:50%; animation:spin .7s linear infinite; }
@keyframes spin { to{transform:rotate(360deg)} }
.empty-ico { font-size:2rem; opacity:.4; }
.center-state p { font-size:.87rem; }

/* Buttons */
.btn { display:inline-flex; align-items:center; gap:.35rem; padding:.48rem 1rem; border-radius:8px; font-size:.83rem; font-weight:600; cursor:pointer; border:none; transition:all .15s; font-family:'Plus Jakarta Sans',sans-serif; white-space:nowrap; }
.btn-sm { padding:.36rem .8rem; font-size:.78rem; }
.btn-accent { background:var(--accent); color:#fff; }
.btn-accent:hover:not(:disabled) { background:var(--accent-dark); }
.btn-accent:disabled { opacity:.5; cursor:not-allowed; }
.btn-ghost { background:var(--surface); color:var(--text2); border:1.5px solid var(--border); }
.btn-ghost:hover { border-color:var(--accent); color:var(--accent); }
.field-hint { font-size:.75rem; color:var(--muted); margin-top:.4rem;
  background:var(--surface2); border:1px solid var(--border);
  border-radius:7px; padding:.4rem .7rem; }

/* Modal */
.modal-overlay { position:fixed; inset:0; background:rgba(15,23,42,.45); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; z-index:200; padding:1rem; }
.modal { background:var(--surface); border:1px solid var(--border); border-radius:16px; padding:1.8rem; width:100%; max-width:500px; box-shadow:var(--shadow-lg); }
.modal h2 { font-size:1.1rem; font-weight:800; margin-bottom:1.3rem; }
.modal-actions { display:flex; gap:.6rem; justify-content:flex-end; margin-top:1.3rem; }
.form-group { margin-bottom:.9rem; }
.form-group label { display:block; font-size:.73rem; font-weight:700; color:var(--muted); margin-bottom:.36rem; text-transform:uppercase; letter-spacing:.05em; }
.form-group input,.form-group textarea,.form-group select { width:100%; padding:.6rem .88rem; background:var(--surface); border:1.5px solid var(--border); border-radius:8px; color:var(--text); font-size:.87rem; outline:none; font-family:'Plus Jakarta Sans',sans-serif; }
.form-group input:focus,.form-group textarea:focus,.form-group select:focus { border-color:var(--accent); box-shadow:0 0 0 3px var(--accent-light); }
.f2 { display:grid; grid-template-columns:1fr 1fr; gap:0 1rem; }

/* Quiz tab */
.quiz-tab-actions { display:flex; align-items:center; justify-content:space-between; gap:1rem; margin-bottom:.75rem; flex-wrap:wrap; }
.quiz-editor-topbar { display:flex; align-items:center; gap:1rem; margin-bottom:1rem; padding-bottom:.75rem; border-bottom:1.5px solid var(--border); }

/* Toast */
.toast { position:fixed; bottom:2rem; right:2rem; padding:.72rem 1.1rem; border-radius:10px; font-size:.84rem; font-weight:600; z-index:999; animation:toastIn .25s ease; border:1.5px solid; box-shadow:var(--shadow); }
.toast.success { background:var(--green-light); color:var(--green); border-color:#6ee7b7; }
.toast.error   { background:var(--red-light);   color:var(--red);   border-color:#fca5a5; }
@keyframes toastIn { from{transform:translateX(110%);opacity:0} to{transform:translateX(0);opacity:1} }
.tbl-pagination {
  display:flex; align-items:center; gap:.3rem; margin-top:.8rem; flex-wrap:wrap;
}
.tbl-pagination .pg-info { margin-left:auto; font-size:.74rem; color:var(--muted); }
.pg-btn {
  min-width:30px; height:30px; padding:0 .35rem; border-radius:7px;
  border:1.5px solid var(--border); background:var(--surface); color:var(--text2);
  font-size:.76rem; font-weight:600; cursor:pointer;
  font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s;
  display:flex; align-items:center; justify-content:center;
}
.pg-btn:hover:not(:disabled):not(.ellipsis) { border-color:var(--accent); color:var(--accent); background:var(--accent-light); }
.pg-btn.active { background:var(--accent); color:#fff; border-color:var(--accent); }
.pg-btn:disabled { opacity:.4; cursor:not-allowed; }
.pg-btn.ellipsis { cursor:default; border-color:transparent; background:none; }
.review-total-badge { font-size:.74rem; font-weight:700; color:var(--accent); background:var(--accent-light); border:1.5px solid #bfdbfe; padding:.18rem .55rem; border-radius:100px; margin-left:auto; }
.star-row { display:flex; align-items:center; gap:1px; }
.rev-star { font-size:.88rem; color:#d1d5db; }
.rev-star.filled { color:#f59e0b; }
.star-num { font-size:.73rem; font-weight:700; color:var(--muted); margin-left:.3rem; }
.review-comment-cell { display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; font-size:.78rem; color:var(--text2); max-width:260px; }

/* ── REVIEWS TAB ── */
.rv-stats-row { display:flex; align-items:center; gap:.7rem; padding:.75rem 1rem; background:var(--surface2); border:1.5px solid var(--border); border-radius:12px; margin-bottom:.85rem; flex-wrap:wrap; }
.rv-stat { display:flex; flex-direction:column; align-items:center; min-width:56px; }
.rv-stat-n { font-size:1.3rem; font-weight:800; line-height:1; color:var(--accent); }
.rv-stat-l { font-size:.65rem; color:var(--muted); margin-top:.15rem; }
.rv-stat-star { flex:1; min-width:90px; }
.rv-star-bar-wrap { width:100%; height:5px; background:var(--border); border-radius:100px; overflow:hidden; margin-bottom:.2rem; }
.rv-star-bar-fill { height:100%; background:linear-gradient(90deg,#f59e0b,#fbbf24); border-radius:100px; transition:width .5s; }
.rv-star-label { font-size:.66rem; color:var(--muted); }
.rv-star-label b { color:var(--text2); }

.rv-star-btns { display:flex; gap:.25rem; flex-wrap:wrap; }
.rv-star-btn { padding:.28rem .6rem; border-radius:6px; border:1.5px solid var(--border); background:var(--surface2); color:var(--muted); font-size:.73rem; font-weight:600; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; }
.rv-star-btn.active { background:var(--accent); color:#fff; border-color:var(--accent); }
.rv-star-btn.active.s5 { background:#16a34a; border-color:#16a34a; }
.rv-star-btn.active.s4 { background:#65a30d; border-color:#65a30d; }
.rv-star-btn.active.s3 { background:#ca8a04; border-color:#ca8a04; }
.rv-star-btn.active.s2 { background:#ea580c; border-color:#ea580c; }
.rv-star-btn.active.s1 { background:#dc2626; border-color:#dc2626; }

.rv-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(280px,1fr)); gap:.75rem; }
.rv-card { background:var(--surface2); border:1.5px solid var(--border); border-radius:12px; padding:.9rem 1rem; display:flex; flex-direction:column; gap:.5rem; transition:border-color .15s; }
.rv-card:hover { border-color:#bfdbfe; }
.rv-card-top { display:flex; align-items:flex-start; justify-content:space-between; gap:.6rem; }
.rv-user { display:flex; align-items:center; gap:.6rem; flex:1; min-width:0; }
.rv-av-wrap { flex-shrink:0; }
.rv-av { width:30px; height:30px; border-radius:50%; background:linear-gradient(135deg,var(--accent),#7c3aed); color:#fff; font-size:.78rem; font-weight:700; display:flex; align-items:center; justify-content:center; }
.rv-av-img { width:30px; height:30px; border-radius:50%; object-fit:cover; display:block; }
.rv-user-info { flex:1; min-width:0; }
.rv-username { font-size:.8rem; font-weight:700; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.rv-course   { font-size:.71rem; color:var(--muted); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.rv-del-btn  { padding:.22rem .42rem; border-radius:6px; border:none; background:var(--red-light); color:var(--red); font-size:.72rem; cursor:pointer; flex-shrink:0; transition:all .15s; }
.rv-del-btn:hover { background:var(--red); color:#fff; }
.rv-stars-row { display:flex; align-items:center; gap:1px; }
.rv-s { font-size:.9rem; color:#d1d5db; }
.rv-s.filled { color:#f59e0b; }
.rv-score-label { font-size:.7rem; font-weight:700; color:var(--muted); margin-left:.3rem; }
.rv-date { font-size:.68rem; color:var(--muted); margin-left:auto; }
.rv-comment    { font-size:.79rem; color:var(--text2); line-height:1.55; margin:0; display:-webkit-box; -webkit-line-clamp:3; -webkit-box-orient:vertical; overflow:hidden; }
.rv-no-comment { font-size:.73rem; color:var(--muted); font-style:italic; margin:0; }

/* ── USER MANAGEMENT ── */
.user-stats-row { display:flex; gap:.6rem; flex-wrap:wrap; padding:.65rem 1rem; background:var(--surface2); border:1.5px solid var(--border); border-radius:10px; }
.ust-item { display:flex; flex-direction:column; align-items:center; min-width:60px; flex:1; }
.ust-n { font-size:1.25rem; font-weight:800; line-height:1; }
.ust-l { font-size:.65rem; color:var(--muted); margin-top:.1rem; }
.action-btns { display:flex; align-items:center; gap:.3rem; flex-wrap:wrap; }
.role-sel { font-size:.72rem; padding:.22rem .5rem; border-radius:6px; border:1.5px solid var(--border); background:var(--surface); color:var(--text); font-family:'Plus Jakarta Sans',sans-serif; cursor:pointer; }
.ab-orange { background:#fff7ed; color:#ea580c; border:none; border-radius:6px; padding:.22rem .42rem; cursor:pointer; font-size:.72rem; }
.ab-orange:hover { background:#ea580c; color:#fff; }
.ab-green  { background:#f0fdf4; color:#16a34a; border:none; border-radius:6px; padding:.22rem .42rem; cursor:pointer; font-size:.72rem; }
.ab-green:hover  { background:#16a34a; color:#fff; }
.last-admin-badge { font-size:.7rem; color:#9ca3af; background:#f1f5f9; border:1px solid #e2e8f0; border-radius:6px; padding:.2rem .5rem; white-space:nowrap; cursor:default; }
.row-banned { opacity:.6; background:var(--red-light); }
.badge-green { background:#dcfce7; color:#16a34a; }
.badge-red   { background:var(--red-light); color:var(--red); }

/* ── NOTIFICATIONS TAB ── */
.notif-top-row { display:flex; align-items:center; justify-content:space-between; gap:.8rem; flex-wrap:wrap; }
.notif-stats { display:flex; gap:.5rem; flex-wrap:wrap; }
.nst { display:flex; flex-direction:column; align-items:center; padding:.45rem .7rem; border-radius:10px; background:var(--surface2); border:1.5px solid var(--border); min-width:52px; }
.nst-n { font-size:1rem; font-weight:800; line-height:1; }
.nst-l { font-size:.6rem; color:var(--muted); margin-top:.1rem; }

.btn-broadcast { display:flex; align-items:center; gap:.4rem; padding:.45rem 1rem; border-radius:10px; border:1.5px solid var(--accent); background:var(--accent-light); color:var(--accent); font-size:.78rem; font-weight:700; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; }
.btn-broadcast:hover { background:var(--accent); color:#fff; }
.bc-arr { font-size:.68rem; opacity:.7; }

.bc-panel { background:var(--surface2); border:1.5px solid var(--border); border-radius:12px; padding:1rem 1.1rem; }
.bc-panel-title { font-size:.82rem; font-weight:700; margin-bottom:.75rem; color:var(--accent); }
.bc-grid { display:grid; grid-template-columns:1fr 1fr; gap:.6rem; }
.bc-full { grid-column: 1 / -1; }
.bc-field { display:flex; flex-direction:column; gap:.22rem; }
.bc-label { font-size:.7rem; font-weight:600; color:var(--muted); }
.req { color:var(--red); }
.bc-input { padding:.42rem .7rem; border-radius:8px; border:1.5px solid var(--border); background:var(--surface); color:var(--text); font-size:.8rem; font-family:'Plus Jakarta Sans',sans-serif; }
.bc-input:focus { outline:none; border-color:var(--accent); }
.bc-textarea { padding:.42rem .7rem; border-radius:8px; border:1.5px solid var(--border); background:var(--surface); color:var(--text); font-size:.8rem; font-family:'Plus Jakarta Sans',sans-serif; resize:vertical; }
.bc-textarea:focus { outline:none; border-color:var(--accent); }
.bc-send-wrap { justify-content:flex-end; }
.btn-send { display:flex; align-items:center; gap:.35rem; padding:.45rem 1.1rem; border-radius:8px; border:none; background:var(--accent); color:#fff; font-size:.78rem; font-weight:700; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; transition:background .15s; }
.btn-send:hover:not(:disabled) { background:#1d4ed8; }
.btn-send:disabled { opacity:.5; cursor:not-allowed; }

.notif-type-btns { display:flex; gap:.25rem; flex-wrap:wrap; }
.ntb { padding:.28rem .65rem; border-radius:7px; border:1.5px solid var(--border); background:var(--surface2); color:var(--muted); font-size:.72rem; font-weight:600; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; }
.ntb.ntb-active, .ntb:hover { background:var(--accent); color:#fff; border-color:var(--accent); }
.ntb.ntb-sys.ntb-active    { background:#2563eb; border-color:#2563eb; }
.ntb.ntb-grade.ntb-active  { background:#d97706; border-color:#d97706; }
.ntb.ntb-enroll.ntb-active { background:#16a34a; border-color:#16a34a; }
.ntb.ntb-done.ntb-active   { background:#7c3aed; border-color:#7c3aed; }
.btn-clear-all { padding:.28rem .65rem; border-radius:7px; border:1.5px solid var(--red); background:var(--red-light); color:var(--red); font-size:.72rem; font-weight:600; cursor:pointer; font-family:'Plus Jakarta Sans',sans-serif; transition:all .15s; }
.btn-clear-all:hover { background:var(--red); color:#fff; }

.notif-empty { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:3rem 1rem; gap:.4rem; }
.notif-empty-ico  { font-size:2.5rem; opacity:.3; }
.notif-empty-text { font-size:.88rem; font-weight:600; color:var(--muted); }
.notif-empty-sub  { font-size:.75rem; color:var(--muted); opacity:.7; }

.notif-list { background:var(--surface); border:1.5px solid var(--border); border-radius:12px; overflow:hidden; }
.notif-row { display:flex; align-items:center; gap:.8rem; padding:.75rem 1rem; border-bottom:1px solid var(--border); transition:background .12s; }
.notif-row:last-child { border-bottom:none; }
.notif-row:hover { background:var(--surface2); }
.nr-unread { border-left:3px solid var(--accent); padding-left:calc(1rem - 3px); }
.nr-read   { opacity:.8; }

.nr-type { display:flex; align-items:center; gap:.35rem; flex-shrink:0; width:110px; }
.nr-type-dot { width:7px; height:7px; border-radius:50%; flex-shrink:0; }
.dot-system     { background:#2563eb; }
.dot-grade      { background:#d97706; }
.dot-enrollment { background:#16a34a; }
.dot-completion { background:#7c3aed; }
.nr-type-label { font-size:.65rem; font-weight:700; padding:.12rem .4rem; border-radius:100px; }
.nt-system     { background:#eff6ff; color:#2563eb; }
.nt-grade      { background:#fef3c7; color:#d97706; }
.nt-enrollment { background:#dcfce7; color:#16a34a; }
.nt-completion { background:#f3e8ff; color:#7c3aed; }

.nr-main  { flex:1; min-width:0; }
.nr-title { font-size:.8rem; font-weight:700; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.nr-msg   { font-size:.72rem; color:var(--muted); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }

.nr-meta  { display:flex; flex-direction:column; align-items:flex-end; gap:.1rem; flex-shrink:0; }
.nr-user  { font-size:.67rem; color:var(--muted); }
.nr-date  { font-size:.67rem; color:var(--muted); }

.nr-status { flex-shrink:0; width:22px; text-align:center; }
.nr-read-dot { font-size:.75rem; font-weight:700; }
.dot-read   { color:#16a34a; }
.dot-unread { color:#f59e0b; }

.nr-del { padding:.2rem .38rem; border:none; background:transparent; color:var(--muted); font-size:.72rem; cursor:pointer; border-radius:5px; flex-shrink:0; transition:all .12s; }
.nr-del:hover { background:var(--red-light); color:var(--red); }

.notif-pagination { display:flex; align-items:center; justify-content:center; gap:.5rem; padding:.6rem; }
.npg-btn  { padding:.3rem .75rem; border-radius:7px; border:1.5px solid var(--border); background:var(--surface2); color:var(--text); font-size:.75rem; font-weight:600; cursor:pointer; transition:all .15s; }
.npg-btn:hover:not(:disabled) { border-color:var(--accent); color:var(--accent); }
.npg-btn:disabled { opacity:.4; cursor:not-allowed; }
.npg-pages { display:flex; gap:.2rem; }
.npg-num  { width:28px; height:28px; border-radius:6px; display:flex; align-items:center; justify-content:center; font-size:.74rem; font-weight:600; cursor:pointer; color:var(--muted); transition:all .12s; }
.npg-num:hover  { background:var(--accent-light); color:var(--accent); }
.npg-num.npg-active { background:var(--accent); color:#fff; }
</style>