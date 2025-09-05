<template>
  <el-dialog
    v-model="dialogVisible"
    title="脚本预览"
    width="80%"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="preview-dialog"
  >
    <div class="preview-container">
      <!-- 工具栏 -->
      <div class="preview-toolbar">
        <div class="toolbar-left">
          <el-button-group>
            <el-button
              :type="previewMode === 'tree' ? 'primary' : ''"
              size="small"
              @click="previewMode = 'tree'"
            >
              <el-icon><List /></el-icon>
              树形预览
            </el-button>
            <el-button
              :type="previewMode === 'jmx' ? 'primary' : ''"
              size="small"
              @click="previewMode = 'jmx'"
            >
              <el-icon><Document /></el-icon>
              JMX源码
            </el-button>
            <el-button
              :type="previewMode === 'json' ? 'primary' : ''"
              size="small"
              @click="previewMode = 'json'"
            >
              <el-icon><Tickets /></el-icon>
              JSON格式
            </el-button>
          </el-button-group>
        </div>
        
        <div class="toolbar-right">
          <el-tooltip content="刷新预览" placement="top">
            <el-button size="small" @click="refreshPreview" :loading="loading">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="复制内容" placement="top">
            <el-button size="small" @click="copyContent">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="下载文件" placement="top">
            <el-button size="small" @click="downloadFile">
              <el-icon><Download /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="格式化" placement="top" v-if="previewMode !== 'tree'">
            <el-button size="small" @click="formatContent">
              <el-icon><MagicStick /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
      
      <!-- 预览内容 -->
      <div class="preview-content">
        <!-- 树形预览 -->
        <div v-if="previewMode === 'tree'" class="tree-preview">
          <div class="tree-summary">
            <div class="summary-item">
              <span class="label">组件总数:</span>
              <span class="value">{{ componentCount }}</span>
            </div>
            <div class="summary-item">
              <span class="label">最后更新:</span>
              <span class="value">{{ lastUpdated }}</span>
            </div>
          </div>
          
          <div class="tree-structure">
            <tree-preview-node
              v-if="rootComponent"
              :component="rootComponent"
              :level="0"
            />
          </div>
        </div>
        
        <!-- JMX源码预览 -->
        <div v-else-if="previewMode === 'jmx'" class="code-preview">
          <div v-if="loading" class="loading-state">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在生成JMX脚本...</span>
          </div>
          <div v-else-if="error" class="error-state">
            <el-icon><WarningFilled /></el-icon>
            <span>{{ error }}</span>
          </div>
          <div v-else class="code-content">
            <pre><code class="language-xml">{{ jmxContent }}</code></pre>
          </div>
        </div>
        
        <!-- JSON格式预览 -->
        <div v-else-if="previewMode === 'json'" class="code-preview">
          <div class="code-content">
            <pre><code class="language-json">{{ jsonContent }}</code></pre>
          </div>
        </div>
      </div>
      
      <!-- 状态栏 -->
      <div class="preview-statusbar">
        <div class="status-left">
          <span class="status-item">
            模式: {{ getModeLabel(previewMode) }}
          </span>
          <span class="status-item" v-if="previewMode !== 'tree'">
            大小: {{ getContentSize() }}
          </span>
        </div>
        <div class="status-right">
          <span class="status-item" v-if="previewMode === 'jmx' && jmxContent">
            行数: {{ jmxContent.split('\\n').length }}
          </span>
          <span class="status-item" v-if="previewMode === 'json' && jsonContent">
            行数: {{ jsonContent.split('\\n').length }}
          </span>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" @click="exportScript">导出脚本</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  List, Document, Tickets, Refresh, CopyDocument, Download, 
  MagicStick, Loading, WarningFilled 
} from '@element-plus/icons-vue'
import { post } from '@/lin/plugin/axios'

// 树形预览节点组件
const TreePreviewNode = {
  name: 'TreePreviewNode',
  props: {
    component: {
      type: Object,
      required: true
    },
    level: {
      type: Number,
      default: 0
    }
  },
  setup(props) {
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
    
    const getPropertiesCount = (component) => {
      return component.properties ? Object.keys(component.properties).length : 0
    }
    
    return {
      getComponentIcon,
      getPropertiesCount
    }
  },
  template: `
    <div class="tree-node" :style="{ paddingLeft: (level * 20) + 'px' }">
      <div class="node-content">
        <div class="node-icon">
          <i :class="getComponentIcon(component.componentType)"></i>
        </div>
        <div class="node-info">
          <div class="node-name">{{ component.name }}</div>
          <div class="node-details">
            <span class="node-type">{{ component.componentType }}</span>
            <span class="node-status" :class="{ 'disabled': !component.enabled }">
              {{ component.enabled ? '启用' : '禁用' }}
            </span>
            <span class="node-props" v-if="getPropertiesCount(component) > 0">
              {{ getPropertiesCount(component) }} 个属性
            </span>
          </div>
        </div>
      </div>
      
      <!-- 子组件 -->
      <tree-preview-node
        v-for="child in component.children"
        :key="child.id"
        :component="child"
        :level="level + 1"
      />
    </div>
  `
}

export default {
  name: 'PreviewDialog',
  components: {
    TreePreviewNode,
    List, Document, Tickets, Refresh, CopyDocument, Download, 
    MagicStick, Loading, WarningFilled
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    rootComponent: {
      type: Object,
      default: null
    }
  },
  emits: ['update:visible'],
  setup(props, { emit }) {
    const previewMode = ref('tree')
    const loading = ref(false)
    const error = ref('')
    const jmxContent = ref('')
    const lastUpdated = ref('')
    
    // 对话框显示状态
    const dialogVisible = computed({
      get: () => props.visible,
      set: (value) => emit('update:visible', value)
    })
    
    // 组件数量
    const componentCount = computed(() => {
      if (!props.rootComponent) return 0
      return countComponents(props.rootComponent)
    })
    
    // JSON内容
    const jsonContent = computed(() => {
      if (!props.rootComponent) return ''
      return JSON.stringify(props.rootComponent, null, 2)
    })
    
    // 计算组件数量
    const countComponents = (component) => {
      if (!component) return 0
      let count = 1
      if (component.children && component.children.length > 0) {
        count += component.children.reduce((sum, child) => sum + countComponents(child), 0)
      }
      return count
    }
    
    // 获取模式标签
    const getModeLabel = (mode) => {
      const labels = {
        tree: '树形结构',
        jmx: 'JMX源码',
        json: 'JSON格式'
      }
      return labels[mode] || mode
    }
    
    // 获取内容大小
    const getContentSize = () => {
      let content = ''
      if (previewMode.value === 'jmx') {
        content = jmxContent.value
      } else if (previewMode.value === 'json') {
        content = jsonContent.value
      }
      
      const bytes = new Blob([content]).size
      if (bytes < 1024) {
        return `${bytes} B`
      } else if (bytes < 1024 * 1024) {
        return `${(bytes / 1024).toFixed(1)} KB`
      } else {
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
      }
    }
    
    // 刷新预览
    const refreshPreview = async () => {
      if (previewMode.value === 'jmx') {
        await generateJmxContent()
      }
      lastUpdated.value = new Date().toLocaleTimeString()
    }
    
    // 生成JMX内容
    const generateJmxContent = async () => {
      if (!props.rootComponent) {
        jmxContent.value = ''
        return
      }
      
      try {
        loading.value = true
        error.value = ''
        
        const response = await post('/v1/jmeter/script-editor/generate-jmx', props.rootComponent)
        
        if (response.success) {
          jmxContent.value = response.jmxContent || ''
        } else {
          error.value = response.message || '生成JMX脚本失败'
        }
      } catch (err) {
        error.value = '生成JMX脚本失败: ' + err.message
        jmxContent.value = ''
      } finally {
        loading.value = false
      }
    }
    
    // 复制内容
    const copyContent = async () => {
      let content = ''
      
      if (previewMode.value === 'tree') {
        ElMessage.warning('树形预览模式无法复制内容')
        return
      } else if (previewMode.value === 'jmx') {
        content = jmxContent.value
      } else if (previewMode.value === 'json') {
        content = jsonContent.value
      }
      
      if (!content) {
        ElMessage.warning('没有可复制的内容')
        return
      }
      
      try {
        await navigator.clipboard.writeText(content)
        ElMessage.success('内容已复制到剪贴板')
      } catch (err) {
        // 降级到传统方法
        const textArea = document.createElement('textarea')
        textArea.value = content
        document.body.appendChild(textArea)
        textArea.focus()
        textArea.select()
        
        try {
          document.execCommand('copy')
          ElMessage.success('内容已复制到剪贴板')
        } catch (e) {
          ElMessage.error('复制失败，请手动复制')
        }
        
        document.body.removeChild(textArea)
      }
    }
    
    // 下载文件
    const downloadFile = () => {
      let content = ''
      let filename = ''
      let contentType = ''
      
      if (previewMode.value === 'tree') {
        ElMessage.warning('树形预览模式无法下载')
        return
      } else if (previewMode.value === 'jmx') {
        content = jmxContent.value
        filename = 'test-plan.jmx'
        contentType = 'application/xml'
      } else if (previewMode.value === 'json') {
        content = jsonContent.value
        filename = 'test-plan.json'
        contentType = 'application/json'
      }
      
      if (!content) {
        ElMessage.warning('没有可下载的内容')
        return
      }
      
      const blob = new Blob([content], { type: contentType })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      a.click()
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('文件下载成功')
    }
    
    // 格式化内容
    const formatContent = () => {
      if (previewMode.value === 'json') {
        try {
          const parsed = JSON.parse(jsonContent.value)
          // JSON内容已经是格式化的
          ElMessage.success('JSON内容已格式化')
        } catch (e) {
          ElMessage.error('JSON格式不正确，无法格式化')
        }
      } else if (previewMode.value === 'jmx') {
        // JMX格式化相对复杂，这里只是示例
        ElMessage.info('XML格式化功能待完善')
      }
    }
    
    // 导出脚本
    const exportScript = async () => {
      if (!props.rootComponent) {
        ElMessage.warning('没有可导出的脚本')
        return
      }
      
      try {
        const response = await post('/v1/jmeter/script-editor/export', {
          rootComponent: props.rootComponent,
          filename: 'test-plan.jmx'
        })
        
        if (response.success) {
          const blob = new Blob([response.content], { type: response.contentType })
          const url = window.URL.createObjectURL(blob)
          const a = document.createElement('a')
          a.href = url
          a.download = response.filename
          a.click()
          window.URL.revokeObjectURL(url)
          
          ElMessage.success('脚本导出成功')
        } else {
          ElMessage.error('导出失败: ' + response.message)
        }
      } catch (error) {
        ElMessage.error('导出失败: ' + error.message)
      }
    }
    
    // 关闭对话框
    const closeDialog = () => {
      dialogVisible.value = false
    }
    
    // 监听预览模式变化
    watch(previewMode, (newMode) => {
      if (newMode === 'jmx' && !jmxContent.value) {
        generateJmxContent()
      }
    })
    
    // 监听对话框显示状态
    watch(() => props.visible, (visible) => {
      if (visible) {
        lastUpdated.value = new Date().toLocaleTimeString()
        if (previewMode.value === 'jmx') {
          generateJmxContent()
        }
      }
    })
    
    return {
      dialogVisible,
      previewMode,
      loading,
      error,
      jmxContent,
      jsonContent,
      componentCount,
      lastUpdated,
      getModeLabel,
      getContentSize,
      refreshPreview,
      copyContent,
      downloadFile,
      formatContent,
      exportScript,
      closeDialog
    }
  }
}
</script>

<style lang="scss" scoped>
.preview-dialog {
  ::v-deep .el-dialog {
    margin: 5vh auto;
    height: 90vh;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    
    .el-dialog__body {
      flex: 1;
      padding: 0;
      overflow: hidden;
    }
    
    .el-dialog__footer {
      padding: 15px 20px;
      border-top: 1px solid #e6e6e6;
    }
  }
}

.preview-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.preview-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #f8f8f8;
  border-bottom: 1px solid #e6e6e6;
  
  .toolbar-left, .toolbar-right {
    display: flex;
    gap: 8px;
  }
}

.preview-content {
  flex: 1;
  overflow: hidden;
  background: white;
}

.tree-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .tree-summary {
    display: flex;
    gap: 20px;
    padding: 15px 20px;
    background: #fafafa;
    border-bottom: 1px solid #e6e6e6;
    font-size: 13px;
    
    .summary-item {
      display: flex;
      align-items: center;
      gap: 5px;
      
      .label {
        color: #666;
      }
      
      .value {
        color: #333;
        font-weight: 500;
      }
    }
  }
  
  .tree-structure {
    flex: 1;
    overflow: auto;
    padding: 15px 20px;
    
    .tree-node {
      margin-bottom: 8px;
      
      .node-content {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        border-radius: 4px;
        background: #f9f9f9;
        border-left: 3px solid #409eff;
        
        .node-icon {
          width: 20px;
          height: 20px;
          margin-right: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          
          i {
            font-size: 16px;
            color: #409eff;
          }
        }
        
        .node-info {
          flex: 1;
          
          .node-name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 4px;
          }
          
          .node-details {
            display: flex;
            gap: 12px;
            font-size: 12px;
            
            .node-type {
              color: #666;
              background: #e6f7ff;
              padding: 2px 6px;
              border-radius: 2px;
            }
            
            .node-status {
              color: #52c41a;
              
              &.disabled {
                color: #f5222d;
              }
            }
            
            .node-props {
              color: #999;
            }
          }
        }
      }
    }
  }
}

.code-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .loading-state, .error-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: #666;
    
    .el-icon {
      font-size: 24px;
    }
  }
  
  .error-state {
    color: #f56c6c;
  }
  
  .code-content {
    flex: 1;
    overflow: auto;
    
    pre {
      margin: 0;
      padding: 20px;
      background: #f8f8f8;
      font-size: 12px;
      line-height: 1.6;
      font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
      
      code {
        white-space: pre-wrap;
        word-break: break-all;
        
        &.language-xml {
          color: #333;
        }
        
        &.language-json {
          color: #333;
        }
      }
    }
  }
}

.preview-statusbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 15px;
  background: #f8f8f8;
  border-top: 1px solid #e6e6e6;
  font-size: 12px;
  color: #666;
  
  .status-left, .status-right {
    display: flex;
    gap: 15px;
  }
  
  .status-item {
    display: flex;
    align-items: center;
  }
}

.dialog-footer {
  text-align: right;
}

::v-deep .el-button-group {
  .el-button {
    padding: 6px 12px;
    
    .el-icon {
      margin-right: 4px;
    }
  }
}
</style>
