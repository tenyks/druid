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


//        String sql = "ALTER TABLE t1 CONVERT TO CHARACTER SET 'UTF8';";
//        String sql = "ALTER TABLE t1 RENAME TO t1_a;";
//        String sql = "SELECT t1.org_id, COUNT(t1.id) AS u_count, GROUP_CONCAT(t2.code) AS code_grp, (SELECT COUNT(*) FROM user_session t WHERE t.user_id = t1.id) AS session_count FROM sm_user t1 JOIN (SELECT * FROM department t0 WHERE t0.org_id = t1.org_id) t2 ON t1.dept_id = t2.id WHERE t1.org_id=? GROUP BY t1.org_id HAVING count(t2.id) > 1 ORDER BY t1.org_id DESC ;";
//        String sql = "CREATE TABLE \"public\".\"summary_refresh_record\" (\"id\" int8 DEFAULT nextval('summary_refresh_record_id_seq'::regclass) NOT NULL,\"module_type\" varchar(50) COLLATE \"default\",\"module_id\" int8,\"refresh_time\" timestamp(0),\"state\" int2,\"remark\" varchar(2048) COLLATE \"default\",CONSTRAINT \"summary_refresh_record_pkey\" PRIMARY KEY (\"id\")) WITH (OIDS=FALSE);";
//        String sql = "CREATE TABLE `about_us` (   `id` bigint(20) NOT NULL AUTO_INCREMENT,   `org_id` bigint(20) NOT NULL,   `dept_id` bigint(20) DEFAULT NULL,   `org_ref_id` bigint(20) DEFAULT NULL,   `app_name` varchar(50) NOT NULL,   `app_name_en` varchar(50) DEFAULT NULL,   `app_explain` varchar(2000) DEFAULT NULL,   `app_explain_en` varchar(2000) DEFAULT NULL,   `app_service_tel` varchar(64) DEFAULT NULL,   `app_logo_url` varchar(255) DEFAULT NULL,   PRIMARY KEY (`id`),   KEY `idx_org_ref_id` (`org_ref_id`) USING BTREE,   KEY `idx_dept_id` (`dept_id`) USING BTREE ) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8;";
//        String sql = "SELECT * FROM a JOIN b on a.id = b.parent_id LEFT JOIN c on c.parent_id = b.id";
        String sql = "DROP TABLE a";
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
