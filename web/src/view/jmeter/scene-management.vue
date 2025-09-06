<template>
  <div class="container">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="module-tree">
          <div class="tree-header">
            <h4>模块管理</h4>
            <el-button type="primary" size="small" @click="refreshModules">
              <i class="el-icon-refresh"></i>
              刷新
            </el-button>
          </div>
          <div class="tree-content">
            <el-tree
              :data="modules"
              node-key="id"
              draggable
              :allow-drop="allowDrop"
              @node-drop="handleNodeDrop"
              @node-click="handleNodeClick"
              :expand-on-click-node="false"
              :default-expand-all="true"
            >
            <template #default="{ node, data }">
              <div class="tree-node-wrapper" @mouseenter="showActions(data)" @mouseleave="hideActions(data)">
                <span class="node-label">{{ node.label }}</span>
                <span class="node-count" v-if="data.childCount > 0">({{ data.childCount }})</span>
                
                <!-- 操作按钮区域 -->
                <div class="node-actions" v-show="data.showActions">
                  <!-- Root节点：只显示+按钮 -->
                  <template v-if="data.name === 'root'">
                    <el-button
                      type="text"
                      size="small"
                      class="action-btn add-btn"
                      @click.stop="createChildNode(data)"
                      title="创建一级节点"
                    >
                      <i class="el-icon-plus"></i>
                    </el-button>
                  </template>
                  
                  <!-- 一级节点：显示重命名、+、删除按钮 -->
                  <template v-else>
                    <el-button
                      type="text"
                      size="small"
                      class="action-btn edit-btn"
                      @click.stop="editNode(data)"
                      title="重命名"
                    >
                      <i class="el-icon-edit"></i>
                    </el-button>
                    <el-button
                      type="text"
                      size="small"
                      class="action-btn add-btn"
                      @click.stop="createChildNode(data)"
                      title="创建子节点"
                    >
                      <i class="el-icon-plus"></i>
                    </el-button>
                    <el-button
                      type="text"
                      size="small"
                      class="action-btn delete-btn"
                      @click.stop="deleteNode(node, data)"
                      title="删除"
                    >
                      <i class="el-icon-delete"></i>
                    </el-button>
                  </template>
                </div>
              </div>
            </template>
            </el-tree>
          </div>
        </div>
      </el-col>
      <el-col :span="18">
        <div class="scene-table-container">
          <div class="table-header">
            <h3>场景列表</h3>
            <div class="table-actions">
              <el-button type="primary" size="small" @click="createScene">
                <i class="el-icon-plus"></i>
                新建场景
              </el-button>
              <el-button type="default" size="small" @click="refreshScenes">
                <i class="el-icon-refresh"></i>
                刷新
              </el-button>
            </div>
          </div>
          <el-table :data="scenes" style="width: 100%" empty-text="暂无数据" v-loading="scenesLoading">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="name" label="场景名称" min-width="150"></el-table-column>
            <el-table-column prop="creator" label="创建人" width="120"></el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
            <el-table-column prop="projectName" label="所属项目" min-width="150"></el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="editScene(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteScene(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from '@/lin/plugin/axios';

export default {
  setup() {
    const route = useRoute();
    const modules = ref([]);
    const scenes = ref([]);
    const scenesLoading = ref(false);

    // 获取模块数据
    const fetchModules = async () => {
      try {
        const response = await axios.get('/api/modules');
        console.log('获取模块数据响应:', response.data); // 添加日志
        if (response.data.code === 0) {
          // 处理后端返回的数据，转换为树形结构
          modules.value = convertToTree(response.data.data);
        }
      } catch (error) {
        console.error('获取模块数据失败:', error);
      }
    };

    // 获取场景数据
    const fetchScenes = async () => {
      try {
        scenesLoading.value = true;
        const response = await axios.get('/api/scenes');
        if (response.data.code === 0) {
          scenes.value = response.data.data;
        }
      } catch (error) {
        console.error('获取场景数据失败:', error);
      } finally {
        scenesLoading.value = false;
      }
    };

    // 将扁平数据转换为树形结构
    const convertToTree = (data) => {
      const map = {};
      const result = [];
      
      // 创建id到节点的映射，并初始化children数组
      data.forEach(item => {
        map[item.id] = { 
          ...item, 
          children: [], // 确保每个节点都有children数组
          label: item.name, // 使用接口返回的name字段
          showActions: false,
          childCount: 0 // 子模块数量，初始为0
        };
      });
      
      // 构建树形结构
      data.forEach(item => {
        if (item.parent_id === null) {
          // 根节点
          result.push(map[item.id]);
        } else {
          // 子节点
          if (map[item.parent_id]) {
            map[item.parent_id].children.push(map[item.id]);
            // 更新父节点的子模块数量
            map[item.parent_id].childCount = map[item.parent_id].children.length;
          } else {
            console.warn(`父节点 ${item.parent_id} 不存在，无法添加子节点 ${item.id}`);
          }
        }
      });
      
      return result;
    };

    // 允许拖拽
    const allowDrop = (draggingNode, dropNode, type) => {
      return type !== 'inner';
    };

    // 处理节点拖拽
    const handleNodeDrop = (draggingNode, dropNode, type, ev) => {
      console.log('tree node dropped');
    };

    // 处理节点点击
    const handleNodeClick = (data) => {
      console.log('node clicked:', data);
    };

    // 显示操作按钮
    const showActions = (data) => {
      data.showActions = true;
    };

    // 隐藏操作按钮
    const hideActions = (data) => {
      data.showActions = false;
    };

    // 创建子节点
    const createChildNode = async (parentData) => {
      try {
        const newNode = {
          name: '新模块',
          parent_id: parentData.id,
          level: parentData.level + 1,
          status: 1
        };

        // 调用API创建节点
        const response = await axios.post('/api/modules', newNode);
        if (response.data.code === 0) {
          const createdNode = {
            ...response.data.data,
            children: [],
            label: response.data.data.name,
            showActions: false,
            childCount: 0
          };

          // 添加到父节点的children中
          if (!parentData.children) {
            parentData.children = [];
          }
          parentData.children.push(createdNode);
          
          // 更新父节点的子模块数量
          parentData.childCount = parentData.children.length;
          
          console.log('创建节点成功:', createdNode);
        }
      } catch (error) {
        console.error('创建节点失败:', error);
        // 如果API调用失败，先添加本地数据
        const tempNode = {
          id: Date.now(),
          name: '新模块',
          parent_id: parentData.id,
          level: parentData.level + 1,
          status: 1,
          children: [],
          label: '新模块',
          showActions: false,
          childCount: 0
        };
        
        if (!parentData.children) {
          parentData.children = [];
        }
        parentData.children.push(tempNode);
        
        // 更新父节点的子模块数量
        parentData.childCount = parentData.children.length;
      }
    };

    // 编辑节点（重命名）
    const editNode = (data) => {
      const newName = prompt('请输入新的模块名称:', data.name);
      if (newName && newName.trim() && newName !== data.name) {
        data.name = newName.trim();
        data.label = newName.trim();
        console.log('重命名节点:', data);
        // TODO: 调用API更新节点名称
      }
    };

    // 删除节点
    const deleteNode = (node, data) => {
      if (confirm(`确定要删除模块 "${data.name}" 吗？`)) {
        const parent = node.parent;
        const children = parent.data.children || parent.data;
        const index = children.findIndex(d => d.id === data.id);
        if (index > -1) {
          children.splice(index, 1);
          
          // 更新父节点的子模块数量
          if (parent.data) {
            parent.data.childCount = parent.data.children ? parent.data.children.length : 0;
          }
          
          console.log('删除节点:', data);
          // TODO: 调用API删除节点
        }
      }
    };

    // 刷新模块数据
    const refreshModules = () => {
      fetchModules();
    };

    // 刷新场景数据
    const refreshScenes = () => {
      fetchScenes();
    };

    // 创建场景
    const createScene = () => {
      console.log('创建场景');
      // TODO: 实现创建场景功能
    };

    // 编辑场景
    const editScene = (scene) => {
      console.log('编辑场景:', scene);
      // TODO: 实现编辑场景功能
    };

    // 删除场景
    const deleteScene = (scene) => {
      if (confirm(`确定要删除场景 "${scene.name}" 吗？`)) {
        console.log('删除场景:', scene);
        // TODO: 实现删除场景功能
      }
    };

    // 页面加载时获取数据
    onMounted(() => {
      fetchModules();
      fetchScenes();
    });

    // 监听路由变化，重新加载数据
    watch(
      () => route.path,
      () => {
        fetchModules();
        fetchScenes();
      },
      { immediate: true }
    );

    return {
      modules,
      scenes,
      scenesLoading,
      allowDrop,
      handleNodeDrop,
      handleNodeClick,
      showActions,
      hideActions,
      createChildNode,
      editNode,
      deleteNode,
      refreshModules,
      refreshScenes,
      createScene,
      editScene,
      deleteScene
    };
  }
};
</script>

<style scoped lang="scss">
.container {
  padding: 20px;
  height: 100%;
}

.module-tree {
  height: 500px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #dcdfe6;
  background: #f8f9fa;
  
  h4 {
    margin: 0;
    color: #303133;
    font-size: 14px;
    font-weight: 500;
  }
}

.tree-content {
  height: calc(100% - 50px);
  overflow-y: auto;
  padding: 8px;
}

.tree-node-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 2px 0;
  position: relative;
  
  &:hover {
    background-color: #f5f7fa;
  }
}

.node-label {
  font-size: 14px;
  color: #606266;
  flex: 1;
}

.node-count {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.node-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-left: 8px;
}

.action-btn {
  width: 20px;
  height: 20px;
  padding: 0;
  border: none;
  background: transparent;
  color: #409eff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px;
  transition: all 0.2s;
  
  &:hover {
    background-color: #ecf5ff;
    color: #409eff;
  }
  
  &.edit-btn {
    color: #67c23a;
    &:hover {
      background-color: #f0f9ff;
      color: #67c23a;
    }
  }
  
  &.add-btn {
    color: #409eff;
    &:hover {
      background-color: #ecf5ff;
      color: #409eff;
    }
  }
  
  &.delete-btn {
    color: #f56c6c;
    &:hover {
      background-color: #fef0f0;
      color: #f56c6c;
    }
  }
}

.scene-table-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #dcdfe6;
  background: #f8f9fa;
  
  h3 {
    margin: 0;
    color: #303133;
    font-size: 14px;
    font-weight: 500;
  }
}

.table-actions {
  display: flex;
  gap: 8px;
}

.el-table {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>