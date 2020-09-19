/*
 * The MIT License
 *
 *   Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.props.processors;

import org.jeasy.props.annotations.DBProperty;
import org.jeasy.props.api.AnnotationProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;

/**
 * An annotation processor that loads properties from a database.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class DBPropertyAnnotationProcessor extends AbstractAnnotationProcessor<DBProperty> {

    public static final String DB_DRIVER = "org.jeasy.props.db.driver";
    public static final String DB_URL = "org.jeasy.props.db.url";
    public static final String DB_USER = "org.jeasy.props.db.user";
    public static final String DB_PASSWORD = "org.jeasy.props.db.password";
    public static final String DB_SCHEMA = "org.jeasy.props.db.schema";
    public static final String DB_TABLE = "org.jeasy.props.db.table";
    public static final String DB_TABLE_KEY_COLUMN = "org.jeasy.props.db.table.keyColumn";
    public static final String DB_TABLE_VALUE_COLUMN = "org.jeasy.props.db.table.valueColumn";

    private static final Logger LOGGER = LoggerFactory.getLogger(DBPropertyAnnotationProcessor.class);

    /**
     * A map holding database configuration properties file names and properties object serving as a cache.
     */
    private final Map<String, Properties> dbConfigurationMap = new HashMap<>();

    @Override
    public Object processAnnotation(final DBProperty dbPropertyAnnotation, final Field field) throws AnnotationProcessingException {

        String configuration = dbPropertyAnnotation.configuration().trim();
        String key = dbPropertyAnnotation.key().trim();
        String defaultValue = dbPropertyAnnotation.defaultValue().trim();
        boolean failFast = dbPropertyAnnotation.failFast();

        //check attributes
        String annotationName = DBProperty.class.getName();
        rejectIfEmpty(configuration, missingAttributeValue("configuration", annotationName, field));
        rejectIfEmpty(key, missingAttributeValue("key", annotationName, field));

        //check if database connection configuration is not already loaded
        if (!dbConfigurationMap.containsKey(configuration)) {
            loadDatabaseConfigurationProperties(configuration);
        }

        Properties dbProperties = loadDatabaseProperties(configuration);

        //check object obtained from database
        String value = dbProperties.getProperty(key);
        if (value == null) {
            String message = String.format("Key '%s' not found in database configured with properties from file '%s'",
                    key, configuration);
            LOGGER.warn(message);
            if (failFast) {
                throw new AnnotationProcessingException(message);
            }
            if (!defaultValue.isEmpty()) {
                value = defaultValue;
            }
        }

        return value;
    }

    private Properties loadDatabaseProperties(final String configuration) throws AnnotationProcessingException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            Properties dbConfigurationProperties = dbConfigurationMap.get(configuration);
            Class.forName(dbConfigurationProperties.getProperty(DB_DRIVER));
            connection = getConnection(dbConfigurationProperties);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(getSqlQuery(dbConfigurationProperties));
            return extractProperties(resultSet, dbConfigurationProperties);
        } catch (Exception e) {
            throw new AnnotationProcessingException("Unable to get database properties from '" + configuration + "'", e);
        } finally {
            try {
                closeResources(connection, statement, resultSet);
            } catch (SQLException e) {
                LOGGER.warn("Unable to close database resources", e);
            }
        }
    }

    private Properties extractProperties(final ResultSet resultSet, final Properties dbConfigurationProperties) throws SQLException {
        Properties dbProperties = new Properties();
        String keyColumn = dbConfigurationProperties.getProperty(DB_TABLE_KEY_COLUMN);
        String valueColumn = dbConfigurationProperties.getProperty(DB_TABLE_VALUE_COLUMN);
        while (resultSet.next()) {
            String dbKey = resultSet.getString(keyColumn);
            String dbValue = resultSet.getString(valueColumn);
            dbProperties.put(dbKey, dbValue);
        }
        return dbProperties;
    }

    private Connection getConnection(final Properties dbConfigurationProperties) throws SQLException {
        String url = dbConfigurationProperties.getProperty(DB_URL);
        String user = dbConfigurationProperties.getProperty(DB_USER);
        String password = dbConfigurationProperties.getProperty(DB_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }

    private String getSqlQuery(final Properties dbConfigurationProperties) {
        String schema = dbConfigurationProperties.getProperty(DB_SCHEMA);
        String table = dbConfigurationProperties.getProperty(DB_TABLE);
        String keyColumn = dbConfigurationProperties.getProperty(DB_TABLE_KEY_COLUMN);
        String valueColumn = dbConfigurationProperties.getProperty(DB_TABLE_VALUE_COLUMN);
        return format("SELECT %s, %s FROM %s.%s", keyColumn, valueColumn, schema, table);
    }

    private void closeResources(final Connection connection, final Statement statement, final ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    private void loadDatabaseConfigurationProperties(final String configuration) throws AnnotationProcessingException {
        Properties dbConfigurationProperties = new Properties();
        try {
            InputStream inputStream = getResourceAsStream(configuration);
            if (inputStream != null) {
                dbConfigurationProperties.load(inputStream);
                dbConfigurationMap.put(configuration, dbConfigurationProperties);
            }
        } catch (IOException e) {
            throw new AnnotationProcessingException(format("Unable to get properties from '%s'", configuration), e);
        }
    }

}
