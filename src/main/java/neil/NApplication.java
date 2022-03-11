package neil;



import java.io.Serializable;

import java.util.Iterator;

import java.util.concurrent.TimeUnit;



import javax.cache.Cache;

import javax.cache.CacheManager;

import javax.cache.Caching;

import javax.cache.configuration.CacheEntryListenerConfiguration;

import javax.cache.configuration.CompleteConfiguration;

import javax.cache.configuration.FactoryBuilder;

import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

import javax.cache.configuration.MutableConfiguration;

import javax.cache.event.CacheEntryCreatedListener;

import javax.cache.event.CacheEntryEvent;

import javax.cache.event.CacheEntryListenerException;

import javax.cache.spi.CachingProvider;



import com.hazelcast.cache.ICache;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.CacheSimpleConfig;

import com.hazelcast.config.Config;

import com.hazelcast.core.DistributedObject;

import com.hazelcast.core.Hazelcast;

import com.hazelcast.core.HazelcastInstance;



public class NApplication implements CacheEntryCreatedListener<String, String>, Serializable {

	private static final long serialVersionUID = 1L;

	private static final String CACHE_NAME = "cache1";



	@SuppressWarnings({ "unchecked", "resource" })

	public static void main(String[] args) throws Exception {

		Config config = new Config();

//		config.getJetConfig().setEnabled(true);

		config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(false);

		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);



		CacheSimpleConfig cacheSimpleConfig = new CacheSimpleConfig();

		cacheSimpleConfig.setName("*");

		cacheSimpleConfig.setStatisticsEnabled(true);

		config.getCacheConfigs().put(cacheSimpleConfig.getName(), cacheSimpleConfig);



		HazelcastInstance hazelcastServer = Hazelcast.newHazelcastInstance(config);

		HazelcastInstance client = HazelcastClient.newHazelcastClient();
		CacheSimpleConfig simpleConfig = new CacheSimpleConfig("testcache2");
		client.getConfig().addCacheConfig(simpleConfig);
		Cache<String, String> cache1 =  client.getCacheManager().getCache("testcache2");
		//		Cache<String, String> cache1 = hazelcastServer.getCacheManager().getCache(CACHE_NAME);





		NApplication application = new NApplication();

		CacheEntryListenerConfiguration<String, String> listenerConf = new MutableCacheEntryListenerConfiguration<>(

				FactoryBuilder.factoryOf(application), null, true, false);



		cache1.registerCacheEntryListener(listenerConf);



		for (int i = 0; i < 5; i++) {

			System.out.printf("-----------------------%n");

			for (DistributedObject distributedObject : hazelcastServer.getDistributedObjects()) {

				if (distributedObject instanceof ICache) {

					ICache<String, String> iCache = (ICache<String, String>) distributedObject;

					System.out.printf("  => ICACHE %s %s icache.size()==%d%n", iCache.getName(), iCache.getPrefixedName(),

							iCache.size());



					cache1

							.put("cache1" + i, String.valueOf(i));

					iCache

							.put("iCache" + i, String.valueOf(i));

					hazelcastServer.getCacheManager().getCache(iCache.getName())

							.put("getCache" + i, String.valueOf(i));

				}

			}

			System.out.printf("-----------------------%n");

			System.out.printf("(%d) = TimeUnit.MINUTES.sleep(1)%n", i);

			TimeUnit.MINUTES.sleep(1);

		}



		System.out.printf("-----------------------%n");

		System.out.printf("hazelcastServer.shutdown();%n");

		hazelcastServer.shutdown();

		System.out.printf("=== END%n");

		System.exit(0);

	}



	@Override

	public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends String>> events)

			throws CacheEntryListenerException {

		Iterator<CacheEntryEvent<? extends String, ? extends String>> iterator = events.iterator();

		while (iterator.hasNext()) {

			CacheEntryEvent<? extends String, ? extends String> cacheEntryEvent = iterator.next();

			System.out.println("onCreated::" + cacheEntryEvent);

		}

	}



}
