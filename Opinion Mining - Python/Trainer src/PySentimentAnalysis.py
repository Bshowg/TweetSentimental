import csv
import nltk
from nltk.classify import NaiveBayesClassifier as nbc
from nltk.corpus import stopwords
from nltk.sentiment import SentimentAnalyzer
from nltk.sentiment.util import *
from nltk.stem.lancaster import LancasterStemmer
import re
import pickle
import os
import filecmp


def main():

    stemmer = LancasterStemmer()

    #create and train a classifier if it doesn't exist
    if os.path.getsize("NBClassifier.txt") == 0:
        sentiment_analyzer = SentimentAnalyzer()

        with open('trainset/trainset.csv') as csvfile:
            readCSV = csv.reader(csvfile, delimiter=',')
            tweets = []
            fraction = input(
                'Type the number representing the fraction of the dataset you want to use to train the classifier (es: if you want 1/3 type 3): ')
            splitted_dataset = choose_subset(readCSV)
            neg_length = len(splitted_dataset[0]) / fraction
            neu_length = len(splitted_dataset[1]) / fraction
            pos_length = len(splitted_dataset[2]) / fraction
            dataset = (splitted_dataset[0][0:neg_length]) + (splitted_dataset[1][0:neu_length]) + (
            splitted_dataset[2][0:pos_length])
            print ("New dataset's length: %d" % len(dataset))

            for row in dataset:
                feature_vector = getFeatureVector(row[5], stemmer)
                if (row[0] == "4"):
                    sentiment = "pos"
                elif (row[0] == "2"):
                    sentiment = "neu"
                else:
                    sentiment = "neg"

                tweets.append((feature_vector, sentiment))

            feature_list = sentiment_analyzer.all_words(tweets, True)

            unigram_feats = sentiment_analyzer.unigram_word_feats(feature_list, min_freq=4)

            sentiment_analyzer.add_feat_extractor(extract_unigram_feats, unigrams=unigram_feats)

            train_set = sentiment_analyzer.apply_features(tweets, True)

            print "Trainset created"
            # print train_set

        classifier = sentiment_analyzer.train(nbc.train, train_set)

        save_classifier = open("NBClassifier.txt", "wb")
        pickle.dump(sentiment_analyzer, save_classifier)
        save_classifier.close()

        print "NBC trained and saved into a file"
    #end if

    else:
        saved_classifier = open("NBClassifier.txt" , "rb")
        sentiment_analyzer = pickle.load(saved_classifier)
        saved_classifier.close()

    #end else

    with open('testset/testset.csv') as csvfile:
        readCSV = csv.reader(csvfile, delimiter=',')

        tweets = []

        for row in readCSV:
            feature_vector = getFeatureVector(row[5] , stemmer)

            if (row[0] == "4") :
                sentiment = "pos"
            elif (row[0] == "2"):
                sentiment = "neu"
            else:
                sentiment = "neg"

            tweets.append((feature_vector , sentiment))

        test_set = sentiment_analyzer.apply_features(tweets , True)
        print "Testset created"




    print ("Tweets to classify: %d" %(len(tweets)) )
    print "Evaluating classifier with testset:"
    for tweet in tweets:
        print tweet
        print sentiment_analyzer.classify(tweet[0])

    sentiment_analyzer.evaluate(test_set , verbose=True)


    return


# look for 2 or more repetitions of character and replace with the character itself
def replaceTwoOrMore(s):

    pattern = re.compile(r"(.)\1{1,}", re.DOTALL)
    return pattern.sub(r"\1\1", s)

# check for little changes to adjust and parse tweets
def preprocessTweets(tweet):

    tweet = tweet.lower()
    #remove additional white spaces
    tweet = re.sub('[\s]+', ' ', tweet)
    #remove hashtags
    tweet = re.sub(r'#([^\s]+)', r'\1', tweet)

    return tweet


def getFeatureVector(tweet , stemmer):
    feature_vector = []
    processed_tweet = preprocessTweets(tweet)
    stopw = set(stopwords.words('english'))
    words = processed_tweet.split()
    for w in words:
        w = replaceTwoOrMore(w)
        w = w.strip('\'"?,.')
        val = re.search(r"^[a-zA-Z][a-zA-Z0-9]*$", w)

        if (w in stopw or val is None):
            continue
        else:
            w = stemmer.stem(w)
            feature_vector.append(w)
    return feature_vector


def choose_subset(dataset):
    pos_subset = []
    neu_subset = []
    neg_subset = []
    splitted_dataset = []
    for row in dataset:
        if row[0] == "4":
            pos_subset.append(row)
        elif row[0] == "2":
            neu_subset.append(row)
        else:
            neg_subset.append(row)
    splitted_dataset.append(neg_subset)
    splitted_dataset.append(neu_subset)
    splitted_dataset.append(pos_subset)

    return splitted_dataset



main()

