package line.example.game;

public class GuessResult {
  private int countA;
  private int countB;

  public int getCountA() {
    return countA;
  }

  public int getCountB() {
    return countB;
  }

  public void incrementCountA() {
    countA++;
  }

  public void incrementCountB() {
    countB++;
  }
}
