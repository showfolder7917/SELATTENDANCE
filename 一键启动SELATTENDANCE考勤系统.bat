@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

set "ROOT_DIR=%~dp0"
if "%ROOT_DIR:~-1%"=="\" set "ROOT_DIR=%ROOT_DIR:~0,-1%"

set "BACKEND_DIR=%ROOT_DIR%\SELSP"
set "FRONTEND_DIR=%ROOT_DIR%\SELVUE"
set "LOG_DIR=%ROOT_DIR%\runtime_logs"
set "BACKEND_ARGS=%LOG_DIR%\selattendance_backend.args"
set "BACKEND_RUNNER=%LOG_DIR%\run_selattendance_backend.bat"
set "FRONTEND_RUNNER=%LOG_DIR%\run_selattendance_frontend.bat"
set "JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\java.exe"
set "NPM_CMD=C:\Program Files\nodejs\npm.cmd"
set "FRONTEND_URL=http://127.0.0.1:5180/"

title SELATTENDANCE Launcher

echo.
echo [SELATTENDANCE] Launcher starting...
echo [SELATTENDANCE] Root dir    : %ROOT_DIR%
echo [SELATTENDANCE] Backend dir : %BACKEND_DIR%
echo [SELATTENDANCE] Frontend dir: %FRONTEND_DIR%

if not exist "%JAVA_EXE%" (
  echo [ERROR] Java 21 executable not found:
  echo %JAVA_EXE%
  exit /b 1
)

if not exist "%NPM_CMD%" (
  echo [ERROR] npm.cmd not found:
  echo %NPM_CMD%
  exit /b 1
)

if not exist "%BACKEND_DIR%\build\classes\java\main\com\sp\selfsp\SelfspApplication.class" (
  echo [ERROR] Backend compiled classes not found:
  echo %BACKEND_DIR%\build\classes\java\main\com\sp\selfsp\SelfspApplication.class
  exit /b 1
)

if not exist "%FRONTEND_DIR%\node_modules" (
  echo [ERROR] Frontend node_modules not found:
  echo %FRONTEND_DIR%\node_modules
  exit /b 1
)

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%" >nul 2>nul

for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":8090 " ^| findstr "LISTENING"') do taskkill /PID %%P /F >nul 2>nul
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":5180 " ^| findstr "LISTENING"') do taskkill /PID %%P /F >nul 2>nul

echo [SELATTENDANCE] Preparing backend config files...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$jar = Get-ChildItem -Path \"$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\jp.or.jasdec.sbf.ma\CPMACOMMON\1.0.36-SNAPSHOT\" -Recurse -Filter 'CPMACOMMON-1.0.36-SNAPSHOT.jar' -ErrorAction Stop | Select-Object -First 1 -ExpandProperty FullName; " ^
  "$target = '%BACKEND_DIR%\src\main\resources\config'; " ^
  "New-Item -ItemType Directory -Path $target -Force | Out-Null; " ^
  "Add-Type -AssemblyName System.IO.Compression.FileSystem; " ^
  "$zip = [System.IO.Compression.ZipFile]::OpenRead($jar); " ^
  "foreach($name in 'config/cpma-alert-msg-def.yml','config/cpma-checkerr-msg-def.yml','config/cpma-mgmtslog-msg-def.yml','config/cpma-api-authority-def.yml'){ " ^
  "  $entry = $zip.Entries | Where-Object { $_.FullName -eq $name }; " ^
  "  if($entry){ " ^
  "    $out = Join-Path $target ([System.IO.Path]::GetFileName($name)); " ^
  "    $reader = New-Object System.IO.StreamReader($entry.Open()); " ^
  "    $text = $reader.ReadToEnd(); " ^
  "    $reader.Dispose(); " ^
  "    [System.IO.File]::WriteAllText($out, $text, [System.Text.UTF8Encoding]::new($false)); " ^
  "  } " ^
  "} " ^
  "$zip.Dispose();"
if errorlevel 1 (
  echo [ERROR] Failed to prepare backend config files.
  exit /b 1
)

echo [SELATTENDANCE] Building backend arg file...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$backend = '%BACKEND_DIR%'; " ^
  "$argFile = '%BACKEND_ARGS%'; " ^
  "$jars = Get-ChildItem -Path \"$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\" -Recurse -Filter '*.jar' -ErrorAction Stop | Where-Object { $_.Name -notlike '*-sources.jar' -and $_.Name -notlike '*-javadoc.jar' -and $_.Name -notlike 'spring-boot-devtools-*.jar' }; " ^
  "$cp = @(\"$backend\build\classes\java\main\", \"$backend\build\resources\main\") + ($jars | Select-Object -ExpandProperty FullName); " ^
  "[System.IO.File]::WriteAllLines($argFile, @('-Dfile.encoding=UTF-8', '-Dspring.devtools.restart.enabled=false', '-cp', ($cp -join ';'), 'com.sp.selfsp.SelfspApplication'), [System.Text.Encoding]::ASCII);"
if errorlevel 1 (
  echo [ERROR] Failed to build backend arg file.
  exit /b 1
)

(
  echo @echo off
  echo cd /d "%BACKEND_DIR%"
  echo "%JAVA_EXE%" @"%BACKEND_ARGS%"
) > "%BACKEND_RUNNER%"

(
  echo @echo off
  echo cd /d "%FRONTEND_DIR%"
  echo "%NPM_CMD%" run dev:local
) > "%FRONTEND_RUNNER%"

echo [SELATTENDANCE] Opening backend window...
start "SELATTENDANCE Backend" "%BACKEND_RUNNER%"
timeout /t 8 /nobreak >nul

echo [SELATTENDANCE] Opening frontend window...
start "SELATTENDANCE Frontend" "%FRONTEND_RUNNER%"
timeout /t 5 /nobreak >nul

start "" "%FRONTEND_URL%" >nul 2>nul
echo.
echo [OK] Startup commands were launched.
echo [OK] Backend runner : %BACKEND_RUNNER%
echo [OK] Frontend runner: %FRONTEND_RUNNER%
echo [OK] Browser URL    : %FRONTEND_URL%
exit /b 0
