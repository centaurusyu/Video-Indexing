import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.sound.sampled.*;

public class VideoPlayer {
    private static final int WIDTH = 480;
    private static final int HEIGHT = 270;
    private static final int FPS = 30;

    private static int currFrame = 0; // 0-indexed, this is where the video starts
    private static int totalFrame = 0; // total frames in the video

    private static File videoFile = null; // video file
    private static Clip audioClip = null; // auido clip object

    private static JFrame frame = null; // the UI Outlayer
    private static JLabel videoLabel = null; // update this label with new frames

    private static VideoIndex videoIndex = null; // where the video index is stored

    private static boolean isPlaying = false; // not playing at the beginning
    private static boolean stopClicked = false; // check if stop is clicked

    private static ArrayList<Integer> startFrame = new ArrayList<>();
    private static ArrayList<Integer> endFrame = new ArrayList<>();

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

        // Get audio clip:
        try {
            File audioFile = new File(audioFileName);
            audioClip = AudioSystem.getClip();
            audioClip.open(AudioSystem.getAudioInputStream(audioFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Build the videoIndex for the given video:
        videoIndex = new VideoIndex(); // indexing is stored in here
        buildVideoIndex(); // where video index is built

        // Create UI:
        buildUI();

        // Run the video and audio:
        Thread videoThread = new Thread(() -> {
            while (true) {
                playVideo();
            }
        });
        videoThread.start();

        // Set a timer to synchronize the audio for every frame:
        Timer audioTimer = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // if audio is faster than video, wait for a bit:
                long correctAudioTime = getClipStartTime(currFrame >= totalFrame ? totalFrame - 1 : currFrame);
                long actualAudioTime = audioClip.getMicrosecondPosition();
                long diff = actualAudioTime - correctAudioTime;
                long threshold = (long) 1000 / FPS;
                if (diff > threshold) {
                    audioClip.setMicrosecondPosition(correctAudioTime);
                }
            }
        });
        audioTimer.start();
    }

    // Returns the total frames in the video clip.
    private static int getTotalFrame() {
        long fileSize = videoFile.length();
        int bytesPerFrame = WIDTH * HEIGHT * 3;
        long totalFramesLong = fileSize / bytesPerFrame;
        return (int) totalFramesLong;
    }

    // Returns a BufferedImage for the given frame.
    // @param: "fm" is a frame that >= 0 && < max video frames
    private static BufferedImage getFrameImage(int fm) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        try {
            RandomAccessFile raf = new RandomAccessFile(videoFile, "r");
            FileChannel channel = raf.getChannel();
            int frameSize = WIDTH * HEIGHT * 3;
            ByteBuffer buffer = ByteBuffer.allocate(frameSize);
            channel.read(buffer, (long) fm * frameSize);
            buffer.rewind();

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    int r = buffer.get() & 0xff;
                    int g = buffer.get() & 0xff;
                    int b = buffer.get() & 0xff;
                    int rgb = (r << 16) | (g << 8) | b;
                    image.setRGB(x, y, rgb);
                }
            }
            // videoLabel.setIcon(extractFrame);
            buffer.clear();
            channel.close();
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // Builds the UI to show the video, the buttons, and indices.
    private static void buildUI() {
        JPanel rightPanel = new JPanel();

        // Create the outlayer of the UI:
        frame = new JFrame("Video Player");
        frame.setSize(new Dimension(900,270)); // this can be changed
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        GridBagConstraints c = new GridBagConstraints();
        rightPanel.setLayout(gLayout);

        // Create the video panel, and place it in the UI:
        videoLabel = new JLabel();
        videoLabel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        videoLabel.setIcon(new ImageIcon(getFrameImage(currFrame))); // First frame
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        rightPanel.add(videoLabel, c);

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
        rightPanel.add(mainControlPanel, c);

        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 1;
        c.gridy = 0;
        frame.getContentPane().add(rightPanel, c);

        // Add action to the buttons:
        setPlayButtonAction(playButton);
        setPauseButtonAction(pauseButton);
        setStopButtonAction(stopButton);

        // Set come constant:
        int scenceNum = videoIndex.getSceneNum();
        int shotNum = 0;
        int subNum = 0;

        // Create left panel:
        DefaultListModel<String> l1 = new DefaultListModel<>();
        for (int i = 0; i < scenceNum; i++) {
            l1.addElement("Scene " + (i + 1));
            SceneNode sc = videoIndex.getScene(i);
            startFrame.add(sc.getStartFrame());
            endFrame.add(sc.getEndFrame());
            shotNum = sc.getShotsNum();
            for (int j = 0; j < shotNum; j++) {
                l1.addElement("      Shot " + (j + 1));
                ShotNode shot = sc.getShot(j);
                startFrame.add(shot.getStartFrame());
                endFrame.add(shot.getEndFrame());
                subNum = shot.getSubshotsNum();
                for (int k = 0; k < subNum; k++) {
                    l1.addElement("            Subshot " + (k + 1));
                    SubshotNode sub = shot.getSubshot(k);
                    startFrame.add(sub.getStartFrame());
                    endFrame.add(sub.getEndFrame());
                }
            }
        }

        // Add auto-highlight function:
        JPanel leftPanel = new JPanel();
        JList<String> sceneList = new JList<>(l1);
        sceneList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                if (currFrame >= startFrame.get(index) && currFrame <= endFrame.get(index)) {
                    setBackground(Color.RED);
                }
                return this;
            }
        });

        // Add scroller view in case of overflow:
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(sceneList);
        sceneList.setLayoutOrientation(JList.VERTICAL);
        sceneList.clearSelection();
        scrollPane.setPreferredSize(new Dimension(200, 270));
        leftPanel.add(scrollPane);
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(leftPanel, c);
        setListAction(sceneList);

        frame.pack();
        // Set the JFrame as visible.
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // Set the action for the list of scene
    private static void setListAction(JList sceneList) {
        sceneList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting() && sceneList.getSelectedValue() != null) {
                    currFrame = startFrame.get(sceneList.getSelectedIndex());
                    audioClip.setMicrosecondPosition(getClipStartTime(currFrame));
                    if (!isPlaying) {
                        videoLabel.setIcon(new ImageIcon(getFrameImage(currFrame)));
                        frame.validate();
                        frame.repaint();
                    }
                    sceneList.clearSelection();
                }
            }
        });
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
                    if (currFrame == totalFrame) {
                        currFrame = 0;
                    }
                    // Play audio:
                    audioClip.setMicrosecondPosition(getClipStartTime(currFrame));
                    audioClip.start();
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
                    audioClip.stop();
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
                    audioClip.stop();
                    isPlaying = false;
                }
                stopClicked = true;
            }
        });
    }

    // Plays the video according to what was clicked previously.
    private static void playVideo() {
        // Video:
        while (isPlaying && currFrame < totalFrame) {
            videoLabel.setIcon(new ImageIcon(getFrameImage(currFrame++)));
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
    }

    // Returns the start time of the audio given the frame.
    // In microseconds unit for audioClip.
    // @param: "f" is a given frame.
    public static long getClipStartTime(int f) {
        return f * 1000000L / FPS;
    }


    // Builds video index based on the results in SceneList.txt, ShotList.txt, and
    // ShotSubshotList.txt.
    private static void buildVideoIndex() {
        // read data from the SceneList.txt, ShotList.txt, and ShotSubshotList.txt and
        // store the data in ArrayList.
        String[] fileNames = new String[] { "SceneList.txt", "ShotList.txt", "ShotSubshotList.txt" };

        ArrayList<Integer> sceneBreakPoints = readBreakPoints(fileNames[0]);
        ArrayList<Integer> shotBreakPoints = readBreakPoints(fileNames[1]);
        ArrayList<Integer> subshotBreakPoints = readBreakPoints(fileNames[2]);

        removeNearbyPints(sceneBreakPoints, shotBreakPoints, subshotBreakPoints);
        int indOfShotBrkPoint = 0;
        int indOfSubshotBrkPoint = 0;
        int indOfCurScene = 0;
        int indOfCurShot = 0;

        for (int i = 0; i < sceneBreakPoints.size() - 1; i++) {
            int sceneStart = sceneBreakPoints.get(i) + 1;
            int sceneEnd = sceneBreakPoints.get(i + 1);
            videoIndex.addScene(sceneStart, sceneEnd);

            int shotStart = sceneStart;
            int shotEnd = 0;
            while (indOfShotBrkPoint < shotBreakPoints.size() && shotBreakPoints.get(indOfShotBrkPoint) < sceneEnd
                    && shotBreakPoints.get(indOfShotBrkPoint) > sceneStart) {


                shotEnd = shotBreakPoints.get(indOfShotBrkPoint);
                videoIndex.getScene(indOfCurScene).addShotNode(shotStart, shotEnd);

                // add subshot
                int subShotStart = shotStart;
                int subShotEnd = 0;

                while(indOfSubshotBrkPoint < subshotBreakPoints.size() && subshotBreakPoints.get(indOfSubshotBrkPoint) < shotEnd && subshotBreakPoints.get(indOfSubshotBrkPoint) > shotStart){

                    subShotEnd = subshotBreakPoints.get(indOfSubshotBrkPoint);
                    videoIndex.getScene(indOfCurScene).getShot(indOfCurShot).addSubshotNode(subShotStart, subShotEnd);

                    subShotStart = subshotBreakPoints.get(indOfSubshotBrkPoint) + 1;
                    indOfSubshotBrkPoint++;
                }

                if (subShotEnd != 0) {
                    videoIndex.getScene(indOfCurScene).getShot(indOfCurShot).addSubshotNode(subShotStart, shotEnd);
                }

                // add subshot
                shotStart = shotBreakPoints.get(indOfShotBrkPoint) + 1;
                indOfShotBrkPoint++;
                indOfCurShot++;
            }

            if (shotEnd != 0) {
                videoIndex.getScene(indOfCurScene).addShotNode(shotStart, sceneEnd);
                // add subshot
                int subShotStart = shotStart;
                int subShotEnd = 0;
                while (indOfSubshotBrkPoint < subshotBreakPoints.size()
                        && subshotBreakPoints.get(indOfSubshotBrkPoint) < shotEnd
                        && subshotBreakPoints.get(indOfSubshotBrkPoint) > shotStart) {

                    subShotEnd = subshotBreakPoints.get(indOfSubshotBrkPoint);
                    videoIndex.getScene(indOfCurScene).getShot(indOfCurShot).addSubshotNode(subShotStart, subShotEnd);

                    subShotStart = subshotBreakPoints.get(indOfSubshotBrkPoint);
                    indOfSubshotBrkPoint++;
                }
    
                if(subShotEnd != 0){
                    videoIndex.getScene(indOfCurScene).getShot(indOfCurShot).addSubshotNode(subShotStart, shotEnd);
                }
                // add subshot
            }
            indOfCurScene++;
            indOfCurShot = 0;
        }
    }

    // Returns the ArrayList containing the data in fileName.
    // @param: "fileName" is the name of file you want read.
    private static ArrayList<Integer> readBreakPoints(String fileName) {
        ArrayList<Integer> breakPoints = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(fileName));
            while (scanner.hasNextLine()) {
                breakPoints.add(Integer.parseInt(scanner.nextLine()));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return breakPoints;
    }

    // Removes the nearby points in the input arrays.
    // Points are considered nearby if their Euclidean distance is less than 5.
    /*
     * @param: "sceneBreakPoints" is the list contains break points of different
     * scenes.
     * 
     * @param: "shotBreakPoints" is the list contains break points of different
     * shot.
     * 
     * @param: "subshotBreakPoints" is the list contains break points of different
     * subshot.
     */
    private static void removeNearbyPints(ArrayList<Integer> sceneBreakPoints, ArrayList<Integer> shotBreakPoints,
            ArrayList<Integer> subshotBreakPoints) {
        int threshold = 5;
        // add the start point and end point for sceneBreakPoints
        if (sceneBreakPoints.get(0) <= threshold) {
            sceneBreakPoints.set(0, 0);
        } else {
            sceneBreakPoints.add(0, 0);
        }

        if (Math.abs(totalFrame - sceneBreakPoints.get(sceneBreakPoints.size() - 1)) <= threshold) {
            sceneBreakPoints.set(sceneBreakPoints.size() - 1, totalFrame);
        } else {
            sceneBreakPoints.add(totalFrame);
        }

        // remove the nearby points in shotBreakPoints and subshotBreakPoints
        for (int num : sceneBreakPoints) {
            for (int i = 0; i < shotBreakPoints.size(); i++) {
                if (Math.abs(num - shotBreakPoints.get(i)) <= threshold) {
                    shotBreakPoints.remove(i);
                    break;
                }
            }
        }

        for (int num : sceneBreakPoints) {
            for (int i = 0; i < subshotBreakPoints.size(); i++) {
                if (Math.abs(num - subshotBreakPoints.get(i)) <= threshold) {
                    subshotBreakPoints.remove(i);
                    break;
                }
            }
        }

        for (int num : shotBreakPoints) {
            for (int i = 0; i < subshotBreakPoints.size(); i++) {
                if (Math.abs(num - subshotBreakPoints.get(i)) <= threshold) {
                    subshotBreakPoints.remove(i);
                    break;
                }
            }
        }

    }
}