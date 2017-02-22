import sys
import string
import csv
import re

from nltk.corpus import stopwords
from nltk.stem.lancaster import LancasterStemmer

from nltk.sentiment import SentimentAnalyzer
import pickle


stemmer = LancasterStemmer()
stopw = set(stopwords.words('english'))

file = open("/home/bshow/Scrivania/tweetproj/NBClassifier.txt", "rb")
sentiment_analyzer = pickle.load(file)
file.close()







keyword = sys.argv[1]

for line in sys.stdin:
    csv_line = list(csv.reader(line , delimiter = ','))
    tweet = csv_line[10][0]
    for c in string.punctuation:
        tweet = tweet.replace(c, " ")
    tweet = re.sub('[\s]+', ' ', tweet)
    tweet = tweet.lower()



    features = []
    keyword_in = False
    #tweet,keyword = line.split( '\t' , 1)
    words = tweet.split()
    
    for w in words:
        if (keyword.strip('\n') in w):
            keyword_in = True
	    break
    if keyword_in == True :
        for w in words:
	    val = re.search(r"^[a-zA-Z][a-zA-Z0-9]*$", w)
       	    if (w in stopw or val is None):
                continue
            else:
                w = stemmer.stem(w)
                features.append(w)
    
        relevance="Relevant"
    else:
        relevance="Irrelevant"


    #tweet,relevance=line.split('\t',1)
    if "Relevant" in relevance:
        classification = sentiment_analyzer.classify(features)

        print ('%s') %(classification.strip('\n'))
    else:
        print ('%s') %(relevance.strip('\n'))









    #print '%s\t%s' %(tweet, keyword)
