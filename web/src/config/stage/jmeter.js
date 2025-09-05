const jmeterRouter = {
    route: null,
    name: null,
    title: 'JMeter工具',
    type: 'tab', // 类型: folder, tab, view
    icon: 'iconfont icon-jmeter',
    filePath: 'view/jmeter/', // 文件路径
    order: null,
    inNav: true,
    permission: ['JMeter工具'],
    children: [
      {
        title: '脚本编辑器',
        type: 'view',
        name: 'jmeterScriptEditor',
        route: '/jmeter/script-editor',
        filePath: 'view/jmeter/script-editor.vue',
        inNav: true,
        icon: 'iconfont icon-script',
        keepAlive: true,
        permission: ['脚本编辑器'],
      },
      {
        title: '聚合报告查询',
        type: 'view',
        name: 'aggregateReportSearch',
        route: '/jmeter/aggregateReport/search',
        filePath: 'view/jmeter/aggregate-report-search.vue',
        inNav: true,
        icon: 'iconfont icon-search',
        keepAlive: true,
        permission: ['聚合报告记录'],
      },
      {
        title: '聚合报告记录',
        type: 'view',
        name: 'aggregateReportRecord',
        route: '/jmeter/aggregateReport/record',
        filePath: 'view/jmeter/aggregate-report-record.vue',
        inNav: true,
        icon: 'iconfont icon-record',
        keepAlive: true,
        permission: ['聚合报告记录'],
      },
    ],
  }
  
  export default jmeterRouter
  