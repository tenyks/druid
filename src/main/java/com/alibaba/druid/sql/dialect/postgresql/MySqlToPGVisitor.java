package com.alibaba.druid.sql.dialect.postgresql;

import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGInsertStatement;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;
import com.alibaba.druid.util.JdbcConstants;

import javax.transaction.NotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Maxwell.Lee
 * @version 3.8.1
 * @company Scho Techonlogy Co. Ltd
 * @date 2019/10/10 14:46
 */
public class MySqlToPGVisitor extends SQLASTVisitorAdapter {

    private Stack<ComponentStack>   stack;

    private ComponentStack  current;

    public MySqlToPGVisitor() {
        push();
    }

    private ComponentStack push() {
        current = new ComponentStack();
        stack.push(current);

        return current;
    }

    private ComponentStack pop(SQLObject targetObj, SQLObject newObj) {
        current = stack.pop();
        current.put(targetObj, newObj);

        return current;
    }

    @Override
    public void endVisit(SQLAllColumnExpr x) {
        current.put(x, x.clone());
    }

    @Override
    public boolean visit(SQLBetweenExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLBetweenExpr x) {
        SQLExpr testExpr = (SQLExpr)current.get(x.getTestExpr());
        SQLExpr beginExpr = (SQLExpr)current.get(x.getBeginExpr());
        SQLExpr endExpr = (SQLExpr)current.get(x.getEndExpr());

        SQLBetweenExpr newObj = new SQLBetweenExpr(testExpr, x.isNot(), beginExpr, endExpr);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLBinaryOpExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLBinaryOpExpr x) {
        SQLExpr left = (SQLExpr)current.get(x.getLeft());
        SQLBinaryOperator operator = x.getOperator();
        SQLExpr right = (SQLExpr) current.get(x.getRight());

        SQLBinaryOpExpr newObj= new SQLBinaryOpExpr(left, operator, right, JdbcConstants.POSTGRESQL);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLCaseExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCaseExpr x) {
        SQLCaseExpr newObj = new SQLCaseExpr();
        newObj.setElseExpr((SQLExpr) current.get(x.getValueExpr()));
        newObj.setElseExpr((SQLExpr) current.get(x.getElseExpr()));

        if (x.getItems() != null) {
            for (SQLCaseExpr.Item item : x.getItems()) {
                newObj.addItem((SQLCaseExpr.Item)current.get(item));
            }
        }

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLCaseExpr.Item x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCaseExpr.Item x) {
        SQLExpr conditionExpr = (SQLExpr)current.get(x.getConditionExpr());
        SQLExpr valueExpr = (SQLExpr)current.get(x.getValueExpr());

        SQLCaseExpr.Item newObj = new SQLCaseExpr.Item(conditionExpr, valueExpr);
        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLCaseStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCaseStatement x) {

        SQLCaseStatement newObj = new SQLCaseStatement();
        newObj.setValueExpr((SQLExpr) current.get(x.getValueExpr()));
        newObj.setAfterSemi(x.isAfterSemi());
        newObj.setDbType(JdbcConstants.POSTGRESQL);
        newObj.setHeadHints(x.getHeadHintsDirect());

        if (x.getElseStatements() != null) {
            for (SQLStatement stmt : x.getElseStatements()) {
                newObj.getElseStatements().add(stmt.clone());
            }
        }
        if (x.getItems() != null) {
            for (SQLCaseStatement.Item item : x.getItems()) {
                newObj.addItem(item);
            }
        }

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLCaseStatement.Item x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCaseStatement.Item x) {
        SQLExpr conditionExpr = (SQLExpr)current.get(x.getConditionExpr());
        SQLStatement statement = (SQLStatement) current.get(x.getStatement());

        SQLCaseStatement.Item newObj = new SQLCaseStatement.Item(conditionExpr, statement);

        pop(x, newObj);
    }

    @Override
    public void endVisit(SQLCharExpr x) {
        current.put(x, x.clone());
    }

    @Override
    public void endVisit(SQLIdentifierExpr x) {
        current.put(x, x.clone());
    }

    @Override
    public boolean visit(SQLInListExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLInListExpr x) {
        SQLInListExpr newObj = new SQLInListExpr((SQLExpr) current.get(x.getExpr()), x.isNot());

        if (x.getTargetList() != null && x.getTargetList().size() > 0) {
            for (SQLExpr expr : x.getTargetList()) {
                newObj.getTargetList().add((SQLExpr)current.get(expr));
            }
        }

        pop(x, newObj);
    }

    @Override
    public void endVisit(SQLIntegerExpr x) {
        current.put(x, x.clone());
    }

    @Override
    public boolean visit(SQLExistsExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLExistsExpr x) {
        SQLSelect subQuery = (SQLSelect)current.get(x.getSubQuery());

        SQLExistsExpr newObj = new SQLExistsExpr(subQuery, x.isNot());

        pop(x, newObj);
    }

    @Override
    public void endVisit(SQLNCharExpr x) {
        current.put(x, x.clone());
    }

    @Override
    public boolean visit(SQLNotExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLNotExpr x) {
        SQLNotExpr newObj = new SQLNotExpr((SQLExpr) current.get(x.getExpr()));

        pop(x, newObj);
    }

    @Override
    public void endVisit(SQLNullExpr x) {
        current.put(x, x.clone());
    }

    @Override
    public void endVisit(SQLNumberExpr x) {
        pop(x, x.clone());
    }


    @Override
    public boolean visit(SQLPropertyExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLPropertyExpr x) {
        SQLPropertyExpr newObj = new SQLPropertyExpr((SQLExpr)current.get(x.getOwner()), x.getName());

        pop(x, newObj);
    }


    @Override
    public boolean visit(SQLSelectGroupByClause x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSelectGroupByClause x) {
        SQLSelectGroupByClause newObj = new SQLSelectGroupByClause();

        newObj.setHaving((SQLExpr)current.get(x.getHaving()));
        newObj.setWithCube(x.isWithCube());
        newObj.setWithRollUp(x.isWithRollUp());

        if (x.getItems() != null) {
            for (SQLExpr item : x.getItems()) {
                newObj.addItem((SQLExpr)current.get(item));
            }
        }

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLSelectItem x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSelectItem x) {
        SQLSelectItem newObj = new SQLSelectItem((SQLExpr) current.get(x.getExpr()), x.getAlias(), x.isConnectByRoot());

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLSelectStatement astNode) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSelectStatement x) {
        SQLSelectStatement newObj = new SQLSelectStatement((SQLSelect) current.get(x.getSelect()), JdbcConstants.POSTGRESQL);

        pop(x, newObj);
    }

    @Override
    public void preVisit(SQLObject x) {

    }

    @Override
    public void postVisit(SQLObject x) {

    }

    @Override
    public boolean visit(SQLCastExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCastExpr x) {
        SQLCastExpr newObj = new SQLCastExpr();

        newObj.setExpr((SQLExpr) current.get(x.getExpr()));
        newObj.setDataType((SQLDataType) current.get(x.getDataType()));
    }

    @Override
    public boolean visit(SQLAggregateExpr astNode) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAggregateExpr x) {
        SQLAggregateExpr newObj = new SQLAggregateExpr(x.getMethodName(), x.getOption());

        if (x.getFilter() != null) {
            x.accept(this);
        }

        newObj.setFilter((SQLExpr)current.get(x.getFilter()));

        if (x.getArguments() != null) {
            for (SQLExpr arg : x.getArguments()) {
                newObj.getArguments().add((SQLExpr) current.get(arg));
            }
        }

        newObj.setKeep((SQLKeep) current.get(x.getKeep()));
        newObj.setOver((SQLOver) current.get(x.getOver()));
        newObj.setOverRef((SQLName) current.get(x.getOverRef()));
        newObj.setWithinGroup((SQLOrderBy) current.get(x.getWithinGroup()));
        newObj.setIgnoreNulls(x.isIgnoreNulls());

        pop(x, newObj);
    }

    @Override
    public void endVisit(SQLVariantRefExpr x) {
        current.put(x, x.clone());
    }

    @Override
    public boolean visit(SQLQueryExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLQueryExpr x) {
        SQLQueryExpr newObj = new SQLQueryExpr((SQLSelect) current.get(x.getSubQuery()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLUnaryExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLUnaryExpr x) {
        SQLUnaryExpr newObj = new SQLUnaryExpr(x.getOperator(), (SQLExpr) current.get(x.getExpr()));

        pop(x, newObj);
    }

    @Override
    public void endVisit(SQLHexExpr x) {
        pop(x, x.clone());
    }

    @Override
    public boolean visit(SQLSelect x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSelect x) {
        SQLSelect newObj = new SQLSelect();

        newObj.setWithSubQuery((SQLWithSubqueryClause) current.get(x.getWithSubQuery()));
        newObj.setQuery((SQLSelectQuery) current.get(x.getQuery()));
        newObj.setOrderBy((SQLOrderBy) current.get(x.getOrderBy()));
        if (x.getHintsSize() > 0) {
            for (SQLHint hint : x.getHints()) {
                newObj.getHints().add((SQLHint) current.get(hint));
            }
        }
        newObj.setRestriction((SQLObject) current.get(x.getRestriction()));
        newObj.setForBrowse(x.isForBrowse());
        if (x.getForXmlOptionsSize() > 0) {
            newObj.getForXmlOptions().addAll(x.getForXmlOptions());
        }

        newObj.setXmlPath(x.getXmlPath().clone());
        newObj.setRowCount((SQLExpr)x.getRowCount());
        newObj.setOffset((SQLExpr)x.getOffset());

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLSelectQueryBlock x) {
        push();

        if (x.getWindows() != null && x.getWindows().size() > 0) {
            for (SQLWindow item : x.getWindows()) {
                item.accept(this);
            }
        }
        if (x.getForUpdateOfSize() > 0) {
            for (SQLExpr item : x.getForUpdateOf()) {
                item.accept(this);
            }
        }
        if (x.getHintsSize() > 0) {
            for (SQLCommentHint item : x.getHints()) {
                item.accept(this);
            }
        }
        if (x.getOrderBySiblings() != null) {
            x.getOrderBySiblings().accept(this);
        }

        return true;
    }

    @Override
    public void endVisit(SQLSelectQueryBlock x) {
        SQLSelectQueryBlock newObj = new SQLSelectQueryBlock();

        newObj.setDbType(JdbcConstants.POSTGRESQL);

        newObj.setBracket(x.isBracket());
        newObj.setDistionOption(x.getDistionOption());

        if (x.getSelectList() != null && x.getSelectList().size() > 0) {
            for (SQLSelectItem item : x.getSelectList()) {
                newObj.addSelectItem((SQLSelectItem) current.get(item));
            }
        }
        newObj.setFrom((SQLTableSource) current.get(x.getFrom()));
        newObj.setInto((SQLExprTableSource)current.get(x.getInto()));
        newObj.setWhere((SQLExpr) current.get(x.getWhere()));
        newObj.setStartWith((SQLExpr)current.get(x.getStartWith()));
        newObj.setConnectBy((SQLExpr)current.get(x.getConnectBy()));

        newObj.setPrior(x.isPrior());
        newObj.setNoCycle(x.isNoCycle());

        if (x.getOrderBySiblings() != null) {
            newObj.setOrderBySiblings((SQLOrderBy) current.get(x.getOrderBySiblings()));
        }
        newObj.setGroupBy((SQLSelectGroupByClause)current.get(x.getGroupBy()));
        if (x.getWindows() != null && x.getWindows().size() > 0) {
            for (SQLWindow item : x.getWindows()) {
                newObj.addWindow((SQLWindow) current.get(item));
            }
        }
        newObj.setOrderBy((SQLOrderBy) current.get(x.getOrderBy()));

        newObj.setParenthesized(x.isParenthesized());
        newObj.setForUpdate(x.isForUpdate());
        newObj.setNoWait(x.isNoWait());

        newObj.setWaitTime((SQLExpr)current.get(x.getWaitTime()));
        newObj.setLimit((SQLLimit) current.get(x.getLimit()));

        if (x.getForUpdateOfSize() > 0) {
            for (SQLExpr item : x.getForUpdateOf()) {
                newObj.getForUpdateOf().add((SQLExpr) current.get(item));
            }
        }
        if (x.getDistributeBy() != null && x.getDistributeBy().size() > 0) {
            for (SQLExpr item : x.getDistributeBy()) {
                newObj.getDistributeBy().add((SQLExpr) current.get(item));
            }
        }
        if (x.getSortBy() != null && x.getSortBy().size() > 0) {
            for (SQLSelectOrderByItem item : x.getSortBy()) {
                newObj.addSortBy((SQLSelectOrderByItem) current.get(item));
            }
        }
        if (x.getHintsSize() > 0) {
            for (SQLCommentHint item : x.getHints()) {
                newObj.getHints().add((SQLCommentHint) current.get(item));
            }
        }

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLExprTableSource x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLExprTableSource x) {
        SQLExprTableSource newObj = x.clone();

        newObj.setExpr((SQLExpr) current.get(x.getExpr()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLOrderBy x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLOrderBy x) {
        SQLOrderBy newObj = new SQLOrderBy();

        if (x.getItems() != null && x.getItems().size() > 0) {
            for (SQLSelectOrderByItem item : x.getItems()) {
                newObj.addItem((SQLSelectOrderByItem) current.get(item));
            }
        }

        newObj.setSibings(x.isSibings());
    }

    @Override
    public boolean visit(SQLSelectOrderByItem x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSelectOrderByItem x) {
        SQLSelectOrderByItem newObj = new SQLSelectOrderByItem();

        newObj.setExpr((SQLExpr) current.get(x.getExpr()));
        newObj.setCollate(x.getCollate());
        newObj.setType(x.getType());
        newObj.setNullsOrderType(x.getNullsOrderType());

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLDropTableStatement x) {
        push();

        return true;
    }

    private List<SQLCommentHint> cloneCommentHints(List<SQLCommentHint> originHints) {
        if (originHints == null || originHints.isEmpty()) return null;

        return new ArrayList<SQLCommentHint>(originHints);
    }

    private List<SQLHint> cloneHints(List<SQLHint> src) {
        if (src == null || src.isEmpty()) return null;

        return new ArrayList<SQLHint>(src);
    }

    private void copyTo(SQLStatement src, SQLStatement dst) {
        dst.setHeadHints(cloneCommentHints(src.getHeadHintsDirect()));
        dst.setAfterSemi(src.isAfterSemi());
    }

    @Override
    public void endVisit(SQLDropTableStatement x) {
        SQLDropTableStatement newObj = new SQLDropTableStatement(JdbcConstants.POSTGRESQL);

        if (x.getTableSources() != null && x.getTableSources().size() > 0) {
            for (SQLExprTableSource item : x.getTableSources()) {
                newObj.addTableSource((SQLExprTableSource)current.get(item));
            }
        }

        newObj.setHints(cloneCommentHints(x.getHints()));
        newObj.setPurge(x.isPurge());
        newObj.setCascade(x.isCascade());
        newObj.setIfExists(x.isIfExists());
        newObj.setTemporary(x.isTemporary());

        copyTo(x, newObj);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLCreateTableStatement x) {
        push();
        return true;
    }

    private Map<String, SQLObject>  convertTableOptions(Map<String, SQLObject> options) {
        if (options == null || options.isEmpty()) return null;

        return null;
    }

    private void convertAndCopyAttributes(Map<String, Object> src, Map<String, Object> dst) {
        if (src == null || src.isEmpty()) return ;

        for (String key : src.keySet()) {
            dst.put(key, src.get(key));
        }
    }

    @Override
    public void endVisit(SQLCreateTableStatement x) {
        MySqlCreateTableStatement xx = (MySqlCreateTableStatement) x;

        SQLCreateTableStatement newObj = new SQLCreateTableStatement(JdbcConstants.POSTGRESQL);

        newObj.setIfNotExists(x.isIfNotExists());
        newObj.setType(x.getType());

        newObj.setTableSource((SQLExprTableSource)current.get(x.getTableSource()));

        if (x.getTableElementList() != null && x.getTableElementList().size() > 0) {
            for (SQLTableElement item : x.getTableElementList()) {
                newObj.getTableElementList().add((SQLTableElement) current.get(item));
            }
        }

        newObj.setInherits((SQLExprTableSource)current.get(x.getInherits()));
        newObj.setSelect((SQLSelect)current.get(x.getSelect()));
        newObj.setComment((SQLExpr) current.get(x.getComment()));
        newObj.setLike((SQLExprTableSource) current.get(x.getLike()));

        newObj.setCompress(x.getCompress());
        newObj.setLogging(x.getLogging());
        newObj.setTablespace((SQLName)current.get(x.getTablespace()));
        newObj.setPartitioning((SQLPartitionBy)current.get(x.getPartitioning()));
        newObj.setStoredAs((SQLName)current.get(x.getStoredAs()));

        newObj.setOnCommitPreserveRows(x.isOnCommitPreserveRows());

        Map<String, SQLObject> options = convertTableOptions(xx.getTableOptions());
        if (options != null) newObj.getTableOptions().putAll(options);

        copyTo(xx, newObj);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLColumnDefinition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLColumnDefinition x) {
        SQLColumnDefinition newObj = new SQLColumnDefinition();

        newObj.setDbType(JdbcConstants.POSTGRESQL);

        newObj.setName(x.getName());
        newObj.setDataType(x.getDataType());
        newObj.setDefaultExpr((SQLExpr)current.get(x.getDefaultExpr()));

        if (x.getConstraints() != null && x.getConstraints().size() > 0) {
            for (SQLColumnConstraint item : x.getConstraints()) {
                newObj.addConstraint((SQLColumnConstraint)current.get(item));
            }
        }

        newObj.setComment(x.getComment().clone());
        newObj.setEnable(x.getEnable());
        newObj.setValidate(x.getValidate());
        newObj.setRely(x.getRely());

        newObj.setAutoIncrement(x.isAutoIncrement());
        if (x.getOnUpdate() != null) {
            newObj.setOnUpdate(x.getOnUpdate().clone());
        }
        if (x.getStorage() != null) {
            newObj.setStorage(x.getStorage().clone());
        }
        if (x.getCharsetExpr() != null) {
            newObj.setCharsetExpr(x.getCharsetExpr().clone());
        }
        if (x.getAsExpr() != null) {
            newObj.setAsExpr(x.getAsExpr().clone());
        }
        newObj.setStored(x.isStored());
        newObj.setVirtual(x.isVirtual());

        if (x.getIdentity() != null) {
            newObj.setIdentity(x.getIdentity().clone());
        }
        if (x.getGeneratedAlwaysAs() != null) {
            newObj.setGeneratedAlwaysAs(x.getGeneratedAlwaysAs().clone());
        }

        pop(x, newObj);

    }

    @Override
    public boolean visit(SQLColumnDefinition.Identity x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLColumnDefinition.Identity x) {
        SQLColumnDefinition.Identity newObj = new SQLColumnDefinition.Identity();

        newObj.setSeed(x.getSeed());
        newObj.setIncrement(x.getIncrement());
        newObj.setNotForReplication(x.isNotForReplication());

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLDataType x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDataType x) {
        SQLDataType newObj = x.clone();

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLCharacterDataType x) {
        push();

        if (x.getArguments() != null && x.getArguments().size() > 0) {
            for (SQLExpr item : x.getArguments()) {
                item.accept(this);
            }
        }

        return true;
    }

    @Override
    public void endVisit(SQLCharacterDataType x) {
        SQLCharacterDataType newObj = new SQLCharacterDataType(x.getName());

        newObj.setCharSetName(x.getCharSetName());
        newObj.setCollate(x.getCollate());
        newObj.setCharType(x.getCharType());
        newObj.setHasBinary(x.isHasBinary());

        newObj.setHints(cloneCommentHints(x.getHints()));

        if (x.getArguments() != null && x.getArguments().size() > 0) {
            for (SQLExpr item : x.getArguments()) {
                newObj.addArgument((SQLExpr)current.get(item));
            }
        }

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLDeleteStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDeleteStatement x) {
        SQLDeleteStatement newObj = new SQLDeleteStatement(JdbcConstants.POSTGRESQL);

        newObj.setWith((SQLWithSubqueryClause)current.get(x.getWith()));
        newObj.setTableSource((SQLTableSource)current.get(x.getTableSource()));
        newObj.setWhere((SQLExpr)current.get(x.getWhere()));

        if (x.getFrom() != null) newObj.setFrom(x.getFrom().clone());
        if (x.getUsing() != null) newObj.setUsing(x.getUsing());

        newObj.setOnly(x.isOnly());

        copyTo(x, newObj);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLCurrentOfCursorExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCurrentOfCursorExpr x) {
        SQLCurrentOfCursorExpr newObj = new SQLCurrentOfCursorExpr((SQLName)current.get(x.getCursorName()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLInsertStatement x) {
        push();

        if (x.getWith() != null) {
            x.getWith().accept(this);
        }

        return true;
    }

    @Override
    public void endVisit(SQLInsertStatement x) {
        PGInsertStatement newObj = new PGInsertStatement();
        newObj.setDbType(JdbcConstants.POSTGRESQL);

        newObj.setWith((SQLWithSubqueryClause) current.get(x.getWith()));
        newObj.setTableSource((SQLExprTableSource) current.get(x.getTableSource()));
        if (x.getColumns() != null && x.getColumns().size() > 0) {
            for (SQLExpr item : x.getColumns()) {
                newObj.addColumn((SQLExpr)current.get(item));
            }
        }
        if (x.getValuesList() != null && x.getValuesList().size() > 0) {
            for (SQLInsertStatement.ValuesClause item : x.getValuesList()) {
                newObj.addValueCause((SQLInsertStatement.ValuesClause)current.get(item));
            }
        }
        newObj.setQuery((SQLSelect) current.get(x.getQuery()));
        newObj.setUpsert(x.isUpsert());
        newObj.setAfterSemi(x.isAfterSemi());

        newObj.setHeadHints(cloneCommentHints(x.getHeadHintsDirect()));

        copyTo(x, newObj);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLInsertStatement.ValuesClause x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLInsertStatement.ValuesClause x) {
        SQLInsertStatement.ValuesClause newObj = new SQLInsertStatement.ValuesClause();

        if (x.getValues() != null && x.getValues().size() > 0) {
            for (SQLExpr item : x.getValues()) {
                newObj.addValue((SQLExpr)current.get(item));
            }
        }

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLUpdateSetItem x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLUpdateSetItem x) {
        SQLUpdateSetItem newObj = new SQLUpdateSetItem();

        newObj.setColumn((SQLExpr)current.get(x.getColumn()));
        newObj.setValue((SQLExpr)current.get(x.getValue()));

        convertAndCopyAttributes(x.getAttributes(), newObj.getAttributes());
    }

    @Override
    public boolean visit(SQLUpdateStatement x) {
        push();

        if (x.getWith() != null) x.getWith().accept(this);

        return true;
    }

    @Override
    public void endVisit(SQLUpdateStatement x) {
        SQLUpdateStatement newObj = new SQLUpdateStatement(JdbcConstants.POSTGRESQL);

        newObj.setWith((SQLWithSubqueryClause) current.get(x.getWith()));
        newObj.setWhere((SQLExpr)current.get(x.getWhere()));
        newObj.setFrom((SQLTableSource)current.get(x.getFrom()));
        newObj.setTableSource((SQLTableSource)current.get(x.getTableSource()));

        if (x.getItems() != null && x.getItems().size() > 0) {
            for (SQLUpdateSetItem item : x.getItems()) {
                newObj.addItem((SQLUpdateSetItem)current.get(item));
            }
        }
        if (x.getReturning() != null && x.getReturning().size() > 0) {
            for (SQLExpr item : x.getReturning()) {
                newObj.addItem((SQLUpdateSetItem)current.get(item));
            }
        }

        newObj.setHints(cloneHints(x.getHints()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLCreateViewStatement x) {
//        push();
//        return true;
        throw new IllegalStateException("Not Supported");
    }

    @Override
    public void endVisit(SQLCreateViewStatement x) {
        throw new IllegalStateException("Not Supported");
    }

    @Override
    public boolean visit(SQLCreateViewStatement.Column x) {
//        push();
//        return true;
        throw new IllegalStateException("Not Supported");
    }

    @Override
    public void endVisit(SQLCreateViewStatement.Column x) {
        throw new IllegalStateException("Not Supported");
    }

    @Override
    public boolean visit(SQLNotNullConstraint x) {
        return true;
    }

    @Override
    public void endVisit(SQLNotNullConstraint x) {
        current.put(x, x.clone());
    }


    @Override
    public boolean visit(SQLMethodInvokeExpr x) {
//        push();
//        return true;
        throw new IllegalStateException("Not Supported");
    }


    @Override
    public void endVisit(SQLMethodInvokeExpr x) {
        throw new IllegalStateException("Not Supported");
    }

    @Override
    public boolean visit(SQLUnionQuery x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLUnionQuery x) {
        SQLUnionQuery newObj = new SQLUnionQuery();
        newObj.setDbType(JdbcConstants.POSTGRESQL);

        newObj.setBracket(x.isBracket());


        newObj.setLeft((SQLSelectQuery) current.get(x.getLeft()));
        newObj.setOperator(x.getOperator());
        newObj.setRight((SQLSelectQuery) current.get(x.getRight()));
        newObj.setOrderBy((SQLOrderBy) current.get(x.getOrderBy()));
        newObj.setLimit((SQLLimit) current.get(x.getLimit()));

        convertAndCopyAttributes(x.getAttributes(), newObj.getAttributes());

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLSetStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSetStatement x) {
        SQLSetStatement newObj = new SQLSetStatement(JdbcConstants.POSTGRESQL);

        newObj.setOption(x.getOption());
        if (x.getItems() != null && x.getItems().size() > 0) {
            for (SQLAssignItem item : x.getItems()) {
                newObj.getItems().add((SQLAssignItem) current.get(item));
            }
        }

        x.setHints(cloneCommentHints(x.getHints()));

        copyTo(x, newObj);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLAssignItem x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAssignItem x) {
        SQLAssignItem newObj = new SQLAssignItem();

        newObj.setTarget((SQLExpr) current.get(x.getTarget()));
        newObj.setValue((SQLExpr) current.get(x.getValue()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLCallStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCallStatement x) {
        SQLCallStatement newObj = new SQLCallStatement(JdbcConstants.POSTGRESQL);

        newObj.setProcedureName((SQLName)current.get(x.getProcedureName()));
        newObj.setBrace(x.isBrace());
        newObj.setOutParameter((SQLVariantRefExpr)current.get(x.getOutParameter()));
        if (x.getParameters() != null && x.getParameters().size() > 0) {
            for (SQLExpr item : x.getParameters()) {
                newObj.getParameters().add((SQLExpr) current.get(item));
            }
        }

        copyTo(x, newObj);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLJoinTableSource x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLJoinTableSource x) {
        SQLJoinTableSource newObj = new SQLJoinTableSource();

        newObj.setAlias(x.getAlias());

        newObj.setLeft((SQLTableSource) current.get(x.getLeft()));
        newObj.setRight((SQLTableSource) current.get(x.getRight()));
        newObj.setJoinType(x.getJoinType());
        newObj.setCondition((SQLExpr) current.get(x.getCondition()));
        if (x.getUsing() != null && x.getUsing().size() > 0) {
            for (SQLExpr item : x.getUsing()) {
                x.getUsing().add((SQLExpr) current.get(item));
            }
        }

        newObj.setHints(cloneHints(x.getHints()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLSomeExpr x) {
        return true;
    }

    @Override
    public void endVisit(SQLSomeExpr x) {
        SQLSomeExpr newObj = new SQLSomeExpr((SQLSelect) current.get(x.getSubQuery()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLAnyExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAnyExpr x) {
        SQLAnyExpr newObj = new SQLAnyExpr((SQLSelect) current.get(x.getSubQuery()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLAllExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAllExpr x) {
        SQLAllExpr newObj = new SQLAllExpr((SQLSelect) current.get(x.getSubQuery()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLInSubQueryExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLInSubQueryExpr x) {
        SQLInSubQueryExpr newObj = new SQLInSubQueryExpr();

        newObj.setSubQuery((SQLSelect) current.get(x.getSubQuery()));
        newObj.setNot(x.isNot());
        newObj.setExpr((SQLExpr) current.get(x.getExpr()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLListExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLListExpr x) {
        SQLListExpr newObj = new SQLListExpr();

        if (x.getItems() != null && x.getItems().size() > 0) {
            for (SQLExpr item : x.getItems()) {
                newObj.addItem((SQLExpr) current.get(item));
            }
        }

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLSubqueryTableSource x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSubqueryTableSource x) {
        SQLSubqueryTableSource newObj = new SQLSubqueryTableSource(x.getAlias());

        newObj.setSelect((SQLSelect) current.get(x.getSelect()));
        newObj.setHints(cloneHints(x.getHints()));

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLTruncateStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLTruncateStatement x) {
        SQLTruncateStatement newObj = new SQLTruncateStatement(x.getDbType());

        if (x.getTableSources() != null && x.getTableSources().size() > 0) {
            for (SQLExprTableSource item : x.getTableSources()) {
                newObj.getTableSources().add((SQLExprTableSource) current.get(item));
            }
        }
        newObj.setPurgeSnapshotLog(x.isPurgeSnapshotLog());
        newObj.setOnly(x.isOnly());
        newObj.setRestartIdentity(x.getRestartIdentity());
        newObj.setCascade(x.getCascade());
        newObj.setDropStorage(x.isDropStorage());
        newObj.setReuseStorage(x.isReuseStorage());
        newObj.setImmediate(x.isImmediate());
        newObj.setIgnoreDeleteTriggers(x.isIgnoreDeleteTriggers());
        newObj.setRestrictWhenDeleteTriggers(x.isRestrictWhenDeleteTriggers());
        newObj.setContinueIdentity(x.isContinueIdentity());

        copyTo(x, newObj);

        pop(x, newObj);

    }

    @Override
    public boolean visit(SQLDefaultExpr x) {
        return true;
    }

    @Override
    public void endVisit(SQLDefaultExpr x) {
        current.put(x, x.clone());
    }

    @Override
    public boolean visit(SQLCommentStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCommentStatement x) {
        SQLCommentStatement newObj = new SQLCommentStatement();
        newObj.setDbType(JdbcConstants.POSTGRESQL);

        newObj.setOn((SQLExprTableSource) current.get(x.getOn()));
        newObj.setComment((SQLExpr) current.get(x.getComment()));
        newObj.setType(x.getType());

        copyTo(x, newObj);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLUseStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLUseStatement x) {
        SQLUseStatement newObj = new SQLUseStatement();
        newObj.setDbType(JdbcConstants.POSTGRESQL);

        newObj.setDatabase((SQLName) current.get(x.getDatabase()));

        copyTo(x, newObj);

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLAlterTableAddColumn x) {
        push();

        if (x.getAfterColumn() != null) x.getAfterColumn().accept(this);
        if (x.getFirstColumn() != null) x.getFirstColumn().accept(this);

        return true;
    }

    @Override
    public void endVisit(SQLAlterTableAddColumn x) {
        SQLAlterTableAddColumn newObj = new SQLAlterTableAddColumn();

        if (x.getColumns() != null && x.getColumns().size() > 0) {
            for (SQLColumnDefinition item : x.getColumns()) {
                newObj.addColumn((SQLColumnDefinition) current.get(item));
            }
        }

        newObj.setAfterColumn((SQLName)current.get(x.getAfterColumn()));
        newObj.setFirstColumn((SQLName) current.get(x.getFirstColumn()));
        newObj.setFirst(x.isFirst());

        pop(x, newObj);
    }

    @Override
    public boolean visit(SQLAlterTableDropColumnItem x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropColumnItem x) {

    }

    @Override
    public boolean visit(SQLAlterTableDropIndex x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropIndex x) {

    }

    @Override
    public boolean visit(SQLDropIndexStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropIndexStatement x) {

    }

    @Override
    public boolean visit(SQLDropViewStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropViewStatement x) {

    }

    @Override
    public boolean visit(SQLSavePointStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSavePointStatement x) {

    }

    @Override
    public boolean visit(SQLRollbackStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLRollbackStatement x) {

    }

    @Override
    public boolean visit(SQLReleaseSavePointStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLReleaseSavePointStatement x) {

    }

    @Override
    public void endVisit(SQLCommentHint x) {

    }

    @Override
    public boolean visit(SQLCommentHint x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCreateDatabaseStatement x) {

    }

    @Override
    public boolean visit(SQLCreateDatabaseStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLOver x) {

    }

    @Override
    public boolean visit(SQLOver x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLKeep x) {

    }

    @Override
    public boolean visit(SQLKeep x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLColumnPrimaryKey x) {

    }

    @Override
    public boolean visit(SQLColumnPrimaryKey x) {
        push();
        return true;
    }

    @Override
    public boolean visit(SQLColumnUniqueKey x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLColumnUniqueKey x) {

    }

    @Override
    public void endVisit(SQLWithSubqueryClause x) {

    }

    @Override
    public boolean visit(SQLWithSubqueryClause x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLWithSubqueryClause.Entry x) {

    }

    @Override
    public boolean visit(SQLWithSubqueryClause.Entry x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableAlterColumn x) {

    }

    @Override
    public boolean visit(SQLAlterTableAlterColumn x) {
        push();
        return true;
    }

    @Override
    public boolean visit(SQLCheck x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCheck x) {

    }

    @Override
    public boolean visit(SQLAlterTableDropForeignKey x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropForeignKey x) {

    }

    @Override
    public boolean visit(SQLAlterTableDropPrimaryKey x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropPrimaryKey x) {

    }

    @Override
    public boolean visit(SQLAlterTableDisableKeys x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDisableKeys x) {

    }

    @Override
    public boolean visit(SQLAlterTableEnableKeys x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableEnableKeys x) {

    }

    @Override
    public boolean visit(SQLAlterTableStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableStatement x) {

    }

    @Override
    public boolean visit(SQLAlterTableDisableConstraint x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDisableConstraint x) {

    }

    @Override
    public boolean visit(SQLAlterTableEnableConstraint x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableEnableConstraint x) {

    }

    @Override
    public boolean visit(SQLColumnCheck x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLColumnCheck x) {

    }

    @Override
    public boolean visit(SQLExprHint x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLExprHint x) {

    }

    @Override
    public boolean visit(SQLAlterTableDropConstraint x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropConstraint x) {

    }

    @Override
    public boolean visit(SQLUnique x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLUnique x) {

    }

    @Override
    public boolean visit(SQLPrimaryKeyImpl x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLPrimaryKeyImpl x) {

    }

    @Override
    public boolean visit(SQLCreateIndexStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCreateIndexStatement x) {

    }

    @Override
    public boolean visit(SQLAlterTableRenameColumn x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableRenameColumn x) {

    }

    @Override
    public boolean visit(SQLColumnReference x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLColumnReference x) {

    }

    @Override
    public boolean visit(SQLForeignKeyImpl x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLForeignKeyImpl x) {

    }

    @Override
    public boolean visit(SQLDropSequenceStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropSequenceStatement x) {

    }

    @Override
    public boolean visit(SQLDropTriggerStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropTriggerStatement x) {

    }

    @Override
    public void endVisit(SQLDropUserStatement x) {

    }

    @Override
    public boolean visit(SQLDropUserStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLExplainStatement x) {

    }

    @Override
    public boolean visit(SQLExplainStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLGrantStatement x) {

    }

    @Override
    public boolean visit(SQLGrantStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropDatabaseStatement x) {

    }

    @Override
    public boolean visit(SQLDropDatabaseStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableAddIndex x) {

    }

    @Override
    public boolean visit(SQLAlterTableAddIndex x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableAddConstraint x) {

    }

    @Override
    public boolean visit(SQLAlterTableAddConstraint x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCreateTriggerStatement x) {

    }

    @Override
    public boolean visit(SQLCreateTriggerStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropFunctionStatement x) {

    }

    @Override
    public boolean visit(SQLDropFunctionStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropTableSpaceStatement x) {

    }

    @Override
    public boolean visit(SQLDropTableSpaceStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropProcedureStatement x) {

    }

    @Override
    public boolean visit(SQLDropProcedureStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLBooleanExpr x) {

    }

    @Override
    public boolean visit(SQLBooleanExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLUnionQueryTableSource x) {

    }

    @Override
    public boolean visit(SQLUnionQueryTableSource x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLTimestampExpr x) {

    }

    @Override
    public boolean visit(SQLTimestampExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLRevokeStatement x) {

    }

    @Override
    public boolean visit(SQLRevokeStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLBinaryExpr x) {

    }

    @Override
    public boolean visit(SQLBinaryExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableRename x) {

    }

    @Override
    public boolean visit(SQLAlterTableRename x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterViewRenameStatement x) {

    }

    @Override
    public boolean visit(SQLAlterViewRenameStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLShowTablesStatement x) {

    }

    @Override
    public boolean visit(SQLShowTablesStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableAddPartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableAddPartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropPartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableDropPartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableRenamePartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableRenamePartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableSetComment x) {

    }

    @Override
    public boolean visit(SQLAlterTableSetComment x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableSetLifecycle x) {

    }

    @Override
    public boolean visit(SQLAlterTableSetLifecycle x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableEnableLifecycle x) {

    }

    @Override
    public boolean visit(SQLAlterTableEnableLifecycle x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDisableLifecycle x) {

    }

    @Override
    public boolean visit(SQLAlterTableDisableLifecycle x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableTouch x) {

    }

    @Override
    public boolean visit(SQLAlterTableTouch x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLArrayExpr x) {

    }

    @Override
    public boolean visit(SQLArrayExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLOpenStatement x) {

    }

    @Override
    public boolean visit(SQLOpenStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLFetchStatement x) {

    }

    @Override
    public boolean visit(SQLFetchStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCloseStatement x) {

    }

    @Override
    public boolean visit(SQLCloseStatement x) {
        push();
        return true;
    }

    @Override
    public boolean visit(SQLGroupingSetExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLGroupingSetExpr x) {

    }

    @Override
    public boolean visit(SQLIfStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLIfStatement x) {

    }

    @Override
    public boolean visit(SQLIfStatement.ElseIf x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLIfStatement.ElseIf x) {

    }

    @Override
    public boolean visit(SQLIfStatement.Else x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLIfStatement.Else x) {

    }

    @Override
    public boolean visit(SQLLoopStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLLoopStatement x) {

    }

    @Override
    public boolean visit(SQLParameter x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLParameter x) {

    }

    @Override
    public boolean visit(SQLCreateProcedureStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCreateProcedureStatement x) {

    }

    @Override
    public boolean visit(SQLCreateFunctionStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCreateFunctionStatement x) {

    }

    @Override
    public boolean visit(SQLBlockStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLBlockStatement x) {

    }

    @Override
    public boolean visit(SQLAlterTableDropKey x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropKey x) {

    }

    @Override
    public boolean visit(SQLDeclareItem x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDeclareItem x) {

    }

    @Override
    public boolean visit(SQLPartitionValue x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLPartitionValue x) {

    }

    @Override
    public boolean visit(SQLPartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLPartition x) {

    }

    @Override
    public boolean visit(SQLPartitionByRange x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLPartitionByRange x) {

    }

    @Override
    public boolean visit(SQLPartitionByHash x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLPartitionByHash x) {

    }

    @Override
    public boolean visit(SQLPartitionByList x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLPartitionByList x) {

    }

    @Override
    public boolean visit(SQLSubPartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSubPartition x) {

    }

    @Override
    public boolean visit(SQLSubPartitionByHash x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSubPartitionByHash x) {

    }

    @Override
    public boolean visit(SQLSubPartitionByList x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSubPartitionByList x) {

    }

    @Override
    public boolean visit(SQLAlterDatabaseStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterDatabaseStatement x) {

    }

    @Override
    public boolean visit(SQLAlterTableConvertCharSet x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableConvertCharSet x) {

    }

    @Override
    public boolean visit(SQLAlterTableReOrganizePartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableReOrganizePartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableCoalescePartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableCoalescePartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableTruncatePartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableTruncatePartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableDiscardPartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDiscardPartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableImportPartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableImportPartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableAnalyzePartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableAnalyzePartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableCheckPartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableCheckPartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableOptimizePartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableOptimizePartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableRebuildPartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableRebuildPartition x) {

    }

    @Override
    public boolean visit(SQLAlterTableRepairPartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableRepairPartition x) {

    }

    @Override
    public boolean visit(SQLSequenceExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLSequenceExpr x) {

    }

    @Override
    public boolean visit(SQLMergeStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLMergeStatement x) {

    }

    @Override
    public boolean visit(SQLMergeStatement.MergeUpdateClause x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLMergeStatement.MergeUpdateClause x) {

    }

    @Override
    public boolean visit(SQLMergeStatement.MergeInsertClause x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLMergeStatement.MergeInsertClause x) {

    }

    @Override
    public boolean visit(SQLErrorLoggingClause x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLErrorLoggingClause x) {

    }

    @Override
    public boolean visit(SQLNullConstraint x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLNullConstraint x) {

    }

    @Override
    public boolean visit(SQLCreateSequenceStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCreateSequenceStatement x) {

    }

    @Override
    public boolean visit(SQLDateExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDateExpr x) {

    }

    @Override
    public boolean visit(SQLLimit x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLLimit x) {

    }

    @Override
    public void endVisit(SQLStartTransactionStatement x) {

    }

    @Override
    public boolean visit(SQLStartTransactionStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDescribeStatement x) {

    }

    @Override
    public boolean visit(SQLDescribeStatement x) {
        push();
        return true;
    }

    @Override
    public boolean visit(SQLWhileStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLWhileStatement x) {

    }

    @Override
    public boolean visit(SQLDeclareStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDeclareStatement x) {

    }

    @Override
    public boolean visit(SQLReturnStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLReturnStatement x) {

    }

    @Override
    public boolean visit(SQLArgument x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLArgument x) {

    }

    @Override
    public boolean visit(SQLCommitStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCommitStatement x) {

    }

    @Override
    public boolean visit(SQLFlashbackExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLFlashbackExpr x) {

    }

    @Override
    public boolean visit(SQLCreateMaterializedViewStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCreateMaterializedViewStatement x) {

    }

    @Override
    public boolean visit(SQLBinaryOpExprGroup x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLBinaryOpExprGroup x) {

    }

    @Override
    public boolean visit(SQLScriptCommitStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLScriptCommitStatement x) {

    }

    @Override
    public boolean visit(SQLReplaceStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLReplaceStatement x) {

    }

    @Override
    public boolean visit(SQLCreateUserStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLCreateUserStatement x) {

    }

    @Override
    public boolean visit(SQLAlterFunctionStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterFunctionStatement x) {

    }

    @Override
    public boolean visit(SQLAlterTypeStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTypeStatement x) {

    }

    @Override
    public boolean visit(SQLIntervalExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLIntervalExpr x) {

    }

    @Override
    public boolean visit(SQLLateralViewTableSource x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLLateralViewTableSource x) {

    }

    @Override
    public boolean visit(SQLShowErrorsStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLShowErrorsStatement x) {

    }

    @Override
    public boolean visit(SQLAlterCharacter x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterCharacter x) {

    }

    @Override
    public boolean visit(SQLExprStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLExprStatement x) {

    }

    @Override
    public boolean visit(SQLAlterProcedureStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterProcedureStatement x) {

    }

    @Override
    public boolean visit(SQLAlterViewStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterViewStatement x) {

    }

    @Override
    public boolean visit(SQLDropEventStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropEventStatement x) {

    }

    @Override
    public boolean visit(SQLDropLogFileGroupStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropLogFileGroupStatement x) {

    }

    @Override
    public boolean visit(SQLDropServerStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropServerStatement x) {

    }

    @Override
    public boolean visit(SQLDropSynonymStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropSynonymStatement x) {

    }

    @Override
    public boolean visit(SQLRecordDataType x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLRecordDataType x) {

    }

    @Override
    public boolean visit(SQLDropTypeStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropTypeStatement x) {

    }

    @Override
    public boolean visit(SQLExternalRecordFormat x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLExternalRecordFormat x) {

    }

    @Override
    public boolean visit(SQLArrayDataType x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLArrayDataType x) {

    }

    @Override
    public boolean visit(SQLMapDataType x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLMapDataType x) {

    }

    @Override
    public boolean visit(SQLStructDataType x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLStructDataType x) {

    }

    @Override
    public boolean visit(SQLStructDataType.Field x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLStructDataType.Field x) {

    }

    @Override
    public boolean visit(SQLDropMaterializedViewStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDropMaterializedViewStatement x) {

    }

    @Override
    public boolean visit(SQLAlterTableRenameIndex x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableRenameIndex x) {

    }

    @Override
    public boolean visit(SQLAlterSequenceStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterSequenceStatement x) {

    }

    @Override
    public boolean visit(SQLAlterTableExchangePartition x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableExchangePartition x) {

    }

    @Override
    public boolean visit(SQLValuesExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLValuesExpr x) {

    }

    @Override
    public boolean visit(SQLValuesTableSource x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLValuesTableSource x) {

    }

    @Override
    public boolean visit(SQLContainsExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLContainsExpr x) {

    }

    @Override
    public boolean visit(SQLRealExpr x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLRealExpr x) {

    }

    @Override
    public boolean visit(SQLWindow x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLWindow x) {

    }

    @Override
    public boolean visit(SQLDumpStatement x) {
        push();
        return true;
    }

    @Override
    public void endVisit(SQLDumpStatement x) {

    }
}
