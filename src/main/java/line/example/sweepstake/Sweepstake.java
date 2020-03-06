package line.example.sweepstake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import line.example.bot.messageapi.LineUser;

/**
 * 抽獎活動.
 *
 * @author ray
 */
public class Sweepstake {
  private String name;
  private String keyword;
  private String senderId;
  private LineUser organizerUser;
  private Set<LineUser> userList;

  /**
   * 建立抽獎活動.
   *
   * @param senderId 抽獎活動房間id
   * @param name 抽獎活動名稱
   * @param keyword 加入抽獎的留言指定關鍵字
   * @param organizerUser 抽獎活動舉辦人
   */
  public Sweepstake(String senderId, String name, String keyword, LineUser organizerUser) {
    userList = Collections.synchronizedSet(new LinkedHashSet<>());
    this.senderId = senderId;
    this.name = name;
    this.keyword = keyword;
    this.organizerUser = organizerUser;
  }

  /**
   * 取得抽獎活動名稱.
   *
   * @return
   */
  public String getName() {
    return name;
  }

  public String getKeyword() {
    return keyword;
  }

  /**
   * 用戶是否符合參加目前的活動.
   *
   * @param senderId 抽獎活動群id/房間id
   * @param userId 抽獎用戶userId
   * @return
   */
  public boolean isValid(String senderId, String userId) {
    if (this.senderId.equals(senderId)) {
      return true;
    }
    return false;
  }

  /**
   * 將用戶加入抽獎者等候區.
   *
   * @param user 抽獎用戶user
   * @return
   */
  public void addUser(LineUser user) {
    userList.add(user);
  }

  /**
   * 抽出中獎者.
   *
   * @param count 中獎人數
   * @return
   */
  public List<LineUser> getLuckyUser(int count) {
    ArrayList<LineUser> allUser = new ArrayList<>(userList);
    ArrayList<LineUser> luckyUserList = new ArrayList<>();

    if (count > allUser.size()) {
      count = allUser.size();
    }
    for (int i = 0; i < count; i++) {
      luckyUserList.add(allUser.remove(ThreadLocalRandom.current().nextInt(0, allUser.size())));
    }

    return luckyUserList;
  }

  /**
   * 取得參加抽獎人數.
   *
   * @return
   */
  public int getUserSize() {
    return userList.size();
  }

  /** 取得活動關係資訊. */
  @Override
  public String toString() {
    StringJoiner info = new StringJoiner(System.lineSeparator());

    info.add("活動名稱:" + name);
    info.add("活動舉辦人:" + organizerUser.getDisplayName());
    info.add("活動關鍵字:" + keyword);
    info.add("活動參加人數:" + userList.size());

    return info.toString();
  }
}
