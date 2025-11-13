package cn.cxdproject.coder.utils;

/**
 * A simple implementation of the Snowflake ID generator.
 * <p>
 * This class generates 64-bit unique IDs based on the current timestamp,
 * data center ID, machine ID, and a sequence number.
 * <p>
 * Structure of the ID (from high to low bits):
 * <pre>
 * 0 - 41 bits timestamp - 5 bits data center ID - 5 bits machine ID - 12 bits sequence
 * </pre>
 * Inspired by Twitter's Snowflake algorithm.
 */
public class SnowflakeIdGenerator {

    /** Custom epoch timestamp (e.g., project start time) */
    private static final long START_TIMESTAMP = 1625155600000L;

    /** Number of bits allocated for machine ID */
    private static final long MACHINE_ID_BITS = 5L;

    /** Number of bits allocated for data center ID */
    private static final long DATA_CENTER_ID_BITS = 5L;

    /** Number of bits allocated for sequence number */
    private static final long SEQUENCE_BITS = 12L;

    /** Maximum values for machine and data center IDs */
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /** Bit shifts */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATA_CENTER_ID_BITS;
    private static final long MACHINE_ID_LEFT_SHIFT = SEQUENCE_BITS + DATA_CENTER_ID_BITS;
    private static final long DATA_CENTER_ID_LEFT_SHIFT = SEQUENCE_BITS;

    /** Sequence and timestamp trackers */
    private static long lastTimestamp = -1L;
    private static long sequence = 0L;

    /** Machine and data center identifiers (could be loaded from config) */
    private static final long MACHINE_ID = 1L;
    private static final long DATA_CENTER_ID = 1L;

    static {
        if (MACHINE_ID > MAX_MACHINE_ID || DATA_CENTER_ID > MAX_DATA_CENTER_ID) {
            throw new IllegalArgumentException("Machine ID or Data Center ID is out of valid range.");
        }
    }

    /**
     * Generates the next unique ID.
     * This method is synchronized to ensure thread safety.
     *
     * @return a globally unique 64-bit ID
     */
    public static synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID for " + (lastTimestamp - timestamp) + "ms");
        }

        if (timestamp == lastTimestamp) {
            // Within the same millisecond, increment sequence
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // Sequence overflow in same millisecond, wait for next millisecond
                timestamp = waitUntilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L; // Reset sequence for a new millisecond
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (DATA_CENTER_ID << DATA_CENTER_ID_LEFT_SHIFT)
                | (MACHINE_ID << MACHINE_ID_LEFT_SHIFT)
                | sequence;
    }

    /**
     * Waits until the system clock moves to the next millisecond.
     *
     * @param lastTs the last recorded timestamp
     * @return the next millisecond timestamp
     */
    private static long waitUntilNextMillis(long lastTs) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTs) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
