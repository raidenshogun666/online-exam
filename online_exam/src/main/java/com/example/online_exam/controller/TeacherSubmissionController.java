package com.example.online_exam.controller;

import com.example.online_exam.controller.dto.ResponseDTO;
import com.example.online_exam.controller.dto.SubmissionView;
import com.example.online_exam.controller.dto.TeacherSubmissionDetailView;
import com.example.online_exam.entity.User;
import com.example.online_exam.service.TeacherExamService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TeacherSubmissionController {

    private final TeacherExamService teacherExamService;

    public TeacherSubmissionController(TeacherExamService teacherExamService) {
        this.teacherExamService = teacherExamService;
    }

    @GetMapping("/exams/{examId}/submissions")
    public ResponseDTO<List<SubmissionView>> submissions(@PathVariable Integer examId, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"teacher".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        return ResponseDTO.success(teacherExamService.listSubmissions(examId));
    }

    @GetMapping("/exams/{examId}/submissions/{studentId}")
    public ResponseDTO<List<TeacherSubmissionDetailView>> submissionDetail(@PathVariable Integer examId,
                                                                           @PathVariable Long studentId,
                                                                           HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return ResponseDTO.unAuth();
        if (!"teacher".equalsIgnoreCase(loginUser.getRole())) return ResponseDTO.forbidden();

        return ResponseDTO.success(teacherExamService.getSubmissionDetail(examId, studentId));
    }
    // 导出：/api/exams/{examId}/export?format=csv|xlsx
    @GetMapping("/exams/{examId}/export")
    public void export(@PathVariable Integer examId,
                       @RequestParam(defaultValue = "csv") String format,
                       HttpSession session,
                       HttpServletResponse response) throws Exception {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) { response.setStatus(401); return; }
        if (!"teacher".equalsIgnoreCase(loginUser.getRole())) { response.setStatus(403); return; }

        List<SubmissionView> rows = teacherExamService.listSubmissions(examId);

        if ("csv".equalsIgnoreCase(format)) {
            exportCsv(examId, rows, response);
            return;
        }
        if ("xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) {
            exportXlsx(examId, rows, response);
            return;
        }

        response.setStatus(400);
    }

    private void exportCsv(Integer examId, List<SubmissionView> rows, HttpServletResponse response) throws Exception {
        String filename = URLEncoder.encode("exam_" + examId + "_scores.csv", StandardCharsets.UTF_8);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);

        PrintWriter w = response.getWriter();
        w.write("\uFEFF"); // BOM，防 Excel 中文乱码
        w.println("username,studentId,totalScore,submitTime");

        for (SubmissionView r : rows) {
            w.printf("%s,%d,%d,%s%n",
                    safeCsv(r.getUsername()),
                    r.getStudentId(),
                    r.getTotalScore(),
                    r.getSubmitTime());
        }
        w.flush();
    }

    private void exportXlsx(Integer examId, List<SubmissionView> rows, HttpServletResponse response) throws Exception {
        String filename = URLEncoder.encode("exam_" + examId + "_scores.xlsx", StandardCharsets.UTF_8);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ServletOutputStream os = response.getOutputStream()) {

            Sheet sheet = wb.createSheet("scores");

            Row head = sheet.createRow(0);
            head.createCell(0).setCellValue("username");
            head.createCell(1).setCellValue("studentId");
            head.createCell(2).setCellValue("totalScore");
            head.createCell(3).setCellValue("submitTime");

            int i = 1;
            for (SubmissionView r : rows) {
                Row row = sheet.createRow(i++);
                row.createCell(0).setCellValue(nvl(r.getUsername()));
                row.createCell(1).setCellValue(r.getStudentId());
                row.createCell(2).setCellValue(r.getTotalScore() == null ? 0 : r.getTotalScore());
                row.createCell(3).setCellValue(r.getSubmitTime() == null ? "" : r.getSubmitTime().toString());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);

            wb.write(os);
            os.flush();
        }
    }

    private String safeCsv(String s) {
        if (s == null) return "";
        return s.replace(",", " ").replace("\n", " ").replace("\r", " ");
    }

    private String nvl(String s) { return s == null ? "" : s; }
}