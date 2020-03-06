package line.example.bot.messageapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BotCommandTest {

  @Test
  public void testEnumOfUsingCommandEcho() {
    String originalMessageText = "/echo aa";
    final String command = originalMessageText.split(" ")[0];
    final BotCommand botEnum = BotCommand.getBotCommand(command);

    assertEquals(BotCommand.ECHO, botEnum);
  }

  @Test
  public void testEnumOfUsingCommandSweepstakes() {
    String originalMessageText = "/抽獎 aa";
    final String command = originalMessageText.split(" ")[0];
    final BotCommand botEnum = BotCommand.getBotCommand(command);

    assertEquals(BotCommand.LUCKYDRAW, botEnum);
  }

  @Test
  public void testGetCommandsDetailNotNull() {
    String r = BotCommand.getCommandsDetail();
    System.out.println(r);
    assertNotNull(r);
  }
}
