const jmeterRouter = {
    route: null,
    name: null,
    title: '场景管理',
    type: 'tab', // 类型: folder, tab, view
    icon: 'iconfont icon-scene-management',
    filePath: 'view/jmeter/', // 文件路径
    order: null,
    inNav: true,
    // permission: ['场景管理'], // 暂时注释掉权限限制
    children: [
      {
        title: '场景管理',
        type: 'view',
        name: 'sceneManagement',
        route: '/jmeter/scene-management',
        filePath: 'view/jmeter/scene-management.vue',
        inNav: true,
        icon: 'iconfont icon-scene',
        keepAlive: true,
        // permission: ['场景管理'], // 暂时注释掉权限限制
      },
    ],
  }
  
  export default jmeterRouter
  