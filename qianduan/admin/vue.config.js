module.exports = {
  lintOnSave: false, // 关闭 ESLint 检查
  devServer: {
    port: 8081, // 管理端使用 8081 端口
    open: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端地址
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  }
}

// 避免 CopyWebpackPlugin 将 public/index.html 也复制为资源，
// 造成与 HtmlWebpackPlugin 输出同名文件冲突。
module.exports.chainWebpack = module.exports.chainWebpack || function (config) {
  const copyPlugin = config.plugin('copy');
  if (copyPlugin) {
    copyPlugin.tap(options => {
      try {
        const patterns = options[0].patterns || [];
        options[0].patterns = patterns.map(p => {
          p.globOptions = p.globOptions || {};
          p.globOptions.ignore = p.globOptions.ignore || [];
          if (!p.globOptions.ignore.includes('**/index.html')) {
            p.globOptions.ignore.push('**/index.html');
          }
          return p;
        });
      } catch (e) {
        // 忽略调整失败，不阻塞构建
      }
      return options;
    });
  }
};