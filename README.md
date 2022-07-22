# README.md

Matrix addition and cache samples.

# File Structure

src/main/scala/madd/MatrixAddition1*.scala - Single-Cycle matrix addition sample in Chisel 3
src/main/scala/madd/MatrixAddition2*.scala - Multi-Cycle matrix addition sample in Chisel 3
src/main/scala/madd/MatrixAddition3*.scala - Pipelined matrix addition sample in Chisel 3
src/main/scala/cache/Cache1.scala - Cache sample in Chisel 3

# Usage

Step 1. Use Visual Studio Code (https://code.visualstudio.com/) + Docker Extension (Docker + Remote Containers, both by Microsoft) to attach to the Docker container as told by the instructor. 

Step 2. Inside the container, run `make test` to run sample test for single cycle, multi-cycle, pipelined matrix addition and cache samples.

Step 3. Implement, run and test single-cycle, multi-cycle, pipelined matrix multiplication and cache in Chisel 3.
