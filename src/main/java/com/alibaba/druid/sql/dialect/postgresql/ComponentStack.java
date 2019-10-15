package com.alibaba.druid.sql.dialect.postgresql;

import com.alibaba.druid.sql.ast.SQLObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maxwell.Lee
 * @version 3.8.1
 * @company Scho Techonlogy Co. Ltd
 * @date 2019/10/10 16:37
 */
public class ComponentStack {

    private Map<SQLObject, SQLObject> mapping = new HashMap<SQLObject, SQLObject>();

    public void put(SQLObject key, SQLObject value) {
        if (key == null || value == null) return;

        mapping.put(key, value);
    }

    public SQLObject get(SQLObject key) {
        if (key == null) return null;

        return mapping.get(key);
    }

    public <T> List<T> collect(List<T> items) {
        List<T> rst = new ArrayList<T>();

        for (T item : items) {
            rst.add((T)mapping.get(item));
        }

        return rst;
    }

}
