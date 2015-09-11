package absyn;

import java.io.FileWriter;
import java.io.IOException;

import semantical.TypeChecker;
import types.ClassType;
import types.TestSignature;
import types.VoidType;

/**
 * Questa classe rappresenta un {@code test}.
 * 
 * @author Federico Bianchi
 */

public class TestDeclaration extends CodeDeclaration {

    /**
     * Il nome del test.
     */
    
    private final String name;

    /**
     * Costruisce un {@code test}.
     * 
     * @param pos la posizione di inizio del {@code test} nel file
     *        sorgente.
     * @param name il nome del test.
     * @param body il corpo del {@code test}.
     * @param next il prossimo {@code ClassMemberDeclaration}.
     */
    
    public TestDeclaration(int pos, String name, Command body, ClassMemberDeclaration next) {
    	super(pos, null, body, next);
        
        this.name = name;
    }
    
    /**
     * Ritorna il nome del test.
     * 
     * @return il nome del test.
     */
    
    public String getName() {
        return name;
    }
    
    @Override
    public TestSignature getSignature() {
        return (TestSignature) super.getSignature();
    }
    
    @Override
    protected void toDotAux(FileWriter where) throws IOException {
        linkToNode("name", toDot(name, where), where);
        linkToNode("body", getBody().toDot(where), where);
    }

    @Override
    protected void addTo(ClassType clazz) {        
    	TestSignature tSig = new TestSignature(clazz, name, this);
        clazz.addTest(name, tSig);

        // we record the signature of this method inside this abstract syntax
        setSignature(tSig); 
    }

    @Override
    protected void typeCheckAux(ClassType clazz) {
        TypeChecker checker = new TypeChecker(VoidType.INSTANCE, clazz.getErrorMsg(), true);
        
        checker = checker.putVar("this", clazz);
        getBody().typeCheck(checker);
        getBody().checkForDeadcode();
    }

}
