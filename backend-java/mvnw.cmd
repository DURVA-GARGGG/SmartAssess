@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Batch Script for Smart Exam Backend
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "DIRNAME=%~dp0"
if "%DIRNAME:~-1%"=="\" set "DIRNAME=%DIRNAME:~0,-1%"
set "APP_HOME=%DIRNAME%"

@rem Detect Java 17+ for Spring Boot 3
set "JAVACMD="

if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
    set "JAVACMD=C:\Program Files\Java\jdk-26.0.2\bin\java.exe"
    set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2"
) else if exist "C:\Program Files\Java\latest\bin\java.exe" (
    set "JAVACMD=C:\Program Files\Java\latest\bin\java.exe"
    set "JAVA_HOME=C:\Program Files\Java\latest"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVACMD=%JAVA_HOME%\bin\java.exe"
)

if not defined JAVACMD (
    set "JAVACMD=java.exe"
)

set "WRAPPER_JAR=%APP_HOME%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

if not exist "%WRAPPER_JAR%" (
    echo [INFO] Downloading Maven Wrapper jar...
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%WRAPPER_JAR%')}"
)

"%JAVACMD%" %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%APP_HOME%" %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto fail
goto mainEnd

:fail
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal
