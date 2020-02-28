package line.example.bot.messageapi;

import com.linecorp.bot.model.profile.UserProfileResponse;
import java.net.URI;
import java.util.Objects;

public class LineUser {
  private String id;
  private String displnName;
  private URI pictureUrl;

  /**
   * 建立line用戶物件.
   *
   * @param response response
   */
  public LineUser(UserProfileResponse response) {
    this.id = response.getUserId();
    this.displnName = response.getDisplayName();
    this.pictureUrl = response.getPictureUrl();
  }

  /**
   * 建立line用戶物件.
   *
   * @param id user id
   * @param displnName 名稱
   * @param pictureUrl 大頭照url
   */
  public LineUser(String id, String displnName, URI pictureUrl) {
    this.id = id;
    this.displnName = displnName;
    this.pictureUrl = pictureUrl;
  }

  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displnName;
  }

  public URI getPictureUrl() {
    return pictureUrl;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LineUser) {
      LineUser user = (LineUser) obj;

      return user.id.equals(id) && user.displnName.equals(displnName);
    }
    return false;
  }

  public long hashcode() {
    return Objects.hash(id, displnName);
  }
}
