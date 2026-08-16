@echo off
setlocal
cd /d "%~dp0"

call gradlew.bat testDebugUnitTest lintDebug assembleRelease
if errorlevel 1 (
    echo.
    echo Die Google-Maps-Links oder der App-Build enthalten einen Fehler.
    echo Die genaue Meldung steht weiter oben.
) else (
    echo.
    echo Fertig: app\build\outputs\apk\release\app-release.apk
)

pause
