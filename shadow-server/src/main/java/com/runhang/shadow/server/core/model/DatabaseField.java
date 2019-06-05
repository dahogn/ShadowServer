package com.runhang.shadow.server.core.model;
/**
 * @ClassName DatabaseField
 * @Description 实体与数据库字段对应
 * @Date 2019/6/4 23:40
 * @author szh
 **/
public class DatabaseField {

    private String table;
    private String column;

    public DatabaseField() {

    }

    public DatabaseField(String table, String column) {
        this.table = table;
        this.column = column;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

}
