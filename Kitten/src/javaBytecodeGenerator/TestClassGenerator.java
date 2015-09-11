package javaBytecodeGenerator;

import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import types.ClassMemberSignature;
import types.ClassType;
import types.FixtureSignature;
import types.TestSignature;

/**
 * Generatore di bytecode per le classi di tests.
 * 
 * @author Federico Bianchi
 */

@SuppressWarnings("serial")
public class TestClassGenerator extends AbstractClassGenerator {

    private final ClassType clazz;

	/**
	 * Genera un {@code TestClassGenerator} per il tipo specificato.
	 *
	 * @param clazz il tipo
	 * @param sigs a set of class member signatures. These are those that must be
	 *             translated. If this is {@code null}, all class members are translated
	 */

	public TestClassGenerator(ClassType clazz, Set<ClassMemberSignature> sigs) {
		super(clazz.getName() + "Test", // il nome della classe
		    "java.lang.Object", // superclasse
			clazz.getName() + ".kit", // source file
			Constants.ACC_PUBLIC, // public
			noInterfaces, 
			new ConstantPoolGen()); // constant pool

		this.clazz = clazz;
		
		// we add the tests
		for (TestSignature test: clazz.getTests().values())
			if (sigs == null || sigs.contains(test))
				test.createTest(this);
		
		// we add the signatures
		for (FixtureSignature fix: clazz.getFixtures())
		    if (sigs == null || sigs.contains(fix)) 
		        fix.createFixture(this);
		
		generateMainMethod();
	}
	
	/**
	 * Creazione del metodo main della classe di test.
	 */
	
	private void generateMainMethod() {
	    
	    InstructionList i = new InstructionList();
	    
	    // Stampa intestazione per i test
	    i.append(getFactory().createPrintln("Test execution for class " + clazz.getName() + ":"));
	    
	    // Salva nelle variabili locali il numero di test da eseguire
	    i.append(new LDC(getConstantPool().addInteger(clazz.getTests().size())));
	    i.append(InstructionFactory.createStore(
	            Type.INT, 2));
	    
	    // Mette sullo stack l'oggetto System.out per stampare in seguito
	    // (nel metodo printTotalStats)
	    i.append(getFactory().createGetStatic(
                "java/lang/System",
                "out",
                Type.getType(java.io.PrintStream.class)));
	    
	    // Mette sullo stack il contatore dei test passati
	    i.append(InstructionFactory.ICONST_0);
	    
	    // Salva il tempo iniziale nella variabile 3-4
	    invokeCurrentMillis(i);
	    i.append(InstructionFactory.createStore(
	            Type.LONG, 3));
	    
	    i.append(getFactory().createPrintln("\t--------------------------------"));
	    
	    for (TestSignature test : clazz.getTests().values()) {
	        
	        // Crea l'oggetto da passare come parametro a tests e fixtures
	        createObject(i);
	        
	        // Chiama tutte le fixture sull'oggetto creato
	        callFixtures(i);
	        
	        // Salva il tempo iniziale (del test) nella variabile 5-6
	        invokeCurrentMillis(i);
	        i.append(InstructionFactory.createStore(
	                Type.LONG, 5));
	        
	        // Chiama il test sull'oggetto creato
	        callTest(i, test.getName());
	        
	        // Salva il tempo finale (del test) nella variabile 8-9
            invokeCurrentMillis(i);
            i.append(InstructionFactory.createStore(
                    Type.LONG, 8));
	        
	        // Stampa delle statistiche del test
	        printStats(i, test.getName());
	        
	        i.append(getFactory().createPrintln("\t--------------------------------"));
	    }
	    
	    invokeCurrentMillis(i);
        i.append(InstructionFactory.createStore(
                Type.LONG, 5));
	    
	    printTotalStats(i);
	    
	    i.append(InstructionFactory.createReturn(Type.VOID));
	    
	    MethodGen m = new MethodGen(
	            Constants.ACC_PUBLIC | Constants.ACC_STATIC,
	            Type.VOID,
	            new Type[] {new ArrayType("java.lang.String", 1)},
	            null,
	            "main",
	            getClassName(),
	            i,
	            this.getConstantPool());
	    
	    m.setMaxStack();
	    m.setMaxLocals();
	    
	    addMethod(m.getMethod());
	}

    /**
     * Modifica l'{@code InstructionList} aggiungendo le istruzioni di creazione
     * dell'oggetto.
     * 
     * @param i l'{@code InstructionList}.
     */
	
    private void createObject(InstructionList i) {
        
        // Esegue la new
        i.append(getFactory().createNew(clazz.getName()));
        
        // Esegue la dup
        i.append(InstructionFactory.DUP);
        
        // Invocazione del costruttore
        i.append(getFactory().createInvoke(
                clazz.getName(), // nome della classe
                "<init>", // invocazione del costruttore
                Type.VOID, // tipo di ritorno del costruttore
                new Type[]{}, // il costruttore utilizzato non ha parametri
                Constants.INVOKESPECIAL)); // invoke special per il costruttore
    }
    
    /**
     * Crea il codice per chiamare le fixture sull'oggetto creato all'interno
     * dell'{@code InstructionList}.
     * 
     * @param i l'{@code InstructionList} contenente l'oggetto su cui chiamare
     *        le fixture.
     */
    
    private void callFixtures(InstructionList i) {
        
        for (FixtureSignature fix : clazz.getFixtures()) {

            i.append(InstructionFactory.DUP);
            
            i.append(getFactory().createInvoke(
                    clazz.getName() + "Test",
                    fix.getName(),
                    Type.VOID,
                    new Type[]{clazz.toBCEL()},
                    Constants.INVOKESTATIC));
        }
    }
    
    /**
     * Crea il codice per la chiamata di tutti i test.
     * 
     * @param i l'{@code InstructionList} contenente l'oggetto su cui chiamare i
     *        tests.
     */
    
    private void callTest(InstructionList i, String testName) {
        
        i.append(getFactory().createInvoke(
                clazz.getName() + "Test",
                testName,
                Type.INT,
                new Type[] {clazz.toBCEL()},
                Constants.INVOKESTATIC));
        
        // Salva il risultato di ritorno del test prima di incrementare il
        // contatore
        i.append(InstructionFactory.DUP);
        i.append(InstructionFactory.createStore(
                Type.INT, 7));
        
        // Incrementa il contatore
        i.append(InstructionFactory.IADD);
        
        // Rimette sullo stack il risultato di ritorno del test
        i.append(InstructionFactory.createLoad(
                Type.INT, 7));
    }
    
    /**
     * Stampa il risultato del singolo test.
     * 
     * @param l'{@code InstructionList} dove aggiungere le istruzioni.
     * @param testName il nome del test eseguito.
     */
    
    private void printStats(InstructionList i, String testName) {
        
        i.append(invokePrint("\t- " + testName + ": "));
        
        // InstructionList temporaneo contenente le istruzioni per il blocco if
        InstructionList ifList = new InstructionList();
        
        InstructionHandle end = ifList.insert(InstructionFactory.NOP);
        InstructionHandle failed = ifList.insert(invokePrint("failed ["));
        ifList.insert(new GOTO(end));
        ifList.insert(invokePrint("passed ["));
        ifList.insert(new IFEQ(failed));
        
        // Sposta tutte le istruzioni dentro ad i
        i.append(ifList);
        
        // Tempo di esecuzione
        i.append(getFactory().createGetStatic(
                "java/lang/System",
                "out",
                Type.getType(java.io.PrintStream.class)));
        i.append(InstructionFactory.createLoad(
                Type.LONG, 8));
        i.append(InstructionFactory.createLoad(
                Type.LONG, 5));
        i.append(InstructionFactory.LSUB);
        i.append(getFactory().createInvoke(
                "java/io/PrintStream",
                "print",
                Type.VOID,
                new Type[] {Type.LONG},
                Constants.INVOKEVIRTUAL));
        
        i.append(getFactory().createPrintln("ms]"));
    }
    
    /**
     * Stampa il risultato complessivo.
     * 
     * @param l'{@code InstructionList} dove aggiungere le istruzioni.
     */
    
    private void printTotalStats(InstructionList i) {
        
        // Stampa i test passati
        i.append(InstructionFactory.DUP);
        i.append(InstructionFactory.createStore(Type.INT, 7));
        i.append(getFactory().createInvoke(
                "java/io/PrintStream",
                "print",
                Type.VOID,
                new Type[] {Type.INT},
                Constants.INVOKEVIRTUAL));
        
        i.append(invokePrint(" test(s) passed, "));
        
        // Stampa i test falliti
        i.append(getFactory().createGetStatic(
                "java/lang/System",
                "out",
                Type.getType(java.io.PrintStream.class)));
        
        i.append(InstructionFactory.createLoad(Type.INT, 2));
        i.append(InstructionFactory.createLoad(Type.INT, 7));
        i.append(InstructionFactory.ISUB);
        
        i.append(getFactory().createInvoke(
                "java/io/PrintStream",
                "print",
                Type.VOID,
                new Type[] {Type.INT},
                Constants.INVOKEVIRTUAL));
        
        i.append(invokePrint(" failed ["));
        
        // Stampa del tempo totale
        i.append(getFactory().createGetStatic(
                "java/lang/System",
                "out",
                Type.getType(java.io.PrintStream.class)));
        
        i.append(InstructionFactory.createLoad(Type.LONG, 5));
        i.append(InstructionFactory.createLoad(Type.LONG, 3));
        i.append(InstructionFactory.LSUB);
        
        i.append(getFactory().createInvoke(
                "java/io/PrintStream",
                "print",
                Type.VOID,
                new Type[] {Type.LONG},
                Constants.INVOKEVIRTUAL));
        
        i.append(invokePrint("ms]\n"));
    }
    
    /**
     * Aggiunge all'{@code InstructionList} specificato la chiamata a
     * {@code System.currentTimeMillis()}.
     * 
     * @param i l'{@code InstructionList} in cui aggiungere il codice.
     */
    
    private void invokeCurrentMillis(InstructionList i) {

        i.append(getFactory().createInvoke(
                "java/lang/System",
                "currentTimeMillis",
                Type.LONG,
                new Type[] {},
                Constants.INVOKESTATIC));
    }
    
    /**
     * Crea un {@code InstructionList} contenente la chiamata al metodo
     * {@code System.out.print()}.
     * 
     * @param s la stringa da stampare.
     */
    
    private InstructionList invokePrint(String s) {
        
        InstructionList i = new InstructionList();
        
        i.append(getFactory().createGetStatic(
                "java/lang/System",
                "out",
                Type.getType(java.io.PrintStream.class)));
        i.append(new LDC(getConstantPool().addString(s)));
        i.append(getFactory().createInvoke(
                "java/io/PrintStream",
                "print",
                Type.VOID,
                new Type[] {Type.STRING},
                Constants.INVOKEVIRTUAL));
        
        return i;
    }

}