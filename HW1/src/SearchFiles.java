/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

/**
 * Simple command-line based search demo.
 */
public class SearchFiles {
    public SearchFiles() {

    }

    /**
     * Simple command-line based search demo.
     */
    public static void searching(String queryPath, String outputPath, String modelType) throws Exception {
        String usage =
                "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";

        String index = "index";
        String field = "contents";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        if (modelType == "BM25") {
            Similarity sim25 = new BM25Similarity();
            searcher.setSimilarity(sim25);

        } else {
            LMDirichletSimilarity lm = new LMDirichletSimilarity();
            searcher.setSimilarity(lm);
        }


        Path file = Paths.get(queryPath);
        System.out.println("Indexed " + reader.numDocs() + " documents");
        QueryParser parser = new QueryParser(field, analyzer);
        doSearch(file, parser, searcher, outputPath, reader, modelType);

        reader.close();
    }

    public static void doSearch(Path file, QueryParser parser, IndexSearcher searcher, String output, IndexReader reader, String modeltype) throws Exception {
        File f = new File(output);
        FileOutputStream fos = new FileOutputStream(f);
        OutputStreamWriter dos = new OutputStreamWriter(fos);
        RMMethods rm = new RMMethods();
        String docs = Files.readString(file);
        String[] singleDoc = docs.split("<top>");
        String pt1 = "(?<=Number:\\s)[0-9]*";
        String pt2 = "(?<=<title>)[\\s\\S]*(?=<desc>)";
        String pt3 = "(?<=Description:)[\\s\\S]*(?=<narr>)";
        Pattern p1 = Pattern.compile(pt1);
        Pattern p2 = Pattern.compile(pt2);
        Pattern p3 = Pattern.compile(pt3);
        for (String i : singleDoc) {
            Matcher m1 = p1.matcher(i);
            Matcher m2 = p2.matcher(i);
            Matcher m3 = p3.matcher(i);
            if (m1.find() && m2.find() && m3.find()) {
                String topicNO = m1.group(0);
                String desc = m2.group(0) + m3.group(0);
                desc = desc.replaceAll("[\\pP\\p{Punct}|\\n]", " ");
                Query query = parser.parse(desc);
                TopDocs results = searcher.search(query, 1000);
                ScoreDoc[] hits = results.scoreDocs;
                Map<String, String> docMap = new HashMap<>();
                Map<String, Double> scoreMap = new HashMap<>();
                int doccount = 0;
                for (int j = 0; j < hits.length; j++) {
                    Document doc = searcher.doc(hits[j].doc);
                    String docNO = doc.get("title");
                    if (doccount < 50) {
                        docMap.put(docNO, doc.get("contents"));
                        scoreMap.put(docNO, (double) hits[j].score);
                    }
                    doccount++;
                    if (modeltype.equals("BM25") || modeltype.equals("LM")) {
//                        System.out.println(topicNO + "\t" + "Q0" + "\t" + docNO + "\t" + (j + 1) + "\t" + hits[j].score + "\t" + "wxu236");
                        dos.write(topicNO + "\t" + "Q0" + "\t" + docNO + "\t" + (j + 1) + "\t" + hits[j].score + "\t" + "wxu236" + "\n");
                    }
                }
                if (modeltype.equals("RM1")|| modeltype.equals("RM3")) {
                    Map<String, Double> resultMap = new LinkedHashMap<>();
                    Map<String, Double> rerankMap = new LinkedHashMap<>();
                    if (modeltype.equals("RM1")) {
                        resultMap = rm.RM1(desc, docMap);
                    }
                    if (modeltype.equals("RM3")) {
                        resultMap = rm.RM3(desc, docMap);
                    }

                    rerankMap = rm.reRank(resultMap, docMap, scoreMap);
                    int count = 1;
                    for (Map.Entry<String, Double> entry : rerankMap.entrySet()) {
//                        System.out.println(topicNO + "\t" + "Q0" + "\t" + entry.getKey() + "\t" + count + "\t" + entry.getValue() + "\t" + "wxu236");
                        dos.write(topicNO + "\t" + "Q0" + "\t" + entry.getKey() + "\t" + count + "\t" + entry.getValue() + "\t" + "wxu236" + "\n");
                        count++;
                    }
                }
            } else {
                continue;
            }
        }
        dos.close();
    }

}
