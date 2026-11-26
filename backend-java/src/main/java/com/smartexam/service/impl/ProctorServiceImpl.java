package com.smartexam.service.impl;

import com.smartexam.dto.ProctorLogRequest;
import com.smartexam.entity.ProctoringLog;
import com.smartexam.entity.Result;
import com.smartexam.entity.StudentExam;
import com.smartexam.repository.ProctoringLogRepository;
import com.smartexam.repository.ResultRepository;
import com.smartexam.repository.StudentExamRepository;
import com.smartexam.service.ProctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProctorServiceImpl implements ProctorService {

    private final ProctoringLogRepository logRepository;
    private final StudentExamRepository studentExamRepository;
    private final ResultRepository resultRepository;

    public ProctorServiceImpl(ProctoringLogRepository logRepository,
                              StudentExamRepository studentExamRepository,
                              ResultRepository resultRepository) {
        this.logRepository = logRepository;
        this.studentExamRepository = studentExamRepository;
        this.resultRepository = resultRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllAttempts() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (StudentExam se : studentExamRepository.findAll()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("attempt_id", se.getAttemptId());
            row.put("student_name", se.getUser() != null ? se.getUser().getName() : "Unknown");
            row.put("student_email", se.getUser() != null ? se.getUser().getEmail() : "");
            row.put("subject", se.getExam() != null ? se.getExam().getSubject() : "Unknown");
            row.put("attempt_status", se.getStatus());
            row.put("start_time", se.getStartTime() != null ? se.getStartTime().toString() : null);

            Optional<ProctoringLog> log = logRepository.findByStudentExamAttemptId(se.getAttemptId());
            int switches = log.map(l -> l.getTabSwitchCount() != null ? l.getTabSwitchCount() : 0).orElse(0);
            boolean flagged = log.map(l -> Boolean.TRUE.equals(l.getSuspiciousFlag())).orElse(false);
            row.put("tab_switch_count", switches);
            row.put("suspicious", flagged);

            Optional<Result> result = resultRepository.findByStudentExamAttemptId(se.getAttemptId());
            row.put("score", result.map(Result::getScore).orElse(null));
            row.put("result_status", result.map(Result::getResultStatus).orElse(null));
            rows.add(row);
        }
        // Flagged attempts first, then the newest attempts
        rows.sort(Comparator.comparing((Map<String, Object> r) -> !(Boolean) r.get("suspicious"))
                .thenComparing(r -> -((Integer) r.get("attempt_id"))));
        return rows;
    }

    @Override
    @Transactional
    public void logAction(ProctorLogRequest request) {
        if (request == null || request.getAttemptId() == null) {
            return;
        }

        ProctoringLog log = logRepository.findByStudentExamAttemptId(request.getAttemptId())
                .orElseGet(() -> {
                    ProctoringLog newLog = new ProctoringLog();
                    StudentExam attempt = studentExamRepository.findById(request.getAttemptId()).orElse(null);
                    newLog.setStudentExam(attempt);
                    newLog.setLoginTime(LocalDateTime.now());
                    newLog.setTabSwitchCount(0);
                    newLog.setSuspiciousFlag(false);
                    return newLog;
                });

        if ("TAB_SWITCH".equalsIgnoreCase(request.getAction())) {
            int currentCount = log.getTabSwitchCount() != null ? log.getTabSwitchCount() : 0;
            currentCount++;
            log.setTabSwitchCount(currentCount);
            if (currentCount > 5) {
                log.setSuspiciousFlag(true);
            }
        }

        logRepository.save(log);
    }
}
