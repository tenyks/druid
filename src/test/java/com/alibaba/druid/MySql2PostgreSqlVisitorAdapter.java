package com.alibaba.druid;

import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;

/**
 * @author Maxwell.Lee
 * @version 3.8.1
 * @company Scho Techonlogy Co. Ltd
 * @date 2019/10/8 17:42
 */
public class MySql2PostgreSqlVisitorAdapter extends MySqlASTVisitorAdapter {

    @Override
    public boolean visit(MySqlShowCreateTableStatement x) {

        return super.visit(x);
    }

}
