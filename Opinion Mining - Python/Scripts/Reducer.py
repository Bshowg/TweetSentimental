import sys
import datetime

counts = {}

for line in sys.stdin:
    classification=line.strip('\n').strip('\t')

    counts[classification] = counts.get(classification, 1) + 1

print counts
text_file = open("Output.txt", "a+")
text_file.write(str(datetime.datetime.now()))
text_file.write('\n')
text_file.write(str(counts))
text_file.write('\n')
text_file.close()

