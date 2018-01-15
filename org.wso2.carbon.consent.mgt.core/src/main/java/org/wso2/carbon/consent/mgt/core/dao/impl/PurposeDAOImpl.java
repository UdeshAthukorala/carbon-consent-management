/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.consent.mgt.core.dao.impl;

import org.wso2.carbon.consent.mgt.core.dao.JdbcTemplate;
import org.wso2.carbon.consent.mgt.core.dao.PurposeDAO;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementServerException;
import org.wso2.carbon.consent.mgt.core.exception.DataAccessException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.persistence.JDBCPersistenceManager;

import java.util.List;

import static org.wso2.carbon.consent.mgt.core.constant.ConfigurationConstants.ErrorMessages;

/**
 * Default implementation of {@link PurposeDAO}. This handles {@link Purpose} related DB operations.
 */
public class PurposeDAOImpl implements PurposeDAO {

    @Override
    public Purpose addPurpose(Purpose purpose) throws ConsentManagementServerException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        final String INSERT_PURPOSE_SQL = "INSERT INTO PURPOSE(NAME, DESCRIPTION) VALUES(?,?)";
        Purpose purposeResult;
        int insertedId;
        try {
            insertedId = jdbcTemplate.executeInsert(INSERT_PURPOSE_SQL, (preparedStatement -> {
                preparedStatement.setString(1, purpose.getName());
                preparedStatement.setString(2, purpose.getDescription());
            }), purpose, true);
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_ADD_PURPOSE.getMessage(),
                                                                     purpose.getName(), purpose.getDescription()),
                                                       ErrorMessages.ERROR_CODE_ADD_PURPOSE.getCode(), e);
        }
        purposeResult = new Purpose(insertedId, purpose.getName(), purpose.getDescription());
        return purposeResult;
    }

    @Override
    public Purpose getPurposeById(int id) throws ConsentManagementException {
        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        final String SELECT_PURPOSE_BY_ID_SQL = "SELECT ID, NAME, DESCRIPTION FROM PURPOSE WHERE ID = ?";
        Purpose purpose;

        try {
            purpose = jdbcTemplate.fetchSingleRecord(SELECT_PURPOSE_BY_ID_SQL, (resultSet, rowNumber) ->
                    new Purpose(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3)),
                                                     preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_ID
                                                                             .getMessage(), id),
                                                       ErrorMessages.ERROR_CODE_SELECT_PURPOSE_BY_ID.getCode(), e);
        }
        return purpose;
    }

    @Override
    public List<Purpose> listPurposes(int limit, int offset) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        final String LIST_PAGINATED_PURPOSE_MYSQL = "SELECT ID, NAME, DESCRIPTION FROM PURPOSE ORDER BY ID  ASC LIMIT" +
                                                    " ? OFFSET ?";

        List<Purpose> purposes;
        try {
            purposes = jdbcTemplate.executeQuery(LIST_PAGINATED_PURPOSE_MYSQL,
                                                 (resultSet, rowNumber) -> new Purpose(resultSet.getInt(1),
                                                                                       resultSet.getString(2),
                                                                                       resultSet.getString(3)),
                                                 preparedStatement -> {
                                                     preparedStatement.setInt(1, limit);
                                                     preparedStatement.setInt(2, offset);
                                                 });
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_LIST_PURPOSE
                                                                             .getMessage(), limit, offset),
                                                       ErrorMessages.ERROR_CODE_LIST_PURPOSE.getCode(), e);
        }
        return purposes;
    }

    @Override
    public int deletePurpose(int id) throws ConsentManagementException {

        JdbcTemplate jdbcTemplate = JDBCPersistenceManager.getInstance().getJDBCTemplate();
        final String DELETE_PURPOSE_SQL = "DELETE FROM PURPOSE WHERE ID = ?";

        try {
            jdbcTemplate.executeUpdate(DELETE_PURPOSE_SQL, preparedStatement -> preparedStatement.setInt(1, id));
        } catch (DataAccessException e) {
            throw new ConsentManagementServerException(String.format(ErrorMessages.ERROR_CODE_DELETE_PURPOSE
                                                                             .getMessage(), id),
                                                       ErrorMessages.ERROR_CODE_DELETE_PURPOSE.getCode(), e);
        }

        return id;
    }
}
