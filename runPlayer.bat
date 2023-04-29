@echo off

REM  
C:\Users\james\anaconda3\envs\576_project\python.exe SeceneShotDetectV2.py %1 %2

<<<<<<< Updated upstream
REM 
=======
REM call python program
C:\ProgramData\anaconda3\python.exe SeceneShotDetectV2.py %1 %2

REM wait for python finishing
>>>>>>> Stashed changes
if %errorlevel% neq 0 (
  echo Python script execution failed.
  exit /b %errorlevel%
)

<<<<<<< Updated upstream
REM 
javac VideoPlayer.java

REM 
java VideoPlayer %1 %2
=======
REM compile java program
javac VideoPlayer.java

REM run Java prgram
java VideoPlayer %1 %2

REM remove temp txts and temp directory
del .\ShotList.txt
del .\SceneList.txt
del .\ShotSubshotList.txt
rmdir .\tmpdata /s /q
>>>>>>> Stashed changes
