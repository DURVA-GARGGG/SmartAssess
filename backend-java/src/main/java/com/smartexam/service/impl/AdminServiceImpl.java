package com.smartexam.service.impl;

import com.smartexam.entity.Result;
import com.smartexam.repository.ResultRepository;
import com.smartexam.service.AdminService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final ResultRepository resultRepository;

    public AdminServiceImpl(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Override
    public List<Map<String, Object>> getAllResults() {
        List<Result> results = resultRepository.findAllWithDetails();
        List<Map<String, Object>> list = new ArrayList<>();

        for (Result r : results) {
            Map<String, Object> map = new HashMap<>();
            String studentName = "Unknown";
            String subject = "Unknown";

            if (r.getStudentExam() != null) {
                if (r.getStudentExam().getUser() != null) {
                    studentName = r.getStudentExam().getUser().getName();
                }
                if (r.getStudentExam().getExam() != null) {
                    subject = r.getStudentExam().getExam().getSubject();
                }
            }

            map.put("student_name", studentName);
            map.put("subject", subject);
            map.put("score", r.getScore());
            map.put("result_status", r.getResultStatus());
            list.add(map);
        }

        return list;
    }
}
