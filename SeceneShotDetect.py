from moviepy.editor import *
import numpy as np
import pandas as pd
import sys
import time

start_time = time.time()
rgb_path = '../data/Ready_Player_One_rgb/InputVideo.rgb'
audio_path = '../data/Ready_Player_One_rgb/InputAudio.wav'

if len(sys.argv) == 3:
    rgb_path = sys.argv[1]
    audio_path = sys.argv[2]

if os.path.exists('./tmpdata') == False:
    os.mkdir('./tmpdata')


############################################################
######               generate mp4 file               #######
############################################################

# Load the RGB image data
frames = []
with open(rgb_path, 'rb') as f:
    while True:
        # Read the next frame from the RGB file
        frame = f.read(480 * 270 * 3)
        if not frame:
            break
        # Convert the raw data to a numpy array and append it to the frames list
        frame = np.frombuffer(frame, dtype='uint8').reshape((270, 480, 3))
        frames.append(frame)

# Load the WAV audio data
audio_clip = AudioFileClip(audio_path)

# Create a VideoClip object from the RGB frames and set the frame rate to 30fps
video_clip = ImageSequenceClip(frames, fps=30)

# Combine the video and audio clips
final_clip = video_clip.set_audio(audio_clip)

# Write the final clip to an MP4 file
final_clip.write_videofile("./tmpdata/test.mp4")

############################################################
######          perform scene shot detect            #######
############################################################

from scenedetect import SceneManager,scene_manager,\
    open_video, ContentDetector,ThresholdDetector,AdaptiveDetector,StatsManager


video_path = './tmpdata/test.mp4'


def adp_scene_detect(video_path,adp_threshold=3,min_scene_len=30):
    video = open_video(video_path,framerate=30,backend='opencv')
    adp_scene_manager = SceneManager(stats_manager=StatsManager())
    adp_scene_manager.add_detector(AdaptiveDetector(adp_threshold,min_scene_len))
    adp_scene_manager.detect_scenes(video, show_progress=True)
    adp_scene_manager.stats_manager.save_to_csv(csv_file='./tmpdata/STATS_FILE_PATH.csv')
    adp_scene_list = adp_scene_manager.get_scene_list()
    with open('./tmpdata/adp.csv', 'w', encoding='utf-8') as f1:
        scene_manager.write_scene_list(f1,adp_scene_list, include_cut_list=True, cut_list=None)
    # scene_manager.write_scene_list_html('./greatcon.html', scene_list)

adp_scene_detect(video_path,adp_threshold=3,min_scene_len=30)

def con_scene_detect(video_path,con_threshold=36.0,min_scene_len=30):
    video = open_video(video_path,framerate=30,backend='opencv')
    con_scene_manager = SceneManager()
    con_scene_manager.add_detector(ContentDetector(con_threshold,min_scene_len))
    con_scene_manager.detect_scenes(video, show_progress=True)
    con_scene_list = con_scene_manager.get_scene_list()
    with open('./tmpdata/con.csv', 'w', encoding='utf-8') as f2:
        scene_manager.write_scene_list(f2,con_scene_list, include_cut_list=True, cut_list=None)

con_scene_detect(video_path,con_threshold=30.0,min_scene_len=30)

############################################################
######           obtain detection result             #######
############################################################

adp = pd.read_csv('./tmpdata/adp.csv',skiprows=1)
con = pd.read_csv('./tmpdata/con.csv',skiprows=1)

start_frames1 = set(adp['Start Frame'])
start_frames2 = set(con['Start Frame'])

final_scene_list = start_frames1.intersection(start_frames2)
final_scene_shot_list = start_frames1.union(start_frames2)


final_scene_list = list(final_scene_list)
final_scene_list.sort()

final_scene_shot_list = list(final_scene_shot_list)
final_scene_shot_list.sort()

print('-'*50)
print('final_scene_list: ',final_scene_list)
print('-'*50)
print('final_scene_shot_list: ',final_scene_shot_list)

with open('SceneList.txt', 'w') as f:
    for item in final_scene_list:
        f.write("%s\n" % item)

with open('SceneShotList.txt', 'w') as f:
    for item in final_scene_shot_list:
        f.write("%s\n" % item)

end_time = time.time()
print('Time cost: ',end_time-start_time,'s')