package com.example.mg.common;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 分页数据包装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageData<T> {
    private List<T> list;
    private Long total;
    private Integer pageSize;
    private Integer page;
    private Integer totalPage;
    private Boolean hasNext;

    public static <T> PageData<T> create(List<T> records, Long total) {
        return PageData.<T>builder()
                .list(records)
                .total(total)
                .build();
    }

    public static <T> PageData<T> create(List<T> records, Long total, Integer current, Integer size) {
        Integer pages = (int) Math.ceil((double) total / size);
        boolean hasNext = current < pages;
        return PageData.<T>builder()
                .list(records)
                .total(total)
                .page(current)
                .pageSize(size)
                .totalPage(pages)
                .hasNext(hasNext)
                .build();
    }

    public static <T> PageData<T> create(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        boolean hasNext = page.getCurrent() < page.getPages();
        return PageData.<T>builder()
                .total(page.getTotal())
                .page((int) page.getCurrent())
                .pageSize((int) page.getSize())
                .totalPage((int) page.getPages())
                .list(page.getRecords())
                .hasNext(hasNext)
                .build();
    }
}