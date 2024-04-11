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
          v-model.trim="parallelism"
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
            v-model.trim="sourceSQLServerParams.partition_column"
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
            v-model.trim="sourceSQLServerParams.partition_num"
            :placeholder="'请输入partition_num'">
          </el-input>
        </div>
      </m-list-box>
    </template>
    <template v-if="['elasticsearch'].includes(sourceDataSourceFormType)">
      <m-list-box>
        <div slot="text">{{ '查询JSON' }}</div>
        <div slot="content">
          <div class="form-mirror">
            <textarea
              id="code-json-mirror-source"
              name="code-json-mirror-source"
              style="opacity: 0">
          </textarea>
          </div>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ 'Index' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model.trim="sourceElasticSearchParams.index"
            :placeholder="'请输入index'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ 'Source' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="textarea"
            size="small"
            v-model.trim="sourceElasticSearchParams.source"
            :placeholder="'请输入source'">
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
            v-model.trim="sourceHiveParams.table_name"
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
    <template v-if="['kafka'].includes(sourceDataSourceFormType)">
      <m-list-box>
        <div slot="text">{{ 'Topic' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model.trim="sourceKafkaParams.topic"
            :placeholder="'请输入Topic名称'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ 'ConsumerGroup' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model.trim="sourceKafkaParams['consumer.group']"
            :placeholder="'请输入消费者组名称'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ '每批消费数量' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model.trim="sourceKafkaParams['kafka.config']['max.poll.records']"
            :placeholder="'请输入每批消费数量'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ 'start_mode' }}</div>
        <div slot="content">
          <div style="display: inline-block;">
            <el-select
              style="width: 350px;"
              size="small"
              v-model="sourceKafkaParams.start_mode"
              :disabled="isDetails"
              @change="(val)=> _handleSourceKafkaStartModeChange(val)"
              filterable
            >
              <el-option
                v-for="type in kafkaStartModeOptions"
                :key="type"
                :value="type"
                :label="type">
              </el-option>
            </el-select>
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
              id="code-sql-mirror-sink-before"
              name="code-sql-mirror-sink-before"
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
            v-model.trim="targetHiveParams.table_name"
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
            v-model.trim="targetClickhouseParams.table"
            :placeholder="'请输入ClickHouse表名'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ '前置CK SQL' }}</div>
        <div slot="content">
          <div class="form-mirror">
            <textarea
              id="code-sql-mirror-sink-before"
              name="code-sql-mirror-sink-before"
              style="opacity: 0">
            </textarea>
          </div>
        </div>
      </m-list-box>
    </template>
    <template v-if="['elasticsearch'].includes(targetDataSourceFormType)">
      <m-list-box>
        <div slot="text">{{ 'Index' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model.trim="targetElasticSearchParams.index"
            :placeholder="'请输入Index'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ 'primaryKeys' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model.trim="targetElasticSearchParams.primary_keys"
            :placeholder="'请输入primaryKeys,多个以英文逗号,分隔'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ 'maxBatchSize' }}</div>
        <div slot="content">
          <el-input
            :disabled="isDetails"
            type="text"
            size="small"
            v-model.trim="targetElasticSearchParams.max_batch_size"
            :placeholder="'请输入maxBatchSize'">
          </el-input>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ 'schemaSaveMode' }}</div>
        <div slot="content">
          <div style="display: inline-block;">
            <el-select
              style="width: 350px;"
              size="small"
              v-model="targetElasticSearchParams.schema_save_mode"
              :disabled="isDetails"
              @change="(val)=> _handleTargetElasticSearchSchemaSaveModeChange(val)"
              filterable
            >
              <el-option
                v-for="type in schemaSaveModeOptions"
                :key="type"
                :value="type"
                :label="type">
              </el-option>
            </el-select>
          </div>
        </div>
      </m-list-box>
      <m-list-box>
        <div slot="text">{{ 'dataSaveMode' }}</div>
        <div slot="content">
          <div style="display: inline-block;">
            <el-select
              style="width: 350px;"
              size="small"
              v-model="targetElasticSearchParams.data_save_mode"
              :disabled="isDetails"
              @change="(val)=> _handleTargetElasticSearchDataSaveModeChange(val)"
              filterable
            >
              <el-option
                v-for="type in dataSaveModeOptions"
                :key="type"
                :value="type"
                :label="type">
              </el-option>
            </el-select>
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
  import disabledState from '@/module/mixin/disabledState'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'
  import { diGuiTree, searchTree } from './_source/resourceTree'

  let editorSource
  let editorTarget
  let editorSinkBefore

  export default {
    name: 'seatunnel',
    data () {
      return {
        valueConsistsOf: 'LEAF_PRIORITY',
        sourceSql: '',
        targetSql: '',
        sourceJson: '',
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

        schemaSaveModeOptions: [
          'RECREATE_SCHEMA',
          'CREATE_SCHEMA_WHEN_NOT_EXIST',
          'ERROR_WHEN_SCHEMA_NOT_EXIST'
        ],

        dataSaveModeOptions: [
          'DROP_DATA',
          'APPEND_DATA',
          'ERROR_WHEN_DATA_EXISTS'
        ],

        kafkaStartModeOptions: [
          'earliest',
          'latest',
          'group_offsets'
        ],

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
        sourceElasticSearchParams: {
          query: '',
          index: '',
          source: ''
        },
        sourceKafkaParams: {
          topic: '',
          'consumer.group': '',
          'kafka.config': {
            'max.poll.records': '1000'
          },
          start_mode: 'group_offsets'
        },

        targetHiveParams: {
          table_name: ''
        },
        targetSQLServerParams: {
          query: ''
        },
        targetClickhouseParams: {
          table: ''
        },
        targetElasticSearchParams: {
          index: '',
          primary_keys: '',
          max_batch_size: '',
          schema_save_mode: '',
          data_save_mode: ''
        }
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
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
          if (editorSource.getValue().indexOf(';') === 1 || editorSource.getValue().indexOf('；') === 1) {
            this.$message.warning('查询SQL不能包含分号;')
            return false
          }
          this.sourceSQLServerParams.query = editorSource.getValue()

          if (this.sourceSQLServerParams.partition_num) {
            if (!Number.isInteger(this.sourceSQLServerParams.partition_num) && this.sourceSQLServerParams.partition_num <= 0) {
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
          if (editorSource.getValue().indexOf(';') === 1 || editorSource.getValue().indexOf('；') === 1) {
            this.$message.warning('查询SQL不能包含分号;')
            return false
          }
          this.sourceClickhouseParams.sql = editorSource.getValue()
          requestParams.source = this.sourceClickhouseParams
        } else if (['elasticsearch'].includes(this.sourceDataSourceFormType)) {
          if (!editorSource.getValue()) {
            this.$message.warning('请输入查询Json')
            return false
          }
          this.sourceElasticSearchParams.query = editorSource.getValue()

          if (!this.sourceElasticSearchParams.index) {
            this.$message.warning('请输入索引')
            return false
          } else if (!this.sourceElasticSearchParams.source) {
            this.$message.warning('请输入source')
            return false
          }

          requestParams.source = this.sourceElasticSearchParams
        } else if (['kafka'].includes(this.sourceDataSourceFormType)) {
          if (!this.sourceKafkaParams.topic) {
            this.$message.warning('请输入Topic')
            return false
          }
          if (!this.sourceKafkaParams['consumer.group']) {
            this.$message.warning('请输入消费者组')
            return false
          }
          if (!this.sourceKafkaParams['kafka.config']['max.poll.records']) {
            this.$message.warning('请输入每批消费数量')
            return false
          }
          if (!Number.isInteger(this.sourceKafkaParams['kafka.config']['max.poll.records']) && this.sourceKafkaParams['kafka.config']['max.poll.records'] <= 0) {
            this.$message.warning(`${i18n.$t('Please enter a positive integer') + '每批消费数量'}`)
            return false
          }
          if (!this.sourceKafkaParams.start_mode) {
            this.$message.warning('请选择start_mode')
          }
          requestParams.source = this.sourceKafkaParams
        } else {
          this.$message.warning(`暂不支持的Source数据源类型${this.sourceDataSourceType}`)
          return false
        }

        if (['mysql', 'sqlserver'].includes(this.targetDataSourceFormType)) {
          if (!editorTarget.getValue()) {
            this.$message.warning('请输入写入SQL')
            return false
          }
          if (editorTarget.getValue().indexOf(';') === 1 || editorTarget.getValue().indexOf('；') === 1) {
            this.$message.warning('写入SQL不能包含分号;')
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
        } else if (['elasticsearch'].includes(this.targetDataSourceFormType)) {
          if (this.targetElasticSearchParams.index === '') {
            this.$message.warning('请输入Index')
            return false
          }
          if (this.targetElasticSearchParams.max_batch_size) {
            if (!Number.isInteger(this.targetElasticSearchParams.max_batch_size) && this.targetElasticSearchParams.max_batch_size <= 0) {
              this.$message.warning(`${i18n.$t('Please enter a positive integer') + 'maxBatchSize'}`)
              return false
            }
          }
          if (this.targetElasticSearchParams.schema_save_mode === '') {
            this.$message.warning('请选择schemaSaveMode')
            return false
          }
          if (this.targetElasticSearchParams.data_save_mode === '') {
            this.$message.warning('请选择dataSaveMode')
            return false
          }
          requestParams.sink = this.targetElasticSearchParams
        } else {
          this.$message.warning(`暂不支持的Sink数据源类型${this.targetDataSourceType}`)
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
      _handlerJsonEditorSource () {
        if (editorSource) {
          editorSource.toTextArea() // Uninstall
          editorSource.off($('.code-json-mirror-source'), 'keypress', this.keypress)
          editorSource.off($('.code-json-mirror-source'), 'changes', this.changes)
          editorSource = null
        }

        // editor
        editorSource = codemirror('code-json-mirror-source', {
          mode: 'json',
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
        editorSource.setValue(this.sourceJson)

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
          editorSinkBefore.off($('.code-sql-mirror-sink-before'), 'keypress', this.keypress)
          editorSinkBefore.off($('.code-sql-mirror-sink-before'), 'changes', this.changes)
          editorSinkBefore = null
        }
        // editor
        editorSinkBefore = codemirror('code-sql-mirror-sink-before', {
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
        this._destroyAllEditorSource()
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
        } else if (['elasticsearch'].includes(this.sourceDataSourceType)) {
          this.sourceDataSourceFormType = this.sourceDataSourceType
          setTimeout(() => {
            this._handlerJsonEditorSource()
          }, 200)
        } else if (['kafka'].includes(this.sourceDataSourceType)) {
          this.sourceDataSourceFormType = this.sourceDataSourceType
        } else {
          this.sourceDataSourceFormType = ''
          this.sourceDataSourceType = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceType
          this.$message.warning(`暂不支持的Source数据源类型${this.sourceDataSourceType}`)
        }
      },
      _handleTargetDataSourceTypeChange (val) {
        this.targetDataSourceId = val
        this.targetDataSourceName = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceName
        this.targetDataSourceType = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceType
        this._destroyAllEditorTarget()
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
        } else if (['elasticsearch'].includes(this.targetDataSourceType)) {
          this.targetDataSourceFormType = this.targetDataSourceType
          this.targetElasticSearchParams.max_batch_size = 1000
          this.targetElasticSearchParams.schema_save_mode = 'CREATE_SCHEMA_WHEN_NOT_EXIST'
          this.targetElasticSearchParams.data_save_mode = 'APPEND_DATA'
        } else {
          this.targetDataSourceFormType = ''
          this.targetDataSourceType = this.dataSourceTypeList.filter(item => item.id === val)[0].datasourceType
          this.$message.warning(`暂不支持的Sink数据源类型${this.targetDataSourceType}`)
        }
      },
      _handleTargetElasticSearchSchemaSaveModeChange (val) {
        this.targetElasticSearchParams.schema_save_mode = val
      },
      _handleTargetElasticSearchDataSaveModeChange (val) {
        this.targetElasticSearchParams.data_save_mode = val
      },
      _handleSourceKafkaStartModeChange (val) {
        this.sourceKafkaParams.start_mode = val
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
      },
      _destroyAllEditorSource () {
        if (editorSource) {
          editorSource.toTextArea() // Uninstall
          editorSource.off($('.code-sql-mirror-source'), 'keypress', this.keypress)
          editorSource.off($('.code-sql-mirror-source'), 'changes', this.changes)
          editorSource = null
        }
      },
      _destroyAllEditorTarget () {
        if (editorTarget) {
          editorTarget.toTextArea() // Uninstall
          editorTarget.off($('.code-sql-mirror-target'), 'keypress', this.keypress)
          editorTarget.off($('.code-sql-mirror-target'), 'changes', this.changes)
          editorTarget = null
        }
        if (editorSinkBefore) {
          editorSinkBefore.toTextArea() // Uninstall
          editorSinkBefore.off($('.code-sql-mirror-sink-before'), 'keypress', this.keypress)
          editorSinkBefore.off($('.code-sql-mirror-sink-before'), 'changes', this.changes)
          editorSinkBefore = null
        }
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
          } else if (['elasticsearch'].includes(this.sourceDataSourceType)) {
            this.sourceJson = o.params.source.query || ''
            this.sourceElasticSearchParams.index = o.params.source.index || ''
            this.sourceElasticSearchParams.source = o.params.source.source || ''
          } else if (['kafka'].includes(this.sourceDataSourceType)) {
            this.sourceKafkaParams.topic = o.params.source.topic || ''
            this.sourceKafkaParams['consumer.group'] = o.params.source['consumer.group'] || ''
            this.sourceKafkaParams['kafka.config']['max.poll.records'] = o.params.source['kafka.config']['max.poll.records'] || 1000
            this.sourceKafkaParams.start_mode = o.params.source.start_mode || 'group_offsets'
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
          } else if (['elasticsearch'].includes(this.targetDataSourceType)) {
            this.targetElasticSearchParams.index = o.params.sink.index || ''
            this.targetElasticSearchParams.primary_keys = o.params.sink.primary_keys || ''
            this.targetElasticSearchParams.max_batch_size = o.params.sink.max_batch_size || 1000
            this.targetElasticSearchParams.schema_save_mode = o.params.sink.schema_save_mode || 'CREATE_SCHEMA_WHEN_NOT_EXIST'
            this.targetElasticSearchParams.data_save_mode = o.params.sink.data_save_mode || 'APPEND_DATA'
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
    },
    destroyed () {
      this._destroyAllEditorSource()
      this._destroyAllEditorTarget()
    },
    components: {
      mListBox
    }
  }
</script>
