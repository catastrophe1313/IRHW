#!/usr/bin/python
from sklearn.datasets import load_svmlight_file
from sklearn.metrics import ndcg_score, average_precision_score, precision_score
import xgboost as xgb
from xgboost import DMatrix

#  This script demonstrate how to do ranking with xgboost.train
x_train, y_train = load_svmlight_file("mq2008.train")
x_valid, y_valid = load_svmlight_file("mq2008.vali")
x_test, y_test = load_svmlight_file("mq2008.test")

group_train = []
with open("mq2008.train.group", "r") as f:
    data = f.readlines()
    for line in data:
        group_train.append(int(line.split("\n")[0]))

group_valid = []
with open("mq2008.vali.group", "r") as f:
    data = f.readlines()
    for line in data:
        group_valid.append(int(line.split("\n")[0]))

group_test = []
with open("mq2008.test.group", "r") as f:
    data = f.readlines()
    for line in data:
        group_test.append(int(line.split("\n")[0]))

train_dmatrix = DMatrix(x_train, y_train)
valid_dmatrix = DMatrix(x_valid, y_valid)
test_dmatrix = DMatrix(x_test)

train_dmatrix.set_group(group_train)
valid_dmatrix.set_group(group_valid)
test_dmatrix.set_group(group_test)

params = {'objective': 'rank:pairwise', 'eta': 0.1, 'gamma': 0.8,
          'min_child_weight': 0.1, 'max_depth': 5}
xgb_model = xgb.train(params, train_dmatrix, num_boost_round=4,
                      evals=[(valid_dmatrix, 'test')])
pred1 = xgb_model.predict(train_dmatrix)
pred2 = xgb_model.predict(valid_dmatrix)
pred3 = xgb_model.predict(test_dmatrix)
print(ndcg_score([y_train], [pred1], k=10))
print(ndcg_score([y_valid], [pred2], k=10))
print(ndcg_score([y_test], [pred3], k=10))
print(ndcg_score([y_train], [pred1], k=3))
print(ndcg_score([y_valid], [pred2], k=3))
print(ndcg_score([y_test], [pred3], k=3))
print(ndcg_score([y_train], [pred1], k=5))
print(ndcg_score([y_valid], [pred2], k=5))
print(ndcg_score([y_test], [pred3], k=5))

