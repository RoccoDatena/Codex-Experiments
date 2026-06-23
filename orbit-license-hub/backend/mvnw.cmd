@ECHO OFF
SETLOCAL EnableDelayedExpansion
SET "BASE_DIR=%~dp0"
IF "%BASE_DIR:~-1%"=="\" SET "BASE_DIR=%BASE_DIR:~0,-1%"
SET "WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper"
SET "WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar"
SET "WRAPPER_PROPS=%WRAPPER_DIR%\maven-wrapper.properties"

IF NOT EXIST "%WRAPPER_JAR%" (
  FOR /F "tokens=1,* delims==" %%A IN (%WRAPPER_PROPS%) DO (
    IF /I "%%A"=="wrapperUrl" SET "WRAPPER_URL=%%B"
  )

  IF NOT DEFINED WRAPPER_URL (
    ECHO Could not find wrapperUrl in %WRAPPER_PROPS%
    EXIT /B 1
  )

  POWERSHELL -NoProfile -ExecutionPolicy Bypass -Command "New-Item -ItemType Directory -Force '%WRAPPER_DIR%' ^| Out-Null; Invoke-WebRequest -UseBasicParsing '!WRAPPER_URL!' -OutFile '%WRAPPER_JAR%'"
  IF ERRORLEVEL 1 (
    ECHO Failed to download maven-wrapper.jar
    EXIT /B 1
  )
)

java -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL
