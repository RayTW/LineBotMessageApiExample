package line.example.game.coda;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import java.time.temporal.ValueRange;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import line.example.bot.messageapi.LineBot;
import line.example.bot.messageapi.LineUser;

public class CodaGameManager {
  private ConcurrentHashMap<String, CodaGame> groupMap;
  private ReentrantLock gameLock;

  public CodaGameManager() {
    groupMap = new ConcurrentHashMap<>();
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
      if (groupMap.containsKey(senderId)) {
        return new TextMessage("終極密碼遊戲進行中");
      }
      isNewGame = true;
    } finally {
      gameLock.unlock();
    }

    String userId = event.getSource().getUserId();
    String[] commands = event.getMessage().getText().split(" ");
    String max = commands.length > 1 ? commands[1] : null;
    LineUser lineUser = LineBot.getInstance().getLineUser(senderId, userId, true);
    StringBuilder txt = new StringBuilder();
    ValueRange range = null;

    if (isNewGame) {
      CodaGame guessGame = new CodaGame(lineUser);
      range = guessGame.reset(max);
      groupMap.put(senderId, guessGame);
    }

    txt.append("遊戲名稱:終極密碼");
    txt.append(System.lineSeparator());
    txt.append("自由參加，先答先贏");
    txt.append(System.lineSeparator());
    txt.append("數值範圍" + range.getMinimum() + "~" + range.getMaximum());
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
    CodaGame old = groupMap.get(senderId);
    gameLock.lock();
    try {
      if (old == null) {
        return new TextMessage("目前沒有終極密碼遊戲");
      } else {
        groupMap.remove(senderId);
      }
    } finally {
      gameLock.unlock();
    }
    String userId = event.getSource().getUserId();
    LineUser lineUser = LineBot.getInstance().getLineUser(senderId, userId, true);
    StringBuilder txt = new StringBuilder();

    txt.append("遊戲結束:終極密碼");
    txt.append(System.lineSeparator());
    txt.append("囗創建者:" + lineUser.getDisplayName());
    txt.append(System.lineSeparator());
    txt.append("囗囗答案:" + old.getAnswer());
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
      CodaGame game = groupMap.get(senderId);
      if (game != null) {
        String guessDigits = message.trim();

        if (game.tryGuess(guessDigits)) {

          String userId = event.getSource().getUserId();
          LineUser lineUser = LineBot.getInstance().getLineUser(senderId, userId, true);
          CodaResult result = game.guess(guessDigits);
          StringBuilder txt = new StringBuilder();

          txt.append("玩家:" + lineUser.getDisplayName());
          txt.append(System.lineSeparator());
          txt.append("次數:" + game.getGuessTimes());
          txt.append(System.lineSeparator());
          txt.append("結果:" + result.getLower() + "~" + result.getUpper());

          // 有玩家猜中，遊戲結束
          if (result.isTheSame()) {
            groupMap.remove(senderId);
            txt.append(System.lineSeparator());
            txt.append("遊戲結束，恭喜猜中[" + game.getAnswer() + "]玩家 " + lineUser.getDisplayName());
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
