package com.example.course.coursemanagement.service;

/**
 * Email templates — thiết kế chuẩn SaaS modern (Notion/Linear style).
 * Tất cả inline style, table-based layout cho Gmail/Outlook compatibility.
 */
public class EmailTemplateBuilder {

    private static final String BLUE   = "#2563eb";
    private static final String NAVY   = "#1e3a5f";
    private static final String GREEN  = "#16a34a";
    private static final String GOLD   = "#ca8a04";
    private static final String DARK   = "#0f172a";
    private static final String URL    = "http://localhost:5173";

    // ══════════════════════════════════════════════════════════
    //  CORE WRAPPER — email học viên
    // ══════════════════════════════════════════════════════════
    private static String wrap(String preheader, String topBarColor, String content) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width,initial-scale=1">
              <meta name="color-scheme" content="light">
            </head>
            <body style="margin:0;padding:0;background:#f8fafc;
                         font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif;
                         -webkit-text-size-adjust:100%%">

              <!-- Preheader -->
              <div style="display:none;max-height:0;overflow:hidden;font-size:1px;color:#f8fafc">
                %s &nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌
              </div>

              <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                     style="background:#f8fafc;min-height:100vh">
                <tr><td align="center" style="padding:48px 16px 40px">

                  <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                         style="max-width:580px">

                    <!-- ── LOGO ── -->
                    <tr>
                      <td align="center" style="padding-bottom:32px">
                        <table cellpadding="0" cellspacing="0" border="0">
                          <tr>
                            <td style="background:#fff;border:1px solid #e2e8f0;
                                       border-radius:12px;padding:10px 20px;
                                       box-shadow:0 1px 3px rgba(0,0,0,.06)">
                              <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                  <td style="font-size:18px;padding-right:8px;
                                             vertical-align:middle;line-height:1">🎓</td>
                                  <td style="font-size:16px;font-weight:800;color:#0f172a;
                                             letter-spacing:-.5px;vertical-align:middle;
                                             line-height:1">EduFlow</td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- ── CARD ── -->
                    <tr>
                      <td style="background:#fff;border-radius:16px;
                                 border:1px solid #e2e8f0;overflow:hidden;
                                 box-shadow:0 4px 6px -1px rgba(0,0,0,.05),
                                            0 2px 4px -2px rgba(0,0,0,.05)">

                        <!-- Top color bar -->
                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                          <tr>
                            <td style="height:4px;background:%s;
                                       font-size:0;line-height:0">&nbsp;</td>
                          </tr>
                        </table>

                        <!-- Content -->
                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                          <tr>
                            <td style="padding:40px 44px 36px">
                              %s
                            </td>
                          </tr>
                        </table>

                      </td>
                    </tr>

                    <!-- ── FOOTER ── -->
                    <tr>
                      <td style="padding:28px 0 0;text-align:center">
                        <table cellpadding="0" cellspacing="0" border="0"
                               style="margin:0 auto 14px">
                          <tr>
                            <td style="padding:0 12px">
                              <a href="%s/courses"
                                 style="font-size:12px;color:#94a3b8;text-decoration:none">
                                Khóa học</a>
                            </td>
                            <td style="font-size:12px;color:#cbd5e1">·</td>
                            <td style="padding:0 12px">
                              <a href="%s/my-courses"
                                 style="font-size:12px;color:#94a3b8;text-decoration:none">
                                Của tôi</a>
                            </td>
                            <td style="font-size:12px;color:#cbd5e1">·</td>
                            <td style="padding:0 12px">
                              <a href="%s/certificates"
                                 style="font-size:12px;color:#94a3b8;text-decoration:none">
                                Chứng chỉ</a>
                            </td>
                          </tr>
                        </table>
                        <p style="font-size:11px;color:#cbd5e1;margin:0;line-height:1.8">
                          © 2026 EduFlow Academy
                          · <a href="%s" style="color:#cbd5e1;text-decoration:none">
                              eduflow.vn</a>
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>

            </body>
            </html>
            """.formatted(preheader, topBarColor, content,
                URL, URL, URL, URL);
    }

    // ══════════════════════════════════════════════════════════
    //  CORE WRAPPER — email admin (dark)
    // ══════════════════════════════════════════════════════════
    private static String wrapAdmin(String preheader, String accentColor, String content) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width,initial-scale=1">
            </head>
            <body style="margin:0;padding:0;background:#020617;
                         font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif">
              <div style="display:none;max-height:0;overflow:hidden;font-size:1px;color:#020617">
                %s &nbsp;‌&nbsp;‌&nbsp;‌
              </div>
              <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                     style="background:#020617;min-height:100vh">
                <tr><td align="center" style="padding:48px 16px 40px">
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                         style="max-width:560px">

                    <!-- Logo row -->
                    <tr>
                      <td style="padding-bottom:28px">
                        <table cellpadding="0" cellspacing="0" border="0">
                          <tr>
                            <td style="font-size:16px;vertical-align:middle;
                                       padding-right:8px;line-height:1">🎓</td>
                            <td style="font-size:15px;font-weight:800;color:#f8fafc;
                                       vertical-align:middle;letter-spacing:-.4px">EduFlow</td>
                            <td style="vertical-align:middle;padding-left:8px">
                              <span style="font-size:10px;font-weight:700;color:%s;
                                           text-transform:uppercase;letter-spacing:.1em;
                                           background:rgba(255,255,255,.06);
                                           padding:3px 8px;border-radius:100px;
                                           border:1px solid rgba(255,255,255,.08)">Admin</span>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>

                    <!-- Card -->
                    <tr>
                      <td style="background:#0f172a;border-radius:14px;
                                 border:1px solid #1e293b;overflow:hidden">
                        <!-- Accent line -->
                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                          <tr><td style="height:3px;background:%s;font-size:0">&nbsp;</td></tr>
                        </table>
                        <!-- Body -->
                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                          <tr><td style="padding:36px 40px">%s</td></tr>
                        </table>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="padding-top:24px;text-align:center">
                        <p style="font-size:11px;color:#334155;margin:0">
                          © 2026 EduFlow Academy · Admin notification
                          · <a href="%s/admin" style="color:#334155;text-decoration:none">
                              Dashboard</a>
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(preheader, accentColor, accentColor, content, URL);
    }

    // ── Helpers ───────────────────────────────────────────────

    /** Nút CTA chính */
    private static String btn(String href, String label, String bg) {
        return """
            <table cellpadding="0" cellspacing="0" border="0"
                   style="margin:32px auto 0">
              <tr>
                <td style="border-radius:10px;background:%s">
                  <a href="%s"
                     style="display:inline-block;padding:14px 32px;
                            font-size:15px;font-weight:700;color:#fff;
                            text-decoration:none;letter-spacing:-.2px;
                            border-radius:10px;background:%s">%s</a>
                </td>
              </tr>
            </table>
            """.formatted(bg, href, bg, label);
    }

    /** Stat pill */
    private static String stat(String val, String label, String color) {
        return """
            <td style="text-align:center;padding:16px 10px;
                       background:#f8fafc;border:1px solid #e2e8f0;
                       border-radius:12px;vertical-align:middle">
              <div style="font-size:22px;font-weight:800;color:%s;
                          line-height:1;margin-bottom:5px">%s</div>
              <div style="font-size:10px;font-weight:600;color:#94a3b8;
                          text-transform:uppercase;letter-spacing:.08em">%s</div>
            </td>
            """.formatted(color, val, label);
    }

    /** Stat pill dark */
    private static String statD(String val, String label, String color) {
        return """
            <td style="text-align:center;padding:14px 10px;
                       background:#020617;border:1px solid #1e293b;
                       border-radius:10px;vertical-align:middle">
              <div style="font-size:20px;font-weight:800;color:%s;
                          line-height:1;margin-bottom:4px">%s</div>
              <div style="font-size:10px;font-weight:600;color:#475569;
                          text-transform:uppercase;letter-spacing:.08em">%s</div>
            </td>
            """.formatted(color, val, label);
    }

    /** Label badge nhỏ */
    private static String badge(String text, String color) {
        return """
            <p style="margin:0 0 14px">
              <span style="display:inline-block;font-size:11px;font-weight:700;
                           color:%s;text-transform:uppercase;letter-spacing:.12em;
                           padding:4px 10px;border-radius:100px;
                           background:%s1a;border:1px solid %s33">%s</span>
            </p>
            """.formatted(color, color, color, text);
    }

    /** Divider line */
    private static String hr(String color) {
        return "<table width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
                "<tr><td style=\"height:1px;background:" + color +
                ";font-size:0;margin:24px 0\">&nbsp;</td></tr></table>";
    }

    /** Info row trong dark card (admin) */
    private static String infoRow(String label, String value, String valueColor, boolean border) {
        String borderStyle = border ? "border-top:1px solid #1e293b;" : "";
        return """
            <tr>
              <td style="%sfont-size:12px;color:#475569;padding:10px 0 10px;
                         width:120px;vertical-align:top">%s</td>
              <td style="%sfont-size:14px;font-weight:700;color:%s;
                         padding:10px 0 10px;vertical-align:top">%s</td>
            </tr>
            """.formatted(borderStyle, label, borderStyle, valueColor, value);
    }


    // ══════════════════════════════════════════════════════════
    //  1. WELCOME
    // ══════════════════════════════════════════════════════════
    public static String welcome(String username, String email) {
        String body = badge("Tài khoản mới", BLUE) + """
            <h1 style="font-size:28px;font-weight:800;color:#0f172a;
                       margin:0 0 14px;letter-spacing:-.7px;line-height:1.2">
              Chào mừng, %s! 🎉
            </h1>
            <p style="font-size:15px;color:#475569;line-height:1.8;margin:0 0 32px">
              Tài khoản EduFlow của bạn đã sẵn sàng. Hàng trăm khóa học
              đang chờ bạn — bắt đầu học ngay hôm nay!
            </p>

            <table width="100%%" cellpadding="0" cellspacing="8" border="0"
                   style="margin-bottom:32px">
              <tr>
                %s<td width="8"></td>%s<td width="8"></td>%s
              </tr>
            </table>

            %s

            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="margin-top:32px">
              <tr>
                <td style="background:#f8fafc;border:1px solid #e2e8f0;
                           border-radius:10px;padding:14px 18px">
                  <p style="font-size:12px;color:#94a3b8;margin:0">
                    Email đăng ký: <strong style="color:#64748b">%s</strong>
                    · Nếu không phải bạn, hãy bỏ qua email này.
                  </p>
                </td>
              </tr>
            </table>
            """.formatted(
                username,
                stat("500+", "Khóa học", BLUE),
                stat("12K+", "Học viên", NAVY),
                stat("98%%", "Hài lòng", GREEN),
                btn(URL + "/courses", "🚀 Khám phá khóa học", BLUE),
                email
        );
        return wrap("Chào mừng " + username + " đến với EduFlow!", BLUE, body);
    }

    // ══════════════════════════════════════════════════════════
    //  2. ENROLLMENT
    // ══════════════════════════════════════════════════════════
    public static String enrollment(String username, String courseTitle, String instructor) {
        String body = badge("Đăng ký thành công", GREEN) + """
            <h1 style="font-size:26px;font-weight:800;color:#0f172a;
                       margin:0 0 12px;letter-spacing:-.6px;line-height:1.2">
              Bạn đã đăng ký khóa học! 📚
            </h1>
            <p style="font-size:15px;color:#475569;line-height:1.8;margin:0 0 28px">
              Xin chào <strong style="color:#0f172a">%s</strong>,
              chúc mừng bạn đã đăng ký thành công.
            </p>

            <!-- Course card -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="margin-bottom:24px;border-radius:12px;overflow:hidden;
                          border:1px solid #dbeafe">
              <tr>
                <td style="width:5px;background:linear-gradient(180deg,%s,%s);
                           font-size:0">&nbsp;</td>
                <td style="background:#eff6ff;padding:20px 22px">
                  <div style="font-size:10px;font-weight:700;color:#93c5fd;
                              text-transform:uppercase;letter-spacing:.12em;
                              margin-bottom:8px">Khóa học</div>
                  <div style="font-size:19px;font-weight:800;color:#1e3a5f;
                              letter-spacing:-.4px;margin-bottom:8px">%s</div>
                  <div style="font-size:13px;color:#64748b">
                    👨‍🏫 Giảng viên:
                    <strong style="color:#374151">%s</strong>
                  </div>
                </td>
              </tr>
            </table>

            <!-- Tip -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="margin-bottom:4px;border-radius:10px;overflow:hidden;
                          border:1px solid #fde68a">
              <tr>
                <td style="background:#fefce8;padding:14px 18px">
                  <p style="font-size:13px;color:#713f12;margin:0;line-height:1.6">
                    💡 <strong>Mẹo:</strong> Hoàn thành 100%% bài học và pass tất cả
                    quiz để nhận <strong>chứng chỉ hoàn thành</strong> khóa học.
                  </p>
                </td>
              </tr>
            </table>

            %s
            """.formatted(
                username,
                BLUE, GREEN,
                courseTitle, instructor,
                btn(URL + "/my-courses", "▶ Bắt đầu học ngay", GREEN)
        );
        return wrap("Đăng ký thành công: " + courseTitle, GREEN, body);
    }

    // ══════════════════════════════════════════════════════════
    //  3. CERTIFICATE
    // ══════════════════════════════════════════════════════════
    public static String certificate(String username, String courseTitle,
                                     String certCode, int totalLessons,
                                     double avgScore, String issuedDate) {
        String verifyUrl = URL + "/verify/" + certCode;
        String body = badge("Chứng chỉ mới", GOLD) + """
            <h1 style="font-size:26px;font-weight:800;color:#0f172a;
                       margin:0 0 12px;letter-spacing:-.6px;line-height:1.2">
              Chúc mừng, %s! 🏆
            </h1>
            <p style="font-size:15px;color:#475569;line-height:1.8;margin:0 0 28px">
              Bạn đã hoàn thành xuất sắc và nhận được chứng chỉ chính thức
              từ EduFlow Academy.
            </p>

            <!-- Certificate card -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="border-radius:14px;overflow:hidden;
                          border:2px solid #1e3a5f;margin-bottom:28px">
              <!-- Cert header gradient -->
              <tr>
                <td style="background:linear-gradient(135deg,#0f172a 0%%,#1e3a5f 50%%,#1d4ed8 100%%);
                           padding:22px 26px">
                  <table cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td style="font-size:30px;padding-right:16px;
                                 vertical-align:middle;line-height:1">🎓</td>
                      <td style="vertical-align:middle">
                        <div style="font-size:10px;font-weight:700;
                                    color:rgba(255,255,255,.5);
                                    text-transform:uppercase;letter-spacing:.2em;
                                    margin-bottom:4px">Certificate of Completion</div>
                        <div style="font-size:18px;font-weight:800;color:#fff;
                                    letter-spacing:-.3px">EduFlow Academy</div>
                      </td>
                      <td align="right" style="vertical-align:middle">
                        <div style="width:44px;height:44px;border-radius:50%%;
                                    border:2px solid rgba(212,175,55,.5);
                                    background:rgba(212,175,55,.08);
                                    text-align:center;line-height:40px;
                                    font-size:18px">★</div>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
              <!-- Cert body -->
              <tr>
                <td style="background:#fdfcf9;padding:24px 26px">
                  <div style="font-size:10px;font-weight:700;color:#9ca3af;
                              text-transform:uppercase;letter-spacing:.2em;
                              margin-bottom:6px">Chứng nhận trao cho</div>
                  <div style="font-size:26px;font-weight:800;color:#1e3a5f;
                              letter-spacing:-.6px;line-height:1.1;
                              margin-bottom:6px">%s</div>
                  <div style="font-size:13px;color:#6b7280;margin-bottom:20px">
                    đã hoàn thành xuất sắc · <strong style="color:%s">%s</strong>
                  </div>

                  <!-- Stats -->
                  <table width="100%%" cellpadding="0" cellspacing="6" border="0"
                         style="margin-bottom:18px">
                    <tr>
                      %s<td width="6"></td>%s<td width="6"></td>%s
                    </tr>
                  </table>

                  <!-- Code bar -->
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                         style="background:#f8fafc;border:1px solid #e5e7eb;
                                border-radius:8px;overflow:hidden">
                    <tr>
                      <td style="padding:10px 14px">
                        <div style="font-size:10px;color:#9ca3af;
                                    text-transform:uppercase;letter-spacing:.1em;
                                    margin-bottom:3px">Mã xác thực</div>
                        <div style="font-family:'Courier New',monospace;
                                    font-size:12px;font-weight:700;
                                    color:#374151;letter-spacing:.06em">%s</div>
                      </td>
                      <td align="right" style="padding:10px 14px">
                        <a href="%s"
                           style="font-size:12px;font-weight:700;color:%s;
                                  text-decoration:none;
                                  border:1.5px solid %s;border-radius:7px;
                                  padding:6px 14px;white-space:nowrap">
                          🔍 Xác thực
                        </a>
                      </td>
                    </tr>
                  </table>

                </td>
              </tr>
            </table>

            %s

            <p style="font-size:12px;color:#94a3b8;text-align:center;
                      margin:20px 0 0;line-height:1.6">
              Chia sẻ thành tích này với bạn bè và nhà tuyển dụng! 🚀<br>
              Link xác thực: <a href="%s" style="color:%s">%s</a>
            </p>
            """.formatted(
                username,
                username,
                BLUE, courseTitle,
                stat(String.valueOf(totalLessons), "Bài học", NAVY),
                stat(String.format("%.0f%%", avgScore), "Điểm TB", GREEN),
                stat(issuedDate, "Ngày cấp", GOLD),
                certCode,
                verifyUrl, NAVY, NAVY,
                btn(verifyUrl, "🏆 Xem chứng chỉ của tôi", NAVY),
                verifyUrl, BLUE, verifyUrl
        );
        return wrap("Bạn vừa nhận được chứng chỉ: " + courseTitle, GOLD, body);
    }

    // ══════════════════════════════════════════════════════════
    //  4. REMINDER
    // ══════════════════════════════════════════════════════════
    public static String reminder(String username, String courseTitle, int progressPct) {
        String barColor = progressPct >= 70 ? GREEN : (progressPct >= 40 ? "#f59e0b" : "#ef4444");
        String msg = progressPct >= 70
                ? "Bạn đang tiến rất tốt — chỉ còn một chút nữa là xong!"
                : progressPct >= 40
                ? "Đã qua nửa chặng đường rồi, đừng bỏ cuộc nhé!"
                : "Hành trình ngàn dặm bắt đầu từ một bước chân nhỏ 💪";

        String body = badge("Nhắc nhở học tập", BLUE) + """
            <h1 style="font-size:26px;font-weight:800;color:#0f172a;
                       margin:0 0 12px;letter-spacing:-.6px;line-height:1.2">
              Hôm nay chưa học, %s? 😊
            </h1>
            <p style="font-size:15px;color:#475569;line-height:1.8;margin:0 0 28px">
              %s Chỉ cần <strong style="color:#0f172a">15 phút</strong>
              mỗi ngày là đủ để tạo thói quen học tập bền vững.
            </p>

            <!-- Progress card -->
            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="border:1px solid #e2e8f0;border-radius:12px;
                          margin-bottom:28px;overflow:hidden">
              <tr>
                <td style="background:#f8fafc;padding:20px 22px">
                  <div style="font-size:10px;font-weight:700;color:#94a3b8;
                              text-transform:uppercase;letter-spacing:.1em;
                              margin-bottom:8px">Đang học</div>
                  <div style="font-size:17px;font-weight:800;color:#0f172a;
                              letter-spacing:-.3px;margin-bottom:16px">📚 %s</div>
                  <!-- bar bg -->
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                    <tr>
                      <td style="background:#e2e8f0;border-radius:100px;
                                 height:10px;overflow:hidden">
                        <!-- filled bar (inline width) -->
                        <div style="width:%d%%;height:10px;background:%s;
                                    border-radius:100px"></div>
                      </td>
                      <td style="width:50px;text-align:right;
                                 font-size:14px;font-weight:800;
                                 color:%s;padding-left:12px">%d%%</td>
                    </tr>
                  </table>
                  <div style="font-size:11px;color:#94a3b8;margin-top:8px">
                    Tiến độ hoàn thành
                  </div>
                </td>
              </tr>
            </table>

            %s

            <p style="font-size:12px;color:#94a3b8;text-align:center;
                      margin:20px 0 0">
              Mỗi ngày một ít · Kiên trì tạo nên sự khác biệt 🌟
            </p>
            """.formatted(
                username, msg,
                courseTitle,
                progressPct, barColor, barColor, progressPct,
                btn(URL + "/my-courses", "▶ Tiếp tục học ngay", BLUE)
        );
        return wrap("Tiếp tục khóa học của bạn nào!", BLUE, body);
    }

    // ══════════════════════════════════════════════════════════
    //  5. ADMIN — học viên mới
    // ══════════════════════════════════════════════════════════
    public static String adminNewUser(String newUsername, String newEmail, long totalUsers) {
        String body = """
            <div style="font-size:10px;font-weight:700;color:#475569;
                        text-transform:uppercase;letter-spacing:.15em;
                        margin-bottom:16px">Hệ thống · Học viên mới</div>
            <h2 style="font-size:20px;font-weight:800;color:#f8fafc;
                       margin:0 0 24px;letter-spacing:-.4px">
              👤 Học viên vừa đăng ký
            </h2>
            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="background:#020617;border:1px solid #1e293b;
                          border-radius:10px;margin-bottom:24px">
              <tr><td style="padding:20px 22px">
                <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                  %s%s%s
                </table>
              </td></tr>
            </table>
            <a href="%s/admin"
               style="display:inline-block;padding:11px 22px;
                      background:%s;color:#fff;font-size:13px;font-weight:700;
                      text-decoration:none;border-radius:8px">
              → Xem Dashboard
            </a>
            """.formatted(
                infoRow("Username", newUsername, "#f8fafc", false),
                infoRow("Email", newEmail, BLUE, true),
                infoRow("Tổng học viên", totalUsers + " người", GREEN, true),
                URL, BLUE
        );
        return wrapAdmin("Học viên mới: " + newUsername, BLUE, body);
    }

    // ══════════════════════════════════════════════════════════
    //  6. ADMIN — chứng chỉ mới
    // ══════════════════════════════════════════════════════════
    public static String adminNewCert(String studentName, String courseTitle,
                                      String certCode, long totalCerts) {
        String body = """
            <div style="font-size:10px;font-weight:700;color:#475569;
                        text-transform:uppercase;letter-spacing:.15em;
                        margin-bottom:16px">Hệ thống · Chứng chỉ mới</div>
            <h2 style="font-size:20px;font-weight:800;color:#f8fafc;
                       margin:0 0 24px;letter-spacing:-.4px">
              🏆 Chứng chỉ vừa được cấp
            </h2>
            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="background:#020617;border:1px solid #1e293b;
                          border-radius:10px;margin-bottom:24px">
              <tr><td style="padding:20px 22px">
                <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                  %s%s%s%s
                </table>
              </td></tr>
            </table>
            <a href="%s/admin"
               style="display:inline-block;padding:11px 22px;
                      background:%s;color:#fff;font-size:13px;font-weight:700;
                      text-decoration:none;border-radius:8px">
              → Xem Dashboard
            </a>
            """.formatted(
                infoRow("Học viên", studentName, "#f8fafc", false),
                infoRow("Khóa học", courseTitle, BLUE, true),
                infoRow("Mã chứng chỉ", "<span style=\"font-family:monospace;font-size:12px\">"
                        + certCode + "</span>", "#64748b", true),
                infoRow("Tổng chứng chỉ", totalCerts + " chứng chỉ", GOLD, true),
                URL, GOLD
        );
        return wrapAdmin("Chứng chỉ: " + studentName + " · " + courseTitle, GOLD, body);
    }

    // ══════════════════════════════════════════════════════════
    //  7. ADMIN — hoàn thành khóa học
    // ══════════════════════════════════════════════════════════
    public static String adminCourseCompleted(String studentName, String courseTitle) {
        String body = """
            <div style="font-size:10px;font-weight:700;color:#475569;
                        text-transform:uppercase;letter-spacing:.15em;
                        margin-bottom:16px">Hệ thống · Hoàn thành</div>
            <h2 style="font-size:20px;font-weight:800;color:#f8fafc;
                       margin:0 0 24px;letter-spacing:-.4px">
              ✅ Học viên hoàn thành khóa học
            </h2>
            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="background:#020617;border:1px solid #1e293b;
                          border-radius:10px;margin-bottom:24px">
              <tr><td style="padding:20px 22px">
                <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                  %s%s
                </table>
              </td></tr>
            </table>
            <a href="%s/admin"
               style="display:inline-block;padding:11px 22px;
                      background:%s;color:#fff;font-size:13px;font-weight:700;
                      text-decoration:none;border-radius:8px">
              → Xem Dashboard
            </a>
            """.formatted(
                infoRow("Học viên", studentName, "#f8fafc", false),
                infoRow("Khóa học", courseTitle, GREEN, true),
                URL, GREEN
        );
        return wrapAdmin("Hoàn thành: " + studentName + " · " + courseTitle, GREEN, body);
    }

    // ══════════════════════════════════════════════════════════
    //  8. ADMIN WEEKLY REPORT
    // ══════════════════════════════════════════════════════════
    public static String adminWeeklyReport(long newUsers, long newEnrollments,
                                           long newCerts, long totalUsers,
                                           long totalCerts, String weekLabel) {
        String body = """
            <div style="font-size:10px;font-weight:700;color:#475569;
                        text-transform:uppercase;letter-spacing:.15em;
                        margin-bottom:16px">Báo cáo định kỳ · %s</div>
            <h2 style="font-size:20px;font-weight:800;color:#f8fafc;
                       margin:0 0 8px;letter-spacing:-.4px">
              📋 Tổng kết tuần
            </h2>
            <p style="font-size:13px;color:#64748b;margin:0 0 24px;
                      line-height:1.6">Hoạt động trong 7 ngày vừa qua.</p>

            <div style="font-size:10px;font-weight:700;color:#475569;
                        text-transform:uppercase;letter-spacing:.1em;
                        margin-bottom:10px">Trong tuần</div>
            <table width="100%%" cellpadding="0" cellspacing="6" border="0"
                   style="margin-bottom:20px">
              <tr>
                %s<td width="6"></td>%s<td width="6"></td>%s
              </tr>
            </table>

            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="height:1px;background:#1e293b;margin:4px 0 20px;
                          font-size:0"><tr><td>&nbsp;</td></tr></table>

            <div style="font-size:10px;font-weight:700;color:#475569;
                        text-transform:uppercase;letter-spacing:.1em;
                        margin-bottom:10px">Tổng cộng</div>
            <table width="100%%" cellpadding="0" cellspacing="6" border="0"
                   style="margin-bottom:28px">
              <tr>
                %s<td width="6"></td>%s
              </tr>
            </table>

            <a href="%s/admin"
               style="display:inline-block;padding:11px 22px;
                      background:%s;color:#fff;font-size:13px;font-weight:700;
                      text-decoration:none;border-radius:8px">
              → Xem Dashboard đầy đủ
            </a>
            """.formatted(
                weekLabel,
                statD(String.valueOf(newUsers), "Học viên mới", BLUE),
                statD(String.valueOf(newEnrollments), "Đăng ký mới", GREEN),
                statD(String.valueOf(newCerts), "Chứng chỉ cấp", GOLD),
                statD(String.valueOf(totalUsers), "Tổng học viên", "#94a3b8"),
                statD(String.valueOf(totalCerts), "Tổng chứng chỉ", "#94a3b8"),
                URL, BLUE
        );
        return wrapAdmin("Báo cáo tuần · " + weekLabel, BLUE, body);
    }
}