package game;

import cards.Card;
import cards.Target;
import model.Deck;
import model.Player;
import model.Pig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GameState {

    private final List<Player> players;
    private final Deck deck;
    private int currentPlayerIndex;
    private boolean gameOver;
    private Player winner;

    public GameState(List<Player> players, Deck deck) {
        this.players = players;
        this.deck = deck;
        this.currentPlayerIndex = 0;
        this.gameOver = false;
        this.winner = null;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Deck getDeck() {
        return deck;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Player getWinner() {
        return winner;
    }

    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public void checkWinCondition() {
        for (Player p : players) {
            if (p.hasAllPigsDirty()) {
                this.winner = p;
                this.gameOver = true;
                return;
            }
        }
    }

    public List<Player> getOpponents(Player player) {
        List<Player> opponents = new ArrayList<>();
        for (Player p : players) {
            if (p != player) {
                opponents.add(p);
            }
        }
        return opponents;
    }

    public static boolean hasPlayableCard(GameState state, Player current) {

        for (Card card : current.getHand()) {

            // Eigene Schweine prüfen
            for (int i = 0; i < current.getPigs().size(); i++) {
                if (card.canPlay(state, current, Target.ofPig(current, i))) {
                    return true;
                }
            }

            // Gegner-Schweine prüfen
            for (Player opponent : state.getOpponents(current)) {
                for (int i = 0; i < opponent.getPigs().size(); i++) {
                    if (card.canPlay(state, current, Target.ofPig(opponent, i))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void applyFireDamage() {
        Player current = getCurrentPlayer();
        for (Pig pig : current.getPigs()) {
            if (pig.isInBarn() && pig.getBarn().isBurning()) {
                boolean burnedDown = pig.getBarn().tickFireDamage();
                if (burnedDown) {
                    pig.destroyBarn();
                }
            }
        }
    }
}