package line.example.game.coda;

import java.time.temporal.ValueRange;
import java.util.concurrent.ThreadLocalRandom;
import line.example.bot.messageapi.LineUser;

/**
 * 終極密碼.
 *
 * @author ray
 */
public class CodaGame {
  private LineUser organizerUser;
  private int anser; // 答案
  private int guessTimes; // 猜的次數
  private CodaResult bound;
  private static final int DEFAULT_ANSER_RANGE = 100;

  public CodaGame(LineUser organizerUser) {
    this.organizerUser = organizerUser;
  }

  /**
   * 設定要玩猜數字區間.
   *
   * <pre>
   * 初始化
   * </pre>
   */
  public ValueRange reset(String asnerBound) {
    int b = 0;
    try {
      b = Integer.parseInt(asnerBound);
    } catch (Exception e) {
      b = DEFAULT_ANSER_RANGE; // use default
    }
    guessTimes = 0;
    anser = ThreadLocalRandom.current().nextInt(b);
    this.bound = new CodaResult();
    this.bound.setLower(1);
    this.bound.setUpper(b);

    return ValueRange.of(bound.getLower(), bound.getUpper());
  }

  /**
   * 檢查輸入的數字是否為合法，若合法回傳char陣列.
   *
   * @param guessNumber 猜測的數字組合
   * @return
   */
  public boolean tryGuess(String guessNumber) {
    String number = guessNumber.trim();

    if (!number.matches("[0-9]+")) {
      return false;
    }

    try {
      int guess = Integer.parseInt(guessNumber);

      if (guess < bound.getLower()) {
        return false;
      }

      if (guess > bound.getUpper()) {
        return false;
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  /**
   * 猜測結果.
   *
   * @param guessNumber 猜測答案
   * @return
   */
  public CodaResult guess(String guessNumber) {
    int guess = Integer.parseInt(guessNumber);

    if (guess == anser) {
      bound.setLower(guess);
      bound.setUpper(guess);
    } else {
      if (guess < anser) {
        bound.setLower(guess);
      } else {
        bound.setUpper(guess);
      }
    }
    guessTimes++;
    return bound;
  }

  /**
   * 取得cpu的解答.
   *
   * @return
   */
  public String getAnswer() {
    return String.valueOf(anser);
  }

  /**
   * 取得目前猜第n次數.
   *
   * @return
   */
  public int getGuessTimes() {
    return guessTimes;
  }

  public LineUser getOrganizerUser() {
    return organizerUser;
  }
}
