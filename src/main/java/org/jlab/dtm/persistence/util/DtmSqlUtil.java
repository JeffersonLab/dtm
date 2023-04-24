package org.jlab.dtm.persistence.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author ryans
 */
public final class DtmSqlUtil {

    private static final Logger logger = Logger.getLogger(
            DtmSqlUtil.class.getName());

    private static final DataSource source;

    private DtmSqlUtil() {
        // not public
    }

    static {
        try {
            source = (DataSource) new InitialContext().lookup("jdbc/dtm");
        } catch (NamingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    public static SQLException getFirstNestedSqlException(Throwable t) {
        SQLException e = null;

        if(t instanceof SQLException) {
            e = (SQLException)t;
        } else {
            while (t != null && t.getCause() != null) {
                t = t.getCause();

                if (t instanceof SQLException) {
                    e = (SQLException) t;
                    break;
                }
            }
        }

        return e;
    }
}
