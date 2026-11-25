# Smart Examination & Coding Assessment Platform (Spring Boot & MySQL)

A modern, enterprise-grade **Online Examination and Coding Assessment Platform** built with **Java (Spring Boot 3.x / Java 21)**, **MySQL**, **Monaco Editor (HackerRank-style)**, and a **Sandboxed Code Execution Engine (Judge0 / Safe Local Process Runner)**.

---

## 🚀 Key Highlights & New Functionality

### 1. Complete Java Backend Migration (Spring Boot 3.x)
- **Architecture**: Strict Layered MVC Architecture (`controller/`, `service/`, `repository/`, `entity/`, `dto/`, `config/`, `exception/`).
- **Persistence**: **Spring Data JPA / Hibernate** with automatic connection pooling (**HikariCP**) connected to MySQL.
- **Security & Tokens**: JWT authentication filter with stateless session management and role-based access control (`Admin`, `Student`).
- **Database Logic Preservation**: Preserves all 9 existing relational tables, stored procedures (`CalculateTotalScore`, `AssignRanks`), and triggers (`AfterExamSubmission`) while enhancing evaluation for coding questions.

### 2. HackerRank-style Coding Assessment Platform
- **Question Types**: Seamless support for both **MCQ tests** and **Live Coding Assessments** in the same exam.
- **Monaco Editor**: Integrated Microsoft Monaco Editor with:
  - Multi-language support: **Java (OpenJDK)**, **Python 3**, **C (GCC)**, **C++ (G++)**, and **JavaScript (Node.js)**.
  - Syntax highlighting, dark/light theme switching (`vs-dark` / `vs-light`), auto-formatting, bracket matching, and starter code reset.
- **Sandboxed Execution**:
  - **Run Code**: Execute solution against custom inputs or sample test cases in real-time.
  - **Submit Code**: Automatically evaluates code against all **sample and hidden test cases**, comparing normalized outputs, calculating execution time (ms), and tracking memory usage.
  - **Judge0 & Safe Sandbox Engine**: Seamlessly forwards jobs to Judge0 API (or self-hosted Judge0 instance) with an automatic failover to the internal isolated sandbox runner with strict execution timeouts (2-3s) to prevent infinite loops and fork bombs.
- **Comprehensive Score Calculation**:
  - Automatically combines MCQ scores (with negative marking deductions) and partial/full coding question scores.
- **Live Proctoring**:
  - Tracks browser tab switches (`visibilitychange` API), triggers alerts, and logs suspicious attempts to the `PROCTORING_LOG` table.

---

## 📁 Project Structure

```
Smart-Examination-System-main/
├── backend-java/                              # Core Java Spring Boot Backend
│   ├── pom.xml                               # Maven Project Descriptor (Java 21, Spring Boot 3.2.5)
│   ├── mvnw.cmd / mvnw                       # Maven Wrapper executable scripts
│   └── src/
│       ├── main/
│       │   ├── java/com/smartexam/
│       │   │   ├── SmartExamApplication.java # Spring Boot Entry Point
│       │   │   ├── config/                   # SecurityConfig, JwtAuthenticationFilter, JwtUtil
│       │   │   ├── controller/               # REST Controllers (Auth, Student, Admin, CodeExecution, Proctor)
│       │   │   ├── dto/                      # Data Transfer Objects
│       │   │   ├── entity/                   # JPA Entities (Role, User, Exam, Question, CodingSubmission, etc.)
│       │   │   ├── exception/                # GlobalExceptionHandler, ApiException
│       │   │   ├── repository/               # Spring Data JPA Repositories
│       │   │   └── service/                  # Services & Implementations (CodeExecutionService, etc.)
│       │   └── resources/
│       │       └── application.properties    # MySQL, JWT, and Judge0 configuration
├── database/
│   ├── schema.sql                            # Original DBMS Lab Schema
│   ├── schema_2.sql                          # Extended DBMS schema with triggers & stored procedures
│   └── migration_coding_support.sql          # Coding Assessment Migration (Tables: templates, testcases, submissions)
├── frontend/                                 # Glassmorphism Web Interface
│   ├── index.html                            # Authentication (Login / Register)
│   ├── student.html                          # Student Portal (Exams list, Start attempt)
│   ├── exam.html                             # Active Exam (Monaco Editor + MCQ + Proctoring)
│   ├── result.html                           # Results & Performance report
│   ├── admin.html                            # Admin Portal (Exam creator, MCQ & Coding question builder)
│   ├── css/style.css                         # Glassmorphism styling & Monaco IDE layout
│   └── js/api.js                             # Unified REST API Client
└── backend/                                  # Preserved & upgraded Python Flask API (port 8080/5000)
```

---

## 🗄️ Database Architecture

The system retains all 9 existing tables and adds 4 new tables:

```
[EXAMS] ──< [QUESTIONS] ──┬──< [OPTIONS] (for MCQs)
                          ├──< [CODING_TEMPLATES] (starter code per language)
                          └──< [CODING_TEST_CASES] (sample & hidden test cases)

[USERS] ──< [STUDENT_EXAM] ──┬──< [STUDENT_RESPONSE] (MCQ selections)
                             ├──< [CODING_SUBMISSIONS] ──< [CODING_SUBMISSION_TEST_RESULTS]
                             ├──< [PROCTORING_LOG]
                             └──< [RESULTS]
```

### Applying the Database Migration
Make sure MySQL server is running locally:
```bash
mysql -u root -p smart_exam < database/migration_coding_support.sql
```
*(This creates `CODING_TEMPLATES`, `CODING_TEST_CASES`, `CODING_SUBMISSIONS`, `CODING_SUBMISSION_TEST_RESULTS`, and populates demo questions for Java, Python, C, C++, and JS).*

---

## 🚀 How to Run the Application

### Option A: Run the Spring Boot Java Backend (Recommended)
1. Navigate to the `backend-java` folder:
   ```bash
   cd backend-java
   ```
2. Build and run using the Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Or on Windows Command Prompt: `mvnw.cmd spring-boot:run`)*
   *(Or if Maven is installed: `mvn spring-boot:run`)*

The Spring Boot backend will start on **`http://localhost:8080`**.

### Option B: Run the Python Flask Backend
If you want to run the upgraded Python server:
```bash
cd backend
python -m pip install -r requirements.txt
python app.py
```
*(Runs on `http://localhost:8080`)*

---

### Opening the Frontend
Open `frontend/index.html` in any web browser (Chrome, Edge, Firefox).

### Testing the Coding Assessment
1. **Login as Student**:
   - Email: `dgarg9_be24@thapar.edu` (or click "Sign up" to create a new student account).
   - Password: `password123` (or your registered password).
2. **Select the Coding Exam**:
   - Click **"Start Assessment"** on **"Coding Assessment & Data Structures"**.
3. **Solve Question 1 (Reverse an Array)**:
   - Select your language (e.g., Python 3 or Java).
   - Review the problem statement, constraints, and sample test cases on the left pane.
   - Enter your code in the Monaco Editor on the right pane.
   - Enter custom input in the bottom drawer and click **"▶ Run Code"**.
   - Click **"✓ Submit Code"** to evaluate against all sample and hidden test cases.
4. **Finish & Submit**:
   - Click **"Finish & Submit"** in the top bar to record your final score and view your results in `result.html`.
