package com.datech.zjfh.api.common;


import com.datech.zjfh.api.common.bean.OrderBy;
import com.datech.zjfh.api.common.bean.PageInfo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IService2<T> {

    boolean save(T entity);

    boolean saveBatch(Collection<T> entityList);

    boolean saveBatch(Collection<T> entityList, int batchSize);

    boolean saveOrUpdateBatch(Collection<T> entityList);

    boolean saveOrUpdateBatch(Collection<T> entityList, int batchSize);

    boolean removeById(Serializable id);

    boolean removeByMap(Map<String, Object> columnMap);

    boolean removeByIds(Collection<? extends Serializable> idList);

    boolean updateById(T entity);

    boolean updateBatchById(Collection<T> entityList);

    boolean updateBatchById(Collection<T> entityList, int batchSize);

    boolean saveOrUpdate(T entity);

    T getById(Serializable id);

    List<T> listByIds(Collection<? extends Serializable> idList);

    List<T> listByMap(Map<String, Object> columnMap);

    int count();

    List<T> list();

    List<Map<String, Object>> listMaps();

    List<Object> listObjs();

    <V> List<V> listObjs(Function<? super Object, V> mapper);

    PageInfo<T> page(T query, Map<String, String[]> parameterMap, List<OrderBy> orderBys, int pageNum, int pageSize);

    /**
     * 获取数据权限SQL
     * @return
     */
    String getDataAuthSQL();

    /**
     * 获取数据权限SQL
     *
     * @param alias 数据表别名(对自定义SQL无效，自定义SQL需要在配置数据规则时自行添加别名)
     * @return
     */
    String getDataAuthSQL(String alias);

//    T selectOne(Wrapper<T> queryWrapper);
//
//    int selectCount(Wrapper<T> queryWrapper);
//
//    int update(LambdaUpdateWrapper<T> lambdaUpdateWrapper);

}
