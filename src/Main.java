import cards.*;
import game.ConsoleUI;
import game.GameState;
import model.Deck;
import model.Player;

import java.util.*;

import static game.GameState.hasPlayableCard;

public class Main {

    static final int HandSize = 3;

    static ArrayList<Player> players = new ArrayList<>();

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {

            ConsoleUI.menu();

            int menuChoice = ConsoleUI.readInt(sc, "Bitte Auswahl eingeben: ");

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

        int playerCount;

        while (true) {

            playerCount = ConsoleUI.readInt(sc, "Bitte Spieler Anzahl eingeben (2-4): ");

            if (playerCount >= 2 && playerCount <= 4) {
                break;
            }

            System.out.println("Bitte eine Zahl zwischen 2 und 4 eingeben.");
        }

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

        for (int i = 0; i < HandSize; i++) {
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

            boolean playableCardExists = hasPlayableCard(state, current);

            if (!playableCardExists) {

                System.out.println(current.getNickname() + " hat keine spielbaren Karten.");
                System.out.println("Alle Handkarten werden abgelegt und neue Karten werden gezogen.");

                discardHand(state, current);

                refillHand(state, current);

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

                choice = ConsoleUI.readInt(sc, "Auswahl: ") - 1;

                if (choice >= 0 && choice < hand.size()) {
                    break;
                }

                System.out.println("Ungültige Kartenauswahl!");
            }

            Card selectedCard = hand.get(choice);

            boolean isTargetOwnPig = Target.isTargetOwnPig(state, current, selectedCard);

            boolean isTargetOpponent = Target.isTargetOpponent(state, current, selectedCard);

            Target target = null;

            if (isTargetOwnPig) {

                target = ConsoleUI.selectOwnPig(sc, current);

            } else if (isTargetOpponent) {

                target = ConsoleUI.selectOpponentPig(sc, state, current);
            }

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

            playCard(state, current, selectedCard, target);

            refillHand(state, current);

            System.out.println(selectedCard.getName() + " wurde gespielt");

            state.checkWinCondition();

            clearConsole();

            if (state.isGameOver()) {

                System.out.println(state.getWinner().getNickname() + " hat gewonnen!");

            } else {

                state.advanceTurn();
            }
        }
    }

    static void discardHand(GameState state, Player player) {

        List<Card> handCopy = new ArrayList<>(player.getHand());

        for (Card card : handCopy) {

            player.removeCard(card);
            state.getDeck().discard(card);
        }
    }

    static void refillHand(GameState state, Player player) {

        for (int i = player.getHand().size(); i < HandSize; i++) {

            player.addCard(state.getDeck().draw());
        }
    }

    static void playCard(GameState state, Player player, Card card, Target target) {

        card.applyCard(state, player, target);

        player.removeCard(card);

        state.getDeck().discard(card);
    }

    static void clearConsole() {

        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}