# 数据源配置指南 Connector Configuration Guide

## CH1 Basic Information

### CH1.1 NAME
MySQL
### CH1.2 Plugin Version
2.10.2
### CH1.3 Author
- Author Name: Sam
- Author Slack: N/A
- Author Wechat: N/A 
- Author Email: 12345678@gmail.com

## 支持的数据源版本 Supported Versions
- 5.0
- 5.1
- 5.5
- 5.6
- 5.7
- 8.x

## 已知问题列表 Known Issues

## 插件实现的源功能 Implemented Features - as Source


#### 全量读 source-feature-full-sync

#### 增量读 source-feature-cdc

#### 全量总数统计 source-feature-count

#### 增量指定时间 source-feature-cdc-time-point

#### 高级查询 source-feature-advance-query

#### 查询索引 ###source-feature-load-index

#### 加载模型 source-feature-load-schema

#### 获取表 source-feature-table-names

#### DDL读取 source-feature-ddl


## 插件实现的目标功能 Implemented Features - as Target


#### 插入事件 target-feature-insert

#### 更新事件 target-feature-update

#### 删除事件 target-feature-delete

#### 清除数据 target-feature-clear-data

#### 创建表 target-feature-create-table

#### 删除表 target-feature-drop-table

#### 创建索引 target-feature-create-index

#### 删除索引 target-feature-drop-index

#### DDL写入 target-feature-ddl

## 作为源时候的数据库配置 Configuration Notes for Source

## 作为目标时候的数据库配置 Configuration Notes for Target

## 类型映射表  Type Mappings 


## 版本历史 Change Log 

- 2020.01.03 Initial Version 
