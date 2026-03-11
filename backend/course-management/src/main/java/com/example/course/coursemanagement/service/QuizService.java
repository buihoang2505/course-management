package com.example.course.coursemanagement.service;

import com.example.course.coursemanagement.dto.*;
import com.example.course.coursemanagement.entity.*;
import com.example.course.coursemanagement.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepository             quizRepo;
    private final QuestionRepository         questionRepo;
    private final ChoiceRepository           choiceRepo;
    private final QuizAttemptRepository      attemptRepo;
    private final AttemptAnswerRepository    answerRepo;
    private final LessonRepository           lessonRepo;
    private final UserRepository             userRepo;
    private final EnrollmentRepository       enrollmentRepo;
    private final GradeRepository            gradeRepo;
    private final LessonCompletionRepository lessonCompletionRepo;
    private final CertificateService         certificateService;

    // ════════════════════════════════════════════════════
    //  CÔNG THỨC TÍNH ĐIỂM TỔNG KẾT
    //  finalScore = quiz_score(thang 10) * 0.4 + progress_score(thang 10) * 0.6
    //  Đúng 100% quiz → quiz_score = 10
    // ════════════════════════════════════════════════════
    private static final double QUIZ_WEIGHT     = 0.4;
    private static final double PROGRESS_WEIGHT = 0.6;

    // ════════════════════════════════════════════════════
    //  ADMIN — CRUD QUIZ
    // ════════════════════════════════════════════════════

    public QuizAdminDTO createQuiz(Long lessonId, CreateQuizRequest req) {
        Lesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài học id=" + lessonId));
        if (quizRepo.existsByLessonId(lessonId))
            throw new IllegalStateException("Bài học này đã có quiz. Hãy chỉnh sửa quiz hiện tại thay vì tạo mới.");

        Quiz quiz = Quiz.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .timeLimitMinutes(req.getTimeLimitMinutes())
                .passingScore(req.getPassingScore() != null ? req.getPassingScore() : 60)
                .maxAttempts(req.getMaxAttempts())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .lesson(lesson)
                .build();
        return QuizAdminDTO.from(quizRepo.save(quiz));
    }

    public QuizAdminDTO updateQuiz(Long quizId, CreateQuizRequest req) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy quiz id=" + quizId));
        quiz.setTitle(req.getTitle());
        quiz.setDescription(req.getDescription());
        quiz.setTimeLimitMinutes(req.getTimeLimitMinutes());
        if (req.getPassingScore() != null) quiz.setPassingScore(req.getPassingScore());
        quiz.setMaxAttempts(req.getMaxAttempts());
        if (req.getIsActive() != null) quiz.setIsActive(req.getIsActive());
        return QuizAdminDTO.from(quizRepo.save(quiz));
    }

    public void deleteQuiz(Long quizId) {
        if (!quizRepo.existsById(quizId))
            throw new EntityNotFoundException("Không tìm thấy quiz id=" + quizId);
        quizRepo.deleteById(quizId);
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — DANH SÁCH QUIZ
    // ════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<QuizAdminDTO> getAllQuizzes() {
        return quizRepo.findAllWithLessonAndCourse()
                .stream().map(QuizAdminDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizAdminDTO> getQuizzesByCourse(Long courseId) {
        return quizRepo.findAllByCourseId(courseId)
                .stream().map(QuizAdminDTO::from).collect(Collectors.toList());
    }

    /**
     * Lấy quiz đầy đủ cho admin edit (có questions + choices + isCorrect).
     * Tách 2 query để tránh MultipleBagFetchException.
     */
    @Transactional(readOnly = true)
    public QuizDetailAdminDTO getQuizDetail(Long quizId) {
        Quiz quiz = quizRepo.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy quiz id=" + quizId));
        List<Question> questions = quizRepo.findQuestionsWithChoicesByQuizId(quizId);
        return QuizDetailAdminDTO.from(quiz, questions);
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — QUESTION CRUD
    // ════════════════════════════════════════════════════

    public QuestionAdminDTO addQuestion(Long quizId, AddQuestionRequest req) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy quiz id=" + quizId));

        long correctCount = req.getChoices().stream()
                .filter(AddQuestionRequest.ChoiceItem::isCorrect).count();
        if (correctCount != 1)
            throw new IllegalArgumentException("Phải có đúng 1 đáp án đúng, hiện có: " + correctCount);

        int orderNum = (int) questionRepo.countByQuizId(quizId) + 1;
        Question question = Question.builder()
                .quiz(quiz).content(req.getContent())
                .explanation(req.getExplanation())
                .points(req.getPoints() != null ? req.getPoints() : 1)
                .orderNum(orderNum).build();
        question = questionRepo.save(question);

        for (AddQuestionRequest.ChoiceItem item : req.getChoices()) {
            choiceRepo.save(Choice.builder()
                    .question(question).content(item.getContent())
                    .isCorrect(item.isCorrect()).build());
        }
        return QuestionAdminDTO.from(questionRepo.findById(question.getId()).orElse(question));
    }

    public QuestionAdminDTO updateQuestion(Long questionId, AddQuestionRequest req) {
        Question q = questionRepo.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy câu hỏi id=" + questionId));

        long correctCount = req.getChoices().stream()
                .filter(AddQuestionRequest.ChoiceItem::isCorrect).count();
        if (correctCount != 1)
            throw new IllegalArgumentException("Phải có đúng 1 đáp án đúng");

        q.setContent(req.getContent());
        q.setExplanation(req.getExplanation());
        if (req.getPoints() != null) q.setPoints(req.getPoints());

        // FIX FK constraint: attempt_answers.choice_id → choices(id)
        // Phải NULL hóa choice_id trong attempt_answers trước khi xóa choices
        // (giữ lịch sử làm bài, chỉ mất thông tin choice đã chọn)
        answerRepo.nullifyChoiceByQuestionId(questionId);
        answerRepo.flush();

        // Sau đó mới xóa choices cũ bằng JPQL (tránh ObjectDeletedException)
        choiceRepo.deleteByQuestionId(questionId);
        choiceRepo.flush();

        q = questionRepo.save(q);

        final Question finalQ = q;
        List<Choice> newChoices = req.getChoices().stream().map(item ->
                Choice.builder()
                        .question(finalQ)
                        .content(item.getContent())
                        .isCorrect(item.isCorrect())
                        .build()
        ).collect(Collectors.toList());
        choiceRepo.saveAll(newChoices);

        return QuestionAdminDTO.from(questionRepo.findById(questionId).orElse(finalQ));
    }

    public void deleteQuestion(Long questionId) {
        Question q = questionRepo.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy câu hỏi"));
        int order   = q.getOrderNum();
        Long quizId = q.getQuiz().getId();
        questionRepo.delete(q);
        questionRepo.shiftOrderNumAfterDelete(quizId, order);
    }

    // ════════════════════════════════════════════════════
    //  STUDENT — XEM QUIZ METADATA
    // ════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public QuizMetaDTO getQuizMetaForLesson(Long lessonId) {
        // Dùng List tránh IncorrectResultSizeDataAccessException khi JOIN FETCH collection
        List<Quiz> results = quizRepo.findActiveByLessonIdWithQuestions(lessonId);
        if (results.isEmpty()) return null;
        return QuizMetaDTO.from(results.get(0));
    }

    @Transactional(readOnly = true)
    public QuizDTO getQuizForStudent(Long lessonId, Long userId) {
        Quiz quiz = quizRepo.findActiveByLessonId(lessonId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Bài học này chưa có quiz hoặc quiz chưa được kích hoạt"));

        if (!canAttempt(quiz.getId(), userId))
            throw new IllegalStateException("Bạn đã dùng hết " + quiz.getMaxAttempts() + " lượt làm bài");

        // FIX: build DTO trực tiếp, không ghép vào lazy collection entity
        // (clear+addAll+orphanRemoval = xóa choices khỏi DB)
        List<Question> questions = quizRepo.findQuestionsWithChoicesByQuizId(quiz.getId());

        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setTimeLimitMinutes(quiz.getTimeLimitMinutes());
        dto.setPassingScore(quiz.getPassingScore());
        dto.setMaxAttempts(quiz.getMaxAttempts());
        dto.setQuestionCount(questions.size());
        dto.setQuestions(questions.stream().map(QuestionDTO::from).collect(Collectors.toList()));
        return dto;
    }

    @Transactional(readOnly = true)
    public boolean canAttempt(Long quizId, Long userId) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy quiz"));
        if (quiz.getMaxAttempts() == null) return true;
        return attemptRepo.countByQuizIdAndUserId(quizId, userId) < quiz.getMaxAttempts();
    }

    // ════════════════════════════════════════════════════
    //  STUDENT — NỘP BÀI & CHẤM ĐIỂM
    // ════════════════════════════════════════════════════

    public QuizResultDTO submitQuiz(Long quizId, SubmitQuizRequest req) {
        // FIX MultipleBagFetchException: tách thành 2 query, không JOIN FETCH 2 collection cùng lúc
        Quiz quiz = quizRepo.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy quiz id=" + quizId));

        // Query 2: questions + choices (tách riêng)
        List<Question> questionsWithChoices = quizRepo.findQuestionsWithChoicesByQuizId(quizId);

        // Build map từ questionsWithChoices — không đụng lazy collection của entity
        Map<Long, Long>         correctMap  = new HashMap<>();
        Map<Long, List<Choice>> choicesMap  = new HashMap<>();
        for (Question q : questionsWithChoices) {
            choicesMap.put(q.getId(), q.getChoices());
            q.getChoices().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsCorrect()))
                    .findFirst()
                    .ifPresent(c -> correctMap.put(q.getId(), c.getId()));
        }

        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user id=" + req.getUserId()));

        if (!canAttempt(quizId, req.getUserId()))
            throw new IllegalStateException("Bạn đã dùng hết lượt làm bài");

        // Server-side time validation — chống cheat bằng cách reload lại trang
        if (quiz.getTimeLimitMinutes() != null && req.getTimeSpentSeconds() != null) {
            int maxSeconds = quiz.getTimeLimitMinutes() * 60;
            if (req.getTimeSpentSeconds() > maxSeconds + 30) { // +30s buffer network
                req.setTimeSpentSeconds(maxSeconds);
            }
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz).user(user)
                .totalQuestions(quiz.getQuestions().size())
                .timeSpentSeconds(req.getTimeSpentSeconds())
                .startedAt(LocalDateTime.now())
                .build();
        attempt = attemptRepo.save(attempt);

        int correct = 0, totalPts = 0, earnedPts = 0;
        List<AttemptAnswer> answers = new ArrayList<>();

        for (SubmitQuizRequest.AnswerItem item : req.getAnswers()) {
            Question q = questionsWithChoices.stream()
                    .filter(x -> x.getId().equals(item.getQuestionId()))
                    .findFirst().orElse(null);
            if (q == null) continue;

            totalPts += q.getPoints();
            boolean ok = item.getChoiceId() != null
                    && item.getChoiceId().equals(correctMap.get(item.getQuestionId()));
            if (ok) { correct++; earnedPts += q.getPoints(); }

            AttemptAnswer aa = AttemptAnswer.builder()
                    .attempt(attempt).question(q).isCorrect(ok).build();
            if (item.getChoiceId() != null) {
                choicesMap.getOrDefault(item.getQuestionId(), Collections.emptyList()).stream()
                        .filter(c -> c.getId().equals(item.getChoiceId()))
                        .findFirst().ifPresent(aa::setChoice);
            }
            answers.add(aa);
        }
        answerRepo.saveAll(answers);

        double quizScore100 = totalPts > 0
                ? Math.round(earnedPts * 100.0 / totalPts * 10) / 10.0 : 0.0;

        attempt.setScore(quizScore100);
        attempt.setCorrectCount(correct);
        attempt.setPassed(quizScore100 >= quiz.getPassingScore());
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setAnswers(answers);
        attempt = attemptRepo.save(attempt);

        // Cập nhật điểm tổng kết tự động sau khi nộp bài
        updateFinalGrade(user, quiz, quizId);

        // ── Auto cấp chứng chỉ nếu pass và đủ điều kiện ──────────
        if (attempt.getPassed()) {
            try {
                Long courseId = quiz.getLesson().getCourse().getId();
                certificateService.tryIssueCertificate(req.getUserId(), courseId);
            } catch (Exception e) {
                // Không để lỗi cert phá luồng nộp bài
                System.err.println("[QuizService] certificate check error: " + e.getMessage());
            }
        }

        return QuizResultDTO.from(
                attemptRepo.findByIdWithAnswers(attempt.getId()).orElse(attempt));
    }

    /**
     * Tính và lưu điểm tổng kết vào Grade + Enrollment.
     *
     * Công thức (thang 10):
     *   quiz_score    = bestAttemptScore / 100 * 10  (đúng 100% → 10 điểm)
     *   progress_score = completedLessons / totalLessons * 10
     *   finalScore    = quiz_score * 40% + progress_score * 60%
     *
     * Chỉ cập nhật khi điểm mới cao hơn điểm cũ.
     */
    private void updateFinalGrade(User user, Quiz quiz, Long quizId) {
        try {
            Long courseId = quiz.getLesson().getCourse().getId();

            Enrollment enrollment = enrollmentRepo
                    .findByUserIdAndCourseId(user.getId(), courseId).orElse(null);
            if (enrollment == null) return;

            // Điểm quiz cao nhất của user cho quiz này (thang 100)
            double bestScore100 = attemptRepo
                    .findByQuizIdAndUserId(quizId, user.getId()).stream()
                    .mapToDouble(a -> a.getScore() != null ? a.getScore() : 0.0)
                    .max().orElse(0.0);

            double quizScore10     = Math.round(bestScore100 / 10.0 * 10) / 10.0;

            // Tiến độ học tập
            long totalLessons     = lessonRepo.countByCourseId(courseId);
            long completedLessons = lessonCompletionRepo.countByUserIdAndCourseId(user.getId(), courseId);
            double progressScore10 = totalLessons > 0
                    ? Math.round(completedLessons * 10.0 / totalLessons * 10) / 10.0 : 0.0;

            double finalScore = Math.round(
                    (quizScore10 * QUIZ_WEIGHT + progressScore10 * PROGRESS_WEIGHT) * 10) / 10.0;

            String feedback = String.format(
                    "Tự động tính: Quiz %.1f/10 (40%%) + Tiến độ %.1f/10 (60%%)",
                    quizScore10, progressScore10);

            Grade grade = gradeRepo.findByEnrollmentId(enrollment.getId()).orElse(null);
            if (grade == null) {
                grade = Grade.builder()
                        .enrollment(enrollment)
                        .score(finalScore)
                        .feedback(feedback)
                        .build();
                gradeRepo.save(grade);
            } else if (finalScore > (grade.getScore() != null ? grade.getScore() : 0.0)) {
                grade.setScore(finalScore);
                grade.setFeedback(feedback);
                gradeRepo.save(grade);
            }

            // Cập nhật finalScore và result trên Enrollment
            enrollment.setFinalScore(finalScore);
            enrollment.setResult(finalScore >= 5.0
                    ? Enrollment.ResultStatus.PASSED
                    : Enrollment.ResultStatus.FAILED);
            enrollmentRepo.save(enrollment);

        } catch (Exception e) {
            // Không để lỗi điểm phá luồng nộp bài
            System.err.println("[QuizService] updateFinalGrade error: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════
    //  STUDENT — LỊCH SỬ
    // ════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<QuizAttemptDTO> getAttemptHistory(Long quizId, Long userId) {
        return attemptRepo.findByQuizIdAndUserIdOrderByStartedAtDesc(quizId, userId)
                .stream().map(QuizAttemptDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizResultDTO getAttemptResult(Long attemptId) {
        return attemptRepo.findByIdWithAnswers(attemptId)
                .map(QuizResultDTO::from)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy kết quả làm bài"));
    }

    // ════════════════════════════════════════════════════
    //  ADMIN — THỐNG KÊ
    // ════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public QuizStatsDTO getStats(Long quizId) {
        // FIX: tách 2 query thay vì JOIN FETCH 2 collection gây MultipleBagFetchException
        Quiz quiz = quizRepo.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy quiz"));
        List<Question> questionsWithChoices = quizRepo.findQuestionsWithChoicesByQuizId(quizId);
        List<QuizAttempt> attempts = attemptRepo.findByQuizId(quizId);

        QuizStatsDTO stats = new QuizStatsDTO();
        stats.setQuizId(quizId);
        stats.setQuizTitle(quiz.getTitle());
        stats.setTotalAttempts(attempts.size());
        stats.setUniqueStudents((int) attempts.stream()
                .map(a -> a.getUser().getId()).distinct().count());

        if (!attempts.isEmpty()) {
            DoubleSummaryStatistics s = attempts.stream()
                    .mapToDouble(QuizAttempt::getScore).summaryStatistics();
            stats.setAverageScore(Math.round(s.getAverage() * 10) / 10.0);
            stats.setHighestScore((int) s.getMax());
            stats.setLowestScore((int) s.getMin());
            stats.setPassRate(Math.round(
                    attempts.stream().filter(QuizAttempt::getPassed).count()
                            * 100.0 / attempts.size() * 10) / 10.0);

            int[] dist = new int[10];
            attempts.forEach(a -> { int b = Math.min((int)(a.getScore()/10), 9); dist[b]++; });
            List<Integer> distList = new ArrayList<>();
            for (int d : dist) distList.add(d);
            stats.setScoreDistribution(distList);
        }

        List<Object[]> raw = answerRepo.getCorrectRateByQuestion(quizId);
        Map<Long, Object[]> rawMap = new HashMap<>();
        raw.forEach(r -> rawMap.put((Long) r[0], r));

        stats.setQuestionStats(questionsWithChoices.stream().map(q -> {
            QuizStatsDTO.QuestionStat qs = new QuizStatsDTO.QuestionStat();
            qs.setQuestionId(q.getId());
            qs.setQuestionContent(q.getContent());
            Object[] r = rawMap.get(q.getId());
            if (r != null) {
                long tot = ((Number)r[1]).longValue(), cor = ((Number)r[2]).longValue();
                qs.setTotalAnswered(tot);
                qs.setCorrectCount(cor);
                qs.setCorrectRate(tot > 0 ? Math.round(cor*100.0/tot*10)/10.0 : 0.0);
            }
            return qs;
        }).collect(Collectors.toList()));

        return stats;
    }
}