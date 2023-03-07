package com.datech.zjfh.api.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.datech.zjfh.api.common.bean.OrderBy;
import com.datech.zjfh.api.common.bean.PageInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PageService<T> {

    default PageInfo<T> toPageInfo(IPage<T> page) {
        return toPageInfo(page, null, true);
    }

    default PageInfo<T> toPageInfo(IPage<T> page, List<OrderBy> orderByList) {
        return toPageInfo(page, orderByList, true);
    }

    default PageInfo<T> toPageInfo(IPage<T> page, boolean containRecords) {
        return toPageInfo(page, null, containRecords);
    }

    default PageInfo<T> toPageInfo(IPage<T> page, List<OrderBy> orderByList, boolean containRecords) {
        PageInfo<T> pageInfo = new PageInfo<T>();
        pageInfo.setCountId(page.countId());
        pageInfo.setCurrent(page.getCurrent());
        pageInfo.setHitCount(page.isHitCount());
        pageInfo.setMaxLimit(page.maxLimit());
        pageInfo.setOptimizeCountSql(page.optimizeCountSql());
        pageInfo.setSearchCount(page.isSearchCount());
        if(containRecords) {
/*            if(page.getRecords() != null && page.getRecords().size() > 0) {
                List<T> list = new ArrayList<>();
                page.getRecords().forEach(e -> {
                    list.add(e);
                });
                pageInfo.setRecords(list);
            }*/
            pageInfo.setRecords(page.getRecords());
        }
        pageInfo.setSize(page.getSize());
        pageInfo.setTotal(page.getTotal());
        Map<String, String> orderByMap = new HashMap<>();
        if(orderByList != null) {
            orderByList.forEach(e -> {
                if(StringUtils.isNotBlank(e.getDbColumn())) {
                    orderByMap.put(e.getDbColumn(), e.getColumn());
                }
            });
        }
        List<OrderItem> orders = page.orders();
        if(orders != null && orders.size() > 0) {
            List<OrderBy> orderBys = new ArrayList<>();
            orders.forEach(e -> {
                if(orderByMap.containsKey(e.getColumn())) {
                    orderBys.add(new OrderBy(orderByMap.get(e.getColumn()), e.isAsc(), false));
                } else {
                    orderBys.add(new OrderBy(e.getColumn(), e.isAsc(), false));
                }
            });
            pageInfo.setOrders(orderBys);
        }
        return pageInfo;
    }

}

