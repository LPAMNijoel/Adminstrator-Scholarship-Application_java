/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scholarshipform;
import java.util.*;
import java.io.*;
/**
 *
 * @author Alex, Nijoel, Micheal
 */
public class ScholarshipForm {

    private Database myDB;
    private Scanner s;
    private Student currentStudent;
    private Scholarship currentScholarship;
    private boolean isAdmin;
    
    public ScholarshipForm(){
        myDB = new Database();
        s = new Scanner(System.in);
        currentStudent = null;
        currentScholarship = null;
        isAdmin = false;
    }
    
    public void debugPrime(){ //DEBUG: Inserts hardcoded debug data
        //TO BE REPLACED BY FILE READER METHODS IN FINAL RELEASE
        //String fn, String ln, String maj, float g, int hr
        //String n, String maj, float mg, int mhr, int ma, float mny
        
        /*
        myDB.addStudent(new Student("Joel","Fishman","Business",4.0f,30));
        myDB.addStudent(new Student("Bob","Slacker","Accounting",2.0f,90));
        myDB.addStudent(new Student("Pete","Slacker","Business",2.0f,90));
        myDB.addStudent(new Student("George","Trump","Engineering",4.0f,100));
        myDB.addStudent(new Student("Gene","Tamer","English",3.0f,60));
        //String n, String maj, float mg, int mhr, int ma, float mny
        myDB.addScholarship(new Scholarship("Turbot Engineering Scholarship","Engineering",2.5f,0,3,500f));
        myDB.addScholarship(new Scholarship("Egalitarian Award","",0f,0,3,100f));
        myDB.addScholarship(new Scholarship("Bodensteiner Award for Excellence","",3.75f,0,3,4000f));
        myDB.addScholarship(new Scholarship("Startup Grant","Business",3.0f,100,3,10000f));
        */
    }
    
    public void loadFile(String filename)
    {
        Database newDB = null;
        try
            (FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);)
        {
            newDB = (Database)ois.readObject();
        }
        catch(Exception e){
            e.printStackTrace(); //comment out in production
            System.out.println("Error: Could not load file.");
        }
        if(newDB != null){
            myDB = newDB;
        }
    }
    
    public void saveFile(String filename){
        try(FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);){
            oos.writeObject(myDB);
        }catch(Exception e){
            e.printStackTrace(); //comment out in production
            System.out.println("Error: Could not save file.");
        }
        
    }
    
    public void consoleStart(){
        
        String t;
        System.out.println("Load database?");
        if(consoleYesNo()){
            loadFile("persist.dat");
        }else{
            debugPrime();
        }
        
        
        System.out.println("Type [exit] at any time to quit.");
        while(true){
            boolean done = false;
            while(!done){
                System.out.println("\nAre you a [student] or an [admin]?");
                t = s.next();
                if(t.equalsIgnoreCase("admin")){
                    isAdmin = true;
                    done = true;
                }else if(t.equalsIgnoreCase("student")){
                    isAdmin = false;
                    done = true;
                }else if(t.equalsIgnoreCase("exit")){
                    System.out.println("Save changes?");
                    if(consoleYesNo()){
                        saveFile("persist.dat");
                    }
                    return;
                }
            }
            if(isAdmin){
                consoleAdmin();
            }else{
                consoleStudent();
            }
            System.out.println("Interaction finished. Enter [EXIT] to terminate program.");
        }
        
    }
    
    public String consoleGetLine(){
        String out = "";
        while(out.length() < 1){
            out = s.nextLine();
        }
        return out;
    }
    
    public void consoleStudent(){
        String sur = "";
        while(sur.length() <= 0){
            System.out.println("What is your last name?");
            sur = consoleGetLine();
        }
        Student[] possibleStudents = myDB.searchStudents(sur);
        if(possibleStudents.length == 1){ //One match, verify.
            System.out.println("Are you " + possibleStudents[0].getName(false)+"?");
            boolean truth = consoleYesNo();
            if(truth){
                currentStudent = possibleStudents[0];
            }else{
                consoleRegisterStudent(sur);
            }
        }else if(possibleStudents.length > 1){ //multiple matches, select.
            System.out.println("Select a student from the following list");
            for(int i=0;i<possibleStudents.length;i++){
                System.out.println((i+1)+": "+possibleStudents[i].getName(true));
            }
            System.out.println((possibleStudents.length+1)+": None of these");
            int c = consoleInt(1,possibleStudents.length+1);
            c--;
            if(c < possibleStudents.length){
                currentStudent = possibleStudents[c];
            }else{
                consoleRegisterStudent(sur);
            }
        }else{ //No matches, 
            consoleRegisterStudent(sur);
        }
        //Current student is now this student, who is registered. Otherwise, fail.
        if(currentStudent == null)
            return;
        boolean firstRun = true;
        
        while(true){
            Scholarship[] qualified = myDB.getQualifiers(currentStudent);
            if(qualified.length == 0){
                if(firstRun){
                    System.out.println("Sorry. You do not qualify for any of the scholarships offered.");
                }else{
                    System.out.println("Sorry, there are no more scholarships for which you qualify.");
                }
                return;
            }
            firstRun = false;
            System.out.println("Available Scholarships:");
            for(int i=0;i<qualified.length;i++){
                System.out.println((i+1)+": "+qualified[i].getName()+" ($"+qualified[i].getAwardMoney()+")");
            }
            System.out.println((qualified.length+1)+": [EXIT]");
            int c = consoleInt(1,qualified.length+1);
            c--;
            if(c < qualified.length){
                myDB.awardScholarship(currentStudent, qualified[c]);
            }else{
                return;
            }
        }
    }
    
    public void consoleRegisterStudent(String sur){ //Initial search retains surname.
        
        //Sanity check before we go asking questions...
        System.out.println("To continue, you must enter your student information. Do you wish to do that now?");
        if(!consoleYesNo())
            return;
        
        System.out.println("What is your first name?");
        String fore = consoleGetLine();
        System.out.println("What is your GPA?");
        float grade = s.nextFloat();
        String[] majors = myDB.getMajors();
        String myMajor;
        if(majors.length > 0){
            System.out.println("Known Majors:");
            for(int i=0;i<majors.length;i++){
                System.out.println((i+1)+": "+majors[i]);
            }
            System.out.println((majors.length+1)+": Other / Not Listed");
            int c = consoleInt(1,majors.length+1);
            c--;
            if(c < majors.length){
                myMajor = majors[c];
            }else{
                System.out.println("Enter your major:");
                myMajor = consoleGetLine();
            }
        }else{
            System.out.println("Enter your major:");
            myMajor = consoleGetLine();
        }
        System.out.println("How many hours have you taken?");
        int hours = s.nextInt();
        Student newGuy = new Student(fore,sur,myMajor,grade,hours);
        myDB.addStudent(newGuy);
        currentStudent = newGuy;
    }
    
    public void consoleAdmin(){
        while(true){
            System.out.println("\nAdmin Menu:");
            System.out.println("1: View Student");
            System.out.println("2: View Scholarship");
            System.out.println("3: Add New Scholarship");
            System.out.println("4: Exit");
            int c = consoleInt(1,4);
            if(c == 4)
                return;
            if(c == 1){
                consoleSearch(true);
                consoleDetailView();
            }
                
            if(c == 2){
                consoleSearch(false);
                consoleDetailView();
            }
                
            if(c == 3)
                consoleNewScholarship();
        }
    }
    
    public void consoleNewScholarship(){
        System.out.println("\nEnter the name of the scholarship to be created:");
        String name = consoleGetLine();
        System.out.println("Enter the minimum GPA (or 0.0 if no GPA requirement:");
        float grade = s.nextFloat();
        System.out.println("Enter the minimum hours necessary (or 0 if no hours requirement:");
        int hours = s.nextInt();
        String[] majors = myDB.getMajors();
        String myMajor;
        if(majors.length > 0){
            System.out.println("Known Majors:");
            for(int i=0;i<majors.length;i++){
                System.out.println((i+1)+": "+majors[i]);
            }
            System.out.println((majors.length+1)+": Other / Not Listed");
            System.out.println((majors.length+2)+": No specific major required");
        }
        int c = consoleInt(1,majors.length+2);
        c--;
        if(c < majors.length){
            myMajor = majors[c];
        }else if (c == majors.length+1){
            myMajor = "";
        }else{
            System.out.println("Enter required major:");
            myMajor = consoleGetLine();
        }
        System.out.println("Enter maximum number of recipients:");
        int max = s.nextInt();
        System.out.println("Enter amount awarded to each recipient:");
        float cash = s.nextFloat();
        
        Scholarship ns = new Scholarship(name,myMajor,grade,hours,max,cash);
        myDB.addScholarship(ns);
        System.out.println("Scholarship created.\n\n");
    }
    
    public void consoleSearch(boolean student){
        currentStudent = null;
        currentScholarship = null;
        System.out.println("Enter all or part of the name of the " + (student?"student":"scholarship") + " that you wish to search for." );
        System.out.println("For all available "+(student?"student":"scholarship")+"s, enter *");
        String term = consoleGetLine();
        if(student){
            Student[] results = myDB.searchStudents(term);
            if(results.length < 1){
                System.out.println("Sorry, no student exists with that name.");
                return;
            }
            for(int i=0;i<results.length;i++){
                System.out.println((i+1)+": "+results[i].getName(true));
            }
            System.out.println((results.length+1)+": [CANCEL]");
            int c = consoleInt(1,results.length+1);
            c--;
            if(c < results.length){
                currentStudent = results[c];
                currentScholarship = null;
            }
            return;
        }else{
            Scholarship[] results = myDB.searchScholarships(term);
            if(results.length < 1){
                System.out.println("Sorry, no student exists with that name.");
                return;
            }
            for(int i=0;i<results.length;i++){
                System.out.println((i+1)+": "+results[i].getName());
            }
            System.out.println((results.length+1)+": [CANCEL]");
            int c = consoleInt(1,results.length+1);
            c--;
            if(c < results.length){
                currentScholarship = results[c];
                currentStudent = null;
            }
        }
    }
    
    public void consoleDetailView(){
        if(currentStudent != null){
            System.out.println("\nStudent: " + currentStudent.getName(false));
            System.out.println("Major: " + currentStudent.getMajor());
            System.out.println("GPA: " + currentStudent.getGPA());
            System.out.println("Hours Awarded: " + currentStudent.getHours());
            String[] awards = currentStudent.getScholarships();
            if(awards.length > 0){
                Scholarship[] wins = myDB.resolve(currentStudent);
                if(wins.length > 0){
                    System.out.println("Scholarships awarded: ");
                    for(Scholarship w : wins){
                        System.out.println(w.getName() + " ($"+w.getAwardMoney()+")");
                    }
                }
            }
        }
        
        if(currentScholarship != null){
            System.out.println("\nScholarship: "+currentScholarship.getName());
            if(currentScholarship.getMinGPA() > 0f){
                System.out.println("Required GPA: "+currentScholarship.getMinGPA());
            }
            if(currentScholarship.getMinHours() > 0){
                System.out.println("Required Hours: "+currentScholarship.getMinHours());
            }
            if(currentScholarship.getMajor().length() > 0){
                System.out.println("Required Major: "+currentScholarship.getMajor());
            }
            System.out.println("This scholarship has been awarded to "
                    + currentScholarship.getNumAwarded() + " of a maximum of "
                    + currentScholarship.getMaxAwards() + " students.");
            System.out.println("Each student will receive $"+currentScholarship.getAwardMoney());
        }
        
        
        
    }
    
    public boolean consoleYesNo(){
        boolean truth = false;
        while(true){
            String q = s.next();
            if(q.equalsIgnoreCase("yes") || q.equalsIgnoreCase("y")){
                truth = true;
                break;
            }
            if(q.equalsIgnoreCase("no") || q.equalsIgnoreCase("n")){
                truth = false;
                break;
            }
        }
        return truth;
    }
    
    public int consoleInt(int low, int high){
        System.out.println("\nMake your selection ["+low+"-"+high+"]:");
        int c = low-1;
        while(c < low || c > high){
            try{
                c = s.nextInt();
            }catch(Exception e){
            }
            
        }
        return c;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ScholarshipForm sf = new ScholarshipForm();
        //sf.debugPrime();
        sf.consoleStart();
    }
    
}




    class Database implements Serializable{
        private ArrayList<Student> students;
        private ArrayList<Scholarship> scholarships;
        
        public Database(){
            students = new ArrayList();
            scholarships = new ArrayList();
        }
        
        public void addStudent(Student st){
            students.add(st);
        }
        
        public void addScholarship(Scholarship sc){
            scholarships.add(sc);
        }
        
        public Student[] searchStudents(String nameFrag){
           if(nameFrag.equals("*")){
                return students.toArray(new Student[0]);
            }
           ArrayList<Student> candidates = new ArrayList();
           for(Student s : students){
               if(s.getName(true).toLowerCase().contains(nameFrag.toLowerCase()) || 
                       s.getName(false).toLowerCase().contains(nameFrag.toLowerCase())){
                   candidates.add(s);
               }
           }
           return candidates.toArray(new Student[0]);
        }
        
        public Scholarship[] searchScholarships(String nameFrag){
            if(nameFrag.equals("*")){
                return scholarships.toArray(new Scholarship[0]);
            }
           ArrayList<Scholarship> candidates = new ArrayList();
           for(Scholarship s : scholarships){
               if(s.getName().toLowerCase().contains(nameFrag.toLowerCase())){
                   candidates.add(s);
               }
           }
           return candidates.toArray(new Scholarship[0]);
        }
        
        public Scholarship[] resolve(Student s){
            String[] toResolve = s.getScholarships();
            ArrayList<Scholarship> results = new ArrayList();
            for(Scholarship sc : scholarships){
                for(String n : toResolve){
                    if(sc.getName().equals(n)){
                        results.add(sc);
                    }
                }
            }
            return results.toArray(new Scholarship[0]);
        }
        
        public Scholarship[] getQualifiers(Student s){
            ArrayList<Scholarship> candidates = new ArrayList();
            String[] alreadyDone = s.getScholarships();
            for(Scholarship c : scholarships){
                if(c.getNumAwarded() >= c.getMaxAwards())
                    continue;
                if(c.getMinGPA() > s.getGPA())
                    continue;
                if(c.getMinHours() > s.getHours())
                    continue;
                if(c.getMajor().length() > 0 && !c.getMajor().equalsIgnoreCase(s.getMajor()))
                    continue;
                boolean isTaken = false;
                for(String scn : alreadyDone){
                    isTaken |= scn.equals(c.getName());
                }
                if(!isTaken){
                    candidates.add(c);
                }
            }
            
            return candidates.toArray(new Scholarship[0]);
        }
        
        public Student[] getAwards(Scholarship s){
            ArrayList<Student> candidates = new ArrayList();
            String n = s.getName();
            if(s.getNumAwarded() > 0){
                for(Student c : students){
                    String[] awards = c.getScholarships();
                    for(String q : awards){
                        if(n.equals(q)){
                            candidates.add(c);
                            break;
                        }
                            
                    }
                }
            }
            return candidates.toArray(new Student[0]);
        }
        
        public void awardScholarship(Student st, Scholarship sc){
            st.addScholarship(sc.getName());
            sc.award();
        }
        
        public String[] getMajors(){
            ArrayList<String> theMajors = new ArrayList();
            for(Scholarship s : scholarships){
                if(!theMajors.contains(s.getMajor()) && s.getMajor().length() > 0){
                    theMajors.add(s.getMajor());
                }
            }
            for(Student s : students){
                if(!theMajors.contains(s.getMajor())){
                    theMajors.add(s.getMajor());
                }
            }
            theMajors.sort(null);
            return theMajors.toArray(new String[0]);
        }
        
    }
    


    class Student implements Serializable{
        private String forename;
        private String surname;
        private String major;
        private float GPA;
        private int hours;
        private ArrayList<String> scholarships;
        
        public Student(String fn, String ln, String maj, float g, int hr){
            forename = fn;
            surname = ln;
            major = maj;
            GPA = g;
            hours = hr;
            scholarships = new ArrayList();
        }
        
        protected Student(){
            this("null","null","null",0f,0);
        }

        /**
         * @return the forename
         */
        public String getForename() {
            return forename;
        }

        /**
         * @param forename the forename to set
         */
        public void setForename(String forename) {
            this.forename = forename;
        }

        /**
         * @return the surname
         */
        public String getSurname() {
            return surname;
        }

        /**
         * @param surname the surname to set
         */
        public void setSurname(String surname) {
            this.surname = surname;
        }

        /**
         * @return the major
         */
        public String getMajor() {
            return major;
        }

        /**
         * @param major the major to set
         */
        public void setMajor(String major) {
            this.major = major;
        }

        /**
         * @return the GPA
         */
        public float getGPA() {
            return GPA;
        }

        /**
         * @param GPA the GPA to set
         */
        public void setGPA(float GPA) {
            this.GPA = GPA;
        }

        /**
         * @return the hours
         */
        public int getHours() {
            return hours;
        }

        /**
         * @param hours the hours to set
         */
        public void setHours(int hours) {
            this.hours = hours;
        }
        
        public String getName(boolean reversed){
            if(reversed){
                return surname + ", " + forename;
            }else{
                return forename + " " + surname;
            }
        }
        
        public void addScholarship(String name){
            scholarships.add(name);
        }
        
        public String[] getScholarships(){
        return scholarships.toArray(new String[0]);
        }
        
    }
    



    class Scholarship implements Serializable{
        private String name;
        private String major;
        private float minGPA;
        private int minHours;
        private int maxAwards;
        private float awardMoney;
        private int numAwarded;
        public Scholarship(String n, String maj, float mg, int mhr, int ma, float mny){
            name = n;
            major = maj;
            minGPA = mg;
            minHours = mhr;
            maxAwards = ma;
            awardMoney = mny;
            numAwarded = 0;
        }
        
        protected Scholarship(){
            this("null","null",0f,0,0,0f);
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the major
         */
        public String getMajor() {
            return major;
        }

        /**
         * @param major the major to set
         */
        public void setMajor(String major) {
            this.major = major;
        }

        /**
         * @return the minGPA
         */
        public float getMinGPA() {
            return minGPA;
        }

        /**
         * @param minGPA the minGPA to set
         */
        public void setMinGPA(float minGPA) {
            this.minGPA = minGPA;
        }

        /**
         * @return the minHours
         */
        public int getMinHours() {
            return minHours;
        }

        /**
         * @param minHours the minHours to set
         */
        public void setMinHours(int minHours) {
            this.minHours = minHours;
        }

        /**
         * @return the maxAwards
         */
        public int getMaxAwards() {
            return maxAwards;
        }

        /**
         * @param maxAwards the maxAwards to set
         */
        public void setMaxAwards(int maxAwards) {
            this.maxAwards = maxAwards;
        }

        /**
         * @return the awardMoney
         */
        public float getAwardMoney() {
            return awardMoney;
        }

        /**
         * @param awardMoney the awardMoney to set
         */
        public void setAwardMoney(float awardMoney) {
            this.awardMoney = awardMoney;
        }

        /**
         * @return the numAwarded
         */
        public int getNumAwarded() {
            return numAwarded;
        }

        /**
         * @param numAwarded the numAwarded to set
         */
        public void setNumAwarded(int numAwarded) {
            this.numAwarded = numAwarded;
        }
        
        public void award(){
            numAwarded++;
        }
        
    }
    
