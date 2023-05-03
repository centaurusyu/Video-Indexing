@echo off


REM call python program
C:\ProgramData\anaconda3\python.exe SeceneShotDetect.py %1 %2

REM wait for python finishing
if %errorlevel% neq 0 (
  echo Python script execution failed.
  exit /b %errorlevel%
)

REM compile java program
javac VideoPlayer.java

REM run Java prgram
java VideoPlayer %1 %2

REM remove temp txts and temp directory
del .\ShotList.txt
del .\SceneList.txt
del .\ShotSubshotList.txt
del .\*.class
rmdir .\tmpdata /s /q