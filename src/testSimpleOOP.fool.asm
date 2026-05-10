push 0
lhp

push function0
lhp
sw
lhp
push 1
add
shp
push function1
lhp
sw
lhp
push 1
add
shp

push 1
push 1

lhp
sw
lhp
push 1
add
shp
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

push 1
push 2

lhp
sw
lhp
push 1
add
shp
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
lfp
lfp
push -4
add
lw
lfp
push -3
add
lw
stm
ltm
ltm
lw
push 1
add
lw
js
lfp
lfp
push -5
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
lfp
lw
push -1
add
lw
lfp
lw
push -2
add
lw
add
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
lfp
lw
stm
ltm
ltm
lw
push 0
add
lw
js
lfp
lfp
push 1
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

lhp
sw
lhp
push 1
add
shp
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
stm
sra
pop
pop
sfp
ltm
lra
js