package com.demo.infrastructure.repository.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.infrastructure.repository.mysql.po.NodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 节点 Mapper 接口
 * 提供节点数据访问能力
 */
@Mapper
public interface NodeMapper extends BaseMapper<NodePO> {

    /**
     * 根据名称查询节点
     *
     * @param name 节点名称
     * @return 节点持久化对象，如果不存在则返回 null
     */
    NodePO selectByName(@Param("name") String name);

    /**
     * 根据类型查询节点列表
     *
     * @param type 节点类型
     * @return 节点列表
     */
    List<NodePO> selectByType(@Param("type") String type);

    /**
     * 根据条件分页查询节点
     *
     * @param page 分页对象
     * @param name 节点名称（可选，支持模糊查询）
     * @param type 节点类型（可选）
     * @return 分页结果
     */
    IPage<NodePO> selectPageByCondition(Page<?> page, 
                                        @Param("name") String name, 
                                        @Param("type") String type);
}
