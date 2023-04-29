@echo off

REM  
C:\Users\james\anaconda3\envs\576_project\python.exe SeceneShotDetectV2.py %1 %2

REM 
if %errorlevel% neq 0 (
  echo Python script execution failed.
  exit /b %errorlevel%
)

REM 
javac VideoPlayer.java

REM 
java VideoPlayer %1 %2