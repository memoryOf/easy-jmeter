<template>
  <el-dialog
    v-model="dialogVisible"
    title="模板管理"
    width="90%"
    :close-on-click-modal="false"
    class="template-manager-dialog"
  >
    <div class="template-manager">
      <!-- 工具栏 -->
      <div class="template-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索模板..."
            size="small"
            style="width: 300px;"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select
            v-model="categoryFilter"
            placeholder="选择分类"
            size="small"
            style="width: 150px; margin-left: 10px;"
            clearable
          >
            <el-option label="全部分类" value="" />
            <el-option
              v-for="category in categories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </div>
        
        <div class="toolbar-right">
          <el-button size="small" type="primary" @click="showCreateTemplate">
            <el-icon><Plus /></el-icon>
            新建模板
          </el-button>
          <el-button size="small" @click="importTemplate">
            <el-icon><Upload /></el-icon>
            导入模板
          </el-button>
        </div>
      </div>
      
      <!-- 模板列表 -->
      <div class="template-content">
        <div class="template-grid">
          <div
            v-for="template in filteredTemplates"
            :key="template.id"
            class="template-card"
            @click="selectTemplate(template)"
            :class="{ 'selected': selectedTemplate?.id === template.id }"
          >
            <div class="card-header">
              <div class="template-icon">
                <i :class="getCategoryIcon(template.category)"></i>
              </div>
              <div class="template-info">
                <div class="template-name">{{ template.name }}</div>
                <div class="template-description">{{ template.description }}</div>
              </div>
              <div class="template-actions">
                <el-dropdown trigger="click" @command="handleTemplateAction">
                  <el-icon class="action-trigger"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{action: 'use', template}">
                        <el-icon><Check /></el-icon>
                        使用模板
                      </el-dropdown-item>
                      <el-dropdown-item :command="{action: 'edit', template}">
                        <el-icon><Edit /></el-icon>
                        编辑模板
                      </el-dropdown-item>
                      <el-dropdown-item :command="{action: 'copy', template}">
                        <el-icon><CopyDocument /></el-icon>
                        复制模板
                      </el-dropdown-item>
                      <el-dropdown-item :command="{action: 'export', template}">
                        <el-icon><Download /></el-icon>
                        导出模板
                      </el-dropdown-item>
                      <el-dropdown-item 
                        :command="{action: 'delete', template}"
                        v-if="!template.isSystem"
                        divided
                      >
                        <el-icon><Delete /></el-icon>
                        删除模板
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            
            <div class="card-body">
              <div class="template-tags">
                <el-tag
                  v-for="tag in template.tags"
                  :key="tag"
                  size="small"
                  type="info"
                >
                  {{ tag }}
                </el-tag>
              </div>
              
              <div class="template-meta">
                <div class="meta-item">
                  <el-icon><User /></el-icon>
                  <span>{{ template.author }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(template.createTime) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><View /></el-icon>
                  <span>{{ template.useCount }} 次使用</span>
                </div>
              </div>
            </div>
            
            <div class="card-footer" v-if="template.isSystem">
              <el-tag size="small" type="warning">系统模板</el-tag>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="filteredTemplates.length === 0" class="empty-state">
          <el-empty description="暂无模板数据">
            <el-button type="primary" @click="showCreateTemplate">创建第一个模板</el-button>
          </el-empty>
        </div>
      </div>
      
      <!-- 预览面板 -->
      <div class="template-preview" v-if="selectedTemplate">
        <div class="preview-header">
          <h3>{{ selectedTemplate.name }}</h3>
          <div class="preview-actions">
            <el-button size="small" @click="previewTemplate">
              <el-icon><View /></el-icon>
              预览结构
            </el-button>
            <el-button size="small" type="primary" @click="useTemplate(selectedTemplate)">
              <el-icon><Check /></el-icon>
              使用此模板
            </el-button>
          </div>
        </div>
        
        <div class="preview-content">
          <div class="template-summary">
            <div class="summary-item">
              <span class="label">分类:</span>
              <span class="value">{{ getCategoryLabel(selectedTemplate.category) }}</span>
            </div>
            <div class="summary-item">
              <span class="label">组件数:</span>
              <span class="value">{{ selectedTemplate.componentCount }}</span>
            </div>
            <div class="summary-item">
              <span class="label">版本:</span>
              <span class="value">{{ selectedTemplate.version }}</span>
            </div>
          </div>
          
          <div class="template-structure">
            <h4>组件结构</h4>
            <template-structure-tree :template="selectedTemplate" />
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button 
          type="primary" 
          @click="useTemplate(selectedTemplate)"
          :disabled="!selectedTemplate"
        >
          使用选中模板
        </el-button>
      </div>
    </template>
  </el-dialog>
  
  <!-- 模板编辑对话框 -->
  <template-editor
    v-model:visible="showEditor"
    :template="editingTemplate"
    @save="handleSaveTemplate"
  />
  
  <!-- 模板预览对话框 -->
  <template-preview
    v-model:visible="showPreview"
    :template="previewingTemplate"
  />
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Upload, MoreFilled, Check, Edit, CopyDocument,
  Download, Delete, User, Calendar, View
} from '@element-plus/icons-vue'
import { get, post, del } from '@/lin/plugin/axios'

// 模板结构树组件
const TemplateStructureTree = {
  name: 'TemplateStructureTree',
  props: {
    template: {
      type: Object,
      required: true
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
        ConstantTimer: 'iconfont icon-timer'
      }
      return iconMap[componentType] || 'iconfont icon-component'
    }
    
    const renderStructure = (component, level = 0) => {
      return {
        ...component,
        level,
        children: component.children?.map(child => renderStructure(child, level + 1)) || []
      }
    }
    
    const structureData = computed(() => {
      if (!props.template.structure) return null
      return renderStructure(props.template.structure)
    })
    
    return {
      structureData,
      getComponentIcon
    }
  },
  template: `
    <div class="structure-tree" v-if="structureData">
      <structure-node :node="structureData" />
    </div>
  `,
  components: {
    StructureNode: {
      name: 'StructureNode',
      props: ['node'],
      setup() {
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
            ConstantTimer: 'iconfont icon-timer'
          }
          return iconMap[componentType] || 'iconfont icon-component'
        }
        
        return { getComponentIcon }
      },
      template: `
        <div class="structure-node" :style="{ paddingLeft: (node.level * 20) + 'px' }">
          <div class="node-item">
            <i :class="getComponentIcon(node.componentType)"></i>
            <span class="node-name">{{ node.name }}</span>
            <span class="node-type">{{ node.componentType }}</span>
          </div>
          <structure-node
            v-for="child in node.children"
            :key="child.id"
            :node="child"
          />
        </div>
      `
    }
  }
}

// 模板编辑器组件（简化版）
const TemplateEditor = {
  name: 'TemplateEditor',
  props: {
    visible: Boolean,
    template: Object
  },
  emits: ['update:visible', 'save'],
  template: `
    <el-dialog v-model="visible" title="编辑模板" width="600px">
      <div>模板编辑器功能待实现</div>
      <template #footer>
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button type="primary" @click="$emit('save', template)">保存</el-button>
      </template>
    </el-dialog>
  `
}

// 模板预览组件（简化版）
const TemplatePreview = {
  name: 'TemplatePreview',
  props: {
    visible: Boolean,
    template: Object
  },
  emits: ['update:visible'],
  template: `
    <el-dialog v-model="visible" title="模板预览" width="800px">
      <div>模板预览功能待实现</div>
      <template #footer>
        <el-button @click="$emit('update:visible', false)">关闭</el-button>
      </template>
    </el-dialog>
  `
}

export default {
  name: 'TemplateManager',
  components: {
    TemplateStructureTree,
    TemplateEditor,
    TemplatePreview,
    Search, Plus, Upload, MoreFilled, Check, Edit, CopyDocument,
    Download, Delete, User, Calendar, View
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:visible', 'use-template'],
  setup(props, { emit }) {
    const searchKeyword = ref('')
    const categoryFilter = ref('')
    const selectedTemplate = ref(null)
    const templates = ref([])
    const loading = ref(false)
    const showEditor = ref(false)
    const showPreview = ref(false)
    const editingTemplate = ref(null)
    const previewingTemplate = ref(null)
    
    // 对话框显示状态
    const dialogVisible = computed({
      get: () => props.visible,
      set: (value) => emit('update:visible', value)
    })
    
    // 分类选项
    const categories = [
      { label: 'HTTP测试', value: 'http' },
      { label: '数据库测试', value: 'database' },
      { label: 'API测试', value: 'api' },
      { label: '性能测试', value: 'performance' },
      { label: '负载测试', value: 'load' },
      { label: '压力测试', value: 'stress' },
      { label: '接口测试', value: 'interface' },
      { label: '其他', value: 'other' }
    ]
    
    // 过滤后的模板列表
    const filteredTemplates = computed(() => {
      let filtered = templates.value
      
      // 关键字过滤
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        filtered = filtered.filter(template =>
          template.name.toLowerCase().includes(keyword) ||
          template.description.toLowerCase().includes(keyword) ||
          template.author.toLowerCase().includes(keyword) ||
          template.tags.some(tag => tag.toLowerCase().includes(keyword))
        )
      }
      
      // 分类过滤
      if (categoryFilter.value) {
        filtered = filtered.filter(template => template.category === categoryFilter.value)
      }
      
      return filtered
    })
    
    // 获取分类图标
    const getCategoryIcon = (category) => {
      const iconMap = {
        http: 'iconfont icon-http',
        database: 'iconfont icon-database',
        api: 'iconfont icon-api',
        performance: 'iconfont icon-performance',
        load: 'iconfont icon-load',
        stress: 'iconfont icon-stress',
        interface: 'iconfont icon-interface',
        other: 'iconfont icon-other'
      }
      return iconMap[category] || 'iconfont icon-template'
    }
    
    // 获取分类标签
    const getCategoryLabel = (category) => {
      const categoryItem = categories.find(c => c.value === category)
      return categoryItem ? categoryItem.label : category
    }
    
    // 格式化日期
    const formatDate = (date) => {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    }
    
    // 加载模板列表
    const loadTemplates = async () => {
      try {
        loading.value = true
        const response = await get('/v1/jmeter/templates')
        
        if (response.success) {
          templates.value = response.templates || []
        } else {
          ElMessage.error('加载模板列表失败: ' + response.message)
        }
      } catch (error) {
        ElMessage.error('加载模板列表失败: ' + error.message)
        // 使用模拟数据
        templates.value = getMockTemplates()
      } finally {
        loading.value = false
      }
    }
    
    // 获取模拟数据
    const getMockTemplates = () => {
      return [
        {
          id: '1',
          name: 'HTTP基础测试模板',
          description: '包含基本的HTTP请求测试组件，适用于RESTful API测试',
          category: 'http',
          tags: ['HTTP', 'API', 'RESTful'],
          author: '系统',
          createTime: '2024-01-01',
          useCount: 156,
          componentCount: 8,
          version: '1.0',
          isSystem: true,
          structure: {
            id: 'root',
            name: '测试计划',
            componentType: 'TestPlan',
            children: [
              {
                id: 'tg1',
                name: '线程组',
                componentType: 'ThreadGroup',
                children: [
                  {
                    id: 'http1',
                    name: 'HTTP请求',
                    componentType: 'HTTPSamplerProxy',
                    children: []
                  }
                ]
              }
            ]
          }
        },
        {
          id: '2',
          name: '性能测试完整模板',
          description: '完整的性能测试模板，包含负载配置、监控和报告组件',
          category: 'performance',
          tags: ['性能', '负载', '监控'],
          author: '张三',
          createTime: '2024-01-15',
          useCount: 89,
          componentCount: 15,
          version: '2.1',
          isSystem: false,
          structure: {
            id: 'root',
            name: '性能测试计划',
            componentType: 'TestPlan',
            children: [
              {
                id: 'tg1',
                name: '负载线程组',
                componentType: 'ThreadGroup',
                children: [
                  {
                    id: 'http1',
                    name: 'API请求',
                    componentType: 'HTTPSamplerProxy',
                    children: []
                  },
                  {
                    id: 'timer1',
                    name: '常量定时器',
                    componentType: 'ConstantTimer',
                    children: []
                  }
                ]
              }
            ]
          }
        },
        {
          id: '3',
          name: 'API接口测试套件',
          description: '专为API接口测试设计的模板，包含认证、参数化等功能',
          category: 'api',
          tags: ['API', '接口', '认证'],
          author: '李四',
          createTime: '2024-02-01',
          useCount: 234,
          componentCount: 12,
          version: '1.3',
          isSystem: false,
          structure: {
            id: 'root',
            name: 'API测试计划',
            componentType: 'TestPlan',
            children: [
              {
                id: 'tg1',
                name: 'API测试组',
                componentType: 'ThreadGroup',
                children: [
                  {
                    id: 'header1',
                    name: 'HTTP信息头管理器',
                    componentType: 'HeaderManager',
                    children: []
                  },
                  {
                    id: 'http1',
                    name: 'API请求',
                    componentType: 'HTTPSamplerProxy',
                    children: []
                  }
                ]
              }
            ]
          }
        }
      ]
    }
    
    // 选择模板
    const selectTemplate = (template) => {
      selectedTemplate.value = template
    }
    
    // 使用模板
    const useTemplate = (template) => {
      if (!template) {
        ElMessage.warning('请先选择一个模板')
        return
      }
      
      emit('use-template', template)
      dialogVisible.value = false
      ElMessage.success(`已应用模板: ${template.name}`)
    }
    
    // 处理模板操作
    const handleTemplateAction = async ({ action, template }) => {
      switch (action) {
        case 'use':
          useTemplate(template)
          break
        case 'edit':
          editingTemplate.value = template
          showEditor.value = true
          break
        case 'copy':
          await copyTemplate(template)
          break
        case 'export':
          await exportTemplate(template)
          break
        case 'delete':
          await deleteTemplate(template)
          break
      }
    }
    
    // 复制模板
    const copyTemplate = async (template) => {
      try {
        const response = await post('/v1/jmeter/templates/copy', { templateId: template.id })
        
        if (response.success) {
          ElMessage.success('模板复制成功')
          await loadTemplates()
        } else {
          ElMessage.error('复制失败: ' + response.message)
        }
      } catch (error) {
        ElMessage.error('复制失败: ' + error.message)
      }
    }
    
    // 导出模板
    const exportTemplate = async (template) => {
      try {
        const response = await get(`/v1/jmeter/templates/${template.id}/export`, {
          responseType: 'blob'
        })
        
        const blob = new Blob([response], { type: 'application/json' })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `${template.name}.json`
        a.click()
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('模板导出成功')
      } catch (error) {
        ElMessage.error('导出失败: ' + error.message)
      }
    }
    
    // 删除模板
    const deleteTemplate = async (template) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除模板 "${template.name}" 吗？此操作无法撤销。`,
          '确认删除',
          {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'warning',
            confirmButtonClass: 'el-button--danger'
          }
        )
        
        const response = await del(`/v1/jmeter/templates/${template.id}`)
        
        if (response.success) {
          ElMessage.success('模板删除成功')
          if (selectedTemplate.value?.id === template.id) {
            selectedTemplate.value = null
          }
          await loadTemplates()
        } else {
          ElMessage.error('删除失败: ' + response.message)
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败: ' + error.message)
        }
      }
    }
    
    // 显示创建模板对话框
    const showCreateTemplate = () => {
      editingTemplate.value = null
      showEditor.value = true
    }
    
    // 导入模板
    const importTemplate = () => {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = '.json'
      input.onchange = async (e) => {
        const file = e.target.files[0]
        if (!file) return
        
        try {
          const text = await file.text()
          const templateData = JSON.parse(text)
          
          const response = await post('/v1/jmeter/templates/import', templateData)
          
          if (response.success) {
            ElMessage.success('模板导入成功')
            await loadTemplates()
          } else {
            ElMessage.error('导入失败: ' + response.message)
          }
        } catch (error) {
          ElMessage.error('导入失败: ' + error.message)
        }
      }
      input.click()
    }
    
    // 预览模板
    const previewTemplate = () => {
      if (!selectedTemplate.value) return
      previewingTemplate.value = selectedTemplate.value
      showPreview.value = true
    }
    
    // 处理保存模板
    const handleSaveTemplate = async (template) => {
      // 保存模板逻辑
      showEditor.value = false
      await loadTemplates()
    }
    
    // 关闭对话框
    const closeDialog = () => {
      dialogVisible.value = false
      selectedTemplate.value = null
    }
    
    // 初始化
    onMounted(() => {
      loadTemplates()
    })
    
    return {
      dialogVisible,
      searchKeyword,
      categoryFilter,
      selectedTemplate,
      templates,
      loading,
      showEditor,
      showPreview,
      editingTemplate,
      previewingTemplate,
      categories,
      filteredTemplates,
      getCategoryIcon,
      getCategoryLabel,
      formatDate,
      selectTemplate,
      useTemplate,
      handleTemplateAction,
      showCreateTemplate,
      importTemplate,
      previewTemplate,
      handleSaveTemplate,
      closeDialog
    }
  }
}
</script>

<style lang="scss" scoped>
.template-manager-dialog {
  ::v-deep .el-dialog {
    margin: 3vh auto;
    height: 94vh;
    max-height: 94vh;
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

.template-manager {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.template-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f8f8;
  border-bottom: 1px solid #e6e6e6;
  
  .toolbar-left {
    display: flex;
    align-items: center;
  }
  
  .toolbar-right {
    display: flex;
    gap: 10px;
  }
}

.template-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.template-grid {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  align-content: start;
}

.template-card {
  background: white;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border-color: #409eff;
  }
  
  &.selected {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }
  
  .card-header {
    display: flex;
    align-items: flex-start;
    padding: 16px;
    gap: 12px;
    
    .template-icon {
      width: 32px;
      height: 32px;
      background: #f0f7ff;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      i {
        font-size: 16px;
        color: #409eff;
      }
    }
    
    .template-info {
      flex: 1;
      min-width: 0;
      
      .template-name {
        font-size: 14px;
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .template-description {
        font-size: 12px;
        color: #666;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }
    
    .template-actions {
      .action-trigger {
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 4px;
        cursor: pointer;
        color: #666;
        
        &:hover {
          background: #f5f5f5;
          color: #409eff;
        }
      }
    }
  }
  
  .card-body {
    padding: 0 16px 12px;
    
    .template-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-bottom: 12px;
    }
    
    .template-meta {
      display: flex;
      justify-content: space-between;
      font-size: 11px;
      color: #999;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;
        
        .el-icon {
          font-size: 12px;
        }
      }
    }
  }
  
  .card-footer {
    padding: 8px 16px;
    background: #fafafa;
    border-top: 1px solid #f0f0f0;
  }
}

.template-preview {
  width: 400px;
  border-left: 1px solid #e6e6e6;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  
  .preview-header {
    padding: 20px;
    border-bottom: 1px solid #e6e6e6;
    
    h3 {
      margin: 0 0 12px 0;
      font-size: 16px;
      color: #333;
    }
    
    .preview-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .preview-content {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    
    .template-summary {
      margin-bottom: 20px;
      
      .summary-item {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid #f0f0f0;
        font-size: 13px;
        
        .label {
          color: #666;
        }
        
        .value {
          color: #333;
          font-weight: 500;
        }
        
        &:last-child {
          border-bottom: none;
        }
      }
    }
    
    .template-structure {
      h4 {
        margin: 0 0 12px 0;
        font-size: 14px;
        color: #333;
      }
      
      .structure-tree {
        .structure-node {
          margin-bottom: 4px;
          
          .node-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 4px 8px;
            background: white;
            border-radius: 4px;
            font-size: 12px;
            
            i {
              color: #409eff;
            }
            
            .node-name {
              font-weight: 500;
              color: #333;
            }
            
            .node-type {
              color: #666;
              font-size: 11px;
              background: #f5f5f5;
              padding: 2px 6px;
              border-radius: 2px;
            }
          }
        }
      }
    }
  }
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-footer {
  text-align: right;
}

::v-deep .el-dropdown-menu {
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      width: 16px;
    }
  }
}
</style>
