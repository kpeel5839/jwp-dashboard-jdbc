package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceConfig;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static void transactionCommand(Runnable runnable) {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);

            runnable.run();

            connection.commit();
        } catch (SQLException e) {
            rollback();
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(DataSourceConfig.getInstance());
        }
    }

    private static Connection getConnection() {
        return DataSourceUtils.getConnection(DataSourceConfig.getInstance());
    }

    private static void rollback() {
        try {
            Connection connection = getConnection();
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static <T> T transactionQuery(Supplier<T> supplier) {
        try {
            getConnection();
            return supplier.get();
        } catch (Exception e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(DataSourceConfig.getInstance());
        }
    }

}
