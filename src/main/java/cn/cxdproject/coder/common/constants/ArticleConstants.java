package cn.cxdproject.coder.common.constants;

import cn.cxdproject.coder.model.entity.Article;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;

import java.util.function.Predicate;

public interface ArticleConstants {
    String NOT_FIND = "不存在或已删除";
    String NO_PERMISSION = "权限不足";
    String ID = "id";
    String TITLE = "title";
    String ISSUE_NAME = "issue_unit";
    String ISSUE_TIME = "issue_time";
    String USER_ID = "user_id";
    String COVER = "cover";
    String DELETED = "deleted";
}
