@echo off


REM 调用 Python 程序
C:\ProgramData\anaconda3\python.exe SeceneShotDetectV2.py %1 %2

REM 等待 Python 程序完成
if %errorlevel% neq 0 (
  echo Python script execution failed.
  exit /b %errorlevel%
)

REM 编译 Java 代码
javac VideoPlayer.java

REM 运行 Java 程序
java VideoPlayer %1 %2