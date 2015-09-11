package bytecode;

import javaBytecodeGenerator.AbstractClassGenerator;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import types.ComparableType;

/**
 * A branching bytecode that compares the top two elements of the stack to check if
 * they are the same. It is used to route the computation at the end of a branching block of code.
 * <br><br>
 * ..., value1, value2 -&gt; ...<br>
 * (checks if value1 != value2)
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

public class IF_CMPNE extends BranchingComparisonBytecode {

	/**
	 * Constructs a bytecode that compares the top two elements of the
	 * stack to check if they are the same.
	 *
	 * @param type the semantical type of the values that are compared
	 */

	public IF_CMPNE(ComparableType type) {
		super(type);
	}

	/**
	 * Yields a branching bytecode that expresses the opposite condition of this.
	 *
	 * @return an {@code if_cmpeq} bytecode of the same type as this
	 */

	@Override
	public BranchingBytecode negate() {
		return new IF_CMPEQ(getType());
	}

	/**
	 * Auxiliary method that adds to the given list of instructions the code that goes
	 * to {@code yes} if the outcome of the test expressed by this branching bytecode is true.
	 * Namely, it generates the Java bytecode<br>
	 * <br>
	 * {@code if_icmpne yes}<br>
	 * <br>
	 * for {@code int} and Boolean values (Booleans are represented as integers in Java bytecode,
	 * with the assumption that 0 = <i>false</i> and 1 = <i>true</i>),<br>
	 * <br>
	 * {@code if_acmpeq yes}<br>
	 * <br>
	 * for objects and arrays, and<br>
	 * <br>
	 * {@code fcmpl}<br>
	 * {@code ifeq yes}<br>
	 * <br>
	 * for {@code float}. The {@code fcmpl} Java bytecode operates over two {@code float} values
	 * on top of the stack and produces an {@code int} value at their place, as it follows:<br>
	 * <br>
	 * ..., value1, value2 -> ..., 1   if value1 &gt; value2<br>
	 * ..., value1, value2 -> ..., 0   if value1 = value2<br>
	 * ..., value1, value2 -> ..., -1  if value1 &lt; value2
	 *
	 * @param il the list of instructions which must be expanded
	 * @param classGen the class generator to be used to generate the code
	 * @param yes the target where one must go if the outcome of the test
	 *            expressed by this branching bytecode is true
	 */

	@Override
	protected void generateJavaBytecodeAux(InstructionList il, AbstractClassGenerator classGen, InstructionHandle yes) {
		getType().JB_if_cmpne(il,yes);
	}
}