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
push function2
push function3
push function5
push function6
lfp
lfp
lfp
lfp
lfp
lfp
stm
ltm
ltm

push -3
add
lw
js
push 4
lfp
stm
ltm
ltm

push -4
add
lw
js
push 3
lfp
stm
ltm
ltm

push -4
add
lw
js
push 2
lfp
stm
ltm
ltm

push -4
add
lw
js
push 1
lfp
stm
ltm
ltm

push -4
add
lw
js
lfp
lfp
lfp
lfp
push -7
add
lw
lfp
stm
ltm
ltm

push -6
add
lw
js
lfp
stm
ltm
ltm

push -5
add
lw
js
lfp
stm
ltm
ltm

push -6
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
push -1
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

function4:
cfp
lra
lfp
push 1
add
lw
lfp
lfp
lw
lw
stm
ltm
ltm

push -3
add
lw
js
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
lfp
push 2
add
lw
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
lw
lw
stm
ltm
ltm

push -4
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

push -2
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
sfp
ltm
lra
js

function5:
cfp
lra
push function4
lfp
push -1
lfp
push 1
add
lw
lfp
stm
ltm
ltm

push -2
add
lw
js
stm
pop
sra
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
push 1
add
lw
lfp
lfp
lw
stm
ltm
ltm

push -3
add
lw
js
beq label6
push 0
b label7
label6:
push 1
label7:
push 1
beq label4

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
print
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
push 1
add
lw
js
lfp
lw
stm
ltm
ltm

push -6
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
b label5
label4:
push -1
print
label5:
stm
sra
pop
pop
sfp
ltm
lra
js