# (Experimental) Jminor Semantic Benchmarks

Benchmarks for evaluating the synthesizer on specifications where examples are given in terms of a sequence of store formulas.
Each store formula represents the values that were read and the values that were written by an unknown command.
A trivial conjunct, e.g., x==x means that that value was read, where as non-trivial conjuncts convey the values that were modified.
