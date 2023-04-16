import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.sound.sampled.*;

public class VideoPlayer {
    private static final int WIDTH = 480;
    private static final int HEIGHT = 270;
    private static final int FPS = 30;

    private static int currFrame = 7800; // 0-indexed, this is where the video starts
    private static int totalFrame = 0; // total frames in the video
    private static long audioClipCurrentTime = 0; // in microseconds

    private static BufferedImage[] frameBuffer = null; // contains all the frames in the video

    private static File videoFile = null; // video file
    private static Clip audioClip = null; // auido clip object

    private static JFrame frame = null; // the UI Outlayer
    private static JLabel videoLabel = null; // update this label with new frames

    private static VideoIndex videoIndex = null; // where the video index is stored

    private static boolean isPlaying = false; // not playing at the beginning
    private static boolean stopClicked = false; // check if stop is clicked

    public static void main(String[] args) throws IOException, InterruptedException {
        // Parse the input arguments:
        if (args.length != 2) {
            System.out.println("Parsing parameters error: wrong number of arguments.");
            System.out.println("Please use the program this way:\n");
            System.out.println("\tjava VideoPlayer [.rgb video file] [.wav audio file]\n");
            System.exit(1);
        }
        String videoFileName = args[0].trim(), audioFileName = args[1].trim();

        // From the given .rgb video file, calculate the total number of frames:
        videoFile = new File(videoFileName);
        totalFrame = getTotalFrame();

        // Get all frame information as BufferedImage in the frameBuffer:
        frameBuffer = new BufferedImage[totalFrame];
        extractFrame();

        // Get audio clip:
        try {
            File audioFile = new File(audioFileName);
            audioClip = AudioSystem.getClip();
            audioClip.open(AudioSystem.getAudioInputStream(audioFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // audioClip.setMicrosecondPosition(audioClipCurrentTime);
        // audioClip.start();

        // Build the videoIndex for the given video:
        videoIndex = new VideoIndex(); // indexing is stored in here
        // ------------------------need modification------------------------
        buildVideoIndex(); // where video index is built
        // -----------------------------------------------------------------

        // Create UI:
        buildUI();

        // Run the video and audio:
        Thread videoThread = new Thread(() -> {
            while (true) {
                playVideo();
            }
        });
        videoThread.start();
        videoThread.join();
    }

    // Returns the total frames in the video clip.
    private static int getTotalFrame() {
        long fileSize = videoFile.length();
        int bytesPerFrame = WIDTH * HEIGHT * 3;
        long totalFramesLong = fileSize / bytesPerFrame;
        return (int) totalFramesLong;
    }

    // Extract frames and stores them into a buffer.
    private static void extractFrame() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(videoFile, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(WIDTH * HEIGHT * 3);
        for (int idx = 0; idx < totalFrame; ++idx) {
            channel.read(buffer);
            buffer.rewind();
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT,
                    BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    int r = buffer.get() & 0xff;
                    int g = buffer.get() & 0xff;
                    int b = buffer.get() & 0xff;
                    int rgb = (r << 16) | (g << 8) | b;
                    image.setRGB(x, y, rgb);
                }
            }
            frameBuffer[idx] = image;
            buffer.clear();
        }
        channel.close();
        raf.close();
    }

    // Builds the UI to show the video, the buttons, and indices.
    private static void buildUI() throws IOException {
        // Create the outlayer of the UI:
        frame = new JFrame("Video Player");
        frame.setSize(new Dimension(900, 500)); // this can be changed
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        GridBagConstraints c = new GridBagConstraints();

        // Create the video panel, and place it in the UI:
        videoLabel = new JLabel();
        videoLabel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        videoLabel.setIcon(new ImageIcon(frameBuffer[currFrame])); // First frame
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(videoLabel, c);

        // Create three main buttons and add to UI:
        JButton playButton = new JButton("PLAY");
        JButton pauseButton = new JButton("PAUSE");
        JButton stopButton = new JButton("STOP");
        JPanel mainControlPanel = new JPanel();
        mainControlPanel.add(playButton);
        mainControlPanel.add(pauseButton);
        mainControlPanel.add(stopButton);
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(mainControlPanel, c);

        // Add action to the buttons:
        setPlayButtonAction(playButton);
        setPauseButtonAction(pauseButton);
        setStopButtonAction(stopButton);

        // Left part of the UI can be added in here:
        // Just a suggestion, you can do whatever u like.
        // --------------------------------------------

        // --------------------------------------------

        // Set the JFrame as visible.
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Set the action for the play button.
    // @param: "playButton" is a JButton.
    private static void setPlayButtonAction(JButton playButton) {
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPlaying) {
                    isPlaying = true;
                    if (stopClicked) {
                        update();
                    }
                    // check if the video is finished:
                    finishUpdate();
                    playAudio();
                }
                stopClicked = false;
            }
        });
    }

    // Set the action for the pause button.
    // @param: "pauseButton" is a JButton.
    private static void setPauseButtonAction(JButton pauseButton) {
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPlaying) {
                    pause();
                    isPlaying = false;
                }
                stopClicked = false;
            }
        });
    }

    // Set the action for the stop button.
    // @param: "stopButton" is a JButton.
    private static void setStopButtonAction(JButton stopButton) {
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPlaying) {
                    pause();
                    isPlaying = false;
                }
                stopClicked = true;
            }
        });
    }

    // Plays the audio.
    private static void playAudio() {
        // Begins the audio play:
        audioClipCurrentTime = getClipStartTime(currFrame);
        audioClip.setMicrosecondPosition(audioClipCurrentTime);
        audioClip.start();
    }

    // Plays the video according to what was clicked previously.
    private static void playVideo() {
        // Video:
        while (isPlaying && currFrame < totalFrame) {
            videoLabel.setIcon(new ImageIcon(frameBuffer[currFrame++]));
            frame.validate();
            frame.repaint();
            // Reaches the end of the video:
            if (currFrame == totalFrame) {
                isPlaying = false;
            }
            // Sleep:
            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Pauses the video and audio.
    private static void pause() {
        audioClip.stop();
        if (currFrame == totalFrame) {
            audioClipCurrentTime = getClipStartTime(totalFrame - 1);
            videoLabel.setIcon(new ImageIcon(frameBuffer[totalFrame - 1]));
        } else {
            audioClipCurrentTime = getClipStartTime(currFrame);
            videoLabel.setIcon(new ImageIcon(frameBuffer[currFrame]));
        }
        frame.validate();
        frame.repaint();
    }

    // Updates the frame to the beginning of its segment
    // and the audio clip start time.
    private static void update() {
        int tempFrame = currFrame;
        if (currFrame == totalFrame) {
            tempFrame = totalFrame - 1;
        }

        // Check which segment the current frame is on, update:
        int sceneIndex = videoIndex.getSceneIndex(tempFrame);
        if (sceneIndex != -1) {
            int shotIndex = videoIndex.getScene(sceneIndex).getShotIndex(tempFrame);
            if (shotIndex != -1) {
                int subshotIndex = videoIndex.getScene(sceneIndex).getShot(shotIndex).getSubshotIndex(tempFrame);
                if (subshotIndex != -1) {
                    tempFrame = videoIndex.getScene(sceneIndex).getShot(shotIndex).getSubshot(subshotIndex)
                            .getStartFrame();
                } else {
                    tempFrame = videoIndex.getScene(sceneIndex).getShot(shotIndex).getStartFrame();
                }
            } else {
                tempFrame = videoIndex.getScene(sceneIndex).getStartFrame();
            }
        } else {
            // In this case, there must be something wrong with the code.
            System.out.println("Error: current frame out of bound.");
            System.exit(1);
        }

        currFrame = tempFrame;
        audioClipCurrentTime = getClipStartTime(currFrame);
    }

    // Pauses the video and audio and reset current frame and audio clip time
    // to the beginning when reaches the end of the video.
    private static void finishUpdate() {
        if (currFrame == totalFrame) {
            // Video freezes at last frame:
            videoLabel.setIcon(new ImageIcon(frameBuffer[totalFrame - 1]));
            frame.validate();
            frame.repaint();
            // Audio stops:
            audioClip.stop();
            currFrame = 0;
            audioClipCurrentTime = 0;
        }
    }

    // Returns the start time of the audio given the frame.
    // In microseconds unit for audioClip.
    // @param: "f" is a given frame.
    public static long getClipStartTime(int f) {
        return f * 1000000L / FPS;
    }

    // -----------------------------------------------------------------------------------
    // Code in this block need to be modified later to build actual video index.
    // I just created a dummy example in here for test with the ReadyPlayerOne clip.
    // Total 8682 frames for this clip, the index info here is totally random.
    // *** 0-indexed.

    // Scene 1: 0 ~ 1799 frame (0:00 - 1:00)
    // --- Shot 1: 0 ~ 899 frame (0:00 - 0:30)
    // --- Shot 2: 900 ~ 1799 frame (0:30 - 1:00)

    // Scene 2: 1800 - 5399 frame (1:00 - 3:00)
    // --- Shot 1: 1800 - 3599 frame (1:00 - 2:00)
    // === Subshot 1: 1800 ~ 2699 frame (1:00 - 1:30)
    // === Subshot 2: 2700 ~ 3599 frame (1:30 - 2:00)
    // --- Shot 2: 3600 ~ 5399 frame (2:00 - 3:00)

    // Scene 3: 5400 ~ 8681 frame (3:00 - end of video clip)
    // --- Shot 1: 5400 ~ 6749 frame (3:00 - 3:45)
    // --- Shot 2: 6750 ~ 7799 frame (3:45 - 4:20)
    // --- Shot 3: 7800 ~ 8681 frame (4:20 - end of video clip)

    private static void buildVideoIndex() {
        // Dummy data insertion below:
        // Scene 1:
        videoIndex.addScene(0, 1799);
        // --- Shot 1:
        videoIndex.getScene(0).addShotNode(0, 899);
        // -- Shot 2:
        videoIndex.getScene(0).addShotNode(900, 1799);

        // Scene 2:
        videoIndex.addScene(1800, 5399);
        // --- Shot 1:
        videoIndex.getScene(1).addShotNode(1800, 3599);
        // === Subshot 1:
        videoIndex.getScene(1).getShot(0).addSubshotNode(1800, 2699);
        // === Subshot 2:
        videoIndex.getScene(1).getShot(0).addSubshotNode(2700, 3599);
        // --- Shot 2:
        videoIndex.getScene(1).addShotNode(3600, 5399);

        // Scene 3:
        videoIndex.addScene(5400, 8681);
        videoIndex.getScene(2).addShotNode(5400, 6749);
        videoIndex.getScene(2).addShotNode(6750, 7799);
        videoIndex.getScene(2).addShotNode(7800, 8681);
    }
    // -----------------------------------------------------------------------------------
}