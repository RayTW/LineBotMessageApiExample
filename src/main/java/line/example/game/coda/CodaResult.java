package line.example.game.coda;

public class CodaResult {
  private int upper;
  private int lower;

  public int getUpper() {
    return upper;
  }

  public int getLower() {
    return lower;
  }

  public void setLower(int lower) {
    this.lower = lower;
  }

  public void setUpper(int upper) {
    this.upper = upper;
  }

  public boolean isTheSame() {
    return lower == upper;
  }

  public String toString() {
    return lower + "~" + upper;
  }
}
