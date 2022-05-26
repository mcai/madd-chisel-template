# README.md

Single-cycle and multi-cycle matrix addition samples.

# File Structure

src/main/scala/madd/MatrixAddition1*.scala - Single-Cycle matrix addition sample in Chisel 3
src/main/scala/madd/MatrixAddition2*.scala - Multi-Cycle matrix addition sample in Chisel 3

# Usage

Step 1. Use Visual Studio Code (https://code.visualstudio.com/) + Docker Extension (Docker + Remote Containers, both by Microsoft) to attach to the Docker container as told by the instructor. 

Step 2. Inside the container, run `./test.sh` to run sample test for single cycle and multi-cycle matrix addition samples.

Step 3. Implement, run and test single-cycle and multi-cycle matrix multiplication in Chisel 3.

# TODOs

madd/mmult
    MatrixMultiplication1.scala -> single cycle matrix multiplication
    MatrixMultiplication1IO.scala
    MatrixMultiplication1Tester.scala

    MatrixMultiplication2.scala -> multi cycle matrix multiplication
    MatrixMultiplication2IO.scala
    MatrixMultiplication2Tester.scala

    MatrixMultiplication3.scala -> pipelined matrix multiplication
    MatrixMultiplication3IO.scala
    MatrixMultiplication3Tester.scala