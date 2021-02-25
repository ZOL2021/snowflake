package com.zlbteam;


/**
 * @author zhoulibin
 */
public class Snowflake {

    /** 起始时间 2021-01-01 00:00:00 */
    private static final long START_TIME = 1609430400000L;
    /** 数据中心ID所占长度 */
    private static final long DATA_LEN = 5L;
    /** 机器ID所占长度 */
    private static final long WORK_LEN = 5L;
    /** 毫秒内序列所占长度 */
    private static final long SEQ_LEN = 12L;
    /** 时间部分左移位数 */
    private static final long TIME_LEFT_BIT = DATA_LEN + WORK_LEN + SEQ_LEN;
    /** 数据中心id最大值 31 */
    private static final long DATA_MAX_NUM = ~(-1 << DATA_LEN);
    /** 机器id最大值 31 */
    private static final long WORK_MAX_NUM = ~(-1 << WORK_LEN);
    /** 毫秒内序列最大值 4095 */
    private static final long SEQ_MAX_NUM = ~(-1 << SEQ_LEN);
    /** 上次生成ID的时间截 */
    private static long lastTimestamp = -1L;
    /** 毫秒内序列(0~4095) */
    private static long sequence = 0L;
    /** 数据中心id（可以手动定义 0-31之间的数） */
    private static long dataId = getDataId();
    /** 机器id（可以手动定义 0-31之间的数） */
    private static long workId = getWorkId();

    private Snowflake(){}

    // ==============================Methods==========================================

    /** 获取雪花算法ID */
    public static synchronized Long snowflake() {
        long timestamp = currentTimeMillis();
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if(timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate the id for %d milliseconds", lastTimestamp - timestamp));
        }
        // 如果是同一时间生成的，则进行毫秒内序列
        if(timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQ_MAX_NUM;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = nextMillis(lastTimestamp);
            }
        }else {
            sequence = 0L;
        }
        // 上次生成ID的时间截
        lastTimestamp = timestamp;
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIME) << TIME_LEFT_BIT)
                | (dataId << DATA_LEN)
                | (workId << WORK_LEN)
                | sequence;
    }

    /** 系统毫秒数 */
    private static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /** 系统下一毫秒 */
    private static long nextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    /** 数据中心号 */
    private static long getDataId() {
        if (dataId > DATA_MAX_NUM || dataId < 0) {
            throw new IllegalArgumentException(String.format("dataId can't be greater than %d or less than 0", DATA_MAX_NUM));
        }
        return 0;
    }

    /** 机器号 */
    private static long getWorkId() {
        if (workId > WORK_MAX_NUM || workId < 0) {
            throw new IllegalArgumentException(String.format("workId can't be greater than %d or less than 0", WORK_MAX_NUM));
        }
        return 0;
    }

}
