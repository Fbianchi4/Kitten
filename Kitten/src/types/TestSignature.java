package types;


import javaBytecodeGenerator.TestClassGenerator;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.MethodGen;

import translation.Block;
import absyn.TestDeclaration;

/**
 * La signature di un test di una classe Kitten.
 * 
 * @author Federico Bianchi
 */

public class TestSignature extends CodeSignature {

    /**
     * Costruisce la signature di un test.
     * 
     * @param clazz la classe a cui il test appartiene.
     * @param name il nome del test.
     * @param abstractSyntax la sintassi astratta della dichiarazione del test.
     */
    
    public TestSignature(ClassType clazz, String name, TestDeclaration abstractSyntax) {
        super(clazz,IntType.INSTANCE,TypeList.EMPTY,name,abstractSyntax);
    }

	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}
	
    /**
     * Crea questo test all'interno del {@code TestClassGenerator}
     * specificato.
     * 
     * @param classGen il {@code TestClassGenerator} da utilizzare.
     */
	
	public void createTest(TestClassGenerator classGen) {
	    MethodGen methodGen = new MethodGen
	            (Constants.ACC_PRIVATE | Constants.ACC_STATIC, // private static
	                    org.apache.bcel.generic.Type.INT, // tipo di ritorno
	                    new org.apache.bcel.generic.Type[] {getDefiningClass().toBCEL()}, // parametro
	                    null, 
	                    getName().toString(), // nome del test
	                    classGen.getClassName(), // classe di appartenenza
	                    classGen.generateJavaBytecode(getCode()), // bytecode 
	                    classGen.getConstantPool()); // constant pool

        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        classGen.addMethod(methodGen.getMethod());
    }
}
