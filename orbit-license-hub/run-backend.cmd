@ECHO OFF
SETLOCAL

SET "JAVA_HOME=C:\Users\RD\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64"
SET "PATH=%JAVA_HOME%\bin;%PATH%"

CD /D "%~dp0backend"
CALL mvnw.cmd spring-boot:run

ENDLOCAL
