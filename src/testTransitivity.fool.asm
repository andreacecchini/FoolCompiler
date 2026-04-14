push 0
lhp

lhp

lhp

push function0
lfp


push 10000
push -3
add
lw
lhp
sw
lhp
lhp
push 1
add
shp
lfp
stm
ltm
ltm

push -5
add
lw
js
halt

function0:
cfp
lra
lfp
push 1
add
lw
stm
sra
pop
pop
sfp
ltm
lra
js