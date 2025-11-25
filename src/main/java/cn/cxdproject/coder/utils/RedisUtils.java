package cn.cxdproject.coder.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.K;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis工具类, Redis 是一款由C语言开发的开源的NoSQL数据库，基于Key-Value进行存储，由于其存储数据是在内存中的
 * 因此Redis的读取性能较高，常用于分布式存储，当然也用在包括消息队列，延迟队列，分布式锁等场景中
 *
 * Redis速度非常快的原因:
 * 1. Redis是单线程的，没有线程之间的切换和加锁导致的性能损耗
 * 2. Redis基于IO多路复用，单线程同时监听了多个Socket，提高了效率
 * 3. Redis是用C语言开发的，其效率比起其他语言开发的还是要快很多
 * 4. Redis基于RESP这种简单的二进制协议进行传输数据，速度自然很快
 * 5. Redis具有多种优化过后的数据结构，比如String类型的简单动态字符串，比如跳表和压缩列表等
 * 6. Redis基于内存进行存储，比起磁盘存储要快得多
 * 7. Redis客户端具有pipeline提高效率
 *
 * 使用Redis可能造成的问题
 * 1. 缓存可能导致数据不一致性
 * 2. 缓存可能导致数据丢失
 * 3. 并发竞争问题
 * 4. 缓存雪崩，缓存击穿，缓存穿透
 *
 * 注意： 这里一般不用Redis作为主数据库，因为Redis是基于内存进行存储的，而内存的成本是很高的
 * 并且Redis也是有一定的丢失数据的风险
 *
 * 本系统使用Redis的原因:
 * 1. 高并发，提高整体系统的并发性，
 * 2. 高性能，提高系统的整体性能，优化访问速度
 * 3. 功能丰富，针对包括验证码过期时间等特殊场景需要用到
 */
@Slf4j
@Component
public class RedisUtils {

    /**
     * RedisTemplate操作
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * json处理器
     */
    private final ObjectMapper objectMapper;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Redis的核心数据存储和读取，set和get都是单线程操作的，也因此其没有由于线程之间的切换导致的性能损耗
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 存储Redis常用数据类型之String类型，其余的还包括List，Set，ZSet，Hash，以及特殊数据结构:
     * 1. Bitmap, 2. GEO, 3. Stream, 4. HyperLogLog
     */
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        try  {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, timeout, unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置缓存
     */
    public void set(String key, Object value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存
     */
    public void set(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取值
     *
     * @param key 键
     * @param clazz 返回的类型
     * @return 值
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }

        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        return objectMapper.convertValue(value, clazz);
    }

    //批量查询key
    public List<Object> multiGet(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 删除缓存，如果有大量的key同时过期的话可能会导致出现问题，包括会导致大量的请求打过来导致缓存雪崩问题
     * 这里可以添加Random随机过期时间的方式让大量的key无法统一时间过期，接着就是可以进行集群操作，或者使用多级缓存的方式
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * lazy-free 惰性释放， 这里通过异步删除的方式进行处理key，这里一般用来处理bigkey，解决那种大key，防止在客户端操作导致阻塞工作线程
     *
     * big key的产生原因:
     * 1. 业务规模预估不当
     * 2. 程序设计错误
     * 3. 使用了错误的数据结构
     * 4. 缓存没有清理，比如hash这种数据类型里面存了太多的空数据
     *
     * big key 的危害:
     * 1. 会阻塞工作线程
     * 2. 会影响查询效率
     * 3. 会阻塞客户端，并且对于网络带宽是灾难级影响
     * 4. 会影响扩容
     * 5. 会导致内存OOM
     *
     * 解决方案:
     * 1. 更换数据结构
     * 2. lazy-free
     * 3. 拆分bigkey
     * 4. 压缩bigkey
     * 5. 及时清理hash中的
     * 6. 引入集群分担压力
     *
     * 如何发现bigkey:
     * 1. 使用bigkey参数查找
     * 2. 使用scan命令查找
     * 3. 使用公有云服务
     * 4. 使用开源项目去分析RDB文件
     */
    public Boolean delLazyFree(String key){
        return redisTemplate.unlink(key);
    }

    /**
     * 判断key是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long time) {
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, Duration duration){
        return redisTemplate.expire(key, duration);
    }

    /**
     * 获取过期时间, 此处的过期时间实际上是Redis的在底层维护了一个过期字典，
     * 从而来判断Redis中的key是否过期，也就是一个哈希表，key存储的是一样的，value存储的是一个long long类型的时间戳
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 自增操作
     * @param key 键
     * @param delta 增量（通常为1，负数表示减少）
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 设置值，如果 key 不存在则设置成功（分布式锁的典型用法）
     */
    public void setIfAbsent(String key, String value) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 设置值，如果 key 不存在则设置成功（分布式锁的典型用法）
     */
    public void setIfAbsent(String key, Object value) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 设置值，如果 key 不存在则设置成功（分布式锁的典型用法）
     */
    public Boolean setIfAbsent(String key, String value, long time) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
        return result != null && result;
    }

    /**
     * 设置值，如果 key 不存在则设置成功（分布式锁的典型用法）
     */
    public Boolean setIfAbsent(String key, String value, long time, TimeUnit minutes) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, time, minutes);
        return result != null && result;
    }

    /**
     * 获取匹配的所有键
     * @param pattern 匹配模式（如 "prefix:*"）
     * @return 匹配的键集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 添加或更新用户分数
     * @param ranking 排行榜名
     * @param user 用户名
     * @param score 分数
     */
    public void addScore(String ranking, String user, double score) {
        redisTemplate.opsForZSet().add(ranking, user, score);
    }

    /**
     * 增加用户分数
     * @param ranking 排行榜名
     * @param user 用户名
     * @param increment 分数
     */
    public void incrementScore(String ranking, String user, double increment) {
        redisTemplate.opsForZSet().incrementScore(ranking, user, increment);
    }

    /**
     * 获取前N名
     * @param ranking 排行榜名
     * @param n 第n
     */
    public Set<Object> getTopN(String ranking, int n) {
        return redisTemplate.opsForZSet().reverseRange(ranking, 0, n - 1);
    }

    /**
     * 获取用户排名
     * @param ranking 排行榜名
     * @param user 用户名
     */
    public Long getUserRank(String ranking, String user) {
        return redisTemplate.opsForZSet().reverseRank(ranking, user);
    }

    /**
     * 获取用户分数
     * @param ranking 排行榜名
     * @param user 用户名
     */
    public Double getRankingScore(String ranking, String user) {
        return redisTemplate.opsForZSet().score(ranking, user);
    }

    // 队列操作

    public void push(String queueName, Object value) {
        redisTemplate.opsForList().rightPush(queueName, value);
    }

    public Object pop(String queueName) {
        return redisTemplate.opsForList().leftPop(queueName);
    }

    public Object peek(String queueName) {
        return redisTemplate.opsForList().index(queueName, 0);
    }

    public long size(String queueName) {
        return redisTemplate.opsForList().size(queueName);
    }

    public void clear(String queueName) {
        redisTemplate.delete(queueName);
    }

    public List<String> multiGet(List<String> redisKeys) {
        List<Object> results = redisTemplate.opsForValue().multiGet(redisKeys);
        // 类型安全转换
        assert results != null;
        return results.stream()
                .map(obj -> obj != null ? obj.toString() : null)
                .collect(Collectors.toList());
    }

    public Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisConnection connection) -> {
            Set<String> keys = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(1000).build());
            try {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } finally {
                try {
                    cursor.close();
                } catch (IOException e) {
                    log.info("关闭游标时发生异常");
                }
            }
            return keys;
        });
    }

    public void setAttendance(long userId, int dayIndex, boolean isCheckedIn) {
        String key = "user:" + userId + ":attendance";
        // 设置用户打卡状态：1 表示打卡，0 表示未打卡
        redisTemplate.opsForValue().setBit(key, dayIndex, isCheckedIn);
    }

    public boolean getAttendance(long userId, int dayIndex) {
        String key = "user:" + userId + ":attendance";
        // 查询用户在指定日期的打卡状态
        return Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, dayIndex));
    }

    public int calculateConsecutiveDays(long userId) {
        String key = "user:" + userId + ":attendance";
        int consecutiveDays = 0;

        // 获取今天的日期，作为起点
        LocalDate currentDate = LocalDate.now();
        int currentDayIndex = currentDate.getDayOfYear();  // 获取当天在本年度中的索引

        // 从当前日期开始，逐天向前检查
        for (int i = currentDayIndex - 1; i >= 0; i--) {
            boolean hasCheckedIn = redisTemplate.opsForValue().getBit(key, i);
            if (hasCheckedIn) {
                consecutiveDays++;
            } else {
                break;  // 如果遇到未打卡的天数，结束连续打卡计算
            }
        }
        return consecutiveDays;
    }

    /**
     * 拓展:
     * 缓存穿透： 主要是由于缓存和数据库中都没有要查询的数据，就会导致每次都去查询数据库，如果有大量这样的请求就会导致
     * 出现数据库宕机的问题
     * 主要解决方案：
     * 1. 简单处理方法，使用缓存空数据的方法
     * 2. 使用系统处理方式，使用布隆过滤器的方案
     * 3. 增加权限校验和参数复杂度，让攻击者无法容易的猜到数据
     * 4. 进行ip，用户，接口级别的限流，包括使用ip黑名单等封禁方式
     *
     * Redis的持久化方案：
     * RDB数据快照，使用save和bgsave的命令，异步的执行生成数据快照
     * AOF增量日志，命令追加的方式，可以通过重放命令实现恢复
     *
     * 在什么场景下你会选择 RDB 持久化，在什么场景下会选择 AOF 持久化？
     * 一般来说对于需要快速恢复，然后数据完整性要求不那么强的可以用RDB持久化，
     * 对于一定需要数据完整性的，用AOF持久化方案
     *
     * 主从同步：
     * 单个Redis节点存储数据的能力是有限的，如果单Redis节点达到性能瓶颈，可以引入Redis集群的方式，一般主要是主从集群
     * 哨兵集群和分片集群的这几种方案
     * 如果是主从集群的话主要是分为一个Redis主节点和几个Redis从节点，一般是三个的样子，一般是主节点用来存储数据，从节点用来读取数据，他们之间
     * 会有一个同步机制，主要是全量同步和增量同步，实现主从节点之间的数据同步
     */
}
