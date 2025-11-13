package cn.cxdproject.coder.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;

import java.util.Collection;

/**
 * 布隆过滤器: 系统性解决缓存穿透问题的方法
 * 布隆过滤器可以理解为是一个巨大的数组，他是通过将key进行三次哈希运算然后对长度取模从而定位一个key的位置
 * 通过这种方式进行存储一个数据是否存在，后续需要验证一个数据是否在容器中的话就可以用这种方式判断容器中的元素，实现快速判断
 * 从而就可以过滤掉没有的数据
 */
public class BloomFilterConfig {

    private static final int EXPECTED_INSERTIONS = 50000; // 预期插入的数量
    private static final double FPP = 0.01; // 误判率

    private static final Funnel<Long> FUNNEL = (from, into) -> into.putLong(from);

    private static final int numBits = (int) (-EXPECTED_INSERTIONS * Math.log(FPP) / (Math.log(2) * Math.log(2)));

    private static final int numHashFunctions = (int) (numBits * Math.log(2) / EXPECTED_INSERTIONS);

    private static final BloomFilter<Long> bloomFilter = BloomFilter.create(FUNNEL, EXPECTED_INSERTIONS, FPP);

    /**
     * 判断是否可能存在某个 id
     * @param id 对象id
     * @return 是都存在该id
     */
    public static boolean mightContain(long id) {
        return bloomFilter.mightContain(id);
    }

    /**
     * 插入单个 id 到布隆过滤器
     * @param id 对象id
     */
    public static void put(long id) {
        bloomFilter.put(id);
    }

    /**
     * 批量插入多个 id 到布隆过滤器
     * @param ids 对象id集合
     */
    public static void putAll(Collection<Long> ids) {
        for (Long id : ids) {
            bloomFilter.put(id);  // 遍历并逐个插入
        }
    }
}