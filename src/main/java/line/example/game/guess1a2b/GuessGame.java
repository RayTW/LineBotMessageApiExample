package line.example.game.guess1a2b;

import java.util.concurrent.ThreadLocalRandom;
import line.example.bot.messageapi.LineUser;

public class GuessGame {
  private LineUser organizerUser;
  private char[] cpu; // 電腦記錄的4位數
  private int count; // 猜幾個數字就答對
  private int guessTimes; // 猜的次數
  private char[] digit; // 用來出題的0~9數字
  private int dightCount = 4;

  public GuessGame(LineUser organizerUser) {
    this.organizerUser = organizerUser;
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
    guessTimes = 0;
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
   * 檢查輸入的數字是否為合法，若合法回傳char陣列.
   *
   * @param guessNumber 猜測的數字組合
   * @return
   */
  public boolean tryGuess(String guessNumber) {
    String number = guessNumber.trim();
    if (guessNumber.length() != dightCount) {
      return false;
    }

    if (!number.matches("[0-9]{" + count + "}")) {
      System.out.println("輸入的數字不是0~9的組合 或 數字個數不符");
      return false;
    }
    final char[] cAry = number.toCharArray();
    // 判斷輸入的數值是否有重複
    for (int i = 0; i < cAry.length; i++) {
      for (int j = i + 1; j < cAry.length; j++) {
        if (cAry[i] == cAry[j]) {
          System.out.println("輸入的數字組合有重複");
          return false;
        }
      }
    }
    return true;
  }

  /**
   * 檢查輸入的是幾a幾b 回傳陣列 [0]:記錄位置對，數字對有幾個,[1]:記錄數字對，位置錯有幾個.
   *
   * @param guessDigits 猜測的數字組合
   * @return
   */
  public GuessResult guess(String guessDigits) {
    GuessResult ab = new GuessResult(); // [0]:記錄位置對，數字對有幾個,[1]:記錄數字對，位置錯有幾個

    for (int i = 0; i < cpu.length; i++) {
      for (int j = 0; j < cpu.length; j++) {
        // 數字對，位置也一樣，就是1A
        if ((guessDigits.charAt(i) == cpu[j]) && (i == j)) {
          ab.incrementCountA();
        } else if ((guessDigits.charAt(i) == cpu[j]) && i != j) { // 數字對，位置不一樣，就是1B
          ab.incrementCountB();
        }
      }
    }
    guessTimes++; // 猜測次數++
    return ab;
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
