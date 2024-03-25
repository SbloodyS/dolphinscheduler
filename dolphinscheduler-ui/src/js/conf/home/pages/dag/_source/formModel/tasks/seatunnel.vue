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
  <div class="shell-model">
    <m-list-box>
      <div slot="text">{{ '并行度' }}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="text"
          size="small"
          v-model="parallelism"
          :placeholder="'请输入并行度'">
        </el-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text" style="font-weight:bold">{{ $t('Data Source') }}</div>
    </m-list-box>
    <hr style="margin-left: 60px;">
    <m-list-box>
      <div slot="text">{{ $t('SourceDataSource') }}</div>
      <div slot="content">
        <div style="display: inline-block;">
          <el-select
            style="width: 350px;"
            size="small"
            v-model="sourceDataSourceName"
            :disabled="isDetails"
            @change="(val)=> _handleSourceDataSourceTypeChange(val)"
            filterable
          >
            <el-option
              v-for="type in dataSourceTypeList"
              :key="type.id"
              :value="type.id"
              :label="type.datasourceName">
            </el-option>
          </el-select>
        </div>
      </div>
    </m-list-box>
    <template v-if="['mysql', 'sqlserver', 'oracle'].includes(sourceDataSourceFormType)">
      <m-list-box>
        <div slot="text">{{ '查询SQL' }}</div>
        <div slot="content">
          <div class="form-mirror">
            <textarea
              id="code-sql-mirror-source"
              name="code-sql-mirror-source"
              style="opacity: 0">
          </textarea>
          </div>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ '拆分列名' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model="sourceSQLServerParams.partition_column"
            :placeholder="'请输入partition_column'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ '拆分数量' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model="sourceSQLServerParams.partition_num"
            :placeholder="'请输入partition_num'">
          </el-input>
        </div>
      </m-list-box>
    </template>
    <template v-if="['hive'].includes(sourceDataSourceFormType)">
      <m-list-box>
        <div slot="text">{{ '来源表名' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model="sourceHiveParams.table_name"
            :placeholder="'请输入Hive表名'">
          </el-input>
        </div>
      </m-list-box>
    </template>
    <template v-if="['clickhouse'].includes(sourceDataSourceFormType)">
      <m-list-box>
        <div slot="text">{{ '查询SQL' }}</div>
        <div slot="content">
          <div class="form-mirror">
            <textarea
              id="code-sql-mirror-source"
              name="code-sql-mirror-source"
              style="opacity: 0">
          </textarea>
          </div>
        </div>
      </m-list-box>
    </template>

    <m-list-box>
      <div slot="text" style="font-weight:bold">{{ $t('Data Target') }}</div>
    </m-list-box>
    <hr style="margin-left: 60px;">
    <m-list-box>
      <div slot="text">{{ $t('TargetDataSource') }}</div>
      <div slot="content">
        <div style="display: inline-block;">
          <el-select
            style="width: 350px;"
            size="small"
            v-model="targetDataSourceName"
            :disabled="isDetails"
            @change="(val)=> _handleTargetDataSourceTypeChange(val)"
            filterable
          >
            <el-option
              v-for="type in dataSourceTypeList"
              :key="type.id"
              :value="type.id"
              :label="type.datasourceName">
            </el-option>
          </el-select>
        </div>
      </div>
    </m-list-box>
    <template v-if="['mysql', 'sqlserver'].includes(targetDataSourceFormType)">
      <m-list-box>
        <div slot="text">{{ '写入SQL' }}</div>
        <div slot="content">
          <div class="form-mirror">
            <textarea
              id="code-sql-mirror-target"
              name="code-sql-mirror-target"
              style="opacity: 0">
            </textarea>
          </div>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ '前置SQL' }}</div>
        <div slot="content">
          <div class="form-mirror">
            <textarea
              id="code-sql-mirror-sinkbefore"
              name="code-sql-mirror-sinkbefore"
              style="opacity: 0">
            </textarea>
          </div>
        </div>
      </m-list-box>
    </template>
    <template v-if="['hive'].includes(targetDataSourceFormType)">
      <m-list-box>
        <div slot="text">{{ '目的表名' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model="targetHiveParams.table_name"
            :placeholder="'请输入Hive表名'">
          </el-input>
        </div>
      </m-list-box>
      <template v-if="['mysql', 'sqlserver', 'oracle', 'clickhouse'].includes(sourceDataSourceFormType)">
        <m-list-box>
        <div slot="text">{{ '自动创建Hive表' }}</div>
        <div slot="content">
          <el-radio-group v-model="autoCreateHiveTableRadio" size="small" @change="_handleAutoCreateHiveTable">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </div>
        </m-list-box>
      </template>
    </template>
    <template v-if="['clickhouse'].includes(targetDataSourceFormType)">
      <m-list-box>
        <div slot="text">{{ '目的表名' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model="targetClickhouseParams.table"
            :placeholder="'请输入ClickHouse表名'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ '前置SQL' }}</div>
        <div slot="content">
          <div class="form-mirror">
            <textarea
              id="code-sql-mirror-sinkbefore"
              name="code-sql-mirror-sinkbefore"
              style="opacity: 0">
            </textarea>
          </div>
        </div>
      </m-list-box>
    </template>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mListBox from './_source/listBox'
  import mScriptBox from './_source/scriptBox'
  import disabledState from '@/module/mixin/disabledState'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'
  import Clipboard from 'clipboard'
  import { diGuiTree, searchTree } from './_source/resourceTree'

  let editorSource
  let editorTarget
  let editorSinkBefore

  export default {
    name: 'seatunnel',
    data () {
      return {
        valueConsistsOf: 'LEAF_PRIORITY',
        // script
        sourceSql: '',
        targetSql: '',
        // Custom parameter
        localParams: [],
        // resource(list)
        resourceList: [],
        // Cache ResourceList
        cacheResourceList: [],
        // define options
        options: [],
        normalizer (node) {
          return {
            label: node.name
          }
        },
        allNoResources: [],
        noRes: [],
        item: '',

        autoCreateHiveTableRadio: 0,
        autoCreateHiveTable: false,
        sinkBeforeSql: '',
        sourceDataSourceFormType: '',
        targetDataSourceFormType: '',
        scriptBoxDialogSource: false,
        sourceDataSourceId: '',
        sourceDataSourceName: '',
        sourceDataSourceType: '',
        targetDataSourceId: '',
        targetDataSourceName: '',
        targetDataSourceType: '',
        dataSourceTypeList: [],
        parallelism: 1,

        sourceHiveParams: {
          table_name: ''
        },
        sourceSQLServerParams: {
          query: '',
          partition_column: '',
          partition_num: ''
        },
        sourceClickhouseParams: {
          sql: ''
        },

        targetHiveParams: {
          table_name: ''
        },
        targetSQLServerParams: {
          query: ''
        },
        targetClickhouseParams: {
          table: ''
        }
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      _copyPath (e, node) {
        e.stopPropagation()
        let clipboard = new Clipboard('.copy-path', {
          text: function () {
            return node.raw.fullName
          }
        })
        clipboard.on('success', handler => {
          this.$message.success(`${i18n.$t('Copy success')}`)
          // Free memory
          clipboard.destroy()
        })
        clipboard.on('error', handler => {
          // Copy is not supported
          this.$message.warning(`${i18n.$t('The browser does not support automatic copying')}`)
          // Free memory
          clipboard.destroy()
        })
      },
      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },
      setEditorVal () {
        this.item = editorSource.getValue()
        this.scriptBoxDialogSource = true

        mScriptBox.methods.setScriptBoxValue(editorSource.getValue())
      },
      getScriptBoxValue (val) {
        editorSource.setValue(val)
      },
      closeAble () {
      },
      /**
       * return resourceList
       *
       */
      _onResourcesData (a) {
        this.resourceList = a
      },
      /**
       * cache resourceList
       */
      _onCacheResourcesData (a) {
        this.cacheResourceList = a
      },
      /**
       * verification
       */
      _verification () {
        if (!Number.isInteger(this.parallelism) && this.parallelism <= 0) {
          this.$message.warning(`${i18n.$t('Please enter a positive integer') + '并行度'}`)
          return false
        }

        let requestParams = {}
        if (['mysql', 'sqlserver', 'oracle'].includes(this.sourceDataSourceFormType)) {
          if (!editorSource.getValue()) {
            this.$message.warning('请输入查询SQL')
            return false
          }
          this.sourceSQLServerParams.query = editorSource.getValue()

          if (this.sourceSQLServerParams.partition_num) {
            if (!Number.isInteger(this.sourceSQLServerParams.partition_num) && this.sourceSQLServerParams.partition_num <= 0) {
              console.log('partition_num:', this.sourceSQLServerParams.partition_num)
              this.$message.warning(`${i18n.$t('Please enter a positive integer') + '拆分数量'}`)
              return false
            }
          }
          requestParams.source = this.sourceSQLServerParams
        } else if (['hive'].includes(this.sourceDataSourceFormType)) {
          requestParams.source = this.sourceHiveParams
        } else if (['clickhouse'].includes(this.sourceDataSourceFormType)) {
          if (!editorSource.getValue()) {
            this.$message.warning('请输入查询SQL')
            return false
          }
          this.sourceClickhouseParams.sql = editorSource.getValue()
          requestParams.source = this.sourceClickhouseParams
        } else {
          this.$message.warning(`暂不支持的数据源类型${this.sourceDataSourceType}`)
          return false
        }

        if (['mysql', 'sqlserver'].includes(this.targetDataSourceFormType)) {
          if (!editorTarget.getValue()) {
            this.$message.warning(`${i18n.$t('Please enter sql(required)')}`)
            return false
          }
          this.targetSQLServerParams.query = editorTarget.getValue()
          requestParams.sinkBeforeSql = editorSinkBefore.getValue()
          requestParams.sink = this.targetSQLServerParams
        } else if (['hive'].includes(this.targetDataSourceFormType)) {
          requestParams.sink = this.targetHiveParams
        } else if (['clickhouse'].includes(this.targetDataSourceFormType)) {
          if (this.targetClickhouseParams.table === '') {
            this.$message.warning('请输入目的表名')
            return false
          }
          requestParams.sinkBeforeSql = editorSinkBefore.getValue()
          requestParams.sink = this.targetClickhouseParams
        } else {
          this.$message.warning(`暂不支持的数据源类型${this.targetDataSourceType}`)
          return false
        }

        requestParams.env = { parallelism: this.parallelism }
        requestParams.sourceDataSourceId = this.sourceDataSourceId
        requestParams.targetDataSourceId = this.targetDataSourceId
        requestParams.autoCreateHiveTable = this.autoCreateHiveTable

        // storage
        this.$emit('on-params', requestParams)
        return true
      },
      /**
       * Processing code highlighting
       */
      _handlerEditorSource () {
        if (editorSource) {
          editorSource.toTextArea() // Uninstall
          editorSource.off($('.code-sql-mirror-source'), 'keypress', this.keypress)
          editorSource.off($('.code-sql-mirror-source'), 'changes', this.changes)
          editorSource = null
        }

        // editor
        editorSource = codemirror('code-sql-mirror-source', {
          mode: 'sql',
          readOnly: this.isDetails
        })

        editorSource.setSize('auto', '350px')

        this.keypress = () => {
          if (!editorSource.getOption('readOnly')) {
            editorSource.showHint({
              completeSingle: false
            })
          }
        }

        // Monitor keyboard
        editorSource.on('keypress', this.keypress)
        editorSource.setValue(this.sourceSql)

        return editorSource
      },
      _handlerEditorTarget () {
        if (editorTarget) {
          editorTarget.toTextArea() // Uninstall
          editorTarget.off($('.code-sql-mirror-target'), 'keypress', this.keypress)
          editorTarget.off($('.code-sql-mirror-target'), 'changes', this.changes)
          editorTarget = null
        }
        // editor
        editorTarget = codemirror('code-sql-mirror-target', {
          mode: 'sql',
          readOnly: this.isDetails
        })

        editorTarget.setSize('auto', '350px')

        this.keypress = () => {
          if (!editorTarget.getOption('readOnly')) {
            editorTarget.showHint({
              completeSingle: false
            })
          }
        }

        // Monitor keyboard
        editorTarget.on('keypress', this.keypress)
        editorTarget.setValue(this.targetSql)

        return editorTarget
      },
      _handlerEditorSinkBefore () {
        if (editorSinkBefore) {
          editorSinkBefore.toTextArea() // Uninstall
          editorSinkBefore.off($('.code-sql-mirror-sinkbefore'), 'keypress', this.keypress)
          editorSinkBefore.off($('.code-sql-mirror-sinkbefore'), 'changes', this.changes)
          editorSinkBefore = null
        }
        // editor
        editorSinkBefore = codemirror('code-sql-mirror-sinkbefore', {
          mode: 'sql',
          readOnly: this.isDetails
        })

        editorSinkBefore.setSize('auto', '150px')

        this.keypress = () => {
          if (!editorSinkBefore.getOption('readOnly')) {
            editorSinkBefore.showHint({
              completeSingle: false
            })
          }
        }

        // Monitor keyboard
        editorSinkBefore.on('keypress', this.keypress)
        editorSinkBefore.setValue(this.sinkBeforeSql)

        return editorSinkBefore
      },
      _handleAutoCreateHiveTable (val) {
        this.autoCreateHiveTable = val === 1
      },
      dataProcess (backResource) {
        let isResourceId = []
        let resourceIdArr = []
        if (this.resourceList.length > 0) {
          this.resourceList.forEach(v => {
            this.options.forEach(v1 => {
              if (searchTree(v1, v)) {
                isResourceId.push(searchTree(v1, v))
              }
            })
          })
          resourceIdArr = isResourceId.map(item => {
            return item.id
          })
          Array.prototype.diff = function (a) {
            return this.filter(function (i) {
              return a.indexOf(i) < 0
            })
          }
          let diffSet = this.resourceList.diff(resourceIdArr)
          let optionsCmp = []
          if (diffSet.length > 0) {
            diffSet.forEach(item => {
              backResource.forEach(item1 => {
                if (item === item1.id || item === item1.res) {
                  optionsCmp.push(item1)
                }
              })
            })
          }
          let noResources = [{
            id: -1,
            name: $t('Unauthorized or deleted resources'),
            fullName: '/' + $t('Unauthorized or deleted resources'),
            children: []
          }]
          if (optionsCmp.length > 0) {
            this.allNoResources = optionsCmp
            optionsCmp = optionsCmp.map(item => {
              return {
                id: item.id,
                name: item.name,
                fullName: item.res
              }
            })
            optionsCmp.forEach(item => {
              item.isNew = true
            })
            noResources[0].children = optionsCmp
            this.options = this.options.concat(noResources)
          }
        }
      },
      _handleSourceDataSourceTypeChange (val) {
        this.sourceDataSourceId = val
        this.sourceDataSourceName = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceName
        this.sourceDataSourceType = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceType

        if (['mysql', 'sqlserver', 'oracle'].includes(this.sourceDataSourceType)) {
          this.sourceDataSourceFormType = this.sourceDataSourceType
          setTimeout(() => {
            this._handlerEditorSource()
          }, 200)
        } else if (['hive'].includes(this.sourceDataSourceType)) {
          this.sourceDataSourceFormType = this.sourceDataSourceType
        } else if (['clickhouse'].includes(this.sourceDataSourceType)) {
          this.sourceDataSourceFormType = this.sourceDataSourceType
          setTimeout(() => {
            this._handlerEditorSource()
          }, 200)
        } else {
          this.sourceDataSourceFormType = ''
          this.sourceDataSourceType = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceType
          this.$message.warning(`暂不支持的数据源类型${this.sourceDataSourceType}`)
        }
      },
      _handleTargetDataSourceTypeChange (val) {
        this.targetDataSourceId = val
        this.targetDataSourceName = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceName
        this.targetDataSourceType = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceType
        if (['mysql', 'sqlserver'].includes(this.targetDataSourceType)) {
          this.targetDataSourceFormType = this.targetDataSourceType
          setTimeout(() => {
            this._handlerEditorTarget()
            this._handlerEditorSinkBefore()
          }, 200)
        } else if (['hive'].includes(this.targetDataSourceType)) {
          this.targetDataSourceFormType = this.targetDataSourceType
        } else if (['clickhouse'].includes(this.targetDataSourceType)) {
          this.targetDataSourceFormType = this.targetDataSourceType
          setTimeout(() => {
            this._handlerEditorSinkBefore()
          }, 200)
        } else {
          this.targetDataSourceFormType = ''
          this.targetDataSourceType = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceType
          this.$message.warning(`暂不支持的数据源类型${this.targetDataSourceType}`)
        }
      },
      _getDataSourceInfo () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('dag/getDatasourceNewInfo').then(res => {
            this.dataSourceTypeList = _.map(res.data, v => {
              return {
                id: v.id,
                datasourceName: v.datasourceName,
                datasourceType: v.datasourceType
              }
            })
            resolve()
          })
        })
      }
    },
    watch: {
      // Watch the cacheParams
      cacheParams (val) {
        this.$emit('on-cache-params', val)
      },
      resourceIdArr (arr) {
        let result = []
        arr.forEach(item => {
          this.allNoResources.forEach(item1 => {
            if (item.id === item1.id) {
              // resultBool = true
              result.push(item1)
            }
          })
        })
        this.noRes = result
      }
    },
    computed: {
      resourceIdArr () {
        let isResourceId = []
        let resourceIdArr = []
        if (this.resourceList.length > 0) {
          this.resourceList.forEach(v => {
            this.options.forEach(v1 => {
              if (searchTree(v1, v)) {
                isResourceId.push(searchTree(v1, v))
              }
            })
          })
          resourceIdArr = isResourceId.map(item => {
            return {
              id: item.id,
              name: item.name,
              res: item.fullName
            }
          })
        }
        return resourceIdArr
      },
      cacheParams () {
        return {
          resourceList: this.resourceIdArr,
          localParams: this.localParams
        }
      }
    },
    created () {
      let item = this.store.state.dag.resourcesListS
      diGuiTree(item)
      this.options = item
      let o = this.backfillItem
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this._getDataSourceInfo().then(() => {
          let sourceDataSourceId = o.params.sourceDataSourceId || ''
          this._handleSourceDataSourceTypeChange(sourceDataSourceId)
          if (['mysql', 'sqlserver', 'oracle'].includes(this.sourceDataSourceType)) {
            this.sourceSql = o.params.source.query || ''
            this.sourceSQLServerParams.partition_column = o.params.source.partition_column || ''
            this.sourceSQLServerParams.partition_num = o.params.source.partition_num || ''
            setTimeout(() => {
              this._handlerEditorSource()
            }, 200)
          } else if (['hive'].includes(this.sourceDataSourceType)) {
            this.sourceHiveParams.table_name = o.params.source.table_name || ''
          } else if (['clickhouse'].includes(this.sourceDataSourceType)) {
            this.sourceSql = o.params.source.sql || ''
          }

          let targetDataSourceId = o.params.targetDataSourceId || ''
          this._handleTargetDataSourceTypeChange(targetDataSourceId)
          if (['mysql', 'sqlserver'].includes(this.targetDataSourceType)) {
            this.targetSql = o.params.sink.query || ''
            this.sinkBeforeSql = o.params.sinkBeforeSql || ''
            setTimeout(() => {
              this._handlerEditorTarget()
              this._handlerEditorSinkBefore()
            }, 200)
          } else if (['hive'].includes(this.targetDataSourceType)) {
            this.targetHiveParams.table_name = o.params.sink.table_name || ''
            this.autoCreateHiveTable = o.params.autoCreateHiveTable || false
            if (this.autoCreateHiveTable) {
              this.autoCreateHiveTableRadio = 1
            } else {
              this.autoCreateHiveTableRadio = 0
            }
          } else if (['clickhouse'].includes(this.targetDataSourceType)) {
            this.targetClickhouseParams.table = o.params.sink.table || ''
            this.sinkBeforeSql = o.params.sinkBeforeSql || ''
            setTimeout(() => {
              this._handlerEditorSinkBefore()
            }, 200)
          }
          this.parallelism = o.params.env.parallelism || 1
        }
        )

        // backfill resourceList
        let backResource = o.params.resourceList || []
        let resourceList = o.params.resourceList || []
        if (resourceList.length) {
          _.map(resourceList, v => {
            if (!v.id) {
              this.store.dispatch('dag/getResourceId', {
                type: 'FILE',
                fullName: '/' + v.res
              }).then(res => {
                this.resourceList.push(res.id)
                this.dataProcess(backResource)
              }).catch(e => {
                this.resourceList.push(v.res)
                this.dataProcess(backResource)
              })
            } else {
              this.resourceList.push(v.id)
              this.dataProcess(backResource)
            }
          })
          this.cacheResourceList = resourceList
        }

        // backfill localParams
        let localParams = o.params.localParams || []
        if (localParams.length) {
          this.localParams = localParams
        }
      } else {
        this._getDataSourceInfo()
      }
    },
    mounted () {
      // setTimeout(() => {
      //   this._handlerEditor()
      //   this._handlerEditorTarget()
      // }, 200)
    },
    destroyed () {
      if (editorSource) {
        editorSource.toTextArea() // Uninstall
        editorSource.off($('.code-shell-mirror'), 'keypress', this.keypress)
      }
    },
    components: {
      mListBox,
      mScriptBox
    }
  }
</script>
