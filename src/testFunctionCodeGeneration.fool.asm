push 0
push 10
push 5
push function0
lfp
lfp
push -3
add
lw
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
push 1
add
lw
lfp
push 2
add
lw
add
stm
sra
pop
pop
pop
sfp
ltm
lra
js