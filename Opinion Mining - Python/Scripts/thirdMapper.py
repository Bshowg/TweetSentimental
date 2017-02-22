from nltk.sentiment import SentimentAnalyzer
import pickle
import sys


file = open("/home/bshow/Scrivania/tweetproj/NBClassifier.txt", "rb")
sentiment_analyzer = pickle.load(file)
file.close()

for line in sys.stdin:
    tweet,relevance=line.split('\t',1)
    if "Relevant" in relevance:
        tokens = tweet.split()
        classification = sentiment_analyzer.classify(tokens)

        print ('%s') %(classification.strip('\n'))
    else:
        print ('%s') %(relevance.strip('\n'))

