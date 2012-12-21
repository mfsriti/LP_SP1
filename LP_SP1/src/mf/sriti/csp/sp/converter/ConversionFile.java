package mf.sriti.csp.sp.converter;

import java.io.File;
import java.math.BigInteger;
import java.util.Random;

import mf.sriti.csp.sp.util.FileHandler;
import mf.sriti.csp.sp.util.GeneralException;

public class ConversionFile {
	FileHandler mSourceFile = null;
	FileHandler mTargetOldFile = null;
	FileHandler mTargetNewFile = null;
	
	String mProblemName = "";

	int mProblemVariableNumber = 0;
	int mPrefsLiteralNumber = 0;
	int mConstraintsCounter = 0;
	
	public ConversionFile(File file) throws ConversionException, GeneralException{
		ConversionManager cm = ConversionManager.getInstance();
		if (cm==null)
			throw new ConversionException("ConversionFile: ConversionManager singleton is NULL");
		
		ConversionPropertiesHandler cph = cm.getProps();
		if (cph==null)
			throw new ConversionException("ConversionFile: ConversionPropertiesHandler not initialized yet.");
		
		mSourceFile = new FileHandler(file, true);
		mProblemName = file.getName();
		String sourceName = file.getAbsolutePath();
		String targetOldName = sourceName.replace(cph.getSourceDir(), cph.getTargetOldDir());
		String targetNewName = sourceName.replace(cph.getSourceDir(), cph.getTargetNewDir());
		// change file extension for target new
		targetNewName = targetNewName.substring(0,targetNewName.lastIndexOf('.')+1) + LP_FILE_EXT;
		//System.out.println(sourceName);
		//System.out.println(targetOldName);
		//System.out.println(targetNewName);
		mTargetOldFile = new FileHandler(targetOldName, false);
		mTargetNewFile = new FileHandler(targetNewName, false);
	}
	
	public void convert() throws GeneralException{
		String currentLine = null;
		System.out.println("ConversionFile.ConversionFile(): processing file: "+ mProblemName);
		while ((currentLine = mSourceFile.nextLine()) != null ) {
			processLine(currentLine);
		}
		mSourceFile.close();
		mTargetOldFile.close();
		mTargetNewFile.close();
	}
	
	private void processLine(String line) throws ConversionException, GeneralException{
		// for the old target file, we print the following line as they are:
		//  -contains only "c"  -equals to "c preferences"     
		if ( line.equals("c") || line.equals("c preferences") ){
			writeToTargetOld(line);
			// for targetnewfile, nothing to do
		} else if (line.isEmpty()){
			writeToTargetOld(line);
			writeToTargetNew(LP_TERM_BOUNDS);
			writeToTargetNew(generateBoundsSection());
			writeToTargetNew(LP_TERM_INTEGER);
			writeToTargetNew(generateIntegersSection());
		    writeToTargetNew(LP_TERM_END);
		} else{
			switch(line.charAt(0)){
			case 'c':
				if (line.startsWith("c S")) {//we process the line contains variable counters, ex: "c S 200 50"
					String a[]=line.split(" ");
					mProblemVariableNumber = Integer.parseInt(a[2]);
					mPrefsLiteralNumber = Integer.parseInt(a[3]);
					writeToTargetOld(line);
					writeToTargetNew(LP_TERM_PROBLEM);
					writeToTargetNew(" "+mProblemName);
				} else { // here it remains only the list of pref line: "c 84<-29, 135<-29, -169<-185, 82<16, ..."
					if (mProblemVariableNumber<=0 || mPrefsLiteralNumber<=0)
						throw new ConversionException("CobnversionFile.processLine: either problemVariableNumber or prefsLiteralNumber is not set properly");
					// generate randomly prefs literals from problem variables
					int prefsLiterals[] = generatePrefsLiterals(mProblemVariableNumber, mPrefsLiteralNumber);
					
					// generate prefs
					String prefsString = generatePreferences(prefsLiterals);
					// write the prefs string into the file
					writeToTargetOld(prefsString);
					
					// generate objective function
					String objFunction = generateObjFunction(prefsLiterals);
					writeToTargetNew(LP_TERM_MAXIMIZE);
					writeToTargetNew(" "+LP_TERM_OBJ+": "+ objFunction);
				}
				break;
			case 'p': // line.startsWith("p cnf")
				writeToTargetOld(line);
				writeToTargetNew(LP_TERM_SUBJECTTO);
			    break;
			case '-':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				writeToTargetOld(line);
				
				String strConstraint[] = line.split(" ");
				int intConstraint[] = new int [strConstraint.length-1];
				for (int i=0; i<strConstraint.length-1; i++)
					intConstraint[i] = Integer.parseInt(strConstraint[i]);
				
				String constraint = generateConstraints(intConstraint);
				writeToTargetNew(" "+LP_TERM_RULE+ ++mConstraintsCounter + ":  "+constraint);
				break;
			default: ;
				
			}
		}
	}
	
	private void writeToTargetOld(String line) throws GeneralException{
		mTargetOldFile.writeLine(line);
	}
	
	private void writeToTargetNew(String line) throws GeneralException{
		mTargetNewFile.writeLine(line);
	}
	
	private int[] generatePrefsLiterals(int nbVariables, int nbLiterals){
		int variables [] = new int[nbVariables];
		int literals [] = new int[nbLiterals];
		
		for(int i=0; i<nbVariables; i++)
			variables [i]=i+1;
		
		int nbRemainders = nbVariables;
		Random random = new Random();
		for (int i=0; i<nbLiterals; i++){
				int lit = random.nextInt(nbRemainders--);
				literals [i] = variables[lit] * (random.nextBoolean() ? 1 : -1);
				variables[lit] = variables[nbRemainders];
		}
		return literals;
	}
	
	private String generatePreferences(int literals[]){
		String result = "c ";
		for(int i=0; i<literals.length-1; i++){
			result += literals[i] + "<" + literals[i+1] + ", "; 
		}
		result = result.substring(0, result.length() - 2);
		result +=";";
		return result;
	}
	
	private String generateObjFunction(int literals[]){
		String result = "";
		for(int i=literals.length-1; i>=0;i--){
			BigInteger bigint = BigInteger.valueOf(2);
			char sign = '+';
			if (literals[i]<0){
				sign = '-';
				literals[i]*=-1;
			}
			result = " " + sign + " " + bigint.pow((literals.length-1) -i).toString() 
					+ "x" + literals[i] + result;
		}
		if (result.startsWith(" + "))
			result = result.substring(3, result.length());
		else // if (result.startsWith(" - "))
			result = "-" + result.substring(3, result.length());
			
		return result;
	}
	
	private String generateConstraints(int constraint[] ){
		String result = "";
		String space="";
		int negatives = 0;
		
		for (int i=0; i<constraint.length;i++){
			char sign = '+';
			if (constraint[i]<0){
				sign = '-';
				++negatives;
				constraint[i]*=-1;
			}
			result += space + sign + space +"x" + constraint[i] ;
			space=" ";
		}
		
		if (result.startsWith("+"))
			result = result.substring(1, result.length());
		result += ">=" + (1 - negatives);
		return result;
	}
	
	private String generateBoundsSection(){
		String result = "";
		for (int i=1; i <= mProblemVariableNumber; i++)
			result += " 0 <= " + "x" + i + " <= 1\n";
		result = result.substring(0, result.length()-1);
		return result;
	}
	
	private String generateIntegersSection(){
		String result = "";
		for (int i=1; i<=mProblemVariableNumber; i++)
			result += " x" + i;
		return result;
	}

	private final static String LP_TERM_PROBLEM = "Problem";
	private final static String LP_TERM_MAXIMIZE = "Maximize";
	private final static String LP_TERM_OBJ = "obj";
	private final static String LP_TERM_SUBJECTTO = "Subject To";
	private final static String LP_TERM_RULE = "r";
	private final static String LP_TERM_INTEGER = "Integer";
	private final static String LP_TERM_BOUNDS = "Bounds";
	private final static String LP_TERM_END = "End";
	private final static String LP_FILE_EXT = "lp";
	
	
}
