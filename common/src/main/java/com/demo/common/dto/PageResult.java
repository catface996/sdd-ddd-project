package com.demo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页结果封装类
 * 用于在各层之间传递分页数据
 *
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
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
     * 数据列表
     */
    private List<T> records;

    /**
     * 类型转换方法
     * 将当前分页结果转换为另一种类型的分页结果
     *
     * @param converter 转换函数
     * @param <R>       目标类型
     * @return 转换后的分页结果
     */
    public <R> PageResult<R> convert(Function<T, R> converter) {
        List<R> convertedRecords = this.records == null ? null :
                this.records.stream()
                        .map(converter)
                        .collect(Collectors.toList());

        return PageResult.<R>builder()
                .current(this.current)
                .size(this.size)
                .total(this.total)
                .pages(this.pages)
                .records(convertedRecords)
                .build();
    }
}
