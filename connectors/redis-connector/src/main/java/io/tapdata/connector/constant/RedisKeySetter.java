package io.tapdata.connector.constant;


import io.tapdata.connector.redis.RedisRecordWriter;
import io.tapdata.entity.logger.TapLogger;
import io.tapdata.entity.utils.DataMap;
import io.tapdata.pdk.apis.context.TapConnectorContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存键设置器
 *
 * @author <a href="mailto:harsen_lin@163.com">Harsen</a>
 * @version v1.0 2021/12/4 下午9:28 Create
 */
public class RedisKeySetter {

  private Map<String, RedisKey> allSet = new HashMap<>();

  private List arrayList = new ArrayList();


  public String getRedisKey(Map<String, Object> value, TapConnectorContext connectorContext, String tableName) {

    DataMap nodeConfig = connectorContext.getNodeConfig();
    if (nodeConfig == null) {
      return tableName;
    }
    String valueType = (String) nodeConfig.get("valueType");
    if (RedisRecordWriter.VALUE_TYPE_LIST.equals(valueType)) {
      return (String) nodeConfig.get("cachePrefix");
    }

    RedisKey redisKey = getOrAuto(tableName, connectorContext);

    StringBuilder buf = new StringBuilder();
    if (StringUtils.isNotBlank(redisKey.getPrefix())) {
      buf.append(redisKey.getPrefix());
    } else {
      buf.append(tableName);
    }

    String[] keys = redisKey.getVal().split(",");
    for (String key : keys) {
      Object object = value.get(key);
      buf.append("_").append(object2String(object));
    }

    return buf.toString();
  }


  public RedisKey getOrAuto(String tableName, TapConnectorContext connectorContext) {
    RedisKey val = allSet.get(tableName);
    if (null == val) {
      val = init(tableName, connectorContext);
      allSet.put(tableName, val);
    }
    return val;
  }

  private RedisKey init(String tableName, TapConnectorContext connectorContext) {

    RedisKey redisKey = new RedisKey();
    DataMap nodeConfig = connectorContext.getNodeConfig();

    List<String> cacheKeys = (List<String>) nodeConfig.get("cacheKeys");

    if (CollectionUtils.isEmpty(cacheKeys)) {
      if (StringUtils.isEmpty(tableName)) {
        throw new RuntimeException("Not found cache key " + tableName);
      }
    }

    redisKey.setVal(String.join(",", cacheKeys));
    redisKey.setPrefix((String) nodeConfig.get("cachePrefix"));

    if (StringUtils.isBlank(redisKey.getVal())) {
      TapLogger.error("Set redis redisKey is '{}'", redisKey.getVal());
      throw new RuntimeException("Redis write failed,rediskey is blank");
    }

    return redisKey;
  }


  private String object2String(Object object) {
    if (object == null) {
      return "null";
    }
    if (object instanceof byte[]) {
      return new String((byte[]) object);
    }
    return object.toString();
  }


}