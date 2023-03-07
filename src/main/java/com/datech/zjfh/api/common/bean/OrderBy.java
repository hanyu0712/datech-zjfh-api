package com.datech.zjfh.api.common.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderBy implements Serializable {
    private static final long serialVersionUID = 1L;

    private final static String DESC = "desc";

    private String column;
    private String order = "asc";

    @JsonIgnore
    private String dbColumn; //原属性名称

    public OrderBy() {
    }

    public OrderBy(final String column, final String order) {
        this.column = column;
        this.dbColumn = convertColumn(column);
        if(order != null && DESC.equals(order.toLowerCase())) {
            this.order = DESC;
        }
    }

    public OrderBy(final String column, final boolean asc, final boolean isConvert) {
        if(isConvert) {
            this.column = column;
            this.dbColumn = convertColumn(column);
        } else {
            this.column = column;
        }
        if(!asc) {
            this.order = DESC;
        }
    }

/*    public static OrderBy asc(String column) {
        return build(column, true);
    }

    public static OrderBy desc(String column) {
        return build(column, false);
    }

    private static OrderBy build(String column, boolean asc) {
        return new OrderBy(column, asc);
    }*/

    public void setColumn(final String column) {
        this.column = column;
        this.dbColumn = convertColumn(column);
    }

    public String getColumn() {
        return this.column;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        if(order != null && DESC.equals(order.toLowerCase())) {
            this.order = DESC;
        }
    }

    @JsonIgnore
    public String getDbColumn() {
        return this.dbColumn;
    }

    public boolean isAsc() {
        if(StringUtils.isNotBlank(this.order) && DESC.equals(this.order)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "OrderBy(column=" + this.getColumn() + ", order=" + this.getOrder() +", dbColumn="
                + this.getDbColumn() +", asc=" + this.isAsc() + ")";
    }

    private String convertColumn(String column) {
        if(column != null) {
            if(column.endsWith("_dictText")) {
                column = column.substring(0, column.length()-9);
            }
            return camel2Underline(column);
        }
        return column;
    }

    private static String camel2Underline(String camelCase){
        Pattern humpPattern = Pattern.compile("[A-Z]");
        Matcher matcher = humpPattern.matcher(camelCase);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            matcher.appendReplacement(sb, "_"+matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
