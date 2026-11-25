from flask import Flask, jsonify, request
from flask_cors import CORS
from db import get_db_connection
import jwt
import datetime
import subprocess
import os
import tempfile
import time
from functools import wraps

app = Flask(__name__)
CORS(app)

app.config['SECRET_KEY'] = 'super_secret_jwt_key_for_dbms_lab'

# --------------------------
# TOKEN MIDDLEWARE
# --------------------------
def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')

        if not token:
            return jsonify({'message': 'Token is missing'}), 401

        try:
            token = token.split(" ")[1]
            data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=["HS256"])
        except Exception as e:
            return jsonify({'message': 'Token invalid', 'error': str(e)}), 401

        return f(data, *args, **kwargs)
    return decorated


# --------------------------
# HEALTH CHECK
# --------------------------
@app.route('/api/health')
def health():
    return jsonify({"status": "Server running"})


# --------------------------
# AUTH
# --------------------------
@app.route('/api/auth/register', methods=['POST'])
def register():
    data = request.json

    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        role_id = data.get('role_id', 2)
        cursor.execute(
            "INSERT INTO USERS (name, email, password, role_id) VALUES (%s, %s, %s, %s)",
            (data['name'], data['email'], data['password'], role_id)
        )
        conn.commit()
        return jsonify({'message': 'User registered successfully'})
    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)})
    finally:
        cursor.close()
        conn.close()


@app.route('/api/auth/login', methods=['POST'])
def login():
    data = request.json

    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("""
        SELECT u.*, r.role_name 
        FROM USERS u 
        JOIN ROLES r ON u.role_id = r.role_id 
        WHERE email=%s AND password=%s
    """, (data['email'], data['password']))

    user = cursor.fetchone()

    if not user:
        return jsonify({'message': 'Invalid credentials'}), 401

    token = jwt.encode({
        'user_id': user['user_id'],
        'role': user['role_name'],
        'name': user['name'],
        'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=24)
    }, app.config['SECRET_KEY'], algorithm="HS256")

    return jsonify({
        'token': token,
        'role': user['role_name'],
        'name': user['name'],
        'user_id': user['user_id']
    })


# --------------------------
# STUDENT ROUTES
# --------------------------

@app.route('/api/student/exams/available', methods=['GET'])
def get_available_exams():
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT * FROM EXAMS ORDER BY exam_id DESC")
    exams = cursor.fetchall()

    cursor.close()
    conn.close()

    return jsonify(exams)


@app.route('/api/student/exams/<int:exam_id>/start', methods=['POST'])
def start_exam(exam_id):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    token = request.headers.get('Authorization')
    user_id = 2  # Default to demo student
    if token and "Bearer " in token:
        try:
            t = token.split(" ")[1]
            payload = jwt.decode(t, app.config['SECRET_KEY'], algorithms=["HS256"])
            user_id = payload.get('user_id', 2)
        except Exception:
            pass

    try:
        # Check for existing attempts
        cursor.execute("SELECT attempt_id, status FROM STUDENT_EXAM WHERE user_id=%s AND exam_id=%s", (user_id, exam_id))
        existing = cursor.fetchone()
        
        if existing:
            if existing['status'] == 'COMPLETED':
                return jsonify({'error': 'You have already completed this exam.'}), 400
            else:
                return jsonify({'attempt_id': existing['attempt_id']})

        cursor.execute("""
            INSERT INTO STUDENT_EXAM (user_id, exam_id, start_time, status)
            VALUES (%s, %s, NOW(), 'IN_PROGRESS')
        """, (user_id, exam_id))

        attempt_id = cursor.lastrowid
        
        # Init proctoring log
        cursor.execute("""
            INSERT INTO PROCTORING_LOG (attempt_id, login_time, tab_switch_count, suspicious_flag)
            VALUES (%s, NOW(), 0, FALSE)
        """, (attempt_id,))

        conn.commit()
        return jsonify({'attempt_id': attempt_id})
    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)}), 400
    finally:
        cursor.close()
        conn.close()


@app.route('/api/student/exams/<int:exam_id>/questions', methods=['GET'])
def get_questions(exam_id):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT * FROM QUESTIONS WHERE exam_id=%s", (exam_id,))
    questions = cursor.fetchall()

    for q in questions:
        q_type = q.get('question_type', 'MCQ')
        if q_type == 'CODING':
            # Templates
            cursor.execute("SELECT language, starter_code FROM CODING_TEMPLATES WHERE question_id=%s", (q['question_id'],))
            q['templates'] = cursor.fetchall()

            # Sample test cases ONLY (hide hidden ones)
            cursor.execute("SELECT input_data, expected_output FROM CODING_TEST_CASES WHERE question_id=%s AND is_sample=TRUE", (q['question_id'],))
            q['sample_test_cases'] = cursor.fetchall()
            q['options'] = []
        else:
            cursor.execute("SELECT option_id, option_text FROM OPTIONS WHERE question_id=%s", (q['question_id'],))
            q['options'] = cursor.fetchall()
            q['templates'] = []
            q['sample_test_cases'] = []

    cursor.close()
    conn.close()

    return jsonify(questions)


@app.route('/api/student/responses', methods=['POST'])
def save_response():
    data = request.json

    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        cursor.execute("""
            SELECT response_id FROM STUDENT_RESPONSE 
            WHERE attempt_id = %s AND question_id = %s
        """, (data['attempt_id'], data['question_id']))
        row = cursor.fetchone()

        if row:
            cursor.execute("""
                UPDATE STUDENT_RESPONSE 
                SET selected_option_id = %s, response_time = NOW()
                WHERE response_id = %s
            """, (data['selected_option_id'], row[0]))
        else:
            cursor.execute("""
                INSERT INTO STUDENT_RESPONSE (attempt_id, question_id, selected_option_id, response_time)
                VALUES (%s, %s, %s, NOW())
            """, (data['attempt_id'], data['question_id'], data['selected_option_id']))

        conn.commit()
        return jsonify({'message': 'Saved'})
    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)})
    finally:
        cursor.close()
        conn.close()


@app.route('/api/student/exams/<int:attempt_id>/submit', methods=['POST'])
def submit_exam(attempt_id):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    try:
        # Mark attempt completed
        cursor.execute("""
            UPDATE STUDENT_EXAM 
            SET status='COMPLETED', end_time=NOW()
            WHERE attempt_id=%s
        """, (attempt_id,))

        # Get exam details
        cursor.execute("""
            SELECT e.exam_id, e.total_marks, e.negative_marking
            FROM STUDENT_EXAM se
            JOIN EXAMS e ON se.exam_id = e.exam_id
            WHERE se.attempt_id = %s
        """, (attempt_id,))
        exam = cursor.fetchone()
        
        # Calculate MCQ score
        cursor.execute("""
            SELECT 
                COALESCE(SUM(CASE WHEN o.is_correct = TRUE THEN q.marks ELSE 0 END), 0) AS correct_score,
                COALESCE(SUM(CASE WHEN o.is_correct = FALSE THEN %s ELSE 0 END), 0) AS penalty
            FROM STUDENT_RESPONSE sr
            JOIN OPTIONS o ON sr.selected_option_id = o.option_id
            JOIN QUESTIONS q ON sr.question_id = q.question_id
            WHERE sr.attempt_id = %s
        """, (exam['negative_marking'], attempt_id))
        mcq_res = cursor.fetchone()
        mcq_score = float(mcq_res['correct_score']) - float(mcq_res['penalty'])

        # Calculate Coding score (best submission per coding question)
        cursor.execute("""
            SELECT COALESCE(SUM(max_score), 0) as coding_total
            FROM (
                SELECT question_id, MAX(score_obtained) as max_score
                FROM CODING_SUBMISSIONS
                WHERE attempt_id = %s
                GROUP BY question_id
            ) as t
        """, (attempt_id,))
        coding_res = cursor.fetchone()
        coding_score = float(coding_res['coding_total']) if coding_res else 0.0

        total_score = max(0.0, mcq_score + coding_score)
        pass_threshold = float(exam['total_marks']) * 0.40
        status = 'PASS' if total_score >= pass_threshold else 'FAIL'

        # Check existing result row (trigger may have inserted initial MCQ score)
        cursor.execute("SELECT result_id FROM RESULTS WHERE attempt_id=%s", (attempt_id,))
        existing_res = cursor.fetchone()
        if existing_res:
            cursor.execute("""
                UPDATE RESULTS SET score=%s, result_status=%s WHERE result_id=%s
            """, (total_score, status, existing_res['result_id']))
        else:
            cursor.execute("""
                INSERT INTO RESULTS (attempt_id, score, result_status)
                VALUES (%s, %s, %s)
            """, (attempt_id, total_score, status))

        # Close proctoring log
        cursor.execute("UPDATE PROCTORING_LOG SET logout_time=NOW() WHERE attempt_id=%s", (attempt_id,))

        conn.commit()
        return jsonify({'message': 'Submitted', 'score': total_score, 'status': status})
    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)}), 400
    finally:
        cursor.close()
        conn.close()


@app.route('/api/student/results', methods=['GET'])
def get_results():
    token = request.headers.get('Authorization')
    user_id = 2
    if token and "Bearer " in token:
        try:
            t = token.split(" ")[1]
            payload = jwt.decode(t, app.config['SECRET_KEY'], algorithms=["HS256"])
            user_id = payload.get('user_id', 2)
        except Exception:
            pass

    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("""
        SELECT r.score, e.subject, r.result_status
        FROM RESULTS r
        JOIN STUDENT_EXAM se ON r.attempt_id = se.attempt_id
        JOIN EXAMS e ON se.exam_id = e.exam_id
        WHERE se.user_id = %s
        ORDER BY r.result_id DESC
    """, (user_id,))

    results = cursor.fetchall()
    cursor.close()
    conn.close()

    return jsonify(results)


# --------------------------
# PROCTORING ROUTE
# --------------------------
@app.route('/api/proctor/logs', methods=['POST'])
def proctor_log():
    data = request.json
    attempt_id = data.get('attempt_id')
    if not attempt_id:
        return jsonify({'message': 'Ignored'}), 200

    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        cursor.execute("""
            UPDATE PROCTORING_LOG 
            SET tab_switch_count = tab_switch_count + 1,
                suspicious_flag = IF(tab_switch_count + 1 > 5, TRUE, suspicious_flag)
            WHERE attempt_id = %s
        """, (attempt_id,))
        conn.commit()
        return jsonify({'message': 'Recorded'})
    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)}), 400
    finally:
        cursor.close()
        conn.close()


# --------------------------
# CODE EXECUTION ROUTES
# --------------------------

def run_code_sandbox(language, source_code, input_data, timeout_sec=3):
    start_time = time.time()
    with tempfile.TemporaryDirectory() as tmpdir:
        lang = language.lower()
        
        try:
            if lang in ['python', 'py']:
                file_path = os.path.join(tmpdir, "solution.py")
                with open(file_path, "w", encoding="utf-8") as f:
                    f.write(source_code)
                cmd = ["python", file_path]
            elif lang == 'java':
                file_path = os.path.join(tmpdir, "Solution.java")
                with open(file_path, "w", encoding="utf-8") as f:
                    f.write(source_code)
                # Compile
                comp = subprocess.run(["javac", file_path], capture_output=True, text=True, timeout=6)
                if comp.returncode != 0:
                    return {
                        "status": "COMPILATION_ERROR",
                        "stdout": "",
                        "stderr": comp.stderr,
                        "compileOutput": comp.stderr,
                        "executionTimeMs": int((time.time() - start_time) * 1000),
                        "memoryKb": 0
                    }
                cmd = ["java", "-cp", tmpdir, "Solution"]
            elif lang in ['javascript', 'js', 'node']:
                file_path = os.path.join(tmpdir, "solution.js")
                with open(file_path, "w", encoding="utf-8") as f:
                    f.write(source_code)
                cmd = ["node", file_path]
            elif lang in ['c', 'cpp', 'c++']:
                ext = ".cpp" if 'cpp' in lang else ".c"
                compiler = "g++" if 'cpp' in lang else "gcc"
                file_path = os.path.join(tmpdir, "solution" + ext)
                exe_path = os.path.join(tmpdir, "solution.exe")
                with open(file_path, "w", encoding="utf-8") as f:
                    f.write(source_code)
                comp = subprocess.run([compiler, "-O2", file_path, "-o", exe_path], capture_output=True, text=True, timeout=6)
                if comp.returncode != 0:
                    return {
                        "status": "COMPILATION_ERROR",
                        "stdout": "",
                        "stderr": comp.stderr,
                        "compileOutput": comp.stderr,
                        "executionTimeMs": int((time.time() - start_time) * 1000),
                        "memoryKb": 0
                    }
                cmd = [exe_path]
            else:
                return {"status": "ERROR", "stderr": f"Unsupported language: {lang}", "executionTimeMs": 0}

            # Run process
            proc = subprocess.run(
                cmd,
                input=input_data,
                capture_output=True,
                text=True,
                timeout=timeout_sec
            )
            elapsed = int((time.time() - start_time) * 1000)

            if proc.returncode != 0:
                return {
                    "status": "RUNTIME_ERROR",
                    "stdout": proc.stdout,
                    "stderr": proc.stderr,
                    "executionTimeMs": elapsed,
                    "memoryKb": 0
                }
            return {
                "status": "SUCCESS",
                "stdout": proc.stdout,
                "stderr": proc.stderr,
                "executionTimeMs": elapsed,
                "memoryKb": 0
            }

        except subprocess.TimeoutExpired:
            return {
                "status": "TIMEOUT",
                "stdout": "",
                "stderr": f"Time Limit Exceeded ({timeout_sec}s)",
                "executionTimeMs": int(timeout_sec * 1000),
                "memoryKb": 0
            }
        except Exception as e:
            return {
                "status": "ERROR",
                "stdout": "",
                "stderr": str(e),
                "executionTimeMs": int((time.time() - start_time) * 1000),
                "memoryKb": 0
            }


@app.route('/api/student/code/run', methods=['POST'])
def run_code():
    data = request.json
    language = data.get('language', 'python')
    source_code = data.get('source_code', '')
    custom_input = data.get('custom_input', '')

    res = run_code_sandbox(language, source_code, custom_input)
    return jsonify(res)


@app.route('/api/student/code/submit', methods=['POST'])
def submit_code():
    data = request.json
    attempt_id = data.get('attempt_id')
    question_id = data.get('question_id')
    language = data.get('language', 'python')
    source_code = data.get('source_code', '')

    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    try:
        # Get question marks
        cursor.execute("SELECT marks FROM QUESTIONS WHERE question_id=%s", (question_id,))
        q_row = cursor.fetchone()
        max_marks = q_row['marks'] if q_row else 10

        # Fetch all test cases
        cursor.execute("SELECT * FROM CODING_TEST_CASES WHERE question_id=%s", (question_id,))
        test_cases = cursor.fetchall()

        if not test_cases:
            return jsonify({'error': 'No test cases configured for this question.'}), 400

        total_weight = sum(tc.get('weightage', 1) for tc in test_cases)
        passed_weight = 0
        passed_count = 0
        max_time = 0
        overall_status = "ACCEPTED"
        test_results_dto = []
        db_records = []

        for tc in test_cases:
            tc_input = tc['input_data']
            tc_expected = tc['expected_output'].strip().replace("\r\n", "\n")
            weight = tc.get('weightage', 1)

            res = run_code_sandbox(language, source_code, tc_input)
            max_time = max(max_time, res.get('executionTimeMs', 0))
            actual = res.get('stdout', '').strip().replace("\r\n", "\n")

            if res.get('status') == 'COMPILATION_ERROR':
                tc_status = 'ERROR'
                overall_status = 'COMPILATION_ERROR'
            elif res.get('status') == 'TIMEOUT':
                tc_status = 'TIMEOUT'
                if overall_status != 'COMPILATION_ERROR':
                    overall_status = 'TIME_LIMIT_EXCEEDED'
            elif res.get('status') == 'RUNTIME_ERROR':
                tc_status = 'ERROR'
                if overall_status != 'COMPILATION_ERROR':
                    overall_status = 'RUNTIME_ERROR'
            elif actual == tc_expected:
                tc_status = 'PASSED'
                passed_count += 1
                passed_weight += weight
            else:
                tc_status = 'FAILED'
                if overall_status == 'ACCEPTED':
                    overall_status = 'WRONG_ANSWER'

            dto = {
                'testCaseId': tc['test_case_id'],
                'isSample': bool(tc['is_sample']),
                'status': tc_status,
                'executionTimeMs': res.get('executionTimeMs', 0),
                'weightage': weight
            }
            if tc['is_sample']:
                dto['inputData'] = tc['input_data']
                dto['expectedOutput'] = tc['expected_output']
                dto['actualOutput'] = res.get('stdout', '')
                dto['errorMessage'] = res.get('stderr', '')

            test_results_dto.append(dto)
            db_records.append((tc['test_case_id'], tc_status, res.get('stdout', ''), res.get('executionTimeMs', 0), res.get('stderr', '')))

        score_obtained = round((passed_weight / total_weight) * max_marks, 2) if total_weight > 0 else 0.0

        # Save to CODING_SUBMISSIONS
        cursor.execute("""
            INSERT INTO CODING_SUBMISSIONS 
            (attempt_id, question_id, language, source_code, status, passed_test_cases, total_test_cases, score_obtained, execution_time_ms, submitted_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
        """, (attempt_id, question_id, language, source_code, overall_status, passed_count, len(test_cases), score_obtained, max_time))

        submission_id = cursor.lastrowid

        # Save test results
        for r in db_records:
            cursor.execute("""
                INSERT INTO CODING_SUBMISSION_TEST_RESULTS 
                (submission_id, test_case_id, status, actual_output, execution_time_ms, error_message)
                VALUES (%s, %s, %s, %s, %s, %s)
            """, (submission_id, r[0], r[1], r[2], r[3], r[4]))

        conn.commit()

        return jsonify({
            'submissionId': submission_id,
            'status': overall_status,
            'passedTestCases': passed_count,
            'totalTestCases': len(test_cases),
            'scoreObtained': score_obtained,
            'maxMarks': max_marks,
            'executionTimeMs': max_time,
            'testCaseResults': test_results_dto
        })

    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)}), 400
    finally:
        cursor.close()
        conn.close()


# --------------------------
# ADMIN ROUTES
# --------------------------

@app.route('/api/admin/results', methods=['GET'])
def get_all_results():
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("""
        SELECT u.name as student_name, e.subject, r.score, r.result_status
        FROM RESULTS r
        JOIN STUDENT_EXAM se ON r.attempt_id = se.attempt_id
        JOIN USERS u ON se.user_id = u.user_id
        JOIN EXAMS e ON se.exam_id = e.exam_id
        ORDER BY r.result_id DESC
    """)

    results = cursor.fetchall()
    cursor.close()
    conn.close()

    return jsonify(results)


@app.route('/api/admin/exams', methods=['GET', 'POST'])
def admin_exams():
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    if request.method == 'GET':
        cursor.execute("SELECT * FROM EXAMS ORDER BY exam_id DESC")
        exams = cursor.fetchall()
        cursor.close()
        conn.close()
        return jsonify(exams)

    if request.method == 'POST':
        data = request.json
        try:
            cursor.execute("""
                INSERT INTO EXAMS (subject, exam_date, duration_minutes, total_marks, negative_marking)
                VALUES (%s, %s, %s, %s, %s)
            """, (data['subject'], data['exam_date'], data['duration_minutes'], data['total_marks'], data['negative_marking']))
            conn.commit()
            return jsonify({'message': 'Exam created successfully'})
        except Exception as e:
            conn.rollback()
            return jsonify({'error': str(e)}), 400
        finally:
            cursor.close()
            conn.close()


@app.route('/api/admin/exams/<int:exam_id>/questions', methods=['POST'])
def admin_add_question(exam_id):
    data = request.json
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        q_type = data.get('question_type', 'MCQ')
        diff = data.get('difficulty', 'MEDIUM')
        topic = data.get('topic', 'General')
        marks = data.get('marks', 5)
        text = data.get('text', '')

        if q_type == 'CODING':
            cursor.execute("""
                INSERT INTO QUESTIONS (exam_id, question_text, question_type, difficulty, topic, marks, problem_statement, `constraints`, input_format, output_format)
                VALUES (%s, %s, 'CODING', %s, %s, %s, %s, %s, %s, %s)
            """, (exam_id, text, diff, topic, marks, data.get('problem_statement', ''), data.get('constraints', ''), data.get('input_format', ''), data.get('output_format', '')))
            question_id = cursor.lastrowid

            # Templates
            for tmpl in data.get('templates', []):
                cursor.execute("""
                    INSERT INTO CODING_TEMPLATES (question_id, language, starter_code)
                    VALUES (%s, %s, %s)
                """, (question_id, tmpl['language'], tmpl['starter_code']))

            # Test cases
            for tc in data.get('test_cases', []):
                cursor.execute("""
                    INSERT INTO CODING_TEST_CASES (question_id, input_data, expected_output, is_sample, weightage)
                    VALUES (%s, %s, %s, %s, %s)
                """, (question_id, tc['input_data'], tc['expected_output'], tc.get('is_sample', False), tc.get('weightage', 10)))

        else:
            cursor.execute("""
                INSERT INTO QUESTIONS (exam_id, question_text, question_type, difficulty, topic, marks)
                VALUES (%s, %s, 'MCQ', %s, %s, %s)
            """, (exam_id, text, diff, topic, marks))
            question_id = cursor.lastrowid

            for opt in data.get('options', []):
                cursor.execute("""
                    INSERT INTO OPTIONS (question_id, option_text, is_correct)
                    VALUES (%s, %s, %s)
                """, (question_id, opt['text'], opt['is_correct']))

        conn.commit()
        return jsonify({'message': f'{q_type} question added successfully'})
    except Exception as e:
        conn.rollback()
        return jsonify({'error': str(e)}), 400
    finally:
        cursor.close()
        conn.close()


# --------------------------
# RUN
# --------------------------
if __name__ == '__main__':
    app.run(debug=True, port=8080)
