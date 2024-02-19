/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io from '@/module/io'

export default {
  /**
   * Data source creation
   * @param "type": string,//MYSQL, POSTGRESQL, HIVE, SPARK, CLICKHOUSE, ORACLE, SQLSERVER, PRESTO
   * @param payload
   */
  createDatasourceNew ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('datasourceNew/create', payload, res => {
        resolve(res)
      }, () => {
        // do nothing
      }, { emulateJSON: false }).catch(e => {
        reject(e)
      })
    })
  },
  getDataSourceNewListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('datasourceNew/list', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Delete data source
   */
  deleteDatasourceNew ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.delete(`datasourceNew/${payload.id}`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Data source editing
   */
  updateDatasourceNew ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.put('datasourceNew/update', payload, res => {
        resolve(res)
      }, () => {
        // do nothing
      }, { emulateJSON: false }).catch(e => {
        reject(e)
      })
    })
  },
  getEditDatasourceNew ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`datasourceNew/${payload.id}`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  }
}
