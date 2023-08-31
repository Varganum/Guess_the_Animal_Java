package animals;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

public class Main {
    private static final LocalTime MORNING_START = LocalTime.of(5, 0);
    private static final LocalTime AFTERNOON_FINISH = LocalTime.of(18, 0);
    private static final String TYPE_PARAMETER_MARKER = "-type";

    private static String[] clarificationPhrases;

    private static String languageSelected;

    private static PropertyResourceBundle messagesBundle = null;

    private static PropertyResourceBundle patternsBundle = null;

    public static void main (String[] args) {

        //determine selected language
        if ("eo".equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            languageSelected = "eo";
        } else {
            languageSelected = "en";
        }

        //determine file format and forming file name for loading and saving knowledge tree
        String pathToProperties = "Guess the Animal (Java)\\task\\src\\main\\resources\\";
        String fileFormat = determineFileFormat(args);
        String knowledgeFileName;
        String messagesFileName;
        String patternsFileName;
        if ("en".equals(languageSelected)) {
            knowledgeFileName = "animals.".concat(fileFormat);
            messagesFileName = pathToProperties.concat("messages.properties");
            patternsFileName = pathToProperties.concat("patterns.properties");
        } else {
            knowledgeFileName = "animals_".concat(languageSelected).concat(".").concat(fileFormat);
            messagesFileName = pathToProperties.concat("messages_eo.properties");
            patternsFileName = pathToProperties.concat("patterns_eo.properties");
        }

        File knowledgeFile = new File(knowledgeFileName);

        ObjectMapper objectMapper;
        if ("json".equals(fileFormat)) {
            objectMapper = new JsonMapper();
        } else if ("xml".equals(fileFormat)) {
            objectMapper = new XmlMapper();
        } else {
            objectMapper = new YAMLMapper();
        }

        try {
            messagesBundle = new PropertyResourceBundle(new BufferedReader(new FileReader(messagesFileName)));
            patternsBundle = new PropertyResourceBundle(new BufferedReader(new FileReader(patternsFileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        clarificationPhrases = messagesBundle.getString("ask.again").split("\f");

        //greeting user corresponding to the local time
        LocalTime currentTime = LocalTime.now();
        greetUser(currentTime);

        //declaring start node object and game binary tree object
        Node startNode = null;
        AnimalBinaryTree gameBinaryTree = null;

        //Initialising game
        if (knowledgeFile.exists()) {
            //case for existing knowledge file
            try {
                startNode = objectMapper.readValue(knowledgeFile, Node.class);
                gameBinaryTree = new AnimalBinaryTree(startNode);
                System.out.println(messagesBundle.getString("welcome"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //Initialising game from zero, i.e. nor existing knowledge file
            System.out.println(messagesBundle.getString("animal.wantLearn"));
            System.out.println(messagesBundle.getString("animal.askFavorite"));
            String animalFavorite = makeAnAnimal();
            startNode = new Node(animalFavorite, null);
            gameBinaryTree = new AnimalBinaryTree(startNode);
            System.out.print(getRandomMember(messagesBundle.getString("animal.nice").split("\f")));
            System.out.println(messagesBundle.getString("animal.learnedMuch"));
            System.out.println(messagesBundle.getString("game.letsPlay"));
            System.out.println(messagesBundle.getString("game.think"));
            System.out.println(messagesBundle.getString("game.enter"));
        }

        String userMadeAnimal1;
        boolean isFirstStatementForAnimal1;
        String userMadeAnimal2;
        boolean isFirstStatementForAnimal2;

        //Start the game cycle
        Scanner gameStart = new Scanner(System.in);
        String enterPressed = null;
        // enterPressed = gameStart.nextLine();
        boolean isGameOver = false;
        boolean isGuessAnimalGameOver;
        Node currentNode = startNode;
        Node nextNode;
        Node newNode1;
        Node newNode2;
        int menuPoint;

        while (!isGameOver) {

            menuPoint = acceptUserChoice();

            if (menuPoint == 0) {
                isGameOver = true;
            } else if (menuPoint == 1) {
                //Menu Point 1 - Play the guessing game
                System.out.println(messagesBundle.getString("game.think"));
                System.out.println(messagesBundle.getString("game.enter"));
                enterPressed = gameStart.nextLine();
                isGuessAnimalGameOver = false;

                while (!isGuessAnimalGameOver) {
                    if (currentNode.isLeaf()) {
                        //case of Leaf realization

                        System.out.println(messagesBundle.getString("game.isIt") + currentNode.getValue() + "?");

                        if (acceptUserAnswerYesOrNo()) {
                            //Animal is guessed successfully
                        } else {
                            userMadeAnimal1 = currentNode.getValue();
                            newNode1 = new Node(userMadeAnimal1, currentNode);
                            userMadeAnimal2 = makeAnAnimal(messagesBundle.getString("game.giveUp"));
                            newNode2 = new Node(userMadeAnimal2, currentNode);

                            currentNode.acceptAndSetUserStatement(userMadeAnimal1, userMadeAnimal2);
                            isFirstStatementForAnimal2 = whatIsFirstStatementAbout(userMadeAnimal2);
                            isFirstStatementForAnimal1 = !isFirstStatementForAnimal2;
                            currentNode.transformNode(newNode1, isFirstStatementForAnimal1, newNode2);

                            currentNode.printConclusionsStage3();

                            System.out.println(getRandomMember(messagesBundle.getString("animal.nice").split("\f")) +
                                    messagesBundle.getString("animal.learnedMuch"));
                        }
                        isGuessAnimalGameOver = true;
                    } else {
                        //case of non Leaf realization
                        nextNode = gameBinaryTree.getNextNode(currentNode);
                        currentNode = nextNode;
                    }
                    if (isGuessAnimalGameOver) {
                        System.out.println();
                        System.out.println(getRandomMember(messagesBundle.getString("game.again").split("\f")));
                        isGuessAnimalGameOver = !acceptUserAnswerYesOrNo();
                        if (!isGuessAnimalGameOver) {
                            currentNode = startNode;
                            System.out.println(messagesBundle.getString("game.think"));
                            System.out.println(messagesBundle.getString("game.enter"));
                            enterPressed = gameStart.nextLine();
                        }
                    }
                }
            } else if (menuPoint == 2) {
                //List of all animals

                System.out.println(messagesBundle.getString("user.choice"));
                System.out.println("2");
                System.out.println(messagesBundle.getString("tree.list.animals"));

                ArrayList<String> animalsList = gameBinaryTree.animalsToList();
                animalsList.sort(Comparator.naturalOrder());
                for (String animal : animalsList) {
                    System.out.println(" - " + animal);
                }

            } else if (menuPoint == 3) {
                //Search for an animal

                System.out.println(messagesBundle.getString("user.choice"));
                System.out.println("3");
                System.out.println(messagesBundle.getString("animal.prompt"));

                String animalToSearch = Node.removeUndefinedArticle(makeAnAnimal());
                Node isSuchAnAnimal = gameBinaryTree.findNode(animalToSearch);

                if (Objects.nonNull(isSuchAnAnimal)) {
                    System.out.printf(messagesBundle.getString("tree.search.facts") + "\n", animalToSearch);
                    gameBinaryTree.printFactsAbout(isSuchAnAnimal);
                } else {
                    System.out.printf(messagesBundle.getString("tree.search.noFacts") + "\n", animalToSearch);
                }

            } else if (menuPoint == 4) {
                //Calculate statistics
                System.out.println(messagesBundle.getString("tree.stats.title") + "\n");
                System.out.printf(messagesBundle.getString("tree.stats.root") + "\n",
                        patternsBundle.getString("pattern.it.capital") +
                                gameBinaryTree.getRoot().getVerb(true) + gameBinaryTree.getRoot().getStatement());

                gameBinaryTree.updateStatistics();
                System.out.printf(messagesBundle.getString("tree.stats.nodes") + "\n", gameBinaryTree.getNodesNumber());
                System.out.printf(messagesBundle.getString("tree.stats.animals") + "\n", gameBinaryTree.getLeavesNumber());
                System.out.printf(messagesBundle.getString("tree.stats.statements") + "\n", (gameBinaryTree.getNodesNumber() - gameBinaryTree.getLeavesNumber()));
                System.out.printf(messagesBundle.getString("tree.stats.height") + "\n", gameBinaryTree.getLeafDepth().get(gameBinaryTree.getLeafDepth().size() - 1));
                System.out.printf(messagesBundle.getString("tree.stats.minimum") + "\n", gameBinaryTree.getLeafDepth().get(0));
                System.out.printf(messagesBundle.getString("tree.stats.average") + "\n", gameBinaryTree.getAverageDepth());

            } else if (menuPoint == 5) {
                //Print the Knowledge Tree
                gameBinaryTree.printPreOrder();
            }

        }

        sayUserBye();

        //saving Binary Tree to the local file

        try {
            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(knowledgeFile, startNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }  //end of main method

    public static PropertyResourceBundle getMessagesBundle() {
        return messagesBundle;
    }

    public static PropertyResourceBundle getPatternsBundle() {
        return patternsBundle;
    }

    public static String getLanguageSelected() {
        return languageSelected;
    }

    private static String getRandomMember(String[] stringArray) {
        int arraySize = stringArray.length;
        Random random = new Random();
        int index = random.nextInt(arraySize);
        return stringArray[index];
    }

    private static int acceptUserChoice() {
        int choice = -1;
        boolean isChoiceCorrect = false;
        Scanner userChoiceScanner = new Scanner(System.in);

        System.out.println(messagesBundle.getString("menu.property.title"));
        System.out.println();
        System.out.println("1. " + messagesBundle.getString("menu.entry.play"));
        System.out.println("2. " + messagesBundle.getString("menu.entry.list"));
        System.out.println("3. " + messagesBundle.getString("menu.entry.search"));
        System.out.println("4. " + messagesBundle.getString("menu.entry.statistics"));
        System.out.println("5. " + messagesBundle.getString("menu.entry.print"));
        System.out.println("0. " + messagesBundle.getString("menu.property.exit"));

        while (!isChoiceCorrect) {
            choice = userChoiceScanner.nextInt();
            if (choice < 0 || choice > 5) {
                System.out.println(messagesBundle.getString("menu.property.error"));
            } else {
                isChoiceCorrect = true;
            }
        }
        return choice;
    }

    private static String determineFileFormat(String[] args) {
        String result = "json";
        if (args.length > 0 && Main.TYPE_PARAMETER_MARKER.equals(args[0])) {
            if (args[1].matches("json|xml|yaml")) {
                result = args[1];
            } else {
                System.out.println("No such file type available. File type is set to JSON.");
            }
        }
        return result;
    }

    private static boolean whatIsFirstStatementAbout(String userMadeAnimal) {
        boolean result;
        System.out.printf(messagesBundle.getString("game.isCorrect"), userMadeAnimal);
        result = acceptUserAnswerYesOrNo();
        return result;
    }

    static boolean acceptUserAnswerYesOrNo() {

        Scanner acceptUserAnswerScanner = new Scanner(System.in);
        boolean isAnswerRecognised = false;
        String userAnswer = null;
        while (!isAnswerRecognised) {
            userAnswer = acceptUserAnswerScanner.nextLine().toLowerCase();

            if (userAnswer.toLowerCase().trim().matches(patternsBundle.getString("positiveAnswer.isCorrect"))) {
                userAnswer = "Yes";
                isAnswerRecognised = true;
            } else if (userAnswer.toLowerCase().trim().matches(patternsBundle.getString("negativeAnswer.isCorrect"))) {
                userAnswer = "No";
                isAnswerRecognised = true;
            } else {
                System.out.println(getRandomMember(clarificationPhrases));
            }
        }

        return "Yes".equals(userAnswer);
    }

    private static String makeAnAnimal(String message) {
        System.out.println(message);
        Scanner makingAnimalScanner = new Scanner(System.in);
        String animal = makingAnimalScanner.nextLine().toLowerCase();
        animal = checkOrTransformToFormat(animal);
        return animal;
    }


    private static String makeAnAnimal() {
        Scanner makingAnimalScanner = new Scanner(System.in);
        String animal = makingAnimalScanner.nextLine().toLowerCase();
        animal = checkOrTransformToFormat(animal);
        return animal;
    }

    private static String checkOrTransformToFormat(String animal) {
        String animalFormatted = animal;
        if (!animal.matches(patternsBundle.getString("animal.isCorrect"))) {
            animalFormatted = addUndefinedArticle(animal);
        }
        return animalFormatted;
    }

    private static String addUndefinedArticle(String userInput) {
        String formattedUserInput = null;

        userInput = removeDefinedArticle(userInput);

        if (userInput.matches(patternsBundle.getString("animal.1.pattern"))) {
            formattedUserInput = userInput.replaceFirst(patternsBundle.getString("animal.1.pattern"), patternsBundle.getString("animal.1.replace"));
        } else if (patternsBundle.containsKey("animal.2.pattern") && userInput.matches(patternsBundle.getString("animal.2.pattern"))) {
            formattedUserInput = userInput.replaceFirst(patternsBundle.getString("animal.2.pattern"), patternsBundle.getString("animal.2.replace"));
        } else if (patternsBundle.containsKey("animal.3.pattern") && userInput.matches(patternsBundle.getString("animal.3.pattern"))) {
            formattedUserInput = userInput.replaceFirst(patternsBundle.getString("animal.3.pattern"), patternsBundle.getString("animal.3.replace"));
        }

        return formattedUserInput;
    }

    private static String removeDefinedArticle(String userInput) {
        String formattedString;

        if (userInput.matches(patternsBundle.getString("animal.0.pattern"))) {
            formattedString = userInput.replaceFirst(patternsBundle.getString("animal.0.pattern"), patternsBundle.getString("animal.0.replace"));
        } else {
            formattedString = userInput;
        }

        return formattedString;
    }

    private static void sayUserBye() {
        System.out.println(getRandomMember(messagesBundle.getString("farewell").split("\f")));
    }

    private static void greetUser(LocalTime currentTime) {
        if (currentTime.equals(Main.MORNING_START) ||
                (currentTime.isAfter(Main.MORNING_START) && currentTime.isBefore(LocalTime.NOON)) ||
                currentTime.equals(LocalTime.NOON)) {
            System.out.println(messagesBundle.getString("greeting.morning"));
        } else if (currentTime.isAfter(LocalTime.NOON) && currentTime.isBefore(Main.AFTERNOON_FINISH) ||
                currentTime.equals(Main.AFTERNOON_FINISH)) {
            System.out.println(messagesBundle.getString("greeting.afternoon"));
        } else {
            System.out.println(messagesBundle.getString("greeting.evening"));
        }
        System.out.println();
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Node {

    //constants

    private static final Pattern PATTERN_CAN = Pattern.compile(Main.getPatternsBundle().getString("pattern.can"), Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HAS = Pattern.compile(Main.getPatternsBundle().getString("pattern.has"), Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_IS = Pattern.compile(Main.getPatternsBundle().getString("pattern.is"), Pattern.CASE_INSENSITIVE);


    //instant variables
    @JsonBackReference
    Node parent;
    String value;
    String statement;
    String scenarioMarkerVerb;
    boolean isPostOrderChecked = false;

    @JsonManagedReference
    Node leftNo;
    @JsonManagedReference
    Node rightYes;


    public Node() {
        this.value = "";
        rightYes = null;
        leftNo = null;
    }

    public Node(String value, Node parent) {
        this.parent = parent;
        this.value = value;
        rightYes = null;
        leftNo = null;
    }

    @JsonIgnore
    boolean isLeaf() {
        return Objects.isNull(this.leftNo) && Objects.isNull(this.rightYes);
    }


    //getters and setters

    public Node getLeftNo() {
        return this.leftNo;
    }

    public Node getRightYes() {
        return this.rightYes;
    }

    public String getScenarioMarkerVerb() {
        return this.scenarioMarkerVerb;
    }

    public void setScenarioMarkerVerb(String scenarioMarkerVerb) {
        this.scenarioMarkerVerb = scenarioMarkerVerb;
    }

    public String getStatement() {
        return this.statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setIsPostOrderChecked(boolean value) {
        this.isPostOrderChecked = value;
    }

    public boolean getIsPostOrderChecked() {
        return this.isPostOrderChecked;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return this.parent;
    }

    /////////////////////////////////UTILITY METHODS///////////////////////


    void acceptAndSetUserStatement(String animal1, String animal2) {

        Scanner firstStatementScanner = new Scanner(System.in);
        boolean statementAccepted = false;
        String userStatement;

        Matcher matcherCan;
        Matcher matcherHas;
        Matcher matcherIs;

        System.out.printf(Main.getMessagesBundle().getString("statement.prompt"), animal1, animal2);

        while (!statementAccepted) {

            userStatement = firstStatementScanner.nextLine().toLowerCase();
            matcherCan = Node.PATTERN_CAN.matcher(userStatement);
            matcherHas = Node.PATTERN_HAS.matcher(userStatement);
            matcherIs = Node.PATTERN_IS.matcher(userStatement);

            if (matcherCan.matches()) {
                statementAccepted = true;
                setScenarioMarkerVerb(Main.getPatternsBundle().getString("verb.can"));
                this.setStatement(userStatement.replaceFirst(Main.getPatternsBundle().getString("pattern.can"), Main.getPatternsBundle().getString("pattern.can.statement")));
            } else if (matcherHas.matches()) {
                statementAccepted = true;
                setScenarioMarkerVerb(Main.getPatternsBundle().getString("verb.has"));
                this.setStatement(userStatement.replaceFirst(Main.getPatternsBundle().getString("pattern.has"), Main.getPatternsBundle().getString("pattern.has.statement")));
            } else if (matcherIs.matches()) {
                statementAccepted = true;
                setScenarioMarkerVerb(Main.getPatternsBundle().getString("verb.is"));
                this.setStatement(userStatement.replaceFirst(Main.getPatternsBundle().getString("pattern.is"), Main.getPatternsBundle().getString("pattern.is.statement")));
            } else {
                System.out.printf(Main.getMessagesBundle().getString("statement.prompt"), animal1, animal2);
                System.out.println(Main.getMessagesBundle().getString("statement.error"));
            }
        }
    }

    @JsonIgnore
    String getQuestion() {
        return this.getQuestionForm() + Node.removeDot(this.statement) + "?";
    }


    @JsonIgnore
    private String getQuestionForm() {
        String verb = this.scenarioMarkerVerb;
        String result = null;

        if (Main.getPatternsBundle().getString("verb.can").equals(verb)) {
            result = Main.getPatternsBundle().getString("verb.can.question") + " ";
        } else if (Main.getPatternsBundle().getString("verb.has").equals(verb)) {
            result = Main.getPatternsBundle().getString("verb.has.question") + " ";
        } else if (Main.getPatternsBundle().getString("verb.is").equals(verb)) {
            result = Main.getPatternsBundle().getString("verb.is.question") + " ";
        }

        return result;
    }

    @JsonIgnore
    String getVerb(boolean isTrue) {
        String verb = getScenarioMarkerVerb();
        String result = null;

        if (Main.getPatternsBundle().getString("verb.can").equals(verb)) {
            result = isTrue ? " " + Main.getPatternsBundle().getString("verb.can") + " " :
                    " " + Main.getPatternsBundle().getString("verb.can.negative") + " ";
        } else if (Main.getPatternsBundle().getString("verb.has").equals(verb)) {
            result = isTrue ? " " + Main.getPatternsBundle().getString("verb.has") + " " :
                    " " + Main.getPatternsBundle().getString("verb.has.negative") + " ";
        } else if (Main.getPatternsBundle().getString("verb.is").equals(verb)) {
            result = isTrue ? " " + Main.getPatternsBundle().getString("verb.is") + " " :
                    " " + Main.getPatternsBundle().getString("verb.is.negative") + " ";
        }

        return result;
    }

    public void printConclusionsStage3() {
        String animal1 = this.leftNo.getValue();
        String animal2 = this.rightYes.getValue();

        if ("en".equals(Main.getLanguageSelected())) {
            animal1 = Node.removeUndefinedArticle(animal1);
            animal2 = Node.removeUndefinedArticle(animal2);
        }

        System.out.println(Main.getMessagesBundle().getString("game.learned"));

        System.out.println("- " + Main.getMessagesBundle().getString("defined.article.cap") + " " + animal1 +
                this.getVerb(false) + Node.removeDot(this.statement) + ".");
        System.out.println("- " + Main.getMessagesBundle().getString("defined.article.cap") + " " + animal2 +
                this.getVerb(true) + Node.removeDot(this.statement) + ".");

        System.out.println(Main.getMessagesBundle().getString("game.distinguish"));
        System.out.println(this.getQuestion());
    }

    @JsonIgnore
    static String removeDot(String withDot) {
        String result;
        if (withDot.charAt(withDot.length() - 1) == '.') {
            result = withDot.substring(0, withDot.length() - 1);
        } else {
            result = withDot;
        }
        return result;
    }

    @JsonIgnore
    static String removeUndefinedArticle(String words) {
        String result;
        if (words.matches("^(an? )(.+)")) {
            result = words.replaceFirst("^(an? )(.+)", "$2");
        } else {
            result = words;
        }

        return result;
    }

    void transformNode(Node node1, boolean isYes, Node node2) {
        if (isYes) {
            this.rightYes = node1;
            this.leftNo = node2;
        } else {
            this.rightYes = node2;
            this.leftNo = node1;
        }
    }

    public void printNodePreOrder(String prefix) {
        String nextPrefixYes = null;
        String nextPrefixNo = null;

        //VLSymbol - Vertical Line Symbol
        String VLSymbol = Main.getMessagesBundle().getString("tree.print.vertical");

        //BrSymbol - Branch Symbol
        String BrSymbol = Main.getMessagesBundle().getString("tree.print.branch");

        //CoSymbol - CornerSymbol
        String CoSymbol = Main.getMessagesBundle().getString("tree.print.corner");

        if (!this.isLeaf()) {
            System.out.print(prefix);
            System.out.println(this.getQuestion());
            if (prefix.length() == 3) {
                nextPrefixYes = "  " + BrSymbol + " ";
                nextPrefixNo = "  " + CoSymbol + " ";
            } else {
                if (prefix.matches(".+" + BrSymbol + ".+")) {
                    nextPrefixYes = prefix.replaceFirst( BrSymbol, VLSymbol + BrSymbol);
                    nextPrefixNo = prefix.replaceFirst( BrSymbol, VLSymbol + CoSymbol);
                } else if (prefix.matches(".+" + CoSymbol + ".+")) {
                    nextPrefixYes = prefix.replaceFirst(CoSymbol, " " + BrSymbol);
                    nextPrefixNo = prefix.replaceFirst(CoSymbol, " " + CoSymbol);
                }
            }

            this.getRightYes().printNodePreOrder(nextPrefixYes);
            this.getLeftNo().printNodePreOrder(nextPrefixNo);

        } else {
            System.out.print(prefix);
            System.out.println(this.getValue());
        }
    }
}

class AnimalBinaryTree {

    Node root;

    //statistics data
    private int nodesNumber;
    private int leavesNumber;

    ArrayList<Integer> leafDepth = new ArrayList<>();

    double averageDepth;

    AnimalBinaryTree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public int getNodesNumber() {
        return nodesNumber;
    }

    public void setNodesNumber(int nodesNumber) {
        this.nodesNumber = nodesNumber;
    }

    public int getLeavesNumber() {
        return leavesNumber;
    }

    public void setLeavesNumber(int leavesNumber) {
        this.leavesNumber = leavesNumber;
    }

    public ArrayList<Integer> getLeafDepth() {
        return leafDepth;
    }

    public void setLeafDepth(ArrayList<Integer> leafDepth) {
        this.leafDepth = leafDepth;
    }

    public double getAverageDepth() {
        return averageDepth;
    }

    public void setAverageDepth(double averageDepth) {
        this.averageDepth = averageDepth;
    }

    public Node getNextNode(Node currentNode) {
        Node result;
        System.out.println(currentNode.getQuestion());
        if (Main.acceptUserAnswerYesOrNo()) {
            result = currentNode.rightYes;
        } else {
            result = currentNode.leftNo;
        }
        return result;
    }

    public void printPreOrder() {
        String initialPrefix = " " + Main.getMessagesBundle().getString("tree.print.corner") + " ";
        this.root.printNodePreOrder(initialPrefix);
    }

    public ArrayList<String> animalsToList() {
        ArrayList<String> resultList = new ArrayList<>();
        Node currentNode = this.root;

        while (!this.root.getIsPostOrderChecked()) {
            if (!currentNode.getIsPostOrderChecked()) {
                if (currentNode.isLeaf()) {
                    resultList.add(Node.removeUndefinedArticle(currentNode.getValue()));
                    currentNode.setIsPostOrderChecked(true);
                } else if (!currentNode.getLeftNo().getIsPostOrderChecked()) {
                    currentNode = currentNode.getLeftNo();
                } else if (!currentNode.getRightYes().getIsPostOrderChecked()) {
                    currentNode = currentNode.getRightYes();
                } else {
                    currentNode.setIsPostOrderChecked(true);
                }
            } else {
                //go back to parent Node
                currentNode = currentNode.getParent();
            }
            if (Objects.isNull(currentNode)) {
                root.setIsPostOrderChecked(true);
            }
        }

        //reset isPostOrderChecked flags for all nodes
        currentNode = this.root;
        while (this.root.getIsPostOrderChecked()) {
            if (currentNode.getIsPostOrderChecked()) {
                if (currentNode.isLeaf()) {
                    currentNode.setIsPostOrderChecked(false);
                } else if (currentNode.getLeftNo().getIsPostOrderChecked()) {
                    currentNode = currentNode.getLeftNo();
                } else if (currentNode.getRightYes().getIsPostOrderChecked()) {
                    currentNode = currentNode.getRightYes();
                } else {
                    currentNode.setIsPostOrderChecked(false);
                }
            } else {
                //go back to parent Node
                currentNode = currentNode.getParent();
            }
            if (Objects.isNull(currentNode)) {
                root.setIsPostOrderChecked(false);
            }
        }

        return resultList;
    }

    public Node findNode(String animalToSearch) {
        Node resultNode = null;
        Node currentNode = this.root;

        while (!this.root.getIsPostOrderChecked()) {
            if (!currentNode.getIsPostOrderChecked()) {
                if (currentNode.isLeaf()) {
                    if (Node.removeUndefinedArticle(currentNode.getValue()).equals(animalToSearch)) {
                        resultNode = currentNode;
                    }
                    currentNode.setIsPostOrderChecked(true);
                } else if (!currentNode.getLeftNo().getIsPostOrderChecked()) {
                    currentNode = currentNode.getLeftNo();
                } else if (!currentNode.getRightYes().getIsPostOrderChecked()) {
                    currentNode = currentNode.getRightYes();
                } else {
                    currentNode.setIsPostOrderChecked(true);
                }
            } else {
                //go back to parent Node
                currentNode = currentNode.getParent();
            }
            if (Objects.isNull(currentNode)) {
                root.setIsPostOrderChecked(true);
            }
        }

        //reset isPostOrderChecked flags for all nodes
        currentNode = this.root;
        while (this.root.getIsPostOrderChecked()) {
            if (currentNode.getIsPostOrderChecked()) {
                if (currentNode.isLeaf()) {
                    currentNode.setIsPostOrderChecked(false);
                } else if (currentNode.getLeftNo().getIsPostOrderChecked()) {
                    currentNode = currentNode.getLeftNo();
                } else if (currentNode.getRightYes().getIsPostOrderChecked()) {
                    currentNode = currentNode.getRightYes();
                } else {
                    currentNode.setIsPostOrderChecked(false);
                }
            } else {
                //go back to parent Node
                currentNode = currentNode.getParent();
            }
            if (Objects.isNull(currentNode)) {
                root.setIsPostOrderChecked(false);
            }
        }

        return resultNode;
    }


    public void printFactsAbout(Node isSuchAnAnimal) {
        Node currentNode = isSuchAnAnimal;
        Node nextNode = currentNode.getParent();
        Deque<String> facts = new ArrayDeque<>();

        while (Objects.nonNull(nextNode)) {

            if (currentNode.equals(nextNode.getLeftNo())) {
                facts.push("- " + Main.getPatternsBundle().getString("pattern.it.capital") +
                        nextNode.getVerb(false) + Node.removeDot(nextNode.getStatement()) + ".");
            } else {
                facts.push("- " + Main.getPatternsBundle().getString("pattern.it.capital") +
                        nextNode.getVerb(true) + Node.removeDot(nextNode.getStatement()) + ".");
            }

            currentNode = nextNode;
            nextNode = currentNode.getParent();
        }
        for (String fact : facts) {
            System.out.println(fact);
        }
    }

    public void updateStatistics() {
        ArrayList<Integer> leafDepth = new ArrayList<>();
        int nodesCounter = 0;
        int leavesCounter = 0;
        Node currentNode = this.root;

        while (!this.root.getIsPostOrderChecked()) {
            if (!currentNode.getIsPostOrderChecked()) {
                if (currentNode.isLeaf()) {
                    leavesCounter++;
                    nodesCounter++;
                    leafDepth.add(getNodeDepth(currentNode));
                    currentNode.setIsPostOrderChecked(true);
                } else if (!currentNode.getLeftNo().getIsPostOrderChecked()) {
                    currentNode = currentNode.getLeftNo();
                } else if (!currentNode.getRightYes().getIsPostOrderChecked()) {
                    currentNode = currentNode.getRightYes();
                } else {
                    currentNode.setIsPostOrderChecked(true);
                    nodesCounter++;
                }
            } else {
                //go back to parent Node
                currentNode = currentNode.getParent();
            }
            if (Objects.isNull(currentNode)) {
                root.setIsPostOrderChecked(true);
            }
        }

        //reset isPostOrderChecked flags for all nodes
        currentNode = this.root;
        while (this.root.getIsPostOrderChecked()) {
            if (currentNode.getIsPostOrderChecked()) {
                if (currentNode.isLeaf()) {
                    currentNode.setIsPostOrderChecked(false);
                } else if (currentNode.getLeftNo().getIsPostOrderChecked()) {
                    currentNode = currentNode.getLeftNo();
                } else if (currentNode.getRightYes().getIsPostOrderChecked()) {
                    currentNode = currentNode.getRightYes();
                } else {
                    currentNode.setIsPostOrderChecked(false);
                }
            } else {
                //go back to parent Node
                currentNode = currentNode.getParent();
            }
            if (Objects.isNull(currentNode)) {
                root.setIsPostOrderChecked(false);
            }
        }

        setNodesNumber(nodesCounter);
        setLeavesNumber(leavesCounter);
        leafDepth.sort(Comparator.naturalOrder());
        setLeafDepth(leafDepth);

        int sumOfDepths = 0;
        for (int depth : leafDepth) {
            sumOfDepths += depth;
        }
        double averageDepth = (double) sumOfDepths / leavesCounter;
        setAverageDepth(averageDepth);
    }

    private Integer getNodeDepth(Node leafNode) {
        Node currentNode = leafNode;
        Node nextNode = currentNode.getParent();
        int depth = 0;

        while (Objects.nonNull(nextNode)) {
            depth++;
            currentNode = nextNode;
            nextNode = currentNode.getParent();
        }
        return depth;
    }
}