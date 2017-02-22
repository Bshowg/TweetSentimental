import sys
import string
import csv
import re
import datetime

text_file = open("Output.txt", "a")
text_file.write(str(datetime.datetime.now()))
text_file.write('\n')
text_file.close()
for line in sys.stdin:
    keyword = 'dentist'
    csv_line = list(csv.reader(line , delimiter = ','))
    tweet = csv_line[10][0]
    for c in string.punctuation:
        tweet = tweet.replace(c, " ")
    tweet = re.sub('[\s]+', ' ', tweet)
    tweet = tweet.lower()

    print '%s\t%s' %(tweet, keyword)
