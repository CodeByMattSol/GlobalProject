package model;

public class HumanPlayer extends Player {
    public HumanPlayer(String name, char symbol) {
        super(name, symbol);
    }

    @Override
    public boolean makeTurn(Board board, int row, int col) {
        return board.makeTurn(row, col, this.symbol);
    }
}
