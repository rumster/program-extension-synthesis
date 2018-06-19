# Jminor Benchmarks

Benchmarks for evaluating the synthesizer on specifications where examples are given in terms of a store formula followed by a seuqnece of commands.

Example ::= example { StoreFormula -> Command -> ... -> Command }

## Store Formulas
A store formula provides a way of representing stores with undefined values. Store formulas range over program variables, integer constants and object variables. An object variable looks just like a variable, except that it is not declared anywhere.
The Jminor front-end automatically constructs a store out of a store formula.

StoreFormula ::= [Equality && ... && Equality]

Equality ::= var == obj

Equality ::= var == int_constant

Equality ::= var == obj.field

Equality ::= obj.field == obj

Equality ::= obj.field == int_constant

## Command
Command ::= Expr = Expr;

Command ::= Command Command

Command ::= if (Expr) { Command }

Command ::= if (Expr) { Command } else { Command }

Command ::= while (Expr) { Command }

Expr ::= var

Expr ::= int_constant

Expr ::= Expr.field

Expr ::= Expr Op Expr

Op ::= + | - | * | /
