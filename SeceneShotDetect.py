from moviepy.editor import *
import numpy as np
import pandas as pd
import sys
import time

start_time = time.time()
rgb_path = '../data/Ready_Player_One_rgb/InputVideo.rgb'
audio_path = '../data/Ready_Player_One_rgb/InputAudio.wav'
scene_interval = 30
shot_interval = 15

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
        # scene_manager.write_scene_list_html('./adp.html', adp_scene_list)

adp_scene_detect(video_path,adp_threshold=3,min_scene_len=scene_interval)

def con_scene_detect(video_path,con_threshold=36.0,min_scene_len=30):
    video = open_video(video_path,framerate=30,backend='opencv')
    con_scene_manager = SceneManager()
    con_scene_manager.add_detector(ContentDetector(con_threshold,min_scene_len))
    con_scene_manager.detect_scenes(video, show_progress=True)
    con_scene_list = con_scene_manager.get_scene_list()
    with open('./tmpdata/con.csv', 'w', encoding='utf-8') as f2:
        scene_manager.write_scene_list(f2,con_scene_list, include_cut_list=True, cut_list=None)
        # scene_manager.write_scene_list_html('./con.html', con_scene_list)

con_scene_detect(video_path,con_threshold=30.0,min_scene_len=scene_interval)

############################################################
######           obtain detection result             #######
############################################################

adp = pd.read_csv('./tmpdata/adp.csv',skiprows=1)
con = pd.read_csv('./tmpdata/con.csv',skiprows=1)

start_frames1 = set(adp['Start Frame'])
start_frames2 = set(con['Start Frame'])

def remove_duplicate(framelist: list, interval: int) -> None:
    for i in range(len(framelist) - 1, 0, -1):
        if framelist[i] - framelist[i - 1] < interval:
            framelist.pop(i)

final_scene_list = start_frames1.intersection(start_frames2)
final_scene_list = list(final_scene_list)
final_scene_list.sort()
remove_duplicate(final_scene_list, scene_interval)


# final_scene_shot_list = start_frames1.union(start_frames2)
# final_scene_shot_list = list(final_scene_shot_list)
# final_scene_shot_list.sort()

# print('-'*50)
# print('final_scene_list: ',final_scene_list)
# print('-'*50)
# print('final_scene_shot_list: ',final_scene_shot_list)

with open('SceneList.txt', 'w') as f:
    for item in final_scene_list:
        f.write("%s\n" % item)

# with open('SceneShotList.txt', 'w') as f:
#     for item in final_scene_shot_list:
#         f.write("%s\n" % item)

adp_shot_list = []
con_shot_list = []

def adp_shot_detect(video_path, end_time, frame_skip, adp_threshold=2,min_shot_len=15):
    video = open_video(video_path,framerate=30,backend='opencv')
    adp_shot_manager = SceneManager()
    adp_shot_manager.add_detector(AdaptiveDetector(adp_threshold,min_shot_len))
    adp_shot_manager.detect_scenes(video, end_time= 1000, frame_skip=10)
    adp_shot_list = adp_shot_manager.get_scene_list()
    with open('./tmpdata/adp_shot.csv', 'w', encoding='utf-8') as f1:
        scene_manager.write_scene_list(f1,adp_shot_list, include_cut_list=True, cut_list=None)


def con_shot_detect(video_path, end_time, frame_skip, con_threshold=20,min_shot_len=15):
    video = open_video(video_path,framerate=30,backend='opencv')
    con_shot_manager = SceneManager()
    con_shot_manager.add_detector(ContentDetector(con_threshold, min_shot_len))
    con_shot_manager.detect_scenes(video, end_time= 1000, frame_skip=10)
    con_shot_list = con_shot_manager.get_scene_list()
    with open('./tmpdata/con_shot.csv', 'w', encoding='utf-8') as f1:
        scene_manager.write_scene_list(f1,con_shot_list, include_cut_list=True, cut_list=None)

for idx in range(len(final_scene_list)):
    if idx == len(final_scene_list)-1:
        break
    else:
        start_frame = final_scene_list[idx]
        end_frame = final_scene_list[idx+1]
        adp_shot_detect(video_path, end_frame - 1, start_frame - 1)
        con_shot_detect(video_path, end_frame - 1, start_frame - 1)
        adp_shot = pd.read_csv('./tmpdata/adp_shot.csv',skiprows=1)
        con_shot = pd.read_csv('./tmpdata/con_shot.csv',skiprows=1)
        adp_shot1 = adp['Start Frame'].values.tolist()
        adp_shot_list += adp_shot1
        con_shot1 = con['Start Frame'].values.tolist()
        con_shot_list += con_shot1

final_shot_list = set(adp_shot_list).intersection(set(con_shot_list))
final_shot_list = list(final_shot_list)
final_shot_list.sort()
remove_duplicate(final_shot_list, shot_interval)

final_subshot_list = set(adp_shot_list).union(set(con_shot_list))
final_subshot_list = list(final_subshot_list)
final_subshot_list.sort()
remove_duplicate(final_subshot_list, shot_interval)

with open('ShotList.txt', 'w') as f:
    for item in final_shot_list:
        f.write("%s\n" % item)

with open('ShotSubshotList.txt', 'w') as f:
    for item in final_subshot_list:
        f.write("%s\n" % item)


end_time = time.time()
print('Time cost: ',end_time-start_time,'s')


# todo list
# 参数调小获取shot，subshot
# intersection ok 

# scene
# shot
# shot subshot