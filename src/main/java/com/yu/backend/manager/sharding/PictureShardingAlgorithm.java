package com.yu.backend.manager.sharding;

import com.yu.backend.constant.PictureConstant;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * picture 表按 spaceId 分片：0 → picture，旗舰团队空间 N → picture_N
 */
public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    private static final String LOGIC_TABLE = "picture";

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        Long spaceId = shardingValue.getValue();
        if (PictureConstant.isPublicSpace(spaceId)) {
            return LOGIC_TABLE;
        }
        String targetTable = LOGIC_TABLE + "_" + spaceId;
        for (String each : availableTargetNames) {
            if (targetTable.equals(extractTableName(each))) {
                return targetTable;
            }
        }
        return LOGIC_TABLE;
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         RangeShardingValue<Long> rangeShardingValue) {
        return availableTargetNames;
    }

    private String extractTableName(String dataNode) {
        int dot = dataNode.lastIndexOf('.');
        return dot >= 0 ? dataNode.substring(dot + 1) : dataNode;
    }

    @Override
    public Properties getProps() {
        return null;
    }

    @Override
    public void init(Properties properties) {
    }
}
