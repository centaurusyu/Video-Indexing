/* The VideoIndex class contains indexing info of the video, and allows
 * the user to add scenes, shots, and subshots to it.
 */

import java.util.*;

public class VideoIndex {
   private List<SceneNode> scenes;

   public VideoIndex() {
      scenes = new ArrayList<SceneNode>();
   }

   // Add a scene into the index.
   // @param: "startFrame" is an integer >= 0 && <= max frames in the video.
   // @param: "endFrame" is an integer >= 0 && <= max frames in the video.
   public void addScene(int startFrame, int endFrame) {
      scenes.add(new SceneNode(startFrame, endFrame));
   }

   // Returns the number of scenes in the current index.
   public int getSceneNum() {
      return scenes.size();
   }

   // Returns a specific scene at a given position.
   // @param: position is an integer that >= 0.
   public SceneNode getScene(int position) {
      return position >= scenes.size() ? null : scenes.get(position);
   }

   // Returns the scene index for a given frame.
   // The scenes are 0 indexed. e.g. Returned value is 0 means Scene 1, etc.
   // @param: "frame" is a valid integer >= 0 && <= max frames in the video.
   public int getSceneIndex(int frame) {
      // In case the list is null, or zero scene in the list, return -1.
      if (scenes == null || scenes.size() == 0) {
         return -1;
      }
      for (int idx = 0; idx < scenes.size(); ++idx) {
         // frame in the range (both way inclusive):
         if (scenes.get(idx).getStartFrame() <= frame && frame <= scenes.get(idx).getEndFrame()) {
            return idx;
         }
      }
      // Not finding the result in the scene indices.
      return -1;
   }
}