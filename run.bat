@echo off
echo Compiling Hotel Management System...
javac *.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilation successful!
    echo.
    echo Starting Hotel Management Server...
    echo.
    echo Open your browser and navigate to: http://localhost:8080
    echo.
    java HotelServer
) else (
    echo.
    echo Compilation failed!
    echo Please check your Java installation and try again.
    pause
)
