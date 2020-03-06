package line.example.bot.messageapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TextMessage;
import java.time.Instant;
import org.junit.Test;

public class LineBotApplicationTest {
  @Test
  public void testActionUsingCommandEcho() {
    LineBotApplication obj = new LineBotApplication();
    MessageEvent<TextMessageContent> event =
        new MessageEvent<>(
            "replyToken",
            new UserSource("U1234"),
            new TextMessageContent("122", "訊息"),
            Instant.now());

    try {
      TextMessage msg = (TextMessage) obj.getActionMap().get(BotCommand.ECHO).apply(event);

      assertEquals("訊息", msg.getText());
      System.out.println(msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testHandleTextMessageUseingNormalMessage() {
    LineBotApplication obj = new LineBotApplication();
    MessageEvent<TextMessageContent> event =
        new MessageEvent<>(
            "replyToken",
            new UserSource("U1234"),
            new TextMessageContent("122", "訊息"),
            Instant.now());

    try {
      TextMessage msg = (TextMessage) obj.handleTextMessageEvent(event);

      assertNull(msg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
