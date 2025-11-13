package cn.cxdproject.coder.mapper;


import cn.cxdproject.coder.model.entity.Admin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Admin Mapper 接口
 * @author Hibiscus-code-generate
 *
 * MySQL的Change Buffer是什么？它有什么作用？
 * ChangeBuffer是MySQL的一个机制，用来暂存对二级索引的查询和变更操作，当页被读取或者Flush的时候才会将这些变更写入二级索引中
 * 通过这种方式提高写入性能和进行批处理优化
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据用户名检查管理员是否存在
     *
     * 了解过索引吗，什么是索引？
     *
     * 索引是一种优化查询速度的高效的数据结构，提高检索效率，降低CPU损耗MySQL的InnoDB引擎的默认索引就是B+树索引的方式
     */
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     *
     * 什么是聚簇索引，什么是非聚簇索引呢？什么是回表查询？
     *
     * 聚簇索引就是行数据和索引放一块，叶子节点存放完整的行数据
     * 非聚簇索引就是数据和索引分开存储，叶子节点存放的是行数据对应的主键
     * 回表查询就是非集簇索引在查询了数据之后获取到了行数据对应的主键值，然后回进行一次回表操作，
     * 也就是使用这个主键值去查询得到完整的行数据
     */
    boolean existsByEmail(@Param("email") String email);

    /**
     * 根据邮箱查询管理员
     *
     * 为什么聚簇索引查询速度快?
     * 因为聚簇索引的叶子节点就是存放的完整的行数据了，不需要回表查询，一次查询就可以得到需要的结果
     *
     * 没有创建主键怎么办?
     * 没有创建主键的话，聚簇索引会由第一个唯一索引充当，如果第一个唯一索引也没有，那么就会有一个隐藏字段，db_row_id进行作为聚簇索引
     */
    Admin selectByEmail(String email);

    /**
     * 根据用户名查询管理员
     *
     * 我们应该如何提升查询效率呢?
     * 我们可以尽量减少回表查询，使用覆盖索引和索引下推的方式进行优化
     *
     * 什么是索引下推？
     * 索引下推就是联合索引在本身数据就有的情况下,
     * 直接通过联合索引再进行一次数据的过滤,而不是通过回表返回到server层进行数据的过滤。
     */
    Admin selectByUsername(@Param("username") String username);

    /**
     * 根据id查询管理员
     *
     * MySQL的索引类型有哪些？
     * 包括有B+树索引，哈希索引，R-树索引，倒排索引，
     * 根据InnoDB引擎这一块又可以分为聚簇索引和非聚簇索引
     * 根据索引的性质可以分为
     * 普通索引，联合索引，主键索引，唯一索引，空间索引，全文索引
     */
    @Select("select * from admin where id = #{adminId}")
    Admin findAdminById(Long adminId);
}