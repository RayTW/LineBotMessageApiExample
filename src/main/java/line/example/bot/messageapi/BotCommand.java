package line.example.bot.messageapi;

import java.util.StringJoiner;

public enum BotCommand {
  HELP("全部Bot指令", "/help", "/?", "/幫助"),
  ECHO("回應相同訊息,ex: /echo 訊息", "/echo"),
  USER_ID("回應用戶ID,ex: /userid", "/userid", "/用戶"),
  ME("回應用戶個人資訊,ex: /我", "/me", "/我"),
  SWEEPSTAKE("啟動抽獎活動,ex: /抽獎 留言抽", "/sweepstakes", "/抽獎");

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
  public static BotCommand enumOf(String command) {
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
    throw new IllegalArgumentException("No enum constant " + command);
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
