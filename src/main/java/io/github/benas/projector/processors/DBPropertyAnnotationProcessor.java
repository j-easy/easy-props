/*
 *   The MIT License
 *
 *    Copyright (c) 2014, Mahmoud Ben Hassine (md.benhassine@gmail.com)
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *    of this software and associated documentation files (the "Software"), to deal
 *    in the Software without restriction, including without limitation the rights
 *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *    copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in
 *    all copies or substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *    THE SOFTWARE.
 */

package io.github.benas.projector.processors;

import io.github.benas.projector.annotations.DBProperty;
import io.github.benas.projector.api.AnnotationProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * An annotation processor that loads properties from a database.
 *
 * @author Mahmoud Ben Hassine (md.benhassine@gmail.com)
 */
public class DBPropertyAnnotationProcessor extends AbstractAnnotationProcessor implements AnnotationProcessor<DBProperty> {

    /**
     * A map holding database configuration properties file names and properties object serving as a cache.
     */
    private Map<String, Properties> dbConfigurationMap = new HashMap<String, Properties>();

    /**
     * A map holding database properties file name and resource properties object serving as a cache.
     */
    private Map<String, Properties> dbPropertiesMap = new HashMap<String, Properties>();

    @Override
    public void processAnnotation(final DBProperty dbPropertyAnnotation, final Field field, Object object) throws Exception {

        String configuration = dbPropertyAnnotation.configuration().trim();
        String key = dbPropertyAnnotation.key().trim();

        //check configuration attribute value
        if (configuration.isEmpty()) {
            throw new Exception(missingAttributeValue("configuration", "@DBProperty", field, object));
        }

        //check key attribute value
        if (key.isEmpty()) {
            throw new Exception(missingAttributeValue("key", "@DBProperty", field, object));
        }

        //check if database connection properties are not already loaded
        if (!dbConfigurationMap.containsKey(configuration)) {
            Properties dbConfigurationProperties = new Properties();
            try {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configuration);
                if (inputStream != null) {
                    dbConfigurationProperties.load(inputStream);
                    dbConfigurationMap.put(configuration, dbConfigurationProperties);
                } else {
                    throw new Exception(missingSourceFile(configuration, field, object));
                }
            } catch (IOException ex) {
                throw new Exception(missingSourceFile(configuration, field, object), ex);
            }
        }

        //check if database properties are not already loaded
        if (!dbPropertiesMap.containsKey(configuration)) {
            Properties dbConfigurationProperties = dbConfigurationMap.get(configuration);

            Class.forName(dbConfigurationProperties.getProperty("db.driver"));

            String url = dbConfigurationProperties.getProperty("db.url");
            String user = dbConfigurationProperties.getProperty("db.user");
            String password = dbConfigurationProperties.getProperty("db.password");
            Connection connection = DriverManager.getConnection(url, user, password);

            String schema = dbConfigurationProperties.getProperty("db.schema");
            String table = dbConfigurationProperties.getProperty("db.table");
            String keyColumn = dbConfigurationProperties.getProperty("db.table.keyColumn");
            String valueColumn = dbConfigurationProperties.getProperty("db.table.valueColumn");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT " + keyColumn + ", " + valueColumn + " FROM " + schema + "." + table
            );

            Properties dbProperties = new Properties();
            while (resultSet.next()) {
                String dbKey = resultSet.getString(keyColumn);
                String dbValue = resultSet.getString(valueColumn);
                dbProperties.put(dbKey, dbValue);
            }

            resultSet.close();
            statement.close();
            connection.close();

            dbPropertiesMap.put(configuration, dbProperties);
        }

        //check object obtained from database
        String value = dbPropertiesMap.get(configuration).getProperty(key);
        if (value == null || value.isEmpty()) {
            throw new Exception("Key " + key + " not found or empty in database configured with properties: " +
                    dbConfigurationMap.get(configuration));
        }

        //inject object in annotated field
        injectProperty(object, field, key, value);

    }

}
