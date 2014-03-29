package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.util.InstructionFinder;

public class ByteCodeOptimizer
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;
	ArrayList<InstructionHandle> gotoList = new ArrayList<InstructionHandle>();


	public ByteCodeOptimizer(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/* 1) GOES THROUGH EACH METHOD */ 
	private void optimize() {
		// load the original class into a class generator
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();
	
		// Do your optimization here
		Method[] methods = cgen.getMethods();
		for (Method m : methods)
		{
			optimiseGoTos(cgen, cpgen, m);
	
		}
	
		// we generate a new class with modifications
		// and store it in a member variable
		this.optimized = cgen.getJavaClass();
	}
	

	/* CHECKS EACH INSTRUCTION HANDLE */
	public Method optimiseGoTos(ClassGen cgen, ConstantPoolGen cpgen, Method method) {
		// Get the Code of the method, which is a collection of bytecode instructions
		Code methodCode = method.getCode();
	
		// Now get the actualy bytecode data in byte array, 
		// and use it to initialise an InstructionList
		InstructionList instList = new InstructionList(methodCode.getCode());
	
		// Initialise a method generator with the original method as the baseline	
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instList, cpgen);
	
		// InstructionHandle is a wrapper for actual Instructions
		
		for (InstructionHandle handle : instList.getInstructionHandles())
		{
			// if the instruction inside is iconst
			if (handle.getInstruction() instanceof GOTO)
			{
				String print = handle.toString();
				// System.out.println("The handle is:" + print); 
			//	System.out.println("Next handle: " + handle.getNext().toString() );
				
				InstructionHandle nextHandle;

				gotoList.add(handle);
				nextHandle = handle.getNext();
				
				
				// int count = 1; // track how many gotos 
				
				
				while (nextHandle.getInstruction() instanceof GOTO ) {
					gotoList.add(nextHandle); // add the goto into the array
					// count++;
					
					System.out.println("Next handle: " + handle.getNext().toString() );
					int lastLineNumber = nextHandle.getPosition(); // get line number of last go to
					InstructionHandle lastInstruction = nextHandle;
					nextHandle = handle.getNext().getNext(); // get the next instruction
					
					
					// Check if next one is a goto, if not update 
					if (!(nextHandle.getInstruction() instanceof GOTO)) {
						for (int i = 0; i <gotoList.size(); i++) {
							System.out.println("Array position at " + i +  " is " + gotoList.get(i)); 
							
						}
						
						System.out.println("Last line number:" + lastLineNumber);
						
						// InstructionHandle toDelete = nextHandle.getPrev();
						
						for (int i = 0; i<=gotoList.size()-1; i++) {
							// if first position, update with last line number
							if (i == 0) {
								instList.insert(gotoList.get(0),  new GOTO(lastInstruction));
								
							} 
							// else delete the rest of the gotos
							else {
								// System.out.println("To delete: "+ toDelete);
								
								
								try {
									for (InstructionHandle handle1 : instList.getInstructionHandles()) {
										System.out.println("All instructions");
										System.out.println(handle.getInstruction() );
									}

									instList.delete(gotoList.get(i));
									// toDelete = toDelete.getPrev();
								} catch (TargetLostException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}	
							}
						}
					}			
				} 
				
				
				/*
				// insert new one with integer 5, and...
				instList.insert(handle, new ICONST(5));
				try
				{
					// delete the old one
					instList.delete(handle);
	
				}
				catch (TargetLostException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				
			}
		}
		
		// setPositions(true) checks whether jump handles 
		// are all within the current method
		instList.setPositions(true);

		// set max stack/local
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// generate the new method with replaced iconst
		Method newMethod = methodGen.getMethod();
		// replace the method in the original class
		cgen.replaceMethod(method, newMethod);
		return newMethod;
		
	}
	
	
	/* Don't need to touch  */
	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		ByteCodeOptimizer optimizer = new ByteCodeOptimizer(args[0]);
		optimizer.write(args[1]);

	}
}