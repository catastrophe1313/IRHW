import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.Scanner;

public class Control {
    public static void main(String[] args) throws Exception {
        String modelType = "";
        String indexPath = "";
        String queryPath = "";
        String outputPath = "";

        if (args.length == 0) {
            modelType = "BM25";
            indexPath = "index";
            queryPath = "data/test/topics.351-400";
            outputPath = "result/result.txt";
        } else {
            modelType = args[0];
            indexPath = args[1];
            queryPath = args[2];
            outputPath = args[3];
        }

        IndexFiles ifs = new IndexFiles();
        SearchFiles sfs = new SearchFiles();
        ifs.indexing(indexPath, modelType);
        sfs.searching(queryPath, outputPath, modelType);
    }
}
