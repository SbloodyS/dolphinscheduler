package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;

public interface DataSyncService {
    Result queryDataSyncByTaskCode(long taskCode);
}
