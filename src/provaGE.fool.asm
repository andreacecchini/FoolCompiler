push 0
push 2
push 1
lfp
push -3
add
lw
lfp
push -2
add
lw
bleq label0
push 0
b label1
label0:
push 1
label1:
lfp
push -4
add
lw
print
halt