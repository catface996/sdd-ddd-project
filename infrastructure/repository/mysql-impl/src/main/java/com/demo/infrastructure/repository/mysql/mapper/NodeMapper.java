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
 * <p>
 * 继承 MyBatis-Plus BaseMapper 获得基础 CRUD 能力，
 * 并定义自定义查询方法。
 * </p>
 *
 * @author system
 * @since 1.0.0
 */
@Mapper
public interface NodeMapper extends BaseMapper<NodePO> {

    /**
     * 根据节点名称查询节点
     * <p>
     * 精确匹配节点名称，只查询未删除的记录。
     * </p>
     *
     * @param name 节点名称
     * @return 节点 PO，不存在返回 null
     */
    NodePO selectByName(@Param("name") String name);

    /**
     * 根据节点类型查询节点列表
     * <p>
     * 精确匹配节点类型，只查询未删除的记录，按创建时间降序排序。
     * </p>
     *
     * @param type 节点类型
     * @return 节点 PO 列表
     */
    List<NodePO> selectByType(@Param("type") String type);

    /**
     * 分页查询节点
     * <p>
     * 支持按名称模糊查询和类型精确查询，只查询未删除的记录，按创建时间降序排序。
     * </p>
     *
     * @param page 分页对象
     * @param name 节点名称（模糊查询，可选）
     * @param type 节点类型（精确查询，可选）
     * @return 分页结果
     */
    IPage<NodePO> selectPageByCondition(Page<?> page,
                                        @Param("name") String name,
                                        @Param("type") String type);
}
