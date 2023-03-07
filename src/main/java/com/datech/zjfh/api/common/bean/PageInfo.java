package com.datech.zjfh.api.common.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class PageInfo<T> implements Serializable {
    private static final long serialVersionUID = 298198062114528083L;

    protected List<T> records;
    protected long total;
    protected long size;
    protected long current;
    protected List<OrderBy> orders;
    protected boolean optimizeCountSql;
    protected boolean isSearchCount;
    protected boolean hitCount;
    protected String countId;
    protected Long maxLimit;

    public PageInfo() {
        this.records = Collections.emptyList();
        this.total = 0L;
        this.size = 10L;
        this.current = 1L;
        this.orders = new ArrayList();
        this.optimizeCountSql = true;
        this.isSearchCount = true;
        this.hitCount = false;
    }

    public PageInfo(long current, long size) {
        this(current, size, 0L);
    }

    public PageInfo(long current, long size, long total) {
        this(current, size, total, true);
    }

    public PageInfo(long current, long size, boolean isSearchCount) {
        this(current, size, 0L, isSearchCount);
    }

    public PageInfo(long current, long size, long total, boolean isSearchCount) {
        this.records = Collections.emptyList();
        this.total = 0L;
        this.size = 10L;
        this.current = 1L;
        this.orders = new ArrayList();
        this.optimizeCountSql = true;
        this.isSearchCount = true;
        this.hitCount = false;
        if (current > 1L) {
            this.current = current;
        }
        this.size = size;
        this.total = total;
        this.isSearchCount = isSearchCount;
    }

    public boolean hasPrevious() {
        return this.current > 1L;
    }

    public boolean hasNext() {
        return this.current < this.getPages();
    }

    public List<T> getRecords() {
        return this.records;
    }

    public PageInfo<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    public long getTotal() {
        return this.total;
    }

    public PageInfo<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    public long getSize() {
        return this.size;
    }

    public PageInfo<T> setSize(long size) {
        this.size = size;
        return this;
    }

    public long getCurrent() {
        return this.current;
    }

    public PageInfo<T> setCurrent(long current) {
        this.current = current;
        return this;
    }

    public String countId() {
        return this.getCountId();
    }

    public Long maxLimit() {
        return this.getMaxLimit();
    }

    private String[] mapOrderToArray(Predicate<OrderBy> filter) {
        List<String> columns = new ArrayList(this.orders.size());
        this.orders.forEach((i) -> {
            if (filter.test(i)) {
                columns.add(i.getColumn());
            }

        });
        return (String[])columns.toArray(new String[0]);
    }

    private void removeOrder(Predicate<OrderBy> filter) {
        for(int i = this.orders.size() - 1; i >= 0; --i) {
            if (filter.test((OrderBy)this.orders.get(i))) {
                this.orders.remove(i);
            }
        }

    }

    public PageInfo<T> addOrder(OrderBy... items) {
        this.orders.addAll(Arrays.asList(items));
        return this;
    }

    public PageInfo<T> addOrder(List<OrderBy> items) {
        this.orders.addAll(items);
        return this;
    }

    public List<OrderBy> orders() {
        return this.getOrders();
    }

    public boolean optimizeCountSql() {
        return this.optimizeCountSql;
    }

    public boolean isOptimizeCountSql() {
        return this.optimizeCountSql();
    }

    public boolean isSearchCount() {
        return this.total < 0L ? false : this.isSearchCount;
    }

    public PageInfo<T> setSearchCount(boolean isSearchCount) {
        this.isSearchCount = isSearchCount;
        return this;
    }

    public PageInfo<T> setOptimizeCountSql(boolean optimizeCountSql) {
        this.optimizeCountSql = optimizeCountSql;
        return this;
    }

    public void hitCount(boolean hit) {
        this.hitCount = hit;
    }

    public void setHitCount(boolean hit) {
        this.hitCount = hit;
    }

    public boolean isHitCount() {
        return this.hitCount;
    }

    public List<OrderBy> getOrders() {
        return this.orders;
    }

    public void setOrders(final List<OrderBy> orders) {
        this.orders = orders;
    }

    public String getCountId() {
        return this.countId;
    }

    public void setCountId(final String countId) {
        this.countId = countId;
    }

    public Long getMaxLimit() {
        return this.maxLimit;
    }

    public void setMaxLimit(final Long maxLimit) {
        this.maxLimit = maxLimit;
    }

    public long getPages() {
        if (this.getSize() == 0L) {
            return 0L;
        } else {
            long pages = this.getTotal() / this.getSize();
            if (this.getTotal() % this.getSize() != 0L) {
                ++pages;
            }
            return pages;
        }
    }

    public long offset() {
        long current = this.getCurrent();
        return current <= 1L ? 0L : (current - 1L) * this.getSize();
    }

}
