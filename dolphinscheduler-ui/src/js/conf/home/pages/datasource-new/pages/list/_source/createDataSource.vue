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
<template>
  <div class="datasource-popup-model">
    <div class="content-p">
      <div class="create-datasource-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Datasource')}}</template>
          <template slot="content" size="small">
              <el-select style="width: 100%;" v-model="datasourceType">
                <el-option v-for="item in datasourceTypeList" :key="item.value" :value="item.value" :label="item.label">
                </el-option>
              </el-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Datasource Name')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="datasourceName"
                    maxlength="60"
                    size="small"
                    :placeholder="$t('Please enter datasource name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Description')}}</template>
          <template slot="content">
            <el-input
                    type="textarea"
                    v-model="datasourceDesc"
                    size="small"
                    :placeholder="$t('Please enter description')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('IP')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="hostname"
                    maxlength="255"
                    size="small"
                    :placeholder="$t('Please enter IP')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Port')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="port"
                    size="small"
                    :placeholder="$t('Please enter port')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('User Name')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="userName"
                    maxlength="60"
                    size="small"
                    :placeholder="$t('Please enter user name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Password')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="password"
                    size="small"
                    :placeholder="$t('Please enter your password')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{'databaseName'}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="databaseName"
                    maxlength="60"
                    size="small"
                    :placeholder="$t('Please enter database name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{'schemaName'}}</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="schemaName"
              maxlength="60"
              size="small"
              :placeholder="'请输入schemaName'">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{'driverName'}}</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="driverName"
              maxlength="255"
              size="small"
              :placeholder="'请输入driverName'">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{'是否启用'}}</template>
          <template slot="content" size="small">
            <el-select style="width: 100%;" v-model="isAble">
              <el-option v-for="item in isAbleList" :key="item.value" :value="item.value" :label="item.label">
              </el-option>
            </el-select>
          </template>
        </m-list-box-f>
      </div>
    </div>
    <div class="bottom-p">
      <el-button type="text" ize="mini" @click="_close()"> {{$t('Cancel')}} </el-button>
<!--      <el-button type="success" size="mini" round @click="_testConnect()" :loading="testLoading">{{testLoading ? $t('Loading...') : $t('Test Connect')}}</el-button>-->
      <el-button type="primary" size="mini" round :loading="spinnerLoading" @click="_ok()">{{spinnerLoading ? $t('Loading...') :item ? `${$t('Edit')}` : `${$t('Submit')}`}} </el-button>
    </div>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import { isJson } from '@/module/util/util'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-datasource-new',
    data () {
      return {
        store,
        // btn loading
        spinnerLoading: false,
        id: '',
        // Data source type
        datasourceType: 'MYSQL',
        // name
        datasourceName: '',
        // description
        datasourceDesc: '',
        // host
        hostname: '',
        // port
        port: '',
        // data storage name
        databaseName: '',
        schemaName: '',
        driverName: '',
        isAble: 1,
        // database username
        userName: '',
        // Database password
        password: '',
        // btn test loading
        testLoading: false,
        showPrincipal: true,
        showDatabase: false,
        showConnectType: false,
        isShowPrincipal: true,
        prePortMapper: {},
        datasourceTypeList: [
          {
            value: 'mysql',
            label: 'MYSQL'
          },
          {
            value: 'postgresql',
            label: 'POSTGRESQL'
          },
          {
            value: 'hive',
            label: 'HIVE'
          },
          {
            value: 'clickhouse',
            label: 'CLICKHOUSE'
          },
          {
            value: 'oracle',
            label: 'ORACLE'
          },
          {
            value: 'sqlserver',
            label: 'SQLSERVER'
          },
          {
            value: 'doris',
            label: 'DORIS'
          },
          {
            value: 'trino',
            label: 'TRINO'
          },
          {
            value: 'elasticsearch',
            label: 'ELASTICSEARCH'
          }
        ],
        isAbleList: [
          {
            value: 1,
            label: '是'
          },
          {
            value: 0,
            label: '否'
          }
        ]
      }
    },
    props: {
      item: Object
    },

    methods: {
      _rtOtherPlaceholder () {
        return `${i18n.$t('Please enter format')} {"key1":"value1","key2":"value2"...} ${i18n.$t('connection parameter')}`
      },
      /**
       * submit
       */
      _ok () {
        this._submit()
      },
      /**
       * close
       */
      _close () {
        this.$emit('close')
      },
      /**
       * return param
       */
      _rtParam () {
        return {
          id: this.id,
          datasourceType: this.datasourceType,
          datasourceName: this.datasourceName,
          datasourceDesc: this.datasourceDesc,
          hostname: this.hostname,
          port: this.port,
          databaseName: this.databaseName,
          userName: this.userName,
          password: this.password,
          schemaName: this.schemaName,
          driverName: this.driverName,
          isAble: this.isAble
        }
      },
      /**
       * test connect
       */
      _testConnect () {
        if (this._verification()) {
          this.testLoading = true
          this.store.dispatch('datasource/connectDatasources', this._rtParam()).then(res => {
            setTimeout(() => {
              this.$message.success(res.msg)
              this.testLoading = false
            }, 800)
          }).catch(e => {
            this.$message.error(e.msg || '')
            this.testLoading = false
          })
        }
      },
      /**
       * Verify that the data source name exists
       */
      _verifName () {
        return new Promise((resolve, reject) => {
          if (this.name === this.item.name) {
            resolve()
            return
          }
          this.store.dispatch('datasource/verifyName', { name: this.name }).then(res => {
            resolve()
          }).catch(e => {
            this.$message.error(e.msg || '')
            reject(e)
          })
        })
      },
      /**
       * verification
       */
      _verification () {
        if (!this.name) {
          this.$message.warning(`${i18n.$t('Please enter resource name')}`)
          return false
        }
        if (!this.host) {
          this.$message.warning(`${i18n.$t('Please enter IP/hostname')}`)
          return false
        }
        if (!this.port) {
          this.$message.warning(`${i18n.$t('Please enter port')}`)
          return false
        }
        if (!this.userName) {
          this.$message.warning(`${i18n.$t('Please enter user name')}`)
          return false
        }

        if (!this.database && this.showDatabase === false) {
          this.$message.warning(`${i18n.$t('Please enter database name')}`)
          return false
        }
        if (this.other) {
          if (!isJson(this.other)) {
            this.$message.warning(`${i18n.$t('jdbc connection parameters is not a correct JSON format')}`)
            return false
          }
        }
        return true
      },
      /**
       * submit => add/update
       */
      _submit () {
        this.spinnerLoading = true
        let param = this._rtParam()
        // edit
        if (this.item) {
          param.id = this.item.id
        }
        this.store.dispatch(`datasourceNew/${this.item ? 'updateDatasourceNew' : 'createDatasourceNew'}`, param).then(res => {
          this.$message.success(res.msg)
          this.spinnerLoading = false
          this.$emit('onUpdate')
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.spinnerLoading = false
        })
      },
      /**
       * Get modified data
       */
      _getEditDatasource () {
        this.store.dispatch('datasourceNew/getEditDatasourceNew', { id: this.item.id }).then(res => {
          this.id = this.item.id
          this.datasourceType = res.datasourceType
          this.datasourceName = res.datasourceName
          this.datasourceDesc = res.datasourceDesc
          this.hostname = res.hostname
          this.userName = res.userName
          this.password = res.password
          this.databaseName = res.databaseName
          this.schemaName = res.schemaName
          this.driverName = res.driverName
          this.isAble = res.isAble

          // When in Editpage, Prevent default value overwrite backfill value
          setTimeout(() => {
            this.port = res.port
          }, 0)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * Set default port for each type.
       */
      _setDefaultValues (value) {
        // Default type is MYSQL
        let type = this.type || 'MYSQL'

        let defaultPort = this._getDefaultPort(type)

        // Backfill the previous input from memcache
        let mapperPort = this.prePortMapper[type]

        this.port = mapperPort || defaultPort
      },

      /**
       * Get default port by type
       */
      _getDefaultPort (type) {
        let defaultPort = ''
        switch (type) {
          case 'MYSQL':
            defaultPort = '3306'
            break
          case 'POSTGRESQL':
            defaultPort = '5432'
            break
          case 'HIVE':
            defaultPort = '10000'
            break
          case 'SPARK':
            defaultPort = '10015'
            break
          case 'CLICKHOUSE':
            defaultPort = '8123'
            break
          case 'ORACLE':
            defaultPort = '1521'
            break
          case 'SQLSERVER':
            defaultPort = '1433'
            break
          case 'DB2':
            defaultPort = '50000'
            break
          case 'PRESTO':
            defaultPort = '8080'
            break
          default:
            break
        }
        return defaultPort
      }
    },
    created () {
      // Backfill
      if (this.item.id) {
        this._getEditDatasource()
      }

      this._setDefaultValues()
    },
    watch: {
      type (value) {
        if (value === 'POSTGRESQL') {
          this.showDatabase = true
        } else {
          this.showDatabase = false
        }

        if (value === 'ORACLE' && !this.item.id) {
          this.showConnectType = true
          this.connectType = 'ORACLE_SERVICE_NAME'
        } else if (value === 'ORACLE' && this.item.id) {
          this.showConnectType = true
        } else {
          this.showConnectType = false
        }
        // Set default port for each type datasource
        this._setDefaultValues(value)
      },
      /**
       * Cache the previous input port for each type datasource
       * @param value
       */
      port (value) {
        this.prePortMapper[this.type] = value
      }
    },

    mounted () {
    },
    components: { mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .datasource-popup-model {
    background: #fff;
    border-radius: 3px;

    .top-p {
      height: 70px;
      line-height: 70px;
      border-radius: 3px 3px 0 0;
      padding: 0 20px;
      >span {
        font-size: 20px;
      }
    }
    .bottom-p {
      text-align: right;
      height: 72px;
      line-height: 72px;
      border-radius:  0 0 3px 3px;
      padding: 0 20px;
    }
    .content-p {
      min-width: 850px;
      min-height: 100px;
      .list-box-f {
        .text {
          width: 166px;
        }
        .cont {
          width: calc(100% - 186px);
        }
      }
    }
    .radio-label-last {
      margin-left: 0px !important;
    }
  }

</style>
