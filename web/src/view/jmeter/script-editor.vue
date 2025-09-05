<template>
  <div class="script-editor-container">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="newScript" :loading="loading">
          <el-icon><Plus /></el-icon>
          新建脚本
        </el-button>
        <el-button type="success" @click="saveScript" :loading="saving" :disabled="!hasChanges">
          <el-icon><DocumentChecked /></el-icon>
          保存脚本
        </el-button>
        <el-button type="info" @click="loadScript" :loading="loading">
          <el-icon><FolderOpened /></el-icon>
          加载脚本
        </el-button>
        <el-button type="warning" @click="showPreview" :disabled="!rootComponent">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button @click="showTemplateManager">
          <el-icon><Files /></el-icon>
          模板
        </el-button>
        <el-button type="primary" @click="validateScript" :disabled="!rootComponent || running">
          <el-icon><CircleCheck /></el-icon>
          验证脚本
        </el-button>
        <el-button type="success" @click="runScript" :loading="running" :disabled="!rootComponent">
          <el-icon><Play /></el-icon>
          运行
        </el-button>
        <el-button type="danger" @click="stopScript" :disabled="!running">
          <el-icon><Close /></el-icon>
          停止
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-dropdown @command="exportScript">
          <el-button>
            <el-icon><Download /></el-icon>
            导出
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="jmx">导出为JMX</el-dropdown-item>
              <el-dropdown-item command="json">导出为JSON</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主编辑区域 -->
    <div class="editor-container">
      <!-- 左侧面板 -->
      <div class="left-panel">
        <!-- 组件库 -->
        <div class="panel-section">
          <component-library @add-component="addComponent" />
        </div>
      </div>

      <!-- 中间组件树区域 -->
      <div class="center-panel">
        <div class="tree-header">
          <span class="tree-title">测试计划</span>
          <el-button-group class="tree-actions">
            <el-button size="small" @click="expandAll">展开</el-button>
            <el-button size="small" @click="collapseAll">收缩</el-button>
          </el-button-group>
        </div>
        <div class="tree-content">
          <component-tree 
            ref="componentTreeRef"
            :root-component="rootComponent"
            :selected-component="selectedComponent"
            @select-component="selectComponent"
            @update-component="updateComponent"
            @delete-component="deleteComponent"
            @move-component="moveComponent"
          />
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="right-panel">
        <property-panel 
          :selected-component="selectedComponent"
          @update-property="updateComponentProperty"
        />
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="status-bar">
      <div class="status-left">
        <span class="status-item">组件数量: {{ componentCount }}</span>
        <span class="status-item" v-if="selectedComponent">
          当前选中: {{ selectedComponent.name }}
        </span>
      </div>
      <div class="status-right">
        <span class="status-item" v-if="lastSaved">最后保存: {{ lastSaved }}</span>
        <span class="status-item" :class="{'unsaved': hasChanges}">
          {{ hasChanges ? '未保存' : '已保存' }}
        </span>
      </div>
    </div>

    <!-- 执行结果面板 -->
    <div class="exec-panel" v-show="execPanel.visible">
      <div class="exec-header">
        <div class="exec-title">{{ running ? '正在运行...' : '执行结果' }}</div>
        <div class="exec-actions">
          <el-button size="small" @click="clearExecLog">清空</el-button>
          <el-button size="small" type="text" @click="execPanel.visible=false">收起</el-button>
        </div>
      </div>
      <div class="exec-content">
        <pre class="exec-log">{{ execLog }}</pre>
      </div>
    </div>

    <!-- 预览对话框 -->
    <preview-dialog 
      v-model:visible="previewVisible"
      :root-component="rootComponent"
    />

    <!-- 模板管理对话框 -->
    <template-manager 
      v-model:visible="templateVisible"
      @apply-template="applyTemplate"
    />

    <!-- 加载脚本对话框 -->
    <el-dialog v-model="loadDialogVisible" title="选择用例" width="600px">
      <el-table 
        :data="caseList" 
        @row-click="selectCase"
        highlight-current-row
        max-height="400px"
      >
        <el-table-column prop="name" label="用例名称" />
        <el-table-column prop="project_name" label="项目" width="120" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="update_time" label="更新时间" width="160" />
      </el-table>
      <template #footer>
        <el-button @click="loadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmLoadScript" :disabled="!selectedCase">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, DocumentChecked, FolderOpened, View, Files, 
  Download, ArrowDown, CircleCheck, Play, Close 
} from '@element-plus/icons-vue'
import { get, post } from '@/lin/plugin/axios'
import ComponentLibrary from './components/component-library.vue'
import ComponentTree from './components/component-tree.vue'
import PropertyPanel from './components/property-panel.vue'
import PreviewDialog from './components/preview-dialog.vue'
import TemplateManager from './components/template-manager.vue'

export default {
  name: 'ScriptEditor',
  components: {
    ComponentLibrary,
    ComponentTree,
    PropertyPanel,
    PreviewDialog,
    TemplateManager,
    Plus, DocumentChecked, FolderOpened, View, Files, 
    Download, ArrowDown, CircleCheck, Play, Close
  },
  setup() {
    // 响应式数据
    const loading = ref(false)
    const saving = ref(false)
    const rootComponent = ref(null)
    const selectedComponent = ref(null)
    const hasChanges = ref(false)
    const lastSaved = ref('')
    const running = ref(false)
    const execLog = ref('')
    const execPanel = reactive({ visible: false })
    
    // 对话框状态
    const previewVisible = ref(false)
    const templateVisible = ref(false)
    const loadDialogVisible = ref(false)
    
    // 用例相关
    const caseList = ref([])
    const selectedCase = ref(null)
    const currentCaseId = ref(null)
    
    // 引用
    const componentTreeRef = ref(null)
    
    // 计算属性
    const componentCount = computed(() => {
      if (!rootComponent.value) return 0
      return countComponents(rootComponent.value)
    })
    
    // 工具方法
    const countComponents = (component) => {
      if (!component) return 0
      let count = 1
      if (component.children && component.children.length > 0) {
        count += component.children.reduce((sum, child) => sum + countComponents(child), 0)
      }
      return count
    }
    
    // 创建默认测试计划
    const createDefaultTestPlan = () => {
      return {
        id: generateId(),
        name: '测试计划',
        componentType: 'TestPlan',
        enabled: true,
        properties: {
          'TestPlan.comments': '',
          'TestPlan.functional_mode': false,
          'TestPlan.serialize_threadgroups': false
        },
        children: []
      }
    }
    
    const generateId = () => {
      return 'comp_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    }
    
    // 脚本操作
    const newScript = () => {
      if (hasChanges.value) {
        ElMessageBox.confirm('当前脚本未保存，是否继续？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          createNewScript()
        })
      } else {
        createNewScript()
      }
    }
    
    const createNewScript = () => {
      rootComponent.value = createDefaultTestPlan()
      selectedComponent.value = null
      currentCaseId.value = null
      hasChanges.value = false
      lastSaved.value = ''
      ElMessage.success('新建脚本成功')
    }
    
    const saveScript = async () => {
      if (!currentCaseId.value) {
        ElMessage.warning('请先选择或创建用例')
        return
      }
      
      try {
        saving.value = true
        await post(`/v1/jmeter/script-editor/save/${currentCaseId.value}`, rootComponent.value)
        hasChanges.value = false
        lastSaved.value = new Date().toLocaleString()
        ElMessage.success('保存脚本成功')
      } catch (error) {
        ElMessage.error('保存脚本失败：' + error.message)
      } finally {
        saving.value = false
      }
    }

    // 运行脚本（占位：调用后端运行接口并流式追加日志）
    const runScript = async () => {
      if (!rootComponent.value) return
      running.value = true
      execPanel.visible = true
      execLog.value += `[${new Date().toLocaleTimeString()}] Start run...\n`
      try {
        // 示例：调用后端验证并运行
        await post('/v1/jmeter/script-editor/validate-tree', rootComponent.value)
        execLog.value += 'Validate OK\n'
        // TODO 调用运行接口，建议采用 websocket/sse 获取实时日志
        const res = await post('/v1/jmeter/script-editor/run', { rootComponent: rootComponent.value })
        execLog.value += (res?.message || 'Run finished') + '\n'
      } catch (e) {
        execLog.value += 'Error: ' + (e.message || e) + '\n'
      } finally {
        running.value = false
        execLog.value += `[${new Date().toLocaleTimeString()}] Done.\n`
      }
    }

    const stopScript = async () => {
      try {
        await post('/v1/jmeter/script-editor/stop', {})
        execLog.value += `[${new Date().toLocaleTimeString()}] Stop requested.\n`
      } catch (e) {
        execLog.value += 'Stop error: ' + (e.message || e) + '\n'
      } finally {
        running.value = false
      }
    }

    const clearExecLog = () => {
      execLog.value = ''
    }
    
    const loadScript = async () => {
      try {
        loading.value = true
        const res = await get('/v1/case/all')
        caseList.value = res
        loadDialogVisible.value = true
      } catch (error) {
        ElMessage.error('获取用例列表失败：' + error.message)
      } finally {
        loading.value = false
      }
    }
    
    const selectCase = (row) => {
      selectedCase.value = row
    }
    
    const confirmLoadScript = async () => {
      if (!selectedCase.value) return
      
      try {
        loading.value = true
        const res = await get(`/v1/jmeter/script-editor/load/${selectedCase.value.id}`)
        if (res.success) {
          rootComponent.value = res.rootComponent || createDefaultTestPlan()
          currentCaseId.value = selectedCase.value.id
          hasChanges.value = false
          lastSaved.value = new Date().toLocaleString()
          selectedComponent.value = null
          ElMessage.success('加载脚本成功')
        } else {
          ElMessage.warning(res.message || '该用例暂无脚本')
          rootComponent.value = createDefaultTestPlan()
          currentCaseId.value = selectedCase.value.id
        }
        loadDialogVisible.value = false
      } catch (error) {
        ElMessage.error('加载脚本失败：' + error.message)
      } finally {
        loading.value = false
      }
    }
    
    // 组件操作
    const addComponent = (componentType) => {
      if (!rootComponent.value) {
        ElMessage.warning('请先创建测试计划')
        return
      }
      
      // 创建新组件的逻辑
      const newComponent = {
        id: generateId(),
        name: componentType,
        componentType: componentType,
        enabled: true,
        properties: {},
        children: []
      }
      
      // 添加到选中组件或根组件
      const target = selectedComponent.value || rootComponent.value
      if (!target.children) {
        target.children = []
      }
      target.children.push(newComponent)
      
      hasChanges.value = true
      ElMessage.success(`添加${componentType}组件成功`)
    }
    
    const selectComponent = (component) => {
      selectedComponent.value = component
    }
    
    const updateComponent = (component) => {
      hasChanges.value = true
    }
    
    const deleteComponent = (component) => {
      if (component === rootComponent.value) {
        ElMessage.warning('不能删除根组件')
        return
      }
      
      ElMessageBox.confirm('确定要删除该组件吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 从父组件中移除
        removeFromParent(rootComponent.value, component.id)
        if (selectedComponent.value?.id === component.id) {
          selectedComponent.value = null
        }
        hasChanges.value = true
        ElMessage.success('删除组件成功')
      })
    }
    
    const removeFromParent = (parent, componentId) => {
      if (parent.children) {
        const index = parent.children.findIndex(child => child.id === componentId)
        if (index !== -1) {
          parent.children.splice(index, 1)
          return true
        }
        for (const child of parent.children) {
          if (removeFromParent(child, componentId)) {
            return true
          }
        }
      }
      return false
    }
    
    const moveComponent = (dragComponent, targetComponent, position) => {
      if (!rootComponent.value || !dragComponent || !targetComponent) return

      // 查找并移除拖拽节点
      const { parent: fromParent, index: fromIndex } = findParentAndIndex(rootComponent.value, dragComponent.id) || {}
      if (!fromParent || fromIndex === -1) return
      const [moved] = fromParent.children.splice(fromIndex, 1)

      // 计算目标插入位置
      if (position === 'inner') {
        if (!targetComponent.children) targetComponent.children = []
        targetComponent.children.push(moved)
      } else {
        const { parent: toParent, index: toIndex } = findParentAndIndex(rootComponent.value, targetComponent.id) || {}
        if (!toParent) return
        const insertAt = position === 'before' ? toIndex : toIndex + 1
        if (!toParent.children) toParent.children = []
        toParent.children.splice(insertAt, 0, moved)
      }

      // 选中被移动的节点并标记未保存
      selectedComponent.value = moved
      hasChanges.value = true
    }

    const findParentAndIndex = (parent, id) => {
      if (!parent || !parent.children) return null
      const index = parent.children.findIndex(c => c.id === id)
      if (index !== -1) return { parent, index }
      for (const child of parent.children) {
        const res = findParentAndIndex(child, id)
        if (res) return res
      }
      return null
    }
    
    const updateComponentProperty = (property, value) => {
      if (selectedComponent.value) {
        selectedComponent.value.properties[property] = value
        hasChanges.value = true
      }
    }
    
    // 树操作
    const expandAll = () => {
      componentTreeRef.value?.expandAll()
    }
    
    const collapseAll = () => {
      componentTreeRef.value?.collapseAll()
    }
    
    // 预览和导出
    const showPreview = () => {
      previewVisible.value = true
    }
    
    const exportScript = async (command) => {
      if (!rootComponent.value) {
        ElMessage.warning('没有可导出的脚本')
        return
      }
      
      try {
        if (command === 'jmx') {
          const res = await post('/v1/jmeter/script-editor/export', {
            rootComponent: rootComponent.value,
            filename: 'test-plan.jmx'
          })
          
          if (res.success) {
            // 创建下载链接
            const blob = new Blob([res.content], { type: 'application/xml' })
            const url = window.URL.createObjectURL(blob)
            const a = document.createElement('a')
            a.href = url
            a.download = res.filename
            a.click()
            window.URL.revokeObjectURL(url)
            ElMessage.success('导出成功')
          }
        } else if (command === 'json') {
          const json = JSON.stringify(rootComponent.value, null, 2)
          const blob = new Blob([json], { type: 'application/json' })
          const url = window.URL.createObjectURL(blob)
          const a = document.createElement('a')
          a.href = url
          a.download = 'test-plan.json'
          a.click()
          window.URL.revokeObjectURL(url)
          ElMessage.success('导出成功')
        }
      } catch (error) {
        ElMessage.error('导出失败：' + error.message)
      }
    }
    
    const validateScript = async () => {
      if (!rootComponent.value) return
      
      try {
        const res = await post('/v1/jmeter/script-editor/validate-tree', rootComponent.value)
        if (res.valid) {
          ElMessage.success('脚本验证通过')
        } else {
          const errors = res.errors.map(error => `${error.field}: ${error.message}`).join('\n')
          ElMessageBox.alert(errors, '验证失败', { type: 'error' })
        }
      } catch (error) {
        ElMessage.error('验证失败：' + error.message)
      }
    }
    
    // 模板管理
    const showTemplateManager = () => {
      templateVisible.value = true
    }
    
    const applyTemplate = (templateComponent) => {
      if (hasChanges.value) {
        ElMessageBox.confirm('当前脚本未保存，是否继续？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          rootComponent.value = templateComponent
          selectedComponent.value = null
          hasChanges.value = true
          templateVisible.value = false
          ElMessage.success('应用模板成功')
        })
      } else {
        rootComponent.value = templateComponent
        selectedComponent.value = null
        hasChanges.value = true
        templateVisible.value = false
        ElMessage.success('应用模板成功')
      }
    }
    
    // 生命周期
    onMounted(() => {
      // 初始化默认脚本
      rootComponent.value = createDefaultTestPlan()
      // 保存快捷键 Cmd/Ctrl + S
      window.addEventListener('keydown', handleSaveShortcut)
    })

    const handleSaveShortcut = (e) => {
      const isSave = (e.metaKey || e.ctrlKey) && (e.key === 's' || e.key === 'S')
      if (isSave) {
        e.preventDefault()
        if (!saving.value) {
          saveScript()
        }
      }
    }
    
    return {
      loading,
      saving,
      rootComponent,
      selectedComponent,
      hasChanges,
      lastSaved,
      componentCount,
      previewVisible,
      templateVisible,
      loadDialogVisible,
      caseList,
      selectedCase,
      currentCaseId,
      componentTreeRef,
      
      // 方法
      newScript,
      saveScript,
      loadScript,
      selectCase,
      confirmLoadScript,
      addComponent,
      selectComponent,
      updateComponent,
      deleteComponent,
      moveComponent,
      updateComponentProperty,
      expandAll,
      collapseAll,
      showPreview,
      exportScript,
      validateScript,
      showTemplateManager,
      applyTemplate,
      // run panel
      running,
      runScript,
      stopScript,
      execPanel,
      execLog,
      clearExecLog
    }
  }
}
</script>

<style lang="scss" scoped>
.script-editor-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: white;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 9;

  .toolbar-left, .toolbar-right {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
}

.editor-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.left-panel {
  width: 220px;
  background: white;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  
  .panel-section {
    flex: 1;
    overflow: auto;
  }
}

.center-panel {
  flex: 1;
  background: white;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  
  .tree-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: #fafafa;
    border-bottom: 1px solid #e6e6e6;
    
    .tree-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }
  
  .tree-content {
    flex: 1;
    overflow: auto;
    padding: 10px;
  }
}

.right-panel {
  width: 420px;
  background: white;
}

.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 20px;
  background: #f8f8f8;
  border-top: 1px solid #e6e6e6;
  font-size: 12px;
  color: #666;
  
  .status-left, .status-right {
    display: flex;
    gap: 20px;
  }
  
  .status-item {
    &.unsaved {
      color: #f56c6c;
      font-weight: 500;
    }
  }
}

.exec-panel {
  height: 200px;
  background: #111;
  color: #e6e6e6;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  border-top: 1px solid #333;
  display: flex;
  flex-direction: column;
  
  .exec-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 10px;
    background: #1a1a1a;
    border-bottom: 1px solid #333;
    
    .exec-title { font-size: 12px; }
    .exec-actions { display: flex; gap: 8px; }
  }
  .exec-content {
    flex: 1;
    overflow: auto;
    padding: 8px 10px;
    
    .exec-log { white-space: pre-wrap; margin: 0; font-size: 12px; }
  }
}

::v-deep .el-button-group {
  .el-button {
    padding: 5px 10px;
  }
}
</style>
