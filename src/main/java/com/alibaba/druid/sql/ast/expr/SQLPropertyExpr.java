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
package com.alibaba.druid.sql.ast.expr;

import java.util.Collections;
import java.util.List;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLExprImpl;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateProcedureStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.util.FnvHash;

/**
 * 例如：select t.id from user t;
 */
public final class SQLPropertyExpr extends SQLExprImpl implements SQLName {
    /**
     * 归属，如例子中的't'；
     */
    private   SQLExpr             owner;

    /**
     * 名称，如例子中的'id'；
     */
    private   String              name;

    protected long                nameHashCod64;
    protected long                hashCode64;

    protected SQLColumnDefinition resolvedColumn;
    protected SQLObject           resolvedOwnerObject;

    public SQLPropertyExpr(String owner, String name){
        this(new SQLIdentifierExpr(owner), name);
    }

    public SQLPropertyExpr(SQLExpr owner, String name){
        setOwner(owner);
        this.name = name;
    }

    public SQLPropertyExpr(SQLExpr owner, String name, long nameHashCod64){
        setOwner(owner);
        this.name = name;
        this.nameHashCod64 = nameHashCod64;
    }

    public SQLPropertyExpr(){

    }

    public String getSimpleName() {
        return name;
    }

    public SQLExpr getOwner() {
        return this.owner;
    }

    public String getOwnernName() {
        if (owner instanceof SQLName) {
            return ((SQLName) owner).toString();
        }

        return null;
    }

    public void setOwner(SQLExpr owner) {
        if (owner != null) {
            owner.setParent(this);
        }

        if (parent instanceof SQLPropertyExpr) {
            SQLPropertyExpr propertyExpr = (SQLPropertyExpr) parent;
            propertyExpr.computeHashCode64();
        }

        this.owner = owner;
        this.hashCode64 = 0;
    }

    protected void computeHashCode64() {
        long hash;
        if (owner instanceof SQLName) {
            hash = ((SQLName) owner).hashCode64();

            hash ^= '.';
            hash *= FnvHash.PRIME;
        } else if (owner == null){
            hash = FnvHash.BASIC;
        } else {
            hash = FnvHash.fnv1a_64_lower(owner.toString());

            hash ^= '.';
            hash *= FnvHash.PRIME;
        }
        hash = FnvHash.hashCode64(hash, name);
        hashCode64 = hash;
    }

    public void setOwner(String owner) {
        this.setOwner(new SQLIdentifierExpr(owner));
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.hashCode64 = 0;
        this.nameHashCod64 = 0;

        if (parent instanceof SQLPropertyExpr) {
            SQLPropertyExpr propertyExpr = (SQLPropertyExpr) parent;
            propertyExpr.computeHashCode64();
        }
    }

    public void output(StringBuffer buf) {
        this.owner.output(buf);
        buf.append(".");
        buf.append(this.name);
    }

    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.owner);
        }

        visitor.endVisit(this);
    }

    @Override
    public List getChildren() {
        return Collections.singletonList(this.owner);
    }

    @Override
    public int hashCode() {
        long hash = hashCode64();
        return (int)(hash ^ (hash >>> 32));
    }

    public long hashCode64() {
        if (hashCode64 == 0) {
            computeHashCode64();
        }

        return hashCode64;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SQLPropertyExpr)) {
            return false;
        }
        SQLPropertyExpr other = (SQLPropertyExpr) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!owner.equals(other.owner)) {
            return false;
        }
        return true;
    }

    public SQLPropertyExpr clone() {
        SQLExpr owner_x = null;
        if (owner != null) {
            owner_x = owner.clone();
        }

        SQLPropertyExpr x = new SQLPropertyExpr(owner_x, name, nameHashCod64);

        x.hashCode64 = hashCode64;
        x.resolvedColumn = resolvedColumn;
        x.resolvedOwnerObject = resolvedOwnerObject;

        return x;
    }

    public boolean matchOwner(String alias) {
        if (owner instanceof SQLIdentifierExpr) {
            return ((SQLIdentifierExpr) owner).getName().equalsIgnoreCase(alias);
        }

        return false;
    }

    public long nameHashCode64() {
        if (nameHashCod64 == 0
                && name != null) {
            nameHashCod64 = FnvHash.hashCode64(name);
        }
        return nameHashCod64;
    }

    public String normalizedName() {

        String ownerName;
        if (owner instanceof SQLIdentifierExpr) {
            ownerName = ((SQLIdentifierExpr) owner).normalizedName();
        } else if (owner instanceof SQLPropertyExpr) {
            ownerName = ((SQLPropertyExpr) owner).normalizedName();
        } else {
            ownerName = owner.toString();
        }

        return ownerName + '.' + SQLUtils.normalize(name);
    }

    public SQLColumnDefinition getResolvedColumn() {
        return resolvedColumn;
    }

    public void setResolvedColumn(SQLColumnDefinition resolvedColumn) {
        this.resolvedColumn = resolvedColumn;
    }

    public SQLTableSource getResolvedTableSource() {
        if (resolvedOwnerObject instanceof SQLTableSource) {
            return (SQLTableSource) resolvedOwnerObject;
        }

        return null;
    }

    public void setResolvedTableSource(SQLTableSource resolvedTableSource) {
        this.resolvedOwnerObject = resolvedTableSource;
    }

    public void setResolvedProcedure(SQLCreateProcedureStatement stmt) {
        this.resolvedOwnerObject = stmt;
    }

    public void setResolvedOwnerObject(SQLObject resolvedOwnerObject) {
        this.resolvedOwnerObject = resolvedOwnerObject;
    }

    public SQLCreateProcedureStatement getResolvedProcudure() {
        if (this.resolvedOwnerObject instanceof SQLCreateProcedureStatement) {
            return (SQLCreateProcedureStatement) this.resolvedOwnerObject;
        }

        return null;
    }

    public SQLObject getResolvedOwnerObject() {
        return resolvedOwnerObject;
    }

    public SQLDataType computeDataType() {
        if (resolvedColumn != null) {
            return resolvedColumn.getDataType();
        }

        if (resolvedOwnerObject != null
                && resolvedOwnerObject instanceof SQLSubqueryTableSource) {
            SQLSelect select = ((SQLSubqueryTableSource) resolvedOwnerObject).getSelect();
            SQLSelectQueryBlock queryBlock = select.getFirstQueryBlock();
            if (queryBlock == null) {
                return null;
            }
            SQLSelectItem selectItem = queryBlock.findSelectItem(nameHashCode64());
            if (selectItem != null) {
                return selectItem.computeDataType();
            }
        }

        return null;
    }

    public boolean nameEquals(String name) {
        return SQLUtils.nameEquals(this.name, name);
    }

    public SQLPropertyExpr simplify() {
        String normalizedName = SQLUtils.normalize(name);
        SQLExpr normalizedOwner = this.owner;
        if (normalizedOwner instanceof SQLIdentifierExpr) {
            normalizedOwner = ((SQLIdentifierExpr) normalizedOwner).simplify();
        }

        if (normalizedName != name || normalizedOwner != owner) {
            return new SQLPropertyExpr(normalizedOwner, normalizedName, hashCode64);
        }

        return this;
    }

    public String toString() {
        if (owner == null) {
            return this.name;
        }

        return owner.toString() + '.' + name;
    }
}
