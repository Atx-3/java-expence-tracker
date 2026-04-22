@echo off
REM Expense Tracker - Quick Run Script
REM This script compiles and runs the application

echo ========================================
echo   Expense Tracker - Java Application
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from: https://adoptium.net/
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% equ 0 (
    echo Using Maven to build and run...
    echo.
    mvn clean compile exec:java -Dexec.mainClass="com.expensetracker.main.Main"
) else (
    echo Maven not found, compiling manually...
    echo.

    REM Create output directory
    if not exist "out" mkdir out

    REM Create lib directory for dependencies
    if not exist "lib" mkdir lib

    REM Download JSON library if not exists
    if not exist "lib\json-20240303.jar" (
        echo Downloading JSON library...
        curl -L -o lib\json-20240303.jar https://repo1.maven.org/maven2/org/json/json/20240303/json-20240303.jar
    )

    REM Compile Java files
    echo Compiling Java sources...
    javac -d out -cp "lib/*" src\com\expensetracker\**\*.java

    if %errorlevel% neq 0 (
        echo.
        echo ERROR: Compilation failed
        pause
        exit /b 1
    )

    echo.
    echo Starting application...
    echo.

    REM Run the application
    java -cp "out;lib/*" com.expensetracker.main.Main
)

pause
