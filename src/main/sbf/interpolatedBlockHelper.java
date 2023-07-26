
HashMap<String,InterpolatedBlock> interpolatedBlocks = new HashMap<>();

public class InterpolatedBlock() {
  double x = null;
  double y = null;
  double z = null;
  double movingToX = null;
  double movingToY = null;
  double movingToZ = null;
  double stepX = null;
  double stepY = null;
  double stepZ = null;
  boolean moving = false;
  int step = 0;
  int steps = 0;
  
  // Init
  public void InterpolatedBlock(double x,double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  // Teleport
  public void goto(double x,double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  // Setup For Interpolation
  public void slideTo(double x,double y, double z,int steps) {
    this.movingToX = x;
    this.movingToY = y;
    this.movingToY = z;
    this.steps = steps;
    this.moving = true;
    this.stepX = (this.movingToX-this.x)/this.steps;
    this.stepY = (this.movingToY-this.y)/this.steps;
    this.stepZ = (this.movingToZ-this.z)/this.steps;
  }
  // Increment interpolation
  public void nextStep() {
    if(step<steps) {
      this.x += this.stepX;
      this.y += this.stepY;
      this.z += this.stepZ;
    }
  }
}
InterpolatedBlock movingBlock = new InterpolatedBlock(20,50,20);
movingBlock.slideTo(40,30,20,20);
interpolatedBlocks.put("Berberis Highlighter",movingBlock);

for(InterpolatedBlock block:interpolatedBlocks) {
  block.nextStep();
}
