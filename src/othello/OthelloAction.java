package othello;

import gamePlayer.Action;
import gamePlayer.InvalidActionException;

/**
 * An Action in the board game Othello.
 * @author Ashoat Tevosyan
 * @author Peter Brook
 * @since Sat April 23 2011
 * @version CSE 473
 */
//整个文件实际上都是基于State文件，只不过封装调用而已
public class OthelloAction implements Action<OthelloState> {//Action，每一步动作用三个变量表示即可，谁下的，下在哪儿。
	
	// Which player are we?
	private boolean player;
	// The x-coordinate of the move.
	private byte x;
	// The y-coordinate of the move.
	private byte y;
	
	/**
	 * Instantiate this object. 
	 * @param player True if player one; false otherwise.
	 * @param x      The x-coordinate of the move.
	 * @param y      The y-coordinate of the move.
	 */
	public OthelloAction(boolean player, byte x, byte y) {
		this.player = player;
		this.x = x;
		this.y = y;
	}
	
	/** {@inheritDoc }*/
	@Override
	public boolean validOn(OthelloState input) {//通过调用moveIsValid来看某一个动作是不是有效
		if (this.player != input.move) return false;
		return input.moveIsValid(this.x, this.y, this.player);
		
	}
	
	/** {@inheritDoc }*/
	@Override
	public OthelloState applyTo(OthelloState input) throws InvalidActionException {//应用操作
		if (this.player != input.move) return null;
		return input.childOnMove(x, y);
	}
	
	/**
	 * Returns a String representation of this OthelloAction.
	 * @return A String representation of this OthelloAction.
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		//builder.append("Player ").append(this.player ? "1" : "0").append(" is moving to ");
		//builder.append("(").append(this.x).append(", ").append(this.y).append(").");
		int charA = 97; //ascii码的97是‘a’
		builder.append((char)(this.y+charA));
		builder.append(this.x + 1);
		return builder.toString();
	}

	/**
	 * Helper to check if this move represents a pass
	 * @return
	 */
	public boolean isPass() {
		return x == -1 && y==-1;
	}
	
	@Override
	public boolean equals(Object obj) {
		OthelloAction other = (OthelloAction) obj;
		return other != null && this.x == other.x && this.y == other.y && this.player == other.player;
	}
}