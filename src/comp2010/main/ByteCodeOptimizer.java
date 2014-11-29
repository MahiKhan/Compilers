package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;

public class ByteCodeOptimizer
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

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

	private void optimize()
	{
        ClassGen gen = new ClassGen(original);
		// Do your optimization here

        ConstantPoolGen cp = gen.getConstantPool(); 
        Method[] methodsList = gen.getMethods();

        for(int i=0;i<methodsList.length;i++)
        {
            MethodGen method = new MethodGen(methodsList[i],gen.getClassName(),cp);
            int checkChange = method.getInstructionList().getLength();
            
            while(true)
            {
            	methodsList[i] = optimiseGoTos(gen, cp, method); //
                
                // Each time, optimiseGoTos reduces the amount of GoTos in method
            	// Eventually it'll have done all the changes, and so the amount of instructions in the list will be the same as the original checkChange variable
            	// Hence no more changes to be made, and so break out of the loop 
                if(method.getInstructionList().getLength() == checkChange)
                    break;
                else
                	checkChange = method.getInstructionList().getLength();
            }
        }

        gen.setMethods(methodsList); // update class's methods 
		this.optimized = gen.getJavaClass();
	}

	/* 
	 * This function goes through each method, and optimises any duplicate gotos it has 
	 * Copies any duplicates extra gotos to the first goto, and then deletes the extra 
	 * Takes structure from five.java file + BCEL Manual 
	 */
    private Method optimiseGoTos(ClassGen gen, ConstantPoolGen cpgen, MethodGen method) 
    {
        InstructionList instructionList = method.getInstructionList();

        InstructionFinder instructionFinder = new InstructionFinder(instructionList);
        for(Iterator iter = instructionFinder.search("GOTO GOTO"); iter.hasNext();) // find a goto pointing to another goto
        {
            InstructionHandle[] extraGoToList = (InstructionHandle[]) iter.next();
            InstructionHandle original = extraGoToList[0];
            original.setInstruction(extraGoToList[1].getInstruction().copy()); // copy the extra goto to the original 
           
            try
            {
            	instructionList.delete(extraGoToList[1]); // delete the extra 
            }
            
            // Taken from BCEL Manual, when there when there are instruction targeters still referencing one of the deleted instructions
            // Will throw TargetLostException, where we deal with it by redirecting these references elsewhere using this code 
            catch(TargetLostException e)
            {
                InstructionHandle[] targets = e.getTargets();
                for(int i=0;i<targets.length;i++)
                {
                    InstructionTargeter[] targeters = targets[i].getTargeters();
                    for(int j=0;j<targeters.length;j++)
                    {
                        targeters[j].updateTarget(targets[i],null);
                    }
                }
            }
        }

        method.setInstructionList(instructionList); // update method's instructions with optimised gotos 

        return method.getMethod();
    }
    
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