1. Please use JAVA 8 to compile and run the program. 

	- The exact version we are using is Java 8u361, and can be downloaded at this website.
	  For macOS: https://www.oracle.com/java/technologies/downloads/#java8-mac
	  For windows: https://www.oracle.com/java/technologies/downloads/#java8-windows
	  For linux: https://www.oracle.com/java/technologies/downloads/#java8-linux

	- After downloading, you might need to configure the java environment.
	  For macOS: https://stackoverflow.com/questions/21964709/how-to-set-or-change-the-default-java-jdk-version-on-macos?page=1&tab=scoredesc#tab-top
	  For windows: https://www.happycoders.eu/java/how-to-switch-multiple-java-versions-windows/

	- It is very important that you are using Java 8 for this program, or it will not work.

2. Video information tested:

	(1) Ready Player One:

		- fps: 30 
		- resolution: 480x270
		- number of frame: 8682

	(2) The Long Dark:

		- fps: 30 
		- resolution: 480x270
		- number of frame: 6276

	(3) The Great Gatsby:

		- fps: 30 
		- resolution: 480x270
		- number of frame: 5686

3. How to run the whole project on windows
	(1) replace "C:\ProgramData\anaconda3\python.exe" in line 5 with your own path
	(2) open windows cmd
	(3) cd to the directory of the project
	(4) type "runPlayer.bat inputVideo inputAudio"
		- inputVideo is the video file's path
		- inputAudio is the audio file's path