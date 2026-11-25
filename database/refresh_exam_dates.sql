-- =========================================================
-- Run before a demo if "Registration closed: exam date has
-- already passed" appears when starting an exam.
-- The BeforeExamAttempt trigger rejects new attempts once an
-- exam's exam_date is in the past; this moves every exam
-- 30 days ahead. Existing attempts and results are untouched.
-- =========================================================

USE smart_exam;

SET SQL_SAFE_UPDATES = 0;
UPDATE EXAMS SET exam_date = DATE_ADD(NOW(), INTERVAL 30 DAY);
SET SQL_SAFE_UPDATES = 1;

SELECT exam_id, subject, exam_date FROM EXAMS;
