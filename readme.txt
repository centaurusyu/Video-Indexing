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

3. Python environment installation
	(1) Use anaconda to manage the virtual environments
	(2) run 'conda create -n 576Project python=3.10' to create a virtual environment
	(3) run 'conda activate 576Project' to switch to the virtual environment
	(4) install packages:
		conda config --add channels conda-forge
		conda install pandas=1.5.3
		conda install -c conda-forge moviepy=1.0.3
		pip install opencv-python
		pip install scenedetect[opencv] --upgrade
	(5) locate the interpreter of new environment
	    use the command
			python -c "import sys; print(sys.executable)"

		example result:
			(576Project) D:\576project\Video-Indexing> python -c "import sys; print(sys.executable)"
			D:\anaconda\envs\576Project\python.exe

4. How to run the whole project on windows
	(1) replace "C:\ProgramData\anaconda3\python.exe" in line 5 with interpreter path from 3
	(2) open windows cmd
	(3) cd to the directory of the project
	(4) type "runPlayer.bat inputVideo inputAudio"
		- inputVideo is the video file's path
		- inputAudio is the audio file's path