<template>
  <div class="property-panel">
    <div class="panel-header">
      <h3>属性面板</h3>
      <div class="header-actions" v-if="selectedComponent">
        <el-tooltip content="重置属性" placement="top">
          <el-button size="small" type="text" @click="resetProperties">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="帮助文档" placement="top">
          <el-button size="small" type="text" @click="showHelp">
            <el-icon><QuestionFilled /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
    
    <div class="panel-content">
      <div v-if="!selectedComponent" class="empty-state">
        <el-empty description="请选择一个组件来编辑属性" />
      </div>
      
      <div v-else class="property-form">
        <!-- 基本信息 -->
        <div class="property-section">
          <div class="section-header">
            <span class="section-title">基本信息</span>
          </div>
          <div class="section-content">
            <div class="form-row">
              <label class="form-label">组件名称</label>
              <el-input
                v-model="selectedComponent.name"
                placeholder="输入组件名称"
                size="small"
                @blur="updateProperty('name', selectedComponent.name)"
              />
            </div>
            <div class="form-row">
              <label class="form-label">组件类型</label>
              <el-input
                :value="selectedComponent.componentType"
                size="small"
                readonly
                disabled
              />
            </div>
            <div class="form-row">
              <label class="form-label">是否启用</label>
              <el-switch
                v-model="selectedComponent.enabled"
                @change="updateProperty('enabled', selectedComponent.enabled)"
              />
            </div>
          </div>
        </div>
        
        <!-- 组件属性（HTTP请求采用JMeter风格Tab） -->
        <div class="property-section" v-if="selectedComponent && selectedComponent.componentType === 'HTTPSamplerProxy'">
          <div class="section-header">
            <span class="section-title">HTTP Request</span>
          </div>
          <div class="section-content">
            <el-tabs v-model="httpActiveTab" type="border-card">
              <el-tab-pane label="Basic" name="basic">
                <div class="property-group">
                  <div class="form-row">
                    <label class="form-label">协议</label>
                    <el-select v-model="propertyValues['HTTPSampler.protocol']" size="small" @change="updateProperty('HTTPSampler.protocol', propertyValues['HTTPSampler.protocol'])">
                      <el-option label="HTTP" value="http" />
                      <el-option label="HTTPS" value="https" />
                    </el-select>
                  </div>
                  <div class="form-row">
                    <label class="form-label">服务器名称或IP</label>
                    <el-input v-model="propertyValues['HTTPSampler.domain']" size="small" placeholder="example.com" @blur="updateProperty('HTTPSampler.domain', propertyValues['HTTPSampler.domain'])" />
                  </div>
                  <div class="form-row two-cols">
                    <div>
                      <label class="form-label">端口号</label>
                      <el-input-number v-model="propertyValues['HTTPSampler.port']" :min="1" :max="65535" size="small" @change="updateProperty('HTTPSampler.port', propertyValues['HTTPSampler.port'])" />
                    </div>
                    <div>
                      <label class="form-label">内容编码</label>
                      <el-select v-model="propertyValues['HTTPSampler.contentEncoding']" size="small" @change="updateProperty('HTTPSampler.contentEncoding', propertyValues['HTTPSampler.contentEncoding'])">
                        <el-option label="UTF-8" value="UTF-8" />
                        <el-option label="GBK" value="GBK" />
                        <el-option label="ISO-8859-1" value="ISO-8859-1" />
                      </el-select>
                    </div>
                  </div>
                  <div class="form-row two-cols">
                    <div>
                      <label class="form-label">HTTP方法</label>
                      <el-select v-model="propertyValues['HTTPSampler.method']" size="small" @change="updateProperty('HTTPSampler.method', propertyValues['HTTPSampler.method'])">
                        <el-option label="GET" value="GET" />
                        <el-option label="POST" value="POST" />
                        <el-option label="PUT" value="PUT" />
                        <el-option label="DELETE" value="DELETE" />
                        <el-option label="PATCH" value="PATCH" />
                        <el-option label="HEAD" value="HEAD" />
                        <el-option label="OPTIONS" value="OPTIONS" />
                      </el-select>
                    </div>
                    <div>
                      <label class="form-label">路径</label>
                      <el-input v-model="propertyValues['HTTPSampler.path']" size="small" placeholder="/api/users" @blur="updateProperty('HTTPSampler.path', propertyValues['HTTPSampler.path'])" />
                    </div>
                  </div>
                </div>
              </el-tab-pane>
              <el-tab-pane label="Parameters" name="params">
                <div class="request-table">
                  <div class="table-toolbar">
                    <el-button size="small" @click="addParamRow" type="primary">Add</el-button>
                    <el-button size="small" @click="removeSelectedParams">Delete</el-button>
                  </div>
                  <el-table :data="httpParams" @selection-change="sel=>paramSelection=sel" size="small" border>
                    <el-table-column type="selection" width="48" />
                    <el-table-column label="Name" width="220">
                      <template #default="{row}">
                        <el-input v-model="row.name" size="small" />
                      </template>
                    </el-table-column>
                    <el-table-column label="Value">
                      <template #default="{row}">
                        <el-input v-model="row.value" size="small" />
                      </template>
                    </el-table-column>
                    <el-table-column label="URL Encode?" width="110">
                      <template #default="{row}">
                        <el-checkbox v-model="row.encode" />
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-tab-pane>
              <el-tab-pane label="Body Data" name="body">
                <el-input type="textarea" :rows="8" v-model="propertyValues['HTTPSampler.postBodyRaw']" placeholder="请求体内容" @blur="updateProperty('HTTPSampler.postBodyRaw', propertyValues['HTTPSampler.postBodyRaw'])" />
              </el-tab-pane>
              <el-tab-pane label="Files Upload" name="files">
                <div class="request-table">
                  <div class="table-toolbar">
                    <el-button size="small" @click="addFileRow" type="primary">Add</el-button>
                    <el-button size="small" @click="removeSelectedFiles">Delete</el-button>
                  </div>
                  <el-table :data="httpFiles" @selection-change="sel=>fileSelection=sel" size="small" border>
                    <el-table-column type="selection" width="48" />
                    <el-table-column label="Param Name" width="160">
                      <template #default="{row}"><el-input v-model="row.param" size="small" /></template>
                    </el-table-column>
                    <el-table-column label="File Path">
                      <template #default="{row}"><el-input v-model="row.path" size="small" /></template>
                    </el-table-column>
                    <el-table-column label="MIME Type" width="160">
                      <template #default="{row}"><el-input v-model="row.mime" size="small" /></template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-tab-pane>
              <el-tab-pane label="Advanced" name="advanced">
                <div class="property-group">
                  <div class="form-row two-cols">
                    <div>
                      <label class="form-label">连接超时(ms)</label>
                      <el-input-number v-model="propertyValues['HTTPSampler.connect_timeout']" :min="0" size="small" @change="updateProperty('HTTPSampler.connect_timeout', propertyValues['HTTPSampler.connect_timeout'])" />
                    </div>
                    <div>
                      <label class="form-label">响应超时(ms)</label>
                      <el-input-number v-model="propertyValues['HTTPSampler.response_timeout']" :min="0" size="small" @change="updateProperty('HTTPSampler.response_timeout', propertyValues['HTTPSampler.response_timeout'])" />
                    </div>
                  </div>
                  <div class="form-row">
                    <el-checkbox v-model="propertyValues['HTTPSampler.follow_redirects']" @change="updateProperty('HTTPSampler.follow_redirects', propertyValues['HTTPSampler.follow_redirects'])">跟随重定向</el-checkbox>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
        
        <!-- 其他组件复用原折叠式表单 -->
        <div class="property-section" v-else-if="componentProperties.length > 0">
          <div class="section-header">
            <span class="section-title">组件属性</span>
            <el-button size="small" type="text" @click="expandAll">{{ allExpanded ? '收起全部' : '展开全部' }}</el-button>
          </div>
          <div class="section-content">
            <el-collapse v-model="activeProperties" accordion>
              <el-collapse-item v-for="group in componentProperties" :key="group.name" :title="group.title" :name="group.name">
                <div class="property-group">
                  <div v-for="property in group.properties" :key="property.key" class="form-row">
                    <label class="form-label" :title="property.description">{{ property.name }}<span class="required" v-if="property.required">*</span></label>
                    <component :is="getEditorType(property)" v-model="propertyValues[property.key]" :property="property" @blur.native="updateProperty(property.key, propertyValues[property.key])" @change="updateProperty(property.key, propertyValues[property.key])" />
                    <div v-if="property.description" class="property-desc">{{ property.description }}</div>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>
        
        <!-- 高级属性 -->
        <div class="property-section" v-if="advancedProperties.length > 0">
          <div class="section-header">
            <span class="section-title">高级属性</span>
            <el-switch
              v-model="showAdvanced"
              size="small"
              active-text="显示"
              inactive-text="隐藏"
            />
          </div>
          <div class="section-content" v-show="showAdvanced">
            <div class="form-row" v-for="property in advancedProperties" :key="property.key">
              <label class="form-label">{{ property.name }}</label>
              <el-input
                v-model="propertyValues[property.key]"
                :placeholder="property.placeholder"
                size="small"
                @blur="updateProperty(property.key, propertyValues[property.key])"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 表格编辑对话框 -->
    <el-dialog v-model="tableDialog.visible" :title="tableDialog.title" width="800px">
      <div class="table-editor-dialog">
        <div class="table-toolbar">
          <el-button size="small" type="primary" @click="addTableRow">
            <el-icon><Plus /></el-icon>
            添加行
          </el-button>
          <el-button size="small" @click="removeSelectedRows">
            <el-icon><Delete /></el-icon>
            删除选中
          </el-button>
          <el-button size="small" @click="clearTable">
            <el-icon><RefreshRight /></el-icon>
            清空
          </el-button>
        </div>
        
        <el-table
          :data="tableDialog.data"
          @selection-change="handleTableSelection"
          size="small"
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column
            v-for="column in tableDialog.columns"
            :key="column.prop"
            :prop="column.prop"
            :label="column.label"
          >
            <template #default="scope">
              <el-input
                v-model="scope.row[column.prop]"
                size="small"
                @blur="updateTableData"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <el-button @click="tableDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmTableEdit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 帮助对话框 -->
    <el-dialog v-model="helpDialog.visible" title="组件帮助" width="600px">
      <div class="help-content" v-html="helpDialog.content"></div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, watch, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshRight, QuestionFilled, Folder, Plus, Delete } from '@element-plus/icons-vue'

export default {
  name: 'PropertyPanel',
  components: {
    RefreshRight, QuestionFilled, Folder, Plus, Delete
  },
  props: {
    selectedComponent: {
      type: Object,
      default: null
    }
  },
  emits: ['update-property'],
  setup(props, { emit }) {
    const activeProperties = ref([])
    const httpActiveTab = ref('basic')
    const httpParams = ref([])
    const paramSelection = ref([])
    const httpFiles = ref([])
    const fileSelection = ref([])
    const showAdvanced = ref(false)
    const allExpanded = ref(false)
    const propertyValues = ref({})
    
    // 表格编辑对话框
    const tableDialog = reactive({
      visible: false,
      title: '',
      propertyKey: '',
      columns: [],
      data: [],
      selection: []
    })
    
    // 帮助对话框
    const helpDialog = reactive({
      visible: false,
      content: ''
    })
    
    // 组件属性配置
    const propertyConfigs = {
      HTTPSamplerProxy: [
        {
          name: 'basic',
          title: '基本配置',
          properties: [
            {
              key: 'HTTPSampler.protocol',
              name: '协议',
              type: 'select',
              options: [
                { label: 'HTTP', value: 'http' },
                { label: 'HTTPS', value: 'https' }
              ],
              default: 'https',
              required: true
            },
            {
              key: 'HTTPSampler.domain',
              name: '服务器名称或IP',
              type: 'string',
              placeholder: 'example.com',
              required: true
            },
            {
              key: 'HTTPSampler.port',
              name: '端口号',
              type: 'number',
              min: 1,
              max: 65535,
              default: 80
            },
            {
              key: 'HTTPSampler.method',
              name: 'HTTP请求方法',
              type: 'select',
              options: [
                { label: 'GET', value: 'GET' },
                { label: 'POST', value: 'POST' },
                { label: 'PUT', value: 'PUT' },
                { label: 'DELETE', value: 'DELETE' },
                { label: 'PATCH', value: 'PATCH' },
                { label: 'HEAD', value: 'HEAD' },
                { label: 'OPTIONS', value: 'OPTIONS' }
              ],
              default: 'GET',
              required: true
            },
            {
              key: 'HTTPSampler.path',
              name: '路径',
              type: 'string',
              placeholder: '/api/users',
              required: true
            }
          ]
        },
        {
          name: 'content',
          title: '内容配置',
          properties: [
            {
              key: 'HTTPSampler.postBodyRaw',
              name: '请求体',
              type: 'textarea',
              placeholder: 'JSON、XML或其他请求体内容'
            },
            {
              key: 'HTTPSampler.contentEncoding',
              name: '内容编码',
              type: 'select',
              options: [
                { label: 'UTF-8', value: 'UTF-8' },
                { label: 'GBK', value: 'GBK' },
                { label: 'ISO-8859-1', value: 'ISO-8859-1' }
              ],
              default: 'UTF-8'
            }
          ]
        },
        {
          name: 'advanced',
          title: '高级配置',
          properties: [
            {
              key: 'HTTPSampler.connect_timeout',
              name: '连接超时',
              type: 'number',
              min: 0,
              default: 5000,
              description: '连接超时时间(毫秒)'
            },
            {
              key: 'HTTPSampler.response_timeout',
              name: '响应超时',
              type: 'number',
              min: 0,
              default: 30000,
              description: '响应超时时间(毫秒)'
            },
            {
              key: 'HTTPSampler.follow_redirects',
              name: '跟随重定向',
              type: 'boolean',
              default: true
            }
          ]
        }
      ],
      ThreadGroup: [
        {
          name: 'thread',
          title: '线程属性',
          properties: [
            {
              key: 'ThreadGroup.num_threads',
              name: '线程数',
              type: 'number',
              min: 1,
              default: 1,
              required: true,
              description: '并发执行的线程数量'
            },
            {
              key: 'ThreadGroup.ramp_time',
              name: 'Ramp-Up时间',
              type: 'number',
              min: 0,
              default: 1,
              description: '启动所有线程的时间(秒)'
            },
            {
              key: 'ThreadGroup.loops',
              name: '循环次数',
              type: 'number',
              min: -1,
              default: 1,
              description: '-1表示无限循环'
            },
            {
              key: 'ThreadGroup.scheduler',
              name: '调度器',
              type: 'boolean',
              default: false,
              description: '启用调度器进行定时执行'
            }
          ]
        }
      ],
      HeaderManager: [
        {
          name: 'headers',
          title: 'HTTP头配置',
          properties: [
            {
              key: 'HeaderManager.headers',
              name: 'HTTP头',
              type: 'table',
              columns: [
                { prop: 'name', label: '名称' },
                { prop: 'value', label: '值' }
              ],
              description: '配置HTTP请求头'
            }
          ]
        }
      ],
      CSVDataSet: [
        {
          name: 'csv',
          title: 'CSV配置',
          properties: [
            {
              key: 'CSV.filename',
              name: '文件名',
              type: 'file',
              required: true,
              description: '选择CSV数据文件'
            },
            {
              key: 'CSV.variableNames',
              name: '变量名称',
              type: 'string',
              placeholder: 'username,password',
              required: true,
              description: '用逗号分隔的变量名'
            },
            {
              key: 'CSV.delimiter',
              name: '分隔符',
              type: 'string',
              default: ',',
              description: '字段分隔符'
            },
            {
              key: 'CSV.recycle',
              name: '遇到文件结束符再次循环',
              type: 'boolean',
              default: true
            },
            {
              key: 'CSV.stopThread',
              name: '遇到文件结束符停止线程',
              type: 'boolean',
              default: false
            }
          ]
        }
      ],
      ResponseAssertion: [
        {
          name: 'assertion',
          title: '断言配置',
          properties: [
            {
              key: 'Assertion.test_field',
              name: '测试字段',
              type: 'select',
              options: [
                { label: '响应文本', value: 'response_text' },
                { label: '响应代码', value: 'response_code' },
                { label: '响应消息', value: 'response_message' },
                { label: '响应头', value: 'response_headers' }
              ],
              default: 'response_text',
              required: true
            },
            {
              key: 'Assertion.test_type',
              name: '匹配规则',
              type: 'select',
              options: [
                { label: '包含', value: 'contains' },
                { label: '匹配', value: 'matches' },
                { label: '等于', value: 'equals' },
                { label: '子字符串', value: 'substring' }
              ],
              default: 'contains',
              required: true
            },
            {
              key: 'Assertion.test_strings',
              name: '测试模式',
              type: 'textarea',
              placeholder: '输入要断言的内容',
              required: true,
              description: '要匹配的文本或正则表达式'
            },
            {
              key: 'Assertion.assume_success',
              name: '忽略状态',
              type: 'boolean',
              default: false,
              description: '忽略HTTP状态码，只检查内容'
            }
          ]
        }
      ]
    }
    
    // 计算组件属性
    const componentProperties = computed(() => {
      if (!props.selectedComponent || !props.selectedComponent.componentType) {
        return []
      }
      
      const config = propertyConfigs[props.selectedComponent.componentType]
      return config || []
    })
    
    // 高级属性
    const advancedProperties = computed(() => {
      return [
        {
          key: 'custom_property_1',
          name: '自定义属性1',
          placeholder: '输入自定义属性值'
        },
        {
          key: 'custom_property_2',
          name: '自定义属性2',
          placeholder: '输入自定义属性值'
        }
      ]
    })
    
    // 初始化属性值
    const initPropertyValues = () => {
      if (!props.selectedComponent) return
      
      propertyValues.value = { ...props.selectedComponent.properties }
      
      // 设置默认值
      componentProperties.value.forEach(group => {
        group.properties.forEach(property => {
          if (property.default !== undefined && propertyValues.value[property.key] === undefined) {
            propertyValues.value[property.key] = property.default
          }
        })
      })

      // 初始化HTTP表格数据
      if (props.selectedComponent.componentType === 'HTTPSamplerProxy') {
        httpParams.value = Array.isArray(propertyValues.value['HTTPSampler.arguments']) ? [...propertyValues.value['HTTPSampler.arguments']] : []
        httpFiles.value = Array.isArray(propertyValues.value['HTTPSampler.files']) ? [...propertyValues.value['HTTPSampler.files']] : []
      }
    }
    
    // 更新属性
    const updateProperty = (key, value) => {
      if (!props.selectedComponent) return
      
      if (!props.selectedComponent.properties) {
        props.selectedComponent.properties = {}
      }
      
      props.selectedComponent.properties[key] = value
      propertyValues.value[key] = value
      emit('update-property', key, value)
    }
    
    // 重置属性
    const resetProperties = () => {
      if (!props.selectedComponent) return
      
      props.selectedComponent.properties = {}
      initPropertyValues()
      ElMessage.success('属性已重置')
    }
    
    // 展开/收起全部
    const expandAll = () => {
      if (allExpanded.value) {
        activeProperties.value = []
      } else {
        activeProperties.value = componentProperties.value.map(group => group.name)
      }
      allExpanded.value = !allExpanded.value
    }
    
    // 选择文件
    const selectFile = (propertyKey) => {
      // 这里可以集成文件选择器
      ElMessage.info('文件选择功能待实现')
    }
    
    // 编辑表格
    const editTable = (propertyKey) => {
      const property = findProperty(propertyKey)
      if (!property) return
      
      tableDialog.visible = true
      tableDialog.title = `编辑${property.name}`
      tableDialog.propertyKey = propertyKey
      tableDialog.columns = property.columns || []
      
      // 初始化表格数据
      const currentValue = propertyValues.value[propertyKey]
      if (Array.isArray(currentValue)) {
        tableDialog.data = [...currentValue]
      } else {
        tableDialog.data = []
      }
      
      // 如果没有数据，添加一行默认数据
      if (tableDialog.data.length === 0) {
        addTableRow()
      }
    }
    
    // 查找属性定义
    const findProperty = (propertyKey) => {
      for (const group of componentProperties.value) {
        const property = group.properties.find(p => p.key === propertyKey)
        if (property) return property
      }
      return null
    }
    
    // 获取表格信息
    const getTableInfo = (propertyKey) => {
      const data = propertyValues.value[propertyKey]
      if (Array.isArray(data)) {
        return `${data.length} 行数据`
      }
      return '点击编辑'
    }
    
    // 添加表格行
    const addTableRow = () => {
      const newRow = {}
      tableDialog.columns.forEach(column => {
        newRow[column.prop] = ''
      })
      tableDialog.data.push(newRow)
    }
    
    // 表格选择变化
    const handleTableSelection = (selection) => {
      tableDialog.selection = selection
    }
    
    // 删除选中行
    const removeSelectedRows = () => {
      if (tableDialog.selection.length === 0) {
        ElMessage.warning('请选择要删除的行')
        return
      }
      
      tableDialog.selection.forEach(row => {
        const index = tableDialog.data.indexOf(row)
        if (index > -1) {
          tableDialog.data.splice(index, 1)
        }
      })
      
      updateTableData()
    }
    
    // 清空表格
    const clearTable = () => {
      tableDialog.data = []
      updateTableData()
      addTableRow()
    }
    
    // 更新表格数据
    const updateTableData = () => {
      // 实时更新不做处理，等确认时统一更新
    }
    
    // 确认表格编辑
    const confirmTableEdit = () => {
      updateProperty(tableDialog.propertyKey, [...tableDialog.data])
      tableDialog.visible = false
      ElMessage.success('表格更新成功')
    }

    // HTTP 参数表
    const addParamRow = () => {
      httpParams.value.push({ name: '', value: '', encode: true })
      updateProperty('HTTPSampler.arguments', [...httpParams.value])
    }
    const removeSelectedParams = () => {
      httpParams.value = httpParams.value.filter(r => !paramSelection.value.includes(r))
      updateProperty('HTTPSampler.arguments', [...httpParams.value])
    }
    // 文件表
    const addFileRow = () => {
      httpFiles.value.push({ param: '', path: '', mime: '' })
      updateProperty('HTTPSampler.files', [...httpFiles.value])
    }
    const removeSelectedFiles = () => {
      httpFiles.value = httpFiles.value.filter(r => !fileSelection.value.includes(r))
      updateProperty('HTTPSampler.files', [...httpFiles.value])
    }
    
    // 显示帮助
    const showHelp = () => {
      if (!props.selectedComponent) return
      
      const componentType = props.selectedComponent.componentType
      helpDialog.content = getHelpContent(componentType)
      helpDialog.visible = true
    }
    
    // 获取帮助内容
    const getHelpContent = (componentType) => {
      const helpContents = {
        HTTPSamplerProxy: `
          <h4>HTTP请求采样器</h4>
          <p>用于发送HTTP/HTTPS请求。支持多种HTTP方法和请求配置。</p>
          <h5>主要属性：</h5>
          <ul>
            <li><strong>协议：</strong>HTTP或HTTPS</li>
            <li><strong>服务器：</strong>目标服务器域名或IP</li>
            <li><strong>端口：</strong>目标端口，HTTP默认80，HTTPS默认443</li>
            <li><strong>路径：</strong>请求的URL路径</li>
            <li><strong>方法：</strong>HTTP请求方法</li>
          </ul>
        `,
        ThreadGroup: `
          <h4>线程组</h4>
          <p>定义并发用户数和测试执行策略。</p>
          <h5>主要属性：</h5>
          <ul>
            <li><strong>线程数：</strong>并发用户数量</li>
            <li><strong>Ramp-Up时间：</strong>启动所有线程的时间</li>
            <li><strong>循环次数：</strong>每个线程执行的次数</li>
            <li><strong>调度器：</strong>定时执行控制</li>
          </ul>
        `
      }
      
      return helpContents[componentType] || `<p>暂无${componentType}的帮助信息</p>`
    }
    
    // 监听选中组件变化
    watch(() => props.selectedComponent, (newComponent) => {
      if (newComponent) {
        initPropertyValues()
        activeProperties.value = componentProperties.value.length > 0 ? [componentProperties.value[0].name] : []
      }
    }, { immediate: true })
    
    return {
      activeProperties,
      showAdvanced,
      allExpanded,
      propertyValues,
      componentProperties,
      advancedProperties,
      tableDialog,
      helpDialog,
      updateProperty,
      resetProperties,
      expandAll,
      selectFile,
      editTable,
      getTableInfo,
      addTableRow,
      handleTableSelection,
      removeSelectedRows,
      clearTable,
      updateTableData,
      confirmTableEdit,
      showHelp
    }
  }
}
</script>

<style lang="scss" scoped>
.property-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e6e6e6;
  background: #fafafa;
  
  h3 {
    margin: 0;
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }
  
  .header-actions {
    display: flex;
    gap: 5px;
  }
}

.panel-content {
  flex: 1;
  overflow: auto;
  padding: 0;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.property-form {
  padding: 0;
}

.two-cols {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.request-table {
  .table-toolbar {
    margin-bottom: 8px;
    display: flex;
    gap: 8px;
  }
}

.property-section {
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: #fafafa;
    border-bottom: 1px solid #e6e6e6;
    
    .section-title {
      font-size: 13px;
      font-weight: 500;
      color: #333;
    }
  }
  
  .section-content {
    padding: 15px 20px;
  }
}

.property-group {
  .form-row {
    margin-bottom: 16px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
}

.form-row {
  margin-bottom: 16px;
  
  .form-label {
    display: block;
    margin-bottom: 6px;
    font-size: 12px;
    font-weight: 500;
    color: #666;
    line-height: 1.4;
    
    .required {
      color: #f56c6c;
      margin-left: 2px;
    }
  }
  
  .property-desc {
    margin-top: 4px;
    font-size: 11px;
    color: #999;
    line-height: 1.3;
  }
}

.file-input {
  .el-input-group__append {
    padding: 0 8px;
  }
}

.table-editor {
  display: flex;
  align-items: center;
  gap: 10px;
  
  .table-info {
    font-size: 12px;
    color: #666;
  }
}

// 表格编辑对话框
.table-editor-dialog {
  .table-toolbar {
    margin-bottom: 16px;
    display: flex;
    gap: 10px;
  }
}

.help-content {
  line-height: 1.6;
  color: #666;
  
  h4, h5 {
    color: #333;
    margin-top: 16px;
    margin-bottom: 8px;
  }
  
  h4 {
    font-size: 16px;
  }
  
  h5 {
    font-size: 14px;
  }
  
  p {
    margin-bottom: 12px;
  }
  
  ul {
    margin: 8px 0;
    padding-left: 20px;
    
    li {
      margin-bottom: 4px;
      
      strong {
        color: #333;
      }
    }
  }
}

:deep(.el-collapse) {
  border: none;
  
  .el-collapse-item {
    .el-collapse-item__header {
      height: 36px;
      line-height: 36px;
      background: #f9f9f9;
      border: none;
      border-bottom: 1px solid #e6e6e6;
      font-size: 12px;
      font-weight: 500;
      padding-left: 15px;
    }
    
    .el-collapse-item__content {
      padding: 0;
      border: none;
    }
    
    .el-collapse-item__wrap {
      border: none;
    }
  }
}

:deep(.el-input--small) {
  .el-input__inner {
    height: 28px;
    line-height: 28px;
    font-size: 12px;
  }
}

:deep(.el-input-number--small) {
  .el-input__inner {
    height: 28px;
    line-height: 28px;
    font-size: 12px;
  }
}

:deep(.el-select--small) {
  .el-input__inner {
    height: 28px;
    line-height: 28px;
    font-size: 12px;
  }
}

:deep(.el-textarea--small) {
  .el-textarea__inner {
    font-size: 12px;
  }
}
</style>
