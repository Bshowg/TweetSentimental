import com.aliasi.tokenizer.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SentimentalTwitter extends Configured implements Tool {
    static OpClassifier op;
    static String search;
    static String class_pattern="";
    static final String noCat="Irrelevant";
    static final String splitter=",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
    static final int splitIndex=5;

    public static class PunctuationMapper extends Mapper<Object,Text,Text,IntWritable>{
        private final static IntWritable one = new IntWritable(1);
        public void map(Object key, Text value, Mapper.Context context) throws IOException, InterruptedException {
            //String[] tweets = value.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            String[] tweets = value.toString().split(splitter);
            if(tweets.length>=splitIndex+1) {
                tweets[splitIndex] = tweets[splitIndex].replaceAll("[^a-zA-Z0-9 ]", " ").toLowerCase();
                tweets[splitIndex]=tweets[splitIndex].replaceAll("\\s+", " ");
                context.write(new Text(tweets[splitIndex]), one);
            }
    }
    }
    public static class StemmingStopMapper
            extends Mapper<Text, IntWritable, Text, IntWritable> {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        PorterStemmerTokenizerFactory stemmingFactory = new PorterStemmerTokenizerFactory(tf);
        EnglishStopTokenizerFactory stopWordsFactory = new EnglishStopTokenizerFactory(tf);

        public void map(Text key, IntWritable value, Mapper.Context context) throws IOException, InterruptedException {
            List<String> words= new ArrayList<>();
            Boolean containsKeyword=false;
            String result="";
            Tokenizer tokenizer = stopWordsFactory.tokenizer(key.toString().toCharArray(),0,key.toString().length());
            for(String s : tokenizer){
                if(s.equals(search)) {
                    containsKeyword=true;
                }
                words.add(s);
            }
            if(containsKeyword) {
                String stemmed;
                for (String s : words) {
                    stemmed = stemmingFactory.stem(s);
                    result += stemmed + " ";
                }

                System.out.println(key.toString());
                context.write(new Text(result), value);
            }else{
                context.write(new Text(noCat),value);
            }
        }
    }

    public static class SentimentalMapper extends Mapper<Text, IntWritable, Text, IntWritable> {

        public void map(Text key, IntWritable value, Mapper.Context context) throws IOException, InterruptedException {
            if(key.toString().equals(noCat)){
                context.write(key, value);
            }else {
                String result = op.classify(key.toString());
                context.write(new Text(result), value);
            }

        }
    }

    public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            int res=0;
            for (IntWritable val : values) {
               res+=val.get();
            }
            context.write(key,new IntWritable(res));
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = getConf();

        args = new GenericOptionsParser(conf, args).getRemainingArgs();
        Job job = Job.getInstance(conf);

        Configuration punctuationConf = new Configuration(false);

        ChainMapper.addMapper(job, PunctuationMapper.class, Object.class,
                Text.class, Text.class, IntWritable.class, punctuationConf);
        Configuration stemmingConf = new Configuration(false);

        ChainMapper.addMapper(job, StemmingStopMapper.class, Text.class,
                IntWritable.class, Text.class, IntWritable.class, stemmingConf);

        Configuration sentimentConf = new Configuration(false);

        ChainMapper.addMapper(job, SentimentalMapper.class, Text.class,
               IntWritable.class, Text.class, IntWritable.class, sentimentConf);

        job.setJarByClass(SentimentalTwitter.class);
        job.setReducerClass(Reduce.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        return (job.waitForCompletion(false) ? 0 : 1);
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        search=args[2].toLowerCase();
        class_pattern=args[3];
        op= new OpClassifier(class_pattern);

        int res = ToolRunner.run(new Configuration(), new SentimentalTwitter(), args);

        long endTime = System.currentTimeMillis();
        long time= (endTime-startTime);
        System.out.print(time);

        System.exit(res);

    }

}
