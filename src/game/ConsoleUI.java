package game;

import cards.Target;
import model.Player;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {


    public static int readInt(Scanner sc, String message) {

        while (true) {

            System.out.print(message);

            try {

                return Integer.parseInt(sc.nextLine());

            } catch (NumberFormatException e) {

                System.out.println("Bitte gib eine gültige Zahl ein.");
            }
        }
    }

    public static Target selectOwnPig(Scanner sc, Player current) {

        for (int i = 0; i < current.getPigs().size(); i++) {
            System.out.println((i + 1) + ": " + current.getPig(i).toString());
        }

        while (true) {

            int pigChoice = ConsoleUI.readInt(sc, "Schwein wählen: ") - 1;

            if (pigChoice >= 0 && pigChoice < current.getPigs().size()) {

                return Target.ofPig(current, pigChoice);
            }

            System.out.println("Ungültige Auswahl!");
        }
    }

    public static Target selectOpponentPig(Scanner sc, GameState state, Player current) {

        List<Player> opponents = state.getOpponents(current);

        for (int i = 0; i < opponents.size(); i++) {

            System.out.println((i + 1) + ": " + opponents.get(i).getNickname());
        }

        Player targetPlayer;

        while (true) {

            int playerChoice = ConsoleUI.readInt(sc, "Spieler wählen: ") - 1;

            if (playerChoice >= 0 && playerChoice < opponents.size()) {

                targetPlayer = opponents.get(playerChoice);
                break;
            }

            System.out.println("Ungültige Auswahl!");
        }

        for (int i = 0; i < targetPlayer.getPigs().size(); i++) {

            System.out.println((i + 1) + ": " + targetPlayer.getPig(i).toString());
        }

        while (true) {

            int pigChoice = ConsoleUI.readInt(sc, "Schwein wählen: ") - 1;

            if (pigChoice >= 0 && pigChoice < targetPlayer.getPigs().size()) {

                return Target.ofPig(targetPlayer, pigChoice);
            }

            System.out.println("Ungültige Auswahl!");
        }
    }

    public static void menu() {
        System.out.println("1 - Neues Normales Spiel");
        System.out.println("2 - Neues Spiel mit Extension");
        System.out.println("0 - Beenden");
    }
}
