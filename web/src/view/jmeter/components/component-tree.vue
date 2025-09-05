<template>
  <div class="component-tree">
    <div v-if="!rootComponent" class="empty-state">
      <el-empty description="暂无组件，请先创建测试计划或加载脚本" />
    </div>
    <div v-else class="tree-container">
      <el-tree
        ref="treeRef"
        :data="[rootComponent]"
        :props="treeProps"
        :expand-on-click-node="false"
        :highlight-current="true"
        :allow-drop="allowDrop"
        :allow-drag="allowDrag"
        node-key="id"
        draggable
        @node-click="handleNodeClick"
        @node-contextmenu="handleContextMenu"
        @node-drop="handleDrop"
        @node-drag-start="handleDragStart"
        @node-drag-enter="handleDragEnter"
        @node-drag-leave="handleDragLeave"
        @node-drag-over="handleDragOver"
        @node-drag-end="handleDragEnd"
        class="tree-content"
      >
        <template #default="{ node, data }">
          <div class="tree-node" :class="{ 'selected': data.id === selectedComponent?.id, 'disabled': !data.enabled }">
            <div class="node-content">
              <div class="node-icon">
                <i :class="getComponentIcon(data.componentType)"></i>
              </div>
              <div class="node-label" :title="data.name">
                <span v-if="!data.editing">{{ data.name }}</span>
                <el-input
                  v-else
                  v-model="data.name"
                  size="small"
                  @blur="finishEdit(data)"
                  @keyup.enter="finishEdit(data)"
                  @keyup.escape="cancelEdit(data)"
                  ref="editInput"
                  class="node-edit-input"
                />
              </div>
              <div class="node-status">
                <el-switch
                  v-model="data.enabled"
                  size="small"
                  @change="updateComponent(data)"
                />
              </div>
            </div>
            <div class="node-actions" v-show="!data.editing">
              <el-tooltip content="添加子组件" placement="top">
                <el-button
                  size="small"
                  type="text"
                  @click.stop="showAddDialog(data)"
                  class="action-btn"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="重命名" placement="top">
                <el-button
                  size="small"
                  type="text"
                  @click.stop="startEdit(data)"
                  class="action-btn"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="复制" placement="top">
                <el-button
                  size="small"
                  type="text"
                  @click.stop="copyComponent(data)"
                  class="action-btn"
                >
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top" v-if="data !== rootComponent">
                <el-button
                  size="small"
                  type="text"
                  @click.stop="deleteComponent(data)"
                  class="action-btn danger"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-tree>
    </div>
    
    <!-- 右键菜单 -->
    <div
      v-if="contextMenu.visible"
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
      @click.stop
    >
      <div class="menu-item" @click="showAddDialog(contextMenu.node)">
        <el-icon><Plus /></el-icon>
        <span>添加子组件</span>
      </div>
      <div class="menu-item" @click="startEdit(contextMenu.node)">
        <el-icon><Edit /></el-icon>
        <span>重命名</span>
      </div>
      <div class="menu-item" @click="copyComponent(contextMenu.node)">
        <el-icon><CopyDocument /></el-icon>
        <span>复制</span>
      </div>
      <div class="menu-separator"></div>
      <div class="menu-item" @click="expandChildren(contextMenu.node)">
        <el-icon><ArrowDown /></el-icon>
        <span>展开所有子节点</span>
      </div>
      <div class="menu-item" @click="collapseChildren(contextMenu.node)">
        <el-icon><ArrowUp /></el-icon>
        <span>收起所有子节点</span>
      </div>
      <div class="menu-separator" v-if="contextMenu.node !== rootComponent"></div>
      <div class="menu-item danger" @click="deleteComponent(contextMenu.node)" v-if="contextMenu.node !== rootComponent">
        <el-icon><Delete /></el-icon>
        <span>删除</span>
      </div>
    </div>
    
    <!-- 添加组件对话框 -->
    <el-dialog v-model="addDialog.visible" title="添加组件" width="500px" :close-on-click-modal="false">
      <div class="add-component-dialog">
        <el-input
          v-model="addDialog.search"
          placeholder="搜索组件类型"
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <div class="component-grid">
          <div
            v-for="component in filteredAddComponents"
            :key="component.type"
            class="component-card"
            @click="selectAddComponent(component)"
            :class="{ 'selected': addDialog.selected?.type === component.type }"
          >
            <div class="card-icon">
              <i :class="component.icon"></i>
            </div>
            <div class="card-name">{{ component.name }}</div>
            <div class="card-desc">{{ component.description }}</div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="addDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddComponent" :disabled="!addDialog.selected">
          添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, nextTick, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, CopyDocument, ArrowDown, ArrowUp, Search } from '@element-plus/icons-vue'

export default {
  name: 'ComponentTree',
  components: {
    Plus, Edit, Delete, CopyDocument, ArrowDown, ArrowUp, Search
  },
  props: {
    rootComponent: {
      type: Object,
      default: null
    },
    selectedComponent: {
      type: Object,
      default: null
    }
  },
  emits: ['select-component', 'update-component', 'delete-component', 'move-component'],
  setup(props, { emit }) {
    const treeRef = ref(null)
    const editInput = ref(null)
    
    // 右键菜单
    const contextMenu = ref({
      visible: false,
      x: 0,
      y: 0,
      node: null
    })
    
    // 添加组件对话框
    const addDialog = ref({
      visible: false,
      parent: null,
      search: '',
      selected: null
    })
    
    // 树属性配置
    const treeProps = {
      children: 'children',
      label: 'name'
    }
    
    // 可添加的组件类型
    const addableComponents = [
      { type: 'ThreadGroup', name: '线程组', description: '标准线程组', icon: 'iconfont icon-threadgroup' },
      { type: 'HTTPSamplerProxy', name: 'HTTP请求', description: 'HTTP/HTTPS请求采样器', icon: 'iconfont icon-http' },
      { type: 'HeaderManager', name: 'HTTP信息头管理器', description: 'HTTP请求头配置', icon: 'iconfont icon-header' },
      { type: 'CookieManager', name: 'HTTP Cookie管理器', description: 'Cookie管理器', icon: 'iconfont icon-cookie' },
      { type: 'CSVDataSet', name: 'CSV数据文件设置', description: 'CSV参数化配置', icon: 'iconfont icon-csv' },
      { type: 'ResponseAssertion', name: '响应断言', description: '响应内容断言', icon: 'iconfont icon-assertion' },
      { type: 'ResultCollector', name: '察看结果树', description: '查看请求响应详情', icon: 'iconfont icon-tree' },
      { type: 'ConstantTimer', name: '固定定时器', description: '固定延迟定时器', icon: 'iconfont icon-timer' },
      { type: 'RegexExtractor', name: '正则表达式提取器', description: '正则表达式提取器', icon: 'iconfont icon-regex' },
      { type: 'JSONExtractor', name: 'JSON提取器', description: 'JSON路径提取器', icon: 'iconfont icon-json' }
    ]
    
    // 父子兼容规则（前端约束，贴近 JMeter GUI）
    const allowedChildrenMap = {
      TestPlan: ['ThreadGroup', 'ResultCollector', 'HeaderManager', 'CSVDataSet'],
      ThreadGroup: ['HTTPSamplerProxy', 'HeaderManager', 'CookieManager', 'CSVDataSet', 'ResponseAssertion', 'ResultCollector', 'ConstantTimer', 'RegexExtractor', 'JSONExtractor'],
      HTTPSamplerProxy: ['HeaderManager', 'CookieManager', 'ResponseAssertion', 'RegexExtractor', 'JSONExtractor', 'ConstantTimer'],
      HeaderManager: [],
      CookieManager: [],
      CSVDataSet: [],
      ResponseAssertion: [],
      ResultCollector: [],
      ConstantTimer: [],
      RegexExtractor: [],
      JSONExtractor: []
    }

    const isAllowedChild = (parentType, childType) => {
      const allowed = allowedChildrenMap[parentType]
      if (!allowed) return true
      return allowed.includes(childType)
    }

    // 过滤后的可添加组件（按搜索与父子规则）
    const filteredAddComponents = computed(() => {
      const base = addableComponents.filter(comp => {
        const parentType = addDialog.value.parent?.componentType || 'TestPlan'
        return isAllowedChild(parentType, comp.type)
      })

      if (!addDialog.value.search) return base
      const search = addDialog.value.search.toLowerCase()
      return base.filter(comp =>
        comp.name.toLowerCase().includes(search) ||
        comp.description.toLowerCase().includes(search)
      )
    })
    
    // 获取组件图标
    const getComponentIcon = (componentType) => {
      const iconMap = {
        TestPlan: 'iconfont icon-testplan',
        ThreadGroup: 'iconfont icon-threadgroup',
        HTTPSamplerProxy: 'iconfont icon-http',
        HeaderManager: 'iconfont icon-header',
        CookieManager: 'iconfont icon-cookie',
        CSVDataSet: 'iconfont icon-csv',
        ResponseAssertion: 'iconfont icon-assertion',
        ResultCollector: 'iconfont icon-tree',
        ConstantTimer: 'iconfont icon-timer',
        RegexExtractor: 'iconfont icon-regex',
        JSONExtractor: 'iconfont icon-json'
      }
      return iconMap[componentType] || 'iconfont icon-component'
    }
    
    // 生成唯一ID
    const generateId = () => {
      return 'comp_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    }
    
    // 节点点击
    const handleNodeClick = (data) => {
      emit('select-component', data)
    }
    
    // 右键菜单
    const handleContextMenu = (event, data, node) => {
      event.preventDefault()
      contextMenu.value = {
        visible: true,
        x: event.clientX,
        y: event.clientY,
        node: data
      }
    }
    
    // 隐藏右键菜单
    const hideContextMenu = () => {
      contextMenu.value.visible = false
    }
    
    // 显示添加对话框
    const showAddDialog = (parent) => {
      addDialog.value = {
        visible: true,
        parent: parent,
        search: '',
        selected: null
      }
      hideContextMenu()
    }
    
    // 选择要添加的组件
    const selectAddComponent = (component) => {
      addDialog.value.selected = component
    }
    
    // 确认添加组件
    const confirmAddComponent = () => {
      const parent = addDialog.value.parent
      const componentType = addDialog.value.selected
      
      if (!parent.children) {
        parent.children = []
      }
      
      const newComponent = {
        id: generateId(),
        name: componentType.name,
        componentType: componentType.type,
        enabled: true,
        properties: {},
        children: []
      }
      
      parent.children.push(newComponent)
      emit('update-component', parent)
      
      addDialog.value.visible = false
      ElMessage.success(`添加${componentType.name}成功`)
    }
    
    // 开始编辑
    const startEdit = (data) => {
      data.editing = true
      data.originalName = data.name
      hideContextMenu()
      
      nextTick(() => {
        const input = editInput.value
        if (input) {
          input.focus()
          input.select()
        }
      })
    }
    
    // 完成编辑
    const finishEdit = (data) => {
      if (data.name && data.name.trim()) {
        data.name = data.name.trim()
        delete data.editing
        delete data.originalName
        emit('update-component', data)
      } else {
        cancelEdit(data)
      }
    }
    
    // 取消编辑
    const cancelEdit = (data) => {
      data.name = data.originalName
      delete data.editing
      delete data.originalName
    }
    
    // 复制组件
    const copyComponent = (data) => {
      const copied = JSON.parse(JSON.stringify(data))
      copied.id = generateId()
      copied.name = copied.name + ' - 副本'
      
      // 递归更新子组件ID
      const updateChildIds = (component) => {
        if (component.children) {
          component.children.forEach(child => {
            child.id = generateId()
            updateChildIds(child)
          })
        }
      }
      updateChildIds(copied)
      
      // 添加到父级
      const parent = findParent(props.rootComponent, data.id)
      if (parent && parent.children) {
        const index = parent.children.findIndex(child => child.id === data.id)
        parent.children.splice(index + 1, 0, copied)
        emit('update-component', parent)
        ElMessage.success('复制组件成功')
      }
      
      hideContextMenu()
    }
    
    // 删除组件
    const deleteComponent = (data) => {
      ElMessageBox.confirm('确定要删除该组件吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        emit('delete-component', data)
        hideContextMenu()
      })
    }
    
    // 更新组件
    const updateComponent = (data) => {
      emit('update-component', data)
    }
    
    // 展开子节点
    const expandChildren = (data) => {
      const node = treeRef.value.getNode(data.id)
      if (node) {
        node.expand(() => {}, true)
      }
      hideContextMenu()
    }
    
    // 收起子节点
    const collapseChildren = (data) => {
      const node = treeRef.value.getNode(data.id)
      if (node) {
        node.collapse()
      }
      hideContextMenu()
    }
    
    // 查找父组件
    const findParent = (root, targetId) => {
      if (root.children) {
        for (const child of root.children) {
          if (child.id === targetId) {
            return root
          }
          const parent = findParent(child, targetId)
          if (parent) {
            return parent
          }
        }
      }
      return null
    }
    
    // 拖拽相关
    const allowDrop = (draggingNode, dropNode, type) => {
      // 不能拖到自己的子节点中
      if (type === 'inner' && isChildOf(dropNode.data, draggingNode.data)) return false

      const draggedType = draggingNode.data.componentType
      if (type === 'inner') {
        const parentType = dropNode.data.componentType
        return isAllowedChild(parentType, draggedType)
      }

      // before/after: 检查目标父节点是否允许该子类型
      const parent = findParent(props.rootComponent, dropNode.data.id)
      if (!parent) return true
      const parentType = parent.componentType
      return isAllowedChild(parentType, draggedType)
    }
    
    const allowDrag = (draggingNode) => {
      // 根节点不能拖拽
      return draggingNode.data !== props.rootComponent
    }
    
    const isChildOf = (parent, child) => {
      if (!parent.children) return false
      for (const item of parent.children) {
        if (item.id === child.id || isChildOf(item, child)) {
          return true
        }
      }
      return false
    }
    
    const handleDrop = (draggingNode, dropNode, dropType) => {
      emit('move-component', draggingNode.data, dropNode.data, dropType)
    }
    
    const handleDragStart = () => {}
    const handleDragEnter = () => {}
    const handleDragLeave = () => {}
    const handleDragOver = () => {}
    const handleDragEnd = () => {}
    
    // 外部方法
    const expandAll = () => {
      if (treeRef.value) {
        const nodes = treeRef.value.store.nodesMap
        for (const nodeId in nodes) {
          nodes[nodeId].expand()
        }
      }
    }
    
    const collapseAll = () => {
      if (treeRef.value) {
        const nodes = treeRef.value.store.nodesMap
        for (const nodeId in nodes) {
          if (nodes[nodeId].level > 0) {
            nodes[nodeId].collapse()
          }
        }
      }
    }
    
    // 监听选中组件变化
    watch(() => props.selectedComponent, (newVal) => {
      if (newVal && treeRef.value) {
        treeRef.value.setCurrentKey(newVal.id)
      }
    })
    
    // 生命周期
    onMounted(() => {
      document.addEventListener('click', hideContextMenu)
    })
    
    onBeforeUnmount(() => {
      document.removeEventListener('click', hideContextMenu)
    })
    
    return {
      treeRef,
      editInput,
      treeProps,
      contextMenu,
      addDialog,
      addableComponents,
      filteredAddComponents,
      getComponentIcon,
      handleNodeClick,
      handleContextMenu,
      showAddDialog,
      selectAddComponent,
      confirmAddComponent,
      startEdit,
      finishEdit,
      cancelEdit,
      copyComponent,
      deleteComponent,
      updateComponent,
      expandChildren,
      collapseChildren,
      allowDrop,
      allowDrag,
      handleDrop,
      handleDragStart,
      handleDragEnter,
      handleDragLeave,
      handleDragOver,
      handleDragEnd,
      expandAll,
      collapseAll
    }
  }
}
</script>

<style lang="scss" scoped>
.component-tree {
  height: 100%;
  position: relative;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tree-container {
  height: 100%;
  overflow: auto;
}

.tree-content {
  background: transparent;
  
  ::v-deep .el-tree-node {
    .el-tree-node__content {
      height: auto;
      padding: 2px 0;
      
      &:hover {
        background-color: #f5f7fa;
        
        .node-actions {
          opacity: 1;
        }
      }
    }
    
    .el-tree-node__expand-icon {
      margin-right: 4px;
      color: #666;
    }
    
    .el-tree-node__label {
      flex: 1;
      overflow: visible;
    }
  }
}

.tree-node {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 8px 4px 0;
  border-radius: 4px;
  
  &.selected {
    background-color: #e6f7ff;
    border-left: 3px solid #409eff;
  }
  
  &.disabled {
    opacity: 0.6;
    
    .node-label {
      color: #999;
      text-decoration: line-through;
    }
  }
  
  .node-content {
    flex: 1;
    display: flex;
    align-items: center;
    min-width: 0;
  }
  
  .node-icon {
    width: 20px;
    height: 20px;
    margin-right: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    i {
      font-size: 14px;
      color: #409eff;
    }
  }
  
  .node-label {
    flex: 1;
    font-size: 13px;
    line-height: 1.4;
    min-width: 0;
    
    .node-edit-input {
      ::v-deep .el-input__inner {
        height: 24px;
        line-height: 24px;
        font-size: 12px;
        padding: 0 8px;
      }
    }
  }
  
  .node-status {
    margin-left: 8px;
    
    ::v-deep .el-switch {
      .el-switch__core {
        width: 32px;
        height: 16px;
        
        .el-switch__button {
          width: 12px;
          height: 12px;
        }
      }
    }
  }
  
  .node-actions {
    display: flex;
    align-items: center;
    margin-left: 8px;
    opacity: 0;
    transition: opacity 0.3s ease;
    
    .action-btn {
      padding: 2px;
      margin: 0 1px;
      border: none;
      
      &:hover {
        background-color: #e6f7ff;
      }
      
      &.danger:hover {
        background-color: #ffe6e6;
        color: #f56c6c;
      }
      
      .el-icon {
        font-size: 12px;
      }
    }
  }
}

// 右键菜单
.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #e6e6e6;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  padding: 4px 0;
  min-width: 150px;
  
  .menu-item {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    cursor: pointer;
    font-size: 13px;
    color: #333;
    
    &:hover {
      background-color: #f5f5f5;
    }
    
    &.danger {
      color: #f56c6c;
      
      &:hover {
        background-color: #ffe6e6;
      }
    }
    
    .el-icon {
      margin-right: 8px;
      font-size: 14px;
    }
  }
  
  .menu-separator {
    height: 1px;
    background-color: #e6e6e6;
    margin: 4px 0;
  }
}

// 添加组件对话框
.add-component-dialog {
  .search-input {
    margin-bottom: 16px;
  }
  
  .component-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
    max-height: 300px;
    overflow-y: auto;
    
    .component-card {
      border: 1px solid #e6e6e6;
      border-radius: 6px;
      padding: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      text-align: center;
      
      &:hover {
        border-color: #409eff;
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
      }
      
      &.selected {
        border-color: #409eff;
        background-color: #e6f7ff;
      }
      
      .card-icon {
        margin-bottom: 8px;
        
        i {
          font-size: 24px;
          color: #409eff;
        }
      }
      
      .card-name {
        font-size: 13px;
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }
      
      .card-desc {
        font-size: 11px;
        color: #666;
        line-height: 1.3;
      }
    }
  }
}
</style>
