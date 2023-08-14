import numpy as np

f = open('../res.txt','r')
lines = f.readlines()
lists = [[] for i in range(0, 9)]
for i in range(0, 5):
    for j in range(len(lists)):
        lists[j].append(float(lines[i*9+j].split()[0]))
print(lists)
for i in lists:
    print(np.mean(i))

