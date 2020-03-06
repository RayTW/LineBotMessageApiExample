package line.example.bot.messageapi;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import line.example.game.GuessGame;
import line.example.game.GuessResult;
import line.example.sweepstake.SweepstakeManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
public class LineBotApplication {
  private Map<BotCommand, FunctionThrowable<MessageEvent<TextMessageContent>, Message>> map;
  private ConcurrentHashMap<String, GuessGame> guessGameMap;
  private ReentrantLock gameLock;
  private SweepstakeManager sweepstakeManager;

  /** 初始化服務應用. */
  public LineBotApplication() {
    guessGameMap = new ConcurrentHashMap<>();
    gameLock = new ReentrantLock();
    sweepstakeManager = new SweepstakeManager();
    stepup();
  }

  private void stepup() {
    // 設定接收指令後要回應的訊息
    map = Collections.synchronizedMap(new EnumMap<>(BotCommand.class));

    map.put(
        BotCommand.HELP,
        (event) -> {
          return new TextMessage(BotCommand.getCommandsDetail());
        });

    map.put(
        BotCommand.ECHO,
        (event) -> {
          return new TextMessage(event.getMessage().getText().replace("/echo", ""));
        });

    map.put(
        BotCommand.ME,
        (event) -> {
          String userId = event.getSource().getUserId();
          String senderId = event.getSource().getSenderId();
          LineUser user = LineBot.getInstance().getLineUser(senderId, userId, false);
          StringBuilder txt = new StringBuilder();

          txt.append("使用者名稱:");
          txt.append(user.getDisplayName());
          txt.append(System.lineSeparator());
          txt.append("使用者圖片:");
          txt.append(user.getPictureUrl());
          return new TextMessage(txt.toString());
        });

    map.put(
        BotCommand.USER_ID,
        (event) -> {
          return new TextMessage("user id :" + event.getSource().getUserId());
        });
    map.put(BotCommand.LUCKYDRAW, sweepstakeManager::luckyDraw);
    map.put(BotCommand.LUCKYDRAW_STATUS, sweepstakeManager::luckyStatus);
    map.put(BotCommand.LUCKY_ALL, sweepstakeManager::luckyAll);
    map.put(BotCommand.LUCKYDRAW_FINISH, sweepstakeManager::luckyFinish);

    map.put(
        BotCommand.GUESS_BEGIN,
        (event) -> {
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
        });

    map.put(
        BotCommand.GUESS_FINISH,
        (event) -> {
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
        });
  }

  private Message handleMismatchCommandText(MessageEvent<TextMessageContent> event)
      throws Exception {
    String originalMessageText = event.getMessage().getText();
    // 用戶留言參加抽獎
    Message reply = sweepstakeManager.joinLineUser(event);

    // 有相同關鍵字的抽獎活動
    if (reply != null) {
      return reply;
    }

    String senderId = event.getSource().getSenderId();
    gameLock.lock();
    try {
      GuessGame game = guessGameMap.get(senderId);
      if (game != null) {
        String guessDigits = originalMessageText.trim();

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

  public Map<BotCommand, FunctionThrowable<MessageEvent<TextMessageContent>, Message>>
      getActionMap() {
    return map;
  }

  /**
   * BOT機器人接收訊息.
   *
   * @param event 訊息事件
   * @return
   */
  @EventMapping
  public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    try {
      String command = event.getMessage().getText().split(" ")[0];
      BotCommand botEnum = BotCommand.getBotCommand(command);

      // 沒有符合的指令
      if (botEnum == null) {
        return handleMismatchCommandText(event);
      }

      FunctionThrowable<MessageEvent<TextMessageContent>, Message> action = map.get(botEnum);
      // 符合的指令
      return action.apply(event);
    } catch (Exception e) {
      e.printStackTrace();
      return new TextMessage("出錯了，救救我~" + e);
    }
  }

  @EventMapping
  public void handleDefaultMessageEvent(Event event) {
    // not do anything
  }

  public static void main(String[] args) {
    SpringApplication.run(LineBotApplication.class, args);
  }
}
