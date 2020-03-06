package line.example.sweepstake;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import line.example.bot.messageapi.LineBot;
import line.example.bot.messageapi.LineUser;
import line.example.sweepstake.Sweepstake;
import line.example.sweepstake.SweepstakeManager;

public class SweepstakeManager {
  private ConcurrentHashMap<String, CopyOnWriteArrayList<Sweepstake>>
      sweepstakeMap; // k=senderId,v=活動物件
  private ReentrantLock lock;

  public SweepstakeManager() {
    sweepstakeMap = new ConcurrentHashMap<>();
    lock = new ReentrantLock();
  }

  /**
   * 創建抽獎活動.
   *
   * @param event 訊息事件
   * @return 回覆的訊息
   * @throws Exception 任何例外錯誤
   */
  public Message luckyDraw(MessageEvent<TextMessageContent> event) throws Exception {
    String[] args = event.getMessage().getText().split(" ");
    String sweepstakesName = args[1];
    String keyword = args[2];
    String userId = event.getSource().getUserId();
    String senderId = event.getSource().getSenderId();
    LineUser lineUser = LineBot.getInstance().getLineUser(senderId, userId, true);

    lock.lock();
    try {
      Optional<Sweepstake> sweepstakeRef =
          getSweepstake(
              senderId,
              obj -> (obj.getName().equals(sweepstakesName) || obj.getKeyword().equals(keyword)));

      if (sweepstakeRef.isPresent()) {
        return new TextMessage("活動名稱:\"" + sweepstakesName + "\"或關鍵字\" + keyword + \"被用囉");
      }

      CopyOnWriteArrayList<Sweepstake> list = new CopyOnWriteArrayList<>();
      Sweepstake sweepstake = new Sweepstake(senderId, sweepstakesName, keyword, lineUser);

      list.add(sweepstake);
      sweepstakeMap.put(senderId, list);
    } finally {
      lock.unlock();
    }

    StringBuilder txt = new StringBuilder();

    txt.append("活動名稱:");
    txt.append(sweepstakesName);
    txt.append(System.lineSeparator());
    txt.append("以下留言\"" + keyword + "\"即可參加抽獎喔");
    return new TextMessage(txt.toString());
  }

  /**
   * 查看抽獎活動.
   *
   * @param event 訊息事件
   * @return 回覆的訊息
   * @throws Exception 任何例外錯誤
   */
  public Message luckyStatus(MessageEvent<TextMessageContent> event) {
    String[] args = event.getMessage().getText().split(" ");
    String sweepstakesName = args[1];
    String senderId = event.getSource().getSenderId();

    Optional<Sweepstake> sweepstake =
        getSweepstake(senderId, obj -> obj.getName().equals(sweepstakesName));

    if (sweepstake.isPresent()) {
      return new TextMessage(sweepstake.get().toString());
    } else {
      return new TextMessage("查看抽獎活動結果，目前沒有\"" + sweepstakesName + "\"抽獎活動");
    }
  }

  /**
   * 取得全部抽獎活動資訊.
   *
   * @param event 訊息事件
   * @return 回覆的訊息
   * @throws Exception 任何例外錯誤
   */
  public Message luckyAll(MessageEvent<TextMessageContent> event) {
    CopyOnWriteArrayList<Sweepstake> list = sweepstakeMap.get(event.getSource().getSenderId());

    if (list != null && list.size() > 0) {
      String allSweepstakesName =
          list.stream()
              .map(o -> o.getName())
              .reduce((all, next) -> all.concat(System.lineSeparator()).concat(next))
              .get();
      return new TextMessage("全部活動:" + System.lineSeparator() + allSweepstakesName);
    } else {
      return new TextMessage("目前沒有抽獎活動");
    }
  }

  /**
   * 結束抽獎活動.
   *
   * @param event 訊息事件
   * @return 回覆的訊息
   * @throws Exception 任何例外錯誤
   */
  public Message luckyFinish(MessageEvent<TextMessageContent> event) {
    String[] args = event.getMessage().getText().split(" ");
    String senderId = event.getSource().getSenderId();
    String sweepstakesName = args[1];
    String luckyUserCount = args[2];

    Optional<Sweepstake> sweepstake =
        getSweepstake(senderId, obj -> obj.getName().equals(sweepstakesName));

    if (sweepstake.isPresent()) {
      final List<LineUser> lucky = sweepstake.get().getLuckyUser(Integer.parseInt(luckyUserCount));

      StringBuilder txt = new StringBuilder();

      txt.append("活動名稱:");
      txt.append(sweepstakesName);
      txt.append(System.lineSeparator());
      txt.append("參加人數:" + sweepstake.get().getUserSize());
      txt.append(System.lineSeparator());
      txt.append("抽出人數:" + lucky.size());
      txt.append(System.lineSeparator());
      txt.append("中獎者:");

      lucky.forEach(
          user -> {
            txt.append(user.getDisplayName());
            txt.append(System.lineSeparator());
          });

      lock.lock();
      try {
        // 抽完中獎者，移除活動
        sweepstakeMap.get(senderId).remove(sweepstake.get());
        sweepstakeMap.remove(sweepstake.get().getKeyword());
      } finally {
        lock.unlock();
      }

      return new TextMessage(txt.toString());
    } else {
      return new TextMessage("沒有\"" + sweepstakesName + "\"抽獎活動");
    }
  }

  /**
   * 用抽獎活動名稱或關鍵字取得活動物件.
   *
   * @param senderId group id/ room id
   * @param function argument 抽獎活動名稱, return關鍵字
   * @return
   */
  public Optional<Sweepstake> getSweepstake(
      String senderId, Function<Sweepstake, Boolean> function) {
    CopyOnWriteArrayList<Sweepstake> list = sweepstakeMap.get(senderId);

    if (list == null) {
      return Optional.ofNullable(null);
    }

    return list.stream().filter(function::apply).findFirst();
  }

  /**
   * 留言參加抽獎活動.
   *
   * @param event 訊息事件
   * @return 回覆的訊息
   * @throws Exception 任何例外錯誤
   */
  public Message joinLineUser(MessageEvent<TextMessageContent> event) throws Exception {
    String senderId = event.getSource().getSenderId();
    CopyOnWriteArrayList<Sweepstake> list = sweepstakeMap.get(senderId);

    if (list != null && list.size() > 0) {
      // 檢查是否為參加抽獎的關鍵字
      Optional<Sweepstake> sweepstake =
          list.stream()
              .filter(o -> o.getKeyword().equals(event.getMessage().getText()))
              .findFirst();

      // 有相同關鍵字的抽獎活動
      if (sweepstake.isPresent()) {
        String userId = event.getSource().getUserId();

        if (sweepstake.get().isValid(senderId, userId)) {
          LineUser user = LineBot.getInstance().getLineUser(senderId, userId, true);

          sweepstake.get().addUser(user);
          return new TextMessage(
              user.getDisplayName() + "參加抽獎活動\"" + sweepstake.get().getName() + "\"成功");
        }
      }
    }

    return null;
  }
}
