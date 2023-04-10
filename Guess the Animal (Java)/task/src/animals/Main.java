package animals;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
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
    private static final String[] NO_ANSWER = {"n", "no", "no way", "nah", "nope", "negative", "I don't think so", "yeah no"};
    private static final Set<String> NO_ANSWER_SET = Arrays.stream(NO_ANSWER).collect(Collectors.toSet());

    private static final String INSTRUCTION1 = "The sentence should be of the format: 'It can/has/is ...'.";
    private static final String INSTRUCTION1_EXAMPLES = """
            The examples of a statement:
             - It can fly
             - It has horn
             - It is a mammal""";

    private static final Pattern PATTERN_CAN = Pattern.compile("It can .+", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_HAS = Pattern.compile("It has .+", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_IS = Pattern.compile("It is .+", Pattern.CASE_INSENSITIVE);

    private static String scenarioMarkerVerb;
    private static String firstStatement;

    public static void main (String[] args) {

        String userMadeAnimal1;
        boolean isFirstStatementForAnimal1;
        String userMadeAnimal2;
        boolean isFirstStatementForAnimal2;

        LocalTime currentTime = LocalTime.now();
        greetUser(currentTime);

        userMadeAnimal1 = makeAnAnimal(1);
        userMadeAnimal2 = makeAnAnimal(2);
        acceptUserFirstStatement(userMadeAnimal1, userMadeAnimal2);
        isFirstStatementForAnimal2 = whatIsFirstStatementAbout(userMadeAnimal2);
        isFirstStatementForAnimal1 = !isFirstStatementForAnimal2;

        printConclusionsStage2(userMadeAnimal1, isFirstStatementForAnimal1, userMadeAnimal2, isFirstStatementForAnimal2);

        /* Stage 1 part
        System.out.println("Is it " + userMadeAnimal1 + "?");
         */

        sayUserBye();
    }

    private static void printConclusionsStage2(String animal1, boolean isFirstStatementForAnimal1, String animal2, boolean isFirstStatementForAnimal2) {
        System.out.println("I learned the following facts about animals:");
        System.out.println("- The " + removeUndefinedArticle(animal1) +
                getVerb(isFirstStatementForAnimal1) + removeDot(getFirstStatement()) + ".");
        System.out.println("- The " + removeUndefinedArticle(animal2) +
                getVerb(isFirstStatementForAnimal2) + removeDot(getFirstStatement()) + ".");
        System.out.println("I can distinguish these animals by asking the question:");
        System.out.println(getQuestionForm() + removeDot(getFirstStatement()) + "?");
    }

    private static String removeDot(String withDot) {
        String result;
        if (withDot.charAt(withDot.length() - 1) == '.') {
            result = withDot.substring(0, withDot.length() - 1);
        } else {
            result = withDot;
        }
        return result;
    }

    private static String getQuestionForm() {
        String verb = getScenarioMarkerVerb();
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

    private static String getVerb(boolean is) {
        String verb = getScenarioMarkerVerb();
        String result = null;

        if ("can".equals(verb)) {
            result = is ? " can " : " can't ";
        } else if ("has".equals(verb)) {
            result = is ? " has " : " doesn't have ";
        } else if ("is".equals(verb)) {
            result = is ? " is " : " isn't ";
        }

        return result;
    }

    private static String removeUndefinedArticle(String words) {
        String[] splitWords = words.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < splitWords.length; i++) {
            result.append(splitWords[i]);
        }
        return new String(result);
    }

    private static boolean whatIsFirstStatementAbout(String userMadeAnimal) {
        boolean result;
        System.out.println("Is it correct for " + userMadeAnimal + "?");
        result = acceptUserAnswer();
        return result;
    }

    private static void acceptUserFirstStatement(String animal1, String animal2) {

        Scanner firstStatementScanner = new Scanner(System.in);
        boolean statementAccepted = false;
        String userStatement;

        Matcher matcherCan;
        Matcher matcherHas;
        Matcher matcherIs;

        System.out.println("Specify a fact that distinguishes " + animal1 + " from " + animal2 + ".");
        System.out.println(Main.INSTRUCTION1);

        while (!statementAccepted) {

            userStatement = firstStatementScanner.nextLine().toLowerCase();
            matcherCan = Main.PATTERN_CAN.matcher(userStatement);
            matcherHas = Main.PATTERN_HAS.matcher(userStatement);
            matcherIs = Main.PATTERN_IS.matcher(userStatement);

            if (matcherCan.matches()) {
                statementAccepted = true;
                setScenarioMarkerVerb("can");
                setFirstStatement(userStatement.substring(7));
            } else if (matcherHas.matches()) {
                statementAccepted = true;
                setScenarioMarkerVerb("has");
                setFirstStatement(userStatement.substring(7));
            } else if (matcherIs.matches()) {
                statementAccepted = true;
                setScenarioMarkerVerb("is");
                setFirstStatement(userStatement.substring(6));
            } else {
                System.out.println("Specify a fact that distinguishes " + animal1 + " from " + animal2 + ".");
                System.out.println(Main.INSTRUCTION1);
                System.out.println(Main.INSTRUCTION1_EXAMPLES);
            }

        }
    }

    private static boolean acceptUserAnswer() {
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

    private static String makeAnAnimal(int number) {
        System.out.println("Enter the " + (number == 1 ? "first" : "second") + " animal:");
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
    }

    public static String getScenarioMarkerVerb() {
        return scenarioMarkerVerb;
    }

    public static void setScenarioMarkerVerb(String scenarioMarkerVerb) {
        Main.scenarioMarkerVerb = scenarioMarkerVerb;
    }

    public static String getFirstStatement() {
        return firstStatement;
    }

    public static void setFirstStatement(String firstStatement) {
        Main.firstStatement = firstStatement;
    }
}
