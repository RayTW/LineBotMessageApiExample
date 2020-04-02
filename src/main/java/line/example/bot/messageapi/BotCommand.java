package line.example.bot.messageapi;

import java.util.StringJoiner;

public enum BotCommand {
  HELP("全部Bot指令", "/help", "/??", "/幫助"),
  ECHO("回應相同訊息, /echo [訊息] ex: /echo 我是訊息", "/echo"),
  USER_ID("回應用戶ID,ex: /userid", "/userid", "/用戶"),
  ME("回應用戶個人資訊,ex: /我", "/me", "/我"),
  LUCKYDRAW("抽獎活動, /抽獎 [抽獎活動名稱] [留言] ex: /抽獎 抽iphone手機 大吉大利", "/luckydraw", "/抽獎"),
  LUCKYDRAW_FINISH("抽獎活動開獎, /開獎 [抽獎活動名稱] [要選出的抽獎人數] ex: /開獎 抽iphone手機 3", "/lucky", "/開獎"),
  LUCKYDRAW_STATUS("抽獎活動狀態, /抽獎狀態 [抽獎活動名稱] ex: /抽獎狀態 抽iphone手機", "/luckydrawstatus", "/抽獎狀態"),
  LUCKY_ALL("顯示全部抽獎活動, ex: /luckyall", "/luckyall"),
  GUESS_BEGIN("建立1A2B遊戲,ex: /ab [玩家猜測次數限制](預設100次，可不帶參數) ex: /ab 100", "/ab"),
  GUESS_FINISH("結束1A2B遊戲並顯示答案, ex: /ba", "/ba"),
  CODA_BEGIN("建立終極密碼遊戲,ex: /coda [最大範圍數](選填)", "/coda"),
  CODA_FINISH("結束終極密碼遊戲並顯示答案, ex: /adoc", "/adoc");

  private final String[] commands;
  private final String detail;

  private BotCommand(String detail, String... commands) {
    this.commands = commands;
    this.detail = detail;
  }

  public String[] getCommands() {
    return commands;
  }

  public String getDetai() {
    return detail;
  }

  /**
   * 取得指定command對應enum物件.
   *
   * @param command line bot接收的指令
   * @return
   */
  public static BotCommand getBotCommand(String command) {
    if (command == null) {
      throw new NullPointerException("command is null");
    }

    for (BotCommand botCommand : values()) {
      for (String cmd : botCommand.commands) {
        if (cmd.equals(command)) {
          return botCommand;
        }
      }
    }
    return null;
  }

  /**
   * 取得全部command及說明.
   *
   * @return
   */
  public static String getCommandsDetail() {
    StringBuilder detail = new StringBuilder();

    for (BotCommand botCommand : values()) {
      StringJoiner cmds = new StringJoiner("、");
      for (String cmd : botCommand.commands) {
        cmds.add(cmd);
      }
      detail.append(cmds);
      detail.append("  ==> ");
      detail.append(botCommand.getDetai());
      detail.append(System.lineSeparator());
    }
    return detail.toString();
  }
}
