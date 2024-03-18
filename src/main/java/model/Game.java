package model;

public class Game {
    private Board board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;

    public Game(String name1, String name2) {
        board = new Board();

        player1 = new HumanPlayer(name1, 'X');;
        player2 = new HumanPlayer(name2, 'O');

        currentPlayer = player1;
    }

    public void switchPlayer() {
        currentPlayer = currentPlayer == player1 ? player2 : player1;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    public Board getBoard() {
        return board;
    }

    public boolean isGameOver() {
        if (board.isWin(currentPlayer.getSymbol()) || board.isDraw()) {
            return true;
        }

        return false;
    }

    public String showResult() {
        if (board.isWin(currentPlayer.getSymbol())) {
            return ("Поздравляем! " + currentPlayer.getName() + " (" + currentPlayer.getSymbol() + ") выиграл!");
        } else if (board.isDraw()) {
            return ("Ничья! Никто не выиграл!");
        } else {
            return ("Игрок " + currentPlayer.getName() + " (" + currentPlayer.getSymbol() + ") сдался!");
        }
    }
}

