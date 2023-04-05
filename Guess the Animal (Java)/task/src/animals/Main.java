package animals;

import java.time.LocalTime;
import java.util.*;
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
    private static final Character[] VOWELS = {'a', 'e', 'i', 'o', 'u', 'w', 'y'};
    private static final Set<Character> VOWELS_SET = Arrays.stream(VOWELS).collect(Collectors.toSet());
    private static final String[] YES_ANSWER = {"y", "yes", "yeah", "yep", "sure", "right", "affirmative",
            "correct", "indeed", "you bet", "exactly", "you said it"};
    private static final Set<String> YES_ANSWER_SET = Arrays.stream(YES_ANSWER).collect(Collectors.toSet());
    private static final String[] NO_ANSWER = {"n", "no", "no way", "nah", "nope", "negative", "I don't think so", "yeah no"};
    private static final Set<String> NO_ANSWER_SET = Arrays.stream(NO_ANSWER).collect(Collectors.toSet());

    public static void main (String[] args) {
        String userMadeAnimal;
        LocalTime currentTime = LocalTime.now();
        greetUser(currentTime);
        userMadeAnimal = makeAnAnimal();
        //System.out.println("Accepted: " + userMadeAnimal);
        System.out.println("Is it " + userMadeAnimal + "?");
        acceptUserAnswer();
        sayUserBye();
    }

    private static void acceptUserAnswer() {
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
        System.out.println("You answered: " + userAnswer);
    }

    private static String makeAnAnimal() {
        System.out.println("Enter an animal:");
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
}
