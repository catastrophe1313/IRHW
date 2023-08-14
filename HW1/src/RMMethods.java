import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;

public class RMMethods {
    static double DOCLEN;
    static Map<String, Map> TERMFREQ = new HashMap<>();
    static Map<String, Double> TERMLEN = new HashMap<>();

    public Map<String, Double> RM1(String query, Map<String, String> doc) {
        DOCLEN = 0.0;
        Map<String, Double> tqMap = new HashMap<>();
        String[] qList = query.split("\\x20");
        double totalSum = 0.0;
        for (Map.Entry<String, String> entry : doc.entrySet()) {
            double qd = 1.0;
            String content = entry.getValue();
            String id = entry.getKey();
            content = content.replaceAll("[\\pP\\p{Punct}|\\n]", " ");
            String[] tList = content.split("\\x20");
            List<String> atList = new ArrayList<>(Arrays.asList(tList));
            atList.removeAll(List.of("", "\n"));
            DOCLEN += atList.size();
            Set<String> tSet = new HashSet();
            tSet.addAll(Arrays.asList(tList));
            for (String j : qList) {
                double qCnt = Collections.frequency(List.of(qList), j);
                double qFreq = (qCnt + 1.0) / (atList.size() + doc.size());
                qd *= qFreq;
            }


            Map<String, Double> tempFreq = new HashMap<>();

            for (String i : tSet) {
                double tCnt = Collections.frequency(atList, i);
                double tFreq = (tCnt + 1.0) / (atList.size() + tSet.size());
                if (!tqMap.containsKey(i)) {
                    tqMap.put(i, tFreq * qd);
                } else {
                    double temp = tqMap.get(i);
                    temp += tFreq * qd;
                    tqMap.put(i, temp);
                }
                tempFreq.put(i, tFreq);
                TERMFREQ.put(id, tempFreq);
                if (!TERMLEN.containsKey(i)) {
                    TERMLEN.put(i, tCnt);
                } else {
                    double temp = TERMLEN.get(i);
                    temp += tCnt;
                    TERMLEN.put(id, temp);
                }

                totalSum += tFreq * qd;
            }
        }

        Map<String, Double> resMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : tqMap.entrySet()) {
            String term = entry.getKey();
            double sum = entry.getValue();
            double result = sum / totalSum;
            resMap.put(term, result);
        }

        Map<String, Double> resSortedMap = new LinkedHashMap<>();
        resMap.entrySet().stream().sorted(comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(entry -> resSortedMap.put(entry.getKey(), entry.getValue()));

        return resSortedMap;
    }

    public Map<String, Double> RM3(String query, Map<String, String> doc) {
        Map<String, Double> resultMap = new LinkedHashMap<>();
        Map<String, Double> resMap = new LinkedHashMap<>();
        Map<String, Double> mleMap = new HashMap<>();
        double lambda = 0.8;
        resultMap = RM1(query, doc);
        for (Map.Entry<String, Double> entry : resultMap.entrySet()) {
            double mle = 0.0;
            String term = entry.getKey();
            mleMap.put(term, mle);
            for (Map.Entry<String, String> entrys : doc.entrySet()) {
                String id = entrys.getKey();
                Map<String, Double> termFreq = TERMFREQ.get(id);
                if (term != "") {
                    double singleTermLen = TERMLEN.get(term);
                    double singleTermFreq = 0.0;
                    double tempMLE = mleMap.get(term);
                    if (termFreq.containsKey(term)) {
                        singleTermFreq = termFreq.get(term);
                    }
                    tempMLE += lambda * singleTermFreq + (1 - lambda) * (singleTermLen / DOCLEN);
                    mleMap.put(term, tempMLE);
                }
            }
        }
        for (Map.Entry<String, Double> entry : resultMap.entrySet()) {
            String term = entry.getKey();
            double rm1 = resultMap.get(term);
            double mle = mleMap.get(term);
            double result = lambda * rm1 + (1 - lambda) * mle;
            resMap.put(term, result);
        }

        Map<String, Double> resSortedMap = new LinkedHashMap<>();
        resMap.entrySet().stream().sorted(comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(entry -> resSortedMap.put(entry.getKey(), entry.getValue()));

        return resSortedMap;
    }

    public Map<String, Double> reRank(Map<String, Double> topTerm, Map<String, String> doc, Map<String, Double> scoreMap) throws Exception {
        Map<String, Double> finalMap = new HashMap<>();
        double lambda = 0.8;
        for (Map.Entry<String, String> entry : doc.entrySet()) {
            String id = entry.getKey();
            Map<String, Double> termFreq = TERMFREQ.get(id);
            double score = scoreMap.get(id);
            int termCount = 0;
            for (Map.Entry<String, Double> entrys : topTerm.entrySet()) {
                String term = entrys.getKey();
                if (term != "") {
                    double singleTermLen = TERMLEN.get(term);
                    double singleTermFreq = 0.0;
                    if (termFreq.containsKey(term)) {
                        singleTermFreq = termFreq.get(term);
                    }
                    if (termCount <= 100) {
                        double topResult = entrys.getValue();
                        double newResult = lambda * singleTermFreq + (1 - lambda) * (singleTermLen / DOCLEN);
                        if (newResult != 0) {
                            score += topResult * Math.log((topResult / newResult) + 1.0);
                        }
                        termCount++;
                    } else {
                        break;
                    }
                }
            }
            finalMap.put(id, score);
        }

        Map<String, Double> finalSortedMap = new LinkedHashMap<>();
        finalMap.entrySet().stream().sorted(comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(entry -> finalSortedMap.put(entry.getKey(), entry.getValue()));

        return finalSortedMap;
    }

}
