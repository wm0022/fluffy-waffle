module.exports = {
  lintOnSave: false, // 关闭 ESLint 检查
  devServer: {
    port: 8082, // 用户端使用 8082 端口
    open: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端地址
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      },
      // 静态资源（图片等）代理到后端，解决封面上传后图片无法显示的问题
      // 后端 context-path=/api，所以需要将 /uploads 重写为 /api/uploads
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: { '^/uploads': '/api/uploads' }
      }
    }
  }
}

// Ensure CopyWebpackPlugin does not copy index.html (avoids duplicate index.html)
// This prevents "Multiple assets emit different content to the same filename index.html" error
module.exports.chainWebpack = module.exports.chainWebpack || function (config) {
  config.plugin('copy').tap(options => {
    if (options && options[0] && options[0].patterns) {
      options[0].patterns.forEach(pattern => {
        pattern.globOptions = pattern.globOptions || {};
        pattern.globOptions.ignore = pattern.globOptions.ignore || [];
        if (!pattern.globOptions.ignore.includes('**/index.html')) {
          pattern.globOptions.ignore.push('**/index.html');
        }
      });
    }
    return options;
  });
};