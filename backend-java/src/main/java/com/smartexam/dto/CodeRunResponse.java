package com.smartexam.dto;

public class CodeRunResponse {
    private String status;
    private String stdout;
    private String stderr;
    private String compileOutput;
    private Integer executionTimeMs = 0;
    private Integer memoryKb = 0;

    public CodeRunResponse() {}

    public CodeRunResponse(String status, String stdout, String stderr, String compileOutput, Integer executionTimeMs, Integer memoryKb) {
        this.status = status;
        this.stdout = stdout;
        this.stderr = stderr;
        this.compileOutput = compileOutput;
        this.executionTimeMs = executionTimeMs;
        this.memoryKb = memoryKb;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String getCompileOutput() {
        return compileOutput;
    }

    public void setCompileOutput(String compileOutput) {
        this.compileOutput = compileOutput;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Integer executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public Integer getMemoryKb() {
        return memoryKb;
    }

    public void setMemoryKb(Integer memoryKb) {
        this.memoryKb = memoryKb;
    }
}
