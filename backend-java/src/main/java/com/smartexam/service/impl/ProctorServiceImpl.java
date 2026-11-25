package com.smartexam.service.impl;

import com.smartexam.dto.ProctorLogRequest;
import com.smartexam.entity.ProctoringLog;
import com.smartexam.entity.StudentExam;
import com.smartexam.repository.ProctoringLogRepository;
import com.smartexam.repository.StudentExamRepository;
import com.smartexam.service.ProctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProctorServiceImpl implements ProctorService {

    private final ProctoringLogRepository logRepository;
    private final StudentExamRepository studentExamRepository;

    public ProctorServiceImpl(ProctoringLogRepository logRepository, StudentExamRepository studentExamRepository) {
        this.logRepository = logRepository;
        this.studentExamRepository = studentExamRepository;
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
