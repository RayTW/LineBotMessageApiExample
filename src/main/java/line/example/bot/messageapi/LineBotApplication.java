package line.example.bot.messageapi;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
public class LineBotApplication {
  private Map<BotCommand, FunctionThrowable<MessageEvent<TextMessageContent>, Message>> map;

  public LineBotApplication() {
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
          UserProfileResponse info = LineBot.getInstance().getInfo(senderId, userId);
          StringBuilder txt = new StringBuilder();

          txt.append("使用者名稱:");
          txt.append(info.getDisplayName());
          txt.append(System.lineSeparator());
          txt.append("使用者圖片:");
          txt.append(info.getPictureUrl());
          return new TextMessage(txt.toString());
        });

    map.put(
        BotCommand.USER_ID,
        (event) -> {
          return new TextMessage("user id :" + event.getSource().getUserId());
        });

    map.put(
        BotCommand.SWEEPSTAKE,
        (event) -> {
          String[] args = event.getMessage().getText().split(" ");
          String sweepstakesName = args[1];
          String keyword = args[2];

          StringBuilder txt = new StringBuilder();

          txt.append("抽將活動名稱:");
          txt.append(sweepstakesName);
          txt.append(System.lineSeparator());
          txt.append("以下留言\"" + keyword + "\"即可參加抽獎喔");
          return new TextMessage(txt.toString());
        });
  }

  /**
   * BOT機器人接收訊息.
   *
   * @param event 訊息事件
   * @return
   */
  @EventMapping
  public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    String originalMessageText = event.getMessage().getText();

    try {
      String command = originalMessageText.split(" ")[0];
      BotCommand botEnum = BotCommand.enumOf(command);
      FunctionThrowable<MessageEvent<TextMessageContent>, Message> action = map.get(botEnum);

      // 沒有符合的指令
      if (action == null) {
        return null;
      }
      // 符合的指令
      return action.apply(event);
    } catch (IllegalArgumentException e) {
      // not do anything
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return new TextMessage("出錯了，救救我~" + e);
    }
  }

  public Map<BotCommand, FunctionThrowable<MessageEvent<TextMessageContent>, Message>>
      getActionMap() {
    return map;
  }

  @EventMapping
  public void handleDefaultMessageEvent(Event event) {
    // not do anything
  }

  public static void main(String[] args) {
    SpringApplication.run(LineBotApplication.class, args);
  }
}
