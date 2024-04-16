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
  <div class="execute-process-instance-model">
    <div class="clearfix list">
      <div class="text">
        {{ '操作类型' }}
      </div>
      <div class="cont">
        <el-select style="width: 250px;" v-model="executeType" size="small">
          <el-option
            v-for="executeType in executeTypeOptions"
            :key="executeType.type"
            :value="executeType.type"
            :label="executeType.desc">
          </el-option>
        </el-select>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{ '项目名称' }}
      </div>
      <div class="cont">
        <el-input
          type="text"
          size="small"
          v-model.trim="projectName"
          style="width: 250px;"
          :placeholder="'请输入项目名称'">
        </el-input>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{ '工作流名称' }}
      </div>
      <div class="cont">
        <el-input
          type="text"
          size="small"
          v-model.trim="processDefinitionName"
          style="width: 250px;"
          :placeholder="'请输入工作流名称'">
        </el-input>
      </div>
    </div>
    <div class="clearfix list">
      <div class="text">
        {{ $t('Schedule date') }}
      </div>
      <div class="cont">
        <el-date-picker
          style="width: 360px"
          v-model="scheduleTime"
          size="small"
          @change="_datepicker"
          type="datetimerange"
          range-separator="-"
          :start-placeholder="$t('startDate')"
          :end-placeholder="$t('endDate')"
          value-format="yyyy-MM-dd HH:mm:ss">
        </el-date-picker>
      </div>
    </div>
    <div class="submit">
      <el-button type="text" size="small" @click="close()"> {{ $t('Cancel') }}</el-button>
      <el-button type="primary" size="small" round :loading="spinnerLoading" @click="ok()">
        {{ spinnerLoading ? $t('Loading...') : $t('Start') }}
      </el-button>
    </div>
  </div>
</template>
<script>
  import store from '@/conf/home/store'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'batch-execute-process-instance',
    data () {
      return {
        store,
        executeType: 'STOP',
        executeTypeOptions: [
          { type: 'REPEAT_RUNNING', desc: '重跑' },
          { type: 'STOP', desc: '停止' }
        ],
        projectName: '',
        processDefinitionName: '',
        scheduleTime: ''
      }
    },
    mixins: [disabledState],
    props: {
    },
    methods: {
      _datepicker (val) {
        this.scheduleTime = val
      },
      _verification () {
        return true
      },
      _start () {
        if (!this._verification()) {
          return
        }
        this.spinnerLoading = true
        let requestParam = {
          projectName: this.projectName,
          processDefinitionName: this.processDefinitionName,
          scheduleTime: this.scheduleTime.length && this.scheduleTime.join(',') || '',
          executeType: this.executeType
        }

        this.store.dispatch('dag/batchExecuteProcessInstance', requestParam).then(res => {
          this.$message.success(res.msg)
          setTimeout(() => {
            this.spinnerLoading = false
            this.close()
          }, 500)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.spinnerLoading = false
        })
      },
      ok () {
        this._start()
      },
      close () {
        this.$emit('closeBatchExecuteProcessInstanceDialog')
      }
    },
    watch: {
    },
    created () {
    },
    mounted () {
    },
    computed: {},
    components: {}
  }
</script>

<style lang="scss" rel="stylesheet/scss">
.execute-process-instance-model {
  width: 660px;
  min-height: 200px;
  background: #fff;
  border-radius: 3px;

  .list {
    margin-bottom: 14px;

    .text {
      width: 140px;
      float: left;
      text-align: right;
      line-height: 32px;
      padding-right: 8px;
    }

    .cont {
      width: 350px;
      float: left;

      .add-email-model {
        padding: 20px;
      }

    }
  }

  .submit {
    text-align: right;
    padding-right: 30px;
    padding-top: 10px;
    padding-bottom: 30px;
  }
}
</style>
