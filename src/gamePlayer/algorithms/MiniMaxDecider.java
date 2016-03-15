package gamePlayer.algorithms;

import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.InvalidActionException;
import gamePlayer.State;
import gamePlayer.State.Status;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an AI Decider that uses a MiniMax algorithm.
 * We use alpha-beta pruning, but besides that we're pretty vanilla.
 * @author Ashoat Tevosyan
 * @author Peter Brook 
 * @since Mon April 28 2011
 * @version CSE 473
 */
public class MiniMaxDecider implements Decider {
	// Are we maximizing or minimizing?
	private boolean maximize;
	// The depth to which we should analyze the search space
	private int depth;
	// HashMap to avoid recalculating States
	private Map<State, Float> computedStates;
	// Used to generate a graph of the search space for each turn in SVG format
	private static final boolean DEBUG = true;
	
	/**
	 * Initialize this MiniMaxDecider. 
	 * @param maximize Are we maximizing or minimizing on this turn? True if the former.
	 * @param depth    The depth to which we should analyze the search space.
	 */
	public MiniMaxDecider(boolean maximize, int depth) {
		this.maximize = maximize;
		this.depth = depth;
		computedStates = new HashMap<State, Float>();
	}
	
	/**
	 * Decide which state to go into.
	 * We manually MiniMax the first layer so we can figure out which heuristic is from which Action.
	 * Also, we want to be able to choose randomly between equally good options.
	 * "I'm the decider, and I decide what is best." - George W. Bush
	 * @param state The start State for our search.
	 * @return The Action we are deciding to take.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Action decide(State state) {
		// Choose randomly between equally good options
		float value = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;//当机器先时，是nega，人先时posi
		//System.out.println("value:"+value);
		List<Action> bestActions = new ArrayList<Action>();
		// Iterate!
		int flag = maximize ? 1 : -1;
		for (Action action : state.getActions()) {//对所有的候选action，进行遍历，这里的action是一个临时变量，用于遍历
			try {
				// Algorithm!
				State newState = action.applyTo(state);//我们将这个action应用到当前的局面上
				float newValue = this.miniMaxRecursor(newState, Float.NEGATIVE_INFINITY,
						Float.POSITIVE_INFINITY, 1, !this.maximize);//真正的决策部分，获得新的评估分数
				// Better candidates?
				if (flag * newValue > flag * value) {//用更高的value替代原有的value
					//System.out.println("value:"+value+"  "+"newValue:"+newValue);
					value = newValue;
					bestActions.clear();
				}
				// Add it to the list of candidates?
				if (flag * newValue >= flag * value) bestActions.add(action);
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
		}
		// If there are more than one best actions, pick one of the best randomly
		Collections.shuffle(bestActions);//打乱候选集合，最后随机选择第一个。
		return bestActions.get(0);
	}
	
	/**
	 * The true implementation of the MiniMax algorithm!
	 * Thoroughly commented for your convenience.
	 * @param state    The State we are currently parsing.
     * @param alpha    The alpha bound for alpha-beta pruning.
     * @param beta     The beta bound for alpha-beta pruning.
	 * @param depth    The current depth we are at.
	 * @param maximize Are we maximizing? If not, we are minimizing.
	 * @return The best point count we can get on this branch of the state space to the specified depth.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public float miniMaxRecursor(State state, float alpha, float beta,int depth, boolean maximize) {//注意，这里的depth是当前遍历的深度，而不是我们设定的深度// Has this state already been computed?
//		if (computedStates.containsKey(state)) //计算过的state就不计算了
//                    // Return the stored result
//                    return computedStates.get(state);
//		// Is this state done?
//		if (state.getStatus() != Status.Ongoing)//Status是一个enum变量，包含了谁在下，状态（ongoing），和draw
//                    // Store and return
//                    return finalize(state, state.heuristic());//这个函数目前看跟没写一样，就是返回heuristic()函数计算出来的value值
//		// Have we reached the end of the line?
//		if (depth == this.depth)//到达深度以后，同样返回value
//                    //Return the heuristic value
//                    return state.heuristic();
//
//		// If not, recurse further. Identify the best actions to take.
//		float value = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
//		int flag = maximize ? 1 : -1;
//		List<Action> test = state.getActions();
//		for (Action action : test) {//增强for循环的遍历方法，只能读取List中的内容，无法对List做修改
//			// Check it. Is it better? If so, keep it.
//			try {
//				State childState = action.applyTo(state);//childstate，由当前state延伸出来的状态
//				float newValue = this.miniMaxRecursor(childState, depth + 1, !maximize);//这儿就是递归的时候，下一深度的入口
//				//Record the best value
//                                if (flag * newValue > flag * value)
//                                    value = newValue;
//			} catch (InvalidActionException e) {
//                                //Should not go here
//				throw new RuntimeException("Invalid action!");
//			}
//		}
//		// Store so we don't have to compute it again.
//		return finalize(state, value);
		if (computedStates.containsKey(state))
			return computedStates.get(state);
		// Is this state done?
		if (state.getStatus() != Status.Ongoing)
			return finalize(state, state.heuristic());
		// Have we reached the end of the line?
		if (depth == this.depth)
			return state.heuristic();
		// If not, recurse further. Identify the best actions to take.
		float value = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		int flag = maximize ? 1 : -1;
		List<Action> test = state.getActions();
		for (Action action : test) {
			// Check it. Is it better? If so, keep it.
			try {
				State childState = action.applyTo(state);
				float newValue = this.miniMaxRecursor(childState, alpha, beta, depth + 1, !maximize);
				//if (DEBUG) GraphVizPrinter.setRelation(childState, newValue, state);
				if (flag * newValue > flag * value) value = newValue;
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
			// A-B prunning here
			System.out.println("A-B prunning here! "+"depth="+depth);
			float p = maximize ? beta : alpha;
			if (flag * value > flag * p) return value;
			// 更新 alpha/beta 值
			if (maximize && value > alpha) alpha = value;
			else if (!maximize && value < beta) beta = value;
		}
		// Store so we don't have to compute it again.
		return finalize(state, value);
	}
	
	/**
	 * Handy private function to stick into HashMap before returning.
	 * We don't always want to stick into our HashMap, so use carefully.
	 * @param state The State we are hashing.
	 * @param value The value that State has.
	 * @return The value we were passed.
	 */
	private float finalize(State state, float value) {
		// THIS IS BROKEN DO NOT USE
		//computedStates.put(state, value);
		return value;
	}
	
}