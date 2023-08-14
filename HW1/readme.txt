Control.java
	Main: Receive user inputs and call index and search functions.


RMMethods.java
	RM1: Calculate term probability from Top 50 initial retrieval documents and return a hashmap of terms and probabilities.

	RM3: Calculate term probability from Top 50 initial retrieval documents depending on the result of RM1 and MLE and return a hashmap of terms and probabilities.

	reRank: Rerank the document scores based on the Top 100 terms from the result of RM1 or RM3. Calculate KL-Divergence to get new scores and return a hashmap of docnumbers and scores.


IndexFiles.java
	indexDoc: Parse documents from the text datasets. Get titles and contents of each article and save them as fields for lucene to use.
	indexDocs: Call indexDoc function.
	indexing: Receive user inputs related to indexing process and set some indexing parameters including similarity functions.


SerchFiles.java
	doSearch: Parse queries from the topic datasets. Use queries to search indexed documents and get retrieval results for outputs or for further reranking of RM1 or RM3.
	searching: Receive user inputs related to indexing process and set some indexing parameters including similarity functions and search fields.