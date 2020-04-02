package line.example.game.guess1a2b;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import line.example.bot.messageapi.LineUser;

public class GuessGame {
  private LineUser organizerUser;
  private char[] cpu; // 電腦記錄的4位數
  private int count; // 猜幾個數字就答對
  private char[] digit; // 用來出題的0~9數字
  private int dightCount = 4;
  // 每個玩家在每局已猜測的次數
  private ConcurrentHashMap<String, AtomicInteger> guessCounterMap;
  private int guessTimesDefault = 100; // 每個玩家最多能猜幾次

  /**
   * 初始遊戲室.
   *
   * @param organizerUser 創建遊戲用戶
   * @param guessTime 每個玩家最多能猜幾次
   */
  public GuessGame(LineUser organizerUser, String guessTime) {
    this.organizerUser = organizerUser;
    guessCounterMap = new ConcurrentHashMap<>();

    if (guessTime != null && guessTime.matches("\\d+")) {
      try {
        guessTimesDefault = Integer.parseInt(guessTime);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 設定要玩猜幾個數字.
   *
   * <pre>
   * 初始化1A2B 重new玩家與電腦記憶的數值組合
   * 重置電腦記錄的4位數
   * </pre>
   */
  public void reset() {
    digit = new char[10]; // 放0~9數字,每局重新開始時打亂排列

    // 陣列裡先放0~9的字元
    char digitChar = '0';

    for (int i = 0; i < digit.length; i++) {
      digit[i] = digitChar;
      digitChar++;
    }

    cpu = new char[dightCount];
    //        cpu預設都放'0'
    for (int i = 0; i < cpu.length; i++) {
      cpu[i] = '0';
    }
    count = dightCount;
    for (int i = 0; i < digit.length; i++) {
      int rand = ThreadLocalRandom.current().nextInt(digit.length);
      char c = digit[rand];
      digit[rand] = digit[i];
      digit[i] = c;
    }
    for (int i = 0; i < cpu.length; i++) {
      cpu[i] = digit[i];
    }
  }

  /**
   * 檢查輸入的數字是否為合法，若合法回傳char陣列.<br>
   * 檢查輸入的是幾a幾b 回傳陣列 [0]:記錄位置對，數字對有幾個,[1]:記錄數字對，位置錯有幾個.
   *
   * @param userId 猜測的玩家
   * @param guessNumber 猜測的數字組合
   * @return
   */
  public Optional<GuessResult> tryGuess(String userId, String guessNumber) {
    String number = guessNumber.trim();
    if (guessNumber.length() != dightCount) {
      return Optional.ofNullable(null);
    }

    if (!number.matches("[0-9]{" + count + "}")) {
      System.out.println("輸入的數字不是0~9的組合 或 數字個數不符");
      return Optional.ofNullable(null);
    }

    AtomicInteger counter = guessCounterMap.get(userId);

    if (counter == null) {
      counter = new AtomicInteger(guessTimesDefault);
      guessCounterMap.put(userId, counter);
    }

    if (counter.get() <= 0) {
      return Optional.ofNullable(null);
    }

    final char[] cAry = number.toCharArray();
    // 判斷輸入的數值是否有重複
    for (int i = 0; i < cAry.length; i++) {
      for (int j = i + 1; j < cAry.length; j++) {
        if (cAry[i] == cAry[j]) {
          System.out.println("輸入的數字組合有重複");
          return Optional.ofNullable(null);
        }
      }
    }
    GuessResult ab = new GuessResult(); // [0]:記錄位置對，數字對有幾個,[1]:記錄數字對，位置錯有幾個

    for (int i = 0; i < cpu.length; i++) {
      for (int j = 0; j < cpu.length; j++) {
        // 數字對，位置也一樣，就是1A
        if ((number.charAt(i) == cpu[j]) && (i == j)) {
          ab.incrementCountA();
        } else if ((number.charAt(i) == cpu[j]) && i != j) { // 數字對，位置不一樣，就是1B
          ab.incrementCountB();
        }
      }
    }
    counter.decrementAndGet();

    return Optional.of(ab);
  }

  /**
   * 取得cpu的解答.
   *
   * @return
   */
  public String getCpuAnswer() {
    return new String(cpu);
  }

  /**
   * 取得指定玩家剩餘可猜的次數.
   *
   * @param userId 猜測玩家
   * @return
   */
  public int getGuessTimes(String userId) {
    return guessCounterMap.get(userId).get();
  }

  public LineUser getOrganizerUser() {
    return organizerUser;
  }

  public int getGuessTimesDefault() {
    return guessTimesDefault;
  }
}
