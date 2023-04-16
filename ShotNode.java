// A tree node structure is used in here to record different shots

import java.util.*;

public class ShotNode {
   private int startFrame;
   private int endFrame;
   private List<SubshotNode> subshots;

   public ShotNode(int startFrame, int endFrame) {
      this.startFrame = startFrame;
      this.endFrame = endFrame;
      subshots = new ArrayList<>();
   }

   // Returns the starting frame of the shot.
   public int getStartFrame() {
      return startFrame;
   }

   // Returns the ending frame of the shot.
   public int getEndFrame() {
      return endFrame;
   }

   // Add subshots to the shot.
   // @param: "subshotStartFrame" >= this.startFrame && <= this.endFrame.
   // @param: "subshotEndFrame" >= this.startFrame && <= this.endFrame.
   public void addSubshotNode(int subshotStartFrame, int subshotEndFrame) {
      subshots.add(new SubshotNode(subshotStartFrame, subshotEndFrame));
   }

   // Returns the total number of subshots in this shot.
   public int getSubshotsNum() {
      return subshots.size();
   }

   // Returns a SubshotNode at a given position.
   // @param: "index" is an integer >= 0.
   public SubshotNode getSubshot(int index) {
      return index >= subshots.size() ? null : subshots.get(index);
   }

   // Returns the subshot index for a given frame.
   // The subshots are 0 indexed. e.g. Returned value is 0 means Subshot 1, etc.
   // @param: "frame" is a valid integer >= 0 && <= max frames in the video.
   public int getSubshotIndex(int frame) {
      // In case the list is null, or zero subshot in the list, return -1.
      if (subshots == null || subshots.size() == 0) {
         return -1;
      }
      for (int idx = 0; idx < subshots.size(); ++idx) {
         // frame in the range (both way inclusive):
         if (subshots.get(idx).getStartFrame() <= frame && frame <= subshots.get(idx).getEndFrame()) {
            return idx;
         }
      }
      // Not finding the result in the shot indices.
      return -1;
   }
}