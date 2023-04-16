package animals;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import java.util.stream.Collectors;

public class Main {
    private static final LocalTime MORNING_START = LocalTime.of(5, 0);
    private static final LocalTime AFTERNOON_FINISH = LocalTime.of(18, 0);
    private static final String MORNING_GREETING = "Good morning!";
    private static final String AFTERNOON_GREETING = "Good afternoon!";
    private static final String EVENING_GREETING = "Good evening!";
    private static final String[] FAREWELL = {"Have a nice day!", "See you soon!", "Bye!", "See you next time!",
                                                "See you later!", "See you later, alligator!", "Catch you later!", "Peace!",
                                                "I'm outta here!", "Hasta la vista, baby!", "Adios, amigos!", "Chao!"};
    private static final String[] CLARIFICATION_PHRASES = {"I'm not sure I caught you: was it yes or no?",
            "Funny, I still don't understand, is it yes or no?",
            "Oh, it's too complicated for me: just tell me yes or no.",
            "Could you please simply say yes or no?",
            "Oh, no, don't try to confuse me: say yes or no."};
    private static final Character[] VOWELS = {'a', 'e', 'i', 'o', 'u', 'y'};
    private static final Set<Character> VOWELS_SET = Arrays.stream(VOWELS).collect(Collectors.toSet());
    private static final String[] YES_ANSWER = {"y", "yes", "yeah", "yep", "sure", "right", "affirmative",
            "correct", "indeed", "you bet", "exactly", "you said it"};
    private static final Set<String> YES_ANSWER_SET = Arrays.stream(YES_ANSWER).collect(Collectors.toSet());
    private static final String[] NO_ANSWER = {"n", "no", "no way", "nah", "nope", "negative", "i don't think so", "yeah no"};
    private static final Set<String> NO_ANSWER_SET = Arrays.stream(NO_ANSWER).collect(Collectors.toSet());

    private static final String STAGE3_FIRST_MESSAGE = """
            I want to learn about animals.
            Which animal do you like most?""";
    private static final String STAGE3_SECOND_MESSAGE = """
            Wonderful! I've learned so much about animals!
            Let's play a game!""";


    private static final String STAGE3_THIRD_MESSAGE = """
           You think of an animal, and I guess it.
           Press enter when you're ready.""";

    private static final String STAGE4_MESSAGE_WITH_KNOWLEDGE = """
            I know a lot about animals.
            Let's play a game!""";

    private static final String TYPE_PARAMETER_MARKER = "-type";

    public static void main (String[] args) {

        String fileFormat = determineFileFormat(args);
        String fileName = "animals.".concat(fileFormat);
        File knowledgeFile = new File(fileName);

        ObjectMapper objectMapper;
        if ("json".equals(fileFormat)) {
            objectMapper = new JsonMapper();
        } else if ("xml".equals(fileFormat)) {
            objectMapper = new XmlMapper();
        } else {
            objectMapper = new YAMLMapper();
        }

        //greeting user corresponding to the local time
        LocalTime currentTime = LocalTime.now();
        greetUser(currentTime);

        Node startNode = null;
        AnimalBinaryTree gameBinaryTree = null;

        if (knowledgeFile.exists()) {
            try {
                startNode = objectMapper.readValue(knowledgeFile, Node.class);
                gameBinaryTree = new AnimalBinaryTree(startNode);
                System.out.println(STAGE4_MESSAGE_WITH_KNOWLEDGE);
                System.out.println(STAGE3_THIRD_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //Initialising game
            System.out.println(STAGE3_FIRST_MESSAGE);
            String animalFavorite = makeAnAnimal();
            startNode = new Node(animalFavorite);
            gameBinaryTree = new AnimalBinaryTree(startNode);
            System.out.println(STAGE3_SECOND_MESSAGE);
            System.out.println(STAGE3_THIRD_MESSAGE);
        }

        String userMadeAnimal1;
        boolean isFirstStatementForAnimal1;
        String userMadeAnimal2;
        boolean isFirstStatementForAnimal2;

        //Start the game cycle
        Scanner gameStart = new Scanner(System.in);
        String enterPressed = gameStart.nextLine();
        boolean isGameOver = false;
        Node currentNode = startNode;
        Node nextNode;
        Node newNode1;
        Node newNode2;

        while (!isGameOver) {
            if (currentNode.isLeaf()) {
                //case of Leaf realization
                System.out.println("Is it " + currentNode.getValue() + "?");
                if (acceptUserAnswerYesOrNo()) {
                    //Animal is guessed successfully
                } else {
                    userMadeAnimal1 = currentNode.getValue();
                    newNode1 = new Node(userMadeAnimal1);
                    userMadeAnimal2 = makeAnAnimal("I give up. What animal do you have in mind?");
                    newNode2 = new Node(userMadeAnimal2);

                    currentNode.acceptAndSetUserStatement(userMadeAnimal1, userMadeAnimal2);
                    isFirstStatementForAnimal2 = whatIsFirstStatementAbout(userMadeAnimal2);
                    isFirstStatementForAnimal1 = !isFirstStatementForAnimal2;
                    currentNode.transformNode(newNode1, isFirstStatementForAnimal1, newNode2);

                    currentNode.printConclusionsStage3();
                    System.out.println("Nice! I've learned so much about animals!");
                }
                isGameOver = true;
            } else {
                //case of non Leaf realization
                nextNode = gameBinaryTree.getNextNode(currentNode);
                currentNode = nextNode;
            }
            if (isGameOver) {
                System.out.println();
                System.out.println("Would you like to play again?");
                isGameOver = !acceptUserAnswerYesOrNo();
                if (!isGameOver) {
                    currentNode = startNode;
                    System.out.println(STAGE3_THIRD_MESSAGE);
                    enterPressed = gameStart.nextLine();
                }
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
        System.out.println("Is the statement correct for " + userMadeAnimal + "?");
        result = acceptUserAnswerYesOrNo();
        return result;
    }


    static boolean acceptUserAnswerYesOrNo() {

        Scanner acceptUserAnswerScanner = new Scanner(System.in);
        Random clarification = new Random();
        boolean isAnswerRecognised = false;
        String userAnswer = "";
        while (!isAnswerRecognised) {
            userAnswer = acceptUserAnswerScanner.nextLine().toLowerCase();
            if (userAnswer.endsWith(".") || userAnswer.endsWith("!")) {
                userAnswer = userAnswer.substring(0, userAnswer.length() - 1);
            }
            if (YES_ANSWER_SET.contains(userAnswer.toLowerCase().trim())) {
                userAnswer = "Yes";
                isAnswerRecognised = true;
            } else if (NO_ANSWER_SET.contains(userAnswer.toLowerCase().trim())) {
                userAnswer = "No";
                isAnswerRecognised = true;
            } else {
                System.out.println(Main.CLARIFICATION_PHRASES[clarification.nextInt(Main.CLARIFICATION_PHRASES.length)]);
            }
        }

        /* Stage 1 part
        System.out.println("You answered: " + userAnswer);
        */

        return "Yes".equals(userAnswer);
    }

    /*
    Method for stage 2 of project;
     */
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
        String[] userInput = animal.split(" ");
        StringBuilder animalFormatted = new StringBuilder();
        if ("a".equals(userInput[0]) || "an".equals(userInput[0])) {
            animalFormatted.append(animal);
        } else {
            animalFormatted = addUndefinedArticle(userInput);
        }
        return animalFormatted.toString().trim();
    }

    private static StringBuilder addUndefinedArticle(String[] userInput) {
        StringBuilder formattedUserInput = new StringBuilder();
        if ("the".equals(userInput[0])) {
            if (Main.VOWELS_SET.contains(userInput[1].charAt(0))) {
                userInput[0] = "an";
            } else {
                userInput[0] = "a";
            }
        } else {
            if (Main.VOWELS_SET.contains(userInput[0].charAt(0))) {
                formattedUserInput.append("an");
            } else {
                formattedUserInput.append("a");
            }
        }
        for (String word : userInput) {
            formattedUserInput.append(" ").append(word);
        }
        return formattedUserInput;
    }

    private static void sayUserBye() {
        Random farewell = new Random();
        System.out.println(Main.FAREWELL[farewell.nextInt(Main.FAREWELL.length)]);
    }

    private static void greetUser(LocalTime currentTime) {
        if (currentTime.equals(Main.MORNING_START) ||
                (currentTime.isAfter(Main.MORNING_START) && currentTime.isBefore(LocalTime.NOON)) ||
                currentTime.equals(LocalTime.NOON)) {
            System.out.println(Main.MORNING_GREETING);
        } else if (currentTime.isAfter(LocalTime.NOON) && currentTime.isBefore(Main.AFTERNOON_FINISH) ||
            currentTime.equals(Main.AFTERNOON_FINISH)) {
            System.out.println(Main.AFTERNOON_GREETING);
        } else {
            System.out.println(Main.EVENING_GREETING);
        }
        System.out.println();
    }

}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Node {

    //constants
    private static final String INSTRUCTION1 = "The sentence should be of the format: 'It can/has/is ...'.";
    private static final String INSTRUCTION1_EXAMPLES = """
                The examples of a statement:
                 - It can fly
                 - It has horn
                 - It is a mammal""";

    private static final Pattern PATTERN_CAN = Pattern.compile("It can .+", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HAS = Pattern.compile("It has .+", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_IS = Pattern.compile("It is .+", Pattern.CASE_INSENSITIVE);


    //instant variables
    String value;
    String statement;
    String scenarioMarkerVerb;

    Node leftNo;
    Node rightYes;


    public Node() {
        this.value = "";
        rightYes = null;
        leftNo = null;
    }

    public Node(String value) {
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

    //utility methods
    void acceptAndSetUserStatement(String animal1, String animal2) {

        Scanner firstStatementScanner = new Scanner(System.in);
        boolean statementAccepted = false;
        String userStatement;

        Matcher matcherCan;
        Matcher matcherHas;
        Matcher matcherIs;

        System.out.println("Specify a fact that distinguishes " + animal1 + " from " + animal2 + ".");
        System.out.println(Node.INSTRUCTION1);

        while (!statementAccepted) {

            userStatement = firstStatementScanner.nextLine().toLowerCase();
            matcherCan = Node.PATTERN_CAN.matcher(userStatement);
            matcherHas = Node.PATTERN_HAS.matcher(userStatement);
            matcherIs = Node.PATTERN_IS.matcher(userStatement);

            if (matcherCan.matches()) {
                statementAccepted = true;
                setScenarioMarkerVerb("can");
                this.setStatement(userStatement.substring(7));
            } else if (matcherHas.matches()) {
                statementAccepted = true;
                setScenarioMarkerVerb("has");
                this.setStatement(userStatement.substring(7));
            } else if (matcherIs.matches()) {
                statementAccepted = true;
                setScenarioMarkerVerb("is");
                this.setStatement(userStatement.substring(6));
            } else {
                System.out.println("Specify a fact that distinguishes " + animal1 + " from " + animal2 + ".");
                System.out.println(Node.INSTRUCTION1);
                System.out.println(Node.INSTRUCTION1_EXAMPLES);
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

        if ("can".equals(verb)) {
            result = "- Can it ";
        } else if ("has".equals(verb)) {
            result = "- Does it have ";
        } else if ("is".equals(verb)) {
            result = "- Is it ";
        }

        return result;
    }

    @JsonIgnore
    private String getVerb(boolean isTrue) {
        String verb = getScenarioMarkerVerb();
        String result = null;

        if ("can".equals(verb)) {
            result = isTrue ? " can " : " can't ";
        } else if ("has".equals(verb)) {
            result = isTrue ? " has " : " doesn't have ";
        } else if ("is".equals(verb)) {
            result = isTrue ? " is " : " isn't ";
        }

        return result;
    }

    public void printConclusionsStage3() {
        String animal1 = this.leftNo.getValue();
        String animal2 = this.rightYes.getValue();

        System.out.println("I learned the following facts about animals:");
        System.out.println("- The " + Node.removeUndefinedArticle(animal1) +
                this.getVerb(false) + Node.removeDot(this.statement) + ".");
        System.out.println("- The " + Node.removeUndefinedArticle(animal2) +
                this.getVerb(true) + Node.removeDot(this.statement) + ".");
        System.out.println("I can distinguish these animals by asking the question:");
        System.out.println(this.getQuestion());
    }

    @JsonIgnore
    private static String removeDot(String withDot) {
        String result;
        if (withDot.charAt(withDot.length() - 1) == '.') {
            result = withDot.substring(0, withDot.length() - 1);
        } else {
            result = withDot;
        }
        return result;
    }

    @JsonIgnore
    private static String removeUndefinedArticle(String words) {
        String[] splitWords = words.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < splitWords.length; i++) {
            result.append(splitWords[i]);
        }
        return new String(result);
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

}

class AnimalBinaryTree {

    Node root;

    AnimalBinaryTree(Node root) {
        this.root = root;
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
}
