package com.tapdata.tm.task.service.impl.dagcheckstrategy;

import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Maps;
import com.tapdata.tm.commons.dag.nodes.DatabaseNode;
import com.tapdata.tm.commons.schema.DataSourceConnectionDto;
import com.tapdata.tm.commons.task.dto.TaskDto;
import com.tapdata.tm.config.security.UserDetail;
import com.tapdata.tm.ds.service.impl.DataSourceService;
import com.tapdata.tm.message.constant.Level;
import com.tapdata.tm.metadatainstance.service.MetadataInstancesService;
import com.tapdata.tm.metadatainstance.vo.SourceTypeEnum;
import com.tapdata.tm.task.constant.DagOutputTemplate;
import com.tapdata.tm.task.constant.DagOutputTemplateEnum;
import com.tapdata.tm.task.entity.TaskDagCheckLog;
import com.tapdata.tm.task.service.DagLogStrategy;
import com.tapdata.tm.utils.Lists;
import com.tapdata.tm.utils.MongoUtils;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

@Component("sourceSettingStrategy")
@Setter(onMethod_ = {@Autowired})
public class SourceSettingStrategyImpl implements DagLogStrategy {

    private DataSourceService dataSourceService;
    private MetadataInstancesService metadataInstancesService;

    private final DagOutputTemplateEnum templateEnum = DagOutputTemplateEnum.SOURCE_SETTING_CHECK;

    @Override
    public List<TaskDagCheckLog> getLogs(TaskDto taskDto, UserDetail userDetail) {
        ObjectId taskId = taskDto.getId();
        String current = DateUtil.now();
        Date now = new Date();

        List<TaskDagCheckLog> result = Lists.newArrayList();
        Map<String, Integer> nameMap = Maps.newHashMap();
        LinkedList<DatabaseNode> sourceNode = taskDto.getDag().getSourceNode();

        if (CollectionUtils.isEmpty(sourceNode)) {
            return Lists.newArrayList();
        }

        sourceNode.forEach(node -> {
            String name = node.getName();
            Integer value;
            String template;
            String grade;
            if (nameMap.containsKey(name)) {
                value = nameMap.get(name) + 1;
                template = templateEnum.getErrorTemplate();
                grade = Level.ERROR.getValue();

                String content = MessageFormat.format(template, current, name);
                TaskDagCheckLog log = new TaskDagCheckLog();
                log.setTaskId(taskId.toHexString());
                log.setCheckType(templateEnum.name());
                log.setCreateAt(now);
                log.setCreateUser(userDetail.getUserId());
                log.setLog(content);
                log.setGrade(grade);
                log.setNodeId(node.getId());

                result.add(log);
            } else {
                value = NumberUtils.INTEGER_ZERO;
            }
            nameMap.put(name, value);

            // check schema
            String connectionId = node.getConnectionId();
            DataSourceConnectionDto connectionDto = dataSourceService.findById(MongoUtils.toObjectId(connectionId));
            Optional.ofNullable(connectionDto).ifPresent(dto -> {
                List<String> tables = metadataInstancesService.tables(connectionId, SourceTypeEnum.SOURCE.name());

                int loadCount = Objects.nonNull(dto.getLoadCount()) ? dto.getLoadCount() : 0;
                if (CollectionUtils.isEmpty(tables)) {
                    TaskDagCheckLog schemaLog = new TaskDagCheckLog();
                    schemaLog.setTaskId(taskId.toHexString());
                    schemaLog.setCheckType(templateEnum.name());
                    schemaLog.setCreateAt(now);
                    schemaLog.setCreateUser(userDetail.getUserId());
                    schemaLog.setLog(MessageFormat.format(DagOutputTemplate.SOURCE_SETTING_ERROR_SCHEMA, name));
                    schemaLog.setGrade(Level.ERROR.getValue());
                    schemaLog.setNodeId(node.getId());
                    result.add(schemaLog);
                } else {
                    int tableCount = Objects.nonNull(dto.getTableCount()) ? dto.getTableCount().intValue() : 0;
                    if (loadCount < tableCount) {
                        TaskDagCheckLog schemaLog = new TaskDagCheckLog();
                        schemaLog.setTaskId(taskId.toHexString());
                        schemaLog.setCheckType(templateEnum.name());
                        schemaLog.setCreateAt(now);
                        schemaLog.setCreateUser(userDetail.getUserId());
                        schemaLog.setLog(MessageFormat.format(DagOutputTemplate.SOURCE_SETTING_ERROR_SCHEMA_LOAD, name));
                        schemaLog.setGrade(Level.ERROR.getValue());
                        schemaLog.setNodeId(node.getId());
                        result.add(schemaLog);
                    }
                }
            });
        });

        if (CollectionUtils.isEmpty(result)) {
            TaskDagCheckLog log = new TaskDagCheckLog();
            String content = MessageFormat.format(templateEnum.getInfoTemplate(), current);
            log.setTaskId(taskId.toHexString());
            log.setCheckType(templateEnum.name());
            log.setCreateAt(now);
            log.setCreateUser(userDetail.getUserId());
            log.setLog(content);
            log.setGrade(Level.INFO.getValue());
            log.setNodeId(taskDto.getDag().getSourceNode().getFirst().getId());

            result.add(log);
        }

        return result;
    }
}
