import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import com.aliasi.tokenizer.*;
import org.apache.hadoop.io.Text;

/**
 * Created by bshow on 07/02/17 for testing
 */

//Callable class used by SentimentLocal to start threads

public class ClassificationTask implements Callable<String> {

    private String tweet;
    private String search;
    static String splitter=",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
    static int splitIndex=5;



    ClassificationTask(String str,String search){
        tweet=str;
        this.search=search;
    }

    public String call() throws Exception {
        Boolean containsKeyword=false;
        //String[] tweets= tweet.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String[] tweets=tweet.split(splitter);
        if(tweets.length<splitIndex+1){
            return "";
        }
        tweets[splitIndex]=tweets[splitIndex].replaceAll("[^a-zA-Z0-9 ]", " ").toLowerCase();
        tweets[splitIndex]=tweets[splitIndex].replaceAll("\\s+", " ");
        List<String> words= new ArrayList<String>();
        String result="";
        Tokenizer tokenizer = SentimentLocal.stopWordsFactory.tokenizer(tweets[splitIndex].toCharArray(),0,tweets[splitIndex].length());
        for(String s : tokenizer){
            if(s.equals(search)) {
                containsKeyword=true;
            }
            words.add(s);
        }
        if(containsKeyword){
            String stemmed;
            for (String s : words) {
                stemmed = SentimentLocal.stemmingFactory.stem(s);
                result += stemmed + " ";

            }

            return SentimentLocal.op.classify(result);
        }
        return "Irrelevant";

    }

}
