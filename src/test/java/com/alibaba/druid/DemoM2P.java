package com.alibaba.druid;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.util.JdbcConstants;

import java.nio.CharBuffer;
import java.util.List;

/**
 * @author Maxwell.Lee
 * @version 3.8.1
 * @company Scho Techonlogy Co. Ltd
 * @date 2019/9/24 15:38
 */
public class DemoM2P {

    public static void main(String[] args) {
        CharBuffer output = CharBuffer.allocate(2048);


        String sql = "ALTER TABLE t1 CONVERT TO CHARACTER SET 'UTF8';";
//        String sql = "ALTER TABLE t1 RENAME TO t1_a;";
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        MySqlOutputVisitor v = new MySqlOutputVisitor(output);

//        String sql = "ALTER TABLE subject ALTER COLUMN Name TYPE varchar(256)";
//        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.POSTGRESQL);
//        PGOutputVisitor v = new PGOutputVisitor(output);

//        v.visit((SQLAlterTableStatement)stmtList.get(0));

        output.rewind();
        System.out.println(output.toString());
    }

}
