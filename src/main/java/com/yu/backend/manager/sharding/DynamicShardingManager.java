package com.yu.backend.manager.sharding;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;

import com.yu.backend.constant.PictureConstant;

import com.yu.backend.mapper.PictureMapper;

import com.yu.backend.mapper.SpaceMapper;

import com.yu.backend.model.entity.Picture;

import com.yu.backend.model.entity.Space;

import com.yu.backend.model.enums.SpaceLevelEnum;

import com.yu.backend.model.enums.SpaceTypeEnum;

import lombok.extern.slf4j.Slf4j;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import org.apache.shardingsphere.mode.manager.ContextManager;

import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;

import org.apache.shardingsphere.sharding.rule.ShardingRule;

import org.springframework.stereotype.Component;



import javax.annotation.PostConstruct;

import javax.annotation.Resource;

import javax.sql.DataSource;

import java.sql.SQLException;

import java.util.Collections;

import java.util.List;

import java.util.Optional;

import java.util.Set;

import java.util.stream.Collectors;



/**

 * 启动与创建空间时，动态维护 picture 分表的 actual-data-nodes

 */

@Component

@Slf4j

public class DynamicShardingManager {



    private static final String LOGIC_TABLE_NAME = "picture";

    private static final String DATASOURCE_NAME = "ds0";



    @Resource

    private DataSource dataSource;



    @Resource

    private SpaceMapper spaceMapper;



    @Resource

    private PictureMapper pictureMapper;



    @PostConstruct

    public void initialize() {

        try {

            log.info("初始化 picture 动态分表配置...");

            migrateSpaceIdNullToZero();

            migrateFlagshipPictureData();

            updateShardingTableNodes();

        } catch (Exception e) {

            log.warn("ShardingSphere 动态分表初始化跳过（可能未启用分片数据源）: {}", e.getMessage());

        }

    }



    /**

     * 公共图库 spaceId：NULL → 0

     */

    public void migrateSpaceIdNullToZero() {

        try {

            boolean updated = SqlRunner.db().update("UPDATE picture SET spaceId = 0 WHERE spaceId IS NULL");
            if (updated) {
                log.info("已将公共图库记录的 spaceId 从 NULL 更新为 0");
            }

        } catch (Exception e) {

            log.debug("spaceId 迁移跳过: {}", e.getMessage());

        }

    }



    /**

     * 旗舰版团队空间历史数据迁移到 picture_{spaceId}

     */

    public void migrateFlagshipPictureData() {

        List<Space> flagshipTeamSpaces = spaceMapper.selectList(null).stream()

                .filter(s -> SpaceTypeEnum.TEAM.getValue() == s.getSpaceType())

                .filter(s -> SpaceLevelEnum.FLAGSHIP.getValue() == s.getSpaceLevel())

                .collect(Collectors.toList());

        for (Space space : flagshipTeamSpaces) {

            migrateSpacePictures(space.getId());

        }

    }



    private void migrateSpacePictures(Long spaceId) {

        if (spaceId == null || spaceId <= 0) {

            return;

        }

        String shardTable = LOGIC_TABLE_NAME + "_" + spaceId;

        try {

            SqlRunner.db().update("CREATE TABLE IF NOT EXISTS " + shardTable + " LIKE " + LOGIC_TABLE_NAME);

            Long countInMain = pictureMapper.selectCount(new QueryWrapper<Picture>().eq("spaceId", spaceId));

            if (countInMain == null || countInMain <= 0) {

                return;

            }

            SqlRunner.db().update(

                    "INSERT INTO " + shardTable + " SELECT * FROM " + LOGIC_TABLE_NAME + " WHERE spaceId = {0}",

                    spaceId);

            SqlRunner.db().update("DELETE FROM " + LOGIC_TABLE_NAME + " WHERE spaceId = {0}", spaceId);

            log.info("旗舰空间 {} 的 {} 条图片已迁移至 {}", spaceId, countInMain, shardTable);

        } catch (Exception e) {

            log.error("迁移旗舰空间 picture 分表失败, spaceId={}", spaceId, e);

        }

    }



    /**

     * 旗舰版团队空间创建独立分表 picture_{spaceId}

     */

    public void createSpacePictureTable(Space space) {

        if (space == null || space.getId() == null) {

            return;

        }

        if (space.getSpaceType() == null || space.getSpaceLevel() == null) {

            return;

        }

        if (SpaceTypeEnum.TEAM.getValue() != space.getSpaceType()

                || SpaceLevelEnum.FLAGSHIP.getValue() != space.getSpaceLevel()) {

            return;

        }

        Long spaceId = space.getId();

        String tableName = LOGIC_TABLE_NAME + "_" + spaceId;

        try {

            SqlRunner.db().update("CREATE TABLE IF NOT EXISTS " + tableName + " LIKE " + LOGIC_TABLE_NAME);

            updateShardingTableNodes();

            log.info("创建并注册图片分表: {}", tableName);

        } catch (Exception e) {

            log.error("创建图片分表失败，spaceId={}", spaceId, e);

        }

    }



    private Set<String> fetchAllPictureTableNames() {

        Set<Long> spaceIds = spaceMapper.selectList(null).stream()

                .filter(s -> SpaceTypeEnum.TEAM.getValue() == s.getSpaceType())

                .filter(s -> SpaceLevelEnum.FLAGSHIP.getValue() == s.getSpaceLevel())

                .map(Space::getId)

                .collect(Collectors.toSet());

        Set<String> tableNames = spaceIds.stream()

                .map(id -> LOGIC_TABLE_NAME + "_" + id)

                .collect(Collectors.toSet());

        tableNames.add(LOGIC_TABLE_NAME);

        return tableNames;

    }



    private void updateShardingTableNodes() throws SQLException {

        Set<String> tableNames = fetchAllPictureTableNames();

        String newActualDataNodes = tableNames.stream()

                .map(name -> DATASOURCE_NAME + "." + name)

                .collect(Collectors.joining(","));

        log.info("picture actual-data-nodes: {}", newActualDataNodes);



        ContextManager contextManager = getContextManager();

        String databaseName = resolveLogicDatabaseName(contextManager);

        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName);

        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);

        if (!shardingRule.isPresent()) {

            log.error("未找到 ShardingSphere 分片规则");

            return;

        }

        ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration) shardingRule.get().getConfiguration();

        List<ShardingTableRuleConfiguration> updatedRules = ruleConfig.getTables().stream()

                .map(oldTableRule -> {

                    if (!LOGIC_TABLE_NAME.equals(oldTableRule.getLogicTable())) {

                        return oldTableRule;

                    }

                    ShardingTableRuleConfiguration newRule = new ShardingTableRuleConfiguration(

                            LOGIC_TABLE_NAME, newActualDataNodes);

                    newRule.setDatabaseShardingStrategy(oldTableRule.getDatabaseShardingStrategy());

                    newRule.setTableShardingStrategy(oldTableRule.getTableShardingStrategy());

                    newRule.setKeyGenerateStrategy(oldTableRule.getKeyGenerateStrategy());

                    newRule.setAuditStrategy(oldTableRule.getAuditStrategy());

                    return newRule;

                })

                .collect(Collectors.toList());

        ruleConfig.setTables(updatedRules);

        contextManager.alterRuleConfiguration(databaseName, Collections.singleton(ruleConfig));

        contextManager.reloadDatabase(databaseName);

        log.info("picture 动态分表规则更新成功");

    }



    private ContextManager getContextManager() throws SQLException {

        try (ShardingSphereConnection connection = dataSource.getConnection().unwrap(ShardingSphereConnection.class)) {

            return connection.getContextManager();

        }

    }



    private String resolveLogicDatabaseName(ContextManager contextManager) {

        Set<String> names = contextManager.getMetaDataContexts().getMetaData().getDatabases().keySet();

        if (names.isEmpty()) {

            throw new IllegalStateException("ShardingSphere 逻辑库为空");

        }

        return names.iterator().next();

    }

}


