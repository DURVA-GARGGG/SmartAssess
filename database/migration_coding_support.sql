-- =========================================================
-- Smart Examination System: Coding Assessment Migration
-- Preserves existing tables, triggers, and stored procedures
-- Adds support for Monaco Editor coding questions, test cases,
-- language templates, and execution submissions.
-- =========================================================

USE smart_exam;

-- 1. Extend QUESTIONS table with coding metadata if columns do not exist
SET @dbname = DATABASE();
SET @tablename = "questions";

-- Add problem_statement column if not present
SET @col = "problem_statement";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @col
  ) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN problem_statement TEXT;")
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add constraints column if not present
SET @col = "constraints";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @col
  ) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN `constraints` TEXT;")
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add input_format column if not present
SET @col = "input_format";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @col
  ) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN input_format TEXT;")
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add output_format column if not present
SET @col = "output_format";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @col
  ) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN output_format TEXT;")
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. CODING_TEMPLATES (Starter code boilerplates per language)
CREATE TABLE IF NOT EXISTS CODING_TEMPLATES (
    template_id INT PRIMARY KEY AUTO_INCREMENT,
    question_id INT NOT NULL,
    language VARCHAR(30) NOT NULL, -- 'java', 'python', 'c', 'cpp', 'javascript'
    starter_code MEDIUMTEXT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
    UNIQUE KEY uq_question_lang (question_id, language)
);

-- 3. CODING_TEST_CASES (Sample and hidden test cases for automated evaluation)
CREATE TABLE IF NOT EXISTS CODING_TEST_CASES (
    test_case_id INT PRIMARY KEY AUTO_INCREMENT,
    question_id INT NOT NULL,
    input_data MEDIUMTEXT NOT NULL,
    expected_output MEDIUMTEXT NOT NULL,
    is_sample BOOLEAN DEFAULT FALSE,
    weightage INT DEFAULT 1,
    time_limit_ms INT DEFAULT 2000,
    memory_limit_kb INT DEFAULT 262144,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- 4. CODING_SUBMISSIONS (Student code submissions & evaluation status)
CREATE TABLE IF NOT EXISTS CODING_SUBMISSIONS (
    submission_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    language VARCHAR(30) NOT NULL,
    source_code MEDIUMTEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    passed_test_cases INT DEFAULT 0,
    total_test_cases INT DEFAULT 0,
    score_obtained DECIMAL(10,2) DEFAULT 0.00,
    execution_time_ms INT DEFAULT 0,
    memory_kb INT DEFAULT 0,
    compiler_output MEDIUMTEXT,
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES student_exam(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- 5. CODING_SUBMISSION_TEST_RESULTS (Per-testcase execution details)
CREATE TABLE IF NOT EXISTS CODING_SUBMISSION_TEST_RESULTS (
    detail_id INT PRIMARY KEY AUTO_INCREMENT,
    submission_id INT NOT NULL,
    test_case_id INT NOT NULL,
    status VARCHAR(30) NOT NULL, -- 'PASSED', 'FAILED', 'TIMEOUT', 'ERROR'
    actual_output MEDIUMTEXT,
    execution_time_ms INT DEFAULT 0,
    memory_kb INT DEFAULT 0,
    error_message TEXT,
    FOREIGN KEY (submission_id) REFERENCES CODING_SUBMISSIONS(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (test_case_id) REFERENCES CODING_TEST_CASES(test_case_id) ON DELETE CASCADE
);

-- 6. Add Demo Coding Exam if not present
INSERT IGNORE INTO exams (exam_id, subject, exam_date, duration_minutes, total_marks, negative_marking)
VALUES (10, 'Coding Assessment & Data Structures', DATE_ADD(NOW(), INTERVAL 7 DAY), 60, 100, 0.00);

-- 7. Add Sample Coding Questions to Exam 10
-- Question 1: Array Reversal
INSERT INTO questions (question_id, exam_id, question_text, question_type, difficulty, topic, marks, problem_statement, `constraints`, input_format, output_format)
VALUES (
    101,
    10,
    'Reverse an Array of Integers',
    'CODING',
    'EASY',
    'Arrays',
    20,
    'Given an array of integers `A` of size `N`, print the reversed array where elements are separated by spaces.',
    '1 <= N <= 1000\n-10^5 <= A[i] <= 10^5',
    'The first line contains an integer N representing the size of the array.\nThe second line contains N space-separated integers.',
    'Print the N elements in reversed order, separated by a single space.'
) ON DUPLICATE KEY UPDATE 
    problem_statement = VALUES(problem_statement),
    `constraints` = VALUES(`constraints`),
    input_format = VALUES(input_format),
    output_format = VALUES(output_format);

-- Starter Templates for Question 101
INSERT INTO CODING_TEMPLATES (question_id, language, starter_code) VALUES
(101, 'java', 'import java.util.Scanner;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if (!sc.hasNextInt()) return;\n        int n = sc.nextInt();\n        int[] arr = new int[n];\n        for (int i = 0; i < n; i++) {\n            arr[i] = sc.nextInt();\n        }\n        \n        // Write your solution here\n        for (int i = n - 1; i >= 0; i--) {\n            System.out.print(arr[i] + (i > 0 ? " " : ""));\n        }\n        System.out.println();\n    }\n}'),
(101, 'python', 'import sys\n\ndef main():\n    lines = sys.stdin.read().split()\n    if not lines:\n        return\n    n = int(lines[0])\n    arr = lines[1:n+1]\n    # Print reversed array separated by space\n    print(" ".join(reversed(arr)))\n\nif __name__ == "__main__":\n    main()'),
(101, 'cpp', '#include <iostream>\n#include <vector>\nusing namespace std;\n\nint main() {\n    int n;\n    if (!(cin >> n)) return 0;\n    vector<int> arr(n);\n    for (int i = 0; i < n; i++) cin >> arr[i];\n    \n    // Write your solution here\n    for (int i = n - 1; i >= 0; i--) {\n        cout << arr[i] << (i > 0 ? " " : "");\n    }\n    cout << endl;\n    return 0;\n}'),
(101, 'c', '#include <stdio.h>\n\nint main() {\n    int n;\n    if (scanf("%d", &n) != 1) return 0;\n    int arr[n];\n    for (int i = 0; i < n; i++) {\n        scanf("%d", &arr[i]);\n    }\n    for (int i = n - 1; i >= 0; i--) {\n        printf("%d%s", arr[i], (i > 0 ? " " : ""));\n    }\n    printf("\\n");\n    return 0;\n}'),
(101, 'javascript', 'const fs = require("fs");\n\nfunction main() {\n    const input = fs.readFileSync(0, "utf-8").trim().split(/\\s+/);\n    if (input.length === 0 || input[0] === "") return;\n    const n = parseInt(input[0], 10);\n    const arr = input.slice(1, n + 1);\n    console.log(arr.reverse().join(" "));\n}\n\nmain();')
ON DUPLICATE KEY UPDATE starter_code = VALUES(starter_code);

-- Test cases for Question 101
INSERT INTO CODING_TEST_CASES (question_id, input_data, expected_output, is_sample, weightage) VALUES
(101, '4\n1 2 3 4', '4 3 2 1', TRUE, 10),
(101, '5\n10 20 30 40 50', '50 40 30 20 10', TRUE, 10),
(101, '1\n99', '99', FALSE, 10),
(101, '6\n-5 -4 -3 -2 -1 0', '0 -1 -2 -3 -4 -5', FALSE, 10);

-- Question 2: Two Sum Problem
INSERT INTO questions (question_id, exam_id, question_text, question_type, difficulty, topic, marks, problem_statement, `constraints`, input_format, output_format)
VALUES (
    102,
    10,
    'Two Sum Target Indices',
    'CODING',
    'MEDIUM',
    'Hash Tables',
    30,
    'Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target` (0-indexed). Assume exactly one solution exists.',
    '2 <= N <= 10^4\n-10^9 <= nums[i] <= 10^9\nExactly one valid answer exists.',
    'First line contains integer N (number of elements).\nSecond line contains N space-separated integers.\nThird line contains integer target.',
    'Print the two indices separated by a space in ascending order.'
) ON DUPLICATE KEY UPDATE 
    problem_statement = VALUES(problem_statement),
    `constraints` = VALUES(`constraints`),
    input_format = VALUES(input_format),
    output_format = VALUES(output_format);

-- Starter Templates for Question 102
INSERT INTO CODING_TEMPLATES (question_id, language, starter_code) VALUES
(102, 'java', 'import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int n = sc.nextInt();\n        int[] nums = new int[n];\n        for (int i = 0; i < n; i++) nums[i] = sc.nextInt();\n        int target = sc.nextInt();\n        \n        Map<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < n; i++) {\n            int complement = target - nums[i];\n            if (map.containsKey(complement)) {\n                System.out.println(map.get(complement) + " " + i);\n                return;\n            }\n            map.put(nums[i], i);\n        }\n    }\n}'),
(102, 'python', 'import sys\n\ndef main():\n    lines = sys.stdin.read().split()\n    if not lines:\n        return\n    n = int(lines[0])\n    nums = [int(x) for x in lines[1:n+1]]\n    target = int(lines[n+1])\n    \n    seen = {}\n    for i, num in enumerate(nums):\n        comp = target - num\n        if comp in seen:\n            print(f"{seen[comp]} {i}")\n            return\n        seen[num] = i\n\nif __name__ == "__main__":\n    main()'),
(102, 'cpp', '#include <iostream>\n#include <vector>\n#include <unordered_map>\nusing namespace std;\n\nint main() {\n    int n;\n    if (!(cin >> n)) return 0;\n    vector<int> nums(n);\n    for (int i = 0; i < n; i++) cin >> nums[i];\n    int target;\n    cin >> target;\n    \n    unordered_map<int, int> mp;\n    for (int i = 0; i < n; i++) {\n        int comp = target - nums[i];\n        if (mp.count(comp)) {\n            cout << mp[comp] << " " << i << endl;\n            return 0;\n        }\n        mp[nums[i]] = i;\n    }\n    return 0;\n}'),
(102, 'c', '#include <stdio.h>\n\nint main() {\n    int n;\n    if (scanf("%d", &n) != 1) return 0;\n    int nums[n];\n    for (int i = 0; i < n; i++) scanf("%d", &nums[i]);\n    int target;\n    scanf("%d", &target);\n    \n    for (int i = 0; i < n; i++) {\n        for (int j = i + 1; j < n; j++) {\n            if (nums[i] + nums[j] == target) {\n                printf("%d %d\\n", i, j);\n                return 0;\n            }\n        }\n    }\n    return 0;\n}'),
(102, 'javascript', 'const fs = require("fs");\n\nfunction main() {\n    const input = fs.readFileSync(0, "utf-8").trim().split(/\\s+/);\n    if (input.length < 3) return;\n    const n = parseInt(input[0], 10);\n    const nums = input.slice(1, n + 1).map(Number);\n    const target = parseInt(input[n + 1], 10);\n    \n    const map = new Map();\n    for (let i = 0; i < n; i++) {\n        const comp = target - nums[i];\n        if (map.has(comp)) {\n            console.log(`${map.get(comp)} ${i}`);\n            return;\n        }\n        map.set(nums[i], i);\n    }\n}\n\nmain();')
ON DUPLICATE KEY UPDATE starter_code = VALUES(starter_code);

-- Test cases for Question 102
INSERT INTO CODING_TEST_CASES (question_id, input_data, expected_output, is_sample, weightage) VALUES
(102, '4\n2 7 11 15\n9', '0 1', TRUE, 15),
(102, '3\n3 2 4\n6', '1 2', TRUE, 15),
(102, '2\n3 3\n6', '0 1', FALSE, 15),
(102, '5\n10 20 30 40 50\n70', '2 3', FALSE, 15);
