package com.datech.zjfh.api.util;

import com.datech.zjfh.api.common.consts.DBConstant;
import com.datech.zjfh.api.common.exception.FastBootException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Slf4j
public class DBUtil {
    /** 当前系统数据库类型 */
    private static String DB_TYPE = "";

    public static String getDatabaseType() {
        if(oConvertUtil.isNotEmpty(DB_TYPE)){
            return DB_TYPE;
        }
        DataSource dataSource = SpringContextUtil.getApplicationContext().getBean(DataSource.class);
        try {
            return getDatabaseTypeByDataSource(dataSource);
        } catch (SQLException e) {
            //e.printStackTrace();
            log.warn(e.getMessage());
            return "";
        }
    }

    /**
     * 获取数据库类型
     * @param dataSource
     * @return
     * @throws SQLException
     */
    private static String getDatabaseTypeByDataSource(DataSource dataSource) throws SQLException {
        if("".equals(DB_TYPE)) {
            Connection connection = dataSource.getConnection();
            try {
                DatabaseMetaData md = connection.getMetaData();
                String dbType = md.getDatabaseProductName().toLowerCase();
                if(dbType.indexOf("mysql")>=0) {
                    DB_TYPE = DBConstant.DB_TYPE_MYSQL;
                }else if(dbType.indexOf("oracle")>=0 ||dbType.indexOf("dm")>=0) {
                    DB_TYPE = DBConstant.DB_TYPE_ORACLE;
                }else if(dbType.indexOf("sqlserver")>=0||dbType.indexOf("sql server")>=0) {
                    DB_TYPE = DBConstant.DB_TYPE_SQLSERVER;
                }else if(dbType.indexOf("postgresql")>=0) {
                    DB_TYPE = DBConstant.DB_TYPE_POSTGRESQL;
                }else {
                    throw new FastBootException("数据库类型:["+dbType+"]不识别!");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }finally {
                connection.close();
            }
        }
        return DB_TYPE;

    }

}
