package line.example.bot.messageapi;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;

public class LineBot {
  private static LineBot instance = new LineBot();
  private LineMessagingClient lineMessagingClient;

  private LineBot() {
    lineMessagingClient =
        LineMessagingClient.builder(System.getenv("LINE_BOT_CHANNEL_TOKEN")).build();
  }

  public static LineBot getInstance() {
    return instance;
  }

  /**
   * 取得user資訊.
   *
   * @param roomIdOrGroupId 房id或群id
   * @param userId 用戶id
   * @return
   */
  public UserProfileResponse getInfo(String roomIdOrGroupId, String userId) throws Exception {
    UserProfileResponse ret = null;
    System.out.println("roomIdOrGroupId[" + roomIdOrGroupId + "],userId[" + userId + "]");

    try {
      ret = lineMessagingClient.getGroupMemberProfile(roomIdOrGroupId, userId).get();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (ret == null) {
      ret = lineMessagingClient.getRoomMemberProfile(roomIdOrGroupId, userId).get();
    }
    return ret;
  }

  /**
   * 發送文字訊息給指定用戶.
   *
   * @param channelToken 頻道token
   * @param userId line用戶id
   * @param text 訊息內容
   */
  public void sendMessage(String channelToken, String userId, String text) {
    lineMessagingClient = LineMessagingClient.builder(channelToken).build();

    Message message = new TextMessage(text);
    PushMessage push = new PushMessage(userId, message);

    try {
      BotApiResponse response =
          lineMessagingClient
              .pushMessage(push)
              .handleAsync(
                  (botApiResponse, exception) -> {
                    if (exception != null) {
                      System.out.println("execption=>" + exception);
                    }
                    return botApiResponse;
                  })
              .get();

      System.out.println(response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
