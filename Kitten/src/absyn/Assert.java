package absyn;

import java.io.FileWriter;

import bytecode.CONST;
import bytecode.NEWSTRING;
import bytecode.RETURN;
import bytecode.VIRTUALCALL;
import semantical.TypeChecker;
import translation.Block;
import types.ClassType;
import types.CodeSignature;
import types.IntType;
import types.MethodSignature;
import types.TypeList;

/**
 * Questa classe rappresenta il comando {@code assert} utilizzato all'interno
 * dei tests per verificare una determinata condizione booleana.
 * 
 * @author Federico Bianchi
 */

public class Assert extends Command {

    /**
     * La condizione che deve essere verificata.
     */
    
    private final Expression asserted;
    
    /**
     * Il messaggio di errore che viene stampato a video nel caso in cui la
     * condizione sia falsa.
     */
    
    private String failureMessage;

    
    /**
     * Costruisce un nodo della sintassi astratta che rappresenta un comando
     * {@code assert}.
     * 
     * @param pos la posizione del comando {@code assert} all'interno del file
     *        sorgente
     * @param condition la condizione che deve essere verificata.
     */
    
    public Assert(int pos, Expression condition) {
        super(pos);

        this.asserted = condition;
    }
    
    /**
     * Ritorna la condizione che deve essere verificata.
     * 
     * @return la condizione che deve essere verificata.
     */
    
    public Expression getCondition() {
        return asserted;
    }

    @Override
    protected void toDotAux(FileWriter where) throws java.io.IOException {
        linkToNode("returned", asserted.toDot(where), where);
    }
    
    @Override
    protected TypeChecker typeCheckAux(TypeChecker checker) {
        
        // Verifica che l'assert incontrato possa essere utilizzato in quel
        // punto nel codice (ovvero solo dentro ai tests)
    	if (!checker.isAssertAllowed())
    		error(checker, "assert not allowed here");
    	
    	// La condizione di un assert deve avere tipo booleano
        asserted.mustBeBoolean(checker);
        
        // Costruisce la stringa di errore che verrà utilizzata a runtime nel
        // caso in cui il test fallisca
        failureMessage = buildErrorMsg(checker, "\t> assert failed") + "\n";
        
        return checker;
    }

    @Override
    public boolean checkForDeadcode() {
        // Non può esserci deadcode all'interno di un assert
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Un comando {@code assert} viene tradotto in Kitten come un comando di
     * test:
     * <ul>
     * <li>Se la condizione da verificare vale {@code true} a runtime,
     * l'esecuzione procede con l'istruzione successiva all'{@code assert}
     * (ovvero si procede con il blocco {@code continuation});
     * <li>Altrimenti, viene stampata la stringa {@link #failureMessage} e
     * l'esecuzione del test si interrompe ritornando l'intero {@code 0}.
     * 
     * @return il codice di questo comando tradotto in Kitten bytecode e la sua
     *         continuazione.
     */
    
    @Override
    public Block translate(CodeSignature where, Block continuation) {
        
        // Ottiene il metodo output della classe String e la classe String
        ClassType s = ClassType.mk("String");
        MethodSignature output = s.methodLookup("output", TypeList.EMPTY);
        
        NEWSTRING ns = new NEWSTRING(failureMessage);
        VIRTUALCALL vc = new VIRTUALCALL(s, output);
        CONST c = new CONST(0);
        RETURN r = new RETURN(IntType.INSTANCE);
        
        // Creazione del blocco da eseguire nel caso in cui l'assert fallisce
        Block error = new Block(r);
        error = c.followedBy(error);
        error = vc.followedBy(error);
        error = ns.followedBy(error);
        
        return asserted.translateAsTest(where, continuation, error);
    }

}
