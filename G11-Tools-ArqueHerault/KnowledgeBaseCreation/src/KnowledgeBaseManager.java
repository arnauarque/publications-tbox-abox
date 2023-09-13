import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KnowledgeBaseManager {

    private OntModel tbox;
    private OntModel abox;

    private final String source;                // Main source
    private final String classSource;           // Class source
    private final String objectPropertySource;  // ObjectProperty source
    private final String datatypeSource;        // DatatypeProperty source
    private final String individualSource;      // Individual source

    // -----------------------------------------------------------------------------
    // -- Main method
    // -----------------------------------------------------------------------------
    public static void main(String[] args) {
        // Initial message
        System.out.println("\n-------------------------------------------------");
        System.out.println("-- SDM Project 3");
        System.out.println("-- Authors: Arnau Arqué and Nora Herault");
        System.out.println("-------------------------------------------------");
        System.out.println("-- TBOX and ABOX creation program");
        System.out.println("-------------------------------------------------\n");

        // Arguments parsing
        KnowledgeBaseManager.parseArguments(args);
        boolean argABOX = Arrays.asList(args).contains("-abox");

        // Creation of the KBManager
        KnowledgeBaseManager mg = new KnowledgeBaseManager("http://sdm/project/3/source/");

        // TBOX creation
        mg.createTBOX();
        // ABOX creation
        if (argABOX) mg.createABOX();

        // Storing the TBOX and ABOX
        mg.saveTBOX("../../G11-B1-ArqueHerault.owl");
        if (argABOX) mg.saveABOX("../../G11-B2-ArqueHerault.owl");
    }

    // -----------------------------------------------------------------------------
    // -- Creator method.
    // -----------------------------------------------------------------------------
    KnowledgeBaseManager(String URISource) {
        source = URISource;
        classSource = source + "class#";
        objectPropertySource = source + "objectProperty#";
        datatypeSource = source + "datatypeProperty#";
        individualSource = source + "individual#";
    }

    // -----------------------------------------------------------------------------
    // -- Method for parsing the arguments allowed in main method.
    // -----------------------------------------------------------------------------
    public static void parseArguments(String[] args) {
        String errorMsg = null;
        boolean argTBOX, argABOX;
        argTBOX = argABOX = false;
        for (String arg: args) {
            if (Objects.equals(arg, "-tbox")) argTBOX = true;
            else if (Objects.equals(arg, "-abox")) argABOX = true;
            else errorMsg = "[ERROR] Wrong argument provided ('" + arg + "'). Only '-tbox' and '-abox' are allowed.";
        }
        if (argABOX && !argTBOX) errorMsg = "[ERROR] If '-abox' argument is provided, '-tbox' argument must be provided too.";
        if (errorMsg != null) {
            System.out.println(errorMsg);
            System.exit(1);
        }
    }

    // -----------------------------------------------------------------------------
    // -- This method creates an OntModel which is meant to be our TBOX.
    // -----------------------------------------------------------------------------
    public void createTBOX() {
        System.out.print("[Creating the TBOX...");

        // -----------------------------------------------------------------------------
        // -- Model declaration
        // -----------------------------------------------------------------------------
        tbox = ModelFactory.createOntologyModel();

        // -----------------------------------------------------------------------------
        // -- Classes
        // -----------------------------------------------------------------------------

        String ns = classSource; // Classes namespace

        // Person
        OntClass author = tbox.createClass(ns + "Author");
        OntClass reviewer = tbox.createClass(ns + "Reviewer");
        OntClass editor = tbox.createClass(ns + "Editor");
        OntClass chair = tbox.createClass(ns + "Chair");
        OntClass manager = tbox.createClass(ns + "Manager");
        manager.addSubClass(editor);
        manager.addSubClass(chair);
        OntClass person = tbox.createClass(ns + "Person");
        person.addSubClass(author);
        person.addSubClass(reviewer);
        person.addSubClass(manager);

        // Conference
        OntClass workshop = tbox.createClass(ns + "Workshop");
        OntClass symposium = tbox.createClass(ns + "Symposium");
        OntClass expertGroup = tbox.createClass(ns + "ExpertGroup");
        OntClass regular = tbox.createClass(ns + "Regular");
        OntClass conference = tbox.createClass(ns + "Conference");
        conference.addSubClass(workshop);
        conference.addSubClass(symposium);
        conference.addSubClass(expertGroup);
        conference.addSubClass(regular);

        // Journal
        OntClass journal = tbox.createClass(ns + "Journal");

        // Venue
        OntClass venue = tbox.createClass(ns + "Venue");
        venue.addSubClass(conference);
        venue.addSubClass(journal);

        conference.addSuperClass(venue);
        journal.addSuperClass(venue);

        // Paper
        OntClass fullPaper = tbox.createClass(ns + "FullPaper");
        OntClass shortPaper = tbox.createClass(ns + "ShortPaper");
        OntClass demoPaper = tbox.createClass(ns + "DemoPaper");
        OntClass poster = tbox.createClass(ns + "Poster");
        OntClass paper = tbox.createClass(ns + "Paper");
        paper.addSubClass(fullPaper);
        paper.addSubClass(shortPaper);
        paper.addSubClass(demoPaper);
        paper.addSubClass(poster);

        // Area
        OntClass area = tbox.createClass(ns + "Area");

        // Submission
        OntClass submission = tbox.createClass(ns + "Submission");

        // Review
        OntClass review = tbox.createClass(ns + "Review");

        // -----------------------------------------------------------------------------
        // -- Object properties
        // -----------------------------------------------------------------------------

        ns = objectPropertySource; // ObjectProperties namespace

        // hasAuthor
        ObjectProperty hasAuthor = tbox.createObjectProperty(ns + "hasAuthor");
        hasAuthor.addDomain(paper);
        hasAuthor.addRange(author);

        // paperRelatedTo
        ObjectProperty paperRelatedTo = tbox.createObjectProperty(ns + "paperRelatedTo");
        paperRelatedTo.addDomain(paper);
        paperRelatedTo.addRange(area);

        // venueRelatedTo
        ObjectProperty venueRelatedTo = tbox.createObjectProperty(ns + "venueRelatedTo");
        venueRelatedTo.addDomain(venue);
        venueRelatedTo.addRange(area);

        // hasPaper
        ObjectProperty hasPaper = tbox.createObjectProperty(ns + "hasPaper");
        hasPaper.addDomain(submission);
        hasPaper.addRange(paper);

        // hasReview
        ObjectProperty hasReview = tbox.createObjectProperty(ns + "hasReview");
        hasReview.addDomain(submission);
        hasReview.addRange(review);

        // submittedTo
        ObjectProperty submittedTo = tbox.createObjectProperty(ns + "submittedTo");
        submittedTo.addDomain(submission);
        submittedTo.addRange(venue);

        // hasReviewer
        ObjectProperty hasReviewer = tbox.createObjectProperty(ns + "hasReviewer");
        hasReviewer.addDomain(review);
        hasReviewer.addRange(reviewer);

        // assignedBy
        ObjectProperty assignedBy = tbox.createObjectProperty(ns + "assignedBy");
        assignedBy.addDomain(review);
        assignedBy.addRange(manager);

        // handlesConference
        ObjectProperty handlesConference = tbox.createObjectProperty(ns + "handlesConference");
        handlesConference.addDomain(chair);
        handlesConference.addRange(conference);

        // handlesJournal
        ObjectProperty handlesJournal = tbox.createObjectProperty(ns + "handlesJournal");
        handlesJournal.addDomain(editor);
        handlesJournal.addRange(journal);

        // -----------------------------------------------------------------------------
        // -- Data properties
        // -----------------------------------------------------------------------------

        ns = datatypeSource; // DatatypeProperties namespace

        DatatypeProperty name = tbox.createDatatypeProperty(ns + "name");
        name.addDomain(person);
        name.addRange(RDFS.Literal); // String

        DatatypeProperty country = tbox.createDatatypeProperty(ns + "country");
        country.addDomain(person);
        country.addRange(RDFS.Literal); // String

        DatatypeProperty hindex = tbox.createDatatypeProperty(ns + "hIndex");
        hindex.addDomain(author);
        hindex.addRange(XSD.integer);

        DatatypeProperty averageScore = tbox.createDatatypeProperty(ns + "averageScore");
        averageScore.addDomain(reviewer);
        averageScore.addRange(XSD.integer);

        DatatypeProperty field = tbox.createDatatypeProperty(ns + "field");
        field.addDomain(area);
        field.addRange(RDFS.Literal); // String

        DatatypeProperty description = tbox.createDatatypeProperty(ns + "description");
        description.addDomain(review);
        description.addRange(RDFS.Literal); // String

        DatatypeProperty decision = tbox.createDatatypeProperty(ns + "decision");
        decision.addDomain(review);
        decision.addRange(RDFS.Literal); // String (Accepted/Rejected)

        DatatypeProperty heading = tbox.createDatatypeProperty(ns + "heading");
        heading.addDomain(venue);
        heading.addRange(RDFS.Literal); // String

        DatatypeProperty title = tbox.createDatatypeProperty(ns + "title");
        title.addDomain(paper);
        title.addRange(RDFS.Literal); // String

        DatatypeProperty numberOfPages = tbox.createDatatypeProperty(ns + "numberOfPages");
        numberOfPages.addDomain(paper);
        numberOfPages.addRange(XSD.integer); // Int

        DatatypeProperty issn = tbox.createDatatypeProperty(ns + "issn");
        issn.addDomain(paper);
        issn.addRange(RDFS.Literal); // String (XXXX-XXXX)

        DatatypeProperty width = tbox.createDatatypeProperty(ns + "width");
        width.addDomain(poster);
        width.addRange(XSD.integer); // Int

        DatatypeProperty height = tbox.createDatatypeProperty(ns + "height");
        height.addDomain(poster);
        height.addRange(XSD.integer); // Int

        DatatypeProperty date = tbox.createDatatypeProperty(ns + "date");
        date.addDomain(submission);
        date.addRange(XSD.date); // Date (YYYY-MM-DD)

        DatatypeProperty comment = tbox.createDatatypeProperty(ns + "comment");
        comment.addDomain(submission);
        comment.addRange(RDFS.Literal); // String

        DatatypeProperty published = tbox.createDatatypeProperty(ns + "published");
        published.addDomain(submission);
        published.addRange(RDFS.Literal); // String

        DatatypeProperty city = tbox.createDatatypeProperty(ns + "city");
        city.addDomain(conference);
        city.addRange(RDFS.Literal); // String

        DatatypeProperty month = tbox.createDatatypeProperty(ns + "month");
        month.addDomain(conference);
        month.addRange(RDFS.Literal); // String (Jan, Feb, Mar, ...)

        DatatypeProperty year = tbox.createDatatypeProperty(ns + "year");
        year.addDomain(conference);
        year.addRange(XSD.integer); // Int

        DatatypeProperty edition = tbox.createDatatypeProperty(ns + "edition");
        edition.addDomain(journal);
        edition.addRange(RDFS.Literal); // String

        DatatypeProperty hasOnlineVersion = tbox.createDatatypeProperty(ns + "hasOnlineVersion");
        hasOnlineVersion.addDomain(journal);
        hasOnlineVersion.addRange(RDFS.Literal); // String

        System.out.println("Success!]");
    }

    // -----------------------------------------------------------------------------
    // -- This method creates an OntModel which is meant to be our ABOX.
    // -----------------------------------------------------------------------------
    public void createABOX() {
        System.out.print("[Creating the ABOX...");

        // Declaration of the ABOX
        abox = ModelFactory.createOntologyModel();

        // Path definitions
        String path = "../data-generation/data/";
        String ipath = path + "individuals/";
        String rpath = path + "relations/";

        // -----------------------------------------------------------------------------
        // -- Individuals
        // -----------------------------------------------------------------------------

        // [ PERSONS ]
        // Authors
        List<List<String>> rows = getContents(ipath + "authors.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Author"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "name"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "country"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "hIndex"), row.get(3));
        }

        // Reviewers
        rows = getContents(ipath + "reviewers.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Reviewer"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "name"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "country"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "averageScore"), row.get(3));
        }

        // Editors
        rows = getContents(ipath + "editors.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Editor"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "name"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "country"), row.get(2));
        }

        // Chairs
        rows = getContents(ipath + "chairs.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Chair"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "name"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "country"), row.get(2));
        }

        // [ PAPERS ]
        // FullPapers
        rows = getContents(ipath + "fulls.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "FullPaper"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "title"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "numberOfPages"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "issn"), row.get(3));
        }

        // ShortPapers
        rows = getContents(ipath + "shorts.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "ShortPaper"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "title"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "numberOfPages"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "issn"), row.get(3));
        }

        // DemoPapers
        rows = getContents(ipath + "demos.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "DemoPaper"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "title"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "numberOfPages"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "issn"), row.get(3));
        }

        // Posters
        rows = getContents(ipath + "posters.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Poster"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "title"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "numberOfPages"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "issn"), row.get(3));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "width"), row.get(4));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "height"), row.get(5));
        }

        // Submissions
        rows = getContents(ipath + "submissions.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Submission"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "comment"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "date"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "published"), row.get(3));
        }

        // [ CONFERENCES ]
        // Workshops
        rows = getContents(ipath + "workshops.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Workshop"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "heading"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "city"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "month"), row.get(3));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "year"), row.get(4));
        }

        // Symposiums
        rows = getContents(ipath + "symposiums.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Symposium"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "heading"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "city"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "month"), row.get(3));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "year"), row.get(4));
        }

        // ExpertGroups
        rows = getContents(ipath + "expertGroups.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "ExpertGroup"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "heading"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "city"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "month"), row.get(3));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "year"), row.get(4));
        }

        // Regulars
        rows = getContents(ipath + "regulars.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Regular"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "heading"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "city"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "month"), row.get(3));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "year"), row.get(4));
        }

        // Journals
        rows = getContents(ipath + "journals.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Journal"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "heading"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "edition"), row.get(2));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "hasOnlineVersion"), row.get(3));
        }

        // Reviews
        rows = getContents(ipath + "reviews.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Review"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "description"), row.get(1));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "decision"), row.get(2));
        }

        // Areas
        rows = getContents(ipath + "areas.csv");
        for (List<String> row: rows) {
            Individual ind = abox.createIndividual(individualSource + row.get(0), tbox.getOntClass(classSource + "Area"));
            ind.addProperty(tbox.getDatatypeProperty(datatypeSource + "field"), row.get(1));
        }

        // -----------------------------------------------------------------------------
        // -- Relations
        // -----------------------------------------------------------------------------

        // submittedTo
        createRelations(rpath + "submittedTo.csv", "submittedTo");
        // hasPaper
        createRelations(rpath + "hasPaper.csv", "hasPaper");
        // hasReview
        createRelations(rpath + "hasReview.csv", "hasReview");
        // hasReviewer
        createRelations(rpath + "hasReviewer.csv", "hasReviewer");
        // assignedBy
        createRelations(rpath + "assignedBy.csv", "assignedBy");
        // hasAuthor
        createRelations(rpath + "hasAuthor.csv", "hasAuthor");
        // paperRelatedTo
        createRelations(rpath + "paperRelatedTo.csv", "paperRelatedTo");
        // venueRelatedTo
        createRelations(rpath + "venueRelatedTo.csv", "venueRelatedTo");
        // handlesJournal
        createRelations(rpath + "handlesJournal.csv", "handlesJournal");
        // handlesConference
        createRelations(rpath + "handlesConference.csv", "handlesConference");

        System.out.println("Success!]");
    }

    // -----------------------------------------------------------------------------
    // -- This method adds a relation in the ABOX from a set of relations stored
    // -- at file 'filename'. More precisely, it generates the ObjectProperty
    // -- 'relation'.
    // -----------------------------------------------------------------------------
    private void createRelations(String filename, String relation) {
        Individual subject, object;
        ObjectProperty predicate = tbox.getObjectProperty(objectPropertySource + relation);
        List<List<String>> rows = getContents(filename);
        for (List<String> row: rows) {
            subject = abox.getIndividual(individualSource + row.get(0));
            object = abox.getIndividual(individualSource + row.get(1));
            abox.add(subject, predicate, object);
        }
    }

    // -----------------------------------------------------------------------------
    // -- This method returns a List of Lists containing each row of the file
    // -- 'filename'.
    // -----------------------------------------------------------------------------
    private List<List<String>> getContents(String filename) {
        List<List<String>> contents = new ArrayList<>();
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader br = new BufferedReader(reader);

            String line = br.readLine(); // Header
            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    List<String> values = Arrays.asList(line.split(";"));
                    contents.add(values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    // -----------------------------------------------------------------------------
    // -- This method stores the TBOX at file 'tbox_filename'.
    // -----------------------------------------------------------------------------
    public void saveTBOX(String tbox_filename) {
        try {
            FileOutputStream fos = new FileOutputStream(tbox_filename);
            tbox.write(fos);
            System.out.println("[TBOX saved at '" + tbox_filename + "']");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------------------
    // -- This method stores the ABOX at file 'abox_filename'.
    // -----------------------------------------------------------------------------
    public void saveABOX(String abox_filename) {
        try {
            FileOutputStream fos = new FileOutputStream(abox_filename);
            abox.write(fos);
            System.out.println("[ABOX saved at '" + abox_filename + "']");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
