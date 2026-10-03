@echo off
cd /d "%~dp0"
echo ==============================================
echo Restaurando debug.keystore para compilacion...
echo ==============================================

if exist "%~dp0debug.keystore.base64" (
    powershell -NoProfile -Command "[System.IO.File]::WriteAllBytes('%~dp0debug.keystore', [System.Convert]::FromBase64String((Get-Content '%~dp0debug.keystore.base64' -Raw).Trim()))"
    if exist "%~dp0debug.keystore" (
        echo [OK] debug.keystore restaurado con exito desde base64.
        goto end
    )
)

echo debug.keystore.base64 no encontrado o fallo. Generando nuevo keystore con keytool...
keytool -genkey -v -keystore "%~dp0debug.keystore" -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"

if exist "%~dp0debug.keystore" (
    echo [OK] debug.keystore generado con exito.
) else (
    echo [ERROR] No se pudo crear debug.keystore. Verifica tener Java instalado.
)

:end
echo ==============================================
pause
