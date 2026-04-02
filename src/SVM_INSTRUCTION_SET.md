# SVM Instruction Set Reference

## Overview

The SVM is a stack-based machine based on 4 bytes words (**wordSize = 4**).

- Instructions are stored in `code` (max size: 10000 words).
- Data, stack, and heap share the same `memory` array (size: 10000 words).
- Stack grows from high addresses to low addresses.
    - -wordSize -> advance
    - +wordSize -> step back
- Heap grows from low addresses to high addresses.
    - +wordSize -> advance
    - -wordSize -> step back

Main registers:

- `ip` (instruction pointer): next opcode position in `code`.
- `sp` (stack pointer): top of stack in `memory`.
- `fp` (frame pointer): current frame base.
- `hp` (heap pointer): current heap top.
- `ra` (return address): return instruction address for jumps/subroutine style control flow.
- `tm` (temporary): a temporary register.

Execution loop:

1. Fetch opcode from `code[ip]`, then increment `ip`.
2. Decode and execute instruction.
3. Stop only on `HALT`.

## Labels and Address Resolution

In assembly, control-flow instructions use labels (for example `b end`).

During parsing:

- label definitions are stored as `label -> address`.
- label uses are stored as `codeIndex -> label`.

At the end of parsing, each unresolved label reference is replaced by its numeric address in `code`.

## Instruction Reference

For each instruction, stack effects are shown as:

- before -> after
- top of stack is on the right side in examples

### Stack

#### `push <INTEGER>`

- Mnemonic: `push`
- Encoding: opcode `PUSH`, immediate operand from code stream
- Stack effect: `... -> ..., value`
- Description: pushes an immediate integer onto the stack.

#### `push <LABEL>`

- Mnemonic: `push`
- Encoding: opcode `PUSH`, immediate address resolved from label
- Stack effect: `... -> ..., address`
- Description: pushes a code address (label address) onto the stack.

#### `pop`

- Mnemonic: `pop`
- Encoding: opcode `POP`
- Stack effect: `..., x -> ...`
- Description: discards the top stack value.

### Arithmetic

All arithmetic instructions pop two values and push one result.

Runtime convention:

- `v1 = pop()` (top value)
- `v2 = pop()` (next value)
- push result of `v2 op v1`

#### `add`

- Mnemonic: `add`
- Stack effect: `..., a, b -> ..., a + b`

#### `sub`

- Mnemonic: `sub`
- Stack effect: `..., a, b -> ..., a - b`
- Note: subtraction is not commutative. The VM computes `v2 - v1`.

#### `mult`

- Mnemonic: `mult`
- Stack effect: `..., a, b -> ..., a * b`

#### `div`

- Mnemonic: `div`
- Stack effect: `..., a, b -> ..., a / b`
- Note: integer division, computed as `v2 / v1`.

### Memory

#### `sw` (STOREW)

- Mnemonic: `sw`
- Stack effect: `..., value, address -> ...`
- Runtime behavior:
    1. `address = pop()`
    2. `memory[address] = pop()`
- Important: address must be at stack top, value just below it.

#### `lw` (LOADW)

- Mnemonic: `lw`
- Stack effect: `..., address -> ..., memory[address]`
- Runtime behavior: pop address, push value loaded from memory.

### Control Flow

#### `b <LABEL>` (BRANCH)

- Mnemonic: `b`
- Operand source: immediate from code stream (not from stack)
- Stack effect: unchanged
- Runtime behavior: `ip = address`
- Notes:
    - unconditional jump
    - does not consume stack values

#### `beq <LABEL>` (BRANCHEQ)

- Mnemonic: `beq`
- Operand source: immediate target address from code stream
- Stack effect: `..., a, b -> ...`
- Runtime behavior:
    1. read `address`
    2. `v1 = pop()`, `v2 = pop()`
    3. if `v2 == v1`, then `ip = address`

#### `bleq <LABEL>` (BRANCHLESSEQ)

- Mnemonic: `bleq`
- Operand source: immediate target address from code stream
- Stack effect: `..., a, b -> ...`
- Runtime behavior:
    1. read `address`
    2. `v1 = pop()`, `v2 = pop()`
    3. if `v2 <= v1`, then `ip = address`

#### `js`

- Mnemonic: `js`
- Operand source: stack (indirect jump)
- Stack effect: `..., address -> ...`
- Runtime behavior:
    1. `address = pop()`
    2. `ra = ip`
    3. `ip = address`
- Note: unlike `b`, target is not read from code stream.

### Register Load/Store

#### `lra` / `sra`

- `lra`: `... -> ..., ra`
- `sra`: `..., x -> ...` and `ra = x`

#### `ltm` / `stm`

- `ltm`: `... -> ..., tm`
- `stm`: `..., x -> ...` and `tm = x`

#### `lfp` / `sfp` / `cfp`

- `lfp`: `... -> ..., fp`
- `sfp`: `..., x -> ...` and `fp = x`
- `cfp`: stack unchanged, `fp = sp`

#### `lhp` / `shp`

- `lhp`: `... -> ..., hp`
- `shp`: `..., x -> ...` and `hp = x`

### Output and Halt

#### `print`

- Mnemonic: `print`
- Stack effect: unchanged
- Behavior:
    - if stack is not empty (`sp < MEMSIZE`): prints top value `memory[sp]`
    - otherwise prints `Empty stack!`

#### `halt`

- Mnemonic: `halt`
- Behavior: stops VM execution and returns from `cpu()`.

## Complete Supported Mnemonics

`push pop add sub mult div sw lw b beq bleq js lra sra ltm stm lfp sfp cfp lhp shp print halt`

## Short Examples

### Example 1: Arithmetic and print

```asm
push 2
push 3
add
print
halt
```

Stack trace (top on right):

1. `[]`
2. `[2]`
3. `[2, 3]`
4. `[5]`
5. output: `5`

### Example 2: Conditional branch

Goal: print `1` if `3 <= 5`, else print `0`.

```asm
push 3
push 5
bleq leq_true
push 0
b end
leq_true:
push 1
end:
print
halt
```

How it works:

- `bleq leq_true` pops `v1 = 5`, `v2 = 3`, checks `3 <= 5` (true), jumps to `leq_true`.
- `push 1` is executed, then `print` outputs `1`.

## Common Pitfalls

1. `sub` and `div` use `v2 op v1`, not `v1 op v2`.
2. `sw` expects `address` on top of stack and `value` below.
3. `b`, `beq`, `bleq` read jump target from code stream (label operand), while `js` reads target from stack.
4. No built-in runtime checks for stack underflow/overflow, heap/stack collision, invalid memory addresses, or division
   by zero.
5. Since stack and heap share `memory`, generated code must keep `sp` and `hp` separated.
