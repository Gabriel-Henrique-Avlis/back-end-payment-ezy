@echo off
REM Script to setup Docker environment for Payment API (Windows)

echo ==========================================
echo Payment API - Docker Setup
echo ==========================================
echo.

REM Check if .env exists
if exist .env (
    echo WARNING: .env file already exists
    set /p OVERWRITE="Do you want to overwrite it? (y/n): "
    if /i not "%OVERWRITE%"=="y" (
        echo Exiting without changes.
        exit /b 0
    )
)

REM Check if OpenSSL is available
where openssl >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: OpenSSL not found in PATH
    echo.
    echo Please install OpenSSL or use Git Bash which includes it:
    echo https://git-scm.com/download/win
    echo.
    echo Alternatively, manually create .env with:
    echo   ENCRYPTION_KEY=your-base64-encoded-32-byte-key
    echo.
    pause
    exit /b 1
)

REM Generate encryption key using Git Bash
echo Generating encryption key...
for /f "delims=" %%i in ('bash -c "openssl rand -base64 32"') do set ENCRYPTION_KEY=%%i

if "%ENCRYPTION_KEY%"=="" (
    echo.
    echo ERROR: Failed to generate encryption key
    pause
    exit /b 1
)

REM Create .env file
(
    echo # Generated on %date% %time%
    echo ENCRYPTION_KEY=%ENCRYPTION_KEY%
) > .env

echo.
echo SUCCESS: .env file created
echo.
echo Encryption Key: %ENCRYPTION_KEY%
echo.
echo Next steps:
echo   1. Review the .env file (optional^)
echo   2. Run: docker-compose up --build
echo   3. Wait for services to be healthy
echo   4. Test the API: curl http://localhost:8080/payments
echo.
echo For detailed instructions, see DOCKER.md
echo.
pause
