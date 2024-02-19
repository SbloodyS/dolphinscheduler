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
  <div class="list-model">
    <div class="table-box">
      <el-table :data="list" size="mini" style="width: 100%">
        <el-table-column prop="id" :label="'ID'" width="50"></el-table-column>
        <el-table-column prop="datasourceName" :label="'名称'"></el-table-column>
        <el-table-column prop="datasourceType" :label="'类型'"></el-table-column>
        <el-table-column prop="hostname" :label="'hostname'"></el-table-column>
        <el-table-column prop="port" :label="'port'"></el-table-column>
        <el-table-column prop="userName" :label="'账号'"></el-table-column>
        <el-table-column prop="password" :label="'密码'" v-if="false"></el-table-column>
        <el-table-column prop="databaseName" :label="'databaseName'"></el-table-column>
        <el-table-column prop="schemaName" :label="'schemaName'"></el-table-column>
        <el-table-column prop="driverName" :label="'driverName'"></el-table-column>
        <el-table-column prop="isAble" :label="'是否启用'" align="center">
          <template slot-scope="scope">
            <span>{{_isAbleFormat(scope.row.isAble)}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Description')" min-width="80">
          <template slot-scope="scope">
            <span>{{scope.row.datasourceDesc | filterNull}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Create Time')" min-width="120">
          <template slot-scope="scope">
            <span>{{scope.row.createTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Update Time')" min-width="120">
          <template slot-scope="scope">
            <span>{{scope.row.updateTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="150">
          <template slot-scope="scope">
            <el-tooltip :content="$t('Edit')" placement="top" :enterable="false">
              <span><el-button type="primary" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" circle></el-button></span>
            </el-tooltip>
            <el-tooltip :content="$t('Delete')" placement="top" :enterable="false">
              <el-popconfirm
                :confirmButtonText="$t('Confirm')"
                :cancelButtonText="$t('Cancel')"
                icon="el-icon-info"
                iconColor="red"
                :title="$t('Delete?')"
                @onConfirm="_delete(scope.row,scope.row.id)"
              >
                <el-button type="danger" size="mini" icon="el-icon-delete" circle slot="reference"></el-button>
              </el-popconfirm>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import { findComponentDownward } from '@/module/util/'
  import mTooltipsJSON from '@/module/components/tooltipsJSON/tooltipsJSON'

  export default {
    name: 'datasource-new-list',
    data () {
      return {
        // list
        list: []
      }
    },
    props: {
      // External incoming data
      datasourcesList: Array,
      // current page number
      pageNo: Number,
      // Total number of articles
      pageSize: Number
    },
    methods: {
      ...mapActions('datasourceNew', ['deleteDatasourceNew']),
      /**
       * Delete current line
       */
      _delete (item, i) {
        this.deleteDatasourceNew({
          id: item.id
        }).then(res => {
          this.$emit('on-update')
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * edit
       */
      _edit (item) {
        findComponentDownward(this.$root, 'datasource-new-indexP')._create(item)
      },
      _isAbleFormat (val) {
        return val === 1 ? this.$t('Yes') : this.$t('No')
      }
    },
    watch: {
      /**
       * Monitor external data changes
       */
      datasourcesList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.datasourcesList
    },
    mounted () {
    },
    components: { mTooltipsJSON }
  }
</script>
