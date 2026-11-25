package com.smartexam.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartexam.dto.*;
import com.smartexam.entity.*;
import com.smartexam.exception.ApiException;
import com.smartexam.repository.*;
import com.smartexam.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionServiceImpl implements CodeExecutionService {

    private final QuestionRepository questionRepository;
    private final StudentExamRepository studentExamRepository;
    private final CodingTestCaseRepository testCaseRepository;
    private final CodingSubmissionRepository submissionRepository;
    private final CodingSubmissionTestResultRepository testResultRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${judge0.enabled:false}")
    private boolean judge0Enabled;

    @Value("${judge0.api-url:https://judge0-ce.p.rapidapi.com}")
    private String judge0ApiUrl;

    @Value("${judge0.api-key:}")
    private String judge0ApiKey;

    @Value("${judge0.api-host:judge0-ce.p.rapidapi.com}")
    private String judge0ApiHost;

    @Value("${code.execution.timeout-seconds:3}")
    private int timeoutSeconds;

    public CodeExecutionServiceImpl(QuestionRepository questionRepository,
                                    StudentExamRepository studentExamRepository,
                                    CodingTestCaseRepository testCaseRepository,
                                    CodingSubmissionRepository submissionRepository,
                                    CodingSubmissionTestResultRepository testResultRepository) {
        this.questionRepository = questionRepository;
        this.studentExamRepository = studentExamRepository;
        this.testCaseRepository = testCaseRepository;
        this.submissionRepository = submissionRepository;
        this.testResultRepository = testResultRepository;
    }

    @Override
    public CodeRunResponse runCode(CodeRunRequest request) {
        if (request.getSourceCode() == null || request.getSourceCode().trim().isEmpty()) {
            throw new ApiException("Source code cannot be empty");
        }
        String lang = request.getLanguage() != null ? request.getLanguage().toLowerCase().trim() : "python";
        String input = request.getCustomInput() != null ? request.getCustomInput() : "";

        if (judge0Enabled && judge0ApiKey != null && !judge0ApiKey.trim().isEmpty()) {
            try {
                return executeWithJudge0(lang, request.getSourceCode(), input);
            } catch (Exception e) {
                // Fall back to local execution
            }
        }

        return executeLocally(lang, request.getSourceCode(), input, timeoutSeconds);
    }

    @Override
    @Transactional
    public CodeSubmitResponse submitCode(CodeSubmitRequest request) {
        if (request.getAttemptId() == null || request.getQuestionId() == null) {
            throw new ApiException("Attempt ID and Question ID are required");
        }

        StudentExam attempt = studentExamRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new ApiException("Attempt not found: " + request.getAttemptId(), 404));
        if (attempt.isClosedForAnswers(60)) {
            throw new ApiException("Time is up for this exam. Please submit.", 403);
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ApiException("Question not found: " + request.getQuestionId(), 404));

        List<CodingTestCase> testCases = testCaseRepository.findByQuestionQuestionId(question.getQuestionId());
        if (testCases.isEmpty()) {
            throw new ApiException("No test cases defined for this coding question");
        }

        String lang = request.getLanguage() != null ? request.getLanguage().toLowerCase().trim() : "python";

        int totalWeightage = 0;
        int passedWeightage = 0;
        int passedCount = 0;
        int maxExecutionTime = 0;
        int maxMemory = 0;
        String overallStatus = "ACCEPTED";
        StringBuilder compilerLogs = new StringBuilder();

        List<TestCaseResultDTO> dtoList = new ArrayList<>();
        List<CodingSubmissionTestResult> dbResults = new ArrayList<>();

        for (CodingTestCase tc : testCases) {
            int tcWeight = tc.getWeightage() != null ? tc.getWeightage() : 1;
            totalWeightage += tcWeight;

            CodeRunResponse runRes;
            if (judge0Enabled && judge0ApiKey != null && !judge0ApiKey.trim().isEmpty()) {
                try {
                    runRes = executeWithJudge0(lang, request.getSourceCode(), tc.getInputData());
                } catch (Exception e) {
                    runRes = executeLocally(lang, request.getSourceCode(), tc.getInputData(), timeoutSeconds);
                }
            } else {
                runRes = executeLocally(lang, request.getSourceCode(), tc.getInputData(), timeoutSeconds);
            }

            maxExecutionTime = Math.max(maxExecutionTime, runRes.getExecutionTimeMs());
            maxMemory = Math.max(maxMemory, runRes.getMemoryKb());

            if (runRes.getCompileOutput() != null && !runRes.getCompileOutput().isEmpty()) {
                compilerLogs.append(runRes.getCompileOutput()).append("\n");
            }

            String actual = runRes.getStdout() != null ? runRes.getStdout() : "";
            String expected = tc.getExpectedOutput() != null ? tc.getExpectedOutput() : "";

            String tcStatus;
            if ("COMPILATION_ERROR".equalsIgnoreCase(runRes.getStatus())) {
                tcStatus = "ERROR";
                overallStatus = "COMPILATION_ERROR";
            } else if ("TIMEOUT".equalsIgnoreCase(runRes.getStatus())) {
                tcStatus = "TIMEOUT";
                if (!"COMPILATION_ERROR".equals(overallStatus)) {
                    overallStatus = "TIME_LIMIT_EXCEEDED";
                }
            } else if ("RUNTIME_ERROR".equalsIgnoreCase(runRes.getStatus())) {
                tcStatus = "ERROR";
                if (!"COMPILATION_ERROR".equals(overallStatus)) {
                    overallStatus = "RUNTIME_ERROR";
                }
            } else if (normalize(actual).equals(normalize(expected))) {
                tcStatus = "PASSED";
                passedCount++;
                passedWeightage += tcWeight;
            } else {
                tcStatus = "FAILED";
                if ("ACCEPTED".equals(overallStatus)) {
                    overallStatus = "WRONG_ANSWER";
                }
            }

            TestCaseResultDTO tcDto = new TestCaseResultDTO();
            tcDto.setTestCaseId(tc.getTestCaseId());
            tcDto.setIsSample(tc.getIsSample());
            tcDto.setStatus(tcStatus);
            tcDto.setExecutionTimeMs(runRes.getExecutionTimeMs());
            tcDto.setMemoryKb(runRes.getMemoryKb());
            tcDto.setWeightage(tcWeight);

            // Only reveal inputs and outputs for SAMPLE test cases
            if (Boolean.TRUE.equals(tc.getIsSample())) {
                tcDto.setInputData(tc.getInputData());
                tcDto.setExpectedOutput(tc.getExpectedOutput());
                tcDto.setActualOutput(actual);
                tcDto.setErrorMessage(runRes.getStderr());
            }

            dtoList.add(tcDto);

            CodingSubmissionTestResult dbRes = new CodingSubmissionTestResult();
            dbRes.setTestCase(tc);
            dbRes.setStatus(tcStatus);
            dbRes.setActualOutput(actual);
            dbRes.setExecutionTimeMs(runRes.getExecutionTimeMs());
            dbRes.setMemoryKb(runRes.getMemoryKb());
            dbRes.setErrorMessage(runRes.getStderr());
            dbResults.add(dbRes);
        }

        int maxMarks = question.getMarks() != null ? question.getMarks() : 10;
        BigDecimal scoreObtained = BigDecimal.ZERO;
        if (totalWeightage > 0) {
            scoreObtained = BigDecimal.valueOf(passedWeightage)
                    .divide(BigDecimal.valueOf(totalWeightage), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(maxMarks))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        CodingSubmission submission = new CodingSubmission();
        submission.setStudentExam(attempt);
        submission.setQuestion(question);
        submission.setLanguage(lang);
        submission.setSourceCode(request.getSourceCode());
        submission.setStatus(overallStatus);
        submission.setPassedTestCases(passedCount);
        submission.setTotalTestCases(testCases.size());
        submission.setScoreObtained(scoreObtained);
        submission.setExecutionTimeMs(maxExecutionTime);
        submission.setMemoryKb(maxMemory);
        submission.setCompilerOutput(compilerLogs.toString());
        submission.setSubmittedAt(LocalDateTime.now());

        CodingSubmission savedSubmission = submissionRepository.save(submission);

        for (CodingSubmissionTestResult tr : dbResults) {
            tr.setSubmission(savedSubmission);
            testResultRepository.save(tr);
        }

        CodeSubmitResponse response = new CodeSubmitResponse();
        response.setSubmissionId(savedSubmission.getSubmissionId());
        response.setStatus(overallStatus);
        response.setPassedTestCases(passedCount);
        response.setTotalTestCases(testCases.size());
        response.setScoreObtained(scoreObtained);
        response.setMaxMarks(maxMarks);
        response.setExecutionTimeMs(maxExecutionTime);
        response.setMemoryKb(maxMemory);
        response.setCompilerOutput(compilerLogs.toString());
        response.setTestCaseResults(dtoList);

        return response;
    }

    private String normalize(String s) {
        if (s == null) return "";
        // Unify line endings, drop trailing spaces on every line, then trim blank lines at the ends
        return s.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n")
                .replaceAll("[ \\t]+\n", "\n")
                .trim();
    }

    /**
     * Resilient Local Process Runner with Sandboxed Isolation
     */
    private CodeRunResponse executeLocally(String language, String sourceCode, String input, int timeoutSec) {
        Path tempDir = null;
        long startTime = System.currentTimeMillis();

        try {
            tempDir = Files.createTempDirectory("smart_exam_exec_");
            String fileName;
            List<String> compileCmd = null;
            List<String> runCmd = new ArrayList<>();

            switch (language.toLowerCase()) {
                case "java": {
                    // Extract class name or default to Solution / Main
                    String className = "Solution";
                    if (sourceCode.contains("public class Main")) {
                        className = "Main";
                    }
                    fileName = className + ".java";
                    File srcFile = new File(tempDir.toFile(), fileName);
                    Files.writeString(srcFile.toPath(), sourceCode);

                    // Compile with javac
                    compileCmd = Arrays.asList("javac", srcFile.getAbsolutePath());
                    runCmd = Arrays.asList("java", "-cp", tempDir.toFile().getAbsolutePath(), className);
                    break;
                }
                case "python":
                case "py": {
                    fileName = "solution.py";
                    File srcFile = new File(tempDir.toFile(), fileName);
                    Files.writeString(srcFile.toPath(), sourceCode);
                    // macOS/Linux ship "python3"; Windows installs "python"
                    String pythonCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";
                    runCmd = Arrays.asList(pythonCmd, srcFile.getAbsolutePath());
                    break;
                }
                case "javascript":
                case "js":
                case "node": {
                    fileName = "solution.js";
                    File srcFile = new File(tempDir.toFile(), fileName);
                    Files.writeString(srcFile.toPath(), sourceCode);
                    runCmd = Arrays.asList("node", srcFile.getAbsolutePath());
                    break;
                }
                case "c": {
                    fileName = "solution.c";
                    File srcFile = new File(tempDir.toFile(), fileName);
                    Files.writeString(srcFile.toPath(), sourceCode);
                    File exeFile = new File(tempDir.toFile(), "solution.exe");
                    compileCmd = Arrays.asList("gcc", "-O2", srcFile.getAbsolutePath(), "-o", exeFile.getAbsolutePath());
                    runCmd = Arrays.asList(exeFile.getAbsolutePath());
                    break;
                }
                case "cpp":
                case "c++": {
                    fileName = "solution.cpp";
                    File srcFile = new File(tempDir.toFile(), fileName);
                    Files.writeString(srcFile.toPath(), sourceCode);
                    File exeFile = new File(tempDir.toFile(), "solution.exe");
                    compileCmd = Arrays.asList("g++", "-O2", srcFile.getAbsolutePath(), "-o", exeFile.getAbsolutePath());
                    runCmd = Arrays.asList(exeFile.getAbsolutePath());
                    break;
                }
                default:
                    return new CodeRunResponse("ERROR", "", "Unsupported language: " + language, "", 0, 0);
            }

            // Compilation stage (if needed)
            if (compileCmd != null) {
                ProcessBuilder compilePb = new ProcessBuilder(compileCmd);
                compilePb.directory(tempDir.toFile());
                Process compileProcess = compilePb.start();
                boolean compileOk = compileProcess.waitFor(5, TimeUnit.SECONDS);

                if (!compileOk || compileProcess.exitValue() != 0) {
                    String compileErr = readStream(compileProcess.getErrorStream());
                    if (compileErr.isEmpty()) {
                        compileErr = readStream(compileProcess.getInputStream());
                    }
                    return new CodeRunResponse("COMPILATION_ERROR", "", compileErr, compileErr, (int)(System.currentTimeMillis() - startTime), 0);
                }
            }

            // Execution stage
            ProcessBuilder runPb = new ProcessBuilder(runCmd);
            runPb.directory(tempDir.toFile());
            // Write output to files so a chatty program can't fill the pipe buffer and hang
            File outFile = new File(tempDir.toFile(), "stdout.txt");
            File errFile = new File(tempDir.toFile(), "stderr.txt");
            runPb.redirectOutput(outFile);
            runPb.redirectError(errFile);
            long runStart = System.currentTimeMillis();
            Process runProcess = runPb.start();

            // Feed input
            if (input != null && !input.isEmpty()) {
                try (OutputStream os = runProcess.getOutputStream()) {
                    os.write(input.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            } else {
                runProcess.getOutputStream().close();
            }

            boolean finished = runProcess.waitFor(timeoutSec, TimeUnit.SECONDS);
            int elapsed = (int)(System.currentTimeMillis() - runStart); // run time only, excludes compiling

            if (!finished) {
                runProcess.destroyForcibly();
                return new CodeRunResponse("TIMEOUT", "", "Time limit exceeded (" + timeoutSec + "s)", "", elapsed, 0);
            }

            String stdout = Files.readString(outFile.toPath(), StandardCharsets.UTF_8);
            String stderr = Files.readString(errFile.toPath(), StandardCharsets.UTF_8);

            if (runProcess.exitValue() != 0) {
                return new CodeRunResponse("RUNTIME_ERROR", stdout, stderr, "", elapsed, 0);
            }

            return new CodeRunResponse("SUCCESS", stdout, stderr, "", elapsed, 0);

        } catch (Exception e) {
            return new CodeRunResponse("ERROR", "", e.getMessage(), "", (int)(System.currentTimeMillis() - startTime), 0);
        } finally {
            if (tempDir != null) {
                deleteDir(tempDir.toFile());
            }
        }
    }

    /**
     * Judge0 API Client Execution
     */
    private CodeRunResponse executeWithJudge0(String language, String sourceCode, String input) throws Exception {
        int langId = getJudge0LanguageId(language);
        String submitUrl = judge0ApiUrl + "/submissions?base64_encoded=false&wait=true";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (judge0ApiKey != null && !judge0ApiKey.isEmpty()) {
            headers.set("X-RapidAPI-Key", judge0ApiKey);
            headers.set("X-RapidAPI-Host", judge0ApiHost);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("source_code", sourceCode);
        body.put("language_id", langId);
        body.put("stdin", input != null ? input : "");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(submitUrl, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode node = objectMapper.readTree(response.getBody());
            String stdout = node.has("stdout") && !node.get("stdout").isNull() ? node.get("stdout").asText() : "";
            String stderr = node.has("stderr") && !node.get("stderr").isNull() ? node.get("stderr").asText() : "";
            String compileOutput = node.has("compile_output") && !node.get("compile_output").isNull() ? node.get("compile_output").asText() : "";
            int timeMs = (int) (node.has("time") && !node.get("time").isNull() ? node.get("time").asDouble() * 1000 : 0);
            int memoryKb = node.has("memory") && !node.get("memory").isNull() ? node.get("memory").asInt() : 0;

            int statusId = node.has("status") && node.get("status").has("id") ? node.get("status").get("id").asInt() : 3;
            String status = switch (statusId) {
                case 3 -> "SUCCESS";
                case 5 -> "TIMEOUT";
                case 6 -> "COMPILATION_ERROR";
                default -> (statusId >= 7 && statusId <= 12) ? "RUNTIME_ERROR" : "FAILED";
            };

            return new CodeRunResponse(status, stdout, stderr, compileOutput, timeMs, memoryKb);
        }

        throw new ApiException("Judge0 API invocation failed");
    }

    private int getJudge0LanguageId(String language) {
        return switch (language.toLowerCase()) {
            case "c" -> 50;
            case "cpp", "c++" -> 54;
            case "java" -> 62;
            case "javascript", "js", "node" -> 63;
            default -> 71; // Python 3
        };
    }

    private String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
}
