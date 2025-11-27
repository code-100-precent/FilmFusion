package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * User 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_users")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 管理员ID
     */
    @TableId
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 电话号码
     */
    @TableField("phoneNumber")
    private String phoneNumber;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 角色
     */
    @TableField("role")
    private String role;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled;

    @TableField("thumb_avatar")
    private String thumbAvatar;


    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone User object", e);
        }
    }
}
