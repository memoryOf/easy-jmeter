<template>
  <div class="component-library">
    <div class="library-header">
      <h3>组件库</h3>
      <el-input
        v-model="searchText"
        placeholder="搜索组件"
        size="small"
        clearable
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <div class="library-content">
      <el-collapse v-model="activeCategories" accordion>
        <el-collapse-item 
          v-for="category in filteredCategories" 
          :key="category.name"
          :title="category.title"
          :name="category.name"
        >
          <div class="component-list">
            <div 
              v-for="component in category.components"
              :key="component.type"
              class="component-item"
              :title="component.description"
              @click="addComponent(component.type)"
              draggable
              @dragstart="startDrag($event, component.type)"
            >
              <div class="component-icon">
                <i :class="component.icon"></i>
              </div>
              <div class="component-info">
                <div class="component-name">{{ component.name }}</div>
                <div class="component-desc">{{ component.description }}</div>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { get } from '@/lin/plugin/axios'

export default {
  name: 'ComponentLibrary',
  components: {
    Search
  },
  emits: ['add-component'],
  setup(props, { emit }) {
    const searchText = ref('')
    const activeCategories = ref(['samplers'])
    const componentCategories = ref([])
    
    // 组件分类数据
    const defaultCategories = [
      {
        name: 'samplers',
        title: '取样器',
        icon: 'el-icon-cpu',
        components: [
          {
            type: 'HTTPSamplerProxy',
            name: 'HTTP请求',
            description: 'HTTP/HTTPS请求采样器',
            icon: 'iconfont icon-http'
          },
          {
            type: 'FTPSampler',
            name: 'FTP请求',
            description: 'FTP请求采样器',
            icon: 'iconfont icon-ftp'
          },
          {
            type: 'JDBCSampler',
            name: 'JDBC请求',
            description: '数据库JDBC请求',
            icon: 'iconfont icon-database'
          },
          {
            type: 'TCPSampler',
            name: 'TCP请求',
            description: 'TCP Socket请求',
            icon: 'iconfont icon-tcp'
          },
          {
            type: 'SMTPSampler',
            name: 'SMTP请求',
            description: '邮件发送请求',
            icon: 'iconfont icon-email'
          }
        ]
      },
      {
        name: 'controllers',
        title: '逻辑控制器',
        icon: 'el-icon-s-operation',
        components: [
          {
            type: 'IfController',
            name: '如果控制器',
            description: '条件执行控制器',
            icon: 'iconfont icon-if'
          },
          {
            type: 'LoopController',
            name: '循环控制器',
            description: '循环执行控制器',
            icon: 'iconfont icon-loop'
          },
          {
            type: 'WhileController',
            name: 'While控制器',
            description: 'While循环控制器',
            icon: 'iconfont icon-while'
          },
          {
            type: 'ForeachController',
            name: 'ForEach控制器',
            description: '遍历控制器',
            icon: 'iconfont icon-foreach'
          },
          {
            type: 'OnceOnlyController',
            name: '仅一次控制器',
            description: '只执行一次的控制器',
            icon: 'iconfont icon-once'
          }
        ]
      },
      {
        name: 'threadgroups',
        title: '线程组',
        icon: 'el-icon-user',
        components: [
          {
            type: 'ThreadGroup',
            name: '线程组',
            description: '标准线程组',
            icon: 'iconfont icon-threadgroup'
          },
          {
            type: 'SetupThreadGroup',
            name: 'setUp线程组',
            description: '前置线程组',
            icon: 'iconfont icon-setup'
          },
          {
            type: 'PostThreadGroup',
            name: 'tearDown线程组',
            description: '后置线程组',
            icon: 'iconfont icon-teardown'
          }
        ]
      },
      {
        name: 'preprocessors',
        title: '前置处理器',
        icon: 'el-icon-s-tools',
        components: [
          {
            type: 'UserParameters',
            name: '用户参数',
            description: '用户参数前置处理器',
            icon: 'iconfont icon-param'
          },
          {
            type: 'JSR223PreProcessor',
            name: 'JSR223预处理器',
            description: '脚本预处理器',
            icon: 'iconfont icon-script'
          },
          {
            type: 'BeanShellPreProcessor',
            name: 'BeanShell预处理器',
            description: 'BeanShell脚本预处理器',
            icon: 'iconfont icon-beanshell'
          },
          {
            type: 'HTMLLinkParser',
            name: 'HTML链接解析器',
            description: 'HTML链接解析器',
            icon: 'iconfont icon-html'
          }
        ]
      },
      {
        name: 'postprocessors',
        title: '后置处理器',
        icon: 'el-icon-s-cooperation',
        components: [
          {
            type: 'RegexExtractor',
            name: '正则表达式提取器',
            description: '正则表达式提取器',
            icon: 'iconfont icon-regex'
          },
          {
            type: 'JSONExtractor',
            name: 'JSON提取器',
            description: 'JSON路径提取器',
            icon: 'iconfont icon-json'
          },
          {
            type: 'XPathExtractor',
            name: 'XPath提取器',
            description: 'XPath表达式提取器',
            icon: 'iconfont icon-xpath'
          },
          {
            type: 'CSSJQueryExtractor',
            name: 'CSS选择器提取器',
            description: 'CSS/jQuery选择器提取器',
            icon: 'iconfont icon-css'
          },
          {
            type: 'JSR223PostProcessor',
            name: 'JSR223后置处理器',
            description: '脚本后置处理器',
            icon: 'iconfont icon-script'
          }
        ]
      },
      {
        name: 'assertions',
        title: '断言',
        icon: 'el-icon-success',
        components: [
          {
            type: 'ResponseAssertion',
            name: '响应断言',
            description: '响应内容断言',
            icon: 'iconfont icon-assertion'
          },
          {
            type: 'DurationAssertion',
            name: '持续时间断言',
            description: '响应时间断言',
            icon: 'iconfont icon-duration'
          },
          {
            type: 'SizeAssertion',
            name: '大小断言',
            description: '响应大小断言',
            icon: 'iconfont icon-size'
          },
          {
            type: 'JSONAssertion',
            name: 'JSON断言',
            description: 'JSON路径断言',
            icon: 'iconfont icon-json'
          },
          {
            type: 'XPathAssertion',
            name: 'XPath断言',
            description: 'XPath表达式断言',
            icon: 'iconfont icon-xpath'
          }
        ]
      },
      {
        name: 'timers',
        title: '定时器',
        icon: 'el-icon-timer',
        components: [
          {
            type: 'ConstantTimer',
            name: '固定定时器',
            description: '固定延迟定时器',
            icon: 'iconfont icon-timer'
          },
          {
            type: 'UniformRandomTimer',
            name: '统一随机定时器',
            description: '随机延迟定时器',
            icon: 'iconfont icon-random'
          },
          {
            type: 'GaussianRandomTimer',
            name: '高斯随机定时器',
            description: '高斯分布随机延迟',
            icon: 'iconfont icon-gaussian'
          },
          {
            type: 'ThroughputShapingTimer',
            name: '吞吐量整形定时器',
            description: '吞吐量控制定时器',
            icon: 'iconfont icon-throughput'
          }
        ]
      },
      {
        name: 'config',
        title: '配置元件',
        icon: 'el-icon-setting',
        components: [
          {
            type: 'CSVDataSet',
            name: 'CSV数据文件设置',
            description: 'CSV参数化配置',
            icon: 'iconfont icon-csv'
          },
          {
            type: 'HeaderManager',
            name: 'HTTP信息头管理器',
            description: 'HTTP请求头配置',
            icon: 'iconfont icon-header'
          },
          {
            type: 'CookieManager',
            name: 'HTTP Cookie管理器',
            description: 'Cookie管理器',
            icon: 'iconfont icon-cookie'
          },
          {
            type: 'CacheManager',
            name: 'HTTP缓存管理器',
            description: '缓存管理器',
            icon: 'iconfont icon-cache'
          },
          {
            type: 'AuthManager',
            name: 'HTTP授权管理器',
            description: '认证管理器',
            icon: 'iconfont icon-auth'
          },
          {
            type: 'DNSCacheManager',
            name: 'DNS缓存管理器',
            description: 'DNS解析缓存',
            icon: 'iconfont icon-dns'
          }
        ]
      },
      {
        name: 'listeners',
        title: '监听器',
        icon: 'el-icon-view',
        components: [
          {
            type: 'ResultCollector',
            name: '察看结果树',
            description: '查看请求响应详情',
            icon: 'iconfont icon-tree'
          },
          {
            type: 'SummaryReport',
            name: '聚合报告',
            description: '汇总测试结果',
            icon: 'iconfont icon-summary'
          },
          {
            type: 'TableVisualizer',
            name: '用表格查看结果',
            description: '表格形式查看结果',
            icon: 'iconfont icon-table'
          },
          {
            type: 'GraphVisualizer',
            name: '图形结果',
            description: '图形化查看结果',
            icon: 'iconfont icon-chart'
          },
          {
            type: 'SimpleDataWriter',
            name: '简单数据写入器',
            description: '将结果写入文件',
            icon: 'iconfont icon-write'
          }
        ]
      }
    ]
    
    // 过滤后的分类
    const filteredCategories = computed(() => {
      if (!searchText.value) {
        return componentCategories.value.length > 0 ? componentCategories.value : defaultCategories
      }
      
      const categories = componentCategories.value.length > 0 ? componentCategories.value : defaultCategories
      return categories.map(category => ({
        ...category,
        components: category.components.filter(component =>
          component.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
          component.description.toLowerCase().includes(searchText.value.toLowerCase())
        )
      })).filter(category => category.components.length > 0)
    })
    
    // 获取组件元数据
    const loadComponentMetadata = async () => {
      try {
        // 这里可以从后端获取组件元数据
        // const res = await get('/v1/jmeter/component/metadata/all')
        // componentCategories.value = res
      } catch (error) {
        console.warn('Failed to load component metadata:', error)
        // 使用默认数据
      }
    }
    
    // 添加组件
    const addComponent = (componentType) => {
      emit('add-component', componentType)
    }
    
    // 拖拽开始
    const startDrag = (event, componentType) => {
      event.dataTransfer.setData('component-type', componentType)
      event.dataTransfer.effectAllowed = 'copy'
    }
    
    // 生命周期
    onMounted(() => {
      loadComponentMetadata()
    })
    
    return {
      searchText,
      activeCategories,
      componentCategories,
      filteredCategories,
      addComponent,
      startDrag
    }
  }
}
</script>

<style lang="scss" scoped>
.component-library {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.library-header {
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
  
  h3 {
    margin: 0 0 10px 0;
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }
  
  .search-input {
    width: 100%;
  }
}

.library-content {
  flex: 1;
  overflow: auto;
}

.component-list {
  padding: 5px;
}

.component-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  margin-bottom: 5px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  
  &:hover {
    background-color: #f0f0f0;
    border-color: #409eff;
  }
  
  &:active {
    background-color: #e6f7ff;
  }
  
  .component-icon {
    width: 24px;
    height: 24px;
    margin-right: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #409eff;
    
    i {
      font-size: 16px;
    }
  }
  
  .component-info {
    flex: 1;
    min-width: 0;
    
    .component-name {
      font-size: 12px;
      font-weight: 500;
      color: #333;
      line-height: 1.4;
      margin-bottom: 2px;
    }
    
    .component-desc {
      font-size: 10px;
      color: #666;
      line-height: 1.3;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

::v-deep .el-collapse {
  border: none;
  
  .el-collapse-item {
    border: none;
    
    .el-collapse-item__header {
      height: 40px;
      line-height: 40px;
      background-color: #fafafa;
      border-bottom: 1px solid #e6e6e6;
      font-size: 13px;
      font-weight: 500;
      color: #333;
      padding-left: 15px;
      
      &:hover {
        background-color: #f0f0f0;
      }
    }
    
    .el-collapse-item__content {
      padding: 5px 0;
    }
    
    .el-collapse-item__wrap {
      border: none;
    }
  }
}

::v-deep .el-input--small {
  .el-input__inner {
    height: 28px;
    line-height: 28px;
    font-size: 12px;
  }
}
</style>
