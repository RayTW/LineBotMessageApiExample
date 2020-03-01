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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
public class LineBotApplication {
  private Map<BotCommand, FunctionThrowable<MessageEvent<TextMessageContent>, Message>> map;
  private ConcurrentHashMap<String, Sweepstake> sweepstakeMap; // k=關鍵字,v=活動物件

  public LineBotApplication() {
    sweepstakeMap = new ConcurrentHashMap<>();
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
    map.put(
        BotCommand.LUCKDRAW,
        (event) -> {
          String[] args = event.getMessage().getText().split(" ");
          String sweepstakesName = args[1];
          String keyword = args[2];

          if (getSweepstake(sweepstakesName) != null) {
            return new TextMessage("活動名稱:\"" + sweepstakesName + "\"被用囉");
          }

          if (sweepstakeMap.containsKey(keyword)) {
            return new TextMessage("留言關鍵字\"" + keyword + "\"被用囉");
          }
          String userId = event.getSource().getUserId();
          String senderId = event.getSource().getSenderId();
          LineUser lineUser = LineBot.getInstance().getLineUser(senderId, userId, true);
          Sweepstake sweepstake = new Sweepstake(senderId, sweepstakesName, keyword, lineUser);

          sweepstakeMap.put(keyword, sweepstake);

          StringBuilder txt = new StringBuilder();

          txt.append("活動名稱:");
          txt.append(sweepstakesName);
          txt.append(System.lineSeparator());
          txt.append("以下留言\"" + keyword + "\"即可參加抽獎喔");
          return new TextMessage(txt.toString());
        });

    map.put(
        BotCommand.LUCKDRAW_STATUS,
        (event) -> {
          String[] args = event.getMessage().getText().split(" ");
          String sweepstakesName = args[1];

          Optional<Sweepstake> sweepstake = getSweepstake(sweepstakesName);

          if (sweepstake.isPresent()) {
            return new TextMessage(sweepstake.get().toString());
          } else {
            return new TextMessage("查看抽獎活動結果，目前沒有\"" + sweepstakesName + "\"抽獎活動");
          }
        });

    map.put(
        BotCommand.LUCKDRAW_FINISH,
        (event) -> {
          String[] args = event.getMessage().getText().split(" ");
          String sweepstakesName = args[1];
          String luckyUserCount = args[2];

          Optional<Sweepstake> sweepstake = getSweepstake(sweepstakesName);

          if (sweepstake.isPresent()) {
            final List<LineUser> lucky =
                sweepstake.get().getLuckyUser(Integer.parseInt(luckyUserCount));

            StringBuilder txt = new StringBuilder();

            txt.append("活動名稱:");
            txt.append(sweepstakesName);
            txt.append(System.lineSeparator());
            txt.append("參加人數:" + sweepstake.get().getUserSize());
            txt.append(System.lineSeparator());
            txt.append("抽出人數:" + lucky.size());
            txt.append(System.lineSeparator());
            txt.append("中獎者:");
            txt.append(System.lineSeparator());

            lucky.forEach(
                user -> {
                  txt.append(user.getDisplayName());
                  txt.append(System.lineSeparator());
                });

            // 抽完中獎者，移除活動
            sweepstakeMap.remove(sweepstake.get().getKeyword());

            return new TextMessage(txt.toString());
          } else {
            return new TextMessage("沒有\"" + sweepstakesName + "\"抽獎活動");
          }
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
      BotCommand botEnum = BotCommand.getBotCommand(command);

      // 沒有符合的指令
      if (botEnum == null) {
        // 檢查是否為參加抽獎的關鍵字
        Sweepstake sweepstake = sweepstakeMap.get(originalMessageText);

        // 有相同關鍵字的抽獎活動
        if (sweepstake != null) {
          String userId = event.getSource().getUserId();
          String senderId = event.getSource().getSenderId();

          if (sweepstake.isValid(senderId, userId)) {
            LineUser user = LineBot.getInstance().getLineUser(senderId, userId, true);

            sweepstake.addUser(user);
            return new TextMessage(
                user.getDisplayName() + "參加抽獎活動\"" + sweepstake.getName() + "\"成功");
          }
        }

        return null;
      }

      FunctionThrowable<MessageEvent<TextMessageContent>, Message> action = map.get(botEnum);
      // 符合的指令
      return action.apply(event);
    } catch (Exception e) {
      e.printStackTrace();
      return new TextMessage("出錯了，救救我~" + e);
    }
  }

  /**
   * 用抽獎活動名稱取得活動物件.
   *
   * @param sweepstakesName 抽獎活動名稱
   * @return
   */
  public Optional<Sweepstake> getSweepstake(String sweepstakesName) {
    return sweepstakeMap
        .values()
        .stream()
        .filter(obj -> obj.getName().equals(sweepstakesName))
        .findFirst();
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
