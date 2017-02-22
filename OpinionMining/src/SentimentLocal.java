import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import org.apache.hadoop.io.IntWritable;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by bshow on 07/02/17 for testing
 */

//
public class SentimentLocal {
    private File file;
    static String search;
    static OpClassifier op;
    Map<String,MutableInt> map =new HashMap<>();
    TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
    static PorterStemmerTokenizerFactory stemmingFactory;
    static EnglishStopTokenizerFactory stopWordsFactory;

    class MutableInt {
        int value = 1; // note that we start at 1 since we're counting
        public void increment () { value++;      }
        public int  get ()       { return value; }
    }

    private SentimentLocal(File file) {
        this.file = file;
        op=new OpClassifier("TrainedClassifier/classifier.txt");
        stopWordsFactory = new EnglishStopTokenizerFactory(tf);
        stemmingFactory= new PorterStemmerTokenizerFactory(tf);
    }

    private void start(int noOfThreads)
            throws Exception
    {
        java.util.List<Future<String>> results =new ArrayList<>();
        ThreadPoolExecutor es =(ThreadPoolExecutor) Executors.newFixedThreadPool(noOfThreads);

        BufferedReader in = new BufferedReader(new FileReader(file));

        String line;

        while((line = in.readLine()) != null) {
            ClassificationTask ct= new ClassificationTask(line,search);
            Future<String> res = es.submit(ct);
            results.add(res);
        }

        es.shutdown();

        for(Future<String> result : results) {
            MutableInt count = map.get(result.get());
            if (count == null) {
                map.put(result.get(), new MutableInt());
            } else {
                count.increment();
            }
        }
        for (Map.Entry<String,MutableInt> entry : map.entrySet())
        {
            System.out.println(entry.getKey() + " " + entry.getValue().get());
        }
    }



    public static void main(String argv[])
            throws Exception
    {
        long startTime = System.currentTimeMillis();

        search=argv[0];
        File infile= new File("input/trainingdata.csv");

        SentimentLocal s = new SentimentLocal(infile);
        s.start(4);

        long endTime = System.currentTimeMillis();
        long time= (endTime-startTime);
        System.out.print(time);

    }
}
