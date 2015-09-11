package absyn;

import java.io.FileWriter;
import java.io.IOException;

import semantical.TypeChecker;
import types.ClassType;
import types.FixtureSignature;
import types.VoidType;

/**
 * Questa classe rappresenta una {@code fixture}.
 * 
 * @author Federico Bianchi
 */

public class FixtureDeclaration extends CodeDeclaration {

    /**
     * Costruisce una {@code fixture}.
     * 
     * @param pos la posizione di inizio della {@code fixture} nel file
     *        sorgente.
     * @param body il corpo della {@code fixture}.
     * @param next il prossimo {@code ClassMemberDeclaration}.
     */
    
    public FixtureDeclaration(int pos, Command body, ClassMemberDeclaration next) {
        super(pos, null, body, next);
    }
    
    @Override
    public FixtureSignature getSignature() {
        return (FixtureSignature) super.getSignature();
    }

    @Override
    protected void toDotAux(FileWriter where) throws IOException {
        linkToNode("body", getBody().toDot(where), where);
    }

    @Override
    protected void addTo(ClassType clazz)
    {
        FixtureSignature fSig = new FixtureSignature(clazz, this);
        clazz.addFixture(fSig);

        // we record the signature of this method inside this abstract syntax
        setSignature(fSig); 
    }

    @Override
    protected void typeCheckAux(ClassType clazz) {
        // il type checking controlla che ritorni void
        TypeChecker checker = new TypeChecker(VoidType.INSTANCE, clazz.getErrorMsg());
        
        checker = checker.putVar("this", clazz);
        getBody().typeCheck(checker);
        getBody().checkForDeadcode();
    }

}
