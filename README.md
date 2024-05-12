# README.md

Chisel Lab.

## Usage 用法

0. 获取GitHub账号并创建Codespace：
   前往 [https://github.com](https://github.com) 注册或登录您的GitHub账号。访问 [https://github.com/mcai/madd-chisel-template](https://github.com/mcai/madd-chisel-template)，点击"Code"绿色按钮并选择创建Codespace，选择main分支后使用VSCode web或VSCode桌面版本进行后续步骤。

1. 配置环境，只需运行一次，在VSCode的Terminal中运行:
   ```bash
   ./prepare.sh
   ```

2. 修改、完善代码

   需要修改的地方：
   src/main/scala/prefetcher/MarkovPrefetcher.scala - Markov prefetcher (Markov预取器模块)
   src/main/scala/prefetcher/MarkovPrefetcherIO.scala - Markov prefetcher IO （Markov预取器IO）
   src/main/scala/prefetcher/MarkovPrefetcherTester.scala - Markov prefetcher tester （Markov预取器单元测试）

3. 运行单元测试，在VSCode的Terminal中运行：

   ```bash
   ./run.sh
   ```

4. 修改、完善代码

5. 运行单元测试，在VSCode的Terminal中运行
   ...

## Examples 例子

src/main/scala/madd/MatrixAddition1*.scala - Single-Cycle matrix addition sample in Chisel 6
src/main/scala/madd/MatrixAddition2*.scala - Multi-Cycle matrix addition sample in Chisel 6
src/main/scala/madd/MatrixAddition3*.scala - Pipelined matrix addition sample in Chisel 6
src/main/scala/cache/Cache1.scala - Cache sample in Chisel 6
