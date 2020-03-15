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
import line.example.game.coda.CodaGameManager;
import line.example.game.guess1a2b.GuessGameManager;
import line.example.sweepstake.SweepstakeManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
public class LineBotApplication {
  private Map<BotCommand, FunctionThrowable<MessageEvent<TextMessageContent>, Message>> map;
  private SweepstakeManager sweepstakeManager;
  private GuessGameManager guessGameManager;
  private CodaGameManager codaGameManager;

  /** 初始化服務應用. */
  public LineBotApplication() {
    guessGameManager = new GuessGameManager();
    sweepstakeManager = new SweepstakeManager();
    codaGameManager = new CodaGameManager();
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
    map.put(BotCommand.GUESS_BEGIN, guessGameManager::begin);
    map.put(BotCommand.GUESS_FINISH, guessGameManager::finish);
    map.put(BotCommand.CODA_BEGIN, codaGameManager::begin);
    map.put(BotCommand.CODA_FINISH, codaGameManager::finish);
  }

  private Message handleMismatchCommandText(MessageEvent<TextMessageContent> event)
      throws Exception {
    Message reply = null;

    reply = guessGameManager.guess(event);

    // 玩家的回應符合參與1A2B遊戲
    if (reply != null) {
      return reply;
    }

    reply = codaGameManager.guess(event);

    // 玩家的回應符合參與終極密碼遊戲
    if (reply != null) {
      return reply;
    }

    // 用戶留言參加抽獎
    reply = sweepstakeManager.joinLineUser(event);

    // 有相同關鍵字的抽獎活動
    if (reply != null) {
      return reply;
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
