package com.ll.security_2025_01_06.standard.page.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageDto<T> {
    private long totalItems;
    private List<T> items;
    private long totalPages;
    private int currentPageNumber;
    private int pageSize;

    public PageDto(Page<T> page) {
        this.totalItems = page.getTotalElements();
        this.items = page.getContent();
        this.totalPages = page.getTotalPages();
        this.currentPageNumber = page.getNumber() + 1;
        this.pageSize = page.getSize();
    }
}
