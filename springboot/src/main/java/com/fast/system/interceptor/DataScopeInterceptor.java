package com.fast.system.interceptor;

import com.fast.system.annotation.DataScope;
import com.fast.system.domain.LoginUser;
import com.fast.system.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Properties;

/**
 * 数据权限拦截器（底层篡改 SQL）
 */
@Slf4j
@Intercepts({
        // 拦截 StatementHandler 的 prepare 方法（在 SQL 发送给数据库之前动手）
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // 1. 获取当前的 MappedStatement（包含 Mapper 方法和注解信息）
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // 2. 判断当前方法是否贴了 @DataScope 注解
        String mappedStatementId = mappedStatement.getId();
        String className = mappedStatementId.substring(0, mappedStatementId.lastIndexOf("."));
        String methodName = mappedStatementId.substring(mappedStatementId.lastIndexOf(".") + 1);

        Class<?> clazz = Class.forName(className);
        Method[] methods = clazz.getMethods();
        DataScope dataScope = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                dataScope = method.getAnnotation(DataScope.class);
                break;
            }
        }

        // 如果没加注解，直接放行，不处理
        if (dataScope == null) {
            return invocation.proceed();
        }

        // 3. 获取当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return invocation.proceed();
        }

        // 4. 如果是超级管理员，直接放行，不拼 SQL
        if (loginUser.isAdmin()) {
            log.info("【数据权限】超级管理员，放行全部数据");
            return invocation.proceed();
        }

        // 5. 获取原始 SQL
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();

        try {
            // 6. 用 JSqlParser 解析原始 SQL
            Select select = (Select) CCJSqlParserUtil.parse(originalSql);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

            // 7. 构建我们要拼接的条件
            // 假设当前用户是部门管理员，只能看本部门数据
            // 拼接：AND u.dept_id = 1
            String deptAlias = dataScope.deptAlias();
            String conditionSql = deptAlias + ".dept_id = " + loginUser.getDeptId();

            Expression sqlExpression = CCJSqlParserUtil.parseCondExpression(conditionSql);

            // 8. 将拼接条件加入原 SQL 的 WHERE 子句
            Expression where = plainSelect.getWhere();
            if (where == null) {
                // 如果原 SQL 没有 WHERE，直接加
                plainSelect.setWhere(sqlExpression);
            } else {
                // 如果原 SQL 有 WHERE，用 AND 连接
                net.sf.jsqlparser.expression.operators.conditional.AndExpression andExpression =
                        new net.sf.jsqlparser.expression.operators.conditional.AndExpression(where, sqlExpression);
                plainSelect.setWhere(andExpression);
            }

            // 9. 将篡改后的 SQL 塞回 BoundSql 中
            String newSql = plainSelect.toString();
            metaObject.setValue("delegate.boundSql.sql", newSql);

            log.info("【数据权限】原 SQL: {}", originalSql);
            log.info("【数据权限】追加 SQL: {}", newSql);

        } catch (Exception e) {
            log.error("【数据权限】SQL 解析异常，降级放行", e);
        }

        // 10. 放行，让 MyBatis 带着新 SQL 去查数据库
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}