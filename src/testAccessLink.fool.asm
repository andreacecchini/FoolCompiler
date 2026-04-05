push 0
push 10
push 3
push function2
lfp
lfp
push -2
add
lw
lfp
stm
ltm
ltm
push -4
add
lw
js
print
halt

function0:
cfp
lra
lfp
lw
push 1
add
lw
lfp
push 1
add
lw
add
stm
sra
pop
pop
sfp
ltm
lra
js

function1:
cfp
lra
lfp
lfp
push 1
add
lw
lfp
lw
stm
ltm
ltm
push -2
add
lw
js
stm
sra
pop
pop
sfp
ltm
lra
js

function2:
cfp
lra
push function0
push function1
lfp
lfp
lw
push -3
add
lw
lfp
stm
ltm
ltm
push -3
add
lw
js
stm
pop
pop
sra
pop
pop
sfp
ltm
lra
js