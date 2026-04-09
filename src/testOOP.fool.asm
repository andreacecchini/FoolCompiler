push 0
lhp

push function0
lhp
sw
lhp
push 1
add
shp

push 100

lhp
sw
lhp
push 1
add
shp
push 10000
push -2
add
lw
lhp
sw
lhp
lhp
push 1
add
shp
push -1
lfp
push 0
lfp
push -3
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
print
halt

function0:
cfp
lra
push 10
lfp
push 1
add
lw
push 1
beq label0
lfp
lw
push -1
add
lw
b label1
label0:
lfp
lw
push -1
add
lw
lfp
push -2
add
lw
add
label1:
stm
pop
sra
pop
pop
sfp
ltm
lra
js