package com.xiaowang.xwpicturebackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisStringTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testSetAndGet() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 测试设置和获取值
        String key = "test:key:1";
        String value = "hello redis";

        ops.set(key, value);
        String result = ops.get(key);

        assertEquals(value, result);
        System.out.println("设置和获取测试成功: " + result);
    }

    @Test
    public void testSetWithExpire() throws InterruptedException {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 测试设置过期时间
        String key = "test:key:expire";
        String value = "this will expire";

        ops.set(key, value, 2, TimeUnit.SECONDS);

        // 立即获取应该存在
        String result = ops.get(key);
        assertEquals(value, result);
        System.out.println("设置后立即获取: " + result);

        // 等待3秒后应该过期
        Thread.sleep(3000);
        String expiredResult = ops.get(key);
        assertNull(expiredResult);
        System.out.println("过期后获取: " + expiredResult);
    }

    @Test
    public void testSetIfAbsent() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        String key = "test:key:absent";

        // 第一次设置应该成功
        boolean firstSet = ops.setIfAbsent(key, "first value");
        assertTrue(firstSet);

        // 第二次设置应该失败（因为key已经存在）
        boolean secondSet = ops.setIfAbsent(key, "second value");
        assertFalse(secondSet);

        // 验证值仍然是第一次设置的值
        String result = ops.get(key);
        assertEquals("first value", result);
        System.out.println("SETNX测试成功: " + result);
    }

    @Test
    public void testIncrementAndDecrement() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        String key = "test:key:counter";

        // 设置初始值
        ops.set(key, "10");

        // 递增操作
        Long incremented = ops.increment(key);
        assertEquals(11L, incremented);
        System.out.println("递增后: " + incremented);

        // 递减操作
        Long decremented = ops.decrement(key);
        assertEquals(10L, decremented);
        System.out.println("递减后: " + decremented);

        // 指定步长递增
        Long incrementedBy5 = ops.increment(key, 5);
        assertEquals(15L, incrementedBy5);
        System.out.println("递增5后: " + incrementedBy5);

        // 指定步长递减
        Long decrementedBy3 = ops.decrement(key, 3);
        assertEquals(12L, decrementedBy3);
        System.out.println("递减3后: " + decrementedBy3);
    }

    @Test
    public void testAppend() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        String key = "test:key:append";

        // 设置初始值
        ops.set(key, "hello");

        // 追加字符串
        Integer newLength = ops.append(key, " world");
        assertEquals(11, newLength);

        String result = ops.get(key);
        assertEquals("hello world", result);
        System.out.println("追加操作成功: " + result);
    }

    @Test
    public void testGetAndSet() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        String key = "test:key:getset";

        // 设置初始值
        ops.set(key, "old value");

        // 获取并设置新值
        String oldValue = ops.getAndSet(key, "new value");
        assertEquals("old value", oldValue);

        String newValue = ops.get(key);
        assertEquals("new value", newValue);
        System.out.println("GETSET测试成功 - 旧值: " + oldValue + ", 新值: " + newValue);
    }

    @Test
    public void testDelete() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        String key = "test:key:delete";

        // 设置值
        ops.set(key, "to be deleted");

        // 验证值存在
        assertNotNull(ops.get(key));

        // 删除key
        Boolean deleted = stringRedisTemplate.delete(key);
        assertTrue(deleted);

        // 验证值已删除
        assertNull(ops.get(key));
        System.out.println("删除操作成功");
    }

    @Test
    public void testExists() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        String key1 = "test:key:exists:1";
        String key2 = "test:key:exists:2";

        // 设置第一个key
        ops.set(key1, "value1");

        // 检查key是否存在
        Boolean exists1 = stringRedisTemplate.hasKey(key1);
        Boolean exists2 = stringRedisTemplate.hasKey(key2);

        assertTrue(exists1);
        assertFalse(exists2);
        System.out.println("存在性检查测试成功 - key1存在: " + exists1 + ", key2存在: " + exists2);
    }

    @Test
    public void testMultipleOperations() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 批量设置多个key
        String key1 = "test:multi:key1";
        String key2 = "test:multi:key2";
        String key3 = "test:multi:key3";

        ops.set(key1, "value1");
        ops.set(key2, "value2", 10, TimeUnit.SECONDS);
        ops.set(key3, "value3");

        // 验证所有key的值
        assertEquals("value1", ops.get(key1));
        assertEquals("value2", ops.get(key2));
        assertEquals("value3", ops.get(key3));

        // 批量删除
        Long deletedCount = stringRedisTemplate.delete(java.util.Arrays.asList(key1, key2, key3));
        assertEquals(3L, deletedCount);

        System.out.println("批量操作测试成功，删除了 " + deletedCount + " 个key");
    }
}