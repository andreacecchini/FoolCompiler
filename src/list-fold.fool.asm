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
lhp

push function2
lhp
sw
lhp
push 1
add
shp
lhp

push function3
lhp
sw
lhp
push 1
add
shp
lhp

push function4
lhp
sw
lhp
push 1
add
shp
lhp

push function5
lhp
sw
lhp
push 1
add
shp
lhp

push function6
lhp
sw
lhp
push 1
add
shp
push function7
push function8
push function9
push function10
lfp
lfp
lfp
lfp
lfp
lfp
stm
ltm
ltm

push -10
add
lw
js
push 4
lfp
stm
ltm
ltm

push -11
add
lw
js
push 3
lfp
stm
ltm
ltm

push -11
add
lw
js
push 2
lfp
stm
ltm
ltm

push -11
add
lw
js
push 1
lfp
stm
ltm
ltm

push -11
add
lw
js
lfp
lfp
push -12
add
lw
lfp
stm
ltm
ltm

push -9
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

function2:
cfp
lra
push 0
stm
sra
pop
pop
pop
sfp
ltm
lra
js

function3:
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

function4:
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
mult
stm
sra
pop
pop
pop
sfp
ltm
lra
js

function5:
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
pop
sfp
ltm
lra
js

function6:
cfp
lra
lfp
push 2
add
lw
stm
sra
pop
pop
pop
sfp
ltm
lra
js

function7:
cfp
lra
lfp
push 1
add
lw
push -1
beq label2
push 0
b label3
label2:
push 1
label3:
push 1
beq label0
lfp
lfp
push 3
add
lw
lfp
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
lfp
push 2
add
lw
lfp
push 3
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
lfp
lfp
push 1
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
lw
stm
ltm
ltm

push -8
add
lw
js
b label1
label0:
lfp
push 2
add
lw
label1:
stm
sra
pop
pop
pop
pop
sfp
ltm
lra
js

function8:
cfp
lra
lfp


push 10000
push -7
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
lfp
push 1
add
lw
lfp
lw
stm
ltm
ltm

push -8
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

function9:
cfp
lra
push -1
stm
sra
pop
sfp
ltm
lra
js

function10:
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
pop
sfp
ltm
lra
js