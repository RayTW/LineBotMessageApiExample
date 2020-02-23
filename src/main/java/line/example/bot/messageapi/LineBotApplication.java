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

  @EventMapping
  public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
    final String originalMessageText = event.getMessage().getText();
    return new TextMessage("echo:" + originalMessageText);
  }

  @EventMapping
  public void handleDefaultMessageEvent(Event event) {
    System.out.println("event: " + event);
  }

  public static void main(String[] args) {
    SpringApplication.run(LineBotApplication.class, args);
  }
}
