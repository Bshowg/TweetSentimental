import sys
from nltk.corpus import stopwords
from nltk.stem.lancaster import LancasterStemmer
import re

stemmer = LancasterStemmer()
stopw = set(stopwords.words('english'))

for line in sys.stdin:
    features = []
    keyword_in = False
    tweet,keyword = line.split( '\t' , 1)
    words = tweet.split()
    processed_tweet = ""
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
   
        for w in features:
            processed_tweet = processed_tweet + w + " "
 
        relevance="Relevant"
    else:
        relevance="Irrelevant"
    print '%s\t%s' %(processed_tweet,relevance.strip('\n'))
