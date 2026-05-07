package com.byteknight.pagequerydemo.vo;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private int page;
    private int size;
    private long total;
    private int totalPages;
    private long elapsedMs;
    private List<T> records;
    private int pageDelta;

    public PageResult(int page, int size, long total, long elapsedMs, List<T> records, int pageDelta) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
        this.elapsedMs = elapsedMs;
        this.records = records;
        this.pageDelta = pageDelta;
    }
}
