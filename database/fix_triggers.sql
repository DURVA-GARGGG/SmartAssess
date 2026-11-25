-- =========================================================
-- Fix Mutating Table Triggers in MySQL (Error 1442 Fix)
-- =========================================================

USE smart_exam;

-- 1. Drop the problematic AFTER INSERT/UPDATE triggers that attempt
-- to update the same table that invoked them (MySQL Error 1442)
DROP TRIGGER IF EXISTS SetPassFail;
DROP TRIGGER IF EXISTS AfterResultInsert;
DROP TRIGGER IF EXISTS CheckSuspiciousActivity;
DROP TRIGGER IF EXISTS AfterProctoringUpdate;
DROP TRIGGER IF EXISTS AfterProctoringInsert;
-- Also drop the BEFORE versions so this script can be re-run safely
DROP TRIGGER IF EXISTS BeforeResultInsert;
DROP TRIGGER IF EXISTS BeforeProctoringUpdate;
DROP TRIGGER IF EXISTS BeforeProctoringInsert;

-- 2. Create proper BEFORE INSERT trigger on RESULTS
-- Valid MySQL: assigns NEW.result_status directly without executing an UPDATE
DELIMITER //
CREATE TRIGGER BeforeResultInsert
BEFORE INSERT ON RESULTS
FOR EACH ROW
BEGIN
    DECLARE v_total_marks INT DEFAULT 0;

    SELECT e.total_marks
    INTO   v_total_marks
    FROM   EXAMS        e
    JOIN   STUDENT_EXAM se ON e.exam_id = se.exam_id
    WHERE  se.attempt_id = NEW.attempt_id;

    IF NEW.score IS NOT NULL AND v_total_marks > 0 THEN
        SET NEW.result_status = IF(NEW.score >= v_total_marks * 0.40, 'PASS', 'FAIL');
    END IF;
END //
DELIMITER ;

-- 3. Create proper BEFORE UPDATE and BEFORE INSERT triggers on PROCTORING_LOG
DELIMITER //
CREATE TRIGGER BeforeProctoringUpdate
BEFORE UPDATE ON PROCTORING_LOG
FOR EACH ROW
BEGIN
    IF NEW.tab_switch_count > 5 THEN
        SET NEW.suspicious_flag = TRUE;
    END IF;
END //

CREATE TRIGGER BeforeProctoringInsert
BEFORE INSERT ON PROCTORING_LOG
FOR EACH ROW
BEGIN
    IF NEW.tab_switch_count > 5 THEN
        SET NEW.suspicious_flag = TRUE;
    END IF;
END //
DELIMITER ;

-- 4. Update Stored Procedure: CalculateTotalScore
-- Now calculates total = (MCQ correct - MCQ negative penalty) + Coding submissions score
DELIMITER //
DROP PROCEDURE IF EXISTS CalculateTotalScore //
CREATE PROCEDURE CalculateTotalScore(
    IN  p_attempt_id   INT,
    OUT p_total_score  DECIMAL(10,2)
)
BEGIN
    DECLARE v_correct_score    DECIMAL(10,2) DEFAULT 0;
    DECLARE v_negative_score   DECIMAL(10,2) DEFAULT 0;
    DECLARE v_negative_marking DECIMAL(5,2) DEFAULT 0;
    DECLARE v_coding_score     DECIMAL(10,2) DEFAULT 0;
    DECLARE v_exam_id          INT;

    -- Get exam id and negative marking factor
    SELECT exam_id
    INTO   v_exam_id
    FROM   STUDENT_EXAM
    WHERE  attempt_id = p_attempt_id;

    SELECT negative_marking
    INTO   v_negative_marking
    FROM   EXAMS
    WHERE  exam_id = v_exam_id;

    -- Sum marks for correctly answered MCQ questions
    SELECT COALESCE(SUM(q.marks), 0)
    INTO   v_correct_score
    FROM   STUDENT_RESPONSE sr
    JOIN   OPTIONS    o ON sr.selected_option_id = o.option_id
    JOIN   QUESTIONS  q ON sr.question_id        = q.question_id
    WHERE  sr.attempt_id = p_attempt_id
      AND  o.is_correct  = TRUE;

    -- Sum penalties for incorrectly answered MCQ questions
    SELECT COALESCE(SUM(v_negative_marking), 0)
    INTO   v_negative_score
    FROM   STUDENT_RESPONSE sr
    JOIN   OPTIONS    o ON sr.selected_option_id = o.option_id
    JOIN   QUESTIONS  q ON sr.question_id        = q.question_id
    WHERE  sr.attempt_id = p_attempt_id
      AND  o.is_correct  = FALSE;

    -- Sum highest score achieved per coding question for this attempt
    SELECT COALESCE(SUM(best_score), 0)
    INTO   v_coding_score
    FROM (
        SELECT question_id, MAX(score_obtained) AS best_score
        FROM   CODING_SUBMISSIONS
        WHERE  attempt_id = p_attempt_id
        GROUP BY question_id
    ) AS cs;

    SET p_total_score = v_correct_score - v_negative_score + v_coding_score;
    IF p_total_score < 0 THEN
        SET p_total_score = 0;
    END IF;
END //
DELIMITER ;
