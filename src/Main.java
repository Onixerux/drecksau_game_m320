import cards.*;
import game.GameState;
import model.Deck;
import model.Player;

import java.util.*;

import static game.GameState.hasPlayableCard;

public class Main {

    static ArrayList<Player> players = new ArrayList<>();

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {

            menu();

            int menuChoice;

            while (true) {
                System.out.print("Bitte Auswahl eingeben: ");
                String input = sc.nextLine();
                try {
                    menuChoice = Integer.parseInt(input);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Ungültige Eingabe. Bitte eine gültige Zahl eingeben.");
                }
            }
            switch (menuChoice) {
                case 1:
                    Deck.extension = false;
                    startGame(sc);
                    break;
                case 2:
                    Deck.extension = true;
                    startGame(sc);
                    break;
                case 0:
                    System.out.println("Spiel wird beendet");
                    return;
                default:
                    System.out.println("Ungültige Auswahl");
            }
        }
    }

    static void startGame(Scanner sc) {

        players.clear();

        System.out.println("Bitte Spieler Anzahl eingeben (2-4)");

        int playerCount = sc.nextInt();

        int pigCount;

        switch (playerCount) {
            case 2:
                pigCount = 5;
                break;
            case 3:
                pigCount = 4;
                break;
            case 4:
                pigCount = 3;
                break;
            default:
                System.out.println("Ungültige Spieleranzahl");
                return;
        }

        Player.createPlayers(players, sc, playerCount, pigCount);

        Deck deck = Deck.createStandardDeck(new Random());

        for (int i = 0; i < 3; i++) {
            for (Player player : players) {
                player.addCard(deck.draw());
            }
        }

        GameState state = new GameState(players, deck);

        gameLoop(state, sc);
    }

    static void gameLoop(GameState state, Scanner sc) {

        while (!state.isGameOver()) {

            Player current = state.getCurrentPlayer();

            state.applyFireDamage();

            //Prüfen ob der current eine Spielbare Karte hat
            boolean hasPlayableCard = hasPlayableCard(state, current);

            if (!hasPlayableCard) {
                System.out.println(current.getNickname() + " hat keine spielbaren Karten.");
                System.out.println("Alle Handkarten werden abgelegt und 3 neue Karten werden gezogen.");

                // Alle Handkarten auf den Ablagestapel legen
                List<Card> handCopy = new ArrayList<>(current.getHand());

                for (Card card : handCopy) {
                    current.removeCard(card);
                    state.getDeck().discard(card);
                }

                // 3 neue Karten ziehen
                for (int i = 0; i < 3; i++) {
                    current.addCard(state.getDeck().draw());
                }

                state.advanceTurn();
                continue;
            }

            System.out.println(current.getNickname() + " ist dran.");
            System.out.println("Bitte wähle die Karte welche du spielen möchtest.");

            List<Card> hand = current.getHand();

            for (int i = 0; i < hand.size(); i++) {
                System.out.println((i + 1) + " - " + hand.get(i).getName());
            }

            int choice;

            while (true) {
                try {
                    System.out.print("Auswahl: ");
                    choice = sc.nextInt() - 1;
                    sc.nextLine();

                    if (choice < 0 || choice >= hand.size()) {
                        System.out.println("Ungültige Kartenauswahl!");
                        continue;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Bitte gib eine Zahl ein!");
                    sc.nextLine();
                }
            }

            Card selectedCard = hand.get(choice);

            boolean isTargetOwnPig = Target.isTargetOwnPig(state, current, selectedCard);

            boolean isTargetOpponent = Target.isTargetOpponent(state, current, selectedCard);


            Target target = null;

            try {
                if (isTargetOwnPig) {

                    for (int i = 0; i < current.getPigs().size(); i++) {
                        System.out.println(i + 1 + ": " + current.getPig(i).toString());
                    }
                    int pigChoice;
                    while (true) {
                        pigChoice = sc.nextInt() - 1;
                        sc.nextLine();
                        if (pigChoice >= 0 && pigChoice < current.getPigs().size()) break;
                        System.out.println("Ungültige Auswahl!");
                        System.out.print("Bitte gib erneut ein: ");
                    }
                    target = Target.ofPig(current, pigChoice);

                } else if (isTargetOpponent) {

                    for (int i = 0; i < state.getOpponents(current).size(); i++) {
                        System.out.println(i + 1 + ": " + state.getOpponents(current).get(i).getNickname());
                    }

                    int playerChoice;
                    while (true) {
                        playerChoice = sc.nextInt() - 1;
                        sc.nextLine();
                        if (playerChoice >= 0 && playerChoice < state.getOpponents(current).size()) break;
                        System.out.println("Ungültige Auswahl!");
                        System.out.print("Bitte gib erneut ein: ");
                    }
                    Player targetPlayer = state.getOpponents(current).get(playerChoice);

                    for (int j = 0; j < targetPlayer.getPigs().size(); j++) {
                        System.out.println(j + 1 + ": " + targetPlayer.getPig(j).toString());

                    }

                    int pigChoice;
                    while (true) {
                        pigChoice = sc.nextInt() - 1;
                        sc.nextLine();
                        if (pigChoice >= 0 && pigChoice < targetPlayer.getPigs().size()) break;
                        System.out.println("Ungültige Auswahl!");
                        System.out.print("Bitte gib erneut ein: ");
                    }
                    target = Target.ofPig(targetPlayer, pigChoice);
                }
            } catch (Exception e) {
                System.out.println("Bitte gib eine Zahl ein!\n");
                sc.nextLine();
                continue;
            }

            // Karte anwenden
            if (target == null) {
                System.out.println("Ausgewählte Karte hat kein gültiges Ziel.");
                System.out.println("Bitte eine andere Karte auswählen.\n");
                continue;
            }

            if (!selectedCard.canPlay(state, current, target)) {
                System.out.println("Anforderungen nicht erfüllt!");
                System.out.println("Bitte eine andere Karte auswählen.\n");
                continue;
            }
            selectedCard.applyCard(state, current, target);
            current.removeCard(selectedCard);
            state.getDeck().discard(selectedCard);

            for (int i = current.getHand().size(); i < 3; i++) {
                current.addCard(state.getDeck().draw());
            }


            System.out.println(selectedCard.getName() + " wurde gespielt");

            // Gewinnbedingung prüfen NACH dem Spielen der Karte
            state.checkWinCondition();

            //Console Clear
            for (int i = 0; i < 100; i++) {
                System.out.println();
            }


            if (state.isGameOver()) {
                System.out.println(state.getWinner().getNickname() + " hat gewonnen!");
            } else {
                state.advanceTurn();
            }
        }
    }


    static void menu() {
        System.out.println("1 - Neues Normales Spiel");
        System.out.println("2 - Neues Spiel mit Extension");
        System.out.println("0 - Beenden");
    }
}