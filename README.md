# Chisel Lab

## 使用方法

0. 获取 GitHub 账号并创建 Codespace：
   访问 [GitHub 官网](https://github.com) 注册或登录您的 GitHub 账号。前往 [madd-chisel-template 项目页面](https://github.com/mcai/madd-chisel-template)，点击“Code”绿色按钮选择创建 Codespace。选择 main 分支后，可选择使用 VSCode Web 版或桌面版进行后续操作。

1. 配置环境，此步骤仅需运行一次，在 VSCode 的 Terminal 中执行：
   ```bash
   ./prepare.sh
   ```

2. 修改和完善代码：

   修改文件如下：
   - `src/main/scala/prefetcher/MarkovPrefetcher.scala` - Markov 预取器模块
   - `src/main/scala/prefetcher/MarkovPrefetcherIO.scala` - Markov 预取器 IO
   - `src/main/scala/prefetcher/MarkovPrefetcherTester.scala` - Markov 预取器单元测试

3. 运行单元测试，在 VSCode 的 Terminal 中执行：
   ```bash
   ./run.sh
   ```

4. 重复修改和完善代码步骤。

5. 再次运行单元测试。

## 示例代码

- `src/main/scala/madd/MatrixAddition1*.scala` - Chisel 6 单周期矩阵加法示例
- `src/main/scala/madd/MatrixAddition2*.scala` - Chisel 6 多周期矩阵加法示例
- `src/main/scala/madd/MatrixAddition3*.scala` - Chisel 6 流水线矩阵加法示例
- `src/main/scala/cache/Cache1.scala` - Chisel 6 缓存示例
