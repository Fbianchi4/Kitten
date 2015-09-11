package types;


import javaBytecodeGenerator.TestClassGenerator;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.MethodGen;

import absyn.FixtureDeclaration;
import translation.Block;

/**
 * La signature di un test di una classe Kitten.
 * 
 * @author Federico Bianchi
 */

public class FixtureSignature extends CodeSignature {

    /**
     * Il contatore utilizzato per assegnare un nome univoco alle fixture (per
     * la creazione di .dot file diversi).
     */
    
    private static int counter = 0;
    
    /**
     * Costruisce la signature di un test.
     * 
     * @param clazz la classe a cui il test appartiene.
     * @param abstractSyntax la sintassi astratta della dichiarazione del test.
     */
    
	public FixtureSignature(ClassType clazz, FixtureDeclaration abstractSyntax) {
		super(clazz,VoidType.INSTANCE,TypeList.EMPTY,"fixture" + (counter++),abstractSyntax);
	}
	
	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}
	
	@Override
	public String toString() {
	    return getDefiningClass() + "." + getName();
	}
	
    /**
     * Crea questa fixture all'interno del {@code TestClassGenerator}
     * specificato.
     * 
     * @param classGen il {@code TestClassGenerator} da utilizzare.
     */
	
	public void createFixture(TestClassGenerator classGen) {
        MethodGen methodGen = new MethodGen
                (Constants.ACC_PRIVATE | Constants.ACC_STATIC, // private static
                        org.apache.bcel.generic.Type.VOID, // tipo di ritorno
                        new org.apache.bcel.generic.Type[] {getDefiningClass().toBCEL()}, // parametro
                        null, 
                        getName().toString(), // nome della fixture
                        classGen.getClassName(), // classe di appartenenza
                        classGen.generateJavaBytecode(getCode()), // bytecode
                        classGen.getConstantPool()); // constant pool

        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        classGen.addMethod(methodGen.getMethod());
    }

}
