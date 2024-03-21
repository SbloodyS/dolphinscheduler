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
  <div ref="tree-grid" class="tree-grid"></div>
</template>
<script>
  import * as echarts from 'echarts'
  import { mapState } from 'vuex'
  import treeGridOption from './treeGridOption'
  import clipboard from 'clipboard'

  export default {
    name: 'treeGrid',
    data () {
      return {}
    },
    props: {
      isShowLabel: Boolean
    },
    methods: {
      init () {
      }
    },
    created () {
    },
    mounted () {
      const treeGrid = echarts.init(this.$refs['tree-grid'], null, {
        renderer: 'canvas',
        useDirtyRect: false
      })
      treeGrid.showLoading()
      treeGrid.setOption(
        treeGridOption(
          this.workFlowRelationTree,
          this.selectedWorkFlowCode,
          this.isShowLabel
        ),
        true
      )
      treeGrid.hideLoading()

      treeGrid.on('contextmenu', (params) => {
        clipboard.copy(params.data.name)
        this.$message.success('工作流名称复制成功')
      })
    },
    components: {},
    computed: {
      ...mapState('dag', ['projectCode']),
      ...mapState('kinship', ['selectedWorkFlowCode', 'workFlowRelationTree'])
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
.tree-grid {
  width: 100%;
  height: calc(100vh - 100px);
  background: url("./img/dag_bg.png");
}
</style>
