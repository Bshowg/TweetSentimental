import com.aliasi.classify.*;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.tokenizer.*;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Files;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class OpClassifier {
    String[] categories;
    LMClassifier classifier;
    TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
    PorterStemmerTokenizerFactory stemmingFactory = new PorterStemmerTokenizerFactory(tf);
    EnglishStopTokenizerFactory stopWordsFactory = new EnglishStopTokenizerFactory(tf);


    OpClassifier(String pattern) {
        try {
            classifier= (LMClassifier) AbstractExternalizable.readObject(new File(pattern));
            categories = classifier.categories();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    String classify(String tweet) {
        ConditionalClassification classification = classifier.classify(tweet);
        return classification.bestCategory();
    }


     void train() throws IOException, ClassNotFoundException {
         File trainDir;
         String[] categories;
         LMClassifier classifier;
         trainDir = new File("trainDirectory");
         categories = trainDir.list();
         int nGram = 7;
         classifier= DynamicLMClassifier.createNGramProcess(categories, nGram);
         for (int i = 0; i < categories.length; ++i) {
             String category = categories[i];
             Classification classification = new Classification(category);
             File file = new File(trainDir, categories[i]);
             File[] trainFiles = file.listFiles();
             for (int j = 0; j < trainFiles.length; ++j) {
                 File trainFile = trainFiles[j];
                 BufferedReader in = new BufferedReader(new FileReader(trainFile));
                 String review="";
                 String line;

                 while((line = in.readLine()) != null) {
                     review = cleanData(line) + " ";
                 }
                 Classified classified = new Classified(review, classification);
                 ((ObjectHandler) classifier).handle(classified);
             }
         }
         AbstractExternalizable.compileTo((Compilable) classifier, new File("TrainedClassifier/trainedClassifier.txt"));
     }

     String cleanData(String tweet){

         String[] s = tweet.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
         s[5]=s[5].replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase();
         List<String> words= new ArrayList<String>();
         String result="";
         Tokenizer tokenizer = stopWordsFactory.tokenizer(s[5].toCharArray(),0,s[5].length());
         for(String str : tokenizer){
             words.add(str);
         }
         for(String str : words){
             String stemmed= stemmingFactory.stem(str);
             result+= stemmed + " ";

         }
         return result;
     }
     public static void main(String argv[]){
         OpClassifier op= new OpClassifier("TrainedClassifier/classifier.txt");
         try {
             op.train();
         }
         catch(Exception e){
             System.out.println("Error");
         }
     }
 }