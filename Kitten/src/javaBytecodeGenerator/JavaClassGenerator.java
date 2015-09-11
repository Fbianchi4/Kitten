package javaBytecodeGenerator;

import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ConstantPoolGen;

import types.ClassMemberSignature;
import types.ClassType;
import types.ConstructorSignature;
import types.FieldSignature;
import types.MethodSignature;

/**
 * A Java bytecode generator. It transforms the Kitten intermediate language
 * into Java bytecode that can be dumped to Java class files and run.
 * It uses the BCEL library to represent Java classes and dump them on the file-system.
 *
 * @author <A HREF="mailto:fausto.spoto@univr.it">Fausto Spoto</A>
 */

@SuppressWarnings("serial")
public class JavaClassGenerator extends AbstractClassGenerator {

	/**
	 * Builds a class generator for the given class type.
	 *
	 * @param clazz the class type
	 * @param sigs a set of class member signatures. These are those that must be
	 *             translated. If this is {@code null}, all class members are translated
	 */

	public JavaClassGenerator(ClassType clazz, Set<ClassMemberSignature> sigs) {
		super(clazz.getName(), // name of the class
			// the superclass of the Kitten Object class is set to be the Java java.lang.Object class
			clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "java.lang.Object",
			clazz.getName() + ".kit", // source file
			Constants.ACC_PUBLIC, // Java attributes: public!
			noInterfaces, // no interfaces
			new ConstantPoolGen()); // empty constant pool, at the beginning

		// we add the fields
		for (FieldSignature field: clazz.getFields().values())
			if (sigs == null || sigs.contains(field))
				field.createField(this);

		// we add the constructors
		for (ConstructorSignature constructor: clazz.getConstructors())
			if (sigs == null || sigs.contains(constructor))
				constructor.createConstructor(this);

		// we add the methods
		for (Set<MethodSignature> s: clazz.getMethods().values())
			for (MethodSignature method: s)
				if (sigs == null || sigs.contains(method))
					method.createMethod(this);
	}

}