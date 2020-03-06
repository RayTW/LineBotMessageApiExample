package line.example.game;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import line.example.bot.messageapi.LineBot;
import line.example.bot.messageapi.LineUser;

public class GuessGameManager {
  private ConcurrentHashMap<String, GuessGame> guessGameMap;
  private ReentrantLock gameLock;

  public GuessGameManager() {
    guessGameMap = new ConcurrentHashMap<>();
    gameLock = new ReentrantLock();
  }

  /**
   * 開新遊戲.
   *
   * @param event 訊息事件
   * @return 回覆的訊息
   * @throws Exception 任何例外錯誤
   */
  public Message begin(MessageEvent<TextMessageContent> event) throws Exception {
    String senderId = event.getSource().getSenderId();
    boolean isNewGame = false;
    gameLock.lock();
    try {
      if (guessGameMap.containsKey(senderId)) {
        return new TextMessage("1A2B遊戲進行中");
      }
      isNewGame = true;
    } finally {
      gameLock.unlock();
    }

    String userId = event.getSource().getUserId();
    LineUser lineUser = LineBot.getInstance().getLineUser(senderId, userId, true);
    StringBuilder txt = new StringBuilder();

    if (isNewGame) {
      GuessGame guessGame = new GuessGame(lineUser);
      guessGame.reset();
      guessGameMap.put(senderId, guessGame);
    }

    txt.append("遊戲名稱:1A2B");
    txt.append(System.lineSeparator());
    txt.append("自由參加，先答先贏");
    txt.append(System.lineSeparator());
    txt.append("囗創建者:" + lineUser.getDisplayName());

    return new TextMessage(txt.toString());
  }

  /**
   * 結束目前遊戲.
   *
   * @param event 訊息事件
   * @return 回覆的訊息
   * @throws Exception 任何例外錯誤
   */
  public Message finish(MessageEvent<TextMessageContent> event) throws Exception {
    String senderId = event.getSource().getSenderId();
    GuessGame old = guessGameMap.get(senderId);
    gameLock.lock();
    try {
      if (old == null) {
        return new TextMessage("目前沒有1A2B遊戲");
      } else {
        guessGameMap.remove(senderId);
      }
    } finally {
      gameLock.unlock();
    }
    String userId = event.getSource().getUserId();
    LineUser lineUser = LineBot.getInstance().getLineUser(senderId, userId, true);
    StringBuilder txt = new StringBuilder();

    txt.append("遊戲結束:1A2B");
    txt.append(System.lineSeparator());
    txt.append("囗創建者:" + lineUser.getDisplayName());
    txt.append(System.lineSeparator());
    txt.append("囗囗答案:" + old.getCpuAnswer());
    txt.append(System.lineSeparator());
    txt.append("猜測次數:" + old.getGuessTimes());

    return new TextMessage(txt.toString());
  }

  /**
   * 玩猜數字.
   *
   * @param event 訊息事件
   * @return 回覆的訊息
   * @throws Exception 任何例外錯誤
   */
  public Message guess(MessageEvent<TextMessageContent> event) throws Exception {
    String senderId = event.getSource().getSenderId();
    String message = event.getMessage().getText().trim();

    gameLock.lock();
    try {
      GuessGame game = guessGameMap.get(senderId);
      if (game != null) {
        String guessDigits = message.trim();

        if (game.tryGuess(guessDigits)) {
          String userId = event.getSource().getUserId();
          LineUser lineUser = LineBot.getInstance().getLineUser(senderId, userId, true);
          GuessResult result = game.guess(guessDigits);
          StringBuilder txt = new StringBuilder();

          txt.append("猜測玩家:" + lineUser.getDisplayName());
          txt.append(System.lineSeparator());
          txt.append("猜測次數:" + game.getGuessTimes());
          txt.append(System.lineSeparator());
          txt.append("猜測數字:" + guessDigits);
          txt.append(System.lineSeparator());
          txt.append("猜測結果:" + result.getCountA());
          txt.append("A");
          txt.append(result.getCountB());
          txt.append("B");

          // 有玩家猜中，遊戲結束
          if (result.getCountA() == game.getCpuAnswer().length()) {
            guessGameMap.remove(senderId);
            txt.append(System.lineSeparator());
            txt.append("遊戲結束，恭喜猜中[" + game.getCpuAnswer() + "]玩家 " + lineUser.getDisplayName());
          }

          return new TextMessage(txt.toString());
        }
      }
    } finally {
      gameLock.unlock();
    }
    return null;
  }
}
