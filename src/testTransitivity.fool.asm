push 0
lhp

push function0
lhp
sw
lhp
push 1
add
shp
lhp

push function0
lhp
sw
lhp
push 1
add
shp
lhp

push function0
lhp
sw
lhp
push 1
add
shp
push function1
lfp

push 10

lhp
sw
lhp
push 1
add
shp
push 10000
push -4
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
lw
push -1
add
lw
stm
sra
pop
sfp
ltm
lra
js

function1:
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