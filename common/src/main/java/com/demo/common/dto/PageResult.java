package com.demo.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页结果（框架无关）
 * <p>
 * 用于封装分页查询结果，支持跨层使用（Repository、Application、HTTP 层）
 * 通过泛型参数适配不同层的数据类型（Entity、DTO、VO）
 * </p>
 *
 * @param <T> 数据类型
 * @author demo
 */
@Data
public class PageResult<T> implements Serializable {

    /**
     * 当前页码
     */
    private long current;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 无参构造函数
     */
    public PageResult() {
    }

    /**
     * 构造函数
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param total   总记录数
     * @param records 当前页数据
     */
    public PageResult(long current, long size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
        // 计算总页数
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }

    /**
     * 转换分页数据类型
     * <p>
     * 用于在不同层之间转换数据类型，例如：
     * - Repository 层：PageResult&lt;Entity&gt; → Application 层：PageResult&lt;DTO&gt;
     * - Application 层：PageResult&lt;DTO&gt; → HTTP 层：PageResult&lt;VO&gt;
     * </p>
     *
     * @param converter 转换函数
     * @param <R>       目标类型
     * @return 转换后的分页结果
     */
    public <R> PageResult<R> convert(Function<T, R> converter) {
        List<R> convertedRecords = this.records.stream()
                .map(converter)
                .collect(Collectors.toList());
        return new PageResult<>(this.current, this.size, this.total, convertedRecords);
    }

    /**
     * 判断是否有数据
     *
     * @return true: 有数据, false: 无数据
     */
    public boolean hasRecords() {
        return records != null && !records.isEmpty();
    }

    /**
     * 判断是否为空
     *
     * @return true: 无数据, false: 有数据
     */
    public boolean isEmpty() {
        return !hasRecords();
    }
}
