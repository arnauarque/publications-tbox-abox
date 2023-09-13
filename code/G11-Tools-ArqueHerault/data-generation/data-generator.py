import sys, os, shutil, re, csv, random, lorem, numpy
from parameters import *

# Setting random seed so that the results don't change between executions
random.seed(2912199)

# Returns an ISSN code
def issn():
    return '%d-%d' % (random.randint(1000,9000), random.randint(1000, 9000))

# Returns a random date between the delimiters
# Format: YYYY-MM-DD
def get_random_date(d0=1, d=31, m0=1, m=13, y0=0, y=2022):
    if d<=d0 or m<=m0 or y<=y0: 
        print('[ERROR] (get_random_date) The limit cannot be smaller or equal than the initial value!')
    d = max(d,31) # We don't allow day=31
    year = random.randint(y0,y)
    month = random.randint(m0,m)
    day = random.randint(d0,d)
    if month == 2: 
        day = max(day,28)
    date = '%d-' % year
    if month < 10: 
        date += '0%d-' % month
    else: 
        date += '%d-' % month
    if day < 10: 
        date += '0%d' % day
    else: 
        date += '%d' % day
    return date
    
# Class to generate unique indexes
class Idx:
    def __init__(self, start=0):
        self.idx = start-1
    def __iter__(self):
        return self
    def __next__(self):
        self.idx += 1
        return str(self.idx)
    def current(self):
        return self.idx

# Class to store all the data
class KnowledgeBaseStorage: 
    def __init__(self, datapath='./data/'):
        # Persons
        self.authors = []
        self.reviewers = []
        self.editors = []
        self.chairs = []
        # Papers
        self.posters = []
        self.demos = []
        self.shorts = []
        self.fulls = []
        # Conferences
        self.workshops = []
        self.symposiums = []
        self.expertGroups = []
        self.regulars = []
        # Others
        self.areas = []
        self.submissions = []
        self.journals = []
        self.reviews = []
        
        # Other variables
        self.idx = Idx()
        self.datapath = datapath
        self.countries = ['China', 'India', 'USA', 'Spain', 'France', 'Germany', 
                            'Iceland', 'Finland', 'Norway', 'Sweden', 
                            'United Kingdom', 'Poland', 'Ukraine', 'Russia', 
                            'Italy', 'Greece', 'Croatia']
        self.cities = ['London', 'Paris', 'New York', 'Moscow', 'Dubai', 'Tokyo', 
                        'Singapore', 'Los Angeles', 'Barcelona', 'Chicago']
        self.months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 
                        'Sep', 'Oct', 'Nov', 'Dec']
        self.number_of_submissions = -1
        self.reviews_per_submission = -1
        self.relations = {}
        
        # Initial actions
        shutil.rmtree(self.datapath)
        os.mkdir(self.datapath)
        
    # Returns a file and a writer for the file 'filename'
    def get_file_writer(self, filename):
        file = open(self.datapath+filename, mode='w+')
        writer = csv.writer(file, delimiter=';')
        return file, writer
    
    # Returns all the papers stored 
    def get_papers(self):
        return self.fulls + self.shorts + self.demos + self.posters
    
    # Returns all the conferences stored
    def get_conferences(self):
        return self.workshops + self.symposiums + self.expertGroups + self.regulars
    
    # Returns a random conference
    def get_random_conference(self):
        return random.choice(self.get_conferences())
        
    # Returns a summary of the individuals of the KBstore
    def individuals_summary(self):
        summ = '''[Summary of INDIVIDUALS] %d individuals generated:
    [Persons] (%d)
    |   %d authors
    |   %d reviewers
    |   [Managers] (%d)
    |   |   %d editors 
    |   |   %d chairs
    [Papers] (%d)
    |   %d posters 
    |   %d demo papers
    |   %d short papers
    |   %d full papers
    [Venues] (%d)
    |   [Conferences] (%d)
    |   |   %d workshops 
    |   |   %d symposiums
    |   |   %d expert groups
    |   |   %d regulars 
    |   %d journals 
    %d areas 
    %d submissions
    %d reviews
''' % (self.number_of_individuals(), self.number_of_persons(), len(self.authors), 
        len(self.reviewers), len(self.editors)+len(self.chairs), len(self.editors), 
        len(self.chairs), self.number_of_papers(),  len(self.posters), 
        len(self.demos), len(self.shorts), len(self.fulls), 
        self.number_of_conferences() + len(self.journals), self.number_of_conferences(), 
        len(self.workshops), len(self.symposiums), len(self.expertGroups), 
        len(self.regulars), len(self.journals), len(self.areas), len(self.submissions), 
        len(self.reviews)) 
        return summ
    
    # Returns a summary of the relations created by using the KBstore
    def relations_summary(self):
        summ = '''[Summary of RELATIONS] %d relations generated:
    %d assignedBy 
    %d handlesConference
    %d handlesJournal
    %d hasAuthor
    %d hasPaper
    %d hasReview
    %d hasReviewer
    %d paperRelatedTo
    %d submittedTo
    %d venueRelatedTo
''' % (sum(self.relations.values()), self.relations['assignedBy'], 
        self.relations['handlesConference'], self.relations['handlesJournal'], 
        self.relations['hasAuthor'], self.relations['hasPaper'], self.relations['hasReview'], 
        self.relations['hasReviewer'], self.relations['paperRelatedTo'], 
        self.relations['submittedTo'], self.relations['venueRelatedTo'])
        return summ
    
    # Pops and returns a tandom paper
    def pop_random_paper(self):
        paper = random.choice(self.get_papers())
        if paper in self.fulls: 
            self.fulls.remove(paper)
        elif paper in self.shorts: 
            self.shorts.remove(paper)
        elif paper in self.demos: 
            self.demos.remove(paper)
        elif paper in self.posters:
            self.posters.remove(paper)
        else:
            nt('[ERROR] (pop_random_paper) The random Paper obtained is not of any\n' + \
               'recognized type. You probably have forgotten to update this method when\n' + \
               'you added some new Paper subclass!')
            sys.exit(1)
        return paper
    
    # Returns the number of papers stored
    def number_of_papers(self):
        return len(self.posters) + len(self.demos) + len(self.shorts) + len(self.fulls)
        
    # Returns the number of persons stored
    def number_of_persons(self):
        return len(self.authors) + len(self.reviewers) + len(self.editors) + len(self.chairs)
        
    # Returns the number of conferences stored
    def number_of_conferences(self):
        return len(self.workshops) + len(self.symposiums) + len(self.expertGroups) + len(self.regulars)
        
    def number_of_reviews(self):
        return self.number_of_submissions * self.reviews_per_submission
        
    # Returns the total number of individuals 
    def number_of_individuals(self):
        n = self.number_of_papers() + self.number_of_persons() + self.number_of_conferences() + \
            len(self.journals) + len(self.areas) + len(self.submissions) + self.number_of_reviews()
        return n
        
    # Sets 'number_of_submissions' variable
    def set_number_of_submissions(self, n):
        if n > self.number_of_papers(): 
            print('[ERROR] (set_number_of_submissions) The number of submissions must be smaller than the number of papers available!')
            sys.exit(1)
        self.number_of_submissions = n
        self.relations['submittedTo'] = n
        self.relations['hasPaper'] = n
        
    # Sets 'reviews_per_submission' variables
    def set_reviews_per_submission(self, n):
        if n < 2:
            print('[ERROR] (set_reviews_per_submission) The minimum number of reviews per submission is 2!')
            sys.exit(1)
        self.reviews_per_submission = n
        nrels = n * self.number_of_submissions
        self.relations['hasReview'] = nrels
        self.relations['hasReviewer'] = nrels
        self.relations['assignedBy'] = nrels
        
    # Sets 'datapath'
    def set_datapath(self, path):
        if not os.path.isdir(path):
            os.mkdir(path)
        self.datapath = path
    
    # Adds a person ID in the 'subtype' specified
    def add_person(self, subtype):
        id = next(self.idx)
        if subtype == 'authors':
            self.authors.append(id)
        elif subtype == 'reviewers':
            self.reviewers.append(id)
        elif subtype == 'editors':
            self.editors.append(id)
        elif subtype == 'chairs':
            self.chairs.append(id)
        else:
            print('[ERROR] (add_persons) Subtype of person not recognized!')
            sys.exit(1)
    
    # Adds a conference ID in the 'subtype' specified
    def add_conference(self, subtype):
        id = next(self.idx)
        if subtype == 'workshops':
            self.workshops.append(id)
        elif subtype == 'symposiums':
            self.symposiums.append(id)
        elif subtype == 'expertGroups':
            self.expertGroups.append(id)
        elif subtype == 'regulars':
            self.regulars.append(id)
        else:
            print('[ERROR] (add_conference) Subtype of conference not recognized!')
            sys.exit(1)
    
    # Adds a paper ID in the 'subtype' specified
    def add_paper(self, subtype):
        id = next(self.idx)
        if subtype == 'posters':
            self.posters.append(id)
        elif subtype == 'demos':
            self.demos.append(id)
        elif subtype == 'shorts':
            self.shorts.append(id)
        elif subtype == 'fulls':
            self.fulls.append(id)
        else:
            print('[ERROR] (add_paper) Subtype of paper not recognized!')
            sys.exit(1)
    
    # Stores all the person ID's
    def add_persons(self, subtype, names):
        file, writer = self.get_file_writer(subtype + '.csv')
        # Header
        header = ['id', 'author', 'birthYear', 'coutry']
        if subtype == 'authors': 
            header.append('hIndex')
        elif subtype == 'reviewers':
            header.append('averageScore')
        writer.writerow(header)
        # Individuals
        for name in names:
            self.add_person(subtype)
            country = random.choice(self.countries)
            row = [self.idx.current(), name, country]
            if subtype == 'authors':
                row.append(random.randint(1,500))
            elif subtype == 'reviewers':
                row.append(random.randint(1,11))
            writer.writerow(row)
        file.close()
    
    # Stores all the area ID's
    def add_areas(self, names):
        file, writer = self.get_file_writer('areas.csv')
        writer.writerow(['id', 'field'])
        for name in names: 
            self.areas.append(next(self.idx))
            writer.writerow([self.idx.current(), name])
        file.close()
    
    # Stores all the paper ID's
    def add_papers(self, subtype, titles):
        file, writer = self.get_file_writer(subtype + '.csv')
        header = ['id', 'title', 'numberOfPages', 'issn']
        if subtype == 'posters': 
            header += ['width', 'height']
        writer.writerow(header)
        for title in titles: 
            self.add_paper(subtype)
            numberOfPages = random.randint(50,200)
            row = [self.idx.current(), title, numberOfPages, issn()]
            if subtype == 'posters':
                row += [random.randint(50,100), random.randint(50,100)]
            writer.writerow(row)
        file.close()
    
    # Stores all the conference ID's
    def add_conferences(self, subtype, headings):
        file, writer = self.get_file_writer(subtype + '.csv')
        writer.writerow(['id', 'heading', 'city', 'month', 'year'])
        for heading in headings:
            self.add_conference(subtype)
            city = random.choice(self.cities)
            month = random.choice(self.months)
            year = random.randint(1980, 2022)
            writer.writerow([self.idx.current(), heading, city, month, year])
        file.close()
    
    # Stores all the journal ID's
    def add_journals(self, headings):
        file, writer = self.get_file_writer('journals.csv')
        writer.writerow(['id', 'heading', 'edition', 'hasOnlineVersion'])
        for heading in headings:
            self.journals.append(next(self.idx))
            edition = random.randint(1,50)
            online = random.choice(['Yes', 'No'])
            writer.writerow([self.idx.current(), heading, edition, online])
        file.close()
    
    # Stores all the submission and review ID'S
    # PRE: self.number_of_submissions and self.reviews_per_submission must be set
    def add_submissions_and_reviews(self, number_of_submissions, reviews_per_submission):
        # Setting necessary attributes
        self.set_number_of_submissions(number_of_submissions)
        self.set_reviews_per_submission(reviews_per_submission)
        # Opening files
        f_sub, w_sub = self.get_file_writer('submissions.csv')
        f_rev, w_rev = self.get_file_writer('reviews.csv')
        # Writing headers
        w_sub.writerow(['id', 'date', 'comment', 'published'])
        w_rev.writerow(['id', 'description', 'decision'])
        # Creating submissions and reviews
        rev = 0
        for _ in range(self.number_of_submissions):
            self.submissions.append(next(self.idx))
            date = get_random_date(y0=1970, y=1980)
            comment = lorem.paragraph()
            row_sub = [self.idx.current(), date, comment]
            accepted = 0
            for i in range(rev, rev+self.reviews_per_submission):
                if i > self.number_of_submissions*self.reviews_per_submission: 
                    print('(add_submissions_and_reviews) Some error has occurred...\n')
                    sys.exit(1)
                self.reviews.append(next(self.idx))
                descr = lorem.paragraph()
                decision = random.choice(['Accepted', 'Rejected'])
                if decision == 'Accepted':
                    accepted += 1
                w_rev.writerow([self.idx.current(), descr, decision])
            rev += self.reviews_per_submission
            published = 'Yes'
            if accepted < self.reviews_per_submission/2:
                published = 'No'
            row_sub.append(published)
            w_sub.writerow(row_sub)
        f_sub.close()
        f_rev.close()
    
    # Stores all the submission ID's
    def add_submissions(self):
        file, writer = self.get_file_writer('submissions.csv')
        writer.writerow(['id', 'date', 'comment'])
        for _ in range(self.number_of_submissions):
            self.submissions.append(next(self.idx))
            date = get_random_date(y0=1970, y=1980)
            comment = lorem.paragraph()
            writer.writerow([self.idx.current(), date, comment])
        file.close()
    
    # Stores all the review ID's
    def add_reviews(self):
        nreviews = self.number_of_submissions * self.reviews_per_submission
        file, writer = self.get_file_writer('reviews.csv')
        writer.writerow(['id', 'description', 'decision'])
        for _ in range(nreviews):
            self.reviews.append(next(self.idx))
            descr = lorem.paragraph()
            decision = random.choice(['Accepted', 'Rejected'])
            writer.writerow([self.idx.current(), descr, decision])
        file.close()
    
    # Stores the hasAuthor and paperRelatedTo relations
    def create_paper_relations(self):
        # hasAuthor
        papers = self.get_papers()
        file, writer = self.get_file_writer('hasAuthor.csv')
        rows = [['paperID', 'authorID']] + \
                [ [paper, random.choice(self.authors)] for paper in papers ]
        self.relations['hasAuthor'] = len(rows)-1
        writer.writerows(rows)
        file.close()
        # paperRelatedTo
        file, writer = self.get_file_writer('paperRelatedTo.csv')
        rows = [['paperID', 'areaID']] + \
                [ [paper, random.choice(self.areas)] for paper in papers ]
        self.relations['paperRelatedTo'] = len(rows)-1
        writer.writerows(rows)
        file.close()
    
    # Stores the venueRelatedTo relations
    def create_venue_relations(self):
        # venueRelatedTo
        venues = self.get_conferences() + self.journals
        file, writer = self.get_file_writer('venueRelatedTo.csv')
        rows = [['venueID', 'areaID']] + \
                [ [venue, random.choice(self.areas)] for venue in venues ]
        self.relations['venueRelatedTo'] = len(rows)-1
        writer.writerows(rows)
        file.close()
    
    # Stores the handlesJournal relations
    def create_editor_relations(self):
        # handlesJournal
        file, writer = self.get_file_writer('handlesJournal.csv')
        rows = [['editorID', 'journalID']] + \
                [ [random.choice(self.editors), journal] for journal in self.journals ]
        self.relations['handlesJournal'] = len(rows)-1
        writer.writerows(rows)
        file.close()
    
    # Stores the handlesConference relations
    def create_chair_relations(self):
        # handlesConference
        file, writer = self.get_file_writer('handlesConference.csv')
        rows = [['editorID', 'conferenceID']] + \
                [ [random.choice(self.chairs), conference] for conference in self.get_conferences() ]
        self.relations['handlesConference'] = len(rows)-1
        writer.writerows(rows)
        file.close()
    
    # Stores the submittedTo, hasPaper, hasReview, hasReviewer and assignedBy relations
    def create_submission_review_relations(self):
        # Opening files and creating writers
        f_submittedTo, w_submittedTo = self.get_file_writer('submittedTo.csv')
        f_hasPaper, w_hasPaper = self.get_file_writer('hasPaper.csv')
        f_hasReview, w_hasReview = self.get_file_writer('hasReview.csv')
        f_hasReviewer, w_hasReviewer = self.get_file_writer('hasReviewer.csv')
        f_assignedBy, w_assignedBy = self.get_file_writer('assignedBy.csv')
        # Headers
        w_submittedTo.writerow(['submissionID', 'venueID'])
        w_hasPaper.writerow(['submissionID', 'paperID'])
        w_hasReview.writerow(['submissionID', 'reviewID'])
        w_hasReviewer.writerow(['reviewID', 'reviewerID'])
        w_assignedBy.writerow(['reviewID', 'managerID'])
        # Creating the submissions' relations
        for submission in self.submissions: 
            # Getting the paper to submit
            paper = self.pop_random_paper()
            # Creating the 'hasPaper' relation
            w_hasPaper.writerow([submission, paper])
            # We decide if the paper is submitted to a Journal/Conference
            journal = bool(random.getrandbits(1))
            if paper in self.posters:
                journal = True
            # Creating the 'submittedTo' relation
            if journal: 
                venue = random.choice(self.journals)
            else: 
                venue = self.get_random_conference()
            w_submittedTo.writerow([submission, venue])
            # Setting the reviews
            reviewersSoFar = []
            for _ in range(self.reviews_per_submission):
                review = self.reviews.pop()
                # Creating the 'hasReview' relation
                w_hasReview.writerow([submission, review])
                # Selecting the reviewer
                reviewer = random.choice(self.reviewers)
                while reviewer in reviewersSoFar: 
                    reviewer = random.choice(self.reviewers)
                # Creating the 'hasReviewer' relation
                w_hasReviewer.writerow([review, reviewer])
                # Selecting the manager
                if journal: 
                    manager = random.choice(self.editors)
                else: 
                    manager = random.choice(self.chairs)
                # Creating the 'assignedBy' relation
                w_assignedBy.writerow([review, manager])
        # Closing files
        f_submittedTo.close()
        f_hasPaper.close()
        f_hasReview.close()
        f_hasReviewer.close()
        f_assignedBy.close()
    
## -----------------------------------------------------------------------------
## -- Data generation program
## -----------------------------------------------------------------------------

msg = '''\n------------------------------------------------------
-- SDM Project 3
-- Authors: Arnau Arqué and Nora Herault
------------------------------------------------------
-- Data generation program
------------------------------------------------------
'''
print(msg)
    
# Creation of the data storage
data = KnowledgeBaseStorage()

## -----------------------------------------------------------------------------
## -- Creation of INSTANCES (and its attributes)
## -----------------------------------------------------------------------------

print('[Generating instances...]')
data.set_datapath('./data/individuals/')

## -----------------------------------------------------------------------------
## -- The following data does not need any parametrization.
## -- You can add/remove instances of any kind in the 'arguments.py' file.
## -----------------------------------------------------------------------------

# Persons
data.add_persons('authors', authors)
data.add_persons('reviewers', reviewers)
data.add_persons('editors', editors)
data.add_persons('chairs', chairs)

# Areas
data.add_areas(areas)

# Papers
data.add_papers('posters', posters)
data.add_papers('demos', demos)
data.add_papers('shorts', shorts)
data.add_papers('fulls', fulls)

# Conferences 
data.add_conferences('workshops', workshops)
data.add_conferences('symposiums', symposiums)
data.add_conferences('expertGroups', expertGroups)
data.add_conferences('regulars', regulars)

# Journals 
data.add_journals(journals)

## -----------------------------------------------------------------------------
## -- From now on you can parametrize the behaviour of the submissions and 
## -- reviews generation by modifying the values of 'number_of_submissions' and 
## -- 'reviews_per_submission'.
## -----------------------------------------------------------------------------

# Submissions and reviews
# We will assume 3/4 parts of the papers in the DB are submitted
# For simplicity, we will assume that each submission has the same number of reviews
nsubs = round(data.number_of_papers()*3/4)
data.add_submissions_and_reviews(nsubs, 3)

# We collect now the summary because when creating relations, we will remove 
# some instances stored
indSummary = data.individuals_summary()

## -----------------------------------------------------------------------------
## -- Creation of RELATIONS (between instances)
## -----------------------------------------------------------------------------

print('[Generating relations...]')
data.set_datapath('./data/relations/')

# We first create the relations hasAuthor, paperRelatedTo, venueRealtedTo,
# handlesJournal, handlesConference
data.create_paper_relations()   # hasAuthor, paperRelatedTo
data.create_venue_relations()   # venueRelatedTo
data.create_editor_relations()  # handlesJournal
data.create_chair_relations()   # handlesConference

# Now, we create the relations submittedTo, hasPaper, hasReview, hasReviewer, 
# assignedBy, which require to fulfill some constraints
data.create_submission_review_relations()

## -----------------------------------------------------------------------------
## -- Succeeded. Printing summary...
## -----------------------------------------------------------------------------

relSummary = data.relations_summary()

print('[Succeed]\n')
print(indSummary)
print(relSummary)
