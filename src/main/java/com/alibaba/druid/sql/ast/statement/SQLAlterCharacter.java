/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.ast.statement;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObjectImpl;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

/**
 * ALTER TABLE命令；
 */
public class SQLAlterCharacter extends SQLObjectImpl implements SQLAlterTableItem {

    /**
     * 字符集；
     */
    private SQLExpr characterSet;

    /**
     * 排序；
     */
    private SQLExpr collate;

    public void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, characterSet);
            acceptChild(visitor, collate);
        }
        visitor.endVisit(this);
    }

    public SQLExpr getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(SQLExpr characterSet) {
        this.characterSet = characterSet;
    }

    public SQLExpr getCollate() {
        return collate;
    }

    public void setCollate(SQLExpr collate) {
        this.collate = collate;
    }

}
