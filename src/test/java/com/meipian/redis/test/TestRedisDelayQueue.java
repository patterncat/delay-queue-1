package com.meipian.redis.test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.meipian.queues.core.Message;
import com.meipian.queues.redis.DelayQueueProcessListener;
import com.meipian.queues.redis.RedisDelayQueue;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

public class TestRedisDelayQueue {
	JedisCluster jedisCluster = null;
	RedisDelayQueue queue = null;

	@Before
	public void init() {
		String ip = "192.168.2.160";
		Set<HostAndPort> nodes = new HashSet<>();
		nodes.add(new HostAndPort(ip, 7701));
		nodes.add(new HostAndPort(ip, 7702));
		nodes.add(new HostAndPort(ip, 7703));
		nodes.add(new HostAndPort(ip, 7704));
		nodes.add(new HostAndPort(ip, 7705));
		nodes.add(new HostAndPort(ip, 7706));
		JedisPoolConfig pool = new JedisPoolConfig();
		pool.setMaxTotal(100);
		pool.setFairness(false);
		pool.setNumTestsPerEvictionRun(100);
		pool.setMaxWaitMillis(5000);
		pool.setTestOnBorrow(true);
		jedisCluster = new JedisCluster(nodes, 1000, 1000, 100, null, pool); // maxAttempt必须调大
		jedisCluster.set("test", "test");
		queue = new RedisDelayQueue("com.meipian", "delayqueue", jedisCluster, 60 * 1000,
				new DelayQueueProcessListener() {
					@Override
					public void pushCallback(Message message) {

					}

					@Override
					public void peekCallback(Message message) {
						System.out.println("message----->" + message);
						queue.ack(message.getId());//确认操作。将会删除消息
					}

					@Override
					public void ackCallback(Message message) {
					}
				});

	}

	@Test
	public void testCreate() throws InterruptedException {
		Message message = new Message();
		for (int i = 0; i < 10; i++) {
			message.setId(i + "");
			message.setPayload("test");
			message.setPriority(0);
			message.setTimeout(3000);
			queue.push(message);
		}
		// message = queue.peek();
		// queue.ack("1234");
		queue.listen();
	}

}
