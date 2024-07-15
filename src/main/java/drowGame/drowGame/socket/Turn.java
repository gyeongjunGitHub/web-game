package drowGame.drowGame.socket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Turn {
    private int turn;
    private int cycle;

    public Turn(int turn, int cycle) {
        this.turn = turn;
        this.cycle = cycle;
    }

    public static Turn increaseTurn(Turn turn, int memberCount){
        if(turn.getTurn() == 1){
            turn.setCycle(turn.getCycle() + 1);
        }

        if (turn.getTurn() + 1 > memberCount){
            turn.setTurn(1);
        }else{
            turn.setTurn(turn.getTurn() + 1);
        }
        return turn;
    }
}
