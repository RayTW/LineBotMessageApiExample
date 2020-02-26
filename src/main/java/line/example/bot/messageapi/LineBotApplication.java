package line.example.bot.messageapi;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
public class LineBotApplication {

  /**
   * BOT機器人接收訊息.
   *
   * @param event 訊息事件
   * @return
   */
  @EventMapping
  public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    final String originalMessageText = event.getMessage().getText();
    if (originalMessageText.startsWith("/echo")) {
      return new TextMessage("echo:" + originalMessageText);
    } else if (originalMessageText.startsWith("/傳說")) {
      return new TextMessage("傳說最新訊息: https://moba.garena.tw/news");
    } else if (originalMessageText.startsWith("/user")) {
      final String userId = event.getSource().getUserId();
      final StringBuilder txt = new StringBuilder();
      final String newLine = "%0D%0A";
      LineBot.getInstance()
          .getInfo(userId)
          .ifPresent(
              info -> {
                txt.append("使用者名稱:");
                txt.append(info.getDisplayName());
                txt.append(newLine);
                txt.append("使用者狀態:");
                txt.append(info.getStatusMessage());
              });
      return new TextMessage(txt.toString());
    }
    return null;
  }

  @EventMapping
  public void handleDefaultMessageEvent(Event event) {
    System.out.println("event: " + event);
  }

  public static void main(String[] args) {
    System.out.println("args=>" + args.length);
    SpringApplication.run(LineBotApplication.class, args);
  }
}
