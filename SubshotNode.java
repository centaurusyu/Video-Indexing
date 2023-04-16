// A tree node structure is used in here to record different subshots

public class SubshotNode {
   private int startFrame;
   private int endFrame;

   public SubshotNode(int startFrame, int endFrame) {
      this.startFrame = startFrame;
      this.endFrame = endFrame;
   }

   // Returns the starting frame of the subshot.
   public int getStartFrame() {
      return startFrame;
   }

   // Returns the ending frame of the subshot.
   public int getEndFrame() {
      return endFrame;
   }
}