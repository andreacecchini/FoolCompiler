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

push function1
lhp
sw
lhp
push 1
add
shp
push function2
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
push function4
lhp
sw
lhp
push 1
add
shp

push 10

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

push 10
push 20

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

push 10
lfp
push -6
add
lw

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
push 1
push 1
beq label4
lfp
push -7
add
lw
b label5
label4:
lfp
push -5
add
lw
label5:
halt

function0:
cfp
lra
lfp
push 1
add
lw
push 1
beq label0
push 0
b label1
label0:
lfp
lw
push -1
add
lw
label1:
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
push 1
lfp
push 1
add
lw
bleq label2
push 0
b label3
label2:
push 1
label3:
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
lfp
lw
push -2
add
lw
stm
sra
pop
sfp
ltm
lra
js

function3:
cfp
lra
lfp
push 0
lfp
lw
push -2
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
stm
sra
pop
sfp
ltm
lra
js

function4:
cfp
lra
lfp
push 1
lfp
lw
push -2
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
stm
sra
pop
sfp
ltm
lra
js