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
import i18n from '@/module/i18n/index.js'

const formatName = (str) => {
  if (typeof str !== 'string') return ''

  return (str.length > 16 ? str.slice(0, 8) + '...' + str.slice(-9, str.length) : str)
}

const publishStatusFormat = (status) => {
  return status === 0 ? i18n.$t('offline') : status === 1 ? i18n.$t('online') : '-'
}

const treeFindPath = (treeData, func, path = []) => {
  if (!treeData) return []
  for (const data of treeData) {
    path.push(data.code)
    if (func(data)) {
      return path
    }
    if (data.children) {
      const findChildren = treeFindPath(data.children, func, path)
      if (findChildren.length) return findChildren
    }
    path.pop()
  }
  return []
}

const setTreeData = (treeData, selectedPath) => {
  if (!treeData) return
  for (const data of treeData) {
    if (selectedPath.includes(data.code)) {
      data.itemStyle = {
        color: '#ff0000'
      }
      data.label = {
        color: '#ff0000'
      }
      data.lineStyle = {
        color: '#ff0000'
      }
      data.collapsed = false
    }
    if (data.children) {
      setTreeData(data.children, selectedPath)
    }
    if (data.schedulePublishStatus == 0) {
      data.symbol = 'image://data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAA7AAAAOwBeShxvQAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAATHSURBVFiFrZddbBRVFMf/58zMzna3lBqkaPyi+CRqAiF+RNltBWksaGxMiNH4AEJDpciD0QpGyCZFSaNEE0JFlKAJiT4YfDAFW0O7ZcWYEiMxQQ1iUB8wocR+bNvt7s7c40O7szP70Z2S3rd7zzn/87t3zr1zL8Fnk1hjNRQ2gWQdgFUgqgdQm7xsGaRDMVMKAfytEfVlKXhw0dG+6350qWLit9YvQ8DeB2ALBOFCe/IPq1iUIVqIftFMvGgeTvx6UwDyarOJJdNvQuQNANXl/EoBOOIE0atpIKhpz1J3fMI3gMSa6oDMKQgenwN9BCLDk1dVvbLFgJT1BJs8Zhj8qHks/ntFAImtXwnYpyG4p8CiQNQL0ElAj1Psu2tua7rtsQcUaa+pjGy0U1hWBKHD0sPUEvwo0VMWYHbmQ0XJCQOwVDsdOPdb+Xnm29SOhhckK4ftabWkEMII6g+6V8IBmPnmqbNFy05IAxylWP+Qn+QekLbIN9a4PO2BCNJYOKjdmasJdiy3pPaU/OYCE6J6ZV/DQ/MFCB1NPBOooQ/cY2paFqdsdTrXJ2B2qxn2FbirnZCGwHTFjkJJE3UOXpgvSGpHpDeblCaXNozFeqSqO/79zArM7HN38gGAowBGXTq1YOq7mZUIfpxo5iBGnAEBVNY+CgAsscZqAFvy7qJgqXaK9Q9BSVNJiNi6h+cDQIAyglq7e0xNycpkW1MdQ2GT54Qj6s1VO3UOXigJIap3vhBm9+AXWpCGc30RkMHTHTx7trt5T3p6CwjBAZx29+2sNDOAVV43PV4YOBdE6pWGrX4BNLY/9AxYuJsBWpHPhJHCE64ShJW0j/uFCHT/cNF99ClLQgxIjTMiMlwirgBCrQPkPydEgaykfTy1M7rNDwTrlHXFMnusRFolAeo89zOUPFkEMa4+SbVHtlckKPhpMUDjeaPcXlHAgcBTcH0OUSBrTI6l2qIvz5lfie7oMBSD8KdLOiSxxtv8QczUBFF+TqJA1oT6tBxEZvfaNaLyVUA6TTKAiwXSG/0A5CD0WtnqF8LKsqdYScc/DFC/102e8wsAAFVHzn/uF0Iy8ry7r+l0hjFh9ADIX5dEmmVfdPVCQ6R3RVrslNyasxNBMiHuYnq/bxJEJ/JyxNC4az4AFSF2Rlqzk/jM7c9hXKo5FL8xuw2NAyAkHatgg8Qa9ywURHZUjqm0LHYcCSBTbwVmLyQU67sOJe951ETekf0NmxcCorDpYRoMHY7/6AAAALiuC4RE3o0YhC9lf8Ne8fF+KIKo0baVimKTRqsw5ew076V0b2QpAtoQgOXeMOmH4g7qHPjJD0B6V6QlO4UTalpqPck1WHqNeX/wyNnLJQEAQN6O3gedeyCo9xogIDkDoq+RsXvo3cS/bnNm99o1dpq3qww222nvbRgA2EBWr5aW4JHznl9y6YfJ3shSBPgrgKJlp0lIAbg2edW6S2wY7hOuKLlJo3oo8Ih75o6tpPbBxDCobgOIOmeu5aUoUQXBvSqLQNnkBOjVNBg2pu4olXzWZe4mscblAHUA8hIEiwrtJR+nBOEwLpGpt+aqvVzzXd3yelMYofRGMD8BkdWArABQm7xiB4ihWKcpGPKXpvG3mRB31RyK3/Cj+z+paznO1gBaggAAAABJRU5ErkJggg=='
    }
  }
}

const formatTreeData = (workFlowRelationTree, selectedWorkFlowCode) => {
  const selectedPath = treeFindPath(workFlowRelationTree, data => data.code == selectedWorkFlowCode)
  setTreeData(workFlowRelationTree, selectedPath)
}

export default function (workFlowRelationTree, selectedWorkFlowCode, isShowLabel) {
  formatTreeData(workFlowRelationTree, selectedWorkFlowCode)

  const option = {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      backgroundColor: '#c9c9c9',
      padding: [4, 8],
      formatter: (params) => {
        if (!params.data.name) return ''
        const { name, schedulePublishStatus } = params.data
        const status = publishStatusFormat(schedulePublishStatus)
        return `
          ${i18n.$t('workflowName')}：${name}<br/>
          ${i18n.$t('schedulePublishStatus')}：${status}<br/>
        `
      },
      textStyle: {
        color: '#ff0000'
      }
    },
    series: [{
      type: 'tree',
      data: workFlowRelationTree,
      roam: true,
      top: '11%',
      left: '5%',
      bottom: '5%',
      right: '5%',
      // symbol: 'roundRect',
      symbolSize: 25,
      edgeForkPosition: '70%',
      orient: 'vertical',
      label: {
        show: isShowLabel,
        position: 'top',
        fontSize: 30,
        formatter: (params) => {
          if (!params.data.name) return ''
          return formatName(params.data.name)
        }
      },
      emphasis: {
        focus: 'descendant'
      },
      expandAndCollapse: true,
      initialTreeDepth: 0,
      animationDuration: 550,
      animationDurationUpdate: 750,
      lineStyle: {
        color: '#999999'
      }
    }]
  }

  return option
}
