// A tree node structure is used in here to record different scenes

import java.util.*;

public class SceneNode {
   private int startFrame;
   private int endFrame;
   private List<ShotNode> shots;

   public SceneNode(int startFrame, int endFrame) {
      this.startFrame = startFrame;
      this.endFrame = endFrame;
      shots = new ArrayList<>();
   }

   // Returns the starting frame of the scene.
   public int getStartFrame() {
      return startFrame;
   }

   // Returns the ending frame of the scene.
   public int getEndFrame() {
      return endFrame;
   }

   // Add shots to the scene.
   // @param: "shotStartFrame" >= this.startFrame && <= this.endFrame.
   // @param: "shotEndFrame" >= this.startFrame && <= this.endFrame.
   public void addShotNode(int shotStartFrame, int shotEndFrame) {
      shots.add(new ShotNode(shotStartFrame, shotEndFrame));
   }

   // Returns the total number of shots in this scene.
   public int getShotsNum() {
      return shots.size();
   }

   // Returns a ShotNode at a given position.
   // @param: "index" is an integer >= 0.
   public ShotNode getShot(int index) {
      return index >= shots.size() ? null : shots.get(index);
   }

   // Returns the shot index for a given frame.
   // The shots are 0 indexed. e.g. Returned value is 0 means Shot 1, etc.
   // @param: "frame" is a valid integer >= 0 && < max frames in the video.
   public int getShotIndex(int frame) {
      // In case the list is null, or zero shot in the list, return -1.
      if (shots == null || shots.size() == 0) {
         return -1;
      }
      for (int idx = 0; idx < shots.size(); ++idx) {
         // frame in the range (both way inclusive):
         if (shots.get(idx).getStartFrame() <= frame && frame <= shots.get(idx).getEndFrame()) {
            return idx;
         }

      }
      // Not finding the result in the shot indices.
      return -1;
   }
}