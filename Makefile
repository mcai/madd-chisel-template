DIR := $(CURDIR)

default: all

all: $(MAKE) verilog $(MAKE) test

verilog:
	# sbt "runMain madd.MatrixAddition1"
	# sbt "runMain madd.MatrixAddition2"
	# sbt "runMain madd.MatrixAddition3"
	# sbt "runMain cache.Cache1"
	# sbt "runMain noc.NoCSimulatorTop"
	sbt "runMain prefetcher.MarkovPrefetcherMain"
	-@rm $(DIR)/source/*.fir
	-@rm $(DIR)/source/*.anno.json

test:
	# sbt "runMain madd.MatrixAddition1Tester"
	# sbt "runMain madd.MatrixAddition2Tester"
	# sbt "runMain madd.MatrixAddition3Tester"
	# sbt "runMain cache.Cache1Tester"
	sbt "runMain prefetcher.MarkovPrefetcherTester"

clean:
	-@rm -rf source/