package org.apache.dolphinscheduler.api.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.DataSourceNewService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.DataSourceNew;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceNewMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class DataSourceNewServiceImpl extends BaseServiceImpl implements DataSourceNewService {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceNewServiceImpl.class);

    @Autowired
    private DataSourceNewMapper dataSourceNewMapper;

    @Autowired
    private UsersService usersService;

    @Override
    public Result<Object> createDataSourceNew(User loginUser, DataSourceNew dataSourceNew) {
        Result<Object> result = new Result<>();
        boolean userPermissionFlag = checkUserPermission(loginUser);
        if (!userPermissionFlag) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Date now = new Date();
        dataSourceNew.setCreateTime(now);
        dataSourceNew.setUpdateTime(now);

        try {
            dataSourceNewMapper.insert(dataSourceNew);
            putMsg(result, Status.SUCCESS);
        } catch (DuplicateKeyException ex) {
            logger.error("Create dataSourceNew error.", ex);
            putMsg(result, Status.DATASOURCE_EXIST);
        }

        return result;
    }

    @Override
    public Result<Object> updateDataSource(User loginUser, DataSourceNew dataSourceNew) {
        Result<Object> result = new Result<>();
        boolean userPermissionFlag = checkUserPermission(loginUser);
        if (!userPermissionFlag) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Date now = new Date();
        dataSourceNew.setUpdateTime(now);
        try {
            dataSourceNewMapper.updateById(dataSourceNew);
            putMsg(result, Status.SUCCESS);
        } catch (DuplicateKeyException ex) {
            logger.error("Update dataSourceNew error.", ex);
            putMsg(result, Status.DATASOURCE_EXIST);
        }
        return result;
    }

    private boolean checkUserPermission(User user) {
        User userInfo = usersService.queryUser(user.getId());
        return userInfo != null && userInfo.getUserType().equals(UserType.ADMIN_USER);
    }

    @Override
    public Result<Object> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result<Object> result = new Result<>();
        boolean userPermissionFlag = checkUserPermission(loginUser);
        if (!userPermissionFlag) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Page<DataSourceNew> page = new Page<>(pageNo, pageSize);
        IPage<DataSourceNew> dataSourceList = dataSourceNewMapper.queryDataSourceListPaging(page, searchVal);
        List<DataSourceNew> dataSources = dataSourceList != null ? dataSourceList.getRecords() : new ArrayList<>();

        PageInfo<DataSourceNew> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) (dataSourceList != null ? dataSourceList.getTotal() : 0L));
        pageInfo.setTotalList(dataSources);
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> deleteDataSourceNew(User loginUser, int datasourceId) {
        Result<Object> result = new Result<>();
        boolean userPermissionFlag = checkUserPermission(loginUser);
        if (!userPermissionFlag) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        try {
            //query datasource by id
            DataSourceNew dataSourceNew = dataSourceNewMapper.selectById(datasourceId);
            if (dataSourceNew == null) {
                logger.error("dataSourceNew id {} not exist", datasourceId);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }

            dataSourceNewMapper.deleteById(datasourceId);
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            logger.error("delete dataSourceNew error", e);
            throw new RuntimeException("delete dataSourceNew error");
        }
        return result;
    }

    @Override
    public Result<Object> queryDataSourceNewById(User loginUser, int datasourceId) {
        Result<Object> result = new Result<>();
        boolean userPermissionFlag = checkUserPermission(loginUser);
        if (!userPermissionFlag) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        DataSourceNew dataSourceNew = dataSourceNewMapper.selectById(datasourceId);
        if (dataSourceNew == null) {
            logger.error("dataSourceNew id {} not exist", datasourceId);
            putMsg(result, Status.RESOURCE_NOT_EXIST);
            return result;
        }
        result.setData(dataSourceNew);
        putMsg(result, Status.SUCCESS);
        return result;
    }

}
