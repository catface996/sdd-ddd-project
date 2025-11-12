package com.demo.common.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页结果类
 * 用于封装分页查询的结果数据
 *
 * @param <T> 数据类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    /**
     * 转换数据类型
     * 用于在不同层之间转换分页数据（如 Entity -> DTO -> VO）
     *
     * @param converter 转换函数
     * @param <R>       目标数据类型
     * @return 转换后的分页结果
     */
    public <R> PageResult<R> convert(Function<T, R> converter) {
        List<R> convertedRecords = this.records.stream()
                .map(converter)
                .collect(Collectors.toList());
        
        return new PageResult<>(
                this.current,
                this.size,
                this.total,
                this.pages,
                convertedRecords
        );
    }
}
