package cn.cxdproject.coder.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 基类， 这里是将普遍的一些属性抽取了出来，一些数据库表中的必有字段，后续的类只需要集成这个Base基类即可拥有
 * 这些属性，从而能够快速构建新的类，这也就是OOP三大特性之一: 继承，可以理解为他的作用就是抽取，后续的类可以通过
 * 继承父类从而实现快速构建类
 *
 * @author heathcetide
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseEntity {
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    private Boolean deleted = false;
}